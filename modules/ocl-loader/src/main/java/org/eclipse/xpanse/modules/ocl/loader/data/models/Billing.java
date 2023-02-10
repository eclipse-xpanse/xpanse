/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.ocl.loader.data.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jdk.jfr.Description;
import lombok.Data;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.BillingCurrency;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.BillingPeriod;

/**
 * Defines the billing model of the managed service.
 */
@Data
public class Billing {

    @NotNull
    @NotBlank
    @NotEmpty
    @Description("The business model of the managed service")
    private String model;

    @NotNull
    @Description("The rental period of the managed service")
    private BillingPeriod period;

    @NotNull
    @Description("The billing currency of the managed service, valid values: euro,uso")
    private BillingCurrency currency;

    @NotNull
    @Description("The fixed price during the period for the managed service")
    private Double fixedPrice;

    @NotNull
    @Description("The price depending of item volume for the managed service")
    private Double variablePrice;

}
