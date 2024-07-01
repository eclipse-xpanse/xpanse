/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import static org.eclipse.xpanse.modules.logging.CustomRequestIdGenerator.TASK_ID;

import jakarta.annotation.Resource;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceStorage;
import org.eclipse.xpanse.modules.database.servicemodification.ServiceModificationAuditEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.deployment.exceptions.DeploymentFailedException;
import org.eclipse.xpanse.modules.models.service.config.ServiceLockConfig;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.BillingModeNotSupported;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.EulaNotAccepted;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.FlavorInvalidException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.InvalidServiceStateException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceFlavorDowngradeNotAllowed;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceModifyParamsNotFoundException;
import org.eclipse.xpanse.modules.models.service.enums.DeployerTaskStatus;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.modify.ModifyRequest;
import org.eclipse.xpanse.modules.models.service.utils.ServiceVariablesJsonSchemaValidator;
import org.eclipse.xpanse.modules.models.servicetemplate.DeployVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.FlavorsWithPrice;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceFlavor;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceFlavorWithPrice;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceRegistrationState;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotApproved;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotRegistered;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.deployment.Deployer;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeploymentScenario;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * Main class which orchestrates the OCL request processing. Calls the available plugins to deploy
 * managed service in the respective infrastructure as defined in the OCL.
 */
@Slf4j
@Component
public class DeployService {

    @Resource
    private ServiceTemplateStorage serviceTemplateStorage;
    @Resource
    private DeployServiceStorage deployServiceStorage;
    @Resource
    private ServiceVariablesJsonSchemaValidator serviceVariablesJsonSchemaValidator;
    @Resource
    private PolicyValidator policyValidator;
    @Resource
    private SensitiveDataHandler sensitiveDataHandler;
    @Resource
    private DeployServiceEntityHandler deployServiceEntityHandler;
    @Resource
    private DeployResultManager deployResultManager;
    @Resource
    private DeployerKindManager deployerKindManager;
    @Resource
    private DeployServiceEntityToDeployTaskConverter deployServiceEntityToDeployTaskConverter;
    @Resource
    private ServiceStateManager serviceStateManager;
    @Resource
    private ServiceModificationAuditManager modificationAuditManager;

    /**
     * Create new deploy task by deploy request.
     *
     * @param deployRequest deploy request.
     * @return new deploy task.
     */
    public DeployTask createNewDeployTask(DeployRequest deployRequest) {
        // Find the approved service template and fill Ocl.
        ServiceTemplateEntity searchServiceTemplate = new ServiceTemplateEntity();
        searchServiceTemplate.setName(StringUtils.lowerCase(deployRequest.getServiceName()));
        searchServiceTemplate.setVersion(StringUtils.lowerCase(deployRequest.getVersion()));
        searchServiceTemplate.setCsp(deployRequest.getCsp());
        searchServiceTemplate.setCategory(deployRequest.getCategory());
        searchServiceTemplate.setServiceHostingType(deployRequest.getServiceHostingType());
        ServiceTemplateEntity existingServiceTemplate =
                serviceTemplateStorage.findServiceTemplate(searchServiceTemplate);
        if (Objects.isNull(existingServiceTemplate) || Objects.isNull(
                existingServiceTemplate.getOcl())) {
            throw new ServiceTemplateNotRegistered("No available service templates found.");
        }
        if (ServiceRegistrationState.APPROVED
                != existingServiceTemplate.getServiceRegistrationState()) {
            String errMsg = String.format("Found service template with id %s but not approved.",
                    existingServiceTemplate.getId());
            log.error(errMsg);
            throw new ServiceTemplateNotApproved("No available service templates found.");
        }
        if (StringUtils.isNotBlank(existingServiceTemplate.getOcl().getEula())
                && !deployRequest.isEulaAccepted()) {
            log.error("Service not accepted Eula.");
            throw new EulaNotAccepted("Service not accepted Eula.");
        }
        if (!existingServiceTemplate.getOcl().getBilling().getBillingModes()
                .contains(deployRequest.getBillingMode())) {
            String errorMsg = String.format(
                    "The service template with id %s does not support billing mode %s.",
                    existingServiceTemplate.getId(), deployRequest.getBillingMode());
            log.error(errorMsg);
            throw new BillingModeNotSupported(errorMsg);
        }
        // Check context validation
        validateDeployRequestWithServiceTemplate(existingServiceTemplate, deployRequest);
        sensitiveDataHandler.encodeDeployVariable(existingServiceTemplate,
                deployRequest.getServiceRequestProperties());

        AvailabilityZonesRequestValidator.validateAvailabilityZones(
                deployRequest.getAvailabilityZones(),
                existingServiceTemplate.getOcl().getDeployment().getServiceAvailabilityConfigs());

        deployRequest.setServiceId(UUID.randomUUID());
        if (StringUtils.isEmpty(deployRequest.getCustomerServiceName())) {
            deployRequest.setCustomerServiceName(generateCustomerServiceName(deployRequest));
        }
        // Create new deploy task by deploy request.
        return getDeployTask(deployRequest, existingServiceTemplate);
    }

