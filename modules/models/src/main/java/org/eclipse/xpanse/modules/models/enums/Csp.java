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

    private final String name;

    Csp(String name) {
        this.name = name;
    }

    /**
     * For CSP serialize.
     */
    @JsonCreator
    public Csp getByValue(String name) {
        for (Csp csp : values()) {
            if (csp.name.equals(StringUtils.lowerCase(name))) {
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
        return this.name;
    }
}