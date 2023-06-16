/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.plugin.monitor.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * The unit of the metric.
 */
public enum MetricUnit {

    MB("mb"),
    KB("kb"),
    PERCENTAGE("percentage"),
    BITS_PER_SECOND("bit/s"),
    BYTES_PER_SECOND("Byte/s");

    private final String type;

    MetricUnit(String type) {
        this.type = type;
    }

    /**
     * For MetricUnit deserialize.
     */
    @JsonCreator
    public static MetricUnit getByValue(String type) {
        for (MetricUnit metricUnit : values()) {
            if (metricUnit.type.equals(StringUtils.lowerCase(type))) {
                return metricUnit;
            }
        }
        return null;
    }

    /**
     * For MetricUnit serialize.
     */
    @JsonValue
    public String toValue() {
        return this.type;
    }
}
