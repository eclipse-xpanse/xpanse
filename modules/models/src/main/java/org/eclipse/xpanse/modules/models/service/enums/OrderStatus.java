/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;

/** Enumeration class for service order status. */
public enum OrderStatus {
    CREATED("created"),
    IN_PROGRESS("in-progress"),
    SUCCESSFUL("successful"),
    FAILED("failed");

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    /** For OrderStatus deserialize. */
    @JsonCreator
    public static OrderStatus getByValue(String value) {
        for (OrderStatus entry : values()) {
            if (StringUtils.equalsIgnoreCase(entry.value, value)) {
                return entry;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("OrderStatus value %s is not supported.", value));
    }

    /** For OrderStatus serialize. */
    @JsonValue
    public String toValue() {
        return this.value;
    }
}
