/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;

/** Variable data types. */
public enum VariableDataType {
    STRING("string"),
    NUMBER("number"),
    BOOLEAN("boolean");

    private final String type;

    VariableDataType(String type) {
        this.type = type;
    }

    /** For VariableKind serialize. */
    @JsonCreator
    public VariableDataType getByValue(String type) {
        for (VariableDataType variableDataType : values()) {
            if (StringUtils.equalsIgnoreCase(variableDataType.type, type)) {
                return variableDataType;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("VariableDataType value %s is not supported.", type));
    }

    /** For VariableKind deserialize. */
    @JsonValue
    public String toValue() {
        return this.type;
    }
}
