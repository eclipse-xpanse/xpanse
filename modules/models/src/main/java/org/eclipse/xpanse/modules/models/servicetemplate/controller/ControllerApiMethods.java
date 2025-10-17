/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

/** Wrapper type to store both write and read method configurations of a specific order type. */
@Data
public class ControllerApiMethods {

    @NotNull
    @Schema(
            description =
                    "API method configuration for all object related methods in the service"
                            + " controller API layer.Only object related methods such as create,"
                            + " delete, update.")
    private List<ApiWriteMethodConfig> apiWriteMethodConfigs;

    @NotNull
    @Schema(
            description =
                    "Read API method configuration for all object related methods in the service"
                            + " controller API layer.Only object related info such as GET with"
                            + " different queries and filters.")
    private List<ApiReadMethodConfig> apiReadMethodConfigs;
}
