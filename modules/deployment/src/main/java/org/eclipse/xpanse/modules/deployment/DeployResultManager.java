/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import jakarta.annotation.Resource;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.resource.DeployResourceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceStorage;
import org.eclipse.xpanse.modules.database.serviceconfiguration.ServiceConfigurationEntity;
import org.eclipse.xpanse.modules.database.servicemigration.ServiceMigrationEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderStorage;
import org.eclipse.xpanse.modules.database.servicerecreate.ServiceRecreateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.deployment.migration.MigrationService;
import org.eclipse.xpanse.modules.deployment.migration.consts.MigrateConstants;
import org.eclipse.xpanse.modules.deployment.recreate.RecreateService;
import org.eclipse.xpanse.modules.deployment.recreate.consts.RecreateConstants;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.models.service.statemanagement.enums.ServiceState;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.deployment.Deployer;
import org.eclipse.xpanse.modules.workflow.utils.WorkflowUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Bean to handle deployment result.
 */
@Component
@Slf4j
public class DeployResultManager {

    @Resource
    private DeployServiceStorage deployServiceStorage;
    @Resource
    private ServiceOrderStorage serviceOrderStorage;
    @Resource
    private ServiceTemplateStorage serviceTemplateStorage;
    @Resource
    private ResourceHandlerManager resourceHandlerManager;
    @Resource
    private MigrationService migrationService;
    @Resource
    private RecreateService recreateService;
    @Resource
    private WorkflowUtils workflowUtils;
    @Resource
    private SensitiveDataHandler sensitiveDataHandler;
    @Resource
    private DeployServiceEntityConverter deployServiceEntityConverter;
    @Resource
    private ServiceOrderManager serviceOrderManager;
    @Resource
    private DeployerKindManager deployerKindManager;


    /**
     * Get failed deploy result.
     *
     * @param task task
     * @param ex   exception
     * @return deploy result.
     */
    public DeployResult getFailedDeployResult(DeployTask task, Exception ex) {
        String errorMsg =
                String.format("Order task %s to %s the service %s failed. %s", task.getOrderId(),
                        task.getTaskType().toValue(), task.getServiceId(), ex.getMessage());
        DeployResult deployResult = new DeployResult();
        deployResult.setOrderId(task.getOrderId());
        deployResult.setIsTaskSuccessful(false);
        deployResult.setMessage(errorMsg);
        return deployResult;
    }

    /**
     * Update service with deploy result.
     *
     * @param deployResult DeployResult.
     */
    public void updateServiceWithDeployResult(DeployResult deployResult) {
        if (Objects.isNull(deployResult) || Objects.isNull(deployResult.getOrderId())
                || Objects.isNull(deployResult.getIsTaskSuccessful())) {
            log.warn("Could not update service data with unuseful deploy result {}.", deployResult);
            return;
        }
        UUID orderId = deployResult.getOrderId();
        ServiceOrderEntity storedOrderEntity = serviceOrderStorage.getEntityById(orderId);
        ServiceOrderType taskType = storedOrderEntity.getTaskType();
        // update deployServiceEntity
        DeployServiceEntity updatedServiceEntity =
                updateDeployServiceEntityWithDeployResult(deployResult, taskType);
        // When the task failed and task type is deploy or retry, start a new order to rollback.
        if (isFailedDeployTask(deployResult.getIsTaskSuccessful(), taskType)) {
            DeployTask rollbackTask = deployServiceEntityConverter.getDeployTaskByStoredService(
                            ServiceOrderType.ROLLBACK, updatedServiceEntity);
            rollbackTask.setParentOrderId(orderId);
            rollbackOnDeploymentFailure(rollbackTask, updatedServiceEntity);
        }

        UUID serviceId = updatedServiceEntity.getId();
        // complete migration and recreate receive task.
        if (taskType == ServiceOrderType.DEPLOY || taskType == ServiceOrderType.ROLLBACK) {
            if (isFailedDeployTask(deployResult.getIsTaskSuccessful(), taskType)) {
                return;
            }
            ServiceMigrationEntity serviceMigrationEntity =
                    migrationService.getServiceMigrationEntityByNewServiceId(serviceId);
            if (Objects.nonNull(serviceMigrationEntity)) {
                workflowUtils.completeReceiveTask(
                        serviceMigrationEntity.getMigrationId().toString(),
                        MigrateConstants.MIGRATION_DEPLOY_RECEIVE_TASK_ACTIVITY_ID);
            }
            ServiceRecreateEntity serviceRecreateEntity =
                    recreateService.getServiceRecreateEntityByNewServiceId(serviceId);
            if (Objects.nonNull(serviceRecreateEntity)) {
                workflowUtils.completeReceiveTask(serviceRecreateEntity.getRecreateId().toString(),
                        RecreateConstants.RECREATE_DEPLOY_RECEIVE_TASK_ACTIVITY_ID);
            }
        }

        if (taskType == ServiceOrderType.DESTROY) {
            ServiceMigrationEntity serviceMigrationEntity =
                    migrationService.getServiceMigrationEntityByOldServiceId(serviceId);
            if (Objects.nonNull(serviceMigrationEntity)) {
                workflowUtils.completeReceiveTask(
                        serviceMigrationEntity.getMigrationId().toString(),
                        MigrateConstants.MIGRATION_DESTROY_RECEIVE_TASK_ACTIVITY_ID);
            }
            ServiceRecreateEntity serviceRecreateEntity =
                    recreateService.getServiceRecreateEntityByOldServiceId(serviceId);
            if (Objects.nonNull(serviceRecreateEntity)) {
                workflowUtils.completeReceiveTask(serviceRecreateEntity.getRecreateId().toString(),
                        RecreateConstants.RECREATE_DESTROY_RECEIVE_TASK_ACTIVITY_ID);
            }
        }
        // update storedOrderEntity
        updateServiceOrderEntityWithDeployResult(deployResult);
    }

