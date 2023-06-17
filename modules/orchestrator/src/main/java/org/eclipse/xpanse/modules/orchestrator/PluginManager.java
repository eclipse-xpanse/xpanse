/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.orchestrator;

import jakarta.annotation.Resource;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * The instance to manage the all the plugins.
 */
@Slf4j
@Component
public class PluginManager {

    private final Map<Csp, OrchestratorPlugin> pluginMap = new ConcurrentHashMap<>();

    @Resource
    private ApplicationContext applicationContext;

    /**
     * Get all OrchestratorPlugin group by Csp.
     *
     * @return pluginMap
     */
    @Bean
    public Map<Csp, OrchestratorPlugin> getPlugins() {
        applicationContext.getBeansOfType(OrchestratorPlugin.class)
                .forEach((key, value) -> pluginMap.put(value.getCsp(), value));
        return pluginMap;
    }


    /**
     * Get available plugin bean implements OrchestratorPlugin by the @csp.
     *
     * @param csp The cloud service provider.
     * @return available plugin bean.
     */
    public OrchestratorPlugin getOrchestratorPlugin(Csp csp) {
        OrchestratorPlugin plugin = pluginMap.get(csp);
        if (Objects.isNull(plugin)) {
            throw new RuntimeException("Can't find suitable plugin for the Csp " + csp.name());
        }
        return plugin;
    }

}
