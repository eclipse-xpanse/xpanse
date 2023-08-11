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
class ResultTypeTest {

    @Test
    void testToValue() {
        assertEquals("Success", ResultType.SUCCESS.toValue());
        assertEquals("Runtime Error", ResultType.RUNTIME_ERROR.toValue());
        assertEquals("Parameters Invalid", ResultType.BAD_PARAMETERS.toValue());
        assertEquals("Terraform Script Invalid",
                ResultType.TERRAFORM_SCRIPT_INVALID.toValue());
        assertEquals("Unprocessable Entity",
                ResultType.UNPROCESSABLE_ENTITY.toValue());
        assertEquals("Response Not Valid",
                ResultType.INVALID_RESPONSE.toValue());
        assertEquals("Failure while connecting to backend",
                ResultType.BACKEND_FAILURE.toValue());
        assertEquals("Credential Capability Not Found",
                ResultType.CREDENTIAL_CAPABILITY_NOT_FOUND.toValue());
        assertEquals("Credentials Not Found",
                ResultType.CREDENTIALS_NOT_FOUND.toValue());
        assertEquals("Credential Variables Not Complete",
                ResultType.CREDENTIALS_VARIABLES_NOT_COMPLETE.toValue());
        assertEquals("Flavor Invalid", ResultType.FLAVOR_NOT_FOUND.toValue());
        assertEquals("Terraform Execution Failed",
                ResultType.TERRAFORM_EXECUTION_FAILED.toValue());
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
        assertEquals("Service Template Already Registered",
                ResultType.SERVICE_TEMPLATE_ALREADY_REGISTERED.toValue());
        assertEquals("Icon Processing Failed",
                ResultType.ICON_PROCESSING_FAILED.toValue());
        assertEquals("Service Template Not Registered",
                ResultType.SERVICE_TEMPLATE_NOT_REGISTERED.toValue());
        assertEquals("Service Deployment Not Found",
                ResultType.SERVICE_DEPLOYMENT_NOT_FOUND.toValue());
        assertEquals("Resource Not Found", ResultType.RESOURCE_NOT_FOUND.toValue());
    }

    @Test
    void testGetResultTypeByValue() {
        assertEquals(ResultType.SUCCESS, ResultType.getResultTypeByValue("Success"));
        assertEquals(ResultType.RUNTIME_ERROR,
                ResultType.getResultTypeByValue("Runtime Error"));
        assertEquals(ResultType.BAD_PARAMETERS,
                ResultType.getResultTypeByValue("Parameters Invalid"));
        assertEquals(ResultType.TERRAFORM_SCRIPT_INVALID,
                ResultType.getResultTypeByValue("Terraform Script Invalid"));
        assertEquals(ResultType.UNPROCESSABLE_ENTITY,
                ResultType.getResultTypeByValue("Unprocessable Entity"));
        assertEquals(ResultType.INVALID_RESPONSE,
                ResultType.getResultTypeByValue("Response Not Valid"));
        assertEquals(ResultType.BACKEND_FAILURE,
                ResultType.getResultTypeByValue("Failure while connecting to backend"));
        assertEquals(ResultType.CREDENTIAL_CAPABILITY_NOT_FOUND,
                ResultType.getResultTypeByValue("Credential Capability Not Found"));
        assertEquals(ResultType.CREDENTIALS_NOT_FOUND,
                ResultType.getResultTypeByValue("Credentials Not Found"));
        assertEquals(ResultType.CREDENTIALS_VARIABLES_NOT_COMPLETE,
                ResultType.getResultTypeByValue("Credential Variables Not Complete"));
        assertEquals(ResultType.FLAVOR_NOT_FOUND,
                ResultType.getResultTypeByValue("Flavor Invalid"));
        assertEquals(ResultType.TERRAFORM_EXECUTION_FAILED,
                ResultType.getResultTypeByValue("Terraform Execution Failed"));
        assertEquals(ResultType.PLUGIN_NOT_FOUND,
                ResultType.getResultTypeByValue("Plugin Not Found"));
        assertEquals(ResultType.DEPLOYER_NOT_FOUND,
                ResultType.getResultTypeByValue("Deployer Not Found"));
        assertEquals(ResultType.TERRAFORM_PROVIDER_NOT_FOUND,
                ResultType.getResultTypeByValue("Terraform Provider Not Found"));
        assertEquals(ResultType.CREDENTIAL_DEFINITIONS_NOT_AVAILABLE,
                ResultType.getResultTypeByValue("No Credential Definition Available"));
        assertEquals(ResultType.SERVICE_STATE_INVALID,
                ResultType.getResultTypeByValue("Invalid Service State"));
        assertEquals(ResultType.RESOURCE_TYPE_INVALID_FOR_MONITORING,
                ResultType.getResultTypeByValue("Resource Invalid For Monitoring"));
        assertEquals(ResultType.UNHANDLED_EXCEPTION,
                ResultType.getResultTypeByValue("Unhandled Exception"));
        assertEquals(ResultType.SERVICE_TEMPLATE_ALREADY_REGISTERED,
                ResultType.getResultTypeByValue("Service Template Already Registered"));
        assertEquals(ResultType.ICON_PROCESSING_FAILED,
                ResultType.getResultTypeByValue("Icon Processing Failed"));
        assertEquals(ResultType.SERVICE_TEMPLATE_NOT_REGISTERED,
                ResultType.getResultTypeByValue("Service Template Not Registered"));
        assertEquals(ResultType.SERVICE_DEPLOYMENT_NOT_FOUND,
                ResultType.getResultTypeByValue("Service Deployment Not Found"));
        assertEquals(ResultType.RESOURCE_NOT_FOUND,
                ResultType.getResultTypeByValue("Resource Not Found"));
        assertNull(ResultType.getResultTypeByValue("null"));
    }

}
