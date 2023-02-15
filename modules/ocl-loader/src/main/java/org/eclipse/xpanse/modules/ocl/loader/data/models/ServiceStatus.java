/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.ocl.loader.data.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.ServiceState;

/**
 * Defines status of a managed service.
 */
@Data
public class ServiceStatus {

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "The name of the service")
    String serviceName;
    @NotNull
    @Schema(description = "Current status of the service")
    ServiceState serviceState;
    @Schema(description = "Status message. Contains the reason in case the deployment has failed.")
    String statusMessage;
}
