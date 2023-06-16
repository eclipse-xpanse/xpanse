/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.openstack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.plugins.openstack.monitor.gnocchi.api.AggregationService;
import org.eclipse.xpanse.plugins.openstack.monitor.gnocchi.api.MeasuresService;
import org.eclipse.xpanse.plugins.openstack.monitor.gnocchi.api.ResourcesService;
import org.eclipse.xpanse.plugins.openstack.monitor.gnocchi.api.utils.MetricsQueryBuilder;
import org.eclipse.xpanse.plugins.openstack.monitor.keystone.KeystoneManager;
import org.eclipse.xpanse.plugins.openstack.monitor.utils.GnocchiToXpanseModelConverter;
import org.eclipse.xpanse.plugins.openstack.monitor.utils.MetricsManager;
import org.junit.jupiter.api.Test;

class OpenstackOrchestratorPluginTest {

    private final OpenstackOrchestratorPlugin plugin = new OpenstackOrchestratorPlugin(
            new MetricsManager(new KeystoneManager(), new ResourcesService(),
                    new GnocchiToXpanseModelConverter(), new AggregationService(new MetricsQueryBuilder()),
                    new MeasuresService(new MetricsQueryBuilder()), null));

    @Test
    void getResourceHandler() {
        assertTrue(plugin.getResourceHandler() instanceof OpenstackTerraformResourceHandler);
    }

    @Test
    void getCsp() {
        assertEquals(Csp.OPENSTACK, plugin.getCsp());
    }
}
