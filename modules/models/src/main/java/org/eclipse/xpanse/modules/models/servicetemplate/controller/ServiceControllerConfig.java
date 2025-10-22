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
import org.eclipse.xpanse.modules.models.servicetemplate.MappableFields;

/**
 * Contains global configuration for the controller's OpenAPI file generation. The configuration in
 * this object will be used by all controller methods.
 */
@Data
public class ServiceControllerConfig {

    @Schema(
            description =
                    "This the base URI of all the service methods. This is used only when the"
                            + " service is offered using it's own service controller.")
    private String baseUri;

    @Schema(description = "rename ontology in the APIs to custom names.")
    @Null
    private Map<MappableFields, String> standardNameMappings;

    @NotNull
    @Schema(description = "Mode/Constellation in which service controller is deployed.")
    private ServiceControllerMode serviceControllerMode;

    private Map<String, String> errorCodeMappings;
}
