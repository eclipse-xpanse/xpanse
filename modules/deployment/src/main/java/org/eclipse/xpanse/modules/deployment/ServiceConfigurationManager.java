/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.resource.DeployResourceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.serviceconfiguration.update.ServiceConfigurationUpdateRequest;
import org.eclipse.xpanse.modules.database.serviceconfiguration.update.ServiceConfigurationUpdateRequestQueryModel;
import org.eclipse.xpanse.modules.database.serviceconfiguration.update.ServiceConfigurationUpdateStorage;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderStorage;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.database.utils.EntityTransUtils;
import org.eclipse.xpanse.modules.logging.CustomRequestIdGenerator;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.models.service.utils.ServiceConfigurationVariablesJsonSchemaGenerator;
import org.eclipse.xpanse.modules.models.service.utils.ServiceConfigurationVariablesJsonSchemaValidator;
import org.eclipse.xpanse.modules.models.serviceconfiguration.AnsibleHostInfo;
import org.eclipse.xpanse.modules.models.serviceconfiguration.ServiceConfigurationChangeRequest;
import org.eclipse.xpanse.modules.models.serviceconfiguration.ServiceConfigurationChangeResult;
import org.eclipse.xpanse.modules.models.serviceconfiguration.ServiceConfigurationUpdate;
import org.eclipse.xpanse.modules.models.serviceconfiguration.enums.ServiceConfigurationStatus;
import org.eclipse.xpanse.modules.models.serviceconfiguration.exceptions.ServiceConfigurationInvalidException;
import org.eclipse.xpanse.modules.models.serviceconfiguration.exceptions.ServiceConfigurationUpdateRequestNotFoundException;
import org.eclipse.xpanse.modules.models.servicetemplate.ConfigManageScript;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceConfigurationManage;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceConfigurationParameter;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotRegistered;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.JsonObjectSchema;
import org.eclipse.xpanse.modules.security.UserServiceHelper;
import org.hibernate.exception.LockTimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Bean to manage service configuration.
 */
@Slf4j
@Component
public class ServiceConfigurationManager {

    private static final String IP = "ip";
    private static final String HOSTS = "hosts";

    @Resource
    private DeployServiceEntityHandler deployServiceEntityHandler;

    @Resource
    private ServiceConfigurationUpdateStorage serviceConfigurationUpdateStorage;

    @Resource
    private ServiceTemplateStorage serviceTemplateStorage;

    @Resource
    private DeployService deployService;

    @Resource
    private ServiceOrderStorage serviceOrderStorage;

    @Resource
    private UserServiceHelper userServiceHelper;

    @Resource
    private ServiceConfigurationVariablesJsonSchemaValidator
            serviceConfigurationVariablesJsonSchemaValidator;

    @Resource
    private ServiceConfigurationVariablesJsonSchemaGenerator
            serviceConfigurationVariablesJsonSchemaGenerator;

    /**
     * update ServiceConfiguration.
     *
     * @param serviceId           The id of the deployed service.
     * @param configurationUpdate serviceConfigurationUpdate.
     */
    public ServiceOrder changeServiceConfiguration(String serviceId,
                                                   ServiceConfigurationUpdate configurationUpdate) {
        if (CollectionUtils.isEmpty(configurationUpdate.getConfiguration())) {
            throw new IllegalArgumentException("Parameter ServiceConfigurationUpdate is empty");
        }
        try {
            DeployServiceEntity deployServiceEntity =
                    deployServiceEntityHandler.getDeployServiceEntity(UUID.fromString(serviceId));
            ServiceTemplateEntity serviceTemplateEntity = serviceTemplateStorage
                    .getServiceTemplateById(deployServiceEntity.getServiceTemplateId());
            if (Objects.isNull(serviceTemplateEntity)) {
                String errMsg = String.format("Service template with id %s not found.",
                        deployServiceEntity.getServiceTemplateId());
                log.error(errMsg);
                throw new ServiceTemplateNotRegistered(errMsg);
            }
            validate(serviceTemplateEntity, configurationUpdate);
            UUID orderId = CustomRequestIdGenerator.generateOrderId();
            addServiceConfigurationUpdateRequests(orderId, serviceId, deployServiceEntity,
                    serviceTemplateEntity.getOcl(), configurationUpdate.getConfiguration());
            return new ServiceOrder(orderId, UUID.fromString(serviceId));
        } catch (ServiceConfigurationInvalidException e) {
            String errorMsg =
                    String.format("Change service configuration error, %s", e.getMessage());
            log.error(errorMsg);
            throw new ServiceConfigurationInvalidException(List.of(errorMsg));
        }
    }

