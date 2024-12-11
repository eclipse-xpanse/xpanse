/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.billing.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;

/** Enum of Billing Mode. */
public enum BillingMode {
    FIXED("Fixed"),
    PAY_PER_USE("Pay per Use");

    private final String value;

    BillingMode(String value) {
        this.value = value;
    }

    /** For BillingMode serialize. */
    @JsonCreator
    public static BillingMode getByValue(String value) {
        for (BillingMode enumeration : values()) {
            if (StringUtils.equalsIgnoreCase(enumeration.value, value)) {
                return enumeration;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("BillingMode value %s is not supported.", value));
    }

    /** For BillingMode deserialize. */
    @JsonValue
    public String toValue() {
        return this.value;
    }
}
