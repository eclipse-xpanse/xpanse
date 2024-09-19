/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.servicetemplate.validators.DeployerVersionConstraint;
import org.springframework.validation.annotation.Validated;

/**
 * Defines the Deployer Tool.
 */
@Data
@Validated
public class DeployerTool {

    @NotNull
    @Schema(description = "The type of the deployer which will handle the service deployment.")
    private DeployerKind kind;

    @NotNull
    @NotBlank
    @DeployerVersionConstraint
    @Schema(description = "The version of the deployer which will handle the service deployment.")
    private String version;
}
