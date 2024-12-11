/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * Test of ResultType.
 */
class ErrorTypeTest {

    @Test
    void testToValue() {
        assertEquals("Runtime Error", ErrorType.RUNTIME_ERROR.toValue());
        assertEquals("Parameters Invalid", ErrorType.BAD_PARAMETERS.toValue());
        assertEquals("Terraform Script Invalid",
                ErrorType.TERRAFORM_SCRIPT_INVALID.toValue());
        assertEquals("Unprocessable Entity",
                ErrorType.UNPROCESSABLE_ENTITY.toValue());
        assertEquals("Response Not Valid",
                ErrorType.INVALID_RESPONSE.toValue());
        assertEquals("Failure while connecting to backend",
                ErrorType.BACKEND_FAILURE.toValue());
        assertEquals("Credential Capability Not Found",
                ErrorType.CREDENTIAL_CAPABILITY_NOT_FOUND.toValue());
        assertEquals("Credentials Not Found",
                ErrorType.CREDENTIALS_NOT_FOUND.toValue());
        assertEquals("Credential Variables Not Complete",
                ErrorType.CREDENTIALS_VARIABLES_NOT_COMPLETE.toValue());
        assertEquals("Flavor Invalid", ErrorType.FLAVOR_NOT_FOUND.toValue());
        assertEquals("Terraform Execution Failed",
                ErrorType.TERRAFORM_EXECUTION_FAILED.toValue());
        assertEquals("Plugin Not Found", ErrorType.PLUGIN_NOT_FOUND.toValue());
        assertEquals("Deployer Not Found", ErrorType.DEPLOYER_NOT_FOUND.toValue());
        assertEquals("No Credential Definition Available",
                ErrorType.CREDENTIAL_DEFINITIONS_NOT_AVAILABLE.toValue());
        assertEquals("Invalid Service State", ErrorType.SERVICE_STATE_INVALID.toValue());
        assertEquals("Resource Invalid For Monitoring",
                ErrorType.RESOURCE_TYPE_INVALID_FOR_MONITORING.toValue());
        assertEquals("Unhandled Exception", ErrorType.UNHANDLED_EXCEPTION.toValue());
        assertEquals("Service Template Request Not Allowed",
                ErrorType.SERVICE_TEMPLATE_REQUEST_NOT_ALLOWED.toValue());
        assertEquals("Icon Processing Failed",
                ErrorType.ICON_PROCESSING_FAILED.toValue());
        assertEquals("Service Template Not Registered",
                ErrorType.SERVICE_TEMPLATE_NOT_REGISTERED.toValue());
        assertEquals("Service Deployment Not Found",
                ErrorType.SERVICE_DEPLOYMENT_NOT_FOUND.toValue());
        assertEquals("Resource Not Found", ErrorType.RESOURCE_NOT_FOUND.toValue());
    }

    @Test
    void testGetResultTypeByValue() {
        assertEquals(ErrorType.RUNTIME_ERROR,
                ErrorType.getResultTypeByValue("Runtime Error"));
        assertEquals(ErrorType.BAD_PARAMETERS,
                ErrorType.getResultTypeByValue("Parameters Invalid"));
        assertEquals(ErrorType.TERRAFORM_SCRIPT_INVALID,
                ErrorType.getResultTypeByValue("Terraform Script Invalid"));
        assertEquals(ErrorType.UNPROCESSABLE_ENTITY,
                ErrorType.getResultTypeByValue("Unprocessable Entity"));
        assertEquals(ErrorType.INVALID_RESPONSE,
                ErrorType.getResultTypeByValue("Response Not Valid"));
        assertEquals(ErrorType.BACKEND_FAILURE,
                ErrorType.getResultTypeByValue("Failure while connecting to backend"));
        assertEquals(ErrorType.CREDENTIAL_CAPABILITY_NOT_FOUND,
                ErrorType.getResultTypeByValue("Credential Capability Not Found"));
        assertEquals(ErrorType.CREDENTIALS_NOT_FOUND,
                ErrorType.getResultTypeByValue("Credentials Not Found"));
        assertEquals(ErrorType.CREDENTIALS_VARIABLES_NOT_COMPLETE,
                ErrorType.getResultTypeByValue("Credential Variables Not Complete"));
        assertEquals(ErrorType.FLAVOR_NOT_FOUND,
                ErrorType.getResultTypeByValue("Flavor Invalid"));
        assertEquals(ErrorType.TERRAFORM_EXECUTION_FAILED,
                ErrorType.getResultTypeByValue("Terraform Execution Failed"));
        assertEquals(ErrorType.PLUGIN_NOT_FOUND,
                ErrorType.getResultTypeByValue("Plugin Not Found"));
        assertEquals(ErrorType.DEPLOYER_NOT_FOUND,
                ErrorType.getResultTypeByValue("Deployer Not Found"));
        assertEquals(ErrorType.CREDENTIAL_DEFINITIONS_NOT_AVAILABLE,
                ErrorType.getResultTypeByValue("No Credential Definition Available"));
        assertEquals(ErrorType.SERVICE_STATE_INVALID,
                ErrorType.getResultTypeByValue("Invalid Service State"));
        assertEquals(ErrorType.RESOURCE_TYPE_INVALID_FOR_MONITORING,
                ErrorType.getResultTypeByValue("Resource Invalid For Monitoring"));
        assertEquals(ErrorType.UNHANDLED_EXCEPTION,
                ErrorType.getResultTypeByValue("Unhandled Exception"));
        assertEquals(ErrorType.SERVICE_TEMPLATE_REQUEST_NOT_ALLOWED,
                ErrorType.getResultTypeByValue("Service Template Request Not Allowed"));
        assertEquals(ErrorType.ICON_PROCESSING_FAILED,
                ErrorType.getResultTypeByValue("Icon Processing Failed"));
        assertEquals(ErrorType.SERVICE_TEMPLATE_NOT_REGISTERED,
                ErrorType.getResultTypeByValue("Service Template Not Registered"));
        assertEquals(ErrorType.SERVICE_DEPLOYMENT_NOT_FOUND,
                ErrorType.getResultTypeByValue("Service Deployment Not Found"));
        assertEquals(ErrorType.RESOURCE_NOT_FOUND,
                ErrorType.getResultTypeByValue("Resource Not Found"));
        assertNull(ErrorType.getResultTypeByValue("null"));
    }

