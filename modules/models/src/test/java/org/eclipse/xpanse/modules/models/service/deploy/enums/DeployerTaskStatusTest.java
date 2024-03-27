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
 * Test of DeployerTaskStatus.
 */
class DeployerTaskStatusTest {

    @Test
    void testGetByValue() {
        assertEquals(DeployerTaskStatus.INIT, DeployerTaskStatus.getByValue("initial"));
        assertEquals(DeployerTaskStatus.DEPLOY_SUCCESS,
                DeployerTaskStatus.getByValue("success"));
        assertEquals(DeployerTaskStatus.DEPLOY_FAILED,
                DeployerTaskStatus.getByValue("failed"));
        assertEquals(DeployerTaskStatus.DESTROY_SUCCESS,
                DeployerTaskStatus.getByValue("destroy_success"));
        assertEquals(DeployerTaskStatus.DESTROY_FAILED,
                DeployerTaskStatus.getByValue("destroy_failed"));
        assertEquals(DeployerTaskStatus.ROLLBACK_SUCCESS,
                DeployerTaskStatus.getByValue("rollback_success"));
        assertEquals(DeployerTaskStatus.ROLLBACK_FAILED,
                DeployerTaskStatus.getByValue("rollback_failed"));
        assertEquals(DeployerTaskStatus.MODIFYING,
                DeployerTaskStatus.getByValue("modifying"));
        assertEquals(DeployerTaskStatus.MODIFYING_FAILED,
                DeployerTaskStatus.getByValue("modifying_failed"));
        assertEquals(DeployerTaskStatus.MODIFICATION_SUCCESSFUL,
                DeployerTaskStatus.getByValue("modification_successful"));
        assertThrows(UnsupportedEnumValueException.class,
                () -> DeployerTaskStatus.getByValue("unavailable"));
    }

    @Test
    void testToValue() {
        assertEquals("initial", DeployerTaskStatus.INIT.toValue());
        assertEquals("success", DeployerTaskStatus.DEPLOY_SUCCESS.toValue());
        assertEquals("failed", DeployerTaskStatus.DEPLOY_FAILED.toValue());
        assertEquals("destroy_success", DeployerTaskStatus.DESTROY_SUCCESS.toValue());
        assertEquals("destroy_failed", DeployerTaskStatus.DESTROY_FAILED.toValue());
        assertEquals("rollback_success", DeployerTaskStatus.ROLLBACK_SUCCESS.toValue());
        assertEquals("rollback_failed", DeployerTaskStatus.ROLLBACK_FAILED.toValue());
        assertEquals("modifying", DeployerTaskStatus.MODIFYING.toValue());
        assertEquals("modifying_failed", DeployerTaskStatus.MODIFYING_FAILED.toValue());
        assertEquals("modification_successful", DeployerTaskStatus.MODIFICATION_SUCCESSFUL.toValue());
    }

}
