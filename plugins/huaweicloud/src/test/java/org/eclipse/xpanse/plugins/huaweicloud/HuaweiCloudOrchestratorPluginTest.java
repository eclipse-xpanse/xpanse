/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.huaweicloud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.xpanse.modules.models.enums.Csp;
import org.junit.jupiter.api.Test;

public class HuaweiCloudOrchestratorPluginTest {

    private final HuaweiCloudOrchestratorPlugin plugin = new HuaweiCloudOrchestratorPlugin();

    @Test
    void getResourceHandler() {
        assertTrue(plugin.getResourceHandler() instanceof HuaweiTerraformResourceHandler);
    }

    @Test
    void getCsp() {
        assertEquals(Csp.HUAWEI, plugin.getCsp());
    }
}
