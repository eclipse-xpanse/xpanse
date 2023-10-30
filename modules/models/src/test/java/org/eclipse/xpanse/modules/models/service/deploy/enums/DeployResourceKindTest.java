/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.deploy.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.junit.jupiter.api.Test;

/**
 * Test of DeployResourceKind.
 */
class DeployResourceKindTest {

    private static final DeployResourceKind vmKind = DeployResourceKind.VM;
    private static final DeployResourceKind containerKind = DeployResourceKind.CONTAINER;
    private static final DeployResourceKind publicIpKind = DeployResourceKind.PUBLIC_IP;
    private static final DeployResourceKind vpcKind = DeployResourceKind.VPC;
    private static final DeployResourceKind volumeKind = DeployResourceKind.VOLUME;
    private static final DeployResourceKind unknownKind = DeployResourceKind.UNKNOWN;

    @Test
    void testGetByValue() {
        assertEquals(vmKind, DeployResourceKind.VM.getByValue("vm"));
        assertEquals(containerKind, DeployResourceKind.CONTAINER.getByValue("container"));
        assertThrows(UnsupportedEnumValueException.class,
                () -> DeployResourceKind.UNKNOWN.getByValue("publicIP"));
        assertEquals(vpcKind, DeployResourceKind.VPC.getByValue("vpc"));
        assertEquals(volumeKind, DeployResourceKind.VOLUME.getByValue("volume"));
        assertEquals(unknownKind, DeployResourceKind.UNKNOWN.getByValue("unknown"));
        assertThrows(UnsupportedEnumValueException.class,
                () -> DeployResourceKind.UNKNOWN.getByValue("unavailable"));
    }

    @Test
    void testToValue() {
        assertEquals("vm", vmKind.toValue());
        assertEquals("container", containerKind.toValue());
        assertEquals("publicIP", publicIpKind.toValue());
        assertEquals("vpc", vpcKind.toValue());
        assertEquals("volume", volumeKind.toValue());
        assertEquals("unknown", unknownKind.toValue());
    }

}
