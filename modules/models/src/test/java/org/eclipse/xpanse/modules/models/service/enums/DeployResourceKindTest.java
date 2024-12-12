/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.junit.jupiter.api.Test;

/** Test of DeployResourceKind. */
class DeployResourceKindTest {

    private static final DeployResourceKind vmKind = DeployResourceKind.VM;
    private static final DeployResourceKind containerKind = DeployResourceKind.CONTAINER;
    private static final DeployResourceKind publicIpKind = DeployResourceKind.PUBLIC_IP;
    private static final DeployResourceKind vpcKind = DeployResourceKind.VPC;
    private static final DeployResourceKind volumeKind = DeployResourceKind.VOLUME;
    private static final DeployResourceKind unknownKind = DeployResourceKind.UNKNOWN;
    private static final DeployResourceKind securityGroupKind = DeployResourceKind.SECURITY_GROUP;
    private static final DeployResourceKind securityGroupRuleKind =
            DeployResourceKind.SECURITY_GROUP_RULE;
    private static final DeployResourceKind keyPairKind = DeployResourceKind.KEYPAIR;

    @Test
    void testGetByValue() {
        assertEquals(vmKind, DeployResourceKind.getByValue("vm"));
        assertEquals(containerKind, DeployResourceKind.getByValue("container"));
        assertEquals(publicIpKind, DeployResourceKind.getByValue("publicIP"));
        assertEquals(vpcKind, DeployResourceKind.getByValue("vpc"));
        assertEquals(volumeKind, DeployResourceKind.getByValue("volume"));
        assertEquals(unknownKind, DeployResourceKind.getByValue("unknown"));
        assertEquals(securityGroupKind, DeployResourceKind.getByValue("Security_Group"));
        assertEquals(securityGroupRuleKind, DeployResourceKind.getByValue("security_Group_rule"));
        assertEquals(keyPairKind, DeployResourceKind.getByValue("Keypair"));
        assertThrows(
                UnsupportedEnumValueException.class,
                () -> DeployResourceKind.getByValue("unavailable"));
    }

    @Test
    void testToValue() {
        assertEquals("vm", vmKind.toValue());
        assertEquals("container", containerKind.toValue());
        assertEquals("publicIP", publicIpKind.toValue());
        assertEquals("vpc", vpcKind.toValue());
        assertEquals("volume", volumeKind.toValue());
        assertEquals("unknown", unknownKind.toValue());
        assertEquals("security_group", securityGroupKind.toValue());
        assertEquals("security_group_rule", securityGroupRuleKind.toValue());
        assertEquals("keypair", keyPairKind.toValue());
    }
}