    /**
     * Query service configuration update request by queryModel.
     */
    public List<ServiceConfigurationUpdateRequest> listServiceConfigurationUpdateRequest(
            String orderId, String serviceId, String resourceName, String configManager,
            ServiceConfigurationStatus status) {
        ServiceConfigurationUpdateRequestQueryModel queryModel =
                new ServiceConfigurationUpdateRequestQueryModel(UUID.fromString(orderId),
                        UUID.fromString(serviceId), resourceName, configManager, status);
        return serviceConfigurationUpdateStorage.listServiceConfigurationUpdateRequests(queryModel);
    }


    private void addServiceConfigurationUpdateRequests(UUID orderId, String serviceId,
                                                       DeployServiceEntity deployServiceEntity,
                                                       Ocl ocl,
                                                       Map<String, Object> updateRequestMap) {

        List<DeployResource> deployResources =
                deployService.listResourcesOfDeployedService(UUID.fromString(serviceId),
                        DeployResourceKind.VM);
        Map<String, List<DeployResource>> deployResourceMap =
                deployResources.stream()
                .collect(Collectors.groupingBy(DeployResource::getGroupName));

        List<ConfigManageScript> configManageScripts =
                ocl.getServiceConfigurationManage().getConfigManageScripts();
        List<ServiceConfigurationParameter> configurationParameters =
                ocl.getServiceConfigurationManage().getConfigurationParameters();

        List<ServiceConfigurationUpdateRequest> requests = new ArrayList<>();
        deployResourceMap.forEach((groupName, deployResourceList) -> {
            configManageScripts.forEach(configManageScript -> {
                if (configManageScript.getConfigManager().equals(groupName)) {
                    if (!CollectionUtils.isEmpty(deployResourceList)) {
                        Map<String, Object> properties =
                                getServiceConfigurationUpdateProperties(groupName,
                                        configurationParameters, updateRequestMap);
                        if (configManageScript.getRunOnlyOnce()) {
                            ServiceConfigurationUpdateRequest request =
                                    getServiceConfigurationUpdateRequest(orderId, groupName,
                                            deployServiceEntity, properties);
                            requests.add(request);
                        } else {
                            deployResourceList.forEach(deployResource -> {
                                ServiceConfigurationUpdateRequest request =
                                        getServiceConfigurationUpdateRequest(orderId, groupName,
                                                deployServiceEntity, properties);
                                request.setResourceName(deployResource.getResourceName());
                                requests.add(request);
                            });

                        }
                    }
                }
            });
        });

        if (!CollectionUtils.isEmpty(requests)) {
            serviceConfigurationUpdateStorage.saveAll(requests);
        }
    }

    private Map<String, Object> getServiceConfigurationUpdateProperties(String groupName,
            List<ServiceConfigurationParameter> params, Map<String, Object> updateRequestMap) {

        Map<String, Object> existsServiceConfig = new HashMap<>();
        params.forEach(serviceConfigurationParameter -> {
            if (groupName.equals(serviceConfigurationParameter.getManagedBy())) {
                existsServiceConfig.put(serviceConfigurationParameter.getName(),
                        serviceConfigurationParameter.getInitialValue());
            }
        });
        updateRequestMap.forEach((k, v) -> {
            if (existsServiceConfig.containsKey(k)) {
                existsServiceConfig.put(k, v);
            }
        });
        return existsServiceConfig;
    }

    private ServiceConfigurationUpdateRequest getServiceConfigurationUpdateRequest(UUID orderId,
            String groupName, DeployServiceEntity entity, Map<String, Object> properties) {

        ServiceConfigurationUpdateRequest request = new ServiceConfigurationUpdateRequest();
        request.setId(UUID.randomUUID());
        ServiceOrderEntity serviceOrderEntity = saveServiceOrder(orderId,
                entity.getId());
        request.setServiceOrderEntity(serviceOrderEntity);
        request.setDeployServiceEntity(entity);
        request.setConfigManager(groupName);
        request.setProperties(properties);
        request.setStatus(ServiceConfigurationStatus.PENDING);
        return request;
    }

    private ServiceOrderEntity saveServiceOrder(UUID orderId, UUID serviceId) {
        ServiceOrderEntity serviceOrderEntity = new ServiceOrderEntity();
        serviceOrderEntity.setOrderId(orderId);
        serviceOrderEntity.setServiceId(serviceId);
        serviceOrderEntity.setTaskType(ServiceOrderType.SERVICE_CONFIGURATION_UPDATE);
        serviceOrderEntity.setUserId(userServiceHelper.getCurrentUserId());
        serviceOrderEntity.setTaskStatus(TaskStatus.CREATED);
        serviceOrderEntity.setStartedTime(OffsetDateTime.now());
        return serviceOrderStorage.storeAndFlush(serviceOrderEntity);
    }

