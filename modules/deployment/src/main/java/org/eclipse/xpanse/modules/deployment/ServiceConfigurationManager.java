/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
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
import org.eclipse.xpanse.modules.database.resource.ServiceResourceEntity;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.serviceconfiguration.ServiceConfigurationEntity;
import org.eclipse.xpanse.modules.database.serviceconfiguration.ServiceConfigurationStorage;
import org.eclipse.xpanse.modules.database.serviceconfiguration.update.ServiceChangeDetailsEntity;
import org.eclipse.xpanse.modules.database.serviceconfiguration.update.ServiceChangeDetailsQueryModel;
import org.eclipse.xpanse.modules.database.serviceconfiguration.update.ServiceChangeDetailsStorage;
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
import org.eclipse.xpanse.modules.models.service.order.exceptions.ServiceOrderNotFound;
import org.eclipse.xpanse.modules.models.service.utils.ServiceConfigurationVariablesJsonSchemaGenerator;
import org.eclipse.xpanse.modules.models.service.utils.ServiceConfigurationVariablesJsonSchemaValidator;
import org.eclipse.xpanse.modules.models.serviceconfiguration.AnsibleHostInfo;
import org.eclipse.xpanse.modules.models.serviceconfiguration.ServiceChangeRequest;
import org.eclipse.xpanse.modules.models.serviceconfiguration.ServiceConfigurationChangeResult;
import org.eclipse.xpanse.modules.models.serviceconfiguration.ServiceConfigurationDetails;
import org.eclipse.xpanse.modules.models.serviceconfiguration.ServiceConfigurationUpdate;
import org.eclipse.xpanse.modules.models.serviceconfiguration.enums.ServiceChangeStatus;
import org.eclipse.xpanse.modules.models.serviceconfiguration.exceptions.ServiceChangeDetailsEntityNotFoundException;
import org.eclipse.xpanse.modules.models.serviceconfiguration.exceptions.ServiceConfigurationInvalidException;
import org.eclipse.xpanse.modules.models.serviceconfiguration.exceptions.ServiceConfigurationNotFoundException;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceChangeManage;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceChangeParameter;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceChangeScript;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotRegistered;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.JsonObjectSchema;
import org.hibernate.exception.LockTimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** Bean to manage service configuration. */
@Slf4j
@Component
public class ServiceConfigurationManager {

    private static final String IP = "ip";
    private static final String HOSTS = "hosts";

    @Resource private ServiceDeploymentEntityHandler serviceDeploymentEntityHandler;

    @Resource private ServiceChangeDetailsStorage serviceChangeDetailsStorage;

    @Resource private ServiceConfigurationStorage serviceConfigurationStorage;

    @Resource private ServiceTemplateStorage serviceTemplateStorage;

    @Resource private DeployService deployService;

    @Resource private ServiceOrderStorage serviceOrderStorage;

    @Resource private ServiceChangeDetailsManager serviceChangeDetailsManager;

    @Resource
    private ServiceConfigurationVariablesJsonSchemaValidator
            serviceConfigurationVariablesJsonSchemaValidator;

    @Resource
    private ServiceConfigurationVariablesJsonSchemaGenerator
            serviceConfigurationVariablesJsonSchemaGenerator;

    /**
     * Query the service's current configuration by id of the deployed service.
     *
     * @param serviceId id of the deployed service
     * @return ServiceConfigurationEntity.
     */
    public ServiceConfigurationDetails getCurrentConfigurationOfService(UUID serviceId) {
        ServiceConfigurationEntity entity =
                serviceConfigurationStorage.findServiceConfigurationById(serviceId);
        if (Objects.isNull(entity)) {
            String errorMsg =
                    String.format("Service Configuration with service id %s not found.", serviceId);
            log.error(errorMsg);
            throw new ServiceConfigurationNotFoundException(errorMsg);
        }
        return EntityTransUtils.transToServiceConfigurationDetails(entity);
    }

