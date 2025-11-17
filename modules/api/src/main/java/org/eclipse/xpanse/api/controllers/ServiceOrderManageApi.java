/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.controllers;

import static org.eclipse.xpanse.modules.security.auth.common.RoleConstants.ROLE_ADMIN;
import static org.eclipse.xpanse.modules.security.auth.common.RoleConstants.ROLE_ISV;
import static org.eclipse.xpanse.modules.security.auth.common.RoleConstants.ROLE_USER;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.api.config.AuditApiRequest;
import org.eclipse.xpanse.modules.deployment.ServiceOrderManager;
import org.eclipse.xpanse.modules.models.service.enums.OrderStatus;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrderDetails;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrderStatusUpdate;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

/** Service Orders Management REST API. */
@Slf4j
@RestController
@RequestMapping("/xpanse")
@CrossOrigin
@Secured({ROLE_ADMIN, ROLE_ISV, ROLE_USER})
@ConditionalOnProperty(
        name = "xpanse.agent-api.enable-agent-api-only",
        havingValue = "false",
        matchIfMissing = true)
public class ServiceOrderManageApi {
    @Resource private ServiceOrderManager serviceOrderManager;

    /**
     * List service orders by the service id.
     *
     * @param serviceId id of the service.
     * @param taskType task type of the service order.
     * @param orderStatus task status of the order.
     * @return service orders.
     */
    @Tag(name = "ServiceOrders", description = "APIs to manage orders of services")
    @Operation(description = "List service orders of the service")
    @GetMapping(value = "/services/{serviceId}/orders", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(methodName = "getCspFromServiceId", paramTypes = UUID.class)
    public List<ServiceOrderDetails> getAllOrdersByServiceId(
            @Parameter(name = "serviceId", description = "Id of the service")
                    @PathVariable(name = "serviceId")
                    UUID serviceId,
            @Parameter(name = "taskType", description = "Task type of the service order.")
                    @RequestParam(name = "taskType", required = false)
                    ServiceOrderType taskType,
            @Parameter(name = "orderStatus", description = "Task status of the service order")
                    @RequestParam(name = "orderStatus", required = false)
                    OrderStatus orderStatus) {
        return serviceOrderManager.listServiceOrders(serviceId, taskType, orderStatus);
    }

    /**
     * Delete all orders of the service.
     *
     * @param serviceId id of the service.
     */
    @Tag(name = "ServiceOrders", description = "APIs to manage orders of services")
    @Operation(description = "Delete all service orders of the service.")
    @DeleteMapping(
            value = "/services/{serviceId}/orders",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @AuditApiRequest(methodName = "getCspFromServiceId", paramTypes = UUID.class)
    public void deleteOrdersByServiceId(
            @Parameter(name = "serviceId", description = "Id of the service")
                    @PathVariable(name = "serviceId")
                    UUID serviceId) {
        serviceOrderManager.deleteOrdersByServiceId(serviceId);
    }

    /**
     * Get details of the service order by the order id.
     *
     * @param orderId id of the service order.
     * @return order details.
     */
    @Tag(name = "ServiceOrders", description = "APIs to manage orders of services")
    @Operation(description = "Get details of the service order by the order id.")
    @GetMapping(value = "/services/orders/{orderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(methodName = "getCspFromServiceOrderId", paramTypes = UUID.class)
    public ServiceOrderDetails getOrderDetailsByOrderId(
            @Parameter(name = "orderId", description = "Id of the service order")
                    @PathVariable(name = "orderId")
                    UUID orderId) {
        return serviceOrderManager.getOrderDetailsByOrderId(orderId);
    }

    /**
     * Delete service order by the order id.
     *
     * @param orderId id of the service order.
     */
    @Tag(name = "ServiceOrders", description = "APIs to manage orders of services")
    @Operation(description = "Delete the service order by the order id.")
    @DeleteMapping(
            value = "/services/orders/{orderId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @AuditApiRequest(methodName = "getCspFromServiceOrderId", paramTypes = UUID.class)
    public void deleteOrderByOrderId(
            @Parameter(name = "orderId", description = "Id of the service order")
                    @PathVariable(name = "orderId")
                    UUID orderId) {
        serviceOrderManager.deleteOrderByOrderId(orderId);
    }

    /** Method to fetch status of service deployment. */
    @Tag(name = "ServiceOrders", description = "APIs to manage orders of services")
    @GetMapping(
            value = "/services/orders/{orderId}/status",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            description =
                    "Long-polling method to get the latest or updated task status of the service"
                            + " order.")
    @AuditApiRequest(methodName = "getCspFromServiceOrderId", paramTypes = UUID.class)
    @ResponseStatus(HttpStatus.OK)
    public DeferredResult<ServiceOrderStatusUpdate> getLatestServiceOrderStatus(
            @Parameter(name = "orderId", description = "Id of the service order")
                    @PathVariable(name = "orderId")
                    UUID orderId,
            @Parameter(
                            name = "lastKnownServiceDeploymentState",
                            description =
                                    "Last known service order task status to client. When provided,"
                                            + " the service will wait for a configured period time"
                                            + " until to see if there is a change to the last known"
                                            + " state.")
                    @RequestParam(name = "lastKnownServiceOrderStatus", required = false)
                    OrderStatus lastKnownServiceOrderStatus) {
        return serviceOrderManager.getLatestServiceOrderStatus(
                orderId, lastKnownServiceOrderStatus);
    }
}