    private void validate(ServiceTemplateEntity serviceTemplateEntity,
                          ServiceConfigurationUpdate serviceConfigurationUpdate) {
        ServiceConfigurationManage serviceConfigurationManage =
                serviceTemplateEntity.getOcl().getServiceConfigurationManage();
        if (Objects.isNull(serviceConfigurationManage)) {
            String errorMsg =
                    String.format("Service template %s has no service configuration manage",
                            serviceTemplateEntity.getId());
            log.error(errorMsg);
            throw new ServiceConfigurationInvalidException(List.of(errorMsg));
        }
        List<ServiceConfigurationParameter> configurationParameters = serviceConfigurationManage
                .getConfigurationParameters();
        JsonObjectSchema jsonObjectSchema = serviceConfigurationVariablesJsonSchemaGenerator
                .buildServiceConfigurationJsonSchema(configurationParameters);
        serviceConfigurationVariablesJsonSchemaValidator.validateServiceConfiguration(
                configurationParameters, serviceConfigurationUpdate.getConfiguration(),
                jsonObjectSchema);
    }

    /**
     * Query pending configuration change request for agent.
     */
    @Transactional
    public ResponseEntity<ServiceConfigurationChangeRequest>
            getPendingConfigurationChangeRequest(String serviceId, String resourceName) {
        try {
            ServiceConfigurationUpdateRequest oldestRequest =
                    getOldestServiceConfigurationChangeRequest(serviceId, resourceName);
            if (Objects.isNull(oldestRequest)) {
                return ResponseEntity
                        .status(HttpStatus.NO_CONTENT)
                        .body(null);
            }
            List<DeployResource> deployResources =
                    getDeployResources(UUID.fromString(serviceId), DeployResourceKind.VM);
            if (Objects.isNull(oldestRequest.getResourceName())) {
                validateConfigManager(serviceId, oldestRequest.getConfigManager(),
                        resourceName, deployResources);
            }
            ServiceConfigurationUpdateRequest request =
                    updateServiceConfigurationUpdateRequest(oldestRequest, resourceName);
            if (Objects.isNull(request)) {
                return ResponseEntity
                        .status(HttpStatus.NO_CONTENT)
                        .body(null);
            }
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(getServiceConfigurationChangeRequest(request, deployResources));
        } catch (LockTimeoutException e) {
            log.error("Update service configuration update request object timed out", e);
            return ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .body(null);
        }
    }

    private ServiceConfigurationUpdateRequest updateServiceConfigurationUpdateRequest(
            ServiceConfigurationUpdateRequest request, String resourceName) {
        if (Objects.isNull(request.getResourceName())) {
            request.setResourceName(resourceName);
        }
        request.setStatus(ServiceConfigurationStatus.PROCESSING);
        return serviceConfigurationUpdateStorage.storeAndFlush(request);
    }

    private ServiceConfigurationUpdateRequest
            getOldestServiceConfigurationChangeRequest(String serviceId, String resourceName) {
        ServiceConfigurationUpdateRequestQueryModel model =
                new ServiceConfigurationUpdateRequestQueryModel(null, UUID.fromString(serviceId),
                        null, null, ServiceConfigurationStatus.PENDING);
        List<ServiceConfigurationUpdateRequest> requests =
                serviceConfigurationUpdateStorage.listServiceConfigurationUpdateRequests(model);
        if (CollectionUtils.isEmpty(requests)) {
            return null;
        }
        Optional<ServiceConfigurationUpdateRequest> oldestRequestOptional = requests.stream()
                .filter(request -> Objects.isNull(request.getResourceName())
                        || resourceName.equals(request.getResourceName()))
                .filter(request -> request.getServiceOrderEntity() != null
                        && request.getServiceOrderEntity().getStartedTime() != null)
                .min(Comparator.comparing(request ->
                        request.getServiceOrderEntity().getStartedTime()));
        return oldestRequestOptional.orElse(null);
    }

    private void validateConfigManager(String serviceId, String configManager,
                                       String resourceName, List<DeployResource> deployResources) {
        Map<String, List<DeployResource>> resourceNameMap =
                deployResources.stream()
                        .collect(Collectors.groupingBy(DeployResource::getResourceName));
        if (!resourceNameMap.containsKey(resourceName)) {
            String errorMsg = String.format("The service with serviceId %s does not contain "
                    + "a resource with resource_name %s", serviceId, resourceName);
            log.error(errorMsg);
            throw new ServiceConfigurationInvalidException(List.of(errorMsg));
        }
        deployResources.forEach(deployResource -> {
            if (resourceName.equals(deployResource.getResourceName())) {
                if (!configManager.equals(deployResource.getGroupName())) {
                    String errorMsg = String.format("The service with serviceId %s does"
                            + " not contain a group with resource_name %s "
                            + "and  group_name %s", serviceId, resourceName, configManager);
                    log.error(errorMsg);
                    throw new ServiceConfigurationInvalidException(List.of(errorMsg));
                }
            }
        });
    }

