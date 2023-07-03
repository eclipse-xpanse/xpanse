/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.credential;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Test of CredentialVariable.
 */
class CredentialVariableTest {
    private static final String name = "variable";
    private static final String description = "Test variable";
    private static final Boolean isMandatory = true;
    private static final Boolean isSensitive = true;
    private static final String value = "value";

    @Test
    public void testConstructorAndGetters() {
        CredentialVariable credentialVariable1 =
                new CredentialVariable(name, description, isMandatory, isSensitive, value);
        credentialVariable1.setValue("HuaweiCloud AK.");

        assertEquals(name, credentialVariable1.getName());
        assertEquals(description, credentialVariable1.getDescription());
        assertEquals(isMandatory, credentialVariable1.getIsMandatory());
        assertEquals(isSensitive, credentialVariable1.getIsSensitive());
        assertEquals("HuaweiCloud AK.", credentialVariable1.getValue());

        CredentialVariable credentialVariable2 =
                new CredentialVariable(name, description, isMandatory, isSensitive);
        assertNull(credentialVariable2.getValue());
    }

    @Test
    public void testConstructorWithDefaultMandatory() {
        CredentialVariable credentialVariable =
                new CredentialVariable(name, description, isSensitive);

        assertEquals(name, credentialVariable.getName());
        assertEquals(description, credentialVariable.getDescription());
        assertTrue(credentialVariable.getIsMandatory());
        assertEquals(isSensitive, credentialVariable.getIsSensitive());
        assertNull(credentialVariable.getValue());
    }

    @Test
    public void testEqualsAndHashCode() {
        CredentialVariable credentialVariable1 =
                new CredentialVariable(name, description, isMandatory, isSensitive, value);
        CredentialVariable credentialVariable2 =
                new CredentialVariable(name, description, isMandatory, isSensitive, value);
        CredentialVariable credentialVariable3 =
                new CredentialVariable("variable2", description, isMandatory, isSensitive, value);
        CredentialVariable credentialVariable4 =
                new CredentialVariable(name, "description2", isMandatory, isSensitive, value);
        CredentialVariable credentialVariable5 =
                new CredentialVariable(name, description, !isMandatory, isSensitive, value);
        CredentialVariable credentialVariable6 =
                new CredentialVariable(name, description, isMandatory, !isSensitive, value);
        CredentialVariable credentialVariable7 =
                new CredentialVariable(name, description, isMandatory, isSensitive, "value2");

        assertEquals(credentialVariable1, credentialVariable1);
        assertEquals(credentialVariable1, credentialVariable2);
        assertNotEquals(credentialVariable1, credentialVariable3);
        assertNotEquals(credentialVariable1, credentialVariable4);
        assertNotEquals(credentialVariable1, credentialVariable5);
        assertNotEquals(credentialVariable1, credentialVariable6);
        assertNotEquals(credentialVariable1, credentialVariable7);
        assertNotEquals(credentialVariable1, null);
        assertNotEquals(credentialVariable1, new Object());

        assertEquals(credentialVariable1.hashCode(), credentialVariable1.hashCode());
        assertEquals(credentialVariable1.hashCode(), credentialVariable2.hashCode());
        assertNotEquals(credentialVariable1.hashCode(), credentialVariable3.hashCode());
        assertNotEquals(credentialVariable1.hashCode(), credentialVariable4.hashCode());
        assertNotEquals(credentialVariable1.hashCode(), credentialVariable5.hashCode());
        assertNotEquals(credentialVariable1.hashCode(), credentialVariable6.hashCode());
        assertNotEquals(credentialVariable1.hashCode(), credentialVariable7.hashCode());
    }

    @Test
    public void testEqualsWithNullValues() {
        Object obj = new Object();
        CredentialVariable credentialVariable1 =
                new CredentialVariable(null, description, isMandatory, isSensitive, value);
        CredentialVariable credentialVariable2 =
                new CredentialVariable(name, null, isMandatory, isSensitive, value);
        CredentialVariable credentialVariable3 =
                new CredentialVariable(name, description, null, isSensitive, value);
        CredentialVariable credentialVariable4 =
                new CredentialVariable(name, description, isMandatory, null, value);
        CredentialVariable credentialVariable5 =
                new CredentialVariable(name, description, isMandatory, isSensitive, null);

        assertEquals(credentialVariable1, credentialVariable1);
        assertNotEquals(credentialVariable1, credentialVariable2);
        assertNotEquals(credentialVariable1, credentialVariable3);
        assertNotEquals(credentialVariable1, credentialVariable4);
        assertNotEquals(credentialVariable1, credentialVariable5);
        assertNotEquals(credentialVariable1, obj);
        assertNotEquals(credentialVariable1, null);

        assertNotEquals(credentialVariable2, credentialVariable3);
        assertNotEquals(credentialVariable2, credentialVariable4);
        assertNotEquals(credentialVariable2, credentialVariable5);

        assertNotEquals(credentialVariable3, credentialVariable4);
        assertNotEquals(credentialVariable3, credentialVariable5);

        assertNotEquals(credentialVariable4, credentialVariable5);

        assertEquals(credentialVariable1.hashCode(), credentialVariable1.hashCode());
        assertNotEquals(credentialVariable1.hashCode(), credentialVariable2.hashCode());
        assertNotEquals(credentialVariable1.hashCode(), credentialVariable3.hashCode());
        assertNotEquals(credentialVariable1.hashCode(), credentialVariable4.hashCode());
        assertNotEquals(credentialVariable1.hashCode(), credentialVariable5.hashCode());
    }

    @Test
    public void testToString() {
        CredentialVariable credentialVariable =
                new CredentialVariable(name, description, isMandatory, isSensitive, value);

        String expectedToString =
                "CredentialVariable(name=variable, description=Test variable, isMandatory=true, " +
                        "isSensitive=true, value=value)";
        assertEquals(expectedToString, credentialVariable.toString());
    }

}
