/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.system.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.junit.jupiter.api.Test;

/** Test of BackendSystemType. */
class BackendSystemTypeTest {

    @Test
    public void testGetByValue() {
        assertEquals(
                BackendSystemType.getByValue("identity provider"),
                BackendSystemType.IDENTITY_PROVIDER);
        assertEquals(BackendSystemType.getByValue("database"), BackendSystemType.DATABASE);
        assertEquals(
                BackendSystemType.getByValue("terraform boot"), BackendSystemType.TERRAFORM_BOOT);
        assertEquals(BackendSystemType.getByValue("tofu maker"), BackendSystemType.TOFU_MAKER);
        assertEquals(BackendSystemType.getByValue("policy man"), BackendSystemType.POLICY_MAN);
        assertEquals(
                BackendSystemType.getByValue("Cache Provider"), BackendSystemType.CACHE_PROVIDER);
        assertEquals(
                BackendSystemType.getByValue("OpenTelemetry Collector"),
                BackendSystemType.OPEN_TELEMETRY_COLLECTOR);
        assertThrows(UnsupportedEnumValueException.class, () -> BackendSystemType.getByValue(null));
    }

    @Test
    public void testToValue() {
        assertEquals("Identity Provider", BackendSystemType.IDENTITY_PROVIDER.toValue());
        assertEquals("Database", BackendSystemType.DATABASE.toValue());
        assertEquals("Terraform Boot", BackendSystemType.TERRAFORM_BOOT.toValue());
        assertEquals("Tofu Maker", BackendSystemType.TOFU_MAKER.toValue());
        assertEquals("Policy Man", BackendSystemType.POLICY_MAN.toValue());
        assertEquals("Cache Provider", BackendSystemType.CACHE_PROVIDER.toValue());
        assertEquals(
                "OpenTelemetry Collector", BackendSystemType.OPEN_TELEMETRY_COLLECTOR.toValue());
    }
}
