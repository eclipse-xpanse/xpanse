/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.resource;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import org.eclipse.xpanse.modules.models.enums.DeployerKind;

/**
 * Defines the Deployment.
 */
@Data
public class Deployment {

    @NotNull
    @Schema(description = "The type of the Deployment, valid values: terraform...")
    private DeployerKind kind;

    @Valid
    @NotNull
    @Schema(description = "The variables for the deployment, which will be passed to the deployer")
    private List<DeployVariable> context;

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "The real deployer, something like terraform scripts...")
    private String deployer;

}
