/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.polling;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderStorage;
import org.eclipse.xpanse.modules.deployment.config.OrderProperties;
import org.eclipse.xpanse.modules.models.service.enums.OrderStatus;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrderStatusUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;

/** Bean implements long-polling mechanism to return the task status of the service order. */
@Slf4j
@RefreshScope
@Component
public class ServiceOrderStatusChangePolling {

    private static final List<OrderStatus> FINAL_TASK_STATUS =
            Arrays.asList(OrderStatus.FAILED, OrderStatus.SUCCESSFUL);

    private final OrderProperties orderProperties;
    private final ServiceOrderStorage orderStorage;

    @Autowired
    public ServiceOrderStatusChangePolling(
            OrderProperties orderProperties, ServiceOrderStorage orderStorage) {
        this.orderProperties = orderProperties;
        this.orderStorage = orderStorage;
    }

    /**
     * Fetch status of the service order by polling database for a fixed period of time.
     *
     * @param deferredResult deferredResult object from the original HTTP thread to which the result
     *     object must be set.
     * @param orderId id of the service order.
     * @param previousKnownOrderStatus previously known task status of the service order to the
     *     client. the poller will wait as long as there is a change to this.
     */
    public void fetchServiceOrderStatusWithPolling(
            DeferredResult<ServiceOrderStatusUpdate> deferredResult,
            UUID orderId,
            OrderStatus previousKnownOrderStatus) {
        log.info("Start polling for service order status with order id: {}", orderId);
        AtomicReference<ServiceOrderStatusUpdate> ref =
                new AtomicReference<>(
                        new ServiceOrderStatusUpdate(
                                previousKnownOrderStatus,
                                FINAL_TASK_STATUS.contains(previousKnownOrderStatus),
                                null));
        try {
            Awaitility.await()
                    .atMost(
                            orderProperties.getOrderStatus().getLongPollingSeconds(),
                            TimeUnit.SECONDS)
                    .pollDelay(0, TimeUnit.SECONDS) // first check runs without wait.
                    .pollInterval(
                            orderProperties.getOrderStatus().getPollingIntervalSeconds(),
                            TimeUnit.SECONDS)
                    .until(
                            () -> {
                                ServiceOrderEntity serviceOrderEntity =
                                        orderStorage.getEntityById(orderId);
                                OrderStatus orderStatus = serviceOrderEntity.getOrderStatus();
                                boolean isOrderCompleted = FINAL_TASK_STATUS.contains(orderStatus);
                                ref.set(
                                        new ServiceOrderStatusUpdate(
                                                orderStatus,
                                                isOrderCompleted,
                                                serviceOrderEntity.getErrorResponse()));
                                boolean statusIsChanged =
                                        Objects.nonNull(previousKnownOrderStatus)
                                                && orderStatus != previousKnownOrderStatus;
                                return isOrderCompleted || statusIsChanged;
                            });
        } catch (ConditionTimeoutException conditionTimeoutException) {
            log.error("The service order is not completed or status not changed yet.");
        } catch (Exception exception) {
            log.error(
                    "Error occurred while polling for service order status with order id: {}",
                    orderId,
                    exception);
            deferredResult.setErrorResult(exception);
        }
        deferredResult.setResult(ref.get());
    }
}
