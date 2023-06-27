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
 * Test of TerraformExecState.
 */
class TerraformExecStateTest {

    @Test
    void testGetByValue() {
        assertEquals(TerraformExecState.INIT, TerraformExecState.INIT.getByValue("initial"));
        assertEquals(TerraformExecState.DEPLOY_SUCCESS,
                TerraformExecState.DEPLOY_SUCCESS.getByValue("success"));
        assertEquals(TerraformExecState.DEPLOY_FAILED,
                TerraformExecState.DEPLOY_FAILED.getByValue("failed"));
        assertEquals(TerraformExecState.DESTROY_SUCCESS,
                TerraformExecState.DESTROY_SUCCESS.getByValue("destroy_success"));
        assertEquals(TerraformExecState.DESTROY_FAILED,
                TerraformExecState.DESTROY_FAILED.getByValue("destroy_failed"));
        assertNull(TerraformExecState.DESTROY_FAILED.getByValue("unavailable"));
    }

    @Test
    void testToValue() {
        assertEquals("initial", TerraformExecState.INIT.toValue());
        assertEquals("success", TerraformExecState.DEPLOY_SUCCESS.toValue());
        assertEquals("failed", TerraformExecState.DEPLOY_FAILED.toValue());
        assertEquals("destroy_success", TerraformExecState.DESTROY_SUCCESS.toValue());
        assertEquals("destroy_failed", TerraformExecState.DESTROY_FAILED.toValue());
    }

}
