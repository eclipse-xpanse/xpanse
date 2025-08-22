/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.billing;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

/** Defines the rating mode of the managed service. */
@Data
public class RatingMode {

    @Schema(
            description =
                    "The fixed prices of the flavor in the managed service for regions. The fixed"
                            + " price of the region includes all prices and shown to the customer.")
    private List<PriceWithRegion> fixedPrices;

    @Schema(description = "The resource usage of the flavor in the managed service.")
    private ResourceUsage resourceUsage;

    @NotNull
    @Schema(
            description =
                    "Whether the price is only for management layer. Consumption of the "
                            + "workload resources will be billed additionally..")
    private Boolean isPriceOnlyForManagementLayer;
}
