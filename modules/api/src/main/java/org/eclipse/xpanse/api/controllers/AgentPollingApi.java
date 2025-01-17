/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.api.config.AuditApiRequest;
import org.eclipse.xpanse.modules.deployment.ServiceConfigurationManager;
import org.eclipse.xpanse.modules.models.serviceconfiguration.ServiceChangeRequest;
import org.eclipse.xpanse.modules.models.serviceconfiguration.ServiceConfigurationChangeResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Agent Polling Api. */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/xpanse")
public class AgentPollingApi {

    @Resource private ServiceConfigurationManager serviceConfigurationManager;

    /**
     * Query pending service change request for agent.
     *
     * @param serviceId the id of service.
     * @param resourceName the name of service`s resource.
     * @return ServiceChangeRequest.
     */
    @Tag(
            name = "Agent Api",
            description = "APIs for agent to poll pending service change requests.")
    @GetMapping(
            value = "/agent/poll/{serviceId}/{resourceName}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get pending service change request for agents to poll.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "204",
                description = "no pending service change update requests",
                content = @Content),
        @ApiResponse(
                responseCode = "200",
                description = "pending service change update request details",
                content = @Content(schema = @Schema(implementation = ServiceChangeRequest.class)))
    })
    @AuditApiRequest(methodName = "getCspFromServiceId", paramTypes = UUID.class)
    public ResponseEntity<ServiceChangeRequest> getPendingServiceChangeRequest(
            @Parameter(name = "serviceId", description = "The id of the deployed service")
                    @PathVariable("serviceId")
                    UUID serviceId,
            @Parameter(
                            name = "resourceName",
                            description = "The name of the resource of deployed service")
                    @PathVariable("resourceName")
                    String resourceName) {
        return serviceConfigurationManager.getPendingServiceChangeRequest(serviceId, resourceName);
    }

    /**
     * Method to update service change result.
     *
     * @param changeId id of the update request.
     * @param result result of the service change request.
     */
    @Tag(
            name = "Agent Api",
            description = "APIs for agent to poll pending service change requests.")
    @PutMapping(
            value = "/agent/update/status/{changeId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(description = "Update service change result for agents.")
    @AuditApiRequest(methodName = "getCspFromServiceChangeRequestId", paramTypes = UUID.class)
    public void updateServiceChangeResult(
            @Parameter(name = "changeId", description = "id of the update request.")
                    @PathVariable("changeId")
                    UUID changeId,
            @Parameter(name = "result", description = "result of the service change request.")
                    @RequestBody
                    ServiceConfigurationChangeResult result) {
        serviceConfigurationManager.updateServiceChangeResult(changeId, result);
    }
}
