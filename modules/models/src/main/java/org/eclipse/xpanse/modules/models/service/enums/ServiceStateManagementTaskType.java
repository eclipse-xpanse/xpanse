/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;

/**
 * Enumeration class for management task type.
 */
public enum ServiceStateManagementTaskType {
    START("start"),
    STOP("stop"),
    RESTART("restart");

    private final String value;

    ServiceStateManagementTaskType(String value) {
        this.value = value;
    }

    /**
     * For ServiceStateManagementTaskType deserialize.
     */
    @JsonCreator
    public static ServiceStateManagementTaskType getByValue(String value) {
        for (ServiceStateManagementTaskType entry : values()) {
            if (StringUtils.equalsIgnoreCase(entry.value, value)) {
                return entry;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("ServiceStateManagementTaskType value %s is not supported.", value));
    }

    /**
     * For ServiceStateManagementTaskType serialize.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

}