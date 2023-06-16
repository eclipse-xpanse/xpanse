/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.register;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.eclipse.xpanse.modules.models.service.register.enums.DeployVariableDataType;
import org.eclipse.xpanse.modules.models.service.register.enums.DeployVariableKind;
import org.eclipse.xpanse.modules.models.service.register.enums.SensitiveScope;

/**
 * Defines for the deploy variable.
 */
@Data
public class DeployVariable {

    @NotNull
    @NotBlank
    @Schema(description = "The name of the deploy variable")
    private String name;

    @NotNull
    @Schema(description = "The kind of the deploy variable")
    private DeployVariableKind kind;

    @NotNull
    @Schema(description = "The type of the deploy variable")
    private DeployVariableDataType dataType;

    @Schema(description = "The default value of the deploy variable")
    private String example;

    @NotNull
    @NotBlank
    @Schema(description = "The description of the deploy variable")
    private String description;

    @Schema(description = "The value of the deploy variable. "
            + "Value can be provided for default variables")
    private String value;

    @NotNull
    @Schema(description = "Indicates if the variable is mandatory")
    private Boolean mandatory;

    @Schema(description = "Validator of the variable")
    private String validator;

    @Schema(description = "Sensitive scope of variable storage")
    private SensitiveScope sensitiveScope = SensitiveScope.NONE;
}
