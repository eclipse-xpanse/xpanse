/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.controllers;

import static org.eclipse.xpanse.modules.security.auth.common.RoleConstants.ROLE_ADMIN;
import static org.eclipse.xpanse.modules.security.auth.common.RoleConstants.ROLE_USER;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.api.config.AuditApiRequest;
import org.eclipse.xpanse.modules.deployment.ServiceStateManager;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Service Management REST API. */
@Slf4j
@RestController
@RequestMapping("/xpanse")
@CrossOrigin
@Secured({ROLE_ADMIN, ROLE_USER})
@ConditionalOnProperty(
        name = "xpanse.agent-api.enable-agent-api-only",
        havingValue = "false",
        matchIfMissing = true)
public class ServiceStateManageApi {

    @Resource private ServiceStateManager serviceStateManager;

    /**
     * Start the deployed service by the service id.
     *
     * @param serviceId id of service.
     * @return id of the service state management task.
     */
    @Tag(
            name = "ServiceStatusManagement",
            description = "APIs to manage status of the service instances")
    @Operation(description = "Start a task to start the service instance.")
    @PutMapping(value = "/services/start/{serviceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @AuditApiRequest(methodName = "getCspFromServiceId", paramTypes = UUID.class)
    public ServiceOrder startService(@PathVariable("serviceId") UUID serviceId) {
        return serviceStateManager.startService(serviceId);
    }

    /**
     * Stop the deployed service by the service id.
     *
     * @param serviceId id of service.
     * @return id of the service state management task.
     */
    @Tag(
            name = "ServiceStatusManagement",
            description = "APIs to manage status of the service instances")
    @Operation(description = "Start a task to stop the service instance.")
    @PutMapping(value = "/services/stop/{serviceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @AuditApiRequest(methodName = "getCspFromServiceId", paramTypes = UUID.class)
    public ServiceOrder stopService(@PathVariable("serviceId") UUID serviceId) {
        return serviceStateManager.stopService(serviceId);
    }

    /**
     * Restart the deployed service by the service id.
     *
     * @param serviceId id of service.
     * @return id of the service state management task.
     */
    @Tag(
            name = "ServiceStatusManagement",
            description = "APIs to manage status of the service instances")
    @Operation(description = "Start a task to restart the service instance.")
    @PutMapping(
            value = "/services/restart/{serviceId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @AuditApiRequest(methodName = "getCspFromServiceId", paramTypes = UUID.class)
    public ServiceOrder restartService(@PathVariable("serviceId") UUID serviceId) {
        return serviceStateManager.restartService(serviceId);
    }
}
