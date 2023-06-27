/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.credential;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.junit.jupiter.api.Test;

/**
 * Test of CreateCredential.
 */
class CreateCredentialTest {

    private static final String name = "credential";
    private static final String xpanseUser = "user";
    private static final Csp csp = Csp.AWS;
    private static final String description = "Test credential";
    private static final CredentialType type = CredentialType.VARIABLES;
    private static final List<CredentialVariable> variables =
            List.of(new CredentialVariable("variable1", "description1", false, false),
                    new CredentialVariable("variable2", "description2", true, true));
    private static final Integer timeToLive = 3600;

    @Test
    public void testConstructorAndGetters() {

        CreateCredential createCredential = new CreateCredential();
        createCredential.setName(name);
        createCredential.setXpanseUser(xpanseUser);
        createCredential.setCsp(csp);
        createCredential.setDescription(description);
        createCredential.setType(type);
        createCredential.setVariables(variables);
        createCredential.setTimeToLive(timeToLive);

        assertEquals(name, createCredential.getName());
        assertEquals(xpanseUser, createCredential.getXpanseUser());
        assertEquals(csp, createCredential.getCsp());
        assertEquals(description, createCredential.getDescription());
        assertEquals(type, createCredential.getType());
        assertEquals(variables, createCredential.getVariables());
        assertEquals(timeToLive, createCredential.getTimeToLive());
    }

    @Test
    public void testEqualsAndHashCode() {
        CreateCredential createCredential1 = new CreateCredential();
        createCredential1.setName(name);
        createCredential1.setXpanseUser(xpanseUser);
        createCredential1.setCsp(csp);
        createCredential1.setDescription(description);
        createCredential1.setType(type);
        createCredential1.setVariables(variables);
        createCredential1.setTimeToLive(timeToLive);

        CreateCredential createCredential2 = new CreateCredential();
        createCredential2.setName(name);
        createCredential2.setXpanseUser(xpanseUser);
        createCredential2.setCsp(csp);
        createCredential2.setDescription(description);
        createCredential2.setType(type);
        createCredential2.setVariables(variables);
        createCredential2.setTimeToLive(timeToLive);

        CreateCredential createCredential3 = new CreateCredential();
        createCredential3.setName("credential3");
        createCredential3.setXpanseUser(xpanseUser);
        createCredential3.setCsp(csp);
        createCredential3.setDescription(description);
        createCredential3.setType(type);
        createCredential3.setVariables(variables);
        createCredential3.setTimeToLive(timeToLive);

        assertEquals(createCredential1, createCredential1);
        assertEquals(createCredential1, createCredential2);
        assertNotEquals(createCredential1, createCredential3);

        assertEquals(createCredential1.hashCode(), createCredential1.hashCode());
        assertEquals(createCredential1.hashCode(), createCredential2.hashCode());
        assertNotEquals(createCredential1.hashCode(), createCredential3.hashCode());
    }

    @Test
    void testToString() {
        CreateCredential createCredential = new CreateCredential();
        createCredential.setName(name);
        createCredential.setXpanseUser(xpanseUser);
        createCredential.setCsp(csp);
        createCredential.setDescription(description);
        createCredential.setType(type);
        createCredential.setVariables(variables);
        createCredential.setTimeToLive(timeToLive);

        String expectedToString = "CreateCredential(" +
                "name=credential, " +
                "xpanseUser=user, " +
                "csp=AWS, " +
                "description=Test credential, " +
                "type=VARIABLES, " +
                "variables=[CredentialVariable(name=variable1, description=description1, " +
                "isMandatory=false, isSensitive=false, value=null), " +
                "CredentialVariable(name=variable2, description=description2, " +
                "isMandatory=true, isSensitive=true, value=null)], " +
                "timeToLive=3600)";
        assertEquals(expectedToString, createCredential.toString());
    }

}
