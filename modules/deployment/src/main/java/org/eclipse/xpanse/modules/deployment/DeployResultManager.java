/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import static org.eclipse.xpanse.modules.deployment.utils.DeploymentScriptsHelper.TF_STATE_FILE_NAME;

import jakarta.annotation.Resource;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.resource.ServiceResourceEntity;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentStorage;
import org.eclipse.xpanse.modules.database.serviceconfiguration.ServiceConfigurationEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderStorage;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.deployment.recreate.consts.RecreateConstants;
import org.eclipse.xpanse.modules.deployment.serviceporting.consts.ServicePortingConstants;
import org.eclipse.xpanse.modules.logging.CustomRequestIdGenerator;
import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.response.ErrorType;
import org.eclipse.xpanse.modules.models.service.deployment.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deployment.DeployResource;
import org.eclipse.xpanse.modules.models.service.deployment.DeployResult;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.models.service.statemanagement.enums.ServiceState;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.deployment.Deployer;
import org.eclipse.xpanse.modules.workflow.utils.WorkflowUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;

/** Bean to handle deployment result. */
@Component
@Slf4j
public class DeployResultManager {

    @Resource private ServiceDeploymentStorage serviceDeploymentStorage;
    @Resource private ServiceOrderStorage serviceOrderStorage;
    @Resource private ServiceTemplateStorage serviceTemplateStorage;
    @Resource private ResourceHandlerManager resourceHandlerManager;
    @Resource private WorkflowUtils workflowUtils;
    @Resource private SensitiveDataHandler sensitiveDataHandler;
    @Resource private DeployServiceEntityConverter deployServiceEntityConverter;
    @Resource private ServiceOrderManager serviceOrderManager;
    @Resource private DeployerKindManager deployerKindManager;

    /**
     * Get failed deploy result.
     *
     * @param task task
     * @param ex exception
     * @return deploy result.
     */
    public DeployResult getFailedDeployResult(DeployTask task, Exception ex) {
        String errorMsg =
                String.format(
                        "Order task %s to %s the service %s failed. %s",
                        task.getOrderId(),
                        task.getTaskType().toValue(),
                        task.getServiceId(),
                        ex.getMessage());
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
        if (Objects.isNull(deployResult)
                || Objects.isNull(deployResult.getOrderId())
                || Objects.isNull(deployResult.getIsTaskSuccessful())) {
            log.warn("Could not update service data with unuseful deploy result {}.", deployResult);
            return;
        }
        UUID orderId = deployResult.getOrderId();
        ServiceOrderEntity storedServiceOrder = serviceOrderStorage.getEntityById(orderId);
        ServiceDeploymentEntity storedServiceDeployment =
                storedServiceOrder.getServiceDeploymentEntity();
        ServiceOrderType taskType = storedServiceOrder.getTaskType();
        ServiceDeploymentEntity updatedServiceDeployment =
                updateServiceDeploymentWithDeployResult(
                        deployResult, storedServiceDeployment, taskType);
        boolean isTaskSuccessful = deployResult.getIsTaskSuccessful();
        // When the task failed and task type is deploy or retry, just update the task status and
        // error message. If the tfState is not null, start a new rollback order task and wait
        // the order callback.
        if (isFailedDeployTask(isTaskSuccessful, taskType)) {
            serviceOrderManager.completeOrderProgressWithDeployResult(
                    storedServiceOrder.getOrderId(), deployResult);
            if (Objects.nonNull(deployResult.getTfStateContent())) {
                DeployTask rollbackTask =
                        deployServiceEntityConverter.getDeployTaskByStoredService(
                                ServiceOrderType.ROLLBACK, updatedServiceDeployment);
                rollbackTask.setParentOrderId(orderId);
                rollbackTask.setOriginalServiceId(storedServiceOrder.getOriginalServiceId());
                rollbackTask.setWorkflowId(storedServiceOrder.getWorkflowId());
                rollbackOnDeploymentFailure(rollbackTask, updatedServiceDeployment);
                return;
            }
        }
        updateServiceOrderEntityWithDeployResult(deployResult, storedServiceOrder);
    }

