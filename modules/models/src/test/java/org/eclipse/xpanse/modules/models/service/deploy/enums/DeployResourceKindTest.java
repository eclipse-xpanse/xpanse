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
    private static final DeployResourceKind securityGroupKind = DeployResourceKind.SECURITY_GROUP;
    private static final DeployResourceKind securityGroupRuleKind =
            DeployResourceKind.SECURITY_GROUP_RULE;
    private static final DeployResourceKind keyPairKind = DeployResourceKind.KEYPAIR;

    @Test
    void testGetByValue() {
        assertEquals(vmKind, DeployResourceKind.UNKNOWN.getByValue("vm"));
        assertEquals(containerKind, DeployResourceKind.UNKNOWN.getByValue("container"));
        assertEquals(publicIpKind, DeployResourceKind.UNKNOWN.getByValue("publicIP"));
        assertEquals(vpcKind, DeployResourceKind.UNKNOWN.getByValue("vpc"));
        assertEquals(volumeKind, DeployResourceKind.UNKNOWN.getByValue("volume"));
        assertEquals(unknownKind, DeployResourceKind.UNKNOWN.getByValue("unknown"));
        assertEquals(securityGroupKind, DeployResourceKind.UNKNOWN.getByValue("Security_Group"));
        assertEquals(securityGroupRuleKind,
                DeployResourceKind.UNKNOWN.getByValue("security_Group_rule"));
        assertEquals(keyPairKind, DeployResourceKind.UNKNOWN.getByValue("Keypair"));
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
        assertEquals("security_group", securityGroupKind.toValue());
        assertEquals("security_group_rule", securityGroupRuleKind.toValue());
        assertEquals("keypair", keyPairKind.toValue());
    }

}
