/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.UUID;
import org.eclipse.xpanse.api.config.AuditApiRequest;
import org.eclipse.xpanse.modules.deployment.ServiceChangeDetailsManager;
import org.eclipse.xpanse.modules.models.serviceconfiguration.ServiceChangeOrderDetails;
import org.eclipse.xpanse.modules.models.serviceconfiguration.enums.ServiceChangeStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

/** service change details Api. */
public class ServiceChangeDetailsApi {

    @Resource private ServiceChangeDetailsManager serviceChangeDetailsManager;

    /** List all service change request. */
    @Tag(name = "Service Change Details", description = "APIs for Service Change Details.")
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
        return serviceChangeDetailsManager.getServiceChangeRequestDetails(
                orderId, serviceId, resourceName, configManager, status);
    }
}
