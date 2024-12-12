/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.system;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.eclipse.xpanse.modules.models.system.enums.BackendSystemType;
import org.eclipse.xpanse.modules.models.system.enums.HealthStatus;

/** Describes health status of the backend system. */
@Data
public class BackendSystemStatus {

    @NotNull
    @Schema(description = "The type of backend system.")
    private BackendSystemType backendSystemType;

    @NotNull
    @Schema(description = "The name of backend system.")
    private String name;

    @NotNull
    @Schema(description = "The health status of backend system.")
    private HealthStatus healthStatus;

    @Schema(
            description =
                    "The endpoint of backend system. This filed is shown when the user have role"
                            + " 'admin' otherwise it is null.")
    private String endpoint;

    @Schema(
            description =
                    "The details why health is not ok.This filed is shown when the user have role"
                            + " 'admin' otherwise it is null.")
    private String details;
}
