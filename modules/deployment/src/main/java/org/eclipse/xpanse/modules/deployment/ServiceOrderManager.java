/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import static org.eclipse.xpanse.modules.async.TaskConfiguration.ASYNC_EXECUTOR_NAME;
import static org.eclipse.xpanse.modules.security.common.RoleConstants.ROLE_ADMIN;

import jakarta.annotation.Resource;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentStorage;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderStorage;
import org.eclipse.xpanse.modules.database.utils.EntityTransUtils;
import org.eclipse.xpanse.modules.deployment.polling.ServiceOrderStatusChangePolling;
import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.service.enums.Handler;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrderDetails;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrderStatusUpdate;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.models.service.order.exceptions.ServiceOrderNotFound;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.security.UserServiceHelper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.async.DeferredResult;

/** Bean to manage service order tasks. */
@Slf4j
@Component
public class ServiceOrderManager {
    @Resource private ServiceOrderStorage serviceOrderStorage;
    @Resource private ServiceDeploymentStorage serviceDeploymentStorage;
    @Resource private UserServiceHelper userServiceHelper;
    @Resource private ServiceOrderStatusChangePolling serviceOrderStatusChangePolling;

    @Resource(name = ASYNC_EXECUTOR_NAME)
    private Executor taskExecutor;

    /**
     * Create service order entity and store into the database.
     *
     * @param task task to be created
     * @param serviceDeploymentEntity service deployment entity
     */
    public ServiceOrderEntity storeNewServiceOrderEntity(
            DeployTask task, ServiceDeploymentEntity serviceDeploymentEntity, Handler handler) {
        ServiceOrderEntity orderTask = new ServiceOrderEntity();
        orderTask.setOrderId(task.getOrderId());
        orderTask.setParentOrderId(task.getParentOrderId());
        orderTask.setTaskType(task.getTaskType());
        orderTask.setUserId(task.getUserId());
        orderTask.setServiceDeploymentEntity(serviceDeploymentEntity);
        orderTask.setOriginalServiceId(task.getOriginalServiceId());
        orderTask.setWorkflowId(task.getWorkflowId());
        orderTask.setNewDeployRequest(task.getDeployRequest());
        orderTask.setTaskStatus(TaskStatus.CREATED);
        orderTask.setRequestBody(task.getRequest());
        if (Objects.nonNull(serviceDeploymentEntity)) {
            orderTask.setPreviousDeployRequest(serviceDeploymentEntity.getDeployRequest());
            orderTask.setPreviousDeployedResources(
                    EntityTransUtils.transToDeployResourceList(
                            serviceDeploymentEntity.getDeployResourceList()));
            if (!CollectionUtils.isEmpty(serviceDeploymentEntity.getDeploymentGeneratedFiles())) {
                serviceDeploymentEntity.setDeploymentGeneratedFiles(
                        new HashMap<>(serviceDeploymentEntity.getDeploymentGeneratedFiles()));
            }
            if (!CollectionUtils.isEmpty(serviceDeploymentEntity.getOutputProperties())) {
                orderTask.setPreviousDeployedServiceProperties(
                        new HashMap<>(serviceDeploymentEntity.getOutputProperties()));
            }
        }
        orderTask.setHandler(handler);
        return serviceOrderStorage.storeAndFlush(orderTask);
    }

    /**
     * Start order progress.
     *
     * @param serviceOrder entity.
     * @return service order entity.
     */
    public ServiceOrderEntity startOrderProgress(ServiceOrderEntity serviceOrder) {
        serviceOrder.setTaskStatus(TaskStatus.IN_PROGRESS);
        serviceOrder.setStartedTime(OffsetDateTime.now());
        return serviceOrderStorage.storeAndFlush(serviceOrder);
    }

    /**
     * Complete order progress.
     *
     * @param orderId service order id.
     * @param taskStatus task status.
     */
    public void completeOrderProgress(
            UUID orderId, TaskStatus taskStatus, ErrorResponse errorResponse) {
        ServiceOrderEntity serviceOrder = serviceOrderStorage.getEntityById(orderId);
        if (Objects.isNull(serviceOrder)) {
            String errMsg =
                    String.format(
                            "Service order with id %s not found "
                                    + "when try to complete order progress.",
                            orderId);
            log.error(errMsg);
            throw new ServiceOrderNotFound(errMsg);
        }
        serviceOrder.setTaskStatus(taskStatus);
        serviceOrder.setCompletedTime(OffsetDateTime.now());
        if (Objects.nonNull(errorResponse)) {
            serviceOrder.setErrorResponse(errorResponse);
        }
        serviceOrderStorage.storeAndFlush(serviceOrder);
    }

