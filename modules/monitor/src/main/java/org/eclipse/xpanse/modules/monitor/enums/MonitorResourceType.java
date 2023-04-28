/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.monitor.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * The Resources Type of The Monitor.
 */
public enum MonitorResourceType {
    CPU("cpu"),
    MEM("mem"),
    NETWORK("network"),
    DISK("disk");

    private final String value;

    MonitorResourceType(String value) {
        this.value = value;
    }

    /**
     * For MonitorResourceType serialize.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * For MonitorResourceType deserialize.
     */
    @JsonCreator
    public MonitorResourceType getByValue(String name) {
        for (MonitorResourceType monitorEnum : values()) {
            if (monitorEnum.value.equals(StringUtils.lowerCase(name))) {
                return monitorEnum;
            }
        }
        return null;
    }
}
