/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.billing.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;

/** Enums of Currency. */
public enum Currency {
    USD("USD"),
    EUR("EUR"),
    CNY("CNY");

    private final String value;

    Currency(String value) {
        this.value = value;
    }

    /** For Currency serialize. */
    @JsonCreator
    public static Currency getByValue(String value) {
        for (Currency enumeration : values()) {
            if (StringUtils.equalsIgnoreCase(enumeration.value, value)) {
                return enumeration;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("Currency value %s is not supported.", value));
    }

    /** For Currency deserialize. */
    @JsonValue
    public String toValue() {
        return this.value;
    }
}
