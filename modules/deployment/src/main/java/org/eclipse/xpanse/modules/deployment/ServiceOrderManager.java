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
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceStorage;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderStorage;
import org.eclipse.xpanse.modules.database.utils.EntityTransUtils;
import org.eclipse.xpanse.modules.deployment.polling.ServiceOrderStatusChangePolling;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrderDetails;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrderStatusUpdate;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.security.UserServiceHelper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * Bean to manage service order tasks.
 */
@Slf4j
@Component
public class ServiceOrderManager {
    @Resource
    private ServiceOrderStorage serviceOrderStorage;
    @Resource
    private DeployServiceStorage deployServiceStorage;
    @Resource
    private UserServiceHelper userServiceHelper;
    @Resource
    private ServiceOrderStatusChangePolling serviceOrderStatusChangePolling;
    @Resource(name = ASYNC_EXECUTOR_NAME)
    private Executor taskExecutor;

    /**
     * Create service order entity and store into the database.
     *
     * @param task                task to be created
     * @param deployServiceEntity deploy service entity
     */
    public ServiceOrderEntity storeNewServiceOrderEntity(DeployTask task,
                                                         DeployServiceEntity deployServiceEntity) {
        ServiceOrderEntity orderTask = new ServiceOrderEntity();
        orderTask.setOrderId(task.getOrderId());
        orderTask.setParentOrderId(task.getParentOrderId());
        orderTask.setTaskType(task.getTaskType());
        orderTask.setUserId(task.getUserId());
        orderTask.setDeployServiceEntity(deployServiceEntity);
        orderTask.setOriginalServerId(task.getServiceId());
        orderTask.setWorkflowId(task.getWorkflowId());
        orderTask.setNewDeployRequest(task.getDeployRequest());
        orderTask.setTaskStatus(TaskStatus.CREATED);
        if (Objects.nonNull(deployServiceEntity)) {
            orderTask.setPreviousDeployRequest(deployServiceEntity.getDeployRequest());
            orderTask.setPreviousDeployedResources(
                    EntityTransUtils.transToDeployResourceList(
                            deployServiceEntity.getDeployResourceList()));
            if (!CollectionUtils.isEmpty(deployServiceEntity.getPrivateProperties())) {
                deployServiceEntity.setPrivateProperties(
                        new HashMap<>(deployServiceEntity.getPrivateProperties()));
            }
            if (!CollectionUtils.isEmpty(deployServiceEntity.getProperties())) {
                orderTask.setPreviousDeployedServiceProperties(
                        new HashMap<>(deployServiceEntity.getProperties()));
            }
        }
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
     * List the service orders.
     *
     * @param serviceId  service id.
     * @param taskStatus order status.
     * @return list of service orders.
     */
    public List<ServiceOrderDetails> listServiceOrders(
            UUID serviceId, ServiceOrderType taskType, TaskStatus taskStatus) {
        validateDeployService(serviceId);
        ServiceOrderEntity query = new ServiceOrderEntity();
        query.setDeployServiceEntity(deployServiceStorage.findDeployServiceById(serviceId));
        query.setTaskType(taskType);
        query.setTaskStatus(taskStatus);
        if (!userServiceHelper.currentUserHasRole(ROLE_ADMIN)) {
            query.setUserId(userServiceHelper.getCurrentUserId());
        }
        List<ServiceOrderEntity> orderEntities = serviceOrderStorage.queryEntities(query);
        return orderEntities.stream()
                .map(EntityTransUtils::transToServiceOrderDetails)
                .toList();
    }


    /**
     * Get the task status update of the service order.
     *
     * @param orderId             if of the service order.
     * @param lastKnownTaskStatus last known task status of the service order.
     * @return updated service order status.
     */
    public DeferredResult<ServiceOrderStatusUpdate> getLatestServiceOrderStatus(
            UUID orderId, TaskStatus lastKnownTaskStatus) {
        DeferredResult<ServiceOrderStatusUpdate> stateDeferredResult = new DeferredResult<>();
        taskExecutor.execute(() -> {
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
        query.setDeployServiceEntity(deployServiceStorage.findDeployServiceById(serviceId));
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
        DeployServiceEntity deployedService = deployServiceStorage.findDeployServiceById(serviceId);
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
            DeployServiceEntity deployedService = orderEntity.getDeployServiceEntity();
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

    private boolean isNotOwnerOrAdminUser(DeployServiceEntity deployServiceEntity) {
        boolean isOwner = userServiceHelper.currentUserIsOwner(deployServiceEntity.getUserId());
        boolean isAdmin = userServiceHelper.currentUserHasRole(ROLE_ADMIN);
        return !isOwner && !isAdmin;
    }
}
