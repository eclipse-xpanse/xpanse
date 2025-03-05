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
import org.eclipse.xpanse.modules.models.servicetemplate.enums.SensitiveScope;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.VariableDataType;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.VariableKind;

/** Defines for the service config parameter. */
@Data
public class ServiceChangeParameter implements Serializable {

    @Serial private static final long serialVersionUID = 4180720936204332216L;

    @NotNull
    @NotBlank
    @Schema(description = "The name of the service config parameter")
    private String name;

    @NotNull
    @Schema(description = "The kind of the service config parameter")
    private VariableKind kind;

    @NotNull
    @Schema(description = "The type of the service config parameter")
    private VariableDataType dataType;

    @Schema(description = "The example value of the service config parameter")
    private String example;

    @NotNull
    @NotBlank
    @Schema(description = "The description of the service config parameter")
    private String description;

    @Schema(
            description =
                    "The value of the service config parameter. "
                            + "Value can be provided for initial value")
    private String value;

    @NotNull
    @Schema(description = "The init value of the service config parameter")
    private Object initialValue;

    @Schema(
            description =
                    "valueSchema of the variable. The key can be any keyword that is part of the"
                        + " JSON schema definition which can be found here"
                        + " https://json-schema.org/draft/2020-12/meta/validation. Only the type"
                        + " field is taken from dataType parameter directly.",
            additionalProperties = Schema.AdditionalPropertiesValue.TRUE)
    private Map<String, Object> valueSchema;

    @Schema(description = "Sensitive scope of service config parameter storage")
    private SensitiveScope sensitiveScope = SensitiveScope.NONE;

    @NotNull
    @Schema(description = "Service config parameter modificationImpact")
    private ModificationImpact modificationImpact;

    @NotNull
    @Schema(description = "Whether the service configuration parameters are read-only")
    private Boolean isReadOnly;

    @NotNull
    @NotBlank
    @Schema(description = "Service component which manages this configuration parameter.")
    private String managedBy;
}
