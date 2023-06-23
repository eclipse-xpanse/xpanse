/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.orchestrator;

import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(classes = {PluginManagerTest.class, DummyOpenstackPluginImpl.class,
        DummyFlexibleEnginePluginImpl.class,
        PluginManager.class})
@ExtendWith(SpringExtension.class)
class PluginManagerTest {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    PluginManager pluginManager;

    @BeforeAll
    void setEnvironment() {
        System.setProperty("TEST_VAR", "foo");
    }

    @Test
    void testIfPluginIsSuccessfullyAdded() {
        this.pluginManager.loadPlugins();
        Assertions.assertFalse(this.pluginManager.getPluginsMap().isEmpty());
        Assertions.assertTrue(this.pluginManager.getPluginsMap().containsKey(Csp.OPENSTACK));
        Assertions.assertFalse(this.pluginManager.getPluginsMap().containsKey(Csp.FLEXIBLE_ENGINE));
    }
}
