/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.system;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.eclipse.xpanse.modules.models.system.enums.HealthStatus;

/** Defines the health status of xpanse.. */
@Data
public class SystemStatus {

    @NotNull
    @Schema(description = "The health status of Xpanse api service.")
    private HealthStatus healthStatus;
}