    private DeployTask getDeployTask(DeployRequest deployRequest,
                                     ServiceTemplateEntity existingServiceTemplate) {
        DeployTask deployTask = new DeployTask();
        deployTask.setId(deployRequest.getServiceId());
        deployTask.setDeployRequest(deployRequest);
        deployTask.setNamespace(existingServiceTemplate.getNamespace());
        deployTask.setOcl(existingServiceTemplate.getOcl());
        if (Objects.nonNull(existingServiceTemplate.getId())) {
            deployTask.setServiceTemplateId(existingServiceTemplate.getId());
        } else {
            throw new ServiceTemplateNotRegistered("service template id can't be null.");
        }
        return deployTask;
    }

    private void validateDeployRequestWithServiceTemplate(
            ServiceTemplateEntity existingServiceTemplate, DeployRequest deployRequest) {
        // Check context validation
        if (Objects.nonNull(existingServiceTemplate.getOcl().getDeployment()) && Objects.nonNull(
                deployRequest.getServiceRequestProperties())) {
            List<DeployVariable> deployVariables =
                    existingServiceTemplate.getOcl().getDeployment().getVariables();

            serviceVariablesJsonSchemaValidator.validateDeployVariables(deployVariables,
                    deployRequest.getServiceRequestProperties(),
                    existingServiceTemplate.getJsonObjectSchema());
        }
        getServiceFlavorWithName(deployRequest.getFlavor(),
                existingServiceTemplate.getOcl().getFlavors());
    }

    private ServiceFlavorWithPrice getServiceFlavorWithName(String flavorName,
                                                            FlavorsWithPrice flavors) {
        Optional<ServiceFlavorWithPrice> flavorOptional = flavors.getServiceFlavors().stream()
                .filter(flavor -> flavor.getName().equals(flavorName)).findAny();
        if (flavorOptional.isEmpty()) {
            throw new FlavorInvalidException(
                    String.format("Could not find service flavor with name %s", flavorName));
        }
        return flavorOptional.get();
    }

    private String generateCustomerServiceName(DeployRequest deployRequest) {
        if (deployRequest.getServiceName().length() > 5) {
            return deployRequest.getServiceName().substring(0, 4) + "-"
                    + RandomStringUtils.randomAlphanumeric(5);
        } else {
            return deployRequest.getServiceName() + "-" + RandomStringUtils.randomAlphanumeric(5);
        }
    }

