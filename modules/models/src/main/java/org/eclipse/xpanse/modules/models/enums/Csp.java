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
 * Cloud service providers.
 */
public enum Csp {
    AWS("aws"),
    AZURE("azure"),
    ALICLOUD("alicloud"),
    HUAWEI("huawei"),
    OPENSTACK("openstack"),
    FLEXIBLE_ENGINE("flexibleEngine");

    private final String value;

    Csp(String value) {
        this.value = value;
    }

    /**
     * For CSP serialize.
     */
    @JsonCreator
    public static Csp getByValue(String name) {
        for (Csp csp : values()) {
            if (StringUtils.equalsIgnoreCase(csp.value, name)) {
                return csp;
            }
        }
        return null;
    }

    /**
     * For CSP deserialize.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }
}
