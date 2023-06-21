/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.response;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * Result codes for the REST API.
 */
public enum ResultType {
    SUCCESS("Success"),
    RUNTIME_ERROR("Runtime Failure"),
    BAD_PARAMETERS("Parameters Invalid"),
    TERRAFORM_SCRIPT_INVALID("Terraform Script Invalid"),
    UNPROCESSABLE_ENTITY("Unprocessable Entity"),
    INVALID_RESPONSE("Response Not Valid"),
    BACKEND_FAILURE("Failure while connecting to backend"),
    CREDENTIAL_CAPABILITY_NOT_FOUND("Credential Capability Not Found"),
    CREDENTIALS_NOT_FOUND("Credentials Not Found"),
    CREDENTIALS_VARIABLES_NOT_COMPLETE("Credential Variables Not Complete");

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

