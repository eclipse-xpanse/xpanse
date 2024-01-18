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
 * Test of TerraformExecState.
 */
class DeployerTaskStatusTest {

    @Test
    void testGetByValue() {
        assertEquals(DeployerTaskStatus.INIT, DeployerTaskStatus.INIT.getByValue("initial"));
        assertEquals(DeployerTaskStatus.DEPLOY_SUCCESS,
                DeployerTaskStatus.DEPLOY_SUCCESS.getByValue("success"));
        assertEquals(DeployerTaskStatus.DEPLOY_FAILED,
                DeployerTaskStatus.DEPLOY_FAILED.getByValue("failed"));
        assertEquals(DeployerTaskStatus.DESTROY_SUCCESS,
                DeployerTaskStatus.DESTROY_SUCCESS.getByValue("destroy_success"));
        assertEquals(DeployerTaskStatus.DESTROY_FAILED,
                DeployerTaskStatus.DESTROY_FAILED.getByValue("destroy_failed"));
        assertThrows(UnsupportedEnumValueException.class,
                () -> DeployerTaskStatus.DESTROY_FAILED.getByValue("unavailable"));
    }

    @Test
    void testToValue() {
        assertEquals("initial", DeployerTaskStatus.INIT.toValue());
        assertEquals("success", DeployerTaskStatus.DEPLOY_SUCCESS.toValue());
        assertEquals("failed", DeployerTaskStatus.DEPLOY_FAILED.toValue());
        assertEquals("destroy_success", DeployerTaskStatus.DESTROY_SUCCESS.toValue());
        assertEquals("destroy_failed", DeployerTaskStatus.DESTROY_FAILED.toValue());
    }

}
