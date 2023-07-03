/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.deploy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployResourceKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of Vm.
 */
class VmTest {

    private static final String ip = "192.168.0.1";
    private static final String resourceId =
            UUID.fromString("f0dcb6ea-cbe7-4c88-9c94-a5d00e82a4f2").toString();
    private static final String name = "vm";
    private static final DeployResourceKind kind = DeployResourceKind.VM;
    private static final Map<String, String> properties = Collections.singletonMap("key", "value");
    private static Vm vm;

    @BeforeEach
    void setUp() {
        vm = new Vm();
        vm.setIp(ip);
        vm.setResourceId(resourceId);
        vm.setName(name);
        vm.setKind(kind);
        vm.setProperties(properties);
    }

    @Test
    void testConstructorAndGetters() {
        assertEquals(ip, vm.getIp());
        assertEquals(resourceId, vm.getResourceId());
        assertEquals(name, vm.getName());
        assertEquals(kind, vm.getKind());
        assertEquals(properties, vm.getProperties());
    }

    @Test
    void testEqualsAndHashCode() {
        assertEquals(vm, vm);
        assertEquals(vm.hashCode(), vm.hashCode());

        Object obj = new Object();
        assertNotEquals(vm, obj);
        assertNotEquals(vm, null);
        assertNotEquals(vm.hashCode(), obj.hashCode());

        Vm vm1 = new Vm();
        Vm vm2 = new Vm();
        assertNotEquals(vm, vm1);
        assertNotEquals(vm, vm2);
        assertEquals(vm1, vm2);
        assertNotEquals(vm.hashCode(), vm1.hashCode());
        assertNotEquals(vm.hashCode(), vm2.hashCode());
        assertEquals(vm1.hashCode(), vm2.hashCode());

        vm1.setIp(ip);
        assertNotEquals(vm, vm1);
        assertNotEquals(vm1, vm2);
        assertNotEquals(vm.hashCode(), vm1.hashCode());
        assertNotEquals(vm1.hashCode(), vm2.hashCode());

        vm1.setResourceId(resourceId);
        assertNotEquals(vm, vm1);
        assertNotEquals(vm1, vm2);
        assertNotEquals(vm.hashCode(), vm1.hashCode());
        assertNotEquals(vm1.hashCode(), vm2.hashCode());

        vm1.setName(name);
        assertNotEquals(vm, vm1);
        assertNotEquals(vm1, vm2);
        assertNotEquals(vm.hashCode(), vm1.hashCode());
        assertNotEquals(vm1.hashCode(), vm2.hashCode());

        vm1.setKind(kind);
        assertNotEquals(vm, vm1);
        assertNotEquals(vm1, vm2);
        assertNotEquals(vm.hashCode(), vm1.hashCode());
        assertNotEquals(vm1.hashCode(), vm2.hashCode());

        vm1.setProperties(properties);
        assertEquals(vm, vm1);
        assertNotEquals(vm1, vm2);
        assertEquals(vm.hashCode(), vm1.hashCode());
        assertNotEquals(vm1.hashCode(), vm2.hashCode());
    }

    @Test
    void testToString() {
        String expectedToString =
                "Vm(super=DeployResource(resourceId=" + resourceId + ", name=" + name + ", kind=" +
                        kind + ", properties=" + properties + "), ip=" + ip + ")";
        assertEquals(expectedToString, vm.toString());
    }

}
