/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.credential;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.junit.jupiter.api.Test;

/**
 * Test of CredentialVariables.
 */
class CredentialVariablesTest {

    private static final Csp csp = Csp.AWS;
    private static final String userId = "user";
    private static final String name = "credential";
    private static final String description = "Test credential";
    private static final CredentialType type = CredentialType.VARIABLES;
    private static final List<CredentialVariable> variables = Arrays.asList(
            new CredentialVariable("variable1", "description1", true, true),
            new CredentialVariable("variable2", "description2", false, false)
    );

    @Test
    public void testConstructorAndGetters() {
        CredentialVariables credentialVariables =
                new CredentialVariables(csp, type, name, description, userId, variables);

        assertEquals(csp, credentialVariables.getCsp());
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
        createCredential.setUserId(userId);
        createCredential.setName(name);
        createCredential.setDescription(description);
        createCredential.setType(type);
        createCredential.setVariables(variables);
        createCredential.setTimeToLive(1800);

        CredentialVariables credentialVariables = new CredentialVariables(createCredential);

        assertEquals(createCredential.getCsp(), credentialVariables.getCsp());
        assertEquals(createCredential.getUserId(), credentialVariables.getUserId());
        assertEquals(createCredential.getName(), credentialVariables.getName());
        assertEquals(createCredential.getDescription(), credentialVariables.getDescription());
        assertEquals(createCredential.getType(), credentialVariables.getType());
        assertEquals(createCredential.getVariables(), credentialVariables.getVariables());
        assertEquals(createCredential.getTimeToLive(), credentialVariables.getTimeToLive());
    }

}
