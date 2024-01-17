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
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceStorage;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResult;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.deploy.enums.TerraformExecState;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.InvalidServiceStateException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.service.utils.ServiceVariablesJsonSchemaValidator;
import org.eclipse.xpanse.modules.models.servicetemplate.DeployVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotRegistered;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.deployment.Deployment;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Main class which orchestrates the OCL request processing. Calls the available plugins to deploy
 * managed service in the respective infrastructure as defined in the OCL.
 */
@Slf4j
@Component
public class DeployService {

    public static final String STATE_FILE_NAME = "terraform.tfstate";
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
    private ResourceHandlerManager resourceHandlerManager;

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
        // Find the registered service template and fill Ocl.
        ServiceTemplateEntity serviceTemplate = new ServiceTemplateEntity();
        serviceTemplate.setName(StringUtils.lowerCase(deployRequest.getServiceName()));
        serviceTemplate.setVersion(StringUtils.lowerCase(deployRequest.getVersion()));
        serviceTemplate.setCsp(deployRequest.getCsp());
        serviceTemplate.setCategory(deployRequest.getCategory());
        serviceTemplate.setServiceHostingType(deployRequest.getServiceHostingType());
        serviceTemplate = serviceTemplateStorage.findServiceTemplate(serviceTemplate);
        if (Objects.isNull(serviceTemplate) || Objects.isNull(serviceTemplate.getOcl())) {
            throw new ServiceTemplateNotRegistered("Service template not found.");
        }

        // Check context validation
        if (Objects.nonNull(serviceTemplate.getOcl().getDeployment()) && Objects.nonNull(
                deployRequest.getServiceRequestProperties())) {
            List<DeployVariable> deployVariables = serviceTemplate.getOcl().getDeployment()
                    .getVariables();

            serviceVariablesJsonSchemaValidator.validateDeployVariables(deployVariables,
                    deployRequest.getServiceRequestProperties(),
                    serviceTemplate.getJsonObjectSchema());
        }
        sensitiveDataHandler.encodeDeployVariable(serviceTemplate,
                deployRequest.getServiceRequestProperties());

