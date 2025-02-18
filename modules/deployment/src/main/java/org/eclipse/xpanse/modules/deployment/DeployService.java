/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import static org.eclipse.xpanse.modules.async.TaskConfiguration.ASYNC_EXECUTOR_NAME;
import static org.eclipse.xpanse.modules.logging.LoggingKeyConstant.SERVICE_ID;

import jakarta.annotation.Resource;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
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
import org.eclipse.xpanse.modules.logging.CustomRequestIdGenerator;
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
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.InvalidServiceStateException;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.ServiceFlavorDowngradeNotAllowed;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.ServiceLockedException;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.ServiceModifyParamsNotFoundException;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.models.service.utils.ServiceDeployVariablesJsonSchemaValidator;
import org.eclipse.xpanse.modules.models.servicetemplate.DeployVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.FlavorsWithPrice;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceFlavor;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceFlavorWithPrice;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateUnavailableException;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.deployment.Deployer;
import org.eclipse.xpanse.modules.security.auth.UserServiceHelper;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
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

    @Resource
    private ServiceDeployVariablesJsonSchemaValidator serviceDeployVariablesJsonSchemaValidator;

    @Resource private PolicyValidator policyValidator;
    @Resource private SensitiveDataHandler sensitiveDataHandler;
    @Resource private ServiceDeploymentEntityHandler serviceDeploymentEntityHandler;
    @Resource private DeployResultManager deployResultManager;
    @Resource private DeployerKindManager deployerKindManager;
    @Resource private DeployServiceEntityConverter deployServiceEntityConverter;
    @Resource private ServiceOrderManager serviceOrderManager;
    @Resource private ServiceDeploymentStatusChangePolling serviceDeploymentStatusChangePolling;

    @Resource(name = ASYNC_EXECUTOR_NAME)
    private Executor taskExecutor;

    /**
     * Create order to deploy new service.
     *
     * @param deployRequest deploy request.
     * @return ServiceOrder.
     */
    public ServiceOrder createOrderToDeployNewService(DeployRequest deployRequest) {
        UUID newServiceId = UUID.randomUUID();
        MDC.put(SERVICE_ID, newServiceId.toString());
        deployRequest.setServiceId(newServiceId);
        deployRequest.setUserId(this.userServiceHelper.getCurrentUserId());
        DeployTask deployTask = createNewDeployTask(deployRequest);
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
        MDC.put(SERVICE_ID, serviceId.toString());
        ServiceDeploymentEntity serviceDeploymentEntity =
                getServiceOwnedByCurrentUser(serviceId, UserOperation.REDEPLOY_SERVICE);
        DeployTask redeployTask = getRedeployTask(serviceDeploymentEntity);
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
        MDC.put(SERVICE_ID, serviceId.toString());
        ServiceDeploymentEntity serviceDeploymentEntity =
                getServiceOwnedByCurrentUser(serviceId, UserOperation.MODIFY_SERVICE);
        modifyRequest.setUserId(this.userServiceHelper.getCurrentUserId());
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
        MDC.put(SERVICE_ID, serviceId.toString());
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
        MDC.put(SERVICE_ID, serviceId.toString());
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
    private DeployTask createNewDeployTask(DeployRequest deployRequest) {
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
        sensitiveDataHandler.encodeDeployVariable(
                availableServiceTemplate, deployRequest.getServiceRequestProperties());

        AvailabilityZonesRequestValidator.validateAvailabilityZones(
                deployRequest.getAvailabilityZones(),
                availableServiceTemplate.getOcl().getDeployment().getServiceAvailabilityConfig());
        if (StringUtils.isEmpty(deployRequest.getCustomerServiceName())) {
            deployRequest.setCustomerServiceName(generateCustomerServiceName(deployRequest));
        }
        DeployTask deployTask = new DeployTask();
        deployTask.setOrderId(CustomRequestIdGenerator.generateOrderId());
        deployTask.setServiceId(deployRequest.getServiceId());
        deployTask.setUserId(deployRequest.getUserId());
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
            List<DeployVariable> deployVariables =
                    existingServiceTemplate.getOcl().getDeployment().getVariables();

            serviceDeployVariablesJsonSchemaValidator.validateDeployVariables(
                    deployVariables,
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
        entity.setId(deployTask.getServiceId());
        entity.setCreateTime(OffsetDateTime.now());
        entity.setVersion(StringUtils.lowerCase(deployTask.getDeployRequest().getVersion()));
        entity.setName(StringUtils.lowerCase(deployTask.getDeployRequest().getServiceName()));
        entity.setCsp(deployTask.getDeployRequest().getCsp());
        entity.setCategory(deployTask.getDeployRequest().getCategory());
        entity.setCustomerServiceName(deployTask.getDeployRequest().getCustomerServiceName());
        entity.setFlavor(deployTask.getDeployRequest().getFlavor());
        entity.setUserId(deployTask.getUserId());
        entity.setDeployRequest(deployTask.getDeployRequest());
        entity.setDeployResources(new ArrayList<>());
        entity.setServiceVendor(deployTask.getServiceVendor());
        entity.setServiceDeploymentState(ServiceDeploymentState.DEPLOYING);
        entity.setServiceTemplateId(deployTask.getServiceTemplateId());
        ServiceLockConfig defaultLockConfig = new ServiceLockConfig();
        defaultLockConfig.setDestroyLocked(false);
        defaultLockConfig.setModifyLocked(false);
        entity.setLockConfig(defaultLockConfig);
        ServiceDeploymentEntity storedEntity = serviceDeploymentEntityHandler.storeAndFlush(entity);
        if (Objects.isNull(storedEntity)) {
            log.error(
                    "Store new deploy service entity with id {} failed.",
                    deployTask.getServiceId());
            throw new RuntimeException("Store new deploy service entity failed.");
        }
        return storedEntity;
    }

    private void deployService(DeployTask deployTask) {
        DeployResult deployResult;
        RuntimeException exception = null;
        DeployerKind kind = deployTask.getOcl().getDeployment().getDeployerTool().getKind();
        Deployer deployer = deployerKindManager.getDeployment(kind);
        ServiceDeploymentEntity serviceEntity = storeNewDeployServiceEntity(deployTask);
        ServiceOrderEntity serviceOrderEntity =
                serviceOrderManager.storeNewServiceOrderEntity(deployTask, serviceEntity);
        try {
            policyValidator.validateDeploymentWithPolicies(deployTask);
            serviceOrderManager.startOrderProgress(serviceOrderEntity);
            deployResult = deployer.deploy(deployTask);
        } catch (RuntimeException e) {
            exception = e;
            deployResult = deployResultManager.getFailedDeployResult(deployTask, exception);
        }
        deployResultManager.updateServiceWithDeployResult(deployResult);
        if (Objects.nonNull(exception)) {
            throw exception;
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
        ServiceOrderEntity serviceOrderEntity =
                serviceOrderManager.storeNewServiceOrderEntity(
                        redeployTask, serviceDeploymentEntity);
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
        deployResultManager.updateServiceWithDeployResult(redeployResult);
        if (Objects.nonNull(exception)) {
            throw exception;
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

        if (!serviceDeploymentEntity
                        .getServiceDeploymentState()
                        .equals(ServiceDeploymentState.DEPLOY_SUCCESS)
                && !serviceDeploymentEntity
                        .getServiceDeploymentState()
                        .equals(ServiceDeploymentState.MODIFICATION_FAILED)
                && !serviceDeploymentEntity
                        .getServiceDeploymentState()
                        .equals(ServiceDeploymentState.MODIFICATION_SUCCESSFUL)) {
            throw new InvalidServiceStateException(
                    String.format(
                            "Service %s with the state %s is not allowed to modify.",
                            serviceDeploymentEntity.getId(),
                            serviceDeploymentEntity.getServiceDeploymentState()));
        }
        ServiceTemplateEntity existingServiceTemplate =
                serviceTemplateStorage.getServiceTemplateById(
                        serviceDeploymentEntity.getServiceTemplateId());
        if (!existingServiceTemplate.getIsAvailableInCatalog()) {
            String errorMsg =
                    String.format(
                            "Used service template %s is unavailable to be used to modify service.",
                            existingServiceTemplate.getId());
            log.error(errorMsg);
            throw new ServiceTemplateUnavailableException(errorMsg);
        }
        DeployRequest previousDeployRequest = serviceDeploymentEntity.getDeployRequest();
        DeployRequest newDeployRequest = new DeployRequest();
        BeanUtils.copyProperties(previousDeployRequest, newDeployRequest);
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
        if (Objects.nonNull(modifyRequest.getServiceRequestProperties())
                && !modifyRequest.getServiceRequestProperties().isEmpty()) {
            newDeployRequest.setServiceRequestProperties(
                    modifyRequest.getServiceRequestProperties());
        }
        validateDeployRequestWithServiceTemplate(existingServiceTemplate, newDeployRequest);
        DeployTask modifyTask =
                deployServiceEntityConverter.getDeployTaskByStoredService(
                        ServiceOrderType.MODIFY, serviceDeploymentEntity);
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
        MDC.put(SERVICE_ID, modifyTask.getServiceId().toString());
        DeployerKind kind = modifyTask.getOcl().getDeployment().getDeployerTool().getKind();
        Deployer deployer = deployerKindManager.getDeployment(kind);
        ServiceOrderEntity serviceOrderEntity =
                serviceOrderManager.storeNewServiceOrderEntity(modifyTask, serviceDeployment);
        try {
            serviceDeployment.setDeployRequest(modifyTask.getDeployRequest());
            serviceDeploymentEntityHandler.updateServiceDeploymentStatus(
                    serviceDeployment, ServiceDeploymentState.MODIFYING);
            serviceOrderManager.startOrderProgress(serviceOrderEntity);
            modifyResult = deployer.modify(modifyTask);
        } catch (RuntimeException e) {
            exception = e;
            modifyResult = deployResultManager.getFailedDeployResult(modifyTask, e);
        }
        deployResultManager.updateServiceWithDeployResult(modifyResult);
        if (Objects.nonNull(exception)) {
            throw exception;
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
        MDC.put(SERVICE_ID, destroyTask.getServiceId().toString());
        DeployerKind kind = destroyTask.getOcl().getDeployment().getDeployerTool().getKind();
        Deployer deployer = deployerKindManager.getDeployment(kind);
        ServiceOrderEntity serviceOrderEntity =
                serviceOrderManager.storeNewServiceOrderEntity(
                        destroyTask, serviceDeploymentEntity);
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
        deployResultManager.updateServiceWithDeployResult(destroyResult);
        if (Objects.nonNull(exception)) {
            throw exception;
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
        MDC.put(SERVICE_ID, purgeTask.getServiceId().toString());
        DeployerKind kind = purgeTask.getOcl().getDeployment().getDeployerTool().getKind();
        Deployer deployer = deployerKindManager.getDeployment(kind);
        ServiceOrderEntity serviceOrderEntity =
                serviceOrderManager.storeNewServiceOrderEntity(purgeTask, serviceDeployment);
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
            deployResultManager.updateServiceWithDeployResult(purgeResult);
            if (Objects.nonNull(exception)) {
                throw exception;
            }
        } else {
            log.info("No resources of service {} need to clear", purgeTask.getServiceId());
            serviceDeploymentStorage.deleteServiceDeployment(serviceDeployment);
        }
    }

    /**
     * Start a new order to execute the deployment task in workflow.
     *
     * @param originalServiceId original service id.
     * @param workflowId workflow id.
     * @param parentOrderId parent order id.
     * @param deployRequest deployRequest
     * @return serviceOrder
     */
    public ServiceOrder deployServiceByWorkflow(
            UUID originalServiceId,
            String workflowId,
            UUID parentOrderId,
            DeployRequest deployRequest) {
        MDC.put(SERVICE_ID, deployRequest.getServiceId().toString());
        // check if the new service is already deployed.
        ServiceDeploymentEntity deployServiceEntity =
                serviceDeploymentStorage.findServiceDeploymentById(deployRequest.getServiceId());
        if (Objects.nonNull(deployServiceEntity)) {
            // retry to deploy the service.
            DeployTask retryTask = getRedeployTask(deployServiceEntity);
            retryTask.setOriginalServiceId(originalServiceId);
            retryTask.setParentOrderId(parentOrderId);
            retryTask.setWorkflowId(workflowId);
            redeployService(retryTask, deployServiceEntity);
            return new ServiceOrder(retryTask.getOrderId(), retryTask.getServiceId());
        } else {
            DeployTask deployTask = createNewDeployTask(deployRequest);
            deployTask.setOriginalServiceId(originalServiceId);
            deployTask.setParentOrderId(parentOrderId);
            deployTask.setWorkflowId(workflowId);
            deployService(deployTask);
            return new ServiceOrder(deployTask.getOrderId(), deployTask.getServiceId());
        }
    }

    /**
     * Start a new order to execute the destroy task in workflow.
     *
     * @param originalServiceId original service id.
     * @param workflowId workflow id.
     * @param parentOrderId parent order id.
     * @return serviceOrder.
     */
    public ServiceOrder destroyServiceByWorkflow(
            UUID originalServiceId, String workflowId, UUID parentOrderId) {
        MDC.put(SERVICE_ID, originalServiceId.toString());
        ServiceDeploymentEntity deployServiceEntity =
                serviceDeploymentEntityHandler.getServiceDeploymentEntity(originalServiceId);
        DeployTask destroyTask =
                deployServiceEntityConverter.getDeployTaskByStoredService(
                        ServiceOrderType.DESTROY, deployServiceEntity);
        destroyTask.setOriginalServiceId(originalServiceId);
        destroyTask.setWorkflowId(workflowId);
        destroyTask.setParentOrderId(parentOrderId);
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
        ServiceDeploymentState state = serviceDeploymentEntity.getServiceDeploymentState();
        if (state.equals(ServiceDeploymentState.DEPLOYING)
                || state.equals(ServiceDeploymentState.DESTROYING)
                || state.equals(ServiceDeploymentState.MODIFYING)) {
            throw new InvalidServiceStateException(
                    String.format(
                            "Service %s with the state %s is not allowed to destroy.",
                            serviceDeploymentEntity.getId(), state));
        }
        return deployServiceEntityConverter.getDeployTaskByStoredService(
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
        ServiceDeploymentState state = serviceDeploymentEntity.getServiceDeploymentState();
        if (!(state == ServiceDeploymentState.DEPLOY_FAILED
                || state == ServiceDeploymentState.DESTROY_SUCCESS
                || state == ServiceDeploymentState.DESTROY_FAILED
                || state == ServiceDeploymentState.ROLLBACK_FAILED
                || state == ServiceDeploymentState.MANUAL_CLEANUP_REQUIRED)) {
            throw new InvalidServiceStateException(
                    String.format(
                            "Service %s with the state %s is not allowed to purge.",
                            serviceDeploymentEntity.getId(), state));
        }
        return deployServiceEntityConverter.getDeployTaskByStoredService(
                ServiceOrderType.PURGE, serviceDeploymentEntity);
    }

    /**
     * Get deploy task to redeploy the failed service by stored deploy service entity.
     *
     * @param serviceDeploymentEntity deploy service entity.
     * @return deploy task.
     */
    public DeployTask getRedeployTask(ServiceDeploymentEntity serviceDeploymentEntity) {
        MDC.put(SERVICE_ID, serviceDeploymentEntity.getId().toString());
        // Get state of service.
        ServiceDeploymentState state = serviceDeploymentEntity.getServiceDeploymentState();
        // Retry's deploy service trigger when service deployment state is DEPLOYMENT_FAILED
        // Recreate's deploy service trigger when service deployment state is DESTROY_SUCCESS
        if (!(state == ServiceDeploymentState.DEPLOY_FAILED
                || state == ServiceDeploymentState.DESTROY_FAILED
                || state == ServiceDeploymentState.DESTROY_SUCCESS
                || state == ServiceDeploymentState.ROLLBACK_FAILED)) {
            throw new InvalidServiceStateException(
                    String.format(
                            "Service %s with the state %s is not allowed to redeploy.",
                            serviceDeploymentEntity.getId(), state));
        }
        return deployServiceEntityConverter.getDeployTaskByStoredService(
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
        return serviceTemplateStorage.getServiceTemplateById(
                deployedService.getServiceTemplateId());
    }
}
