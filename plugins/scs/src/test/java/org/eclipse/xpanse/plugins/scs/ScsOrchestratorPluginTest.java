/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.scs;

import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.plugins.scs.common.constants.ScsEnvironmentConstants;
import org.eclipse.xpanse.plugins.scs.manage.ScsServersManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ScsOrchestratorPlugin.class})
class ScsOrchestratorPluginTest {

    @Autowired
    private ScsOrchestratorPlugin plugin;

    @MockBean
    private ScsTerraformResourceHandler handler;

    @MockBean
    private ScsServersManager scsServersManager;

    @Test
    void getResourceHandler() {
        assertTrue(plugin.resourceHandlers().get(DeployerKind.TERRAFORM) instanceof ScsTerraformResourceHandler);
    }

    @Test
    void getCsp() {
        assertEquals(Csp.SCS, plugin.getCsp());
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
        assertEquals("Variables", credentialVariables.getName());
        assertEquals("Authenticate at the specified URL using an account and password.",
                credentialVariables.getDescription());
        assertEquals(CredentialType.VARIABLES, credentialVariables.getType());

        List<CredentialVariable> variables = credentialVariables.getVariables();
        assertEquals(4, variables.size());

        //Verify the attribute value of the first CredentialVariable
        CredentialVariable authUrlVariable = variables.get(0);
        assertEquals(ScsEnvironmentConstants.PROJECT, authUrlVariable.getName());
        assertEquals("The Name of the Tenant or Project to use.", authUrlVariable.getDescription());
        assertTrue(authUrlVariable.getIsMandatory());
        assertFalse(authUrlVariable.getIsSensitive());
    }

    @Test
    void testRequiredProperties() {
        assertThat(plugin.requiredProperties()).isEqualTo(Collections.emptyList());
    }
}
