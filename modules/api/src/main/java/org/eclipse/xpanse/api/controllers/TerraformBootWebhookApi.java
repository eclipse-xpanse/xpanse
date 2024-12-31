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
 * Webhook class for terraform-boot. These API methods are not exposed to the end user. They are
 * only for machine-to-machine communication. This class implements the callback methods described
 * in the terraform-boot specifications.
 */
@Slf4j
@RestController
@Profile("terraform-boot")
@CrossOrigin
@ConditionalOnProperty(name = "enable.agent.api.only", havingValue = "false", matchIfMissing = true)
public class TerraformBootWebhookApi {

    @Resource
    private TerraformDeploymentResultCallbackManager terraformDeploymentResultCallbackManager;

    /** Webhook method to receive the execution result of the order task from terraform-boot. */
    @Tag(name = "Webhook", description = "Webhook APIs")
    @Operation(description = "Process the execution result of the order task from terraform-boot.")
    @PostMapping(
            value = "${webhook.terraform-boot.orderCallbackUri}/{orderId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(methodName = "getCspFromServiceOrderId", paramTypes = UUID.class)
    public void orderCallback(
            @Parameter(name = "orderId", description = "Id of the order.") @PathVariable("orderId")
                    UUID orderId,
            @Valid @RequestBody TerraformResult result) {
        terraformDeploymentResultCallbackManager.orderCallback(orderId, result);
    }
}
