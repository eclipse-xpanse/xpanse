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
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.callbacks.OpenTofuDeploymentResultCallbackManager;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuResult;
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
    @PostMapping(value = "${webhook.tofu-maker.deployCallbackUri}/{task_id}", produces =
            MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void deployCallback(
            @Parameter(name = "task_id", description = "task id")
            @PathVariable("task_id") String taskId,
            @Valid @RequestBody OpenTofuResult result) {

        openTofuDeploymentResultCallbackManager.deployCallback(UUID.fromString(taskId), result);
    }

    /**
     * Webhook methods to receive openTofu execution result.
     */
    @Tag(name = "Webhook", description = "Webhook APIs")
    @Operation(description = "Process the execution result after openTofu executes the command "
            + "line.")
    @PostMapping(value = "${webhook.tofu-maker.destroyCallbackUri}/{task_id}", produces =
            MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void destroyCallback(
            @Parameter(name = "task_id", description = "task id")
            @PathVariable("task_id") String taskId,
            @Valid @RequestBody OpenTofuResult result) {

        openTofuDeploymentResultCallbackManager.destroyCallback(UUID.fromString(taskId), result);
    }

}
