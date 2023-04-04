/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.openstack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.xpanse.modules.models.enums.Csp;
import org.junit.jupiter.api.Test;

public class OpenstackOrchestratorPluginTest {

    private final OpenstackOrchestratorPlugin plugin = new OpenstackOrchestratorPlugin();

    @Test
    void getResourceHandler() {
        assertTrue(plugin.getResourceHandler() instanceof OpenstackTerraformResourceHandler);
    }

    @Test
    void getCsp() {
        assertEquals(Csp.OPENSTACK, plugin.getCsp());
    }
}
