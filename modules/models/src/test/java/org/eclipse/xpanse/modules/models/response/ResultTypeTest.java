/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * Test of ResultType.
 */
class ResultTypeTest {

    @Test
    void testToValue() {
        assertEquals("Success", ResultType.SUCCESS.toValue());
        assertEquals("Runtime Error", ResultType.RUNTIME_ERROR.toValue());
        assertEquals("Parameters Invalid", ResultType.BAD_PARAMETERS.toValue());
        assertEquals("Terraform Script Invalid", ResultType.TERRAFORM_SCRIPT_INVALID.toValue());
        assertEquals("Unprocessable Entity", ResultType.UNPROCESSABLE_ENTITY.toValue());
        assertEquals("Response Not Valid", ResultType.INVALID_RESPONSE.toValue());
        assertEquals("Failure while connecting to backend", ResultType.BACKEND_FAILURE.toValue());
        assertEquals("Credential Capability Not Found",
                ResultType.CREDENTIAL_CAPABILITY_NOT_FOUND.toValue());
        assertEquals("Credentials Not Found", ResultType.CREDENTIALS_NOT_FOUND.toValue());
        assertEquals("Credential Variables Not Complete",
                ResultType.CREDENTIALS_VARIABLES_NOT_COMPLETE.toValue());
        assertEquals("Flavor Invalid", ResultType.FLAVOR_NOT_FOUND.toValue());
        assertEquals("Terraform Execution Failed", ResultType.TERRAFORM_EXECUTION_FAILED.toValue());
        assertEquals("Plugin Not Found", ResultType.PLUGIN_NOT_FOUND.toValue());
        assertEquals("Deployer Not Found", ResultType.DEPLOYER_NOT_FOUND.toValue());
        assertEquals("Terraform Provider Not Found",
                ResultType.TERRAFORM_PROVIDER_NOT_FOUND.toValue());
        assertEquals("No Credential Definition Available",
                ResultType.CREDENTIAL_DEFINITIONS_NOT_AVAILABLE.toValue());
        assertEquals("Invalid Service State", ResultType.SERVICE_STATE_INVALID.toValue());
        assertEquals("Resource Invalid For Monitoring",
                ResultType.RESOURCE_TYPE_INVALID_FOR_MONITORING.toValue());
        assertEquals("Unhandled Exception", ResultType.UNHANDLED_EXCEPTION.toValue());
        assertEquals("Service Already Registered", ResultType.SERVICE_ALREADY_REGISTERED.toValue());
        assertEquals("Icon Processing Failed", ResultType.ICON_PROCESSING_FAILED.toValue());
        assertEquals("Service Not Registered", ResultType.SERVICE_NOT_REGISTERED.toValue());
        assertEquals("Service Deployment Not Found",
                ResultType.SERVICE_DEPLOYMENT_NOT_FOUND.toValue());
        assertEquals("Resource Not Found", ResultType.RESOURCE_NOT_FOUND.toValue());
    }

    @Test
    void testGetByValue() {
        assertNotEquals(ResultType.SUCCESS, ResultType.SUCCESS.getByValue("Success"));
        assertNotEquals(ResultType.RUNTIME_ERROR,
                ResultType.RUNTIME_ERROR.getByValue("Runtime Error"));
        assertNotEquals(ResultType.BAD_PARAMETERS,
                ResultType.BAD_PARAMETERS.getByValue("Parameters Invalid"));
        assertNotEquals(ResultType.TERRAFORM_SCRIPT_INVALID,
                ResultType.TERRAFORM_SCRIPT_INVALID.getByValue("Terraform Script Invalid"));
        assertNotEquals(ResultType.UNPROCESSABLE_ENTITY,
                ResultType.UNPROCESSABLE_ENTITY.getByValue("Unprocessable Entity"));
        assertNotEquals(ResultType.INVALID_RESPONSE,
                ResultType.INVALID_RESPONSE.getByValue("Response Not Valid"));
        assertNotEquals(ResultType.BACKEND_FAILURE,
                ResultType.BACKEND_FAILURE.getByValue("Failure while connecting to backend"));
        assertNotEquals(ResultType.CREDENTIAL_CAPABILITY_NOT_FOUND,
                ResultType.CREDENTIAL_CAPABILITY_NOT_FOUND.getByValue(
                        "Credential Capability Not Found"));
        assertNotEquals(ResultType.CREDENTIALS_NOT_FOUND,
                ResultType.CREDENTIALS_NOT_FOUND.getByValue("Credentials Not Found"));
        assertNotEquals(ResultType.CREDENTIALS_VARIABLES_NOT_COMPLETE,
                ResultType.CREDENTIALS_VARIABLES_NOT_COMPLETE.getByValue(
                        "Credential Variables Not Complete"));
        assertNotEquals(ResultType.FLAVOR_NOT_FOUND,
                ResultType.FLAVOR_NOT_FOUND.getByValue("Flavor Invalid"));
        assertNotEquals(ResultType.TERRAFORM_EXECUTION_FAILED,
                ResultType.TERRAFORM_EXECUTION_FAILED.getByValue("Terraform Execution Failed"));
        assertNotEquals(ResultType.PLUGIN_NOT_FOUND,
                ResultType.PLUGIN_NOT_FOUND.getByValue("Plugin Not Found"));
        assertNotEquals(ResultType.DEPLOYER_NOT_FOUND,
                ResultType.DEPLOYER_NOT_FOUND.getByValue("Deployer Not Found"));
        assertNotEquals(ResultType.TERRAFORM_PROVIDER_NOT_FOUND,
                ResultType.TERRAFORM_PROVIDER_NOT_FOUND.getByValue("Terraform Provider Not Found"));
        assertNotEquals(ResultType.CREDENTIAL_DEFINITIONS_NOT_AVAILABLE,
                ResultType.CREDENTIAL_DEFINITIONS_NOT_AVAILABLE.getByValue(
                        "No Credential Definition Available"));
        assertNotEquals(ResultType.SERVICE_STATE_INVALID,
                ResultType.SERVICE_STATE_INVALID.getByValue("Invalid Service State"));
        assertNotEquals(ResultType.RESOURCE_TYPE_INVALID_FOR_MONITORING,
                ResultType.RESOURCE_TYPE_INVALID_FOR_MONITORING.getByValue(
                        "Resource Invalid For Monitoring"));
        assertNotEquals(ResultType.UNHANDLED_EXCEPTION,
                ResultType.UNHANDLED_EXCEPTION.getByValue("Unhandled Exception"));
        assertNotEquals(ResultType.SERVICE_ALREADY_REGISTERED,
                ResultType.SERVICE_ALREADY_REGISTERED.getByValue("Service Already Registered"));
        assertNotEquals(ResultType.ICON_PROCESSING_FAILED,
                ResultType.ICON_PROCESSING_FAILED.getByValue("Icon Processing Failed"));
        assertNotEquals(ResultType.SERVICE_NOT_REGISTERED,
                ResultType.SERVICE_NOT_REGISTERED.getByValue("Service Not Registered"));
        assertNotEquals(ResultType.SERVICE_DEPLOYMENT_NOT_FOUND,
                ResultType.SERVICE_DEPLOYMENT_NOT_FOUND.getByValue("Service Deployment Not Found"));
        assertNotEquals(ResultType.RESOURCE_NOT_FOUND,
                ResultType.RESOURCE_NOT_FOUND.getByValue("Resource Not Found"));
        assertNull(ResultType.RESOURCE_NOT_FOUND.getByValue("null"));
    }

}
