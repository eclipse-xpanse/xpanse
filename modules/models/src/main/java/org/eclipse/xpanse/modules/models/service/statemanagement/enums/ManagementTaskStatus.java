/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.statemanagement.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;

/**
 * Enumeration class for service running status.
 */
public enum ManagementTaskStatus {
    CREATED("created"),
    IN_PROGRESS("in progress"),
    SUCCESSFUL("successful"),
    FAILED("failed");

    private final String value;

    ManagementTaskStatus(String value) {
        this.value = value;
    }

    /**
     * For ManagementTaskStatus deserialize.
     */
    @JsonCreator
    public static ManagementTaskStatus getByValue(String value) {
        for (ManagementTaskStatus entry : values()) {
            if (StringUtils.equalsIgnoreCase(entry.value, value)) {
                return entry;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("ManagementTaskStatus value %s is not supported.", value));
    }

    /**
     * For ManagementTaskStatus serialize.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

}