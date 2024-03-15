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
import java.util.Map;
import lombok.Data;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployVariableDataType;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployVariableKind;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.SensitiveScope;

/**
 * Defines for the deploy variable.
 */
@Data
public class DeployVariable implements Serializable {

    @Serial
    private static final long serialVersionUID = 4180720936204332115L;

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

    @Schema(description = "The example value of the deploy variable")
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

    @Schema(description = "valueSchema of the variable. "
            + "The key be any keyword that is part of the JSON schema definition which can be "
            + "found here https://json-schema.org/draft/2020-12/schema")
    private Map<String, Object> valueSchema;

    @Schema(description = "Sensitive scope of variable storage")
    private SensitiveScope sensitiveScope = SensitiveScope.NONE;

    @Schema(description = "Variable autofill")
    private AutoFill autoFill;

    @NotNull
    @Schema(description = "Variable modificationImpact")
    private ModificationImpact modificationImpact;
}
