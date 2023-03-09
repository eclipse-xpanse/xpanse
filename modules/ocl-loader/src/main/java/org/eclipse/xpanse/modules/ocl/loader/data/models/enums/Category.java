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
 * Period for Billing.
 */
public enum Category {
    AI("AI"),
    COMPUTE("COMPUTE"),
    CONTAINER("CONTAINER"),
    STORAGE("STORAGE"),
    NETWORK("NETWORK"),
    DATABASE("DATABASE"),
    MEDIA_SERVICE("MediaService"),
    SECURITY("SECURITY"),
    MIDDLEWARE("MIDDLEWARE"),
    OTHERS("OTHERS");

    private final String catalog;

    Category(String catalog) {
        this.catalog = catalog;
    }

    /**
     * For Category serialize.
     */
    @JsonCreator
    public Category getByValue(String period) {
        for (Category category : values()) {
            if (StringUtils.equalsIgnoreCase(category.catalog, period)) {
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
