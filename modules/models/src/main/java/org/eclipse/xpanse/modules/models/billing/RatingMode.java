/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.billing;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * Defines the rating mode of the managed service.
 */
@Data
public class RatingMode implements Serializable {

    @Serial
    private static final long serialVersionUID = 240913796673011260L;

    @Schema(description = "The fixed price of the flavor in the managed service. This price "
            + "includes all prices and this is the price shown to the customer..")
    private Price fixedPrice;

    @Schema(description = "The resource usage of the flavor in the managed service.")
    private ResourceUsage resourceUsage;

    @NotNull
    @Schema(description = "Whether the price is only for management layer. Consumption of the "
            + "workload resources will be billed additionally..")
    private Boolean isPriceOnlyForManagementLayer;
}