    @Test
    void testGetTypeByValue() {
        ErrorType test = ErrorType.BACKEND_FAILURE;

        assertEquals(ErrorType.RUNTIME_ERROR,
                test.getByValue("Runtime Error"));
        assertEquals(ErrorType.BAD_PARAMETERS,
                test.getByValue("Parameters Invalid"));
        assertEquals(ErrorType.TERRAFORM_SCRIPT_INVALID,
                test.getByValue("Terraform Script Invalid"));
        assertEquals(ErrorType.UNPROCESSABLE_ENTITY,
                test.getByValue("Unprocessable Entity"));
        assertEquals(ErrorType.INVALID_RESPONSE,
                test.getByValue("Response Not Valid"));
        assertEquals(ErrorType.BACKEND_FAILURE,
                test.getByValue("Failure while connecting to backend"));
        assertEquals(ErrorType.CREDENTIAL_CAPABILITY_NOT_FOUND,
                test.getByValue("Credential Capability Not Found"));
        assertEquals(ErrorType.CREDENTIALS_NOT_FOUND,
                test.getByValue("Credentials Not Found"));
        assertEquals(ErrorType.CREDENTIALS_VARIABLES_NOT_COMPLETE,
                test.getByValue("Credential Variables Not Complete"));
        assertEquals(ErrorType.FLAVOR_NOT_FOUND,
                test.getByValue("Flavor Invalid"));
        assertEquals(ErrorType.TERRAFORM_EXECUTION_FAILED,
                test.getByValue("Terraform Execution Failed"));
        assertEquals(ErrorType.PLUGIN_NOT_FOUND,
                test.getByValue("Plugin Not Found"));
        assertEquals(ErrorType.DEPLOYER_NOT_FOUND,
                test.getByValue("Deployer Not Found"));
        assertEquals(ErrorType.CREDENTIAL_DEFINITIONS_NOT_AVAILABLE,
                test.getByValue("No Credential Definition Available"));
        assertEquals(ErrorType.SERVICE_STATE_INVALID,
                test.getByValue("Invalid Service State"));
        assertEquals(ErrorType.RESOURCE_TYPE_INVALID_FOR_MONITORING,
                test.getByValue("Resource Invalid For Monitoring"));
        assertEquals(ErrorType.UNHANDLED_EXCEPTION,
                test.getByValue("Unhandled Exception"));
        assertEquals(ErrorType.SERVICE_TEMPLATE_REQUEST_NOT_ALLOWED,
                test.getByValue("Service Template Request Not Allowed"));
        assertEquals(ErrorType.ICON_PROCESSING_FAILED,
                test.getByValue("Icon Processing Failed"));
        assertEquals(ErrorType.SERVICE_TEMPLATE_NOT_REGISTERED,
                test.getByValue("Service Template Not Registered"));
        assertEquals(ErrorType.SERVICE_DEPLOYMENT_NOT_FOUND,
                test.getByValue("Service Deployment Not Found"));
        assertEquals(ErrorType.RESOURCE_NOT_FOUND,
                test.getByValue("Resource Not Found"));
        assertNull(test.getByValue("null"));
    }

}
