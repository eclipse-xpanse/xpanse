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
 * Deploy variable validator type.
 */
public enum VariableValidator {

    // minLength=8
    MINLENGTH("minLength"),
    // maxLength=16
    MAXLENGTH("maxLength"),
    // minimum=1
    MINIMUM("minimum"),
    // maximum=100
    MAXIMUM("maximum"),
    // pattern=*e*
    PATTERN("pattern"),
    // enum=["AWS","ALI","HUAWEI"]
    ENUM("enum");

    private final String type;

    VariableValidator(String type) {
        this.type = type;
    }

    /**
     * For DeployVariableKind serialize.
     */
    @JsonCreator
    public VariableValidator getByValue(String type) {
        for (VariableValidator deployVariableType : values()) {
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