    /**
     * Perform rollback when deployment fails and destroy the created resources.
     */
    public void rollbackOnDeploymentFailure(DeployTask rollbackTask,
                                            DeployServiceEntity deployServiceEntity) {
        DeployResult rollbackResult;
        RuntimeException exception = null;
        log.info("Performing rollback of already provisioned resources.");
        rollbackTask.setOrderId(UUID.randomUUID());
        rollbackTask.setTaskType(ServiceOrderType.ROLLBACK);
        ServiceOrderEntity serviceOrderEntity =
                serviceOrderManager.storeNewServiceOrderEntity(rollbackTask, deployServiceEntity);
        Deployer deployer = deployerKindManager.getDeployment(
                rollbackTask.getOcl().getDeployment().getDeployerTool().getKind());
        try {
            if (CollectionUtils.isEmpty(deployServiceEntity.getDeployResourceList())) {
                log.info("No resources need to destroy, the rollback task success.");
                rollbackResult = new DeployResult();
                rollbackResult.setOrderId(rollbackTask.getOrderId());
                rollbackResult.setIsTaskSuccessful(true);
            } else {
                log.info("Rollback to destroy created resources of the service {}",
                        rollbackTask.getServiceId());
                serviceOrderManager.startOrderProgress(serviceOrderEntity);
                rollbackResult = deployer.destroy(rollbackTask);
            }
        } catch (RuntimeException e) {
            exception = e;
            rollbackResult = getFailedDeployResult(rollbackTask, exception);
        }
        updateServiceWithDeployResult(rollbackResult);
        if (Objects.nonNull(exception)) {
            throw exception;
        }
    }


    private boolean isFailedDeployTask(boolean isTaskSuccessful, ServiceOrderType taskType) {
        return !isTaskSuccessful
                && (taskType == ServiceOrderType.DEPLOY || taskType == ServiceOrderType.RETRY);
    }

    private DeployServiceEntity updateDeployServiceEntityWithDeployResult(
            DeployResult deployResult, ServiceOrderType taskType) {
        DeployServiceEntity deployServiceEntity =
                serviceOrderStorage.getDeployServiceByOrderId(deployResult.getOrderId());
        log.info("Update deploy service entity {} with deploy result {}",
                deployServiceEntity.getId(), deployResult);
        if (StringUtils.isNotBlank(deployResult.getTfStateContent())) {
            ServiceTemplateEntity serviceTemplateEntity =
                    serviceTemplateStorage.getServiceTemplateById(
                            deployServiceEntity.getServiceTemplateId());
            DeployerKind deployerKind =
                    serviceTemplateEntity.getOcl().getDeployment().getDeployerTool().getKind();
            resourceHandlerManager.getResourceHandler(deployServiceEntity.getCsp(), deployerKind)
                    .handler(deployResult);
        }
        DeployServiceEntity deployServiceToUpdate = new DeployServiceEntity();
        BeanUtils.copyProperties(deployServiceEntity, deployServiceToUpdate);
        updateServiceEntityWithDeployResult(deployResult, taskType, deployServiceToUpdate);
        return deployServiceStorage.storeAndFlush(deployServiceToUpdate);
    }


