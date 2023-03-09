/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.resource;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.eclipse.xpanse.modules.models.enums.BillingCurrency;
import org.eclipse.xpanse.modules.models.enums.BillingPeriod;

/**
 * Defines the billing model of the managed service.
 */
@Data
public class Billing {

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "The business model of the managed service")
    private String model;

    @NotNull
    @Schema(description = "The rental period of the managed service")
    private BillingPeriod period;

    @NotNull
    @Schema(description = "The billing currency of the managed service, valid values: euro,uso")
    private BillingCurrency currency;

}
