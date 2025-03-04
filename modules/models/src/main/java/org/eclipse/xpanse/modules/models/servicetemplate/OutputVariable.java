/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.SensitiveScope;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.VariableDataType;

/** Defines for the output variable. */
@Data
public class OutputVariable implements Serializable {

    @Serial private static final long serialVersionUID = 4180720936204332115L;

    @NotNull
    @NotBlank
    @Schema(description = "The name of the output variable")
    private String name;

    @NotNull
    @Schema(description = "The type of the output variable")
    private VariableDataType dataType;

    @Schema(description = "The description of the output variable")
    private String description;

    @Schema(description = "Sensitive scope of variable storage")
    private SensitiveScope sensitiveScope = SensitiveScope.NONE;
}
