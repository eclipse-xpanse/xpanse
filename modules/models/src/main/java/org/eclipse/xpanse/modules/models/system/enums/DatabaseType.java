/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.system.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;

/**
 * Defines type of Database.
 */
public enum DatabaseType {

    H2DB("h2"),
    MYSQL("mysql");

    private final String code;

    DatabaseType(String code) {
        this.code = code;
    }

    /**
     * For DatabaseType serialize.
     */
    @JsonCreator
    public static DatabaseType getByValue(String code) {
        for (DatabaseType providerType : values()) {
            if (StringUtils.equalsIgnoreCase(code, providerType.code)) {
                return providerType;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("DatabaseType value %s is not supported.", code));
    }

    /**
     * For DatabaseType deserialize.
     */
    @JsonValue
    public String toValue() {
        return this.code;
    }
}
