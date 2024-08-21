/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test of ConfigManageScript.
 */
public class ConfigManageScriptTest {


    private final String configManager = "configManager";
    private final Boolean runOnlyOnce = Boolean.FALSE;
    @Mock
    private AnsibleScriptConfig ansibleScriptConfig;

    private ConfigManageScript test;


    @BeforeEach
    void setUp() throws Exception {
        test = new ConfigManageScript();
        test.setConfigManager(configManager);
        test.setRunOnlyOnce(runOnlyOnce);
        test.setAnsibleScriptConfig(ansibleScriptConfig);
    }

    @Test
    void testGetters() {
        assertThat(test.getConfigManager()).isEqualTo(configManager);
        assertThat(test.getRunOnlyOnce()).isEqualTo(runOnlyOnce);
        assertThat(test.getAnsibleScriptConfig()).isEqualTo(ansibleScriptConfig);
    }

    @Test
    void testEqualsAndHashCode() {
        Object obj = new Object();
        assertThat(test).isNotEqualTo(obj);
        assertThat(test.hashCode()).isNotEqualTo(obj.hashCode());
        ConfigManageScript test1 = new ConfigManageScript();
        assertThat(test).isNotEqualTo(test1);
        assertThat(test.hashCode()).isNotEqualTo(test1.hashCode());
        ConfigManageScript test2 = new ConfigManageScript();
        test2.setConfigManager(configManager);
        test2.setRunOnlyOnce(runOnlyOnce);
        test2.setAnsibleScriptConfig(ansibleScriptConfig);
        assertThat(test).isEqualTo(test2);
        assertThat(test.hashCode()).isEqualTo(test2.hashCode());
    }

    @Test
    void testCanEqual() {
        assertThat(test.canEqual("other")).isFalse();
        assertThat(test.canEqual(new ConfigManageScript())).isTrue();
    }

    @Test
    void testToString() throws Exception {
        String result = "ConfigManageScript(configManager=" + configManager
                + ", runOnlyOnce=" + runOnlyOnce + ", ansibleScriptConfig=" + ansibleScriptConfig + ")";
        assertThat(test.toString()).isEqualTo(result);
    }
}
