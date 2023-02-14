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
 * Defines possible states of the system.
 */
public enum HealthStatus {

    OK("OK"),
    NOK("NOK");
    private final String state;

    HealthStatus(String state) {
        this.state = state;
    }

    /**
     * For RuntimeState serialize.
     */
    @JsonCreator
    public HealthStatus getByValue(String state) {
        for (HealthStatus healthStatus : values()) {
            if (healthStatus.state.equals(StringUtils.upperCase(state))) {
                return healthStatus;
            }
        }
        return null;
    }

    /**
     * For RuntimeState deserialize.
     */
    @JsonValue
    public String toValue() {
        return this.state;
    }
}
