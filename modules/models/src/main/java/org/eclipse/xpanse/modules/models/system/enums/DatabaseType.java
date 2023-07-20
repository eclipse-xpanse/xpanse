/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.system.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * Defines type of Database.
 */
public enum DatabaseType {

    H2DB("h2"),
    MARIADB("mariadb");

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
        return null;
    }

    /**
     * For DatabaseType deserialize.
     */
    @JsonValue
    public String toValue() {
        return this.code;
    }
}
