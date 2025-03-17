/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;

/** Defines the tool of service object manage. */
public enum ObjectActionType {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete");

    private final String type;

    ObjectActionType(String type) {
        this.type = type;
    }

    /** For ObjectActionType serialize. */
    @JsonCreator
    public static ObjectActionType getByValue(String type) {
        for (ObjectActionType kind : values()) {
            if (kind.toValue().equals(StringUtils.lowerCase(type))) {
                return kind;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("ObjectActionType value %s is not supported.", type));
    }

    /** For ObjectActionType deserialize. */
    @JsonValue
    public String toValue() {
        return this.type;
    }
}
