/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.register.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * Deploy variable data types.
 */
public enum DeployVariableDataType {
    STRING("string"),
    NUMBER("number"),
    BOOLEAN("boolean");

    private final String type;

    DeployVariableDataType(String type) {
        this.type = type;
    }

    /**
     * For DeployVariableKind serialize.
     */
    @JsonCreator
    public DeployVariableDataType getByValue(String type) {
        for (DeployVariableDataType deployVariableType : values()) {
            if (StringUtils.equalsIgnoreCase(deployVariableType.type, type)) {
                return deployVariableType;
            }
        }
        return null;
    }

    /**
     * For DeployVariableKind deserialize.
     */
    @JsonValue
    public String toValue() {
        return this.type;
    }
}
