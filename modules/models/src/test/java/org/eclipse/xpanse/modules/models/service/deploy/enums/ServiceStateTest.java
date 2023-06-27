/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.deploy.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * Test of ServiceState.
 */
class ServiceStateTest {

    @Test
    void testGetByValue() {
        assertEquals(ServiceState.REGISTERED, ServiceState.REGISTERED.getByValue("registered"));
        assertEquals(ServiceState.UPDATED, ServiceState.UPDATED.getByValue("updated"));
        assertEquals(ServiceState.DEPLOYING, ServiceState.DEPLOYING.getByValue("deploying"));
        assertEquals(ServiceState.DEPLOY_SUCCESS,
                ServiceState.DEPLOY_SUCCESS.getByValue("deploy_success"));
        assertEquals(ServiceState.DEPLOY_FAILED,
                ServiceState.DEPLOY_FAILED.getByValue("deploy_failed"));
        assertEquals(ServiceState.DESTROYING, ServiceState.DESTROYING.getByValue("destroying"));
        assertEquals(ServiceState.DESTROY_SUCCESS,
                ServiceState.DESTROY_SUCCESS.getByValue("destroy_success"));
        assertEquals(ServiceState.DESTROY_FAILED,
                ServiceState.DESTROY_FAILED.getByValue("destroy_failed"));
        assertNull(ServiceState.DESTROY_FAILED.getByValue("unavailable"));
    }

    @Test
    void testToValue() {
        assertEquals("REGISTERED", ServiceState.REGISTERED.toValue());
        assertEquals("UPDATED", ServiceState.UPDATED.toValue());
        assertEquals("DEPLOYING", ServiceState.DEPLOYING.toValue());
        assertEquals("DEPLOY_SUCCESS", ServiceState.DEPLOY_SUCCESS.toValue());
        assertEquals("DEPLOY_FAILED", ServiceState.DEPLOY_FAILED.toValue());
        assertEquals("DESTROYING", ServiceState.DESTROYING.toValue());
        assertEquals("DESTROY_SUCCESS", ServiceState.DESTROY_SUCCESS.toValue());
        assertEquals("DESTROY_FAILED", ServiceState.DESTROY_FAILED.toValue());
    }

}
