/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.billing;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.eclipse.xpanse.modules.models.billing.enums.BillingMode;

/** Defines the price calculation result of the flavor. */
@Data
public class FlavorPriceResult {

    @NotNull
    @Schema(description = "The name of the flavor.")
    private String flavorName;

    @NotNull
    @Schema(description = "The billing mode of the price.")
    private BillingMode billingMode;

    @Schema(description = "The recurring price of the flavor.")
    private Price recurringPrice;

    @Schema(description = "The one time payment price of the flavor.")
    private Price oneTimePaymentPrice;

    @NotNull
    @Schema(description = "Is price calculation successful.")
    private boolean isSuccessful;

    @Schema(description = "Error reason when price calculation fails.")
    private String errorMessage;
}
