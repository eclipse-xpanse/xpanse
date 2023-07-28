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
                ServiceDeploymentState.DEPLOY_SUCCESS.getByValue("deploy_success"));
        assertEquals(ServiceDeploymentState.DEPLOY_FAILED,
                ServiceDeploymentState.DEPLOY_FAILED.getByValue("deploy_failed"));
        assertEquals(ServiceDeploymentState.DESTROYING,
                ServiceDeploymentState.DESTROYING.getByValue("destroying"));
        assertEquals(ServiceDeploymentState.DESTROY_SUCCESS,
                ServiceDeploymentState.DESTROY_SUCCESS.getByValue("destroy_success"));
        assertEquals(ServiceDeploymentState.DESTROY_FAILED,
                ServiceDeploymentState.DESTROY_FAILED.getByValue("destroy_failed"));
        assertThrows(UnsupportedEnumValueException.class,
                () -> ServiceDeploymentState.DESTROY_FAILED.getByValue("unavailable"));
    }

    @Test
    void testToValue() {
        assertEquals("DEPLOYING", ServiceDeploymentState.DEPLOYING.toValue());
        assertEquals("DEPLOY_SUCCESS", ServiceDeploymentState.DEPLOY_SUCCESS.toValue());
        assertEquals("DEPLOY_FAILED", ServiceDeploymentState.DEPLOY_FAILED.toValue());
        assertEquals("DESTROYING", ServiceDeploymentState.DESTROYING.toValue());
        assertEquals("DESTROY_SUCCESS", ServiceDeploymentState.DESTROY_SUCCESS.toValue());
        assertEquals("DESTROY_FAILED", ServiceDeploymentState.DESTROY_FAILED.toValue());
    }

}
