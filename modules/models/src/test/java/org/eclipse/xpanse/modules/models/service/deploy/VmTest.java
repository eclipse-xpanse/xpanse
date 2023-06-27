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
    private static final Vm vm = new Vm();

    @BeforeEach
    void setUp() {
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
        Vm vm2 = new Vm();
        vm2.setIp(ip);
        vm2.setResourceId(resourceId);
        vm2.setName(name);
        vm2.setKind(kind);
        vm2.setProperties(properties);

        Vm vm3 = new Vm();
        vm3.setIp("192.168.0.0");
        vm3.setResourceId("20424910-5f64-4984-84f0-6013c63c64f5");
        vm3.setName("name");
        vm3.setKind(DeployResourceKind.PUBLIC_IP);
        vm3.setProperties(properties);

        assertEquals(vm, vm);
        assertEquals(vm, vm2);
        assertNotEquals(vm, vm3);

        assertEquals(vm.hashCode(), vm.hashCode());
        assertEquals(vm.hashCode(), vm2.hashCode());
        assertNotEquals(vm.hashCode(), vm3.hashCode());
    }

    @Test
    void testToString() {
        String expectedToString =
                "Vm(super=DeployResource(resourceId=" + resourceId + ", name=" + name + ", kind=" +
                        kind + ", properties=" + properties + "), ip=" + ip + ")";
        assertEquals(expectedToString, vm.toString());
    }

}
