/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import static org.eclipse.xpanse.modules.async.TaskConfiguration.ASYNC_EXECUTOR_NAME;

import jakarta.annotation.Resource;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.resource.ServiceResourceEntity;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentStorage;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateQueryModel;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.database.utils.EntityTranslationUtils;
import org.eclipse.xpanse.modules.deployment.polling.ServiceDeploymentStatusChangePolling;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.common.enums.UserOperation;
import org.eclipse.xpanse.modules.models.service.config.ServiceLockConfig;
import org.eclipse.xpanse.modules.models.service.deployment.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deployment.DeployResource;
import org.eclipse.xpanse.modules.models.service.deployment.DeployResult;
import org.eclipse.xpanse.modules.models.service.deployment.DeploymentStatusUpdate;
import org.eclipse.xpanse.modules.models.service.deployment.ModifyRequest;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.BillingModeNotSupported;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.EulaNotAccepted;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.FlavorInvalidException;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.ServiceFlavorDowngradeNotAllowed;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.ServiceLockedException;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.ServiceModifyParamsNotFoundException;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.service.enums.Handler;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.models.service.utils.ServiceInputVariablesJsonSchemaValidator;
import org.eclipse.xpanse.modules.models.servicetemplate.FlavorsWithPrice;
import org.eclipse.xpanse.modules.models.servicetemplate.InputVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceFlavor;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceFlavorWithPrice;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateUnavailableException;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.DeploymentVariableHelper;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.deployment.Deployer;
import org.eclipse.xpanse.modules.security.auth.UserServiceHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * Main class which orchestrates the OCL request processing. Calls the available plugins to deploy
 * managed service in the respective infrastructure as defined in the OCL.
 */
@Slf4j
@Component
public class DeployService {
    @Resource private UserServiceHelper userServiceHelper;
    @Resource private PluginManager pluginManager;
    @Resource private ServiceTemplateStorage serviceTemplateStorage;
    @Resource private ServiceDeploymentStorage serviceDeploymentStorage;
    @Resource private ServiceInputVariablesJsonSchemaValidator inputVariablesJsonSchemaValidator;
    @Resource private PolicyValidator policyValidator;
    @Resource private SensitiveDataHandler sensitiveDataHandler;
    @Resource private ServiceDeploymentEntityHandler serviceDeploymentEntityHandler;
    @Resource private DeployResultManager deployResultManager;
    @Resource private DeployerKindManager deployerKindManager;
    @Resource private ServiceDeploymentEntityConverter serviceDeploymentEntityConverter;
    @Resource private ServiceOrderManager serviceOrderManager;
    @Resource private ServiceDeploymentStatusChangePolling serviceDeploymentStatusChangePolling;

    @Resource(name = ASYNC_EXECUTOR_NAME)
    private Executor taskExecutor;

    @Value("${spring.profiles.active}")
    private String activeProfiles;

    /**
     * Create order to deploy new service.
     *
     * @param deployRequest deploy request.
     * @return ServiceOrder.
     */
    public ServiceOrder createOrderToDeployNewService(DeployRequest deployRequest) {
        String userId = userServiceHelper.getCurrentUserId();
        DeployTask deployTask = createNewDeployTask(userId, deployRequest);
        deployService(deployTask);
        return new ServiceOrder(deployTask.getOrderId(), deployTask.getServiceId());
    }

    /**
     * Create order to redeploy failed service.
     *
     * @param serviceId failed service id.
     * @return ServiceOrder.
     */
    public ServiceOrder createOrderToRedeployFailedService(UUID serviceId) {
        ServiceDeploymentEntity serviceDeploymentEntity =
                getServiceOwnedByCurrentUser(serviceId, UserOperation.REDEPLOY_SERVICE);
        DeployTask redeployTask = getRedeployTask(serviceDeploymentEntity, ServiceOrderType.RETRY);
        redeployService(redeployTask, serviceDeploymentEntity);
        log.info(
                "Order task {} to redeploy failed service {} started.",
                redeployTask.getOrderId(),
                redeployTask.getServiceId());
        return new ServiceOrder(redeployTask.getOrderId(), redeployTask.getServiceId());
    }

