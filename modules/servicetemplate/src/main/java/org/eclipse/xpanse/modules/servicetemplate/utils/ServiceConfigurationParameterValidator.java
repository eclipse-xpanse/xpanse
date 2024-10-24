/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.servicetemplate.utils;

import jakarta.annotation.Resource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.exceptions.TerraformExecutorException;
import org.eclipse.xpanse.modules.deployment.utils.ScriptsGitRepoManage;
import org.eclipse.xpanse.modules.models.serviceconfiguration.exceptions.ServiceConfigurationInvalidException;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceConfigurationParameter;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Defines method to validate the service configuration parameter of deployment.
 */
@Slf4j
@Component
public class ServiceConfigurationParameterValidator {

    public static final String SCRIPT_FILE_NAME = "resources.tf";
    public static final String CSP_SCRIPT_FILE_NAME = "provider.tf";

    @Resource
    private PluginManager pluginManager;

    @Resource
    private ScriptsGitRepoManage scriptsGitRepoManage;

    @Value("${terraform.workspace.directory}")
    private String workspaceDirectory;

    /**
     * validate service configuration parameters.
     */
    public void validateServiceConfigurationParameters(Ocl ocl) {
        OrchestratorPlugin plugin = pluginManager.getOrchestratorPlugin(
                ocl.getCloudServiceProvider().getName());
        List<File> scriptFiles = prepareDeployWorkspaceWithScripts(ocl);
        List<ServiceConfigurationParameter> configurationParameters =
                ocl.getServiceConfigurationManage().getConfigurationParameters();
        if (CollectionUtils.isEmpty(scriptFiles)
                || CollectionUtils.isEmpty(configurationParameters)) {
            return;
        }
        List<String> errors = new ArrayList<>();
        scriptFiles.forEach(scriptFile -> {
            Map<String, String> resourceMap =
                    plugin.getComputeResourcesInServiceDeployment(scriptFile);
            if (!CollectionUtils.isEmpty(resourceMap)) {
                configurationParameters.forEach(configurationParameter -> {
                    if (!resourceMap.containsKey(configurationParameter.getManagedBy())) {
                        String errorMsg = String.format(
                                "managedBy field value %s of %s parameter is not valid",
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
        String workspace = getWorkspacePath(UUID.randomUUID());
        if (Objects.nonNull(ocl.getDeployment().getScriptsRepo())) {
            File[] files = scriptsGitRepoManage
                    .checkoutScripts(workspace, ocl.getDeployment().getScriptsRepo());
            return Arrays.stream(files)
                    .filter(file -> file.getName().endsWith(".tf"))
                    .filter(file -> !CSP_SCRIPT_FILE_NAME.equals(file.getName()))
                    .toList();
        } else {
            return List.of(createScriptFile(workspace, ocl.getDeployment().getDeployer()));
        }
    }

    private File createScriptFile(String workspace, String script) {
        File ws = new File(workspace);
        if (!ws.exists() && !ws.mkdirs()) {
            throw new TerraformExecutorException(
                    "workspace creation failed, File path not created: " + ws.getAbsolutePath());
        }
        String scriptPath = workspace + File.separator + SCRIPT_FILE_NAME;
        try (FileWriter scriptWriter = new FileWriter(scriptPath)) {
            scriptWriter.write(script);
            log.info("Terraform script creation successful");
            return new File(scriptPath);
        } catch (IOException ex) {
            log.error("create version file failed.", ex);
            throw new TerraformExecutorException("version file creation failed.", ex);
        }
    }


    private String getWorkspacePath(UUID id) {
        return System.getProperty("java.io.tmpdir") + File.separator
                + workspaceDirectory + File.separator + id.toString();
    }
}