    private DeployServiceEntity storeNewDeployServiceEntity(DeployTask deployTask) {
        DeployServiceEntity entity = new DeployServiceEntity();
        entity.setId(deployTask.getId());
        entity.setCreateTime(OffsetDateTime.now());
        entity.setVersion(StringUtils.lowerCase(deployTask.getDeployRequest().getVersion()));
        entity.setName(StringUtils.lowerCase(deployTask.getDeployRequest().getServiceName()));
        entity.setCsp(deployTask.getDeployRequest().getCsp());
        entity.setCategory(deployTask.getDeployRequest().getCategory());
        entity.setCustomerServiceName(deployTask.getDeployRequest().getCustomerServiceName());
        entity.setFlavor(deployTask.getDeployRequest().getFlavor());
        entity.setUserId(deployTask.getDeployRequest().getUserId());
        entity.setDeployRequest(deployTask.getDeployRequest());
        entity.setDeployResourceList(new ArrayList<>());
        entity.setNamespace(deployTask.getNamespace());
        entity.setServiceDeploymentState(ServiceDeploymentState.DEPLOYING);
        if (Objects.nonNull(deployTask.getServiceTemplateId())) {
            entity.setServiceTemplateId(deployTask.getServiceTemplateId());
        } else {
            throw new ServiceTemplateNotRegistered("service template id can't be null.");
        }
        ServiceLockConfig defaultLockConfig = new ServiceLockConfig();
        defaultLockConfig.setDestroyLocked(false);
        defaultLockConfig.setModifyLocked(false);
        entity.setLockConfig(defaultLockConfig);
        DeployServiceEntity storedEntity = deployServiceEntityHandler.storeAndFlush(entity);
        if (Objects.isNull(storedEntity)) {
            log.error("Store new deploy service entity with id:{} failed.", deployTask.getId());
            throw new RuntimeException("Store new deploy service entity failed.");
        }
        return storedEntity;
    }

    /**
     * Async method to deploy service.
     *
     * @param deployTask deployTask
     */
    public void deployService(DeployTask deployTask) {
        deployTask.setDeploymentScenario(DeploymentScenario.DEPLOY);
        deploy(deployTask);
    }

    private void deploy(DeployTask deployTask) {
        MDC.put(TASK_ID, deployTask.getId().toString());
        DeployResult deployResult;
        Exception exception = null;
        String errorMsg = null;
        DeployServiceEntity storedEntity = storeNewDeployServiceEntity(deployTask);
        DeployerKind deployerKind = deployTask.getOcl().getDeployment().getKind();
        Deployer deployer = deployerKindManager.getDeployment(deployerKind);
        try {
            policyValidator.validateDeploymentWithPolicies(deployTask);
            deployResult = deployer.deploy(deployTask);
        } catch (RuntimeException e) {
            exception = e;
            errorMsg = String.format("Deploy service %s failed. Error message:\n %s",
                    deployTask.getId(), e.getMessage());
            deployResult = new DeployResult();
            deployResult.setId(deployTask.getId());
            deployResult.setState(getDeployerTaskFailedState(deployTask.getDeploymentScenario()));
            deployResult.setMessage(errorMsg);
        }
        DeployServiceEntity updatedDeployServiceEntity =
                deployResultManager.updateDeployServiceEntityWithDeployResult(deployResult,
                        storedEntity);
        if (ServiceDeploymentState.DEPLOY_FAILED
                == updatedDeployServiceEntity.getServiceDeploymentState()) {
            rollbackOnDeploymentFailure(deployTask, updatedDeployServiceEntity);
        }
        if (Objects.nonNull(exception) && Objects.nonNull(errorMsg)) {
            throw new DeploymentFailedException(errorMsg);
        }
    }

