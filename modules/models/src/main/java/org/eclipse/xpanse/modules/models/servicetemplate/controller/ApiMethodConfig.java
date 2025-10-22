/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import org.eclipse.xpanse.modules.models.servicetemplate.methods.MethodParameter;

/**
 * This base data type is used to describe how functions from OCL must land in the OpenAPI schema of
 * the service controller.
 */
@Data
public class ApiMethodConfig {

    @NotNull
    @Schema(description = "service name to be displayed in the service manager API")
    private String methodName;

    @NotNull
    @Schema(
            description =
                    "service URI to be used to call the specific action of the service. This must"
                            + " ensure that the URI must contain the standard parameters for every"
                            + " action type.Its not possible to generate it fully. ",
            examples = {"/upgrade/service/"})
    private String serviceUri;

    @NotNull
    @Schema(description = "service name to be displayed in the service manager API docs.")
    private String methodDescription;

    @NotNull
    @Schema(
            description =
                    "Adds the method to a specific group in OpenAPI. Maps to operation in OpenAPI"
                            + " schema.",
            examples = "manage databases")
    private String serviceGroupName;

    @NotEmpty
    @Schema(description = "describe the parameters required as part of the URI")
    private List<MethodParameter> methodParameters;

    @Schema(
            description =
                    "defines the name to be used to map objectId within the xpanse runtime.When the"
                        + " controller is generated, the controller methods will have to do the"
                        + " translation to the correct name that xpanse understands.This is not for"
                        + " the object name but for the ID. The object name is already there in the"
                        + " object model.")
    // is it needed only for objects or for everything else?
    private String customIdName;
}
