/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.SensitiveScope;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.VariableDataType;

/** Defines for the service object parameter. */
@Valid
@Data
@Slf4j
public class ObjectParameter implements Serializable {

    @Serial private static final long serialVersionUID = 8759114725757852274L;

    @NotNull
    @NotBlank
    @Schema(description = "The name of the service object parameter")
    private String name;

    @NotNull
    @Schema(description = "The type of the service object  parameter")
    private VariableDataType dataType;

    @NotNull
    @NotBlank
    @Schema(description = "The description of the service object parameter")
    private String description;

    @Schema(description = "The example value of the service object  parameter")
    private Object example;

    @Schema(
            description =
                    "valueSchema of the service config parameter. The key be any keyword that is"
                            + " part of the JSON schema definition which can be found here"
                            + " https://json-schema.org/draft/2020-12/schema")
    private Map<String, Object> valueSchema;

    @Schema(description = "Sensitive scope of service config parameter storage")
    private SensitiveScope sensitiveScope = SensitiveScope.ALWAYS;

    @Schema(description = "idicates whether the object parameter is mandatory.")
    private Boolean isMandatory = true;

    @Schema(description = "the type of the linkedObject.")
    private String linkedObjectType;
}
