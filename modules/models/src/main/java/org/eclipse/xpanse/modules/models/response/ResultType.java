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
    RUNTIME_ERROR("Runtime Error"),
    BAD_PARAMETERS("Parameters Invalid"),
    TERRAFORM_SCRIPT_INVALID("Terraform Script Invalid"),
    UNPROCESSABLE_ENTITY("Unprocessable Entity"),
    INVALID_RESPONSE("Response Not Valid"),
    BACKEND_FAILURE("Failure while connecting to backend"),
    CREDENTIAL_CAPABILITY_NOT_FOUND("Credential Capability Not Found"),
    CREDENTIALS_NOT_FOUND("Credentials Not Found"),
    CREDENTIALS_VARIABLES_NOT_COMPLETE("Credential Variables Not Complete"),
    FLAVOR_NOT_FOUND("Flavor Invalid"),
    TERRAFORM_EXECUTION_FAILED("Terraform Execution Failed"),
    PLUGIN_NOT_FOUND("Plugin Not Found"),
    DEPLOYER_NOT_FOUND("Deployer Not Found"),
    TERRAFORM_PROVIDER_NOT_FOUND("Terraform Provider Not Found"),
    CREDENTIAL_DEFINITIONS_NOT_AVAILABLE("No Credential Definition Available"),
    SERVICE_STATE_INVALID("Invalid Service State"),
    RESOURCE_TYPE_INVALID_FOR_MONITORING("Resource Invalid For Monitoring"),
    UNHANDLED_EXCEPTION("Unhandled Exception"),
    SERVICE_TEMPLATE_ALREADY_REGISTERED("Service Template Already Registered"),
    ICON_PROCESSING_FAILED("Icon Processing Failed"),
    SERVICE_TEMPLATE_NOT_REGISTERED("Service Template Not Registered"),
    SERVICE_DEPLOYMENT_NOT_FOUND("Service Deployment Not Found"),
    RESOURCE_NOT_FOUND("Resource Not Found"),
    DEPLOYMENT_VARIABLE_INVALID("Deployment Variable Invalid"),
    SERVICE_TEMPLATE_UPDATE_NOT_ALLOWED("Service Template Update Not Allowed"),
    UNAUTHORIZED("Unauthorized"),
    ACCESS_DENIED("Access Denied"),
    SENSITIVE_FIELD_ENCRYPTION_DECRYPTION_EXCEPTION("Sensitive "
            + "Field Encryption Or Decryption Failed Exception"),
    UNSUPPORTED_ENUM_VALUE("Unsupported Enum Value"),
    TERRAFORM_BOOT_REQUEST_FAILED("Terraform Boot Request Failed"),
    METRICS_DATA_NOT_READY("Metrics Data Not Ready"),
    VARIABLE_VALIDATION_FAILED("Variable Validation Failed");

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
     * Get ResultType by value.
     *
     * @param value value
     * @return ResultType
     */
    public static ResultType getResultTypeByValue(String value) {
        for (ResultType resultType : values()) {
            if (StringUtils.endsWithIgnoreCase(resultType.value, value)) {
                return resultType;
            }
        }
        return null;
    }

    /**
     * For ResultType serialize.
     */
    @JsonCreator
    public ResultType getByValue(String value) {
        for (ResultType resultType : values()) {
            if (StringUtils.endsWithIgnoreCase(resultType.value, value)) {
                return resultType;
            }
        }
        return null;
    }
}

