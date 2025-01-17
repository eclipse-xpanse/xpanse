/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.api.config.AuditApiRequest;
import org.eclipse.xpanse.modules.deployment.ServiceActionManager;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.serviceaction.ServiceActionRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Service Actions Api. */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/xpanse")
public class ServiceActionsApi {

    @Resource private ServiceActionManager serviceActionManager;

    @Tag(name = "Service Actions", description = "APIs for Service Actions.")
    @PutMapping(value = "/services/action/{serviceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(description = "Create Service Actions.")
    @AuditApiRequest(methodName = "getCspFromServiceId", paramTypes = UUID.class)
    public ServiceOrder createServiceAction(
            @Parameter(name = "serviceId", description = "The id of the deployed service")
                    @PathVariable("serviceId")
                    UUID serviceId,
            @Valid @RequestBody ServiceActionRequest request) {

        return serviceActionManager.createServiceAction(serviceId, request);
    }
}
