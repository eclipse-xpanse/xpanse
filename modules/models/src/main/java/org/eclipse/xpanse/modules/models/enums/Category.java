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
     * Get Category by value.
     *
     * @param catalog catalog
     * @return category
     */
    public static Category getCategoryByCatalog(String catalog) {
        for (Category category : values()) {
            if (StringUtils.equalsIgnoreCase(category.catalog, catalog)) {
                return category;
            }
        }
        return null;
    }

    /**
     * For Category serialize.
     */
    @JsonCreator
    public Category getByValue(String period) {
        for (Category category : values()) {
            if (StringUtils.equalsIgnoreCase(category.catalog, catalog)) {
                return category;
            }
        }
        return null;
    }

    /**
     * For Category deserialize.
     */
    @JsonValue
    public String toValue() {
        return this.catalog;
    }
}
