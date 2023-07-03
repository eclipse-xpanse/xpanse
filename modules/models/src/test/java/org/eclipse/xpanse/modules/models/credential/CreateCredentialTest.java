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
import org.junit.jupiter.api.BeforeEach;
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
    private static final Integer timeToLive = 36000;
    private static CreateCredential createCredential;

    @BeforeEach
    void setUp() {
        createCredential = new CreateCredential();
        createCredential.setName(name);
        createCredential.setXpanseUser(xpanseUser);
        createCredential.setCsp(csp);
        createCredential.setDescription(description);
        createCredential.setType(type);
        createCredential.setVariables(variables);
        createCredential.setTimeToLive(timeToLive);
    }

    @Test
    public void testConstructorAndGetters() {
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
        assertEquals(createCredential, createCredential);
        assertEquals(createCredential.hashCode(), createCredential.hashCode());

        Object obj = new Object();
        assertNotEquals(createCredential, obj);
        assertNotEquals(createCredential, null);
        assertNotEquals(createCredential.hashCode(), obj.hashCode());

        CreateCredential createCredential1 = new CreateCredential();
        CreateCredential createCredential2 = new CreateCredential();
        assertNotEquals(createCredential, createCredential1);
        assertNotEquals(createCredential, createCredential2);
        assertEquals(createCredential1, createCredential2);
        assertNotEquals(createCredential.hashCode(), createCredential1.hashCode());
        assertNotEquals(createCredential.hashCode(), createCredential2.hashCode());
        assertEquals(createCredential1.hashCode(), createCredential2.hashCode());

        createCredential1.setName(name);
        assertNotEquals(createCredential, createCredential1);
        assertNotEquals(createCredential1, createCredential2);
        assertNotEquals(createCredential.hashCode(), createCredential1.hashCode());
        assertNotEquals(createCredential1.hashCode(), createCredential2.hashCode());

        createCredential1.setXpanseUser(xpanseUser);
        assertNotEquals(createCredential, createCredential1);
        assertNotEquals(createCredential1, createCredential2);
        assertNotEquals(createCredential.hashCode(), createCredential1.hashCode());
        assertNotEquals(createCredential1.hashCode(), createCredential2.hashCode());

        createCredential1.setDescription(description);
        assertNotEquals(createCredential, createCredential1);
        assertNotEquals(createCredential1, createCredential2);
        assertNotEquals(createCredential.hashCode(), createCredential1.hashCode());
        assertNotEquals(createCredential1.hashCode(), createCredential2.hashCode());

        createCredential1.setCsp(csp);
        assertNotEquals(createCredential, createCredential1);
        assertNotEquals(createCredential, createCredential2);
        assertNotEquals(createCredential1, createCredential2);
        assertNotEquals(createCredential.hashCode(), createCredential1.hashCode());
        assertNotEquals(createCredential1.hashCode(), createCredential2.hashCode());

        createCredential1.setType(type);
        assertNotEquals(createCredential, createCredential1);
        assertNotEquals(createCredential1, createCredential2);
        assertNotEquals(createCredential.hashCode(), createCredential1.hashCode());
        assertNotEquals(createCredential1.hashCode(), createCredential2.hashCode());

        createCredential1.setVariables(variables);
        assertNotEquals(createCredential, createCredential1);
        assertNotEquals(createCredential1, createCredential2);
        assertNotEquals(createCredential.hashCode(), createCredential1.hashCode());
        assertNotEquals(createCredential1.hashCode(), createCredential2.hashCode());

        createCredential1.setTimeToLive(timeToLive);
        assertEquals(createCredential, createCredential1);
        assertNotEquals(createCredential1, createCredential2);
        assertEquals(createCredential.hashCode(), createCredential1.hashCode());
        assertNotEquals(createCredential1.hashCode(), createCredential2.hashCode());

        createCredential1.setTimeToLive(48000);
        assertNotEquals(createCredential, createCredential1);
        assertNotEquals(createCredential1, createCredential2);
        assertNotEquals(createCredential.hashCode(), createCredential1.hashCode());
        assertNotEquals(createCredential1.hashCode(), createCredential2.hashCode());
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
                "timeToLive=36000)";
        assertEquals(expectedToString, createCredential.toString());
    }

}
