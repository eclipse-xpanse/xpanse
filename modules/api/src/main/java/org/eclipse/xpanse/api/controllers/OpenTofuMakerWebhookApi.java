/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
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
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.callbacks.OpenTofuDeploymentResultCallbackManager;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeploymentScenario;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Webhook class for tofu-maker. These API methods are not exposed to the end user.
 * They are only for machine-to-machine communication.
 * This class implements the callback methods described in the tofu-maker specifications.
 */
@Slf4j
@RestController
@Profile("tofu-maker")
@CrossOrigin
public class OpenTofuMakerWebhookApi {


    @Resource
    private OpenTofuDeploymentResultCallbackManager openTofuDeploymentResultCallbackManager;

    /**
     * Webhook methods to receive openTofu execution result.
     */
    @Tag(name = "Webhook", description = "Webhook APIs")
    @Operation(description = "Process the execution result after openTofu executes the command "
            + "line.")
    @PostMapping(value = "${webhook.tofu-maker.deployCallbackUri}/{serviceId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(enabled = false)
    public void deployCallback(
            @Parameter(name = "serviceId", description = "id of the service instance")
            @PathVariable("serviceId") String serviceId,
            @Valid @RequestBody OpenTofuResult result) {

        openTofuDeploymentResultCallbackManager.deployCallback(UUID.fromString(serviceId), result);
    }

    /**
     * Webhook methods to receive openTofu execution result.
     */
    @Tag(name = "Webhook", description = "Webhook APIs")
    @Operation(description = "Process the execution result after openTofu executes the command "
            + "line.")
    @PostMapping(value = "${webhook.tofu-maker.modifyCallbackUri}/{serviceId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(enabled = false)
    public void modifyCallback(
            @Parameter(name = "serviceId", description = "id of the service instance")
            @PathVariable("serviceId") String serviceId,
            @Valid @RequestBody OpenTofuResult result) {
        openTofuDeploymentResultCallbackManager.modifyCallback(UUID.fromString(serviceId), result);
    }

    /**
     * Webhook methods to receive openTofu execution result.
     */
    @Tag(name = "Webhook", description = "Webhook APIs")
    @Operation(description = "Process the execution result after openTofu executes the command "
            + "line.")
    @PostMapping(value = "${webhook.tofu-maker.destroyCallbackUri}/{serviceId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(enabled = false)
    public void destroyCallback(
            @Parameter(name = "serviceId", description = "id of the service instance")
            @PathVariable("serviceId") String serviceId,
            @Valid @RequestBody OpenTofuResult result) {

        openTofuDeploymentResultCallbackManager.destroyCallback(UUID.fromString(serviceId), result,
                DeploymentScenario.DESTROY);
    }


    /**
     * Webhook methods to receive openTofu execution result.
     */
    @Tag(name = "Webhook", description = "Webhook APIs")
    @Operation(description = "Process the execution result after openTofu executes the command "
            + "line to rollback service deployment.")
    @PostMapping(value = "${webhook.tofu-maker.rollbackCallbackUri}/{serviceId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(enabled = false)
    public void rollbackCallback(
            @Parameter(name = "serviceId", description = "id of the service instance")
            @PathVariable("serviceId") String serviceId,
            @Valid @RequestBody OpenTofuResult result) {
        openTofuDeploymentResultCallbackManager.destroyCallback(UUID.fromString(serviceId), result,
                DeploymentScenario.ROLLBACK);
    }


    /**
     * Webhook methods to receive openTofu execution result.
     */
    @Tag(name = "Webhook", description = "Webhook APIs")
    @Operation(description = "Process the execution result after openTofu executes the command "
            + "line to purge service.")
    @PostMapping(value = "${webhook.tofu-maker.purgeCallbackUri}/{serviceId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(enabled = false)
    public void purgeCallback(
            @Parameter(name = "serviceId", description = "id of the service instance")
            @PathVariable("serviceId") String serviceId,
            @Valid @RequestBody OpenTofuResult result) {
        openTofuDeploymentResultCallbackManager.destroyCallback(UUID.fromString(serviceId), result,
                DeploymentScenario.PURGE);
    }

}
