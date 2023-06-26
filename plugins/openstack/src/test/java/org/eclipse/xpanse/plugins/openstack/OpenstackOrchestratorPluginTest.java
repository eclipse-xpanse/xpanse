/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.openstack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.monitor.MonitorMetricStore;
import org.eclipse.xpanse.modules.monitor.cache.MonitorMetricCacheManager;
import org.eclipse.xpanse.plugins.openstack.constants.OpenstackEnvironmentConstants;
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
            new MetricsManager(new KeystoneManager(null), new ResourcesService(),
                    new GnocchiToXpanseModelConverter(),
                    new AggregationService(new MetricsQueryBuilder()),
                    new MeasuresService(new MetricsQueryBuilder()), null,
                    new MonitorMetricStore(new MonitorMetricCacheManager())));

    @Test
    void getResourceHandler() {
        assertTrue(plugin.getResourceHandler() instanceof OpenstackTerraformResourceHandler);
    }

    @Test
    void getCsp() {
        assertEquals(Csp.OPENSTACK, plugin.getCsp());
    }

    @Test
    void getAvailableCredentialTypes() {
        assertEquals(List.of(CredentialType.VARIABLES), plugin.getAvailableCredentialTypes());
    }

    @Test
    void getCredentialDefinitions() {
        List<AbstractCredentialInfo> result = plugin.getCredentialDefinitions();

        //Verify whether the returned results meet expectations
        assertNotNull(result);
        assertEquals(1, result.size());

        //Verify that the attribute values of the CredentialVariables object match expectations
        CredentialVariables credentialVariables = (CredentialVariables) result.get(0);
        assertEquals(plugin.getCsp(), credentialVariables.getCsp());
        assertNull(credentialVariables.getXpanseUser());
        assertEquals("Variables", credentialVariables.getName());
        assertEquals("Authenticate at the specified URL using an account and password.",
                credentialVariables.getDescription());
        assertEquals(CredentialType.VARIABLES, credentialVariables.getType());

        List<CredentialVariable> variables = credentialVariables.getVariables();
        assertEquals(4, variables.size());

        //Verify the attribute value of the first CredentialVariable
        CredentialVariable authUrlVariable = variables.get(0);
        assertEquals(OpenstackEnvironmentConstants.PROJECT, authUrlVariable.getName());
        assertEquals("The Name of the Tenant or Project to use.", authUrlVariable.getDescription());
        assertTrue(authUrlVariable.getIsMandatory());
        assertFalse(authUrlVariable.getIsSensitive());
    }
}