    /**
     * Redeploy service with failed state.
     *
     * @param serviceToRedeploy serviceToRedeploy
     */
    public void redeployService(DeployServiceEntity serviceToRedeploy) {
        MDC.put(TASK_ID, serviceToRedeploy.getId().toString());
        DeployResult redeployResult;
        Exception exception = null;
        String errorMsg = null;
        DeployTask redeployTask = getRedeployTask(serviceToRedeploy);
        DeployerKind deployerKind = redeployTask.getOcl().getDeployment().getKind();
        Deployer deployer = deployerKindManager.getDeployment(deployerKind);
        try {
            serviceToRedeploy.setServiceDeploymentState(ServiceDeploymentState.DEPLOYING);
            serviceToRedeploy = deployServiceEntityHandler.storeAndFlush(serviceToRedeploy);
            policyValidator.validateDeploymentWithPolicies(redeployTask);
            redeployResult = deployer.deploy(redeployTask);
        } catch (RuntimeException e) {
            exception = e;
            errorMsg = String.format("Redeploy service %s failed. Error message:\n %s",
                    redeployTask.getId(), exception.getMessage());
            redeployResult = new DeployResult();
            redeployResult.setId(redeployTask.getId());
            redeployResult.setState(getDeployerTaskFailedState(DeploymentScenario.DEPLOY));
            redeployResult.setMessage(errorMsg);
        }
        DeployServiceEntity updatedDeployServiceEntity =
                deployResultManager.updateDeployServiceEntityWithDeployResult(redeployResult,
                        serviceToRedeploy);
        if (ServiceDeploymentState.DEPLOY_FAILED
                == updatedDeployServiceEntity.getServiceDeploymentState()) {
            rollbackOnDeploymentFailure(redeployTask, updatedDeployServiceEntity);
        }
        if (Objects.nonNull(exception) && Objects.nonNull(errorMsg)) {
            log.error(errorMsg);
            throw new DeploymentFailedException(errorMsg);
        }
    }


    /**
     * Perform rollback when deployment fails and destroy the created resources.
     */
    public void rollbackOnDeploymentFailure(DeployTask deployTask,
                                            DeployServiceEntity deployServiceEntity) {
        log.info("Performing rollback of already provisioned resources.");
        if (Objects.nonNull(deployServiceEntity.getDeployResourceList())
                && !deployServiceEntity.getDeployResourceList().isEmpty()) {
            log.info("Rollback to destroy created resources for service with ID: {}",
                    deployTask.getId());
            deployTask.setDeploymentScenario(DeploymentScenario.ROLLBACK);
            destroy(deployTask, deployServiceEntity);
        }
    }

    /**
     * Method to change lock config of service.
     *
     * @param config              serviceLockConfig
     * @param deployServiceEntity deployServiceEntity
     */
    public void changeServiceLockConfig(ServiceLockConfig config,
                                        DeployServiceEntity deployServiceEntity) {
        deployServiceEntity.setLockConfig(config);
        deployServiceStorage.storeAndFlush(deployServiceEntity);
    }

    /**
     * Get modify task by stored deploy service entity.
     *
     * @param deployServiceEntity deploy service entity.
     */
    public DeployTask getModifyTask(ModifyRequest modifyRequest,
                                    DeployServiceEntity deployServiceEntity) {
        if (StringUtils.isBlank(modifyRequest.getFlavor()) && Objects.isNull(
                modifyRequest.getServiceRequestProperties())) {
            throw new ServiceModifyParamsNotFoundException("No params found for modify services.");
        }

        if (!deployServiceEntity.getServiceDeploymentState()
                .equals(ServiceDeploymentState.DEPLOY_SUCCESS)
                && !deployServiceEntity.getServiceDeploymentState()
                .equals(ServiceDeploymentState.MODIFICATION_FAILED)
                && !deployServiceEntity.getServiceDeploymentState()
                .equals(ServiceDeploymentState.MODIFICATION_SUCCESSFUL)) {
            throw new InvalidServiceStateException(
                    String.format("Service %s with the state %s is not allowed to modify.",
                            deployServiceEntity.getId(),
                            deployServiceEntity.getServiceDeploymentState()));
        }
        ServiceTemplateEntity existingServiceTemplate =
                serviceTemplateStorage.getServiceTemplateById(
                        deployServiceEntity.getServiceTemplateId());
        DeployRequest previousDeployRequest = deployServiceEntity.getDeployRequest();
        DeployRequest newDeployRequest = new DeployRequest();
        BeanUtils.copyProperties(previousDeployRequest, newDeployRequest);
        if (StringUtils.isNotEmpty(modifyRequest.getCustomerServiceName())) {
            newDeployRequest.setCustomerServiceName(modifyRequest.getCustomerServiceName());
        }
        if (StringUtils.isNotBlank(modifyRequest.getFlavor())) {
            validateFlavorDowngradedIsAllowed(deployServiceEntity.getFlavor(),
                    modifyRequest.getFlavor(), existingServiceTemplate.getOcl().getFlavors());
            newDeployRequest.setFlavor(modifyRequest.getFlavor());
        }
        if (Objects.nonNull(modifyRequest.getServiceRequestProperties())
                && !modifyRequest.getServiceRequestProperties().isEmpty()) {
            newDeployRequest.setServiceRequestProperties(
                    modifyRequest.getServiceRequestProperties());
        }
        validateDeployRequestWithServiceTemplate(existingServiceTemplate, newDeployRequest);
        DeployTask modifyTask = new DeployTask();
        modifyTask.setId(deployServiceEntity.getId());
        modifyTask.setDeployRequest(newDeployRequest);
        modifyTask.setOcl(existingServiceTemplate.getOcl());
        modifyTask.setDeploymentScenario(DeploymentScenario.MODIFY);
        return modifyTask;
    }

