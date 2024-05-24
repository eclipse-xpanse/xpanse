/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.billing;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;
import org.eclipse.xpanse.modules.models.billing.enums.Currency;
import org.eclipse.xpanse.modules.models.billing.enums.PricingPeriod;

/**
 * Defines the price data model.
 */
@Data
public class Price implements Serializable {

    @Serial
    private static final long serialVersionUID = 240913796673011260L;

    @NotNull
    @Schema(description = "The value of the cost.")
    private BigDecimal cost;

    @NotNull
    @Schema(description = "The currency of the cost.")
    private Currency currency;

    @Schema(description = "The period of the cost.")
    private PricingPeriod period;
}
