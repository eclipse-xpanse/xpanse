/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

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
    @Resource
    private Executor taskExecutor;

    /**
     * Create service order task.
     *
     * @param deployTask              deploy task
     * @param previousDeployedService previous deployed service entity
     * @return DB entity of the service order
     */
    public ServiceOrderEntity createServiceOrderTask(
            DeployTask deployTask, DeployServiceEntity previousDeployedService) {
        ServiceOrderEntity orderTask = new ServiceOrderEntity();
        orderTask.setOrderId(deployTask.getOrderId());
        orderTask.setTaskType(deployTask.getTaskType());
        orderTask.setUserId(deployTask.getUserId());
        orderTask.setServiceId(deployTask.getServiceId());
        orderTask.setNewDeployRequest(deployTask.getDeployRequest());
        orderTask.setTaskStatus(TaskStatus.CREATED);
        if (Objects.nonNull(previousDeployedService)) {
            orderTask.setPreviousDeployRequest(previousDeployedService.getDeployRequest());
            orderTask.setPreviousDeployedResources(
                    EntityTransUtils.transToDeployResourceList(
                            previousDeployedService.getDeployResourceList()));
            orderTask.setPreviousDeployedResultProperties(
                    new HashMap<>(previousDeployedService.getPrivateProperties()));
            orderTask.setPreviousDeployedServiceProperties(
                    new HashMap<>(previousDeployedService.getProperties()));
        }
        return serviceOrderStorage.storeAndFlush(orderTask);
    }

    /**
     * Start order progress by id.
     *
     * @param orderId id of the order
     */
    public void startOrderProgress(UUID orderId) {
        ServiceOrderEntity orderTask =
                serviceOrderStorage.getEntityById(orderId);
        orderTask.setTaskStatus(TaskStatus.IN_PROGRESS);
        orderTask.setStartedTime(OffsetDateTime.now());
        serviceOrderStorage.storeAndFlush(orderTask);
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
        query.setServiceId(serviceId);
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
        query.setServiceId(serviceId);
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
     * Get the latest service order with the service id.
     *
     * @param serviceId service id.
     * @return service order details.
     */
    public ServiceOrderDetails getLatestModificationOrder(UUID serviceId) {
        ServiceOrderEntity query = new ServiceOrderEntity();
        query.setServiceId(serviceId);
        query.setTaskType(ServiceOrderType.MODIFY);
        List<ServiceOrderEntity> orderEntities = serviceOrderStorage.queryEntities(query);
        if (!CollectionUtils.isEmpty(orderEntities)) {
            return EntityTransUtils.transToServiceOrderDetails(
                    orderEntities.getFirst());
        }
        return null;
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
            DeployServiceEntity deployedService =
                    deployServiceStorage.findDeployServiceById(orderEntity.getServiceId());
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