    private void updateServiceEntityWithDeployResult(DeployResult deployResult,
                                                     ServiceOrderType taskType,
                                                     DeployServiceEntity deployServiceEntity) {
        boolean isTaskSuccessful = deployResult.getIsTaskSuccessful();
        ServiceDeploymentState deploymentState =
                getServiceDeploymentState(taskType, isTaskSuccessful);
        if (Objects.nonNull(deploymentState)) {
            deployServiceEntity.setServiceDeploymentState(deploymentState);
        }
        if (StringUtils.isNotBlank(deployResult.getMessage())) {
            deployServiceEntity.setResultMessage(deployResult.getMessage());
        } else {
            // When rollback successfully, the result message should be the previous error message.
            if (isTaskSuccessful && taskType != ServiceOrderType.ROLLBACK) {
                deployServiceEntity.setResultMessage(null);
            }
        }
        if (deploymentState == ServiceDeploymentState.MODIFICATION_SUCCESSFUL) {
            DeployRequest modifyRequest = deployServiceEntity.getDeployRequest();
            deployServiceEntity.setFlavor(modifyRequest.getFlavor());
            deployServiceEntity.setCustomerServiceName(modifyRequest.getCustomerServiceName());
        }

        updateServiceConfiguration(deploymentState, deployServiceEntity);
        updateServiceState(deploymentState, deployServiceEntity);

        if (CollectionUtils.isEmpty(deployResult.getPrivateProperties())) {
            if (isTaskSuccessful) {
                deployServiceEntity.setPrivateProperties(Collections.emptyMap());
            }
        } else {
            deployServiceEntity.setPrivateProperties(deployResult.getPrivateProperties());
        }

        if (CollectionUtils.isEmpty(deployResult.getProperties())) {
            if (isTaskSuccessful) {
                deployServiceEntity.setProperties(Collections.emptyMap());
            }
        } else {
            deployServiceEntity.setProperties(deployResult.getProperties());
        }

        if (CollectionUtils.isEmpty(deployResult.getResources())) {
            if (isTaskSuccessful) {
                deployServiceEntity.setDeployResourceList(Collections.emptyList());
            }
        } else {
            deployServiceEntity.setDeployResourceList(
                    getDeployResourceEntityList(deployResult.getResources(), deployServiceEntity));
        }
        sensitiveDataHandler.maskSensitiveFields(deployServiceEntity);
    }

    private void updateServiceConfiguration(ServiceDeploymentState state,
                                            DeployServiceEntity deployServiceEntity) {
        if (state == ServiceDeploymentState.DEPLOY_SUCCESS) {
            ServiceConfigurationEntity serviceConfigurationEntity =
                    deployServiceEntityConverter.getInitialServiceConfiguration(
                            deployServiceEntity);
            deployServiceEntity.setServiceConfigurationEntity(serviceConfigurationEntity);
        }
        if (state == ServiceDeploymentState.DESTROY_SUCCESS) {
            deployServiceEntity.setServiceConfigurationEntity(null);
        }
    }

    private void updateServiceState(ServiceDeploymentState state,
                                    DeployServiceEntity deployServiceEntity) {
        if (state == ServiceDeploymentState.DEPLOY_SUCCESS
                || state == ServiceDeploymentState.MODIFICATION_SUCCESSFUL) {
            deployServiceEntity.setServiceState(ServiceState.RUNNING);
            deployServiceEntity.setLastStartedAt(OffsetDateTime.now());
        }
        if (state == ServiceDeploymentState.DEPLOY_FAILED
                || state == ServiceDeploymentState.DESTROY_SUCCESS) {
            deployServiceEntity.setServiceState(ServiceState.NOT_RUNNING);
        }
        // case other cases, do not change the state of service.
    }

