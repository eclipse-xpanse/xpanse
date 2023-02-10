/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.ocl.loader.data.models.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


/**
 * Currency for Billing.
 */
public enum BillingCurrency {
    USD("usd"),
    EUR("euro"),
    GBP("gbp"),
    CAD("cad"),
    DEM("dem"),
    FRF("frf"),
    CNY("cny");

    private final String currency;

    BillingCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * For BillingCurrency serialize.
     */
    @JsonCreator
    public BillingCurrency getByValue(String currency) {
        for (BillingCurrency billingCurrency : values()) {
            if (billingCurrency.currency.equals(currency)) {
                return billingCurrency;
            }
        }
        return null;
    }

    /**
     * For BillingCurrency deserialize.
     */
    @JsonValue
    public String toValue() {
        return this.currency;
    }
}