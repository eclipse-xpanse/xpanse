/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.orchestrator;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
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

    @Getter
    private final Map<Csp, OrchestratorPlugin> pluginsMap = new ConcurrentHashMap<>();

    @Resource
    private ApplicationContext applicationContext;

    /**
     * Instantiates plugins map.
     */
    @Bean
    public void loadPlugins() {
        applicationContext.getBeansOfType(OrchestratorPlugin.class)
                .forEach((key, value) -> {
                    if (isPluginUsable(value.requiredProperties(), value.getCsp())) {
                        pluginsMap.put(value.getCsp(), value);
                    } else {
                        log.error(String.format(
                                "Plugin for csp %s is not in usable state. Will not be activated.",
                                value.getCsp()));
                    }
                });
    }


    /**
     * Get available plugin bean implements OrchestratorPlugin by the @csp.
     *
     * @param csp The cloud service provider.
     * @return available plugin bean.
     */
    public OrchestratorPlugin getOrchestratorPlugin(Csp csp) {
        OrchestratorPlugin plugin = pluginsMap.get(csp);
        if (Objects.isNull(plugin)) {
            throw new RuntimeException("Can't find suitable plugin for the Csp " + csp.name());
        }
        return plugin;
    }

    private boolean isPluginUsable(List<String> requiredProperties, Csp csp) {
        List<String> missingMandatoryProperties = new ArrayList<>();
        for (String property : requiredProperties) {
            if (Strings.isBlank(this.applicationContext.getEnvironment().getProperty(property))) {
                missingMandatoryProperties.add(property);
            }
        }
        if (!missingMandatoryProperties.isEmpty()) {
            log.error("Missing mandatory configuration properties for Csp " + csp + ": "
                    + String.join(", ", missingMandatoryProperties));
            return false;
        }
        return true;
    }

}
