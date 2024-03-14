/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import jakarta.annotation.Resource;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceStorage;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployerTaskStatus;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.InvalidServiceStateException;
import org.eclipse.xpanse.modules.models.service.utils.ServiceVariablesJsonSchemaValidator;
import org.eclipse.xpanse.modules.models.servicetemplate.DeployVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceRegistrationState;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotApproved;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotRegistered;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.deployment.Deployer;
import org.eclipse.xpanse.modules.orchestrator.deployment.DestroyScenario;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * Main class which orchestrates the OCL request processing. Calls the available plugins to deploy
 * managed service in the respective infrastructure as defined in the OCL.
 */
@Slf4j
@Component
public class DeployService {

    private static final String TASK_ID = "TASK_ID";

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
        if (Objects.isNull(existingServiceTemplate)
                || Objects.isNull(existingServiceTemplate.getOcl())) {
            throw new ServiceTemplateNotRegistered("No available service templates found.");
        }
        if (ServiceRegistrationState.APPROVED
                != existingServiceTemplate.getServiceRegistrationState()) {
            String errMsg = String.format("Found service template with id %s but not approved.",
                    existingServiceTemplate.getId());
            log.error(errMsg);
            throw new ServiceTemplateNotApproved("No available service templates found.");
        }
        // Check context validation
        if (Objects.nonNull(existingServiceTemplate.getOcl().getDeployment())
                && Objects.nonNull(deployRequest.getServiceRequestProperties())) {
            List<DeployVariable> deployVariables =
                    existingServiceTemplate.getOcl().getDeployment().getVariables();

            serviceVariablesJsonSchemaValidator.validateDeployVariables(deployVariables,
                    deployRequest.getServiceRequestProperties(),
                    existingServiceTemplate.getJsonObjectSchema());
        }
        sensitiveDataHandler.encodeDeployVariable(existingServiceTemplate,
                deployRequest.getServiceRequestProperties());

        AvailabilityZonesRequestValidator.validateAvailabilityZones(
                deployRequest.getAvailabilityZones(),
                existingServiceTemplate.getOcl().getDeployment().getServiceAvailability());

        if (StringUtils.isEmpty(deployRequest.getCustomerServiceName())) {
            deployRequest.setCustomerServiceName(generateCustomerServiceName(deployRequest));
        }
        // Create new deploy task by deploy request.
        DeployTask deployTask = new DeployTask();
        deployTask.setId(UUID.randomUUID());
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
        deploy(deployTask);
    }

    private void deploy(DeployTask deployTask) {
        MDC.put(TASK_ID, deployTask.getId().toString());
        DeployResult deployResult;
        DeployServiceEntity storedEntity = null;
        Deployer deployer =
                deployerKindManager.getDeployment(deployTask.getOcl().getDeployment().getKind());
        try {
            storedEntity = storeNewDeployServiceEntity(deployTask);
            policyValidator.validateDeploymentWithPolicies(deployTask);
            deployResult = deployer.deploy(deployTask);
        } catch (RuntimeException e) {
            log.info("Deploy service with id:{} failed.", deployTask.getId(), e);
            deployResult = new DeployResult();
            deployResult.setId(deployTask.getId());
            deployResult.setState(DeployerTaskStatus.DEPLOY_FAILED);
            deployResult.setMessage(e.getMessage());
        }
        try {
            DeployServiceEntity updatedDeployServiceEntity =
                    deployResultManager.updateDeployServiceEntityWithDeployResult(deployResult,
                            storedEntity);
            if (ServiceDeploymentState.DEPLOY_FAILED
                    == updatedDeployServiceEntity.getServiceDeploymentState()) {
                rollbackOnDeploymentFailure(deployTask, updatedDeployServiceEntity);
            }
        } catch (RuntimeException e) {
            log.error("Deploy service with id:{} update database entity failed.",
                    deployTask.getId(), e);
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
            deployTask.setDestroyScenario(DestroyScenario.ROLLBACK);
            destroy(deployTask, deployServiceEntity);
        }
    }

    /**
     * Async method to destroy service.
     *
     * @param destroyTask         destroyTask.
     * @param deployServiceEntity deployServiceEntity
     */
    public void destroyService(DeployTask destroyTask, DeployServiceEntity deployServiceEntity) {
        destroyTask.setDestroyScenario(DestroyScenario.DESTROY);
        destroy(destroyTask, deployServiceEntity);
    }

    private void destroy(DeployTask destroyTask, DeployServiceEntity deployServiceEntity) {
        MDC.put(TASK_ID, destroyTask.getId().toString());
        DeployResult destroyResult;
        Deployer deployer =
                deployerKindManager.getDeployment(destroyTask.getOcl().getDeployment().getKind());
        try {
            if (DestroyScenario.ROLLBACK != destroyTask.getDestroyScenario()) {
                deployServiceEntity.setServiceDeploymentState(ServiceDeploymentState.DESTROYING);
            }
            deployServiceStorage.storeAndFlush(deployServiceEntity);
            destroyResult = deployer.destroy(destroyTask);
        } catch (RuntimeException e) {
            log.info("Destroy service with id:{} failed.", destroyTask.getId(), e);
            destroyResult = new DeployResult();
            destroyResult.setId(destroyTask.getId());
            destroyResult.setState(getDestroyTaskState(destroyTask.getDestroyScenario()));
            destroyResult.setMessage(e.getMessage());
        }
        try {
            DeployServiceEntity updatedDeployServiceEntity =
                    deployResultManager.updateDeployServiceEntityWithDeployResult(destroyResult,
                            deployServiceEntity);
            if (ServiceDeploymentState.DESTROY_SUCCESS
                    == updatedDeployServiceEntity.getServiceDeploymentState()) {
                deployer.deleteTaskWorkspace(destroyTask.getId());
            }
        } catch (RuntimeException e) {
            log.error("Destroy service with id:{} update database entity failed.",
                    destroyTask.getId(), e);
        }
    }

    private DeployerTaskStatus getDestroyTaskState(DestroyScenario destroyScenario) {
        return switch (destroyScenario) {
            case DESTROY -> DeployerTaskStatus.DESTROY_FAILED;
            case ROLLBACK -> DeployerTaskStatus.ROLLBACK_FAILED;
            case PURGE -> DeployerTaskStatus.PURGE_FAILED;
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
        try {
            if (Objects.nonNull(deployServiceEntity.getDeployResourceList())
                    && !deployServiceEntity.getDeployResourceList().isEmpty()) {
                log.info("destroying created resources for service with ID: {}",
                        destroyTask.getId());
                destroyTask.setDestroyScenario(DestroyScenario.PURGE);
                destroy(destroyTask, deployServiceEntity);
            }
            deployServiceStorage.deleteDeployService(deployServiceEntity);
            log.info("Database entry with ID {} purged.", deployServiceEntity.getId());
        } catch (RuntimeException e) {
            log.error("Error purging created resources for service with ID: {}. Ignoring.",
                    destroyTask.getId(), e);
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
        deployTask.setDestroyScenario(DestroyScenario.DESTROY);
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
                ServiceDeploymentState.DESTROYING)) {
            throw new InvalidServiceStateException(
                    String.format("Service with id %s is %s.", deployServiceEntity.getId(), state));
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
                    String.format("Service %s is not in the state allowed for purging.",
                            deployServiceEntity.getId()));
        }
        return deployServiceEntityToDeployTaskConverter.getDeployTaskByStoredService(
                deployServiceEntity);
    }
}
