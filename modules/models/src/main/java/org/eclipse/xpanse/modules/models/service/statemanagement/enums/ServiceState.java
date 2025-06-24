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

/** Enumeration class for service running status. */
public enum ServiceState {
    NOT_RUNNING("not running"),
    RUNNING("running"),
    STARTING("starting"),
    STOPPING("stopping"),
    STOPPED("stopped"),
    RESTARTING("restarting"),
    UNKNOWN("unknown");

    private final String value;

    ServiceState(String value) {
        this.value = value;
    }

    /** For ServiceState deserialize. */
    @JsonCreator
    public static ServiceState getByValue(String value) {
        for (ServiceState entry : values()) {
            if (StringUtils.equalsIgnoreCase(entry.value, value)) {
                return entry;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("ServiceState value %s is not supported.", value));
    }

    /** For ServiceState serialize. */
    @JsonValue
    public String toValue() {
        return this.value;
    }
}
