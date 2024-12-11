/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.observability.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/** Defines possible status from otel-collector health-check extension. */
public enum CollectorStatus {
    SERVER_AVAILABLE("Server available"),
    SERVER_NOT_AVAILABLE("Server not available");

    private final String value;

    CollectorStatus(String value) {
        this.value = value;
    }

    /** For CollectorStatus deserialize. */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /** For CollectorStatus serialize. */
    @JsonCreator
    public CollectorStatus getByValue(String value) {
        for (CollectorStatus collectorStatus : values()) {
            if (StringUtils.endsWithIgnoreCase(collectorStatus.value, value)) {
                return collectorStatus;
            }
        }
        return CollectorStatus.SERVER_NOT_AVAILABLE;
    }
}