    /**
     * Create order to modify deployed service.
     *
     * @param serviceId deployed service id.
     * @param modifyRequest modify request.
     * @return ServiceOrder.
     */
    public ServiceOrder createOrderToModifyDeployedService(
            UUID serviceId, ModifyRequest modifyRequest) {
        ServiceDeploymentEntity serviceDeploymentEntity =
                getServiceOwnedByCurrentUser(serviceId, UserOperation.MODIFY_SERVICE);
        DeployTask modifyTask = getModifyTask(modifyRequest, serviceDeploymentEntity);
        modifyService(modifyTask, serviceDeploymentEntity);
        log.info(
                "Order task {} to modify deployed service {} started.",
                modifyTask.getOrderId(),
                modifyTask.getServiceId());
        return new ServiceOrder(modifyTask.getOrderId(), modifyTask.getServiceId());
    }

    /**
     * Create order to destroy deployed service.
     *
     * @param serviceId deployed service id.
     * @return ServiceOrder.
     */
    public ServiceOrder createOrderToDestroyDeployedService(UUID serviceId) {
        ServiceDeploymentEntity serviceDeploymentEntity =
                getServiceOwnedByCurrentUser(serviceId, UserOperation.DESTROY_SERVICE);
        DeployTask destroyTask = getDestroyTask(serviceDeploymentEntity);
        destroyService(destroyTask, serviceDeploymentEntity);
        log.info(
                "Order task {} to destroy deployed service {} started.",
                destroyTask.getOrderId(),
                destroyTask.getServiceId());
        return new ServiceOrder(destroyTask.getOrderId(), destroyTask.getServiceId());
    }

    /**
     * Create order to purge destroyed service.
     *
     * @param serviceId destroyed service id.
     * @return ServiceOrder.
     */
    public ServiceOrder createOrderToPurgeDestroyedService(UUID serviceId) {

        ServiceDeploymentEntity serviceDeploymentEntity =
                getServiceOwnedByCurrentUser(serviceId, UserOperation.PURGE_SERVICE);
        DeployTask purgeTask = getPurgeTask(serviceDeploymentEntity);
        purgeService(purgeTask, serviceDeploymentEntity);
        log.info(
                "Order task {} to purge the destroyed service {} started.",
                purgeTask.getOrderId(),
                serviceId);
        return new ServiceOrder(purgeTask.getOrderId(), purgeTask.getServiceId());
    }

    /**
     * Get availability zones of region.
     *
     * @param csp cloud service provider.
     * @param siteName the site of the region belongs to.
     * @param regionName region name.
     * @param serviceId deployed service id.
     * @return List of availability zones.
     */
    public List<String> getAvailabilityZonesOfRegion(
            Csp csp, String siteName, String regionName, UUID serviceId, UUID serviceTemplateId) {
        String currentUserId = this.userServiceHelper.getCurrentUserId();
        OrchestratorPlugin orchestratorPlugin = pluginManager.getOrchestratorPlugin(csp);
        return orchestratorPlugin.getAvailabilityZonesOfRegion(
                siteName, regionName, currentUserId, serviceId, serviceTemplateId);
    }

    /**
     * List resources of service.
     *
     * @param serviceId service id.
     * @param resourceKind resource kind.
     * @return List of DeployResource.
     */
    public List<DeployResource> listResourcesOfDeployedService(
            UUID serviceId, DeployResourceKind resourceKind) {
        ServiceDeploymentEntity deployedService =
                getServiceOwnedByCurrentUser(serviceId, UserOperation.VIEW_RESOURCES_OF_SERVICE);
        Stream<ServiceResourceEntity> resourceEntities =
                deployedService.getDeployResources().stream();
        if (Objects.nonNull(resourceKind)) {
            resourceEntities =
                    resourceEntities.filter(
                            resourceEntity ->
                                    resourceEntity.getResourceKind().equals(resourceKind));
        }
        return EntityTranslationUtils.transToDeployResources(resourceEntities.toList());
    }

    /**
     * Get user managed service.
     *
     * @param serviceId deployed service id.
     * @param userOperation userOperation.
     * @return DeployServiceEntity.
     * @throws AccessDeniedException if the current user is not the owner of the service.
     */
    private ServiceDeploymentEntity getServiceOwnedByCurrentUser(
            UUID serviceId, UserOperation userOperation) {
        ServiceDeploymentEntity deployedService =
                serviceDeploymentEntityHandler.getServiceDeploymentEntity(serviceId);
        boolean currentUserIsOwner =
                userServiceHelper.currentUserIsOwner(deployedService.getUserId());
        if (!currentUserIsOwner) {
            String errorMsg =
                    String.format(
                            "No permission to %s owned by other users.", userOperation.toValue());
            log.error(errorMsg);
            throw new AccessDeniedException(errorMsg);
        }
        return deployedService;
    }

