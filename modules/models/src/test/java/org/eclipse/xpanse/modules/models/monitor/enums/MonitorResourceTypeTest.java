/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.monitor.enums;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Test of MonitorResourceType. */
class MonitorResourceTypeTest {

    @Test
    void testToValue() {
        Assertions.assertEquals("cpu", MonitorResourceType.CPU.toValue());
        Assertions.assertEquals("mem", MonitorResourceType.MEM.toValue());
        Assertions.assertEquals(
                "vm_network_incoming", MonitorResourceType.VM_NETWORK_INCOMING.toValue());
        Assertions.assertEquals(
                "vm_network_outgoing", MonitorResourceType.VM_NETWORK_OUTGOING.toValue());
    }

    @Test
    void testGetByValue() {
        Assertions.assertEquals(MonitorResourceType.CPU, MonitorResourceType.getByValue("cpu"));
        Assertions.assertEquals(MonitorResourceType.MEM, MonitorResourceType.getByValue("mem"));
        Assertions.assertEquals(
                MonitorResourceType.VM_NETWORK_INCOMING,
                MonitorResourceType.getByValue("vm_network_incoming"));
        Assertions.assertEquals(
                MonitorResourceType.VM_NETWORK_OUTGOING,
                MonitorResourceType.getByValue("vm_network_outgoing"));
        Assertions.assertThrows(
                UnsupportedEnumValueException.class, () -> MonitorResourceType.getByValue("null"));
    }
}
