/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.response;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * Result codes for the REST API.
 */
public enum ResultType {
    SUCCESS("success"),
    RUNTIME_ERROR("Runtime failure"),
    BAD_PARAMETERS("Parameters invalid");

    private final String value;

    ResultType(String value) {
        this.value = value;
    }

    /**
     * For ResultType deserialize.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * For ResultType serialize.
     */
    @JsonCreator
    public ResultType getByValue(String name) {
        for (ResultType resultType : values()) {
            if (resultType.value.equals(StringUtils.lowerCase(name))) {
                return resultType;
            }
        }
        return null;
    }
}

