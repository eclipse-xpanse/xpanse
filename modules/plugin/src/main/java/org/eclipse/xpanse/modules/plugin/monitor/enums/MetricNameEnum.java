/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.plugin.monitor.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * The Name of the Metric.
 */
public enum MetricNameEnum {

    VM_CPU_USAGE("vm_cpu_usage"),
    VM_MEM_USAGE("vm_mem_usage"),
    VM_DISK_USAGE("vm_disk_usage");

    private String name;

    MetricNameEnum(String name) {
        this.name = name;
    }

    /**
     * For MetricName serialize.
     */
    @JsonValue
    public String toValue() {
        return this.name;
    }

    /**
     * For MetricName deserialize.
     */
    @JsonCreator
    public MetricNameEnum getByValue(String name) {
        for (MetricNameEnum mertricName : values()) {
            if (mertricName.name.equals(StringUtils.lowerCase(name))) {
                return mertricName;
            }
        }
        return null;
    }
}