    /**
     * Create new deploy task by deploy request.
     *
     * @param deployRequest deploy request.
     * @return new deploy task.
     */
    private DeployTask createNewDeployTask(String userId, DeployRequest deployRequest) {
        // Find service templates and fill Ocl.
        ServiceTemplateQueryModel queryModel =
                ServiceTemplateQueryModel.builder()
                        .category(deployRequest.getCategory())
                        .csp(deployRequest.getCsp())
                        .serviceName(deployRequest.getServiceName())
                        .serviceVersion(deployRequest.getVersion())
                        .serviceHostingType(deployRequest.getServiceHostingType())
                        .build();
        List<ServiceTemplateEntity> existingServiceTemplates =
                serviceTemplateStorage.listServiceTemplates(queryModel);
        ServiceTemplateEntity availableServiceTemplate =
                existingServiceTemplates.stream()
                        .filter(
                                serviceTemplate ->
                                        serviceTemplate.getIsAvailableInCatalog()
                                                && Objects.nonNull(serviceTemplate.getOcl()))
                        .findFirst()
                        .orElse(null);
        if (Objects.isNull(availableServiceTemplate)) {
            String errorMsg = "No service template is available to be used to deploy service";
            log.error(errorMsg);
            throw new ServiceTemplateUnavailableException(errorMsg);
        }
        if (StringUtils.isNotBlank(availableServiceTemplate.getOcl().getEula())
                && !deployRequest.isEulaAccepted()) {
            log.error("Service not accepted Eula.");
            throw new EulaNotAccepted("Service not accepted Eula.");
        }
        if (!availableServiceTemplate
                .getOcl()
                .getBilling()
                .getBillingModes()
                .contains(deployRequest.getBillingMode())) {
            String errorMsg =
                    String.format(
                            "The service template with id %s does not support billing mode %s.",
                            availableServiceTemplate.getId(), deployRequest.getBillingMode());
            throw new BillingModeNotSupported(errorMsg);
        }
        // Check context validation
        validateDeployRequestWithServiceTemplate(availableServiceTemplate, deployRequest);
        List<InputVariable> definedInputVariables =
                DeploymentVariableHelper.getInputVariables(
                        availableServiceTemplate.getOcl().getDeployment());
        sensitiveDataHandler.encodeInputVariables(
                definedInputVariables, deployRequest.getServiceRequestProperties());

        AvailabilityZonesRequestValidator.validateAvailabilityZones(
                deployRequest.getAvailabilityZones(),
                availableServiceTemplate.getOcl().getDeployment().getServiceAvailabilityConfig());
        if (StringUtils.isEmpty(deployRequest.getCustomerServiceName())) {
            deployRequest.setCustomerServiceName(generateCustomerServiceName(deployRequest));
        }
        DeployTask deployTask = new DeployTask();
        deployTask.setUserId(userId);
        deployTask.setDeployRequest(deployRequest);
        deployTask.setRequest(deployRequest);
        deployTask.setTaskType(ServiceOrderType.DEPLOY);
        deployTask.setServiceVendor(availableServiceTemplate.getServiceVendor());
        deployTask.setOcl(availableServiceTemplate.getOcl());
        deployTask.setServiceTemplateId(availableServiceTemplate.getId());
        return deployTask;
    }

    private void validateDeployRequestWithServiceTemplate(
            ServiceTemplateEntity existingServiceTemplate, DeployRequest deployRequest) {
        // Check context validation
        if (Objects.nonNull(existingServiceTemplate.getOcl().getDeployment())
                && Objects.nonNull(deployRequest.getServiceRequestProperties())) {
            List<InputVariable> inputVariables =
                    DeploymentVariableHelper.getInputVariables(
                            existingServiceTemplate.getOcl().getDeployment());
            inputVariablesJsonSchemaValidator.validateInputVariables(
                    inputVariables,
                    deployRequest.getServiceRequestProperties(),
                    existingServiceTemplate.getJsonObjectSchema());
        }
        getServiceFlavorWithName(
                deployRequest.getFlavor(), existingServiceTemplate.getOcl().getFlavors());
    }

