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
 * Enumeration class for service order status.
 */
public enum TaskStatus {
    CREATED("created"),
    IN_PROGRESS("in progress"),
    SUCCESSFUL("successful"),
    FAILED("failed");

    private final String value;

    TaskStatus(String value) {
        this.value = value;
    }

    /**
     * For WorkFlowTaskStatus deserialize.
     */
    @JsonCreator
    public static TaskStatus getByValue(String value) {
        for (TaskStatus entry : values()) {
            if (StringUtils.equalsIgnoreCase(entry.value, value)) {
                return entry;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("WorkFlowTaskStatus value %s is not supported.", value));
    }

    /**
     * For WorkFlowTaskStatus serialize.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

}