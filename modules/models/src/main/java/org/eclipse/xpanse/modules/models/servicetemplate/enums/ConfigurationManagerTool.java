/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;

/** Defines the tool of service configuration manage. */
public enum ConfigurationManagerTool {
    ANSIBLE("ansible");

    private final String type;

    ConfigurationManagerTool(String type) {
        this.type = type;
    }

    /** For ConfigurationManagerTool serialize. */
    @JsonCreator
    public static ConfigurationManagerTool getByValue(String type) {
        for (ConfigurationManagerTool tool : values()) {
            if (StringUtils.equalsIgnoreCase(tool.type, type)) {
                return tool;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("ConfigurationManagerTool value %s is not supported.", type));
    }

    /** For ConfigurationManagerTool deserialize. */
    @JsonValue
    public String toValue() {
        return this.type;
    }
}
