/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.modules.models.enums.TerraformExecState;

/**
 * The result of the deployment.
 */
@Data
public class DeployResult {

    /**
     * The id of the XpanseDeployTask.
     */
    @NotNull
    @Schema(description = "The id of the service task.")
    private UUID id;
    /**
     * The state of the XpanseDeployTask.
     */
    @NotNull
    @Schema(description = "The state of the service.")
    private TerraformExecState state;

    /**
     * The resources of the server.
     */
    @NotNull
    @Schema(description = "The deployed resources of the service.")
    private List<DeployResource> resources;

    /**
     * The result property of the service task.
     */
    @NotNull
    @Schema(description = "The result property of the service task.")
    private Map<String, String> properties = new HashMap<>();

    @NotNull
    @Schema(description = "The properties of the deployed result.")
    private Map<String, String> privateProperties = new HashMap<>();

}
