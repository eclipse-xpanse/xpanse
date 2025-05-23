/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.ai.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;

/** Enum of AiApplicationType. */
public enum AiApplicationType {
    GAUSSDB_MCP("gaussdb_mcp"),
    MYSQL_MCP("mysql_mcp");

    private final String value;

    AiApplicationType(String value) {
        this.value = value;
    }

    /** For BillingMode serialize. */
    @JsonCreator
    public static AiApplicationType getByValue(String value) {
        for (AiApplicationType enumeration : values()) {
            if (StringUtils.equalsIgnoreCase(enumeration.value, value)) {
                return enumeration;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("AiApplicationType value %s is not supported.", value));
    }

    /** For AiApplicationType deserialize. */
    @JsonValue
    public String toValue() {
        return this.value;
    }
}
