/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.system.enums;


import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
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

        BackendSystemType type2 = BackendSystemType.getByValue("terraform boot");
        Assertions.assertEquals(type2, BackendSystemType.TERRAFORM_BOOT);

        BackendSystemType type3 = BackendSystemType.getByValue("tofu maker");
        Assertions.assertEquals(type3, BackendSystemType.TOFU_MAKER);

        BackendSystemType type4 = BackendSystemType.getByValue("policy man");
        Assertions.assertEquals(type4, BackendSystemType.POLICY_MAN);

        BackendSystemType type5 = BackendSystemType.getByValue("OpenTelemetry Collector");
        Assertions.assertEquals(type5, BackendSystemType.OPEN_TELEMETRY_COLLECTOR);

        Assertions.assertThrows(UnsupportedEnumValueException.class,
                () -> BackendSystemType.getByValue(null));
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

        String value2 = BackendSystemType.TERRAFORM_BOOT.toValue();
        Assertions.assertEquals("Terraform Boot", value2);
        String name2 = BackendSystemType.TERRAFORM_BOOT.name();
        Assertions.assertEquals("TERRAFORM_BOOT", name2);
        String string2 = BackendSystemType.TERRAFORM_BOOT.toString();
        Assertions.assertEquals("TERRAFORM_BOOT", string2);

        String value3 = BackendSystemType.TOFU_MAKER.toValue();
        Assertions.assertEquals("Tofu Maker", value3);
        String name3 = BackendSystemType.TOFU_MAKER.name();
        Assertions.assertEquals("TOFU_MAKER", name3);
        String string3 = BackendSystemType.TOFU_MAKER.toString();
        Assertions.assertEquals("TOFU_MAKER", string3);

        String value4 = BackendSystemType.POLICY_MAN.toValue();
        Assertions.assertEquals("Policy Man", value4);
        String name4 = BackendSystemType.POLICY_MAN.name();
        Assertions.assertEquals("POLICY_MAN", name4);
        String string4 = BackendSystemType.POLICY_MAN.toString();
        Assertions.assertEquals("POLICY_MAN", string4);

        String value5 = BackendSystemType.OPEN_TELEMETRY_COLLECTOR.toValue();
        Assertions.assertEquals("OpenTelemetry Collector", value5);
        String name5 = BackendSystemType.OPEN_TELEMETRY_COLLECTOR.name();
        Assertions.assertEquals("OPEN_TELEMETRY_COLLECTOR", name5);
        String string5 = BackendSystemType.OPEN_TELEMETRY_COLLECTOR.toString();
        Assertions.assertEquals("OPEN_TELEMETRY_COLLECTOR", string5);
    }

    @Test
    public void testEqualsAndHashCode() {

        Assertions.assertEquals(BackendSystemType.IDENTITY_PROVIDER.hashCode(),
                BackendSystemType.IDENTITY_PROVIDER.hashCode());

        Assertions.assertEquals(BackendSystemType.DATABASE.hashCode(),
                BackendSystemType.DATABASE.hashCode());

        Assertions.assertEquals(BackendSystemType.TERRAFORM_BOOT.hashCode(),
                BackendSystemType.TERRAFORM_BOOT.hashCode());

        Assertions.assertNotEquals(BackendSystemType.IDENTITY_PROVIDER.hashCode(),
                BackendSystemType.DATABASE.hashCode());
    }

}
