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
 * type for StorageSize.
 */
public enum StorageType {
    SSD("SSD"),
    SAS("SAS");

    private final String type;

    StorageType(String type) {
        this.type = type;
    }

    /**
     * For StorageType serialize.
     */
    @JsonCreator
    public StorageType getByValue(String type) {
        for (StorageType storageType : values()) {
            if (storageType.type.equals(StringUtils.upperCase(type))) {
                return storageType;
            }
        }
        return null;
    }

    /**
     * For StorageType deserialize.
     */
    @JsonValue
    public String toValue() {
        return this.type;
    }
}