/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import static org.eclipse.xpanse.modules.deployment.utils.DeploymentScriptsHelper.TF_STATE_FILE_NAME;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.eclipse.xpanse.modules.deployment.recreate.consts.RecreateConstants;
import org.eclipse.xpanse.modules.deployment.serviceporting.consts.ServicePortingConstants;
import org.eclipse.xpanse.modules.models.common.exceptions.XpanseUnhandledException;
import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.response.ErrorType;
import org.eclipse.xpanse.modules.models.service.deployment.DeployResource;
import org.eclipse.xpanse.modules.models.service.deployment.DeployResult;
import org.eclipse.xpanse.modules.models.service.deployment.ModifyRequest;
import org.eclipse.xpanse.modules.models.service.enums.Handler;
import org.eclipse.xpanse.modules.models.service.enums.OrderStatus;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.models.service.statemanagement.enums.ServiceState;
import org.eclipse.xpanse.modules.models.servicetemplate.Deployment;
import org.eclipse.xpanse.modules.models.servicetemplate.InputVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.OutputVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.DeploymentVariableHelper;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.deployment.Deployer;
import org.eclipse.xpanse.modules.workflow.utils.WorkflowUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** Bean to handle deployment result. */
@Component
@Slf4j
public class DeployResultManager {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ServiceDeploymentStorage serviceDeploymentStorage;
    private final ServiceOrderStorage serviceOrderStorage;
    private final ResourceHandlerManager resourceHandlerManager;
    private final WorkflowUtils workflowUtils;
    private final SensitiveDataHandler sensitiveDataHandler;
    private final ServiceDeploymentEntityConverter serviceDeploymentEntityConverter;
    private final ServiceOrderManager serviceOrderManager;
    private final DeployerKindManager deployerKindManager;

    /** Constructor method. */
    @Autowired
    public DeployResultManager(
            ServiceDeploymentStorage serviceDeploymentStorage,
            ServiceOrderStorage serviceOrderStorage,
            ResourceHandlerManager resourceHandlerManager,
            WorkflowUtils workflowUtils,
            SensitiveDataHandler sensitiveDataHandler,
            ServiceDeploymentEntityConverter serviceDeploymentEntityConverter,
            ServiceOrderManager serviceOrderManager,
            DeployerKindManager deployerKindManager) {
        this.serviceDeploymentStorage = serviceDeploymentStorage;
        this.serviceOrderStorage = serviceOrderStorage;
        this.resourceHandlerManager = resourceHandlerManager;
        this.workflowUtils = workflowUtils;
        this.sensitiveDataHandler = sensitiveDataHandler;
        this.serviceDeploymentEntityConverter = serviceDeploymentEntityConverter;
        this.serviceOrderManager = serviceOrderManager;
        this.deployerKindManager = deployerKindManager;
    }

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
    public void updateServiceWithDeployResult(DeployResult deployResult, Handler handler) {
        if (Objects.isNull(deployResult)
                || Objects.isNull(deployResult.getOrderId())
                || Objects.isNull(deployResult.getIsTaskSuccessful())) {
            log.warn(
                    "Deploy result has no useful information. No update required. DeployResult: {}",
                    deployResult);
            return;
        }
        UUID orderId = deployResult.getOrderId();
        ServiceOrderEntity storedServiceOrder = serviceOrderStorage.getEntityById(orderId);
        ServiceOrderType taskType = storedServiceOrder.getTaskType();
        boolean isRollbackRequired =
                isFailedDeployTask(deployResult.getIsTaskSuccessful(), taskType)
                        && Objects.nonNull(deployResult.getTfStateContent());
        ServiceDeploymentEntity updatedServiceDeployment =
                updateServiceDeploymentWithDeployResult(
                        deployResult, storedServiceOrder, taskType, isRollbackRequired);
        updateServiceOrderEntityWithDeployResult(deployResult, storedServiceOrder);
        if (isRollbackRequired) {
            DeployTask rollbackTask =
                    serviceDeploymentEntityConverter.getDeployTaskByStoredService(
                            ServiceOrderType.ROLLBACK, updatedServiceDeployment);
            rollbackTask.setParentOrderId(orderId);
            rollbackTask.setOriginalServiceId(storedServiceOrder.getOriginalServiceId());
            rollbackTask.setWorkflowId(storedServiceOrder.getWorkflowId());
            rollbackOnDeploymentFailure(rollbackTask, updatedServiceDeployment, handler);
        }
    }

