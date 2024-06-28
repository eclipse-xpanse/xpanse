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
 * Defines for the service config parameter.
 */
@Data
public class ServiceConfigurationParameter implements Serializable {

    @Serial
    private static final long serialVersionUID = 4180720936204332216L;

    @NotNull
    @NotBlank
    @Schema(description = "The name of the service config parameter")
    private String name;

    @NotNull
    @Schema(description = "The kind of the service config parameter")
    private DeployVariableKind kind;

    @NotNull
    @Schema(description = "The type of the service config parameter")
    private DeployVariableDataType dataType;

    @Schema(description = "The example value of the service config parameter")
    private String example;

    @NotNull
    @NotBlank
    @Schema(description = "The description of the service config parameter")
    private String description;

    @Schema(description = "The value of the service config parameter. "
            + "Value can be provided for initial value")
    private String value;

    @NotNull
    @Schema(description = "The init value of the service config parameter")
    private String initialValue;

    @NotNull
    @Schema(description = "Indicates if the service config parameter is mandatory")
    private Boolean mandatory;

    @Schema(description = "valueSchema of the service config parameter. "
            + "The key be any keyword that is part of the JSON schema definition which can be "
            + "found here https://json-schema.org/draft/2020-12/schema")
    private Map<String, Object> valueSchema;

    @Schema(description = "Sensitive scope of service config parameter storage")
    private SensitiveScope sensitiveScope = SensitiveScope.NONE;

    @Schema(description = "Service config parameter autofill")
    private AutoFill autoFill;

    @NotNull
    @Schema(description = "Service config parameter modificationImpact")
    private ModificationImpact modificationImpact;

}
