/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

/** Test of ConfigManageScript. */
public class ServiceChangeScriptTest {

    private final String changeHandler = "changeHandler";
    private final Boolean runOnlyOnce = Boolean.FALSE;
    @Mock private AnsibleScriptConfig ansibleScriptConfig;

    private ServiceChangeScript test;

    @BeforeEach
    void setUp() throws Exception {
        test = new ServiceChangeScript();
        test.setChangeHandler(changeHandler);
        test.setRunOnlyOnce(runOnlyOnce);
        test.setAnsibleScriptConfig(ansibleScriptConfig);
    }

    @Test
    void testGetters() {
        assertThat(test.getChangeHandler()).isEqualTo(changeHandler);
        assertThat(test.getRunOnlyOnce()).isEqualTo(runOnlyOnce);
        assertThat(test.getAnsibleScriptConfig()).isEqualTo(ansibleScriptConfig);
    }

    @Test
    void testEqualsAndHashCode() {
        Object obj = new Object();
        assertThat(test).isNotEqualTo(obj);
        assertThat(test.hashCode()).isNotEqualTo(obj.hashCode());
        ServiceChangeScript test1 = new ServiceChangeScript();
        assertThat(test).isNotEqualTo(test1);
        assertThat(test.hashCode()).isNotEqualTo(test1.hashCode());
        ServiceChangeScript test2 = new ServiceChangeScript();
        test2.setChangeHandler(changeHandler);
        test2.setRunOnlyOnce(runOnlyOnce);
        test2.setAnsibleScriptConfig(ansibleScriptConfig);
        assertThat(test).isEqualTo(test2);
        assertThat(test.hashCode()).isEqualTo(test2.hashCode());
    }

    @Test
    void testCanEqual() {
        assertThat(test.canEqual("other")).isFalse();
        assertThat(test.canEqual(new ServiceChangeScript())).isTrue();
    }

    @Test
    void testToString() throws Exception {
        String result =
                "ServiceChangeScript(changeHandler="
                        + changeHandler
                        + ", runOnlyOnce="
                        + runOnlyOnce
                        + ", ansibleScriptConfig="
                        + ansibleScriptConfig
                        + ")";
        assertThat(test.toString()).isEqualTo(result);
    }
}
