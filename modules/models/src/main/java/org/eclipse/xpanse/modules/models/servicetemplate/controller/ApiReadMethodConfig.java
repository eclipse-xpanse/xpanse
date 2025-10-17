/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.modules.models.servicetemplate.MappableFields;

/**
 * Data model to describe the read methods that must be added to the service controllers OpenAPI
 * schema.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ApiReadMethodConfig extends ApiMethodConfig {

    @Schema(description = "rename ontology in the APIs to custom names.")
    @Null
    private Map<MappableFields, String> standardNameMappings;

    @NotNull
    @Schema(
            description =
                    "response body structure of the read method. This is mandatory since every read"
                            + " method will have a response body.")
    private CustomBody responseBody;

    @Schema(
            description =
                    "Describes if the response is a list/array. When true, the service returns the"
                            + " lsit of the response body defined in the responseBody.")
    @NotNull
    private Boolean isArray = false;
}
