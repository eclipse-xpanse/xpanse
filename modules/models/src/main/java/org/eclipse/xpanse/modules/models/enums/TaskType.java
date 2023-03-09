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
 * Defines possible task type of  managed services.
 */
public enum TaskType {

    REGISTER("register"),
    START("start"),
    STOP("stop"),
    UPDATE("update"),
    UNREGISTER("unregister");

    private final String taskType;

    TaskType(String taskType) {
        this.taskType = taskType;
    }

    /**
     * For BillingPeriod serialize.
     */
    @JsonCreator
    public TaskType getByValue(String state) {
        for (TaskType taskType : values()) {
            if (taskType.taskType.equals(StringUtils.lowerCase(state))) {
                return taskType;
            }
        }
        return null;
    }

    /**
     * For BillingPeriod deserialize.
     */
    @JsonValue
    public String toValue() {
        return this.taskType;
    }
}
