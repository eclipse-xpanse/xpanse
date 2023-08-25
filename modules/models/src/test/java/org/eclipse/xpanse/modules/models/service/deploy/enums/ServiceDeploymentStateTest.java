/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.deploy.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.junit.jupiter.api.Test;

/**
 * Test of ServiceDeploymentStateTest.
 */
class ServiceDeploymentStateTest {

    @Test
    void testGetByValue() {
        assertEquals(ServiceDeploymentState.DEPLOYING,
                ServiceDeploymentState.DEPLOYING.getByValue("deploying"));
        assertEquals(ServiceDeploymentState.DEPLOY_SUCCESS,
                ServiceDeploymentState.DEPLOY_SUCCESS.getByValue("deployment successful"));
        assertEquals(ServiceDeploymentState.DEPLOY_FAILED,
                ServiceDeploymentState.DEPLOY_FAILED.getByValue("deployment failed"));
        assertEquals(ServiceDeploymentState.DESTROYING,
                ServiceDeploymentState.DESTROYING.getByValue("destroying"));
        assertEquals(ServiceDeploymentState.DESTROY_SUCCESS,
                ServiceDeploymentState.DESTROY_SUCCESS.getByValue("destroy successful"));
        assertEquals(ServiceDeploymentState.DESTROY_FAILED,
                ServiceDeploymentState.DESTROY_FAILED.getByValue("destroy failed"));
        assertThrows(UnsupportedEnumValueException.class,
                () -> ServiceDeploymentState.DESTROY_FAILED.getByValue("unavailable"));
    }

    @Test
    void testToValue() {
        assertEquals("deploying", ServiceDeploymentState.DEPLOYING.toValue());
        assertEquals("deployment successful", ServiceDeploymentState.DEPLOY_SUCCESS.toValue());
        assertEquals("deployment failed", ServiceDeploymentState.DEPLOY_FAILED.toValue());
        assertEquals("destroying", ServiceDeploymentState.DESTROYING.toValue());
        assertEquals("destroy successful", ServiceDeploymentState.DESTROY_SUCCESS.toValue());
        assertEquals("destroy failed", ServiceDeploymentState.DESTROY_FAILED.toValue());
    }

}
