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

/** Test of Deployment. */
class DeploymentTest {
    private final String deployer = "deployer";
    private final CredentialType credentialType = CredentialType.API_KEY;
    private DeployerTool deployerTool;
    private ScriptsRepo scriptsRepo;
    private List<DeployVariable> variables;
    private List<AvailabilityZoneConfig> availabilityZoneConfig;
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
        this.availabilityZoneConfig = List.of(availabilityZoneConfig);

        scriptsRepo = new ScriptsRepo();
        scriptsRepo.setRepoUrl("repoUrl");
        scriptsRepo.setBranch("branch");
        scriptsRepo.setScriptsPath("scriptsPath");

        deployerTool = new DeployerTool();
        deployerTool.setKind(DeployerKind.TERRAFORM);
        deployerTool.setVersion("=1.6.1");

        test = new Deployment();
        test.setDeployer(deployer);
        test.setDeployerTool(deployerTool);
        test.setVariables(variables);
        test.setCredentialType(credentialType);
        test.setServiceAvailabilityConfig(this.availabilityZoneConfig);
        test.setScriptsRepo(scriptsRepo);
    }

    @Test
    void testGetters() {
        assertEquals(deployerTool, test.getDeployerTool());
        assertEquals(deployer, test.getDeployer());
        assertEquals(variables, test.getVariables());
        assertEquals(credentialType, test.getCredentialType());
        assertEquals(availabilityZoneConfig, test.getServiceAvailabilityConfig());
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
        String expectedString =
                "Deployment(deployerTool="
                        + deployerTool
                        + ", variables="
                        + variables
                        + ", credentialType="
                        + credentialType
                        + ", serviceAvailabilityConfig="
                        + availabilityZoneConfig
                        + ", deployer="
                        + deployer
                        + ", scriptsRepo="
                        + scriptsRepo
                        + ")";
        assertEquals(expectedString, test.toString());
    }
}
