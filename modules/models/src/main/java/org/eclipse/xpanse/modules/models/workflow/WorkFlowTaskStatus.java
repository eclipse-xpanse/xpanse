/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.workflow;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;

/**
 * Workflow Task Status enums.
 */
public enum WorkFlowTaskStatus {
    DONE("done"),
    FAILED("failed");

    private final String status;

    WorkFlowTaskStatus(String status) {
        this.status = status;
    }

    /**
     * For Category deserialize.
     */
    @JsonCreator
    public static WorkFlowTaskStatus getByValue(String status) {
        for (WorkFlowTaskStatus workflowTaskStatus : values()) {
            if (StringUtils.equalsIgnoreCase(workflowTaskStatus.status, status)) {
                return workflowTaskStatus;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("Category value %s is not supported.", status));
    }

    /**
     * For Category serialize.
     */
    @JsonValue
    public String toValue() {
        return this.status;
    }
}
