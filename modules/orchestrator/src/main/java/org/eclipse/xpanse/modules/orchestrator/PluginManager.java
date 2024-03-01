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
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.exceptions.DuplicateCredentialDefinition;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.PluginNotFoundException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * The instance to manage all the plugins.
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
        checkCredentialDefinitions();
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
            throw new PluginNotFoundException(
                    "Can't find suitable plugin for the Csp " + csp.name());
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

    private void checkCredentialDefinitions() {
        List<String> errorMsgList = new ArrayList<>();
        for (Map.Entry<Csp, OrchestratorPlugin> entry : pluginsMap.entrySet()) {
            List<AbstractCredentialInfo> cspCredentialDefinitions =
                    entry.getValue().getCredentialDefinitions();
            if (CollectionUtils.isEmpty(cspCredentialDefinitions)) {
                log.error("No defined credential definition found in the plugin of csp:{}",
                        entry.getKey());
                continue;
            }
            Map<String, List<AbstractCredentialInfo>> uniqueKeyCredentialDefinitionsMap =
                    cspCredentialDefinitions.stream()
                            .collect(Collectors.groupingBy(AbstractCredentialInfo::getUniqueKey));
            for (Map.Entry<String, List<AbstractCredentialInfo>> credentialEntry
                    : uniqueKeyCredentialDefinitionsMap.entrySet()) {
                if (!CollectionUtils.isEmpty(credentialEntry.getValue())
                        && credentialEntry.getValue().size() > 1) {
                    String errorMsg = String.format("In the plugin of csp %s defined duplicate "
                                    + "credential definitions with key %s", entry.getKey(),
                            credentialEntry.getKey());
                    errorMsgList.add(errorMsg);
                }
            }
        }
        if (!errorMsgList.isEmpty()) {
            throw new DuplicateCredentialDefinition(StringUtils.join(errorMsgList, "\n"));
        }

    }

}
