/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.register.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;


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
            if (billingCurrency.currency.equals(StringUtils.lowerCase(currency))) {
                return billingCurrency;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("BillingCurrency value %s is not supported.", currency));
    }

    /**
     * For BillingCurrency deserialize.
     */
    @JsonValue
    public String toValue() {
        return this.currency;
    }
}