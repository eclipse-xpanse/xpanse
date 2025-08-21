/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** Defines for service ModificationImpact. */
@Data
public class ModificationImpact {

    @Schema(description = "Is data lost when service configuration is modified.")
    private Boolean isDataLost;

    @Schema(
            description =
                    "Is service availability interrupted when the configuration is "
                            + "interrupted.")
    private Boolean isServiceInterrupted;
}
