/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;

/**
 * Data type to describe an order task status update.
 * Will only hold the changes related to task status.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceOrderStatusUpdate {

    @NotNull
    @Schema(description = "Current task status of the service order.")
    private TaskStatus taskStatus;

    @NotNull
    @Schema(description = "Describes if the service order is now completed.")
    private Boolean isOrderCompleted;

    @Schema(description = "The error message if the service order failed.")
    private ErrorResponse error;
}
