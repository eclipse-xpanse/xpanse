/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.system.enums;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test of HealthStatus.
 */
class IdentityProviderTypeTest {

    @Test
    public void testGetByValue() {
        IdentityProviderType type = IdentityProviderType.getByValue("zitadel");
        Assertions.assertEquals(type, IdentityProviderType.ZITADEL);

        IdentityProviderType type1 = IdentityProviderType.getByValue("ZITAdel");
        Assertions.assertEquals(type1, IdentityProviderType.ZITADEL);

        IdentityProviderType type2 = IdentityProviderType.getByValue(null);
        Assertions.assertNull(type2);
    }

    @Test
    public void testToValue() {
        String value = IdentityProviderType.ZITADEL.toValue();
        Assertions.assertEquals("zitadel", value);

        String name = IdentityProviderType.ZITADEL.name();
        Assertions.assertEquals("ZITADEL", name);

        String string = IdentityProviderType.ZITADEL.toString();
        Assertions.assertEquals("ZITADEL", string);
    }

    @Test
    public void testEqualsAndHashCode() {
        Assertions.assertEquals(IdentityProviderType.ZITADEL.hashCode(),
                IdentityProviderType.ZITADEL.hashCode());
    }

}
