/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.flexibleengine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.monitor.ServiceMetricsStore;
import org.eclipse.xpanse.modules.monitor.cache.ServiceMetricsCacheManager;
import org.eclipse.xpanse.plugins.flexibleengine.monitor.constant.FlexibleEngineMonitorConstants;
import org.eclipse.xpanse.plugins.flexibleengine.monitor.models.FlexibleEngineMonitorClient;
import org.eclipse.xpanse.plugins.flexibleengine.monitor.utils.FlexibleEngineMetricsConverter;
import org.eclipse.xpanse.plugins.flexibleengine.monitor.utils.FlexibleEngineMetricsService;
import org.eclipse.xpanse.plugins.flexibleengine.monitor.utils.RetryTemplateService;
import org.junit.jupiter.api.Test;

class FlexibleEngineOrchestratorPluginTest {

    private final FlexibleEngineOrchestratorPlugin plugin = new FlexibleEngineOrchestratorPlugin(
            new FlexibleEngineMetricsService(new FlexibleEngineMonitorClient(),
                    new FlexibleEngineMetricsConverter(),
                    new ServiceMetricsStore(new ServiceMetricsCacheManager()),
                    new RetryTemplateService(), null), new FlexibleEngineTerraformResourceHandler());

    @Test
    void getResourceHandler() {
        assertTrue(plugin.getResourceHandler() instanceof FlexibleEngineTerraformResourceHandler);
    }

    @Test
    void getCsp() {
        assertEquals(Csp.FLEXIBLE_ENGINE, plugin.getCsp());
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
        assertNull(credentialVariables.getUserId());
        assertEquals("AK_SK", credentialVariables.getName());
        assertEquals("The access key and security key.",
                credentialVariables.getDescription());
        assertEquals(CredentialType.VARIABLES, credentialVariables.getType());

        List<CredentialVariable> variables = credentialVariables.getVariables();
        assertEquals(2, variables.size());

        //Verify the attribute value of the first CredentialVariable
        CredentialVariable accessKey = variables.get(0);
        assertEquals(FlexibleEngineMonitorConstants.OS_ACCESS_KEY, accessKey.getName());
        assertEquals("The access key.", accessKey.getDescription());
        assertTrue(accessKey.getIsMandatory());
        assertTrue(accessKey.getIsSensitive());
    }
}
