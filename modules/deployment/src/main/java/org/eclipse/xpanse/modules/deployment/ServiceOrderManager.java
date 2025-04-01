/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import static org.eclipse.xpanse.modules.async.TaskConfiguration.ASYNC_EXECUTOR_NAME;
import static org.eclipse.xpanse.modules.security.auth.common.RoleConstants.ROLE_ADMIN;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentStorage;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderStorage;
import org.eclipse.xpanse.modules.database.utils.EntityTranslationUtils;
import org.eclipse.xpanse.modules.deployment.polling.ServiceOrderStatusChangePolling;
import org.eclipse.xpanse.modules.deployment.utils.MdcUtils;
import org.eclipse.xpanse.modules.models.common.enums.UserOperation;
import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.response.ErrorType;
import org.eclipse.xpanse.modules.models.service.deployment.DeployResult;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.service.enums.Handler;
import org.eclipse.xpanse.modules.models.service.enums.OrderStatus;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrderDetails;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrderStatusUpdate;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.models.service.order.exceptions.ServiceOrderNotFound;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.security.auth.UserServiceHelper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;

/** Bean to manage service order tasks. */
@Slf4j
@Component
public class ServiceOrderManager {

    private final ObjectMapper objectMapper = new ObjectMapper();
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
        orderTask.setParentOrderId(task.getParentOrderId());
        orderTask.setTaskType(task.getTaskType());
        orderTask.setUserId(task.getUserId());
        orderTask.setServiceDeploymentEntity(serviceDeploymentEntity);
        orderTask.setOriginalServiceId(task.getOriginalServiceId());
        orderTask.setWorkflowId(task.getWorkflowId());
        orderTask.setOrderStatus(OrderStatus.CREATED);
        orderTask.setStartedTime(OffsetDateTime.now());
        orderTask.setRequestBody(getRequestBody(task.getRequest()));
        orderTask.setHandler(handler);
        orderTask = serviceOrderStorage.storeAndFlush(orderTask);
        MdcUtils.putServiceIdAndOrderId(
                serviceDeploymentEntity.getId().toString(), orderTask.getOrderId().toString());
        task.setOrderId(orderTask.getOrderId());
        task.setServiceId(serviceDeploymentEntity.getId());
        return orderTask;
    }

    private Map<String, Object> getRequestBody(Object request) {
        try {
            return objectMapper.readValue(objectMapper.writeValueAsString(request), Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates and stores any generic request in SERVICE_ORDER table.
     *
     * @param serviceDeploymentEntity serviceDeploymentEntity to which the order is related to.
     * @param serviceOrderType type of the order.
     * @param originalRequest The request body received from the customer. Will be serialized into
     *     JSON and stored.
     * @return serviceOrderEntity created.
     */
    public ServiceOrderEntity createAndStoreGenericServiceOrderEntity(
            ServiceDeploymentEntity serviceDeploymentEntity,
            ServiceOrderType serviceOrderType,
            Object originalRequest,
            Handler handler) {
        ServiceOrderEntity serviceOrderEntity = new ServiceOrderEntity();
        if (Objects.nonNull(serviceDeploymentEntity.getServiceOrders())) {
            serviceDeploymentEntity.getServiceOrders().add(serviceOrderEntity);
        } else {
            serviceDeploymentEntity.setServiceOrders(List.of(serviceOrderEntity));
        }
        serviceOrderEntity.setServiceDeploymentEntity(serviceDeploymentEntity);
        serviceOrderEntity.setTaskType(serviceOrderType);
        serviceOrderEntity.setUserId(userServiceHelper.getCurrentUserId());
        serviceOrderEntity.setOrderStatus(OrderStatus.CREATED);
        serviceOrderEntity.setStartedTime(OffsetDateTime.now());
        serviceOrderEntity.setRequestBody(getRequestBody(originalRequest));
        serviceOrderEntity.setHandler(handler);
        return serviceOrderStorage.storeAndFlush(serviceOrderEntity);
    }

    /**
     * Start order progress.
     *
     * @param serviceOrder entity.
     * @return service order entity.
     */
    public ServiceOrderEntity startOrderProgress(ServiceOrderEntity serviceOrder) {
        serviceOrder.setOrderStatus(OrderStatus.IN_PROGRESS);
        serviceOrder.setStartedTime(OffsetDateTime.now());
        return serviceOrderStorage.storeAndFlush(serviceOrder);
    }

    /**
     * Complete order progress.
     *
     * @param orderId service order id.
     * @param orderStatus task status.
     */
    public void completeOrderProgress(
            UUID orderId, OrderStatus orderStatus, ErrorResponse errorResponse) {
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
        serviceOrder.setOrderStatus(orderStatus);
        serviceOrder.setCompletedTime(OffsetDateTime.now());
        if (Objects.nonNull(errorResponse)) {
            serviceOrder.setErrorResponse(errorResponse);
        }
        serviceOrderStorage.storeAndFlush(serviceOrder);
    }

    /**
     * Complete order progress.
     *
     * @param serviceOrder service order entity.
     * @param deployResult deploy result of this order.
     */
    public void completeOrderProgressWithDeployResult(
            ServiceOrderEntity serviceOrder, DeployResult deployResult) {
        if (deployResult.getIsTaskSuccessful()) {
            serviceOrder.setOrderStatus(OrderStatus.SUCCESSFUL);
        } else {
            serviceOrder.setOrderStatus(OrderStatus.FAILED);
            ErrorResponse errorResponse =
                    ErrorResponse.errorResponse(
                            ErrorType.DEPLOYMENT_FAILED_EXCEPTION,
                            List.of(deployResult.getMessage()));
            serviceOrder.setErrorResponse(errorResponse);
        }
        serviceOrder.setCompletedTime(OffsetDateTime.now());
        serviceOrder.setResultProperties(getResultProperties(deployResult));
        serviceOrderStorage.storeAndFlush(serviceOrder);
    }

    /**
     * Update service order with deploy result.
     *
     * @param serviceOrder entity.
     * @param deployResult deploy result.
     */
    public void updateOrderWithDeployResult(
            ServiceOrderEntity serviceOrder, DeployResult deployResult) {
        serviceOrder.setResultProperties(getResultProperties(deployResult));
        serviceOrderStorage.storeAndFlush(serviceOrder);
    }

    /**
     * List the service orders.
     *
     * @param serviceId service id.
     * @param orderStatus order status.
     * @return list of service orders.
     */
    public List<ServiceOrderDetails> listServiceOrders(
            UUID serviceId, ServiceOrderType taskType, OrderStatus orderStatus) {
        validateDeployService(serviceId, UserOperation.VIEW_ORDERS_OF_SERVICE);
        ServiceOrderEntity query = new ServiceOrderEntity();
        query.setServiceDeploymentEntity(
                serviceDeploymentStorage.findServiceDeploymentById(serviceId));
        query.setTaskType(taskType);
        query.setOrderStatus(orderStatus);
        if (!userServiceHelper.currentUserHasRole(ROLE_ADMIN)) {
            query.setUserId(userServiceHelper.getCurrentUserId());
        }
        List<ServiceOrderEntity> orderEntities = serviceOrderStorage.queryEntities(query);
        return orderEntities.stream()
                .map(EntityTranslationUtils::transToServiceOrderDetails)
                .toList();
    }

    /**
     * Get the task status update of the service order.
     *
     * @param orderId if of the service order.
     * @param lastKnownOrderStatus last known task status of the service order.
     * @return updated service order status.
     */
    public DeferredResult<ServiceOrderStatusUpdate> getLatestServiceOrderStatus(
            UUID orderId, OrderStatus lastKnownOrderStatus) {
        DeferredResult<ServiceOrderStatusUpdate> stateDeferredResult = new DeferredResult<>();
        taskExecutor.execute(
                () -> {
                    try {
                        this.serviceOrderStatusChangePolling.fetchServiceOrderStatusWithPolling(
                                stateDeferredResult, orderId, lastKnownOrderStatus);
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
        validateDeployService(serviceId, UserOperation.DELETE_ORDERS_OF_SERVICE);
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
        ServiceOrderEntity orderEntity =
                getServiceOrderEntity(orderId, UserOperation.VIEW_ORDER_DETAILS_OF_SERVICE);
        return EntityTranslationUtils.transToServiceOrderDetails(orderEntity);
    }

    /**
     * Delete the service order with the order id.
     *
     * @param orderId order id.
     */
    public void deleteOrderByOrderId(UUID orderId) {
        ServiceOrderEntity orderEntity =
                getServiceOrderEntity(orderId, UserOperation.DELETE_ORDERS_OF_SERVICE);
        serviceOrderStorage.delete(orderEntity);
    }

    private void validateDeployService(UUID serviceId, UserOperation userOperation) {
        ServiceDeploymentEntity deployedService =
                serviceDeploymentStorage.findServiceDeploymentById(serviceId);
        if (Objects.nonNull(deployedService)) {
            checkPermission(deployedService, userOperation);
        } else {
            String errorMsg = "Service with id " + serviceId + " not found.";
            throw new ServiceNotDeployedException(errorMsg);
        }
    }

    /**
     * Method to get request from service order by order id.
     *
     * @param orderId orderId
     * @return request
     */
    public Map<String, Object> getRequestByStoredOrder(UUID orderId) {
        ServiceOrderEntity serviceOrder = serviceOrderStorage.getEntityById(orderId);
        return serviceOrder.getRequestBody();
    }

    /**
     * Get the service order entity with the order id.
     *
     * @param orderId order id.
     * @return service order entity.
     */
    private ServiceOrderEntity getServiceOrderEntity(UUID orderId, UserOperation userOperation) {
        ServiceOrderEntity orderEntity = serviceOrderStorage.getEntityById(orderId);
        if (Objects.nonNull(orderEntity)) {
            ServiceDeploymentEntity deployedService = orderEntity.getServiceDeploymentEntity();
            if (Objects.nonNull(deployedService)) {
                checkPermission(deployedService, userOperation);
            }
        }
        return orderEntity;
    }

    private void checkPermission(
            ServiceDeploymentEntity serviceDeploymentEntity, UserOperation userOperation) {
        boolean isOwner = userServiceHelper.currentUserIsOwner(serviceDeploymentEntity.getUserId());
        boolean isAdmin = userServiceHelper.currentUserHasRole(ROLE_ADMIN);
        if (!isOwner && !isAdmin) {
            String errorMsg =
                    String.format(
                            "No permission to %s owned by other users.", userOperation.toValue());
            log.error(errorMsg);
            throw new AccessDeniedException(errorMsg);
        }
    }

    private Map<String, Object> getResultProperties(DeployResult deployResult) {
        Map<String, Object> resultProperties = new HashMap<>();
        resultProperties.put("Output Properties", deployResult.getOutputProperties());
        resultProperties.put("Deployed Resources", deployResult.getResources());
        return resultProperties;
    }
}
