/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * Deploy variable data types.
 */
public enum DeployVariableType {
    STRING("string"),
    NUMBER("number");

    private final String type;

    DeployVariableType(String type) {
        this.type = type;
    }

    /**
     * For DeployVariableKind serialize.
     */
    @JsonCreator
    public DeployVariableType getByValue(String type) {
        for (DeployVariableType deployVariableType : values()) {
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