    /** Perform rollback when deployment fails and destroy the created resources. */
    public void rollbackOnDeploymentFailure(
            DeployTask rollbackTask, ServiceDeploymentEntity serviceDeploymentEntity) {
        DeployResult rollbackResult;
        RuntimeException exception = null;
        log.info("Performing rollback of already provisioned resources.");
        rollbackTask.setOrderId(CustomRequestIdGenerator.generateOrderId());
        rollbackTask.setTaskType(ServiceOrderType.ROLLBACK);
        ServiceOrderEntity serviceOrderEntity =
                serviceOrderManager.storeNewServiceOrderEntity(
                        rollbackTask, serviceDeploymentEntity);
        Deployer deployer =
                deployerKindManager.getDeployment(
                        rollbackTask.getOcl().getDeployment().getDeployerTool().getKind());
        try {
            if (CollectionUtils.isEmpty(serviceDeploymentEntity.getDeployResources())) {
                log.info("No resources need to destroy, the rollback task success.");
                rollbackResult = new DeployResult();
                rollbackResult.setOrderId(rollbackTask.getOrderId());
                rollbackResult.setIsTaskSuccessful(true);
            } else {
                log.info(
                        "Rollback to destroy created resources of the service {}",
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

    private ServiceDeploymentEntity updateServiceDeploymentWithDeployResult(
            DeployResult deployResult,
            ServiceDeploymentEntity serviceDeployment,
            ServiceOrderType taskType) {
        log.info(
                "Updating service deployment with id:{} by deploy result {}.",
                serviceDeployment.getId(),
                deployResult);
        ServiceDeploymentEntity serviceDeploymentToUpdate = new ServiceDeploymentEntity();
        BeanUtils.copyProperties(serviceDeployment, serviceDeploymentToUpdate);
        handleDeploymentResult(deployResult, serviceDeploymentToUpdate);
        boolean isTaskSuccessful = deployResult.getIsTaskSuccessful();
        ServiceDeploymentState deploymentState =
                getServiceDeploymentState(taskType, isTaskSuccessful);
        updateServiceState(deploymentState, serviceDeploymentToUpdate);
        if (Objects.nonNull(deploymentState)) {
            if (deploymentState == ServiceDeploymentState.DEPLOY_FAILED) {
                // when the task failed and task type is deploy or retry, and tfState is null,
                // set the deployment state to DEPLOY_FAILED.
                if (Objects.isNull(deployResult.getTfStateContent())) {
                    serviceDeploymentToUpdate.setServiceDeploymentState(deploymentState);
                }
            } else {
                serviceDeploymentToUpdate.setServiceDeploymentState(deploymentState);
            }
        }
        if (StringUtils.isNotBlank(deployResult.getMessage())) {
            serviceDeploymentToUpdate.setResultMessage(deployResult.getMessage());
        } else {
            // When rollback successfully, the result message should be the previous error message.
            if (isTaskSuccessful && taskType != ServiceOrderType.ROLLBACK) {
                serviceDeploymentToUpdate.setResultMessage(null);
            }
        }
        if (deploymentState == ServiceDeploymentState.MODIFICATION_SUCCESSFUL) {
            DeployRequest modifyRequest = serviceDeploymentToUpdate.getDeployRequest();
            serviceDeploymentToUpdate.setFlavor(modifyRequest.getFlavor());
            serviceDeploymentToUpdate.setCustomerServiceName(
                    modifyRequest.getCustomerServiceName());
        }
        ServiceTemplateEntity serviceTemplateEntity =
                serviceTemplateStorage.getServiceTemplateById(
                        serviceDeploymentToUpdate.getServiceTemplateId());
        if (Objects.nonNull(serviceTemplateEntity)
                && Objects.nonNull(
                        serviceTemplateEntity.getOcl().getServiceConfigurationManage())) {
            updateServiceConfiguration(deploymentState, serviceDeploymentToUpdate);
        }

        if (CollectionUtils.isEmpty(deployResult.getDeploymentGeneratedFiles())) {
            if (isTaskSuccessful) {
                serviceDeploymentToUpdate.setDeploymentGeneratedFiles(Collections.emptyMap());
            }
        } else {
            serviceDeploymentToUpdate.setDeploymentGeneratedFiles(
                    deployResult.getDeploymentGeneratedFiles());
        }

        if (CollectionUtils.isEmpty(deployResult.getOutputProperties())) {
            if (isTaskSuccessful) {
                serviceDeploymentToUpdate.setOutputProperties(Collections.emptyMap());
            }
        } else {
            serviceDeploymentToUpdate.setOutputProperties(deployResult.getOutputProperties());
        }

        if (CollectionUtils.isEmpty(deployResult.getResources())) {
            if (isTaskSuccessful) {
                serviceDeploymentToUpdate.setDeployResources(Collections.emptyList());
            }
        } else {
            serviceDeploymentToUpdate.setDeployResources(
                    getDeployResourceEntities(
                            deployResult.getResources(), serviceDeploymentToUpdate));
        }
        sensitiveDataHandler.maskSensitiveFields(serviceDeploymentToUpdate);
        return serviceDeploymentStorage.storeAndFlush(serviceDeploymentToUpdate);
    }

    private boolean isFailedDeployTask(boolean isTaskSuccessful, ServiceOrderType taskType) {
        return !isTaskSuccessful
                && (taskType == ServiceOrderType.DEPLOY || taskType == ServiceOrderType.RETRY);
    }

    private void handleDeploymentResult(
            DeployResult deployResult, ServiceDeploymentEntity serviceDeploymentEntity) {
        // If the tfState is null, try to use the stored tfState
        if (Objects.isNull(deployResult.getTfStateContent())) {
            if (Objects.nonNull(serviceDeploymentEntity.getDeploymentGeneratedFiles())) {
                String storedTfStateContent =
                        serviceDeploymentEntity
                                .getDeploymentGeneratedFiles()
                                .get(TF_STATE_FILE_NAME);
                if (StringUtils.isNotBlank(storedTfStateContent)) {
                    deployResult.setTfStateContent(storedTfStateContent);
                }
            }
        }

        if (StringUtils.isNotBlank(deployResult.getTfStateContent())) {
            deployResult
                    .getDeploymentGeneratedFiles()
                    .put(TF_STATE_FILE_NAME, deployResult.getTfStateContent());
            ServiceTemplateEntity serviceTemplateEntity =
                    serviceTemplateStorage.getServiceTemplateById(
                            serviceDeploymentEntity.getServiceTemplateId());
            DeployerKind deployerKind =
                    serviceTemplateEntity.getOcl().getDeployment().getDeployerTool().getKind();
            try {
                resourceHandlerManager
                        .getResourceHandler(serviceDeploymentEntity.getCsp(), deployerKind)
                        .handler(deployResult);
            } catch (RuntimeException e) {
                log.error("Exception occurred in handling deployment result.", e);
                deployResult.setIsTaskSuccessful(false);
                deployResult.setMessage(e.getMessage());
            }
        }
    }

    private void updateServiceConfiguration(
            ServiceDeploymentState state, ServiceDeploymentEntity serviceDeploymentEntity) {
        if (state == ServiceDeploymentState.DEPLOY_SUCCESS) {
            ServiceConfigurationEntity serviceConfigurationEntity =
                    deployServiceEntityConverter.getInitialServiceConfiguration(
                            serviceDeploymentEntity);
            serviceDeploymentEntity.setServiceConfiguration(serviceConfigurationEntity);
        }
        if (state == ServiceDeploymentState.DESTROY_SUCCESS) {
            serviceDeploymentEntity.setServiceConfiguration(null);
        }
    }

    private void updateServiceState(
            ServiceDeploymentState state, ServiceDeploymentEntity serviceDeploymentEntity) {
        if (state == ServiceDeploymentState.DEPLOY_SUCCESS
                || state == ServiceDeploymentState.MODIFICATION_SUCCESSFUL) {
            serviceDeploymentEntity.setServiceState(ServiceState.RUNNING);
            serviceDeploymentEntity.setLastStartedAt(OffsetDateTime.now());
        }
        if (state == ServiceDeploymentState.DEPLOY_FAILED
                || state == ServiceDeploymentState.DESTROY_SUCCESS) {
            serviceDeploymentEntity.setServiceState(ServiceState.NOT_RUNNING);
            serviceDeploymentEntity.setLastStoppedAt(OffsetDateTime.now());
        }
        // case other cases, do not change the state of service.
    }

    private ServiceDeploymentState getServiceDeploymentState(
            ServiceOrderType taskType, boolean isTaskSuccessful) {
        return switch (taskType) {
            case DEPLOY, RETRY ->
                    isTaskSuccessful
                            ? ServiceDeploymentState.DEPLOY_SUCCESS
                            : ServiceDeploymentState.DEPLOY_FAILED;
            case DESTROY ->
                    isTaskSuccessful
                            ? ServiceDeploymentState.DESTROY_SUCCESS
                            : ServiceDeploymentState.DESTROY_FAILED;
            case MODIFY ->
                    isTaskSuccessful
                            ? ServiceDeploymentState.MODIFICATION_SUCCESSFUL
                            : ServiceDeploymentState.MODIFICATION_FAILED;
            case ROLLBACK ->
                    isTaskSuccessful
                            ? ServiceDeploymentState.DEPLOY_FAILED
                            : ServiceDeploymentState.ROLLBACK_FAILED;
            case PURGE ->
                    isTaskSuccessful
                            ? ServiceDeploymentState.DESTROY_SUCCESS
                            : ServiceDeploymentState.MANUAL_CLEANUP_REQUIRED;
            default -> null;
        };
    }

    /** Convert service resources to deploy resource entities. */
    private List<ServiceResourceEntity> getDeployResourceEntities(
            List<DeployResource> deployResources, ServiceDeploymentEntity serviceDeploymentEntity) {
        List<ServiceResourceEntity> deployResourceEntities = new ArrayList<>();
        if (CollectionUtils.isEmpty(deployResources)) {
            return deployResourceEntities;
        }
        for (DeployResource resource : deployResources) {
            ServiceResourceEntity deployResource = new ServiceResourceEntity();
            BeanUtils.copyProperties(resource, deployResource);
            deployResource.setServiceDeploymentEntity(serviceDeploymentEntity);
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
    private void updateServiceOrderEntityWithDeployResult(
            DeployResult deployResult, ServiceOrderEntity storedOrderEntity) {
        log.info(
                "Updating service order with id:{} by deploy result:{}.",
                storedOrderEntity.getOrderId(),
                deployResult);
        // When the related parent order id is not null, complete the parent service order.
        if (Objects.nonNull(storedOrderEntity.getParentOrderId())) {
            completeParentServiceOrder(storedOrderEntity.getParentOrderId(), deployResult);
        }
        // When the related workflow id is not null, process the related workflow task.
        if (Objects.nonNull(storedOrderEntity.getWorkflowId())) {
            processRelatedWorkflowTask(storedOrderEntity, deployResult);
        }
        ServiceOrderEntity entityToUpdate = new ServiceOrderEntity();
        BeanUtils.copyProperties(storedOrderEntity, entityToUpdate);
        entityToUpdate.setCompletedTime(OffsetDateTime.now());
        serviceOrderManager.completeOrderProgressWithDeployResult(
                storedOrderEntity.getOrderId(), deployResult);
    }

    private void completeParentServiceOrder(UUID parentOrderId, DeployResult deployResult) {
        ServiceOrderEntity parentOrder = serviceOrderStorage.getEntityById(parentOrderId);
        // When the parent order is not port or recreate task, complete it.
        if (parentOrder.getTaskType() != ServiceOrderType.PORT
                && parentOrder.getTaskType() != ServiceOrderType.RECREATE) {
            ServiceOrderEntity entityToUpdate = new ServiceOrderEntity();
            BeanUtils.copyProperties(parentOrder, entityToUpdate);
            serviceOrderManager.completeOrderProgressWithDeployResult(parentOrderId, deployResult);
        }
        // process the related workflow task of the parent order.
        if (Objects.nonNull(parentOrder.getWorkflowId())) {
            processRelatedWorkflowTask(parentOrder, deployResult);
        }
    }

    private void processRelatedWorkflowTask(
            ServiceOrderEntity serviceOrder, DeployResult deployResult) {

        String processInstanceId = serviceOrder.getWorkflowId();
        if (Objects.nonNull(serviceOrder.getParentOrderId())
                && StringUtils.isNotBlank(processInstanceId)) {
            try {
                ServiceOrderEntity parentOrder =
                        serviceOrderStorage.getEntityById(serviceOrder.getParentOrderId());
                serviceOrderManager.updateOrderWithDeployResult(parentOrder, deployResult);
                if (parentOrder.getTaskType() == ServiceOrderType.PORT) {
                    if (serviceOrder.getTaskType() == ServiceOrderType.DEPLOY
                            || serviceOrder.getTaskType() == ServiceOrderType.RETRY) {
                        workflowUtils.completeReceiveTask(
                                processInstanceId,
                                ServicePortingConstants
                                        .SERVICE_PORTING_DEPLOY_RECEIVE_TASK_ACTIVITY_ID);
                    }
                    if (serviceOrder.getTaskType() == ServiceOrderType.DESTROY) {
                        workflowUtils.completeReceiveTask(
                                processInstanceId,
                                ServicePortingConstants
                                        .SERVICE_PORTING_DESTROY_RECEIVE_TASK_ACTIVITY_ID);
                    }
                }
                if (parentOrder.getTaskType() == ServiceOrderType.RECREATE) {
                    if (serviceOrder.getTaskType() == ServiceOrderType.DEPLOY
                            || serviceOrder.getTaskType() == ServiceOrderType.RETRY) {
                        workflowUtils.completeReceiveTask(
                                processInstanceId,
                                RecreateConstants.RECREATE_DEPLOY_RECEIVE_TASK_ACTIVITY_ID);
                    }
                    if (serviceOrder.getTaskType() == ServiceOrderType.DESTROY) {
                        workflowUtils.completeReceiveTask(
                                processInstanceId,
                                RecreateConstants.RECREATE_DESTROY_RECEIVE_TASK_ACTIVITY_ID);
                    }
                }
            } catch (Exception e) {
                log.error(
                        "Failed to process the related workflow task of service order: {}",
                        serviceOrder.getOrderId(),
                        e);
            }
        }
    }

    /** update service state and service order state by exception. */
    public void saveDeploymentResultWhenErrorReceived(
            ServiceDeploymentEntity serviceEntity,
            ServiceOrderEntity serviceOrder,
            ErrorType errorType,
            HttpClientErrorException e) {
        serviceEntity.setServiceDeploymentState(ServiceDeploymentState.MANUAL_CLEANUP_REQUIRED);
        serviceOrder.setTaskStatus(TaskStatus.FAILED);
        serviceOrder.setErrorResponse(
                ErrorResponse.errorResponse(errorType, List.of(e.getMessage())));
        serviceDeploymentStorage.storeAndFlush(serviceEntity);
        serviceOrderStorage.storeAndFlush(serviceOrder);
    }
}
