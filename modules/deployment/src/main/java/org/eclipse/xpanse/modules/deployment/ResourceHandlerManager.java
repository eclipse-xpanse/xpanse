/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import jakarta.annotation.Resource;
import java.util.Objects;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.PluginNotFoundException;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResourceHandler;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.springframework.stereotype.Component;

/**
 * Bean to decide which resource handler to be used.
 */
@Component
public class ResourceHandlerManager {

    @Resource
    private PluginManager pluginManager;

    /**
     * method to decide which resource handler to be used for handling deployment result.
     *
     * @param csp CSP of the deployment.
     * @return DeployResourceHandler to be used.
     */
    public DeployResourceHandler getResourceHandler(Csp csp) {
        OrchestratorPlugin plugin =
                pluginManager.getPluginsMap().get(csp);
        if (Objects.isNull(plugin) || Objects.isNull(plugin.getResourceHandler())) {
            throw new PluginNotFoundException(
                    "Can't find suitable plugin and resource handler for the Task.");
        }
        return plugin.getResourceHandler();
    }

    /**
     * method to update deployTask with the resource handler to used for deploying.
     *
     * @param deployTask DeployTask object to be updated.
     */
    void fillHandler(DeployTask deployTask) {
        DeployResourceHandler resourceHandler =
                getResourceHandler(deployTask.getDeployRequest().getCsp());
        deployTask.setDeployResourceHandler(resourceHandler);
    }
}
