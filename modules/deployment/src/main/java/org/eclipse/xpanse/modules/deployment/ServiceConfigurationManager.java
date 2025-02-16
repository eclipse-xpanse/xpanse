/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.servicechange.ServiceChangeDetailsEntity;
import org.eclipse.xpanse.modules.database.servicechange.ServiceChangeDetailsStorage;
import org.eclipse.xpanse.modules.database.serviceconfiguration.ServiceConfigurationEntity;
import org.eclipse.xpanse.modules.database.serviceconfiguration.ServiceConfigurationStorage;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.database.utils.EntityTransUtils;
import org.eclipse.xpanse.modules.logging.CustomRequestIdGenerator;
import org.eclipse.xpanse.modules.models.common.enums.UserOperation;
import org.eclipse.xpanse.modules.models.service.deployment.DeployResource;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.models.service.utils.ServiceConfigurationVariablesJsonSchemaGenerator;
import org.eclipse.xpanse.modules.models.service.utils.ServiceConfigurationVariablesJsonSchemaValidator;
import org.eclipse.xpanse.modules.models.serviceconfiguration.ServiceConfigurationDetails;
import org.eclipse.xpanse.modules.models.serviceconfiguration.ServiceConfigurationUpdate;
import org.eclipse.xpanse.modules.models.serviceconfiguration.exceptions.ServiceConfigurationInvalidException;
import org.eclipse.xpanse.modules.models.serviceconfiguration.exceptions.ServiceConfigurationNotFoundException;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceChangeManage;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceChangeParameter;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceChangeScript;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotRegistered;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.JsonObjectSchema;
import org.eclipse.xpanse.modules.security.auth.UserServiceHelper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/** Bean to manage service configuration. */
@Slf4j
@Component
public class ServiceConfigurationManager {

    @Resource private ServiceDeploymentEntityHandler serviceDeploymentEntityHandler;

    @Resource private ServiceChangeDetailsStorage serviceChangeDetailsStorage;

    @Resource private ServiceConfigurationStorage serviceConfigurationStorage;

    @Resource private ServiceTemplateStorage serviceTemplateStorage;

    @Resource private DeployService deployService;

    @Resource private ServiceChangeDetailsManager serviceChangeDetailsManager;

    @Resource private UserServiceHelper userServiceHelper;

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
        checkPermission(
                entity.getServiceDeploymentEntity(), UserOperation.VIEW_CONFIGURATIONS_OF_SERVICE);
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
            checkPermission(serviceDeploymentEntity, UserOperation.CHANGE_SERVICE_CONFIGURATION);
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
            Map<String, Object> updateRequestMap =
                    getModifiedConfigurations(
                            configurationUpdate.getConfiguration(),
                            serviceDeploymentEntity.getServiceConfiguration().getConfiguration());
            UUID orderId = CustomRequestIdGenerator.generateOrderId();
            if (MapUtils.isNotEmpty(updateRequestMap)) {
                createServiceChangeRequestsInDatabase(
                        orderId,
                        serviceId,
                        serviceDeploymentEntity,
                        serviceTemplateEntity.getOcl(),
                        updateRequestMap);
            }

            return new ServiceOrder(orderId, serviceId);
        } catch (ServiceConfigurationInvalidException e) {
            String errorMsg =
                    String.format("Change service configuration error, %s", e.getErrorReasons());
            log.error(errorMsg);
            throw e;
        }
    }

    /**
     * Compares two configurations, only return modified or added.
     *
     * @param updateConfigurations The updated configuration map containing new values.
     * @param entityConfigurations The existing configuration map for comparison.
     * @return A map containing only the key-value pairs that have been modified.
     */
    private Map<String, Object> getModifiedConfigurations(
            Map<String, Object> updateConfigurations, Map<String, Object> entityConfigurations) {
        Map<String, Object> modifiedConfigs = new HashMap<>();

        for (String key : updateConfigurations.keySet()) {
            Object entityConfig = entityConfigurations.get(key);
            Object updateConfig = updateConfigurations.get(key);
            if (Objects.isNull(entityConfig) || !Objects.equals(entityConfig, updateConfig)) {
                modifiedConfigs.put(key, updateConfig);
            }
        }
        return modifiedConfigs;
    }

    private void checkPermission(
            ServiceDeploymentEntity deployedService, UserOperation userOperation) {
        boolean currentUserIsOwner =
                userServiceHelper.currentUserIsOwner(deployedService.getUserId());
        if (!currentUserIsOwner) {
            String errorMsg =
                    String.format(
                            "No permission to %s owned by other users.", userOperation.toValue());
            log.error(errorMsg);
            throw new AccessDeniedException(errorMsg);
        }
    }

    private void createServiceChangeRequestsInDatabase(
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
        serviceChangeDetailsManager.createAndQueueAllServiceChangeRequests(
                orderId,
                serviceDeployment,
                updateRequestMap,
                deployResourceMap,
                configManageScripts,
                configurationParameters,
                ServiceOrderType.CONFIG_CHANGE);
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

    /** Gets the config management script from service template. */
    public Optional<ServiceChangeScript> getConfigManageScript(ServiceChangeDetailsEntity request) {
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
        return serviceTemplateEntity
                .getOcl()
                .getServiceConfigurationManage()
                .getConfigManageScripts()
                .stream()
                .filter(
                        serviceChangeScript -> {
                            String configManager = request.getChangeHandler();
                            return configManager != null
                                    && configManager.equals(serviceChangeScript.getChangeHandler());
                        })
                .findFirst();
    }

    /**
     * updates configuration data after the configuration change request is processed successfully.
     */
    public void updateServiceConfiguration(ServiceChangeDetailsEntity request) {
        ServiceConfigurationEntity serviceConfigurationEntity =
                request.getServiceDeploymentEntity().getServiceConfiguration();
        Map<String, Object> config = request.getServiceOrderEntity().getRequestBody();
        serviceConfigurationEntity.setConfiguration(config);
        serviceConfigurationEntity.setUpdatedTime(OffsetDateTime.now());
        serviceConfigurationStorage.storeAndFlush(serviceConfigurationEntity);
    }
}
