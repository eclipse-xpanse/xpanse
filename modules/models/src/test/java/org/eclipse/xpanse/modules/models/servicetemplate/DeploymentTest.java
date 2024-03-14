/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

/**
 * Test of Deployment.
 */
class DeploymentTest {

    private final DeployerKind kind = DeployerKind.TERRAFORM;
    private final String deployer = "deployer";
    private ScriptsRepo scriptsRepo;
    private List<DeployVariable> variables;
    private List<AvailabilityZoneConfig> availabilityZones;
    private final CredentialType credentialType = CredentialType.API_KEY;
    private Deployment test;

    @BeforeEach
    void setUp() {
        DeployVariable deployVariable = new DeployVariable();
        deployVariable.setName("HW_AK");
        variables = List.of(deployVariable);

        AvailabilityZoneConfig availabilityZoneConfig = new AvailabilityZoneConfig();
        availabilityZoneConfig.setDisplayName("displayName");
        availabilityZoneConfig.setVarName("varName");
        availabilityZoneConfig.setMandatory(true);
        availabilityZoneConfig.setDescription("description");
        availabilityZones = List.of(availabilityZoneConfig);

        scriptsRepo = new ScriptsRepo();
        scriptsRepo.setRepoUrl("repoUrl");
        scriptsRepo.setBranch("branch");
        scriptsRepo.setScriptsPath("scriptsPath");

        test = new Deployment();
        test.setKind(kind);
        test.setDeployer(deployer);
        test.setVariables(variables);
        test.setCredentialType(credentialType);
        test.setServiceAvailability(availabilityZones);
        test.setScriptsRepo(scriptsRepo);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(kind, test.getKind());
        assertEquals(deployer, test.getDeployer());
        assertEquals(variables, test.getVariables());
        assertEquals(credentialType, test.getCredentialType());
        assertEquals(availabilityZones, test.getServiceAvailability());
        assertEquals(scriptsRepo, test.getScriptsRepo());
    }

    @Test
    void testEqualsAndHashCode() {
        Object obj = new Object();
        assertThat(test).isNotEqualTo(obj);
        assertThat(test.hashCode()).isNotEqualTo(obj.hashCode());
        Deployment test1 = new Deployment();
        assertThat(test).isNotEqualTo(test1);
        assertThat(test.hashCode()).isNotEqualTo(test1.hashCode());
        Deployment test2 = new Deployment();
        BeanUtils.copyProperties(test, test2);
        assertThat(test).isEqualTo(test2);
        assertThat(test.hashCode()).isEqualTo(test2.hashCode());
    }

    @Test
    void testCanEqual() {
        assertThat(test.canEqual("other")).isFalse();
        assertThat(test.canEqual(new Deployment())).isTrue();
    }


    @Test
    void testToString() {
        String expectedString = "Deployment(" +
                "kind=" + kind +
                ", variables=" + variables +
                ", credentialType=" + credentialType +
                ", serviceAvailability=" + availabilityZones +
                ", deployer=" + deployer +
                ", scriptsRepo=" + scriptsRepo +
                ")";
        assertEquals(expectedString, test.toString());
    }

}