    private ServiceConfigurationChangeRequest
            getServiceConfigurationChangeRequest(ServiceConfigurationUpdateRequest request,
                                                 List<DeployResource> deployResources) {
        ServiceConfigurationChangeRequest serviceConfigurationChangeRequest =
                new ServiceConfigurationChangeRequest();
        serviceConfigurationChangeRequest.setChangeId(request.getId());
        Optional<ConfigManageScript> configManageScriptOptional = getConfigManageScript(request);
        configManageScriptOptional.ifPresent(configManageScript ->
                serviceConfigurationChangeRequest.setAnsibleScriptConfig(
                        configManageScript.getAnsibleScriptConfig()));
        serviceConfigurationChangeRequest.setConfigParameters(request.getProperties());
        serviceConfigurationChangeRequest.setAnsibleInventory(getAnsibleInventory(deployResources));
        return serviceConfigurationChangeRequest;
    }


    private Optional<ConfigManageScript>
            getConfigManageScript(ServiceConfigurationUpdateRequest request) {
        ServiceTemplateEntity serviceTemplateEntity =
                serviceTemplateStorage.getServiceTemplateById(request
                        .getDeployServiceEntity().getServiceTemplateId());
        if (Objects.isNull(serviceTemplateEntity)) {
            String errMsg = String.format("Service template with id %s not found.",
                    request.getDeployServiceEntity().getServiceTemplateId());
            log.error(errMsg);
            throw new ServiceTemplateNotRegistered(errMsg);
        }
        Optional<ConfigManageScript> configManageScriptOptional =
                serviceTemplateEntity.getOcl().getServiceConfigurationManage()
                        .getConfigManageScripts().stream()
                        .filter(configManageScript -> {
                            String configManager = request.getConfigManager();
                            return configManager != null
                                    && configManager.equals(configManageScript.getConfigManager());
                        }).findFirst();
        return configManageScriptOptional;
    }

    private Map<String, Object> getAnsibleInventory(List<DeployResource> deployResources) {
        Map<String, List<DeployResource>> deployResourceMap =
                deployResources.stream()
                        .collect(Collectors.groupingBy(DeployResource::getGroupName));
        Map<String, Object> ansibleInventory = new HashMap<>();
        deployResourceMap.forEach((groupName, deployResourceList) -> {
            Map<String, AnsibleHostInfo> hosts = new HashMap<>();
            deployResourceList.forEach(deployResource -> {
                AnsibleHostInfo ansibleHostInfo = new AnsibleHostInfo();
                ansibleHostInfo.setAnsibleHost(deployResource.getProperties().get(IP));
                hosts.put(deployResource.getResourceName(), ansibleHostInfo);
            });
            Map<String, Map<String, AnsibleHostInfo>> resourceMap = new HashMap<>();
            resourceMap.put(HOSTS, hosts);
            ansibleInventory.put(groupName, resourceMap);
        });
        return ansibleInventory;
    }

    private List<DeployResource> getDeployResources(UUID serviceId,
                                                    DeployResourceKind resourceKind) {
        DeployServiceEntity deployedService =
                deployServiceEntityHandler.getDeployServiceEntity(serviceId);
        Stream<DeployResourceEntity> resourceEntities =
                deployedService.getDeployResourceList().stream();
        if (Objects.nonNull(resourceKind)) {
            resourceEntities = resourceEntities.filter(
                    resourceEntity -> resourceEntity.getResourceKind().equals(resourceKind));
        }
        return EntityTransUtils.transToDeployResourceList(resourceEntities.toList());
    }

    /**
     * Method to update service configuration update result.
     *
     * @param changeId id of the update request.
     * @param result   result of the service configuration update request.
     */
    public void updateConfigurationChangeResult(String changeId,
                                                ServiceConfigurationChangeResult result) {
        ServiceConfigurationUpdateRequest request =
                serviceConfigurationUpdateStorage.findById(UUID.fromString(changeId));
        if (Objects.isNull(request)
                || ServiceConfigurationStatus.PROCESSING.equals(request.getStatus())) {
            String errorMsg = String.format(
                    "Service Configuration update request with id %s , status %s not found",
                    changeId, ServiceConfigurationStatus.PROCESSING);
            log.error(errorMsg);
            throw new ServiceConfigurationUpdateRequestNotFoundException(errorMsg);
        }
        if (result.getIsSuccessful()) {
            request.setStatus(ServiceConfigurationStatus.SUCCESSFUL);
        } else {
            request.setStatus(ServiceConfigurationStatus.ERROR);
            request.setResultMessage(result.getError());
        }
        serviceConfigurationUpdateStorage.storeAndFlush(request);
    }

}
