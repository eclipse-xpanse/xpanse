/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test of AnsibleScriptConfig.
 */
public class AnsibleScriptConfigTest {

    private final String playbookName = "kafka-container-manage.yml";
    private final String virtualEnv = "/workspace";
    private final String pythonVersion = "3.10";
    private final Boolean isPrepareAnsibleEnvironment = Boolean.TRUE;
    private final String repoUrl = "https://github.com/";
    private final String branch = "feature/testAnsible";
    private final String requirementsFile = "requirements.txt";
    private final String galaxyFile = "galaxyFile";
    private final Boolean ansibleInventoryRequired = Boolean.FALSE;

    private AnsibleScriptConfig test;


    @BeforeEach
    void setUp() throws Exception {
        test = new AnsibleScriptConfig();
        test.setPlaybookName(playbookName);
        test.setVirtualEnv(virtualEnv);
        test.setPythonVersion(pythonVersion);
        test.setIsPrepareAnsibleEnvironment(isPrepareAnsibleEnvironment);
        test.setRepoUrl(repoUrl);
        test.setBranch(branch);
        test.setRequirementsFile(requirementsFile);
        test.setGalaxyFile(galaxyFile);
        test.setAnsibleInventoryRequired(ansibleInventoryRequired);
    }

    @Test
    void testGetters() {
        assertThat(test.getPlaybookName()).isEqualTo(playbookName);
        assertThat(test.getVirtualEnv()).isEqualTo(virtualEnv);
        assertThat(test.getPythonVersion()).isEqualTo(pythonVersion);
        assertThat(test.getIsPrepareAnsibleEnvironment()).isEqualTo(isPrepareAnsibleEnvironment);
        assertThat(test.getRepoUrl()).isEqualTo(repoUrl);
        assertThat(test.getBranch()).isEqualTo(branch);
        assertThat(test.getRequirementsFile()).isEqualTo(requirementsFile);
        assertThat(test.getGalaxyFile()).isEqualTo(galaxyFile);
        assertThat(test.getAnsibleInventoryRequired()).isEqualTo(ansibleInventoryRequired);
    }

    @Test
    void testEqualsAndHashCode() {
        Object obj = new Object();
        assertThat(test).isNotEqualTo(obj);
        assertThat(test.hashCode()).isNotEqualTo(obj.hashCode());
        AnsibleScriptConfig test1 = new AnsibleScriptConfig();
        assertThat(test).isNotEqualTo(test1);
        assertThat(test.hashCode()).isNotEqualTo(test1.hashCode());
        AnsibleScriptConfig test2 = new AnsibleScriptConfig();
        test2.setPlaybookName(playbookName);
        test2.setVirtualEnv(virtualEnv);
        test2.setPythonVersion(pythonVersion);
        test2.setIsPrepareAnsibleEnvironment(isPrepareAnsibleEnvironment);
        test2.setRepoUrl(repoUrl);
        test2.setBranch(branch);
        test2.setRequirementsFile(requirementsFile);
        test2.setGalaxyFile(galaxyFile);
        test2.setAnsibleInventoryRequired(ansibleInventoryRequired);
        assertThat(test).isEqualTo(test2);
        assertThat(test.hashCode()).isEqualTo(test2.hashCode());
    }

    @Test
    void testCanEqual() {
        assertThat(test.canEqual("other")).isFalse();
        assertThat(test.canEqual(new AnsibleScriptConfig())).isTrue();
    }

    @Test
    void testToString() throws Exception {
        String result = "AnsibleScriptConfig(playbookName=" + playbookName
                + ", virtualEnv=" + virtualEnv
                + ", pythonVersion=" + pythonVersion
                + ", isPrepareAnsibleEnvironment=" + isPrepareAnsibleEnvironment
                + ", repoUrl=" + repoUrl
                + ", branch=" + branch
                + ", requirementsFile=" + requirementsFile
                + ", galaxyFile=" + galaxyFile
                + ", ansibleInventoryRequired=" + ansibleInventoryRequired + ")";
        assertThat(test.toString()).isEqualTo(result);
    }

}
