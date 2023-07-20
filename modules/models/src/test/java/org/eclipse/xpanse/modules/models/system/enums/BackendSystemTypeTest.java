/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.system.enums;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test of BackendSystemType.
 */
class BackendSystemTypeTest {

    @Test
    public void testGetByValue() {
        BackendSystemType type = BackendSystemType.getByValue("identityProvider");
        Assertions.assertEquals(type, BackendSystemType.IDENTITY_PROVIDER);

        BackendSystemType type1 = BackendSystemType.getByValue("database");
        Assertions.assertEquals(type1, BackendSystemType.DATABASE);

        BackendSystemType type2 = BackendSystemType.getByValue(null);
        Assertions.assertNull(type2);
    }

    @Test
    public void testToValue() {
        String value = BackendSystemType.IDENTITY_PROVIDER.toValue();
        Assertions.assertEquals("IdentityProvider", value);

        String name = BackendSystemType.IDENTITY_PROVIDER.name();
        Assertions.assertEquals("IDENTITY_PROVIDER", name);

        String string = BackendSystemType.IDENTITY_PROVIDER.toString();
        Assertions.assertEquals("IDENTITY_PROVIDER", string);

        String value1 = BackendSystemType.DATABASE.toValue();
        Assertions.assertEquals("Database", value1);

        String name1 = BackendSystemType.DATABASE.name();
        Assertions.assertEquals("DATABASE", name1);

        String string1 = BackendSystemType.DATABASE.toString();
        Assertions.assertEquals("DATABASE", string1);
    }

    @Test
    public void testEqualsAndHashCode() {

        Assertions.assertEquals(BackendSystemType.IDENTITY_PROVIDER.hashCode(),
                BackendSystemType.IDENTITY_PROVIDER.hashCode());

        Assertions.assertEquals(BackendSystemType.DATABASE.hashCode(),
                BackendSystemType.DATABASE.hashCode());

        Assertions.assertNotEquals(BackendSystemType.IDENTITY_PROVIDER.hashCode(),
                BackendSystemType.DATABASE.hashCode());
    }

}
