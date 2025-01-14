/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.orchestrator;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.PluginNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** The instance to manage all the plugins. */
@Slf4j
@Component
public class PluginManager implements ApplicationListener<ContextRefreshedEvent> {

    @Getter private final Map<Csp, OrchestratorPlugin> pluginsMap = new ConcurrentHashMap<>();

    @Value("${multiple.providers.black.properties}")
    private String multipleProvidersBlackProperties;

    @Resource private ApplicationContext applicationContext;

    /** Instantiates plugins map. */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        applicationContext
                .getBeansOfType(OrchestratorPlugin.class)
                .forEach(
                        (key, value) -> {
                            if (isPluginUsable(value.requiredProperties(), value.getCsp())) {
                                pluginsMap.put(value.getCsp(), value);
                            }
                        });
        checkCredentialDefinitions();
        log.info("Cloud service providers {} with activated plugins.", pluginsMap.keySet());
        if (pluginsMap.isEmpty()) {
            throw new PluginNotFoundException("No plugin is activated.");
        }
        if (pluginsMap.size() > 1) {
            List<String> blackProperties =
                    Arrays.asList(StringUtils.split(multipleProvidersBlackProperties, ","));
            if (!CollectionUtils.isEmpty(blackProperties)) {
                blackProperties.forEach(
                        env -> {
                            if (Objects.nonNull(
                                    applicationContext.getEnvironment().getProperty(env))) {
                                log.warn(
                                        "More than one plugin is activated. The environment"
                                                + " variable "
                                                + env
                                                + " will cause the exception when connecting to the"
                                                + " provider service.");
                            }
                        });
            }
        }
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
                    "Can't find suitable plugin for the Csp " + csp.toValue());
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
            log.warn(
                    "Plugin for provider {} will not be activated. Due to missing mandatory "
                            + "configuration properties {}",
                    csp.toValue(),
                    String.join(",", missingMandatoryProperties));
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
                log.warn(
                        "No defined credential definition found in the plugin of csp:{}",
                        entry.getKey().toValue());
                continue;
            }
            Map<String, List<AbstractCredentialInfo>> uniqueKeyCredentialDefinitionsMap =
                    cspCredentialDefinitions.stream()
                            .collect(Collectors.groupingBy(AbstractCredentialInfo::getUniqueKey));
            for (Map.Entry<String, List<AbstractCredentialInfo>> credentialEntry :
                    uniqueKeyCredentialDefinitionsMap.entrySet()) {
                if (!CollectionUtils.isEmpty(credentialEntry.getValue())
                        && credentialEntry.getValue().size() > 1) {
                    String errorMsg =
                            String.format(
                                    "In the plugin of csp %s defined duplicate "
                                            + "credential definitions with key %s",
                                    entry.getKey().toValue(), credentialEntry.getKey());
                    errorMsgList.add(errorMsg);
                }
            }
        }
        if (!errorMsgList.isEmpty()) {
            throw new DuplicateCredentialDefinition(StringUtils.join(errorMsgList, "\n"));
        }
    }
}
