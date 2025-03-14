/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;

/** Defines for the Linked Object Type. */
public enum LinkedObjectType {
    DATABASE("database");

    private final String type;

    LinkedObjectType(String type) {
        this.type = type;
    }

    /** For LinkedObjectType serialize. */
    @JsonCreator
    public static LinkedObjectType getByValue(String type) {
        for (LinkedObjectType kind : values()) {
            if (kind.toValue().equals(StringUtils.lowerCase(type))) {
                return kind;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("LinkedObjectType value %s is not supported.", type));
    }

    /** For LinkedObjectType deserialize. */
    @JsonValue
    public String toValue() {
        return this.type;
    }
}
