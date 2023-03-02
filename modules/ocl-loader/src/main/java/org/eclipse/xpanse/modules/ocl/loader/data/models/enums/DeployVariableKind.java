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
 * Deploy variable kinds.
 */
public enum DeployVariableKind {
    FIX("fix"),
    VARIABLE("variable");

    private final String type;

    DeployVariableKind(String type) {
        this.type = type;
    }

    /**
     * For DeployVariableKind serialize.
     */
    @JsonCreator
    public DeployVariableKind getByValue(String type) {
        for (DeployVariableKind deployVariableKind : values()) {
            if (deployVariableKind.type.equals(StringUtils.lowerCase(type))) {
                return deployVariableKind;
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
