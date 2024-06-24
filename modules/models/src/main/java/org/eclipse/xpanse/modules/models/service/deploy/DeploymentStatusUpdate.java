/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.deploy;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;

/**
 * Data type to describe an order status update.
 * Will only hold the changes related to deployment status.
 */
@Data
@AllArgsConstructor
public class DeploymentStatusUpdate {

    @NotNull
    @Schema(description = "Current state of the deployment request.")
    private ServiceDeploymentState serviceDeploymentState;

    @NotNull
    @Schema(description = "Describes if the deployment request is now completed")
    private Boolean isOrderCompleted;
}
