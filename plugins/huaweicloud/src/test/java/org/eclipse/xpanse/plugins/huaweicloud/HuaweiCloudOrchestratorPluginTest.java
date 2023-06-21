/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.huaweicloud;

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
import org.eclipse.xpanse.plugins.huaweicloud.monitor.constant.HuaweiCloudMonitorConstants;
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
        assertEquals(HuaweiCloudMonitorConstants.IAM, credentialVariables.getName());
        assertEquals("Using The access key and security key authentication.",
                credentialVariables.getDescription());
        assertEquals(CredentialType.VARIABLES, credentialVariables.getType());

        List<CredentialVariable> variables = credentialVariables.getVariables();
        assertEquals(2, variables.size());

        //Verify the attribute value of the first CredentialVariable
        CredentialVariable accessKey = variables.get(0);
        assertEquals(HuaweiCloudMonitorConstants.HW_ACCESS_KEY, accessKey.getName());
        assertEquals("The access key.", accessKey.getDescription());
        assertTrue(accessKey.getIsMandatory());
        assertTrue(accessKey.getIsSensitive());
    }
}
