/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.deployment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Data;

/** The result of the deployment. */
@Data
public class DeployResult implements Serializable {

    @Serial private static final long serialVersionUID = 2268625554476607215L;

    @NotNull
    @Schema(description = "The id of the service order task.")
    private UUID orderId;

    @NotNull
    @Schema(description = "True if the deployer task is successful.")
    private Boolean isTaskSuccessful;

    @Schema(description = "The message of the service order task.")
    private String message;

    @NotNull
    @Schema(description = "The deployed resources of the service instance.")
    private List<@Valid DeployResource> resources;

    @NotNull
    @Schema(description = "The output properties of the service instance.")
    private Map<String, String> outputProperties = new HashMap<>();

    @NotNull
    @Schema(description = "The deployment generated files of the service instance deployment.")
    private Map<String, String> deploymentGeneratedFiles = new HashMap<>();

    @Schema(description = "The version of deployer tool used to execute deployment scripts.")
    private String deployerVersionUsed;

    @Hidden @JsonIgnore private String tfStateContent;
}
