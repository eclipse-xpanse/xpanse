/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/** Defines for service ModificationImpact. */
@Data
public class ModificationImpact implements Serializable {

    @Serial private static final long serialVersionUID = -4160044806152791293L;

    @Schema(description = "Is data lost when service configuration is modified.")
    private Boolean isDataLost;

    @Schema(
            description =
                    "Is service availability interrupted when the configuration is "
                            + "interrupted.")
    private Boolean isServiceInterrupted;
}
