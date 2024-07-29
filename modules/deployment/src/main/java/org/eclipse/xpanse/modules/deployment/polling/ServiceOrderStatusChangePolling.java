/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.polling;

import jakarta.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderStorage;
import org.eclipse.xpanse.modules.deployment.DeployServiceEntityHandler;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrderStatusUpdate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * Bean implements long-polling mechanism to return the task status of the service order.
 */
@Slf4j
@Component
public class ServiceOrderStatusChangePolling {

    private static final List<TaskStatus> FINAL_TASK_STATUS
            = Arrays.asList(TaskStatus.FAILED, TaskStatus.SUCCESSFUL);
    @Value("${service.order.status.long.polling.interval.in.seconds:5}")
    private int pollingInterval;
    @Value("${service.order.status.long.polling.wait.time.in.seconds:60}")
    private int pollingWaitPeriod;
    @Resource
    private ServiceOrderStorage orderStorage;
    @Resource
    private DeployServiceEntityHandler deployServiceEntityHandler;

    /**
     * Fetch status of the service order by polling database for a fixed period of time.
     *
     * @param deferredResult          deferredResult object from the original HTTP thread to
     *                                which the result object must be set.
     * @param orderId                 id of the service order.
     * @param previousKnownTaskStatus previously known task status of the service order to the
     *                                client. the poller will wait as long as there is a change
     *                                to this.
     */
    public void fetchServiceOrderTaskStatusWithPolling(
            DeferredResult<ServiceOrderStatusUpdate> deferredResult, UUID orderId,
            TaskStatus previousKnownTaskStatus) {
        log.info("Start polling for service order status with order id: {}", orderId);
        AtomicReference<ServiceOrderStatusUpdate> ref =
                new AtomicReference<>(new ServiceOrderStatusUpdate(previousKnownTaskStatus,
                        FINAL_TASK_STATUS.contains(previousKnownTaskStatus), null, null));
        try {
            Awaitility.await().atMost(pollingWaitPeriod, TimeUnit.SECONDS)
                    .pollDelay(0, TimeUnit.SECONDS) // first check runs without wait.
                    .pollInterval(pollingInterval, TimeUnit.SECONDS).until(() -> {
                        ServiceOrderEntity serviceOrderEntity = orderStorage.getEntityById(orderId);
                        TaskStatus taskStatus = serviceOrderEntity.getTaskStatus();
                        boolean isOrderCompleted = FINAL_TASK_STATUS.contains(taskStatus);
                        Map<String, String> deployServiceProperties = new HashMap<>();
                        if (taskStatus == TaskStatus.SUCCESSFUL) {
                            DeployServiceEntity entity = deployServiceEntityHandler
                                    .getDeployServiceEntity(serviceOrderEntity.getServiceId());
                            deployServiceProperties = entity.getProperties();
                        }
                        ref.set(new ServiceOrderStatusUpdate(taskStatus, isOrderCompleted,
                                serviceOrderEntity.getErrorMsg(), deployServiceProperties));
                        boolean statusIsChanged = Objects.nonNull(previousKnownTaskStatus)
                                && taskStatus != previousKnownTaskStatus;
                        return isOrderCompleted || statusIsChanged;
                    });
        } catch (ConditionTimeoutException conditionTimeoutException) {
            log.error("The service order is not completed or status not changed yet.");
        } catch (Exception exception) {
            log.error("Error occurred while polling for service order status with order id: {}",
                    orderId, exception);
            deferredResult.setErrorResult(exception);
        }
        deferredResult.setResult(ref.get());
    }

}
