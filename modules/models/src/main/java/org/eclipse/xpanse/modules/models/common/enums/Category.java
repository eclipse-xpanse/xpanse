/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;

/**
 * Service Category enums.
 */
public enum Category {
    AI("ai"),
    COMPUTE("compute"),
    CONTAINER("container"),
    STORAGE("storage"),
    NETWORK("network"),
    DATABASE("database"),
    MEDIA_SERVICE("mediaService"),
    SECURITY("security"),
    MIDDLEWARE("middleware"),
    OTHERS("others");

    private final String catalog;

    Category(String catalog) {
        this.catalog = catalog;
    }

    /**
     * For Category deserialize.
     */
    @JsonCreator
    public static Category getByValue(String catalog) {
        for (Category category : values()) {
            if (StringUtils.equalsIgnoreCase(category.catalog, catalog)) {
                return category;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("Category value %s is not supported.", catalog));
    }

    /**
     * For Category serialize.
     */
    @JsonValue
    public String toValue() {
        return this.catalog;
    }
}
