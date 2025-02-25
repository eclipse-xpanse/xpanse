/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/** Result codes for the REST API. */
public enum ErrorType {
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
    CREDENTIAL_DEFINITIONS_NOT_AVAILABLE("No Credential Definition Available"),
    SERVICE_STATE_INVALID("Invalid Service State"),
    RESOURCE_TYPE_INVALID_FOR_MONITORING("Resource Invalid For Monitoring"),
    UNHANDLED_EXCEPTION("Unhandled Exception"),
    ICON_PROCESSING_FAILED("Icon Processing Failed"),
    SERVICE_TEMPLATE_NOT_REGISTERED("Service Template Not Registered"),
    SERVICE_TEMPLATE_UNAVAILABLE("Service Template Unavailable"),
    SERVICE_TEMPLATE_REQUEST_NOT_ALLOWED("Service Template Request Not Allowed"),
    SERVICE_TEMPLATE_REQUEST_NOT_FOUND("Service Template Request Not Found"),
    REVIEW_SERVICE_TEMPLATE_REQUEST_NOT_ALLOWED("Review Service Template Request Not Allowed"),
    INVALID_SERVICE_VERSION("Invalid Service Version"),
    INVALID_SERVICE_FLAVORS("Invalid Service Flavors"),
    MANDATORY_VALUE_MISSING("Mandatory Value Missing"),
    INVALID_BILLING_CONFIG("Invalid Billing Config"),
    UNAVAILABLE_SERVICE_REGIONS("Unavailable Service Regions"),
    SERVICE_DEPLOYMENT_NOT_FOUND("Service Deployment Not Found"),
    RESOURCE_NOT_FOUND("Resource Not Found"),
    DEPLOYMENT_VARIABLE_INVALID("Deployment Variable Invalid"),
    UNAUTHORIZED("Unauthorized"),
    ACCESS_DENIED("Access Denied"),
    SENSITIVE_FIELD_ENCRYPTION_DECRYPTION_EXCEPTION(
            "Sensitive " + "Field Encryption Or Decryption Failed Exception"),
    UNSUPPORTED_ENUM_VALUE("Unsupported Enum Value"),
    TERRA_BOOT_REQUEST_FAILED("Terra Boot Request Failed"),
    TOFU_MAKER_REQUEST_FAILED("Tofu Maker Request Failed"),
    METRICS_DATA_NOT_READY("Metrics Data Not Ready"),
    VARIABLE_VALIDATION_FAILED("Variable Validation Failed"),
    VARIABLE_SCHEMA_DEFINITION_INVALID("Variable Schema Definition Invalid"),
    POLICY_NOT_FOUND("Policy Not Found"),
    POLICY_DUPLICATE("Duplicate Policy"),
    POLICY_VALIDATION_FAILED("Policy Validation Failed"),
    POLICY_EVALUATION_FAILED("Policy Evaluation Failed"),
    USER_NO_LOGIN_EXCEPTION("Current Login User No Found"),
    SERVICE_DETAILS_NOT_ACCESSIBLE("Service Details No Accessible"),
    ACTIVITI_TASK_NOT_FOUND("Service Porting Activiti Task Not Found"),
    SERVICE_PORTING_FAILED_EXCEPTION("Service Porting Failed Exception"),
    SERVICE_PORTING_NOT_FOUND("Service Porting Not Found"),
    SERVICE_LOCKED("Service Locked"),
    EULA_NOT_ACCEPTED("Eula Not Accepted"),
    SERVICE_FLAVOR_DOWNGRADE_NOT_ALLOWED("Service Flavor Downgrade Not Allowed"),
    BILLING_MODE_NOT_SUPPORTED("Billing Mode Not Supported"),
    SERVICE_STATE_MANAGEMENT_TASK_NOT_FOUND("Service State Management Task Not Found"),
    SERVICE_ORDER_NOT_FOUND("Service Order Not Found"),
    SERVICE_PRICE_CALCULATION_FAILED("Service Price Calculation Failed"),
    INVALID_GIT_REPO_DETAILS("Invalid Git Repo Details"),
    FILE_LOCKED("File Locked"),
    INVALID_SERVICE_CONFIGURATION("Service Configuration Invalid"),
    SERVICE_CONFIG_UPDATE_REQUEST_NOT_FOUND("Service Configuration Update Request Not Found"),
    SERVICE_CONFIGURATION_NOT_FOUND("Service Configuration Not Found"),
    INVALID_DEPLOYER_TOOL("Invalid Deployer Tool"),
    DEPLOYMENT_SCRIPTS_CREATION_FAILED("Deployment Scripts Creation Failed"),
    ASYNC_START_SERVICE_ERROR("Async Start Service Error"),
    ASYNC_STOP_SERVICE_ERROR("Async Stop Service Error"),
    ASYNC_RESTART_SERVICE_ERROR("Async Restart Service Error"),
    DEPLOYMENT_FAILED_EXCEPTION("Deployment Failed Exception"),
    DESTROY_FAILED_EXCEPTION("Destroy Failed Exception"),
    INVALID_SERVICE_ACTION("Service Action Invalid"),
    SERVICE_CHANGE_FAILED("Service Change Request Failed"),
    SERVICE_CONFIG_CHANGE_ORDER_ALREADY_EXISTS("Service Configuration Change Order Already Exists");

    private final String value;

    ErrorType(String value) {
        this.value = value;
    }

    /**
     * Get ResultType by value.
     *
     * @param value value
     * @return ResultType
     */
    public static ErrorType getResultTypeByValue(String value) {
        for (ErrorType errorType : values()) {
            if (StringUtils.endsWithIgnoreCase(errorType.value, value)) {
                return errorType;
            }
        }
        return null;
    }

    /** For ResultType deserialize. */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /** For ResultType serialize. */
    @JsonCreator
    public ErrorType getByValue(String value) {
        return getResultTypeByValue(value);
    }
}
