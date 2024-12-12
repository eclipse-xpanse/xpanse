/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import jakarta.annotation.Resource;
import java.util.Objects;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.PluginNotFoundException;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResourceHandler;
import org.springframework.stereotype.Component;

/** Bean to decide which resource handler to be used. */
@Component
public class ResourceHandlerManager {

    @Resource private PluginManager pluginManager;

    /**
     * method to decide which resource handler to be used for handling deployment result.
     *
     * @param csp CSP of the deployment.
     * @return DeployResourceHandler to be used.
     */
    public DeployResourceHandler getResourceHandler(Csp csp, DeployerKind deployerKind) {
        OrchestratorPlugin plugin = pluginManager.getPluginsMap().get(csp);
        if (Objects.isNull(plugin) || Objects.isNull(plugin.resourceHandlers().get(deployerKind))) {
            throw new PluginNotFoundException(
                    "Can't find suitable plugin and resource handler for the Task.");
        }
        return plugin.resourceHandlers().get(deployerKind);
    }
}
