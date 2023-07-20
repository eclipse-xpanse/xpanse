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

/**
 * Describes health status of the system.
 */
@Data
public class SystemStatus {

    @NotNull
    @Schema(description = "The health status of Xpanse api service.")
    private HealthStatus healthStatus;

    @Schema(description = "The health status of backend systems. "
            + "This list contains status of identity provider and status of database."
            + "The status of identity provider will return when authorization is enabled.")
    @NotNull
    private List<BackendSystemStatus> backendSystemStatuses;
}