    private void validateFlavorDowngradedIsAllowed(String originalFlavor, String newFlavor,
                                                   FlavorsWithPrice flavors) {
        if (!flavors.isDowngradeAllowed()) {
            ServiceFlavor newServiceFlavor = getServiceFlavorWithName(newFlavor, flavors);
            ServiceFlavor originalServiceFlavor = getServiceFlavorWithName(originalFlavor, flavors);
            if (newServiceFlavor.getPriority() > originalServiceFlavor.getPriority()) {
                String errorMsg = String.format("Downgrading of flavors is not allowed. New flavor"
                                + " priority %d is lower than the original flavor priority %d.",
                        newServiceFlavor.getPriority(), originalServiceFlavor.getPriority());
                throw new ServiceFlavorDowngradeNotAllowed(errorMsg);
            }
        }
    }

    /**
     * Async method to modify service.
     *
     * @param modificationId      modificationId.
     * @param modifyTask          modifyTask.
     * @param deployServiceEntity deployServiceEntity
     */
    public void modifyService(UUID modificationId, DeployTask modifyTask,
                              DeployServiceEntity deployServiceEntity) {
        MDC.put(TASK_ID, modifyTask.getId().toString());
        Exception exception = null;
        String errorMsg = null;
        DeployResult modifyResult;
        Deployer deployer =
                deployerKindManager.getDeployment(modifyTask.getOcl().getDeployment().getKind());
        ServiceModificationAuditEntity modificationAuditEntity =
                modificationAuditManager.createNewModificationAudit(
                        modificationId, modifyTask, deployServiceEntity);
        try {
            deployServiceEntity.setDeployRequest(modifyTask.getDeployRequest());
            deployServiceEntity.setServiceDeploymentState(ServiceDeploymentState.MODIFYING);
            deployServiceStorage.storeAndFlush(deployServiceEntity);
            modificationAuditManager.startModificationProgressById(modificationId);
            modifyResult = deployer.modify(modificationId, modifyTask);
        } catch (RuntimeException e) {
            exception = e;
            errorMsg = String.format("Modify service %s failed. Error message:\n %s",
                    modifyTask.getId(), exception.getMessage());
            modifyResult = new DeployResult();
            modifyResult.setId(modifyTask.getId());
            modifyResult.setState(getDeployerTaskFailedState(modifyTask.getDeploymentScenario()));
            modifyResult.setMessage(errorMsg);
        }
        deployResultManager.updateDeployServiceEntityWithDeployResult(modifyResult,
                deployServiceEntity);
        modificationAuditManager.updateModificationAuditWithDeployResult(
                modificationAuditEntity, modifyResult);
        if (Objects.nonNull(exception) && Objects.nonNull(errorMsg)) {
            log.error(errorMsg);
            throw new DeploymentFailedException(errorMsg);
        }
    }

    /**
     * Async method to destroy service.
     *
     * @param destroyTask         destroyTask.
     * @param deployServiceEntity deployServiceEntity
     */
    public void destroyService(DeployTask destroyTask, DeployServiceEntity deployServiceEntity) {
        destroyTask.setDeploymentScenario(DeploymentScenario.DESTROY);
        destroy(destroyTask, deployServiceEntity);
    }