    /**
     * update ServiceConfiguration.
     *
     * @param serviceId The id of the deployed service.
     * @param configurationUpdate serviceConfigurationUpdate.
     */
    @Transactional
    public ServiceOrder changeServiceConfiguration(
            UUID serviceId, ServiceConfigurationUpdate configurationUpdate) {
        try {
            ServiceDeploymentEntity serviceDeploymentEntity =
                    serviceDeploymentEntityHandler.getServiceDeploymentEntity(serviceId);
            ServiceTemplateEntity serviceTemplateEntity =
                    serviceTemplateStorage.getServiceTemplateById(
                            serviceDeploymentEntity.getServiceTemplateId());
            if (Objects.isNull(serviceTemplateEntity)) {
                String errMsg =
                        String.format(
                                "Service template with id %s not found.",
                                serviceDeploymentEntity.getServiceTemplateId());
                log.error(errMsg);
                throw new ServiceTemplateNotRegistered(errMsg);
            }
            validate(serviceTemplateEntity, configurationUpdate);
            UUID orderId = CustomRequestIdGenerator.generateOrderId();
            addServiceChangeDetails(
                    orderId,
                    serviceId,
                    serviceDeploymentEntity,
                    serviceTemplateEntity.getOcl(),
                    configurationUpdate.getConfiguration());
            return new ServiceOrder(orderId, serviceId);
        } catch (ServiceConfigurationInvalidException e) {
            String errorMsg =
                    String.format("Change service configuration error, %s", e.getErrorReasons());
            log.error(errorMsg);
            throw e;
        }
    }

    private void addServiceChangeDetails(
            UUID orderId,
            UUID serviceId,
            ServiceDeploymentEntity serviceDeployment,
            Ocl ocl,
            Map<String, Object> updateRequestMap) {
        Map<String, List<DeployResource>> deployResourceMap =
                deployService
                        .listResourcesOfDeployedService(serviceId, DeployResourceKind.VM)
                        .stream()
                        .collect(Collectors.groupingBy(DeployResource::getGroupName));

        ServiceChangeManage configManage = ocl.getServiceConfigurationManage();
        List<ServiceChangeScript> configManageScripts = configManage.getConfigManageScripts();
        List<ServiceChangeParameter> configurationParameters =
                configManage.getConfigurationParameters();

        List<ServiceChangeDetailsEntity> requests =
                serviceChangeDetailsManager.getAllServiceChangeDetails(
                        orderId,
                        serviceDeployment,
                        updateRequestMap,
                        deployResourceMap,
                        configManageScripts,
                        configurationParameters,
                        ServiceOrderType.CONFIG_CHANGE);

        if (!CollectionUtils.isEmpty(requests)) {
            serviceChangeDetailsStorage.saveAll(requests);
        }
    }

    private void validate(
            ServiceTemplateEntity serviceTemplateEntity,
            ServiceConfigurationUpdate serviceConfigurationUpdate) {
        ServiceChangeManage serviceConfigurationManage =
                serviceTemplateEntity.getOcl().getServiceConfigurationManage();
        if (Objects.isNull(serviceConfigurationManage)) {
            String errorMsg =
                    String.format(
                            "Service template %s has no service configuration manage",
                            serviceTemplateEntity.getId());
            log.error(errorMsg);
            throw new ServiceConfigurationInvalidException(List.of(errorMsg));
        }
        List<ServiceChangeParameter> configurationParameters =
                serviceConfigurationManage.getConfigurationParameters();
        JsonObjectSchema jsonObjectSchema =
                serviceConfigurationVariablesJsonSchemaGenerator
                        .buildServiceConfigurationJsonSchema(configurationParameters);
        serviceConfigurationVariablesJsonSchemaValidator.validateServiceConfiguration(
                configurationParameters,
                serviceConfigurationUpdate.getConfiguration(),
                jsonObjectSchema);
    }