    private ServiceFlavorWithPrice getServiceFlavorWithName(
            String flavorName, FlavorsWithPrice flavors) {
        Optional<ServiceFlavorWithPrice> flavorOptional =
                flavors.getServiceFlavors().stream()
                        .filter(flavor -> flavor.getName().equals(flavorName))
                        .findAny();
        if (flavorOptional.isEmpty()) {
            throw new FlavorInvalidException(
                    String.format("Could not find service flavor with name %s", flavorName));
        }
        return flavorOptional.get();
    }

    private String generateCustomerServiceName(DeployRequest deployRequest) {
        if (deployRequest.getServiceName().length() > 5) {
            return deployRequest.getServiceName().substring(0, 4)
                    + "-"
                    + RandomStringUtils.secure().nextAlphanumeric(5);
        } else {
            return deployRequest.getServiceName()
                    + "-"
                    + RandomStringUtils.secure().nextAlphanumeric(5);
        }
    }

    private ServiceDeploymentEntity storeNewDeployServiceEntity(DeployTask deployTask) {
        ServiceDeploymentEntity entity = new ServiceDeploymentEntity();
        entity.setCreatedTime(OffsetDateTime.now());
        entity.setVersion(StringUtils.lowerCase(deployTask.getDeployRequest().getVersion()));
        entity.setName(StringUtils.lowerCase(deployTask.getDeployRequest().getServiceName()));
        entity.setCsp(deployTask.getDeployRequest().getCsp());
        entity.setCategory(deployTask.getDeployRequest().getCategory());
        entity.setCustomerServiceName(deployTask.getDeployRequest().getCustomerServiceName());
        entity.setUserId(deployTask.getUserId());
        entity.setServiceHostingType(deployTask.getDeployRequest().getServiceHostingType());
        entity.setRegion(deployTask.getDeployRequest().getRegion());
        if (!CollectionUtils.isEmpty(deployTask.getDeployRequest().getAvailabilityZones())) {
            entity.setAvailabilityZones(deployTask.getDeployRequest().getAvailabilityZones());
        }
        entity.setFlavor(deployTask.getDeployRequest().getFlavor());
        entity.setBillingMode(deployTask.getDeployRequest().getBillingMode());
        entity.setIsEulaAccepted(deployTask.getDeployRequest().isEulaAccepted());
        if (!CollectionUtils.isEmpty(deployTask.getDeployRequest().getServiceRequestProperties())) {
            Map<String, String> inputProperties = new HashMap<>();
            for (Map.Entry<String, Object> entry :
                    deployTask.getDeployRequest().getServiceRequestProperties().entrySet()) {
                inputProperties.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
            entity.setInputProperties(inputProperties);
        }
        entity.setDeployResources(new ArrayList<>());
        entity.setServiceVendor(deployTask.getServiceVendor());
        entity.setServiceDeploymentState(ServiceDeploymentState.DEPLOYING);
        ServiceTemplateEntity serviceTemplateEntity =
                serviceTemplateStorage.getServiceTemplateById(deployTask.getServiceTemplateId());
        entity.setServiceTemplateEntity(serviceTemplateEntity);
        ServiceLockConfig defaultLockConfig = new ServiceLockConfig();
        defaultLockConfig.setDestroyLocked(false);
        defaultLockConfig.setModifyLocked(false);
        entity.setLockConfig(defaultLockConfig);
        ServiceDeploymentEntity storedEntity = serviceDeploymentEntityHandler.storeAndFlush(entity);
        if (Objects.isNull(storedEntity)) {
            log.error(
                    "Store new deploy service entity with id {} failed.",
                    deployTask.getServiceId());
            throw new ServiceNotDeployedException("Store new deploy service entity failed.");
        }
        return storedEntity;
    }

    private void deployService(DeployTask deployTask) {
        DeployResult deployResult;
        RuntimeException exception = null;
        DeployerKind kind = deployTask.getOcl().getDeployment().getDeployerTool().getKind();
        Deployer deployer = deployerKindManager.getDeployment(kind);
        ServiceDeploymentEntity serviceEntity = storeNewDeployServiceEntity(deployTask);
        Handler handler = getHandler(activeProfiles, kind);
        ServiceOrderEntity serviceOrderEntity =
                serviceOrderManager.storeNewServiceOrderEntity(deployTask, serviceEntity, handler);
        try {
            policyValidator.validateDeploymentWithPolicies(deployTask);
            serviceOrderManager.startOrderProgress(serviceOrderEntity);
            deployResult = deployer.deploy(deployTask);
        } catch (RuntimeException e) {
            exception = e;
            deployResult = deployResultManager.getFailedDeployResult(deployTask, exception);
        }
        deployResultManager.updateServiceWithDeployResult(deployResult, handler);
        if (Objects.nonNull(exception)) {
            throw new ServiceNotDeployedException(exception.getMessage());
        }
    }

    /**
     * Redeploy service with failed state.
     *
     * @param redeployTask redeployTask
     * @param serviceDeploymentEntity deployServiceEntity
     */
    private void redeployService(
            DeployTask redeployTask, ServiceDeploymentEntity serviceDeploymentEntity) {
        DeployResult redeployResult;
        RuntimeException exception = null;
        DeployerKind kind = redeployTask.getOcl().getDeployment().getDeployerTool().getKind();
        Deployer deployer = deployerKindManager.getDeployment(kind);
        Handler handler = getHandler(activeProfiles, kind);
        redeployTask.setRequest(redeployTask.getDeployRequest());
        ServiceOrderEntity serviceOrderEntity =
                serviceOrderManager.storeNewServiceOrderEntity(
                        redeployTask, serviceDeploymentEntity, handler);
        try {
            policyValidator.validateDeploymentWithPolicies(redeployTask);
            serviceDeploymentEntityHandler.updateServiceDeploymentStatus(
                    serviceDeploymentEntity, ServiceDeploymentState.DEPLOYING);
            serviceOrderManager.startOrderProgress(serviceOrderEntity);
            redeployResult = deployer.deploy(redeployTask);
        } catch (RuntimeException e) {
            exception = e;
            redeployResult = deployResultManager.getFailedDeployResult(redeployTask, exception);
        }
        deployResultManager.updateServiceWithDeployResult(redeployResult, handler);
        if (Objects.nonNull(exception)) {
            throw new ServiceNotDeployedException(exception.getMessage());
        }
    }

    /**
     * Get modify task by stored deploy service entity.
     *
     * @param serviceDeploymentEntity deploy service entity.
     */
    private DeployTask getModifyTask(
            ModifyRequest modifyRequest, ServiceDeploymentEntity serviceDeploymentEntity) {
        if (Objects.nonNull(serviceDeploymentEntity.getLockConfig())
                && serviceDeploymentEntity.getLockConfig().isModifyLocked()) {
            String errorMsg =
                    "Service " + serviceDeploymentEntity.getId() + " is locked from modification.";
            throw new ServiceLockedException(errorMsg);
        }

        if (StringUtils.isBlank(modifyRequest.getFlavor())
                && Objects.isNull(modifyRequest.getServiceRequestProperties())) {
            throw new ServiceModifyParamsNotFoundException("No params found for modify services.");
        }

        this.serviceDeploymentEntityHandler.validateServiceDeploymentStateForOrderType(
                serviceDeploymentEntity, ServiceOrderType.MODIFY);
        ServiceTemplateEntity existingServiceTemplate =
                serviceDeploymentEntity.getServiceTemplateEntity();
        if (!existingServiceTemplate.getIsAvailableInCatalog()) {
            String errorMsg =
                    String.format(
                            "Used service template %s is unavailable to be used to modify service.",
                            existingServiceTemplate.getId());
            log.error(errorMsg);
            throw new ServiceTemplateUnavailableException(errorMsg);
        }
        DeployTask modifyTask =
                serviceDeploymentEntityConverter.getDeployTaskByStoredService(
                        ServiceOrderType.MODIFY, serviceDeploymentEntity);
        modifyTask.setUserId(userServiceHelper.getCurrentUserId());
        DeployRequest newDeployRequest = modifyTask.getDeployRequest();
        if (StringUtils.isNotEmpty(modifyRequest.getCustomerServiceName())) {
            newDeployRequest.setCustomerServiceName(modifyRequest.getCustomerServiceName());
        }
        if (StringUtils.isNotBlank(modifyRequest.getFlavor())) {
            validateFlavorDowngradedIsAllowed(
                    serviceDeploymentEntity.getFlavor(),
                    modifyRequest.getFlavor(),
                    existingServiceTemplate.getOcl().getFlavors());
            newDeployRequest.setFlavor(modifyRequest.getFlavor());
        }
        Map<String, Object> serviceRequestProperties = modifyRequest.getServiceRequestProperties();
        if (!CollectionUtils.isEmpty(serviceRequestProperties)) {
            newDeployRequest.setServiceRequestProperties(serviceRequestProperties);
        }
        validateDeployRequestWithServiceTemplate(existingServiceTemplate, newDeployRequest);
        List<InputVariable> definedInputVariables =
                DeploymentVariableHelper.getInputVariables(
                        existingServiceTemplate.getOcl().getDeployment());
        sensitiveDataHandler.encodeInputVariables(
                definedInputVariables, newDeployRequest.getServiceRequestProperties());
        modifyTask.setDeployRequest(newDeployRequest);
        modifyTask.setRequest(modifyRequest);
        return modifyTask;
    }

    private void validateFlavorDowngradedIsAllowed(
            String originalFlavor, String newFlavor, FlavorsWithPrice flavors) {
        if (!flavors.isDowngradeAllowed()) {
            ServiceFlavor newServiceFlavor = getServiceFlavorWithName(newFlavor, flavors);
            ServiceFlavor originalServiceFlavor = getServiceFlavorWithName(originalFlavor, flavors);
            if (newServiceFlavor.getPriority() > originalServiceFlavor.getPriority()) {
                String errorMsg =
                        String.format(
                                "Downgrading of flavors is not allowed. New flavor priority %d is"
                                        + " lower than the original flavor priority %d.",
                                newServiceFlavor.getPriority(),
                                originalServiceFlavor.getPriority());
                throw new ServiceFlavorDowngradeNotAllowed(errorMsg);
            }
        }
    }

    /**
     * Async method to modify service.
     *
     * @param modifyTask modifyTask.
     * @param serviceDeployment deployServiceEntity
     */
    public void modifyService(DeployTask modifyTask, ServiceDeploymentEntity serviceDeployment) {
        RuntimeException exception = null;
        DeployResult modifyResult;
        DeployerKind kind = modifyTask.getOcl().getDeployment().getDeployerTool().getKind();
        Deployer deployer = deployerKindManager.getDeployment(kind);
        Handler handler = getHandler(activeProfiles, kind);
        ServiceOrderEntity serviceOrderEntity =
                serviceOrderManager.storeNewServiceOrderEntity(
                        modifyTask, serviceDeployment, handler);
        try {
            serviceDeploymentEntityHandler.updateServiceDeploymentStatus(
                    serviceDeployment, ServiceDeploymentState.MODIFYING);
            serviceOrderManager.startOrderProgress(serviceOrderEntity);
            modifyResult = deployer.modify(modifyTask);
        } catch (RuntimeException e) {
            exception = e;
            modifyResult = deployResultManager.getFailedDeployResult(modifyTask, e);
        }
        deployResultManager.updateServiceWithDeployResult(modifyResult, handler);
        if (Objects.nonNull(exception)) {
            throw new ServiceNotDeployedException(exception.getMessage());
        }
    }

    /**
     * Async method to destroy service.
     *
     * @param destroyTask destroyTask.
     * @param serviceDeploymentEntity deployServiceEntity
     */
    public void destroyService(
            DeployTask destroyTask, ServiceDeploymentEntity serviceDeploymentEntity) {
        DeployResult destroyResult;
        RuntimeException exception = null;
        DeployerKind kind = destroyTask.getOcl().getDeployment().getDeployerTool().getKind();
        Deployer deployer = deployerKindManager.getDeployment(kind);
        Handler handler = getHandler(activeProfiles, kind);
        ServiceOrderEntity serviceOrderEntity =
                serviceOrderManager.storeNewServiceOrderEntity(
                        destroyTask, serviceDeploymentEntity, handler);
        try {
            if (ServiceOrderType.ROLLBACK != destroyTask.getTaskType()) {
                serviceDeploymentEntityHandler.updateServiceDeploymentStatus(
                        serviceDeploymentEntity, ServiceDeploymentState.DESTROYING);
            }
            serviceOrderManager.startOrderProgress(serviceOrderEntity);
            destroyResult = deployer.destroy(destroyTask);
        } catch (RuntimeException e) {
            exception = e;
            destroyResult = deployResultManager.getFailedDeployResult(destroyTask, e);
        }
        deployResultManager.updateServiceWithDeployResult(destroyResult, handler);
        if (Objects.nonNull(exception)) {
            throw new ServiceNotDeployedException(exception.getMessage());
        }
    }

    /**
     * purge the service based on the serviceDeploymentState.
     *
     * @param purgeTask purgeTask.
     * @param serviceDeployment deployServiceEntity
     */
    private void purgeService(DeployTask purgeTask, ServiceDeploymentEntity serviceDeployment) {
        RuntimeException exception = null;
        DeployResult purgeResult;
        DeployerKind kind = purgeTask.getOcl().getDeployment().getDeployerTool().getKind();
        Deployer deployer = deployerKindManager.getDeployment(kind);
        Handler handler = getHandler(activeProfiles, kind);
        ServiceOrderEntity serviceOrderEntity =
                serviceOrderManager.storeNewServiceOrderEntity(
                        purgeTask, serviceDeployment, handler);
        if (!CollectionUtils.isEmpty(serviceDeployment.getDeployResources())) {
            try {
                log.info(
                        "Resources of service {} need to clear with order task {}",
                        purgeTask.getServiceId(),
                        purgeTask.getOrderId());
                serviceDeploymentEntityHandler.updateServiceDeploymentStatus(
                        serviceDeployment, ServiceDeploymentState.DESTROYING);
                serviceOrderManager.startOrderProgress(serviceOrderEntity);
                purgeResult = deployer.destroy(purgeTask);
            } catch (RuntimeException e) {
                exception = e;
                purgeResult = deployResultManager.getFailedDeployResult(purgeTask, e);
            }
            deployResultManager.updateServiceWithDeployResult(purgeResult, handler);
            if (Objects.nonNull(exception)) {
                throw new ServiceNotDeployedException(exception.getMessage());
            }
        } else {
            log.info("No resources of service {} need to clear", purgeTask.getServiceId());
            serviceDeploymentStorage.deleteServiceDeployment(serviceDeployment);
        }
    }

    /**
     * Start a new order to execute the deployment task in workflow.
     *
     * @param workFlowDeployTask deployTask in workflow.
     * @return serviceOrder
     */
    public ServiceOrder deployServiceByWorkflow(DeployTask workFlowDeployTask) {
        if (workFlowDeployTask.getTaskType() == ServiceOrderType.RECREATE) {
            ServiceDeploymentEntity deployServiceEntity =
                    serviceDeploymentStorage.findServiceDeploymentById(
                            workFlowDeployTask.getServiceId());
            // recreate the service destroy_success.
            DeployTask retryTask = getRedeployTask(deployServiceEntity, ServiceOrderType.RECREATE);
            retryTask.setOriginalServiceId(workFlowDeployTask.getOriginalServiceId());
            retryTask.setParentOrderId(workFlowDeployTask.getParentOrderId());
            retryTask.setWorkflowId(workFlowDeployTask.getWorkflowId());
            redeployService(retryTask, deployServiceEntity);
            return new ServiceOrder(retryTask.getOrderId(), retryTask.getServiceId());
        } else {
            DeployTask deployTask =
                    createNewDeployTask(
                            workFlowDeployTask.getUserId(), workFlowDeployTask.getDeployRequest());
            deployTask.setOriginalServiceId(workFlowDeployTask.getOriginalServiceId());
            deployTask.setParentOrderId(workFlowDeployTask.getParentOrderId());
            deployTask.setWorkflowId(workFlowDeployTask.getWorkflowId());
            deployService(deployTask);
            return new ServiceOrder(workFlowDeployTask.getOrderId(), deployTask.getServiceId());
        }
    }

    /**
     * Start a new order to execute the destroy task in workflow.
     *
     * @param workFlowDestroyTask destroyTask in workflow.
     * @return serviceOrder.
     */
    public ServiceOrder destroyServiceByWorkflow(DeployTask workFlowDestroyTask) {
        UUID originalServiceId = workFlowDestroyTask.getOriginalServiceId();
        ServiceDeploymentEntity deployServiceEntity =
                serviceDeploymentEntityHandler.getServiceDeploymentEntity(originalServiceId);
        DeployTask destroyTask =
                serviceDeploymentEntityConverter.getDeployTaskByStoredService(
                        ServiceOrderType.DESTROY, deployServiceEntity);
        destroyTask.setOriginalServiceId(originalServiceId);
        destroyTask.setWorkflowId(workFlowDestroyTask.getWorkflowId());
        destroyTask.setParentOrderId(workFlowDestroyTask.getParentOrderId());
        destroyService(destroyTask, deployServiceEntity);
        return new ServiceOrder(destroyTask.getOrderId(), destroyTask.getServiceId());
    }

    /**
     * Get destroy task by stored deploy service entity.
     *
     * @param serviceDeploymentEntity deploy service entity.
     */
    private DeployTask getDestroyTask(ServiceDeploymentEntity serviceDeploymentEntity) {
        if (Objects.nonNull(serviceDeploymentEntity.getLockConfig())
                && serviceDeploymentEntity.getLockConfig().isDestroyLocked()) {
            String errorMsg =
                    "Service " + serviceDeploymentEntity.getId() + " is locked from deletion.";
            throw new ServiceLockedException(errorMsg);
        }
        // Get state of service.
        this.serviceDeploymentEntityHandler.validateServiceDeploymentStateForOrderType(
                serviceDeploymentEntity, ServiceOrderType.DESTROY);

        return serviceDeploymentEntityConverter.getDeployTaskByStoredService(
                ServiceOrderType.DESTROY, serviceDeploymentEntity);
    }

    /**
     * Get purge task by stored deploy service entity.
     *
     * @param serviceDeploymentEntity deploy service entity.
     * @return deploy task.
     */
    private DeployTask getPurgeTask(ServiceDeploymentEntity serviceDeploymentEntity) {
        // Get state of service.
        this.serviceDeploymentEntityHandler.validateServiceDeploymentStateForOrderType(
                serviceDeploymentEntity, ServiceOrderType.PURGE);
        return serviceDeploymentEntityConverter.getDeployTaskByStoredService(
                ServiceOrderType.PURGE, serviceDeploymentEntity);
    }

    /**
     * Get deploy task to redeploy the failed service by stored deploy service entity.
     *
     * @param serviceDeploymentEntity deploy service entity.
     * @return deploy task.
     */
    public DeployTask getRedeployTask(
            ServiceDeploymentEntity serviceDeploymentEntity, ServiceOrderType serviceOrderType) {
        this.serviceDeploymentEntityHandler.validateServiceDeploymentStateForOrderType(
                serviceDeploymentEntity, serviceOrderType);
        return serviceDeploymentEntityConverter.getDeployTaskByStoredService(
                ServiceOrderType.RETRY, serviceDeploymentEntity);
    }

    /**
     * Get latest service deployment status.
     *
     * @param serviceId service id.
     * @param lastKnownDeploymentState last known service deployment state.
     * @return DeferredResult.
     */
    public DeferredResult<DeploymentStatusUpdate> getLatestServiceDeploymentStatus(
            UUID serviceId, ServiceDeploymentState lastKnownDeploymentState) {
        DeferredResult<DeploymentStatusUpdate> stateDeferredResult = new DeferredResult<>();
        taskExecutor.execute(
                () -> {
                    try {
                        this.serviceDeploymentStatusChangePolling
                                .fetchServiceDeploymentStatusWithPolling(
                                        stateDeferredResult, serviceId, lastKnownDeploymentState);
                    } catch (RuntimeException exception) {
                        stateDeferredResult.setErrorResult(exception);
                    }
                });
        return stateDeferredResult;
    }

    /**
     * Get used service template entity by service id.
     *
     * @param serviceId id of deployed service.
     * @return service template entity used to deploy the service.
     */
    public ServiceTemplateEntity getOrderableServiceDetailsByServiceId(UUID serviceId) {
        ServiceDeploymentEntity deployedService =
                serviceDeploymentEntityHandler.getServiceDeploymentEntity(serviceId);
        return deployedService.getServiceTemplateEntity();
    }

    private Handler getHandler(String activeProfile, DeployerKind kind) {
        List<String> activeProfiles = Arrays.asList(activeProfile.split(","));
        if (kind.equals(DeployerKind.TERRAFORM)) {
            if (activeProfiles.contains(Handler.TERRA_BOOT.toValue())) {
                return Handler.TERRA_BOOT;
            } else {
                return Handler.TERRAFORM_LOCAL;
            }
        } else if (kind.equals(DeployerKind.OPEN_TOFU)) {
            if (activeProfiles.contains(Handler.TOFU_MAKER.toValue())) {
                return Handler.TOFU_MAKER;
            } else {
                return Handler.OPEN_TOFU_LOCAL;
            }
        } else {
            return Handler.TERRAFORM_LOCAL;
        }
    }
}
