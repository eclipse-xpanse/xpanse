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
                BackendSystemType.IDENTITY_PROVIDER,
                BackendSystemType.getByValue("identity provider"));
        assertEquals(BackendSystemType.DATABASE, BackendSystemType.getByValue("database"));
        assertEquals(BackendSystemType.TERRA_BOOT, BackendSystemType.getByValue("terra boot"));
        assertEquals(BackendSystemType.TOFU_MAKER, BackendSystemType.getByValue("tofu maker"));
        assertEquals(BackendSystemType.POLICY_MAN, BackendSystemType.getByValue("policy man"));
        assertEquals(
                BackendSystemType.CACHE_PROVIDER, BackendSystemType.getByValue("Cache Provider"));
        assertEquals(
                BackendSystemType.OPEN_TELEMETRY_COLLECTOR,
                BackendSystemType.getByValue("OpenTelemetry Collector"));
        assertThrows(UnsupportedEnumValueException.class, () -> BackendSystemType.getByValue(null));
    }

    @Test
    public void testToValue() {
        assertEquals("Identity Provider", BackendSystemType.IDENTITY_PROVIDER.toValue());
        assertEquals("Database", BackendSystemType.DATABASE.toValue());
        assertEquals("Terra Boot", BackendSystemType.TERRA_BOOT.toValue());
        assertEquals("Tofu Maker", BackendSystemType.TOFU_MAKER.toValue());
        assertEquals("Policy Man", BackendSystemType.POLICY_MAN.toValue());
        assertEquals("Cache Provider", BackendSystemType.CACHE_PROVIDER.toValue());
        assertEquals(
                "OpenTelemetry Collector", BackendSystemType.OPEN_TELEMETRY_COLLECTOR.toValue());
    }
}
