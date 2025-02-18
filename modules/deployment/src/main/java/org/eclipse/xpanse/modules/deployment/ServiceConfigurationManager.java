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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.servicechange.ServiceChangeRequestEntity;
import org.eclipse.xpanse.modules.database.serviceconfiguration.ServiceConfigurationEntity;
import org.eclipse.xpanse.modules.database.serviceconfiguration.ServiceConfigurationStorage;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.database.utils.EntityTranslationUtils;
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

    @Resource private ServiceConfigurationStorage serviceConfigurationStorage;

    @Resource private ServiceTemplateStorage serviceTemplateStorage;

    @Resource private DeployService deployService;

    @Resource private ServiceChangeRequestsManager serviceChangeRequestsManager;

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
        return EntityTranslationUtils.transToServiceConfigurationDetails(entity);
    }

    /**
     * creates service change requests in the database for the agents to query and process it later.
     *
     * @param serviceId The id of the deployed service.
     * @param configurationUpdate serviceConfigurationUpdate.
     * @return serviceOrder service order created to track the service configuration change request.
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
            validateRequestParameters(serviceTemplateEntity, configurationUpdate);
            UUID orderId =
                    createServiceChangeRequestsInDatabase(
                            serviceId,
                            serviceDeploymentEntity,
                            serviceTemplateEntity.getOcl(),
                            configurationUpdate,
                            getCurrentConfigurationOfService(serviceId).getConfiguration());
            return new ServiceOrder(orderId, serviceId);
        } catch (ServiceConfigurationInvalidException e) {
            String errorMsg =
                    String.format("Change service configuration error, %s", e.getErrorReasons());
            log.error(errorMsg);
            throw e;
        }
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

    private UUID createServiceChangeRequestsInDatabase(
            UUID serviceId,
            ServiceDeploymentEntity serviceDeployment,
            Ocl ocl,
            ServiceConfigurationUpdate updateRequest,
            Map<String, Object> currentConfigurationStored) {
        Map<String, List<DeployResource>> deployResourceMap =
                deployService
                        .listResourcesOfDeployedService(serviceId, DeployResourceKind.VM)
                        .stream()
                        .collect(Collectors.groupingBy(DeployResource::getGroupName));
        // Add missing new configuration parameters which were added to the service template with
        // its default values.
        Map<String, Object> currentConfiguration =
                mergeCurrentConfigurations(
                        ocl.getServiceConfigurationManage(), currentConfigurationStored);
        // Get only the exact scripts to be executed based on the configuration parameters changed.
        List<ServiceChangeScript> configManageScripts =
                getChangeScriptsToBeExecuted(
                        updateRequest.getConfiguration(),
                        currentConfiguration,
                        ocl.getServiceConfigurationManage());
        // update existing configuration map with values received from end user.
        Map<String, Object> newMergedConfiguration =
                createFullNewRequestedConfiguration(
                        updateRequest.getConfiguration(), currentConfiguration);
        return serviceChangeRequestsManager.createServiceOrderAndQueueServiceChangeRequests(
                serviceDeployment,
                updateRequest,
                updateRequest.getConfiguration(),
                newMergedConfiguration,
                deployResourceMap,
                configManageScripts,
                ServiceOrderType.CONFIG_CHANGE);
    }

    private void validateRequestParameters(
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

    /**
     * Gets the config management script from service template. Returns the change script
     * corresponding to the service change request.
     */
    public Optional<ServiceChangeScript> getConfigManageScript(ServiceChangeRequestEntity request) {
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
    public void updateServiceConfigurationInDatabase(
            ServiceChangeRequestEntity request, Map<String, Object> originalRequestProperties) {
        log.info(
                "Updating service configuration for service ID {}",
                request.getServiceDeploymentEntity().getId());
        ServiceConfigurationEntity serviceConfigurationEntity =
                request.getServiceDeploymentEntity().getServiceConfiguration();
        ServiceTemplateEntity serviceTemplateEntity =
                serviceTemplateStorage.getServiceTemplateById(
                        request.getServiceDeploymentEntity().getServiceTemplateId());
        // merge current stored configuration with missing parameters from its service template.#
        Map<String, Object> currentConfigurationMergedWithMissingParametersFromTemplate =
                mergeCurrentConfigurations(
                        serviceTemplateEntity.getOcl().getServiceConfigurationManage(),
                        serviceConfigurationEntity.getConfiguration());
        // merge current configuration with user requested parameters.
        Map<String, Object> updatedFinalConfiguration =
                createFullNewRequestedConfiguration(
                        originalRequestProperties,
                        currentConfigurationMergedWithMissingParametersFromTemplate);
        serviceConfigurationEntity.setConfiguration(updatedFinalConfiguration);
        serviceConfigurationEntity.setUpdatedTime(OffsetDateTime.now());
        serviceConfigurationStorage.storeAndFlush(serviceConfigurationEntity);
    }

    /**
     * Method to generate the final full configuration of the service. The method takes the existing
     * configuration of the service and replace found keys with the values requested from the user.
     *
     * @param userRequestedConfig configuration parameters requested from customer. This can be the
     *     full config set or just the parameters the user wants to change.
     * @param currentConfiguration Last saved configuration of the service.
     * @return The full and final configuration set of the service.
     */
    private Map<String, Object> createFullNewRequestedConfiguration(
            Map<String, Object> userRequestedConfig, Map<String, Object> currentConfiguration) {
        HashMap<String, Object> newRequestedConfig = new HashMap<>(userRequestedConfig);
        currentConfiguration.forEach(
                (key, value) -> {
                    if (!newRequestedConfig.containsKey(key)) {
                        newRequestedConfig.put(key, value);
                    }
                });
        return newRequestedConfig;
    }

    /**
     * update current configuration in case the service template has changed after the service is
     * created.
     */
    private Map<String, Object> mergeCurrentConfigurations(
            ServiceChangeManage serviceChangeManage,
            Map<String, Object> currentStoredConfiguration) {
        Map<String, Object> currentConfiguration = new HashMap<>(currentStoredConfiguration);
        serviceChangeManage
                .getConfigurationParameters()
                .forEach(
                        configurationParameter -> {
                            if (!currentConfiguration.containsKey(
                                    configurationParameter.getName())) {
                                // set initial value for missing configurations.
                                currentConfiguration.put(
                                        configurationParameter.getName(),
                                        configurationParameter.getValue());
                            }
                        });
        return currentConfiguration;
    }

    /**
     * This method filters which service change scripts that must be executed based on the
     * difference between current configuration and changes requested by the user.
     */
    private List<ServiceChangeScript> getChangeScriptsToBeExecuted(
            Map<String, Object> currentConfiguration,
            Map<String, Object> updatedConfiguration,
            ServiceChangeManage serviceChangeManage) {
        List<ServiceChangeScript> changeScriptsToBeExecuted = new ArrayList<>();
        updatedConfiguration.forEach(
                (key, value) -> {
                    if (!currentConfiguration.get(key).equals(value)) {
                        serviceChangeManage.getConfigurationParameters().stream()
                                .filter(
                                        serviceChangeParameter ->
                                                serviceChangeParameter.getName().equals(key))
                                .findFirst()
                                .ifPresent(
                                        serviceChangeParameter -> {
                                            serviceChangeManage
                                                    .getConfigManageScripts()
                                                    .forEach(
                                                            serviceChangeScript -> {
                                                                if (serviceChangeScript
                                                                        .getChangeHandler()
                                                                        .equals(
                                                                                serviceChangeParameter
                                                                                        .getManagedBy())) {
                                                                    changeScriptsToBeExecuted.add(
                                                                            serviceChangeScript);
                                                                }
                                                            });
                                        });
                    }
                });
        return changeScriptsToBeExecuted;
    }
}
