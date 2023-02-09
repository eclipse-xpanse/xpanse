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
 * SizeUnit for StorageSize.
 */
public enum StorageSizeUnit {
    KB("KB"),
    MB("MB"),
    GB("GB"),
    TB("TB"),
    PB("PB");

    private final String sizeUnit;

    StorageSizeUnit(String sizeUnit) {
        this.sizeUnit = sizeUnit;
    }

    /**
     * For StorageSizeUnit serialize.
     */
    @JsonCreator
    public StorageSizeUnit getByValue(String sizeUnit) {
        for (StorageSizeUnit storageSizeUnit : values()) {
            if (storageSizeUnit.sizeUnit.equals(StringUtils.upperCase(sizeUnit))) {
                return storageSizeUnit;
            }
        }
        return null;
    }

    /**
     * For StorageSizeUnit deserialize.
     */
    @JsonValue
    public String toValue() {
        return this.sizeUnit;
    }
}