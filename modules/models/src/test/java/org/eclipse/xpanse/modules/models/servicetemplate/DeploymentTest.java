/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of Deployment.
 */
class DeploymentTest {

    private static final DeployerKind kind = DeployerKind.TERRAFORM;
    private static final String deployer = "deployer";
    private static List<DeployVariable> variables;
    private static Deployment deployment;
    private final CredentialType credentialType = CredentialType.API_KEY;

    @BeforeEach
    void setUp() {
        DeployVariable deployVariable = new DeployVariable();
        deployVariable.setName("HW_AK");
        variables = List.of(deployVariable);

        deployment = new Deployment();
        deployment.setKind(kind);
        deployment.setDeployer(deployer);
        deployment.setVariables(variables);
        deployment.setCredentialType(credentialType);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(kind, deployment.getKind());
        assertEquals(deployer, deployment.getDeployer());
        assertEquals(variables, deployment.getVariables());
        assertEquals(credentialType, deployment.getCredentialType());
    }

    @Test
    void testEqualsAndHashCode() {
        assertEquals(deployment.hashCode(), deployment.hashCode());

        Object obj = new Object();
        assertNotEquals(deployment, obj);
        assertNotEquals(deployment, null);
        assertNotEquals(deployment.hashCode(), obj.hashCode());

        Deployment deployment1 = new Deployment();
        Deployment deployment2 = new Deployment();
        assertNotEquals(deployment, deployment1);
        assertNotEquals(deployment, deployment2);
        assertEquals(deployment1, deployment2);
        assertNotEquals(deployment.hashCode(), deployment1.hashCode());
        assertNotEquals(deployment.hashCode(), deployment2.hashCode());
        assertEquals(deployment1.hashCode(), deployment2.hashCode());

        deployment1.setKind(kind);
        assertNotEquals(deployment, deployment1);
        assertNotEquals(deployment1, deployment2);
        assertNotEquals(deployment.hashCode(), deployment1.hashCode());
        assertNotEquals(deployment1.hashCode(), deployment2.hashCode());

        deployment1.setVariables(variables);
        assertNotEquals(deployment, deployment1);
        assertNotEquals(deployment1, deployment2);
        assertNotEquals(deployment.hashCode(), deployment1.hashCode());
        assertNotEquals(deployment1.hashCode(), deployment2.hashCode());

        deployment1.setDeployer(deployer);
        assertNotEquals(deployment, deployment1);
        assertNotEquals(deployment1, deployment2);
        assertNotEquals(deployment.hashCode(), deployment1.hashCode());
        assertNotEquals(deployment1.hashCode(), deployment2.hashCode());

        deployment1.setCredentialType(credentialType);
        assertEquals(deployment, deployment1);
        assertNotEquals(deployment1, deployment2);
        assertEquals(deployment.hashCode(), deployment1.hashCode());
        assertNotEquals(deployment1.hashCode(), deployment2.hashCode());
    }

    @Test
    void testToString() {
        String expectedString = "Deployment(" +
                "kind=" + kind +
                ", variables=" + variables +
                ", credentialType=" + credentialType +
                ", deployer=" + deployer +
                ", scriptsRepo=null" +
                ")";
        assertEquals(expectedString, deployment.toString());
    }

}
