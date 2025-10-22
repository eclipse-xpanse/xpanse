/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.eclipse.xpanse.modules.models.servicetemplate.controller.ControllerApiMethods;

/**
 * Data model to describe if the service allows the resource state to be controlled. For SaaS
 * services that run on shared infrastructure, this would be false.
 */
@Data
public class ResourceStateManage {

    @NotNull
    @Schema(
            description =
                    "defines if the service deployment includes resources that can be started,"
                        + " stopped or restarted.For services that uses shared infrastructure, this"
                        + " will be false.")
    private Boolean isResourceStateControllable;

    @Schema(
            description =
                    "controller methods for service state control. Can have one method for start,"
                            + " stop and restart and one or more methods to check status of each"
                            + " request.")
    private ControllerApiMethods controllerApiMethods;
}
