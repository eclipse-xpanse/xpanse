/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.system;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import org.eclipse.xpanse.modules.models.system.enums.HealthStatus;

/** Describes health status of the system (only for ADMIN users). */
@Data
public class StackStatus {

    @NotNull
    @Schema(description = "The health status of Xpanse service.")
    private HealthStatus healthStatus;

    @Schema(
            description =
                    "The health status of the entire xpanse stack. This contains all components"
                            + " that are connected to xpanse.")
    @NotNull
    private List<BackendSystemStatus> backendSystemStatuses;
}