    /** Perform rollback when deployment fails and destroy the created resources. */
    public void rollbackOnDeploymentFailure(
            DeployTask rollbackTask,
            ServiceDeploymentEntity serviceDeploymentEntity,
            Handler handler) {
        DeployResult rollbackResult;
        RuntimeException exception = null;
        log.info("Performing rollback of already provisioned resources.");
        rollbackTask.setTaskType(ServiceOrderType.ROLLBACK);
        ServiceOrderEntity serviceOrderEntity =
                serviceOrderManager.storeNewServiceOrderEntity(
                        rollbackTask, serviceDeploymentEntity, handler);
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
        updateServiceWithDeployResult(rollbackResult, handler);
        if (Objects.nonNull(exception)) {
            throw new XpanseUnhandledException(exception.getMessage());
        }
    }

    private ServiceDeploymentEntity updateServiceDeploymentWithDeployResult(
            DeployResult deployResult,
            ServiceOrderEntity serviceOrderEntity,
            ServiceOrderType taskType,
            boolean isRollbackRequired) {
        ServiceDeploymentEntity serviceDeploymentEntity =
                serviceOrderEntity.getServiceDeploymentEntity();
        log.info(
                "Updating service deployment with id:{} by deploy result {}.",
                serviceDeploymentEntity.getId(),
                deployResult);
        ServiceDeploymentEntity serviceDeploymentToUpdate = new ServiceDeploymentEntity();
        BeanUtils.copyProperties(serviceDeploymentEntity, serviceDeploymentToUpdate);
        handleDeploymentResult(deployResult, serviceDeploymentToUpdate);
        boolean isTaskSuccessful = deployResult.getIsTaskSuccessful();
        ServiceDeploymentState deploymentState =
                getServiceDeploymentState(taskType, isTaskSuccessful, isRollbackRequired);
        updateServiceState(deploymentState, serviceDeploymentToUpdate);
        if (Objects.nonNull(deploymentState)) {
            serviceDeploymentToUpdate.setServiceDeploymentState(deploymentState);
        }
        if (deploymentState == ServiceDeploymentState.MODIFICATION_SUCCESSFUL) {
            try {
                Map<String, Object> request =
                        serviceOrderManager.getRequestByStoredOrder(deployResult.getOrderId());
                ModifyRequest modifyRequest =
                        objectMapper.convertValue(request, ModifyRequest.class);
                serviceDeploymentToUpdate.setFlavor(modifyRequest.getFlavor());
                serviceDeploymentToUpdate.setCustomerServiceName(
                        modifyRequest.getCustomerServiceName());
                if (!CollectionUtils.isEmpty(modifyRequest.getServiceRequestProperties())) {
                    Map<String, String> inputProperties = new HashMap<>();
                    for (Map.Entry<String, Object> entry :
                            modifyRequest.getServiceRequestProperties().entrySet()) {
                        inputProperties.put(entry.getKey(), String.valueOf(entry.getValue()));
                    }
                    serviceDeploymentToUpdate.setInputProperties(inputProperties);
                }
            } catch (IllegalArgumentException e) {
                log.error("Failed to convert request to ModifyRequest.");
            }
        }
        if (Objects.nonNull(
                serviceDeploymentEntity
                        .getServiceTemplateEntity()
                        .getOcl()
                        .getServiceConfigurationManage())) {
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
            Deployment deployment = getDeploymentByServiceOrder(serviceOrderEntity);
            if (Objects.nonNull(deployment)) {
                List<OutputVariable> outputVariables =
                        DeploymentVariableHelper.getOutputVariables(deployment);
                sensitiveDataHandler.encodeOutputVariables(
                        outputVariables, deployResult.getOutputProperties());
            }
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
                    serviceDeploymentEntity.getServiceTemplateEntity();
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
                    serviceDeploymentEntityConverter.getInitialServiceConfiguration(
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
        if (state == ServiceDeploymentState.MODIFICATION_FAILED) {
            // when modification fails, we do not know what's the exact state of the service.
            serviceDeploymentEntity.setServiceState(ServiceState.UNKNOWN);
        }
        // case other cases, do not change the state of service .
    }

    private ServiceDeploymentState getServiceDeploymentState(
            ServiceOrderType taskType, boolean isTaskSuccessful, boolean isRollbackRequired) {
        return switch (taskType) {
            case DEPLOY, RETRY ->
                    isTaskSuccessful
                            ? ServiceDeploymentState.DEPLOY_SUCCESS
                            : isRollbackRequired
                                    ? ServiceDeploymentState.ROLLING_BACK
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

        Deployment deployment = getDeploymentByServiceOrder(storedOrderEntity);
        if (Objects.nonNull(deployment)) {
            List<InputVariable> inputVariables =
                    DeploymentVariableHelper.getInputVariables(deployment);
            if (!CollectionUtils.isEmpty(inputVariables)
                    && !CollectionUtils.isEmpty(entityToUpdate.getRequestBody())) {
                Map<String, Object> requestBodyWithSensitiveFields =
                        sensitiveDataHandler.getOrderRequestBodyWithSensitiveFields(
                                storedOrderEntity.getRequestBody(), inputVariables);
                entityToUpdate.setRequestBody(requestBodyWithSensitiveFields);
            }
        }

        serviceOrderManager.completeOrderProgressWithDeployResult(entityToUpdate, deployResult);
    }

    private Deployment getDeploymentByServiceOrder(ServiceOrderEntity serviceOrderEntity) {
        try {
            return serviceOrderEntity
                    .getServiceDeploymentEntity()
                    .getServiceTemplateEntity()
                    .getOcl()
                    .getDeployment();
        } catch (Exception e) {
            log.error("Failed to get deployment by service order:{}.", serviceOrderEntity);
            return null;
        }
    }

    private void completeParentServiceOrder(UUID parentOrderId, DeployResult deployResult) {
        ServiceOrderEntity parentOrder = serviceOrderStorage.getEntityById(parentOrderId);

        // if parent order is already in final state, do not update the parent service order.
        if (parentOrder.getOrderStatus() == OrderStatus.FAILED
                || parentOrder.getOrderStatus() == OrderStatus.SUCCESSFUL) {
            return;
        }
        // When the parent order is not port or recreate task, complete it.
        if (parentOrder.getTaskType() != ServiceOrderType.PORT
                && parentOrder.getTaskType() != ServiceOrderType.RECREATE) {
            ServiceOrderEntity entityToUpdate = new ServiceOrderEntity();
            BeanUtils.copyProperties(parentOrder, entityToUpdate);
            serviceOrderManager.completeOrderProgressWithDeployResult(entityToUpdate, deployResult);
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
                    Map<String, Object> variables = new HashMap<>();
                    variables.put(
                            ServicePortingConstants.RESULT_MESSAGE, deployResult.getMessage());
                    if (serviceOrder.getTaskType() == ServiceOrderType.DEPLOY
                            || serviceOrder.getTaskType() == ServiceOrderType.RETRY) {
                        workflowUtils.completeReceiveTaskWithVariables(
                                processInstanceId,
                                ServicePortingConstants
                                        .SERVICE_PORTING_DEPLOY_RECEIVE_TASK_ACTIVITY_ID,
                                variables);
                    }
                    if (serviceOrder.getTaskType() == ServiceOrderType.DESTROY) {
                        workflowUtils.completeReceiveTaskWithVariables(
                                processInstanceId,
                                ServicePortingConstants
                                        .SERVICE_PORTING_DESTROY_RECEIVE_TASK_ACTIVITY_ID,
                                variables);
                    }
                }
                if (parentOrder.getTaskType() == ServiceOrderType.RECREATE) {
                    Map<String, Object> variables = new HashMap<>();
                    variables.put(RecreateConstants.RESULT_MESSAGE, deployResult.getMessage());
                    if (serviceOrder.getTaskType() == ServiceOrderType.DEPLOY
                            || serviceOrder.getTaskType() == ServiceOrderType.RETRY) {
                        workflowUtils.completeReceiveTaskWithVariables(
                                processInstanceId,
                                RecreateConstants.RECREATE_DEPLOY_RECEIVE_TASK_ACTIVITY_ID,
                                variables);
                    }
                    if (serviceOrder.getTaskType() == ServiceOrderType.DESTROY) {
                        workflowUtils.completeReceiveTaskWithVariables(
                                processInstanceId,
                                RecreateConstants.RECREATE_DESTROY_RECEIVE_TASK_ACTIVITY_ID,
                                variables);
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
            String errorMessage) {
        serviceEntity.setServiceDeploymentState(ServiceDeploymentState.MANUAL_CLEANUP_REQUIRED);
        serviceOrder.setOrderStatus(OrderStatus.FAILED);
        serviceOrder.setErrorResponse(
                ErrorResponse.errorResponse(errorType, List.of(errorMessage)));
        serviceDeploymentStorage.storeAndFlush(serviceEntity);
        serviceOrderStorage.storeAndFlush(serviceOrder);
    }
}
