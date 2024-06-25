/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import java.util.concurrent.Executor;
import org.eclipse.xpanse.api.config.AuditApiRequest;
import org.eclipse.xpanse.modules.async.TaskConfiguration;
import org.eclipse.xpanse.modules.deployment.polling.StatusChangePolling;
import org.eclipse.xpanse.modules.models.service.deploy.DeploymentStatusUpdate;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * REST API methods to fetch status of service state changes.
 */
@RestController
@RequestMapping("/xpanse")
public class ServiceStatusChangePollingApi {

    private final StatusChangePolling statusChangePolling;

    private final Executor taskExecutor;

    @Autowired
    public ServiceStatusChangePollingApi(StatusChangePolling statusChangePolling,
                                         @Qualifier(TaskConfiguration.ASYNC_EXECUTOR_NAME)
                                         Executor taskExecutor) {
        this.statusChangePolling = statusChangePolling;
        this.taskExecutor = taskExecutor;
    }

    /**
     *  Method to fetch status of service deployment.
     */
    @Tag(name = "Status Updates",
            description = "Long polling methods to fetch latest status updates")
    @GetMapping(value = "/service/deployment/status",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description =
            "Long-polling method to get the latest service "
                    + "deployment or service update status.")
    @AuditApiRequest(methodName = "getCspFromServiceId")
    @ResponseStatus(HttpStatus.OK)
    public DeferredResult<DeploymentStatusUpdate> getLatestServiceDeploymentStatus(
            @Parameter(name = "id", description = "ID of the service")
            @RequestParam(name = "id") String serviceId,
            @Parameter(name = "lastKnownServiceDeploymentState",
                    description = "Last known service status to client. When provided, "
                            + "the service will wait for a configured period time until "
                            + "to see if there is a change to the last known state.")
            @RequestParam(name = "lastKnownServiceDeploymentState", required = false)
            ServiceDeploymentState lastKnownServiceDeploymentState
    ) {
        DeferredResult<DeploymentStatusUpdate> stateDeferredResult = new DeferredResult<>();
        taskExecutor.execute(() -> {
            try {
                this.statusChangePolling.fetchServiceDeploymentStatusWithPolling(
                        stateDeferredResult,
                        UUID.fromString(serviceId),
                        lastKnownServiceDeploymentState);
            } catch (Exception exception) {
                stateDeferredResult.setErrorResult(exception);
            }
        });
        return stateDeferredResult;
    }

}
