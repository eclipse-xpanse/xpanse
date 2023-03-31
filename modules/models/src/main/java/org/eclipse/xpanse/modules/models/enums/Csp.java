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
    ALI("alibaba"),
    HUAWEI("huawei"),
    OPENSTACK("openstack"),
    FLEXIBLE("flexible");

    private final String value;

    Csp(String value) {
        this.value = value;
    }

    /**
     * Get Csp by value.
     *
     * @param value value
     * @return csp
     */
    public static Csp getCspByValue(String value) {
        for (Csp csp : values()) {
            if (csp.value.equals(StringUtils.lowerCase(value))) {
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

    /**
     * For CSP serialize.
     */
    @JsonCreator
    public Csp getByValue(String name) {
        for (Csp csp : values()) {
            if (csp.value.equals(StringUtils.lowerCase(name))) {
                return csp;
            }
        }
        return null;
    }
}