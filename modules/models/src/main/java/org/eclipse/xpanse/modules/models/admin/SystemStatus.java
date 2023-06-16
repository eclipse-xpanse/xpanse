/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.eclipse.xpanse.modules.models.admin.enums.HealthStatus;

/**
 * Describes health status of the system.
 */
@Data
public class SystemStatus {

    @NotNull
    HealthStatus healthStatus;
}