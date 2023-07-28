/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.monitor.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;

/**
 * The Resources Type of The Monitor.
 */
public enum MonitorResourceType {
    CPU("cpu"),
    MEM("mem"),
    VM_NETWORK_INCOMING("vm_network_incoming"),
    VM_NETWORK_OUTGOING("vm_network_outgoing");

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
    public static MonitorResourceType getByValue(String name) {
        for (MonitorResourceType monitorEnum : values()) {
            if (monitorEnum.value.equals(StringUtils.lowerCase(name))) {
                return monitorEnum;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("MonitorResourceType value %s is not supported.", name));
    }
}