    /** Query pending service change request for agent. */
    @Transactional
    public ResponseEntity<ServiceChangeRequest> getPendingServiceChangeRequest(
            String serviceId, String resourceName) {
        try {
            ServiceChangeDetailsEntity oldestRequest =
                    getOldestServiceChangeDetails(serviceId, resourceName);
            if (Objects.isNull(oldestRequest)) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
            }
            List<DeployResource> deployResources =
                    getDeployResources(UUID.fromString(serviceId), DeployResourceKind.VM);
            if (Objects.isNull(oldestRequest.getResourceName())) {
                validateConfigManager(
                        serviceId, oldestRequest.getChangeHandler(), resourceName, deployResources);
            }
            updateServiceOrder(oldestRequest);
            ServiceChangeDetailsEntity request =
                    updateServiceChangeDetails(oldestRequest, resourceName);
            if (Objects.isNull(request)) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
            }
            return ResponseEntity.status(HttpStatus.OK)
                    .body(getServiceConfigurationChangeRequest(request, deployResources));
        } catch (LockTimeoutException e) {
            log.error("Update service configuration update request object timed out", e);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
        }
    }

    private void updateServiceOrder(ServiceChangeDetailsEntity oldestRequest) {
        ServiceOrderEntity serviceOrderEntity = oldestRequest.getServiceOrderEntity();
        if (Objects.isNull(serviceOrderEntity)) {
            String errorMsg =
                    String.format(
                            "ServiceOrder with service" + " change details id %s not found.",
                            oldestRequest.getId());
            log.error(errorMsg);
            throw new ServiceOrderNotFound(errorMsg);
        }
        serviceOrderEntity.setCompletedTime(OffsetDateTime.now());
        serviceOrderEntity.setTaskStatus(TaskStatus.IN_PROGRESS);
        serviceOrderStorage.storeAndFlush(serviceOrderEntity);
    }

    private ServiceChangeDetailsEntity updateServiceChangeDetails(
            ServiceChangeDetailsEntity request, String resourceName) {
        if (Objects.isNull(request.getResourceName())) {
            request.setResourceName(resourceName);
        }
        request.setStatus(ServiceChangeStatus.PROCESSING);
        return serviceChangeDetailsStorage.storeAndFlush(request);
    }

    private ServiceChangeDetailsEntity getOldestServiceChangeDetails(
            String serviceId, String resourceName) {
        ServiceChangeDetailsQueryModel model =
                new ServiceChangeDetailsQueryModel(
                        null, UUID.fromString(serviceId), null, null, ServiceChangeStatus.PENDING);
        List<ServiceChangeDetailsEntity> requests =
                serviceChangeDetailsStorage.listServiceChangeDetails(model);
        if (CollectionUtils.isEmpty(requests)) {
            return null;
        }
        Optional<ServiceChangeDetailsEntity> oldestRequestOptional =
                requests.stream()
                        .filter(
                                request ->
                                        Objects.isNull(request.getResourceName())
                                                || resourceName.equals(request.getResourceName()))
                        .filter(
                                request ->
                                        request.getServiceOrderEntity() != null
                                                && request.getServiceOrderEntity().getStartedTime()
                                                        != null)
                        .min(
                                Comparator.comparing(
                                        request ->
                                                request.getServiceOrderEntity().getStartedTime()));
        return oldestRequestOptional.orElse(null);
    }

    private void validateConfigManager(
            String serviceId,
            String configManager,
            String resourceName,
            List<DeployResource> deployResources) {
        Map<String, List<DeployResource>> resourceNameMap =
                deployResources.stream()
                        .collect(Collectors.groupingBy(DeployResource::getResourceName));
        if (!resourceNameMap.containsKey(resourceName)) {
            String errorMsg =
                    String.format(
                            "The service with serviceId %s does not contain "
                                    + "a resource with resource_name %s",
                            serviceId, resourceName);
            log.error(errorMsg);
            throw new ServiceConfigurationInvalidException(List.of(errorMsg));
        }
        deployResources.forEach(
                deployResource -> {
                    if (resourceName.equals(deployResource.getResourceName())) {
                        if (!configManager.equals(deployResource.getGroupName())) {
                            String errorMsg =
                                    String.format(
                                            "The service with serviceId %s does"
                                                    + " not contain a group with resource_name %s "
                                                    + "and  group_name %s",
                                            serviceId, resourceName, configManager);
                            log.error(errorMsg);
                            throw new ServiceConfigurationInvalidException(List.of(errorMsg));
                        }
                    }
                });
    }

    private ServiceChangeRequest getServiceConfigurationChangeRequest(
            ServiceChangeDetailsEntity request, List<DeployResource> deployResources) {
        ServiceChangeRequest serviceChangeRequest = new ServiceChangeRequest();
        serviceChangeRequest.setChangeId(request.getId());
        Optional<ServiceChangeScript> configManageScriptOptional = getConfigManageScript(request);
        configManageScriptOptional.ifPresent(
                serviceChangeScript ->
                        serviceChangeRequest.setAnsibleScriptConfig(
                                serviceChangeScript.getAnsibleScriptConfig()));
        serviceChangeRequest.setConfigParameters(request.getProperties());
        serviceChangeRequest.setAnsibleInventory(getAnsibleInventory(deployResources));
        return serviceChangeRequest;
    }

    private Optional<ServiceChangeScript> getConfigManageScript(
            ServiceChangeDetailsEntity request) {
        ServiceTemplateEntity serviceTemplateEntity =
                serviceTemplateStorage.getServiceTemplateById(
                        request.getServiceDeploymentEntity().getServiceTemplateId());
        if (Objects.isNull(serviceTemplateEntity)) {
            String errMsg =
                    String.format(
                            "Service template with id %s not found.",
                            request.getServiceDeploymentEntity().getServiceTemplateId());
            log.error(errMsg);
            throw new ServiceTemplateNotRegistered(errMsg);
        }
        Optional<ServiceChangeScript> configManageScriptOptional =
                serviceTemplateEntity
                        .getOcl()
                        .getServiceConfigurationManage()
                        .getConfigManageScripts()
                        .stream()
                        .filter(
                                serviceChangeScript -> {
                                    String configManager = request.getChangeHandler();
                                    return configManager != null
                                            && configManager.equals(
                                                    serviceChangeScript.getChangeHandler());
                                })
                        .findFirst();
        return configManageScriptOptional;
    }

    private Map<String, Object> getAnsibleInventory(List<DeployResource> deployResources) {
        Map<String, List<DeployResource>> deployResourceMap =
                deployResources.stream()
                        .collect(Collectors.groupingBy(DeployResource::getGroupName));
        Map<String, Object> ansibleInventory = new HashMap<>();
        deployResourceMap.forEach(
                (groupName, deployResourceList) -> {
                    Map<String, AnsibleHostInfo> hosts = new HashMap<>();
                    deployResourceList.forEach(
                            deployResource -> {
                                AnsibleHostInfo ansibleHostInfo = new AnsibleHostInfo();
                                ansibleHostInfo.setAnsibleHost(
                                        deployResource.getProperties().get(IP));
                                hosts.put(deployResource.getResourceName(), ansibleHostInfo);
                            });
                    Map<String, Map<String, AnsibleHostInfo>> resourceMap = new HashMap<>();
                    resourceMap.put(HOSTS, hosts);
                    ansibleInventory.put(groupName, resourceMap);
                });
        return ansibleInventory;
    }

    private List<DeployResource> getDeployResources(
            UUID serviceId, DeployResourceKind resourceKind) {
        ServiceDeploymentEntity deployedService =
                serviceDeploymentEntityHandler.getServiceDeploymentEntity(serviceId);
        Stream<ServiceResourceEntity> resourceEntities =
                deployedService.getDeployResourceList().stream();
        if (Objects.nonNull(resourceKind)) {
            resourceEntities =
                    resourceEntities.filter(
                            resourceEntity ->
                                    resourceEntity.getResourceKind().equals(resourceKind));
        }
        return EntityTransUtils.transToDeployResourceList(resourceEntities.toList());
    }

    /**
     * Method to update service change result.
     *
     * @param changeId id of the update request.
     * @param result result of the service change request.
     */
    public void updateServiceChangeResult(
            String changeId, ServiceConfigurationChangeResult result) {
        ServiceChangeDetailsEntity request =
                serviceChangeDetailsStorage.findById(UUID.fromString(changeId));
        if (Objects.isNull(request)
                || !ServiceChangeStatus.PROCESSING.equals(request.getStatus())) {
            String errorMsg =
                    String.format(
                            "Service change details with id %s , status %s not found",
                            changeId, ServiceChangeStatus.PROCESSING);
            log.error(errorMsg);
            throw new ServiceChangeDetailsEntityNotFoundException(errorMsg);
        }
        if (result.getIsSuccessful()) {
            request.setStatus(ServiceChangeStatus.SUCCESSFUL);
        } else {
            request.setStatus(ServiceChangeStatus.ERROR);
            request.setResultMessage(result.getError());
        }
        request.setTasks(result.getTasks());
        serviceChangeDetailsStorage.storeAndFlush(request);
        updateServiceChangeResult(request);
    }

    private void updateServiceChangeResult(ServiceChangeDetailsEntity request) {
        ServiceChangeDetailsQueryModel model =
                new ServiceChangeDetailsQueryModel(
                        request.getServiceOrderEntity().getOrderId(),
                        request.getServiceDeploymentEntity().getId(),
                        null,
                        null,
                        null);
        List<ServiceChangeDetailsEntity> requests =
                serviceChangeDetailsStorage.listServiceChangeDetails(model);

        if (CollectionUtils.isEmpty(requests)) {
            String errorMsg =
                    String.format(
                            "Service configuration change details with service "
                                    + "id %s not found, ",
                            request.getServiceDeploymentEntity().getId());
            log.error(errorMsg);
            throw new ServiceChangeDetailsEntityNotFoundException(errorMsg);
        }
        boolean isNeedUpdateServiceConfigurationChange =
                requests.stream()
                        .allMatch(
                                changeDetails ->
                                        changeDetails.getStatus() == ServiceChangeStatus.SUCCESSFUL
                                                || changeDetails.getStatus()
                                                        == ServiceChangeStatus.ERROR);
        if (isNeedUpdateServiceConfigurationChange) {
            ServiceOrderEntity entity = request.getServiceOrderEntity();
            if (Objects.nonNull(entity)) {
                boolean isAllSuccessful =
                        requests.stream()
                                .allMatch(
                                        changeDetails ->
                                                request.getStatus()
                                                        == ServiceChangeStatus.SUCCESSFUL);
                if (isAllSuccessful) {
                    updateServiceOrderByResult(entity, TaskStatus.SUCCESSFUL);
                    updateServiceConfiguration(request);
                } else {
                    updateServiceOrderByResult(entity, TaskStatus.FAILED);
                }
            }
        }
    }

    private void updateServiceOrderByResult(ServiceOrderEntity entity, TaskStatus status) {
        entity.setTaskStatus(status);
        entity.setCompletedTime(OffsetDateTime.now());
        serviceOrderStorage.storeAndFlush(entity);
    }

    private void updateServiceConfiguration(ServiceChangeDetailsEntity request) {
        ServiceConfigurationEntity serviceConfigurationEntity =
                request.getServiceDeploymentEntity().getServiceConfigurationEntity();
        Map<String, Object> config =
                (Map<String, Object>) request.getServiceOrderEntity().getRequestBody();
        serviceConfigurationEntity.setConfiguration(config);
        serviceConfigurationEntity.setUpdatedTime(OffsetDateTime.now());
        serviceConfigurationStorage.storeAndFlush(serviceConfigurationEntity);
    }
}
