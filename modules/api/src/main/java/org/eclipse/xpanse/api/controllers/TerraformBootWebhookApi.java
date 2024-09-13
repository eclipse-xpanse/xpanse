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
import org.eclipse.xpanse.modules.deployment.deployers.terraform.callbacks.TerraformDeploymentResultCallbackManager;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeploymentScenario;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
 * Webhook class for terraform-boot. These API methods are not exposed to the end user.
 * They are only for machine-to-machine communication.
 * This class implements the callback methods described in the terraform-boot specifications.
 */
@Slf4j
@RestController
@Profile("terraform-boot")
@CrossOrigin
@ConditionalOnProperty(name = "enable.agent.api.only", havingValue = "false", matchIfMissing = true)
public class TerraformBootWebhookApi {

    @Resource
    private TerraformDeploymentResultCallbackManager terraformDeploymentResultCallbackManager;

    /**
     * Webhook methods to receive terraform execution result.
     */
    @Tag(name = "Webhook", description = "Webhook APIs")
    @Operation(description = "Process the execution result after terraform executes the command "
            + "line.")
    @PostMapping(value = "${webhook.terraform-boot.deployCallbackUri}/{serviceId}", produces =
            MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(enabled = false)
    public void deployCallback(
            @Parameter(name = "serviceId", description = "id of the service instance")
            @PathVariable("serviceId") String serviceId,
            @Valid @RequestBody TerraformResult result) {

        terraformDeploymentResultCallbackManager.deployCallback(UUID.fromString(serviceId), result);
    }

    /**
     * Webhook methods to receive terraform execution result.
     */
    @Tag(name = "Webhook", description = "Webhook APIs")
    @Operation(description = "Process the execution result after terraform executes the command "
            + "line.")
    @PostMapping(value = "${webhook.terraform-boot.modifyCallbackUri}/{serviceId}", produces =
            MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(enabled = false)
    public void modifyCallback(
            @Parameter(name = "serviceId", description = "id of the service instance")
            @PathVariable("serviceId") String serviceId,
            @Valid @RequestBody TerraformResult result) {

        terraformDeploymentResultCallbackManager.modifyCallback(UUID.fromString(serviceId), result);
    }

    /**
     * Webhook methods to receive terraform execution result.
     */
    @Tag(name = "Webhook", description = "Webhook APIs")
    @Operation(description = "Process the execution result after terraform executes the command "
            + "line to destroy service.")
    @PostMapping(value = "${webhook.terraform-boot.destroyCallbackUri}/{serviceId}", produces =
            MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(enabled = false)
    public void destroyCallback(
            @Parameter(name = "serviceId", description = "id of the service instance")
            @PathVariable("serviceId") String serviceId,
            @Valid @RequestBody TerraformResult result) {

        terraformDeploymentResultCallbackManager.destroyCallback(UUID.fromString(serviceId), result,
                DeploymentScenario.DESTROY);
    }


    /**
     * Webhook methods to receive terraform execution result.
     */
    @Tag(name = "Webhook", description = "Webhook APIs")
    @Operation(description = "Process the execution result after terraform executes the command "
            + "line to rollback service deployment.")
    @PostMapping(value = "${webhook.terraform-boot.rollbackCallbackUri}/{serviceId}", produces =
            MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(enabled = false)
    public void rollbackCallback(
            @Parameter(name = "serviceId", description = "id of the service instance")
            @PathVariable("serviceId") String serviceId,
            @Valid @RequestBody TerraformResult result) {
        terraformDeploymentResultCallbackManager.destroyCallback(UUID.fromString(serviceId), result,
                DeploymentScenario.ROLLBACK);
    }


    /**
     * Webhook methods to receive terraform execution result.
     */
    @Tag(name = "Webhook", description = "Webhook APIs")
    @Operation(description = "Process the execution result after terraform executes the command "
            + "line to purge service.")
    @PostMapping(value = "${webhook.terraform-boot.purgeCallbackUri}/{serviceId}", produces =
            MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(enabled = false)
    public void purgeCallback(
            @Parameter(name = "serviceId", description = "id of the service instance")
            @PathVariable("serviceId") String serviceId,
            @Valid @RequestBody TerraformResult result) {
        terraformDeploymentResultCallbackManager.destroyCallback(UUID.fromString(serviceId), result,
                DeploymentScenario.PURGE);
    }

}
