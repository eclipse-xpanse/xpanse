/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.runtime.modules.orcherstrator;

import jakarta.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.PluginNotFoundException;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        properties = {
            "spring.profiles.active=oauth,zitadel,zitadel-testbed,terraform-boot,tofu-maker,test,dev"
        })
@AutoConfigureMockMvc
class PluginManagerTest {

    @Resource private PluginManager pluginManager;
    @Resource private ApplicationContext applicationContext;
    private List<OrchestratorPlugin> plugins;

    @BeforeEach
    void setup() {
        plugins =
                applicationContext.getBeansOfType(OrchestratorPlugin.class).values().stream()
                        .toList();
    }

    @Test
    void testPluginManagerMethods() {
        testGetPluginsMap();
        testGetOrchestratorPlugin();
    }

    void testGetPluginsMap() {
        // Setup
        Set<Csp> activatedCspSet =
                new HashSet<>(plugins.stream().map(OrchestratorPlugin::getCsp).toList());
        // run test
        Map<Csp, OrchestratorPlugin> res = this.pluginManager.getPluginsMap();
        // verify the result
        Assertions.assertFalse(this.pluginManager.getPluginsMap().isEmpty());
        Assertions.assertEquals(this.pluginManager.getPluginsMap().keySet(), activatedCspSet);
    }

    void testGetOrchestratorPlugin() {
        // Setup
        // Run the test
        final OrchestratorPlugin result =
                pluginManager.getOrchestratorPlugin(plugins.getFirst().getCsp());
        // Verify the results
        Assertions.assertNotNull(result);
        Assertions.assertInstanceOf(plugins.getFirst().getClass(), result);

        // Run the test
        Assertions.assertThrows(
                PluginNotFoundException.class,
                () -> {
                    pluginManager.getOrchestratorPlugin(Csp.AWS);
                });
    }
}
