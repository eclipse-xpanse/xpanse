/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.api.config.AuditApiRequest;
import org.eclipse.xpanse.modules.deployment.ServiceConfigurationManager;
import org.eclipse.xpanse.modules.models.serviceconfiguration.ServiceConfigurationChangeRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Agent Polling Api.
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/xpanse")
public class AgentPollingApi {

    @Resource
    private ServiceConfigurationManager serviceConfigurationManager;

    @Tag(name = "Agent Api",
            description = "APIs for agent to poll pending configuration change requests.")
    @GetMapping(value = "/agent/poll/{serviceId}/{resourceName}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(description = "Get pending configuration change request for agents to poll.")
    @AuditApiRequest(enabled = false)
    public ResponseEntity<ServiceConfigurationChangeRequest> getPendingConfigurationChangeRequest(
            @Parameter(name = "serviceId",
                    description = "The id of the deployed service")
            @PathVariable("serviceId") String serviceId,
            @Parameter(name = "resourceName",
                    description = "The name of the resource of deployed service")
            @PathVariable("resourceName") String resourceName) {
        return serviceConfigurationManager
                .getPendingConfigurationChangeRequest(serviceId, resourceName);
    }

}