        if (StringUtils.isEmpty(deployRequest.getCustomerServiceName())) {
            deployRequest.setCustomerServiceName(generateCustomerServiceName(deployRequest));
        }
        // Create new deploy task by deploy request.
        DeployTask deployTask = new DeployTask();
        deployTask.setId(UUID.randomUUID());
        deployRequest.setOcl(serviceTemplate.getOcl());
        deployTask.setDeployRequest(deployRequest);
        deployTask.setNamespace(serviceTemplate.getNamespace());
        deployTask.setOcl(serviceTemplate.getOcl());
        deployTask.setServiceTemplateId(serviceTemplate.getId());
        // Fill the handler
        resourceHandlerManager.fillHandler(deployTask);
        return deployTask;
    }



    private String generateCustomerServiceName(DeployRequest deployRequest) {
        if (deployRequest.getServiceName().length() > 5) {
            return deployRequest.getServiceName().substring(0, 4) + "-"
                    + RandomStringUtils.randomAlphanumeric(5);
        } else {
            return deployRequest.getServiceName() + "-"
                    + RandomStringUtils.randomAlphanumeric(5);
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
     * @param deployment deployment
     * @param deployTask deployTask
     */
    public void deployService(Deployment deployment, DeployTask deployTask) {
        deploy(deployment, deployTask);
    }

    private void deploy(Deployment deployment, DeployTask deployTask) {
        MDC.put(TASK_ID, deployTask.getId().toString());
        DeployResult deployResult;
        DeployServiceEntity storedEntity = null;
        try {
            storedEntity = storeNewDeployServiceEntity(deployTask);
            policyValidator.validateDeploymentWithPolicies(deployment, deployTask);
            deployResult = deployment.deploy(deployTask);
        } catch (RuntimeException e) {
            log.info("Deploy service with id:{} failed.", deployTask.getId(), e);
            deployResult = new DeployResult();
            deployResult.setId(deployTask.getId());
            deployResult.setState(TerraformExecState.DEPLOY_FAILED);
            deployResult.setMessage(e.getMessage());
        }
        try {
            DeployServiceEntity updatedDeployServiceEntity =
                    deployResultManager.updateDeployServiceEntityWithDeployResult(
                            deployResult, storedEntity);
            if (ServiceDeploymentState.DEPLOY_FAILED
                    == updatedDeployServiceEntity.getServiceDeploymentState()) {
                rollbackOnDeploymentFailure(deployment, deployTask, updatedDeployServiceEntity);
            }
        } catch (RuntimeException e) {
            log.info("Deploy service with id:{} update database entity failed.",
                    deployTask.getId(), e);
        }
    }



    /**
     * Perform rollback when deployment fails and destroy the created resources.
     */
    public void rollbackOnDeploymentFailure(Deployment deployment,
                                             DeployTask deployTask,
                                             DeployServiceEntity deployServiceEntity) {
        log.info("Performing rollback of already provisioned resources.");
        if (Objects.nonNull(deployServiceEntity.getDeployResourceList())
                && !deployServiceEntity.getDeployResourceList().isEmpty()) {
            log.info("destroying created resources for service with ID: {}", deployTask.getId());
            destroy(deployment, deployTask, deployServiceEntity, true);
        }
    }

    /**
     * Async method to destroy service.
     *
     * @param deployment          deployment
     * @param deployTask          deployTask
     * @param deployServiceEntity deployServiceEntity
     */
    public void destroyService(Deployment deployment, DeployTask deployTask,
                                    DeployServiceEntity deployServiceEntity) {
        destroy(deployment, deployTask, deployServiceEntity, false);
    }

    private void destroy(Deployment deployment, DeployTask destroyTask,
                         DeployServiceEntity deployServiceEntity, boolean isCalledWhenRollback) {
        MDC.put(TASK_ID, destroyTask.getId().toString());
        DeployResult destroyResult;
        try {
            String stateFile = getStoredStateContent(deployServiceEntity);
            destroyResult = deployment.destroy(destroyTask, stateFile);
        } catch (RuntimeException e) {
            log.info("Destroy service with id:{} failed.", destroyTask.getId(), e);
            destroyResult = new DeployResult();
            destroyResult.setId(destroyTask.getId());
            destroyResult.setState(TerraformExecState.DESTROY_FAILED);
            destroyResult.setState(TerraformExecState.DESTROY_FAILED);
            destroyResult.setMessage(e.getMessage());
        }

        try {
            DeployServiceEntity updatedDeployServiceEntity =
                    deployResultManager.updateDeployServiceEntityWithDestroyResult(
                            destroyResult, deployServiceEntity, isCalledWhenRollback);
            if (ServiceDeploymentState.DESTROY_SUCCESS
                    == updatedDeployServiceEntity.getServiceDeploymentState()
                    || ServiceDeploymentState.DEPLOY_FAILED
                    == updatedDeployServiceEntity.getServiceDeploymentState()) {
                deployment.deleteTaskWorkspace(destroyTask.getId().toString());
            }
        } catch (RuntimeException e) {
            log.info("Destroy service with id:{} update database entity failed.",
                    destroyTask.getId(), e);
        }
    }

    private String getStoredStateContent(DeployServiceEntity deployServiceEntity) {
        if (Objects.isNull(deployServiceEntity)
                || CollectionUtils.isEmpty(deployServiceEntity.getPrivateProperties())
                || StringUtils.isEmpty(
                deployServiceEntity.getPrivateProperties().get(STATE_FILE_NAME))) {
            throw new ServiceNotDeployedException(
                    "Can't find valid state context in stored deployed service.");
        }
        return deployServiceEntity.getPrivateProperties().get(STATE_FILE_NAME);
    }

    /**
     * Async method to purge deployed service.
     *
     * @param deployment          deployment
     * @param deployTask          deployTask
     * @param deployServiceEntity deployServiceEntity
     */
    @Async("xpanseAsyncTaskExecutor")
    public void asyncPurgeService(Deployment deployment, DeployTask deployTask,
                                  DeployServiceEntity deployServiceEntity) {
        purgeService(deployment, deployTask, deployServiceEntity);
    }

    /**
     * purge the service based on the serviceDeploymentState.
     *
     * @param deployment          deployment
     * @param deployTask          deployTask
     * @param deployServiceEntity deployServiceEntity
     */
    private void purgeService(Deployment deployment, DeployTask deployTask,
                              DeployServiceEntity deployServiceEntity) {
        MDC.put(TASK_ID, deployTask.getId().toString());
        try {
            if (Objects.nonNull(deployServiceEntity.getDeployResourceList())
                    && !deployServiceEntity.getDeployResourceList().isEmpty()) {
                log.info("destroying created resources for service with ID: {}",
                        deployTask.getId());
                destroy(deployment, deployTask, deployServiceEntity, false);
            }
            deployServiceStorage.deleteDeployService(deployServiceEntity);
            log.info("Database entry with ID {} purged.", deployServiceEntity.getId());
        } catch (RuntimeException e) {
            log.error("Error purging created resources for service with ID: {}. Ignoring.",
                    deployTask.getId(), e);
        }
    }

    /**
     * Deployment service.
     *
     * @param newId         new service id.
     * @param userId        user id.
     * @param deployRequest deploy request.
     * @return new deployed service entity.
     */
    @Async("xpanseAsyncTaskExecutor")
    public CompletableFuture<DeployServiceEntity> deployServiceById(UUID newId,
                                                                String userId,
                                                                DeployRequest deployRequest) {
        MDC.put(TASK_ID, newId.toString());
        log.info("Migrate workflow start deploy new service with id: {}", newId);
        DeployTask deployTask = createNewDeployTask(deployRequest);
        // override task id and user id.
        deployTask.setId(newId);
        deployTask.getDeployRequest().setUserId(userId);
        Deployment deployment = deployerKindManager.getDeployment(
                deployTask.getOcl().getDeployment().getKind());
        storeNewDeployServiceEntity(deployTask);
        deploy(deployment, deployTask);
        DeployServiceEntity deployServiceEntity = deployServiceStorage.findDeployServiceById(newId);
        return CompletableFuture.completedFuture(deployServiceEntity);
    }

    /**
     * Destroy service by deployed service id.
     */
    @Async("xpanseAsyncTaskExecutor")
    public CompletableFuture<DeployServiceEntity> destroyServiceById(String id) {
        MDC.put(TASK_ID, id);
        DeployServiceEntity deployServiceEntity = deployServiceEntityHandler
                .getDeployServiceEntity(UUID.fromString(id));
        DeployTask deployTask = deployServiceEntityToDeployTaskConverter
                .getDeployTaskByStoredService(deployServiceEntity);
        Deployment deployment = deployerKindManager.getDeployment(
                deployTask.getOcl().getDeployment().getKind());
        destroy(deployment, deployTask, deployServiceEntity, false);
        return CompletableFuture.completedFuture(deployServiceEntity);
    }

    /**
     * Get destroy task by stored deploy service entity.
     *
     * @param deployServiceEntity deploy service entity.
     * @return deploy task.
     */
    public DeployTask getDestroyTask(DeployServiceEntity deployServiceEntity) {

        // Get state of service.
        ServiceDeploymentState state = deployServiceEntity.getServiceDeploymentState();
        if (state.equals(ServiceDeploymentState.DEPLOYING)
                || state.equals(ServiceDeploymentState.DESTROYING)) {
            throw new InvalidServiceStateException(String.format("Service with id %s is %s.",
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
                || state == ServiceDeploymentState.MANUAL_CLEANUP_REQUIRED)) {
            throw new InvalidServiceStateException(
                    String.format("Service %s is not in the state allowed for purging.",
                            deployServiceEntity.getId()));
        }
        return deployServiceEntityToDeployTaskConverter.getDeployTaskByStoredService(
                deployServiceEntity);
    }
}
