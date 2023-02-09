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
 * user data types for UserData.
 */
public enum UserDataType {
    SHELL("shell"),
    POWERSHELL("powershell");

    private final String scriptType;

    UserDataType(String scriptType) {
        this.scriptType = scriptType;
    }

    /**
     * For UserDataType serialize.
     */
    @JsonCreator
    public UserDataType getByValue(String scriptType) {
        for (UserDataType userDataType : values()) {
            if (userDataType.scriptType.equals(StringUtils.lowerCase(scriptType))) {
                return userDataType;
            }
        }
        return null;
    }

    /**
     * For UserDataType deserialize.
     */
    @JsonValue
    public String toValue() {
        return this.scriptType;
    }
}