    private void destroy(DeployTask destroyTask, DeployServiceEntity deployServiceEntity) {
        MDC.put(TASK_ID, destroyTask.getId().toString());
        DeployResult destroyResult;
        Exception exception = null;
        String errorMsg = null;
        Deployer deployer =
                deployerKindManager.getDeployment(destroyTask.getOcl().getDeployment().getKind());
        try {
            if (DeploymentScenario.ROLLBACK != destroyTask.getDeploymentScenario()) {
                deployServiceEntity.setServiceDeploymentState(ServiceDeploymentState.DESTROYING);
            }
            deployServiceStorage.storeAndFlush(deployServiceEntity);
            destroyResult = deployer.destroy(destroyTask);
        } catch (RuntimeException e) {
            exception = e;
            errorMsg = String.format("Destroy service %s failed. Error message:\n %s",
                    destroyTask.getId(), exception.getMessage());
            destroyResult = new DeployResult();
            destroyResult.setId(destroyTask.getId());
            destroyResult.setState(getDeployerTaskFailedState(destroyTask.getDeploymentScenario()));
            destroyResult.setMessage(errorMsg);
        }
        DeployServiceEntity updatedDeployServiceEntity =
                deployResultManager.updateDeployServiceEntityWithDeployResult(destroyResult,
                        deployServiceEntity);
        if (ServiceDeploymentState.DESTROY_SUCCESS
                == updatedDeployServiceEntity.getServiceDeploymentState()) {
            deployer.deleteTaskWorkspace(destroyTask.getId());
        }

        if (Objects.nonNull(exception) && Objects.nonNull(errorMsg)) {
            log.error(errorMsg);
            throw new DeploymentFailedException(errorMsg);
        }
    }

    private DeployerTaskStatus getDeployerTaskFailedState(DeploymentScenario deploymentScenario) {
        return switch (deploymentScenario) {
            case DEPLOY -> DeployerTaskStatus.DEPLOY_FAILED;
            case DESTROY -> DeployerTaskStatus.DESTROY_FAILED;
            case ROLLBACK -> DeployerTaskStatus.ROLLBACK_FAILED;
            case PURGE -> DeployerTaskStatus.PURGE_FAILED;
            case MODIFY -> DeployerTaskStatus.MODIFICATION_FAILED;
        };
    }

    /**
     * purge the service based on the serviceDeploymentState.
     *
     * @param destroyTask         destroyTask
     * @param deployServiceEntity deployServiceEntity
     */
    public void purgeService(DeployTask destroyTask, DeployServiceEntity deployServiceEntity) {
        MDC.put(TASK_ID, destroyTask.getId().toString());
        Exception exception = null;
        String errorMsg = null;
        try {
            if (Objects.nonNull(deployServiceEntity.getDeployResourceList())
                    && !deployServiceEntity.getDeployResourceList().isEmpty()) {
                log.info("destroying created resources for service with ID: {}",
                        destroyTask.getId());
                destroyTask.setDeploymentScenario(DeploymentScenario.PURGE);
                destroy(destroyTask, deployServiceEntity);
            }
            deployServiceStorage.deleteDeployService(deployServiceEntity);
            serviceStateManager.deleteManagementTasksByServiceId(destroyTask.getId());
            log.info("Database entry with ID {} purged.", deployServiceEntity.getId());
        } catch (RuntimeException e) {
            exception = e;
            errorMsg = String.format("Purge service %s failed. Error message:\n %s",
                    destroyTask.getId(), exception.getMessage());
        }
        if (Objects.nonNull(exception) && Objects.nonNull(errorMsg)) {
            log.error(errorMsg);
            throw new DeploymentFailedException(errorMsg);
        }
    }

