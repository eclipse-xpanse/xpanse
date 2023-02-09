/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.ocl.loader.data.models.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;


/**
 * Period for Billing.
 */
public enum BillingPeriod {
    DAILY("daily"),
    WEEKLY("weekly"),
    MONTHLY("monthly"),
    QUARTERLY("quarterly"),
    YEARLY("yearly");

    private final String period;

    BillingPeriod(String period) {
        this.period = period;
    }

    /**
     * For BillingPeriod serialize.
     */
    @JsonCreator
    public BillingPeriod getByValue(String period) {
        for (BillingPeriod billingPeriod : values()) {
            if (billingPeriod.period.equals(StringUtils.lowerCase(period))) {
                return billingPeriod;
            }
        }
        return null;
    }

    /**
     * For BillingPeriod deserialize.
     */
    @JsonValue
    public String toValue() {
        return this.period;
    }
}