    /**
     * List the service orders.
     *
     * @param serviceId service id.
     * @param taskStatus order status.
     * @return list of service orders.
     */
    public List<ServiceOrderDetails> listServiceOrders(
            UUID serviceId, ServiceOrderType taskType, TaskStatus taskStatus) {
        validateDeployService(serviceId);
        ServiceOrderEntity query = new ServiceOrderEntity();
        query.setServiceDeploymentEntity(
                serviceDeploymentStorage.findServiceDeploymentById(serviceId));
        query.setTaskType(taskType);
        query.setTaskStatus(taskStatus);
        if (!userServiceHelper.currentUserHasRole(ROLE_ADMIN)) {
            query.setUserId(userServiceHelper.getCurrentUserId());
        }
        List<ServiceOrderEntity> orderEntities = serviceOrderStorage.queryEntities(query);
        return orderEntities.stream().map(EntityTransUtils::transToServiceOrderDetails).toList();
    }

    /**
     * Get the task status update of the service order.
     *
     * @param orderId if of the service order.
     * @param lastKnownTaskStatus last known task status of the service order.
     * @return updated service order status.
     */
    public DeferredResult<ServiceOrderStatusUpdate> getLatestServiceOrderStatus(
            UUID orderId, TaskStatus lastKnownTaskStatus) {
        DeferredResult<ServiceOrderStatusUpdate> stateDeferredResult = new DeferredResult<>();
        taskExecutor.execute(
                () -> {
                    try {
                        this.serviceOrderStatusChangePolling.fetchServiceOrderTaskStatusWithPolling(
                                stateDeferredResult, orderId, lastKnownTaskStatus);
                    } catch (Exception exception) {
                        stateDeferredResult.setErrorResult(exception);
                    }
                });
        return stateDeferredResult;
    }

    /**
     * Delete the service orders by the service id.
     *
     * @param serviceId service id.
     */
    public void deleteOrdersByServiceId(UUID serviceId) {
        validateDeployService(serviceId);
        ServiceOrderEntity query = new ServiceOrderEntity();
        query.setServiceDeploymentEntity(
                serviceDeploymentStorage.findServiceDeploymentById(serviceId));
        if (!userServiceHelper.currentUserHasRole(ROLE_ADMIN)) {
            query.setUserId(userServiceHelper.getCurrentUserId());
        }
        List<ServiceOrderEntity> orderEntities = serviceOrderStorage.queryEntities(query);
        serviceOrderStorage.deleteBatch(orderEntities);
    }

    /**
     * Get the service order details with the order id.
     *
     * @param orderId order id.
     * @return service order details.
     */
    public ServiceOrderDetails getOrderDetailsByOrderId(UUID orderId) {
        ServiceOrderEntity orderEntity = getServiceOrderEntity(orderId);
        return EntityTransUtils.transToServiceOrderDetails(orderEntity);
    }

    /**
     * Delete the service order with the order id.
     *
     * @param orderId order id.
     */
    public void deleteOrderByOrderId(UUID orderId) {
        ServiceOrderEntity orderEntity = getServiceOrderEntity(orderId);
        serviceOrderStorage.delete(orderEntity);
    }

    private void validateDeployService(UUID serviceId) {
        ServiceDeploymentEntity deployedService =
                serviceDeploymentStorage.findServiceDeploymentById(serviceId);
        if (Objects.nonNull(deployedService)) {
            if (isNotOwnerOrAdminUser(deployedService)) {
                String errorMsg =
                        "No permissions to manage service orders belonging to other users.";
                throw new AccessDeniedException(errorMsg);
            }
        } else {
            String errorMsg = "Service with id " + serviceId + " not found.";
            throw new ServiceNotDeployedException(errorMsg);
        }
    }

    /**
     * Get the service order entity with the order id.
     *
     * @param orderId order id.
     * @return service order entity.
     */
    private ServiceOrderEntity getServiceOrderEntity(UUID orderId) {
        ServiceOrderEntity orderEntity = serviceOrderStorage.getEntityById(orderId);
        if (Objects.nonNull(orderEntity)) {
            ServiceDeploymentEntity deployedService = orderEntity.getServiceDeploymentEntity();
            if (Objects.nonNull(deployedService)) {
                if (isNotOwnerOrAdminUser(deployedService)) {
                    String errorMsg =
                            "No permissions to manage service orders belonging to other users.";
                    throw new AccessDeniedException(errorMsg);
                }
            }
        }
        return orderEntity;
    }

    private boolean isNotOwnerOrAdminUser(ServiceDeploymentEntity serviceDeploymentEntity) {
        boolean isOwner = userServiceHelper.currentUserIsOwner(serviceDeploymentEntity.getUserId());
        boolean isAdmin = userServiceHelper.currentUserHasRole(ROLE_ADMIN);
        return !isOwner && !isAdmin;
    }
}
