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
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.deployment.utils.DeploymentScriptsHelper;
import org.eclipse.xpanse.modules.models.serviceconfiguration.exceptions.ServiceActionTemplateInvalidException;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceAction;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceChangeParameter;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** Defines method to validate the service action of deployment. */
@Slf4j
@Component
public class ServiceActionTemplateValidator {

    public static final String CSP_SCRIPT_FILE_NAME = "provider.tf";
    @Resource private PluginManager pluginManager;
    @Resource private DeploymentScriptsHelper deploymentScriptsHelper;

    /** validate service actions. */
    public void validateServiceAction(Ocl ocl) {
        List<ServiceAction> serviceActions = ocl.getServiceActions();
        if (CollectionUtils.isEmpty(serviceActions)) {
            return;
        }
        if (!isActionNamesUnique(serviceActions)) {
            String error = "The action name of each service action in the template must be unique.";
            log.error(error);
            throw new ServiceActionTemplateInvalidException(List.of(error));
        }
        validateActionParameters(ocl);
    }

    /** validate service actions parameters. */
    private void validateActionParameters(Ocl ocl) {
        OrchestratorPlugin plugin =
                pluginManager.getOrchestratorPlugin(ocl.getCloudServiceProvider().getName());
        List<File> scriptFiles = prepareDeployWorkspaceWithScripts(ocl);
        List<ServiceChangeParameter> actionParameters = getAllActionParameters(ocl);
        if (CollectionUtils.isEmpty(scriptFiles) || CollectionUtils.isEmpty(actionParameters)) {
            return;
        }
        List<String> errors = new ArrayList<>();
        scriptFiles.forEach(
                scriptFile -> {
                    Map<String, String> resourceMap =
                            plugin.getComputeResourcesInServiceDeployment(scriptFile);
                    if (!CollectionUtils.isEmpty(resourceMap)) {
                        actionParameters.forEach(
                                actionParameter -> {
                                    if (!resourceMap.containsKey(actionParameter.getManagedBy())) {
                                        String errorMsg =
                                                String.format(
                                                        "managedBy field value %s of %s parameter"
                                                                + " is not valid",
                                                        actionParameter.getManagedBy(),
                                                        actionParameter.getName());
                                        log.error(errorMsg);
                                        errors.add(errorMsg);
                                    }
                                });
                    }
                });
        if (!CollectionUtils.isEmpty(errors)) {
            throw new ServiceActionTemplateInvalidException(errors);
        }
    }

    private boolean isActionNamesUnique(List<ServiceAction> serviceActions) {
        Map<String, Long> nameCount =
                serviceActions.stream()
                        .collect(
                                Collectors.groupingBy(
                                        ServiceAction::getName, Collectors.counting()));
        return nameCount.values().stream().allMatch(count -> count == 1);
    }

    private List<ServiceChangeParameter> getAllActionParameters(Ocl ocl) {
        return ocl.getServiceActions().stream()
                .map(ServiceAction::getActionParameters)
                .filter(actionParameters -> !CollectionUtils.isEmpty(actionParameters))
                .flatMap(List::stream)
                .collect(Collectors.toList());
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
