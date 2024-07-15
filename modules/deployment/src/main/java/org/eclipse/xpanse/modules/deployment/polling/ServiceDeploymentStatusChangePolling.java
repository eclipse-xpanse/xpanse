/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.polling;

import jakarta.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceStorage;
import org.eclipse.xpanse.modules.models.service.deploy.DeploymentStatusUpdate;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * Bean implements long-polling mechanism to return the status of the order.
 */
@Slf4j
@Component
public class ServiceDeploymentStatusChangePolling {

    private static final List<ServiceDeploymentState> FINAL_SERVICE_DEPLOYMENT_STATES
            = Arrays.asList(
            ServiceDeploymentState.DEPLOY_FAILED,
            ServiceDeploymentState.DEPLOY_SUCCESS,
            ServiceDeploymentState.DESTROY_FAILED,
            ServiceDeploymentState.DESTROY_SUCCESS,
            ServiceDeploymentState.MODIFICATION_FAILED,
            ServiceDeploymentState.MODIFICATION_SUCCESSFUL,
            ServiceDeploymentState.ROLLBACK_FAILED,
            ServiceDeploymentState.MANUAL_CLEANUP_REQUIRED
    );

    @Value("${service.status.long.polling.interval.in.seconds:5}")
    private int pollingInterval;

    @Value("${service.status.long.polling.wait.time.in.seconds:30}")
    private int pollingWaitPeriod;

    @Resource
    private DeployServiceStorage deployServiceStorage;

    /**
     * Method to fetch order status by polling database for a fixed period of time.
     *
     * @param deferredResult                      DeferredResult object from the original HTTP
     *                                            thread to which the result object must be set.
     * @param serviceId                           ID of the service.
     * @param previousKnownServiceDeploymentState previously known state of the service deployment
     *                                            to client. If not null, the poller will wait as
     *                                            long as there is a change to this.
     */
    public void fetchServiceDeploymentStatusWithPolling(
            DeferredResult<DeploymentStatusUpdate> deferredResult, UUID serviceId,
            ServiceDeploymentState previousKnownServiceDeploymentState) {
        AtomicReference<DeploymentStatusUpdate> ref = new AtomicReference<>(
                new DeploymentStatusUpdate(previousKnownServiceDeploymentState,
                        FINAL_SERVICE_DEPLOYMENT_STATES.contains(
                                previousKnownServiceDeploymentState)));
        try {
            Awaitility.await()
                    .atMost(pollingWaitPeriod, TimeUnit.SECONDS)
                    .pollDelay(0, TimeUnit.SECONDS) // first check runs without wait.
                    .pollInterval(pollingInterval, TimeUnit.SECONDS)
                    .until(() -> {
                        DeployServiceEntity deployServiceEntity =
                                deployServiceStorage.findDeployServiceById(serviceId);
                        if (Objects.isNull(deployServiceEntity)) {
                            throw new ServiceNotDeployedException(
                                    "Service with id " + serviceId + " not found");
                        }
                        ref.set(new DeploymentStatusUpdate(
                                deployServiceEntity.getServiceDeploymentState(),
                                FINAL_SERVICE_DEPLOYMENT_STATES.contains(
                                        deployServiceEntity.getServiceDeploymentState())));
                        return Objects.isNull(previousKnownServiceDeploymentState)
                                || FINAL_SERVICE_DEPLOYMENT_STATES.contains(
                                deployServiceEntity.getServiceDeploymentState())
                                || deployServiceEntity.getServiceDeploymentState()
                                != previousKnownServiceDeploymentState;
                    });
        } catch (ConditionTimeoutException conditionTimeoutException) {
            log.info("No change to service deployment status yet.");
        } catch (Exception exception) {
            deferredResult.setErrorResult(exception);
        }
        deferredResult.setResult(ref.get());
    }

}
