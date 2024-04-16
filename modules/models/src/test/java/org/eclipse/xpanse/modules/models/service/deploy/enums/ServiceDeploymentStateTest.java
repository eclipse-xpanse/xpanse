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
                ServiceDeploymentState.getByValue("deploying"));
        assertEquals(ServiceDeploymentState.DEPLOY_SUCCESS,
                ServiceDeploymentState.getByValue("deployment successful"));
        assertEquals(ServiceDeploymentState.DEPLOY_FAILED,
                ServiceDeploymentState.getByValue("deployment failed"));
        assertEquals(ServiceDeploymentState.MODIFYING,
                ServiceDeploymentState.getByValue("modifying"));
        assertEquals(ServiceDeploymentState.MODIFICATION_SUCCESSFUL,
                ServiceDeploymentState.getByValue("modification successful"));
        assertEquals(ServiceDeploymentState.MODIFICATION_FAILED,
                ServiceDeploymentState.getByValue("modification failed"));
        assertEquals(ServiceDeploymentState.DESTROYING,
                ServiceDeploymentState.getByValue("destroying"));
        assertEquals(ServiceDeploymentState.DESTROY_SUCCESS,
                ServiceDeploymentState.getByValue("destroy successful"));
        assertEquals(ServiceDeploymentState.DESTROY_FAILED,
                ServiceDeploymentState.getByValue("destroy failed"));
        assertEquals(ServiceDeploymentState.MIGRATING,
                ServiceDeploymentState.getByValue("migrating"));
        assertEquals(ServiceDeploymentState.MIGRATION_SUCCESS,
                ServiceDeploymentState.getByValue("migration successful"));
        assertEquals(ServiceDeploymentState.MIGRATION_FAILED,
                ServiceDeploymentState.getByValue("migration failed"));
        assertEquals(ServiceDeploymentState.MANUAL_CLEANUP_REQUIRED,
                ServiceDeploymentState.getByValue("manual cleanup required"));
        assertEquals(ServiceDeploymentState.ROLLBACK_FAILED,
                ServiceDeploymentState.getByValue("rollback failed"));
        assertThrows(UnsupportedEnumValueException.class,
                () -> ServiceDeploymentState.getByValue("unavailable"));
    }

    @Test
    void testToValue() {
        assertEquals("deploying", ServiceDeploymentState.DEPLOYING.toValue());
        assertEquals("deployment successful", ServiceDeploymentState.DEPLOY_SUCCESS.toValue());
        assertEquals("deployment failed", ServiceDeploymentState.DEPLOY_FAILED.toValue());
        assertEquals("modifying", ServiceDeploymentState.MODIFYING.toValue());
        assertEquals("modification successful",
                ServiceDeploymentState.MODIFICATION_SUCCESSFUL.toValue());
        assertEquals("modification failed", ServiceDeploymentState.MODIFICATION_FAILED.toValue());
        assertEquals("destroying", ServiceDeploymentState.DESTROYING.toValue());
        assertEquals("destroy successful", ServiceDeploymentState.DESTROY_SUCCESS.toValue());
        assertEquals("destroy failed", ServiceDeploymentState.DESTROY_FAILED.toValue());
        assertEquals("migrating", ServiceDeploymentState.MIGRATING.toValue());
        assertEquals("migration successful", ServiceDeploymentState.MIGRATION_SUCCESS.toValue());
        assertEquals("migration failed", ServiceDeploymentState.MIGRATION_FAILED.toValue());
        assertEquals("manual cleanup required",
                ServiceDeploymentState.MANUAL_CLEANUP_REQUIRED.toValue());
        assertEquals("rollback failed", ServiceDeploymentState.ROLLBACK_FAILED.toValue());
    }

}