    private ServiceDeploymentState getServiceDeploymentState(ServiceOrderType taskType,
                                                             boolean isTaskSuccessful) {
        return switch (taskType) {
            case DEPLOY, RETRY -> isTaskSuccessful ? ServiceDeploymentState.DEPLOY_SUCCESS : null;
            case DESTROY -> isTaskSuccessful ? ServiceDeploymentState.DESTROY_SUCCESS
                    : ServiceDeploymentState.DESTROY_FAILED;
            case MODIFY -> isTaskSuccessful ? ServiceDeploymentState.MODIFICATION_SUCCESSFUL
                    : ServiceDeploymentState.MODIFICATION_FAILED;
            case ROLLBACK -> isTaskSuccessful ? ServiceDeploymentState.DEPLOY_FAILED
                    : ServiceDeploymentState.ROLLBACK_FAILED;
            case PURGE -> isTaskSuccessful ? ServiceDeploymentState.DESTROY_SUCCESS
                    : ServiceDeploymentState.MANUAL_CLEANUP_REQUIRED;
            default -> null;
        };
    }


    /**
     * Convert deploy resources to deploy resource entities.
     */
    private List<DeployResourceEntity> getDeployResourceEntityList(
            List<DeployResource> deployResources, DeployServiceEntity deployServiceEntity) {
        List<DeployResourceEntity> deployResourceEntities = new ArrayList<>();
        if (CollectionUtils.isEmpty(deployResources)) {
            return deployResourceEntities;
        }
        for (DeployResource resource : deployResources) {
            DeployResourceEntity deployResource = new DeployResourceEntity();
            BeanUtils.copyProperties(resource, deployResource);
            deployResource.setDeployService(deployServiceEntity);
            deployResourceEntities.add(deployResource);
        }
        return deployResourceEntities;
    }

    /**
     * Update service order entity in the database by the deployment result. We must ensure the
     * order is not set to a final state until all related process is completed.
     *
     * @param deployResult Deployment Result.
     */
    private void updateServiceOrderEntityWithDeployResult(DeployResult deployResult) {
        ServiceOrderEntity storedOrderEntity =
                serviceOrderStorage.getEntityById(deployResult.getOrderId());
        ServiceOrderEntity entityToUpdate = new ServiceOrderEntity();
        BeanUtils.copyProperties(storedOrderEntity, entityToUpdate);
        boolean isTaskSuccessful = deployResult.getIsTaskSuccessful();
        TaskStatus taskStatus = isTaskSuccessful ? TaskStatus.SUCCESSFUL : TaskStatus.FAILED;
        ServiceOrderType taskType = storedOrderEntity.getTaskType();
        // When the task failed and task type is deploy or retry, just update the task status and
        // wait the new rollback task complete to set the completed time of this task.
        if (isFailedDeployTask(isTaskSuccessful, taskType)) {
            entityToUpdate.setTaskStatus(taskStatus);
            serviceOrderStorage.storeAndFlush(entityToUpdate);
            return;
        }
        // When the task type is rollback, complete this order and then update the parent order.
        if (taskType == ServiceOrderType.ROLLBACK) {
            entityToUpdate.setTaskStatus(taskStatus);
            entityToUpdate.setCompletedTime(OffsetDateTime.now());
            serviceOrderStorage.storeAndFlush(entityToUpdate);
            completeParentServiceOrder(storedOrderEntity.getParentOrderId());
            return;
        }
        // Update the task status and completed time.
        entityToUpdate.setTaskStatus(taskStatus);
        entityToUpdate.setCompletedTime(OffsetDateTime.now());
        serviceOrderStorage.storeAndFlush(entityToUpdate);
    }

    private void completeParentServiceOrder(UUID parentOrderId) {
        ServiceOrderEntity parentOrder = serviceOrderStorage.getEntityById(parentOrderId);
        ServiceOrderEntity entityToUpdate = new ServiceOrderEntity();
        BeanUtils.copyProperties(parentOrder, entityToUpdate);
        entityToUpdate.setCompletedTime(OffsetDateTime.now());
        serviceOrderStorage.storeAndFlush(entityToUpdate);
    }
}
