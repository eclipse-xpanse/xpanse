/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.ocl.loader.data.models.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Cloud service providers.
 */
public enum Csp {
    AWS("aws"),
    AZURE("azure"),
    ALI("alibaba"),
    HUAWEI("huawei");

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
            if (csp.name.equals(name)) {
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