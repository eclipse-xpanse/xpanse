/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.resource;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.eclipse.xpanse.modules.models.enums.DeployVariableKind;

/**
 * Defines for the deploy variable.
 */
@Data
public class DeployVariable {

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "The name of the deploy variable")
    private String name;

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "The description for the deploy variable")
    private String description;

    @NotNull
    @Schema(description = "The kind of the deploy variable")
    private DeployVariableKind kind;

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "The type of the deploy variable")
    private String type;

    @Schema(description = "The value of the deploy variable")
    private String value;

    @NotNull
    @Schema(description = "Indicate the variable if is mandatory")
    private Boolean mandatory;

    @Schema(description = "Validator of the variable")
    private String validator;
}
