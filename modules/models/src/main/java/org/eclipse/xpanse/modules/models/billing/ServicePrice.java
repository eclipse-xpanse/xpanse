/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.billing;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Defines the price of the managed service.
 */
@Data
public class ServicePrice {

    @Schema(description = "The recurring price")
    private Price recurringPrice;

    @Schema(description = "The one time payment price")
    private Price oneTimePaymentPrice;
}
