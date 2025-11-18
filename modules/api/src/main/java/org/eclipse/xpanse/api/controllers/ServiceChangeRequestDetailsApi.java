/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.controllers;

import static org.eclipse.xpanse.modules.security.auth.common.RoleConstants.ROLE_ADMIN;
import static org.eclipse.xpanse.modules.security.auth.common.RoleConstants.ROLE_USER;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.api.config.AuditApiRequest;
import org.eclipse.xpanse.modules.deployment.ServiceChangeRequestsManager;
import org.eclipse.xpanse.modules.models.servicechange.ServiceChangeOrderDetails;
import org.eclipse.xpanse.modules.models.servicechange.enums.ServiceChangeStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** service change details Api. */
@Slf4j
@RestController
@RequestMapping("/xpanse")
@CrossOrigin
@Secured({ROLE_ADMIN, ROLE_USER})
@ConditionalOnProperty(
        name = "xpanse.agent-api.enable-agent-api-only",
        havingValue = "false",
        matchIfMissing = true)
public class ServiceChangeRequestDetailsApi {

    private final ServiceChangeRequestsManager serviceChangeRequestsManager;

    /** Constructor method. */
    @Autowired
    public ServiceChangeRequestDetailsApi(
            ServiceChangeRequestsManager serviceChangeRequestsManager) {
        this.serviceChangeRequestsManager = serviceChangeRequestsManager;
    }

    /** List all service change request. */
    @Tag(name = "ServiceChangeDetails", description = "APIs for Service Change Details.")
    @GetMapping(value = "/services/change/requests", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(description = "List service's change details Request.")
    @AuditApiRequest(methodName = "getCspFromServiceId", paramTypes = UUID.class)
    public List<ServiceChangeOrderDetails> getServiceChangeRequestDetails(
            @Parameter(name = "serviceId", description = "Id of the deployed service")
                    @RequestParam(name = "serviceId")
                    String serviceId,
            @Parameter(name = "orderId", description = "id of the service order")
                    @RequestParam(name = "orderId", required = false)
                    String orderId,
            @Parameter(name = "resourceName", description = "name of the service resource")
                    @RequestParam(name = "resourceName", required = false)
                    String resourceName,
            @Parameter(
                            name = "configManager",
                            description = "Manager of the service configuration parameter.")
                    @RequestParam(name = "configManager", required = false)
                    String configManager,
            @Parameter(name = "status", description = "Status of the service configuration")
                    @RequestParam(name = "status", required = false)
                    ServiceChangeStatus status) {
        return serviceChangeRequestsManager.getAllChangeRequests(
                orderId, serviceId, resourceName, configManager, status);
    }
}
