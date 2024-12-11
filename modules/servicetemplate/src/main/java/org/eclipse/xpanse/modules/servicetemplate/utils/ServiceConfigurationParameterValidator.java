/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.servicetemplate.utils;

import static org.eclipse.xpanse.modules.deployment.utils.DeploymentScriptsHelper.TF_SCRIPT_FILE_EXTENSION;

import jakarta.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.deployment.utils.DeploymentScriptsHelper;
import org.eclipse.xpanse.modules.models.serviceconfiguration.exceptions.ServiceConfigurationInvalidException;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceConfigurationParameter;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** Defines method to validate the service configuration parameter of deployment. */
@Slf4j
@Component
public class ServiceConfigurationParameterValidator {

    public static final String CSP_SCRIPT_FILE_NAME = "provider.tf";
    @Resource private PluginManager pluginManager;
    @Resource private DeploymentScriptsHelper deploymentScriptsHelper;

    /** validate service configuration parameters. */
    public void validateServiceConfigurationParameters(Ocl ocl) {
        OrchestratorPlugin plugin =
                pluginManager.getOrchestratorPlugin(ocl.getCloudServiceProvider().getName());
        List<File> scriptFiles = prepareDeployWorkspaceWithScripts(ocl);
        List<ServiceConfigurationParameter> configurationParameters =
                ocl.getServiceConfigurationManage().getConfigurationParameters();
        if (CollectionUtils.isEmpty(scriptFiles)
                || CollectionUtils.isEmpty(configurationParameters)) {
            return;
        }
        List<String> errors = new ArrayList<>();
        scriptFiles.forEach(
                scriptFile -> {
                    Map<String, String> resourceMap =
                            plugin.getComputeResourcesInServiceDeployment(scriptFile);
                    if (!CollectionUtils.isEmpty(resourceMap)) {
                        configurationParameters.forEach(
                                configurationParameter -> {
                                    if (!resourceMap.containsKey(
                                            configurationParameter.getManagedBy())) {
                                        String errorMsg =
                                                String.format(
                                                        "managedBy field value %s of %s parameter"
                                                                + " is not valid",
                                                        configurationParameter.getManagedBy(),
                                                        configurationParameter.getName());
                                        log.error(errorMsg);
                                        errors.add(errorMsg);
                                    }
                                });
                    }
                });
        if (!CollectionUtils.isEmpty(errors)) {
            throw new ServiceConfigurationInvalidException(errors);
        }
    }

    private List<File> prepareDeployWorkspaceWithScripts(Ocl ocl) {
        String taskWorkspace =
                deploymentScriptsHelper.createWorkspaceForTask(
                        getBaseWorkspace(), UUID.randomUUID());
        List<File> files =
                deploymentScriptsHelper.prepareDeploymentScripts(
                        taskWorkspace, ocl.getDeployment(), null);
        return files.stream()
                .filter(file -> file.getName().endsWith(TF_SCRIPT_FILE_EXTENSION))
                .filter(file -> !CSP_SCRIPT_FILE_NAME.equals(file.getName()))
                .toList();
    }

    private String getBaseWorkspace() {
        return "tf-workspace";
    }
}
