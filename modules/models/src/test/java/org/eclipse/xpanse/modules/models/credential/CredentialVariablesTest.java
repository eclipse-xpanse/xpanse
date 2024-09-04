/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.credential;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.junit.jupiter.api.Test;

/**
 * Test of CredentialVariables.
 */
class CredentialVariablesTest {

    private final Csp csp = Csp.HUAWEI_CLOUD;
    private final String site = "site";
    private final String userId = "user";
    private final String name = "credential";
    private final String description = "Test credential";
    private final CredentialType type = CredentialType.VARIABLES;
    private final List<CredentialVariable> variables = Arrays.asList(
            new CredentialVariable("variable1", "description1", true, true),
            new CredentialVariable("variable2", "description2", false, false)
    );

    @Test
    public void testConstructorAndGetters() {
        CredentialVariables credentialVariables =
                new CredentialVariables(csp, site, type, name, description, userId, variables);
        assertEquals(csp, credentialVariables.getCsp());
        assertEquals(site, credentialVariables.getSite());
        assertEquals(userId, credentialVariables.getUserId());
        assertEquals(name, credentialVariables.getName());
        assertEquals(description, credentialVariables.getDescription());
        assertEquals(type, credentialVariables.getType());
        assertEquals(variables, credentialVariables.getVariables());
    }

    @Test
    public void testConstructorWithCreateCredential() {
        CreateCredential createCredential = new CreateCredential();
        createCredential.setCsp(csp);
        createCredential.setSite(site);
        createCredential.setUserId(userId);
        createCredential.setName(name);
        createCredential.setDescription(description);
        createCredential.setType(type);
        createCredential.setVariables(variables);
        createCredential.setTimeToLive(1800);

        CredentialVariables credentialVariables = new CredentialVariables(createCredential);

        assertEquals(createCredential.getCsp(), credentialVariables.getCsp());
        assertEquals(createCredential.getSite(), credentialVariables.getSite());
        assertEquals(createCredential.getUserId(), credentialVariables.getUserId());
        assertEquals(createCredential.getName(), credentialVariables.getName());
        assertEquals(createCredential.getDescription(), credentialVariables.getDescription());
        assertEquals(createCredential.getType(), credentialVariables.getType());
        assertEquals(createCredential.getVariables(), credentialVariables.getVariables());
        assertEquals(createCredential.getTimeToLive(), credentialVariables.getTimeToLive());
    }

}
