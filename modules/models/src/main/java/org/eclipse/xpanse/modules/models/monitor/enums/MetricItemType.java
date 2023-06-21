/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.monitor.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * The type of the metrics item.
 */
public enum MetricItemType {
    VALUE("value"),
    COUNT("count"),
    SUM("sum");

    private final String type;

    MetricItemType(String type) {
        this.type = type;
    }

    /**
     * For MetricsItemType serialize.
     */
    @JsonCreator
    public MetricItemType getByValue(String type) {
        for (MetricItemType metricsItemType : values()) {
            if (metricsItemType.type.equals(StringUtils.lowerCase(type))) {
                return metricsItemType;
            }
        }
        return null;
    }

    /**
     * For MetricsItemType deserialize.
     */
    @JsonValue
    public String toValue() {
        return this.type;
    }
}
