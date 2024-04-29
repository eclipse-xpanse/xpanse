/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.billing.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;


/**
 * Enum of PricingPeriod.
 */
public enum PricingPeriod {

    YEARLY("yearly"),
    MONTHLY("monthly"),
    DAILY("daily"),
    HOURLY("hourly"),
    ONE_TIME("oneTime");

    private final String value;

    PricingPeriod(String value) {
        this.value = value;
    }

    /**
     * For PricingPeriod serialize.
     */
    @JsonCreator
    public static PricingPeriod getByValue(String value) {
        for (PricingPeriod enumeration : values()) {
            if (StringUtils.equalsIgnoreCase(enumeration.value, value)) {
                return enumeration;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("PricingPeriod value %s is not supported.", value));
    }

    /**
     * For PricingPeriod deserialize.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }
}