/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Define view object of the service order. */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Order details")
public class ServiceOrder {

    @NotNull
    @Schema(description = "The id of the service order.")
    private UUID orderId;

    @NotNull
    @Schema(description = "The id of the deployed service.")
    private UUID serviceId;
}
