/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.system.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;

/** Defines possible states of the system. */
public enum HealthStatus {
    OK("OK"),
    NOK("NOK");
    private final String state;

    HealthStatus(String state) {
        this.state = state;
    }

    /** For RuntimeState serialize. */
    @JsonCreator
    public HealthStatus getByValue(String state) {
        for (HealthStatus healthStatus : values()) {
            if (healthStatus.state.equals(StringUtils.upperCase(state))) {
                return healthStatus;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("HealthStatus value %s is not supported.", state));
    }

    /** For RuntimeState deserialize. */
    @JsonValue
    public String toValue() {
        return this.state;
    }
}