    /**
     * Deployment service.
     *
     * @param newId         new service id.
     * @param userId        user id.
     * @param deployRequest deploy request.
     */
    public void deployServiceById(UUID newId, String userId, DeployRequest deployRequest) {
        MDC.put(TASK_ID, newId.toString());
        log.info("Migrate workflow start deploy new service with id: {}", newId);
        DeployTask deployTask = createNewDeployTask(deployRequest);
        deployTask.setDeploymentScenario(DeploymentScenario.DEPLOY);
        // override task id and user id.
        deployTask.setId(newId);
        deployTask.getDeployRequest().setUserId(userId);
        deploy(deployTask);
    }

    /**
     * Destroy service by deployed service id.
     */
    public void destroyServiceById(String id) {
        MDC.put(TASK_ID, id);
        DeployServiceEntity deployServiceEntity =
                deployServiceEntityHandler.getDeployServiceEntity(UUID.fromString(id));
        DeployTask deployTask =
                deployServiceEntityToDeployTaskConverter.getDeployTaskByStoredService(
                        deployServiceEntity);
        deployTask.setDeploymentScenario(DeploymentScenario.DESTROY);
        destroy(deployTask, deployServiceEntity);
    }

    /**
     * Get destroy task by stored deploy service entity.
     *
     * @param deployServiceEntity deploy service entity.
     */
    public DeployTask getDestroyTask(DeployServiceEntity deployServiceEntity) {

        // Get state of service.
        ServiceDeploymentState state = deployServiceEntity.getServiceDeploymentState();
        if (state.equals(ServiceDeploymentState.DEPLOYING) || state.equals(
                ServiceDeploymentState.DESTROYING) || state.equals(
                ServiceDeploymentState.MODIFYING)) {
            throw new InvalidServiceStateException(
                    String.format("Service %s with the state %s is not allowed to destroy.",
                            deployServiceEntity.getId(), state));
        }
        deployServiceEntity.setServiceDeploymentState(ServiceDeploymentState.DESTROYING);
        DeployServiceEntity updatedDeployServiceEntity =
                deployServiceStorage.storeAndFlush(deployServiceEntity);
        return deployServiceEntityToDeployTaskConverter.getDeployTaskByStoredService(
                updatedDeployServiceEntity);
    }

    /**
     * Get purge task by stored deploy service entity.
     *
     * @param deployServiceEntity deploy service entity.
     * @return deploy task.
     */
    public DeployTask getPurgeTask(DeployServiceEntity deployServiceEntity) {
        // Get state of service.
        ServiceDeploymentState state = deployServiceEntity.getServiceDeploymentState();
        if (!(state == ServiceDeploymentState.DEPLOY_FAILED
                || state == ServiceDeploymentState.DESTROY_SUCCESS
                || state == ServiceDeploymentState.DESTROY_FAILED
                || state == ServiceDeploymentState.ROLLBACK_FAILED
                || state == ServiceDeploymentState.MANUAL_CLEANUP_REQUIRED)) {
            throw new InvalidServiceStateException(
                    String.format("Service %s with the state %s is not allowed to purge.",
                            deployServiceEntity.getId(), state));
        }
        return deployServiceEntityToDeployTaskConverter.getDeployTaskByStoredService(
                deployServiceEntity);
    }


    /**
     * Get deploy task to redeploy the failed service by stored deploy service entity.
     *
     * @param deployServiceEntity deploy service entity.
     * @return deploy task.
     */
    public DeployTask getRedeployTask(DeployServiceEntity deployServiceEntity) {
        // Get state of service.
        ServiceDeploymentState state = deployServiceEntity.getServiceDeploymentState();
        if (!(state == ServiceDeploymentState.DEPLOY_FAILED
                || state == ServiceDeploymentState.DESTROY_FAILED
                || state == ServiceDeploymentState.ROLLBACK_FAILED)) {
            throw new InvalidServiceStateException(
                    String.format("Service %s with the state %s is not allowed to redeploy.",
                            deployServiceEntity.getId(), state));
        }
        return deployServiceEntityToDeployTaskConverter.getDeployTaskByStoredService(
                deployServiceEntity);
    }
}
