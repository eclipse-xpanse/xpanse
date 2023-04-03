/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.deployment.Deployment;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.exceptions.TerraformExecutorException;
import org.eclipse.xpanse.modules.deployment.utils.DeployEnvironments;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.modules.models.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.enums.TerraformExecState;
import org.eclipse.xpanse.modules.models.service.DeployResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Implementation of th deployment with terraform.
 */
@Slf4j
@Component
public class TerraformDeployment implements Deployment {

    public static final String VERSION_FILE_NAME = "version.tf";
    public static final String SCRIPT_FILE_NAME = "resources.tf";


    private final String workspaceDirectory;

    public TerraformDeployment(
            @Value("${terraform.workspace.directory:xpanse_deploy_ws}") String workspaceDirectory) {
        this.workspaceDirectory = workspaceDirectory;
    }

    /**
     * Deploy the DeployTask.
     *
     * @param task the task for the deployment.
     */
    @Override
    public DeployResult deploy(DeployTask task) {
        String workspace = getWorkspacePath(task.getId().toString());
        // Create the workspace.
        buildWorkspace(workspace);
        createScriptFile(task.getCreateRequest().getCsp(), task.getCreateRequest().getRegion(),
                workspace, task.getOcl().getDeployment().getDeployer());
        // Execute the terraform command.
        TerraformExecutor executor = getExecutor(task, workspace);
        executor.deploy();
        String tfState = executor.getTerraformState();

        DeployResult deployResult = new DeployResult();
        if (StringUtils.isBlank(tfState)) {
            deployResult.setState(TerraformExecState.DEPLOY_FAILED);
        } else {
            deployResult.setState(TerraformExecState.DEPLOY_SUCCESS);
            deployResult.getProperty().put("stateFile", tfState);
        }

        if (task.getDeployResourceHandler() != null) {
            task.getDeployResourceHandler().handler(deployResult);
        }
        return deployResult;
    }


    /**
     * Destroy the DeployTask.
     *
     * @param task the task for the deployment.
     */
    @Override
    public DeployResult destroy(DeployTask task) {
        String workspace = getWorkspacePath(task.getId().toString());
        TerraformExecutor executor = getExecutor(task, workspace);
        DeployResult result = new DeployResult();
        result.setId(task.getId());
        executor.destroy();
        result.setState(TerraformExecState.DESTROY_SUCCESS);
        return result;
    }

    /**
     * Get a TerraformExecutor.
     *
     * @param task      the task for the deployment.
     * @param workspace the workspace of the deployment.
     */
    private TerraformExecutor getExecutor(DeployTask task, String workspace) {
        Map<String, String> envVariables = DeployEnvironments.getEnv(task);
        Map<String, String> flavorVariables = DeployEnvironments.getFlavorVariables(task);
        Map<String, String> tfFlavorVariables = new HashMap<>();
        for (String key : flavorVariables.keySet()) {
            tfFlavorVariables.put("TF_VAR_" + key, flavorVariables.get(key));
        }
        envVariables.putAll(tfFlavorVariables);
        return new TerraformExecutor(envVariables, DeployEnvironments.getVariables(task),
                workspace);
    }

    /**
     * Create terraform script.
     *
     * @param csp       the cloud service provider.
     * @param workspace the workspace for terraform.
     * @param script    the terraform scripts of the task.
     */
    private void createScriptFile(Csp csp, String region, String workspace, String script) {
        log.info("start create terraform script");
        String verScriptPath = workspace + File.separator + VERSION_FILE_NAME;
        String scriptPath = workspace + File.separator + SCRIPT_FILE_NAME;
        try {
            try (FileWriter verWriter = new FileWriter(verScriptPath);
                    FileWriter scriptWriter = new FileWriter(scriptPath)) {
                verWriter.write(TerraformProviders.getProvider(csp).getProvider(region));
                scriptWriter.write(script);
            }
            log.info("terraform script create success");
        } catch (IOException ex) {
            log.error("create version file failed.", ex);
            throw new TerraformExecutorException("create version file failed.", ex);
        }
    }

    /**
     * Build workspace of the `terraform`.
     *
     * @param workspace The workspace of the task.
     */
    private void buildWorkspace(String workspace) {
        log.info("start create workspace");
        File ws = new File(workspace);
        if (!ws.exists() && !ws.mkdirs()) {
            throw new TerraformExecutorException(
                    "Create workspace failed, File path not created: " + ws.getAbsolutePath());
        }
        log.info("workspace create success,Working directory is " + ws.getAbsolutePath());
    }

    /**
     * Get the workspace path for terraform.
     *
     * @param taskId The id of the task.
     */
    private String getWorkspacePath(String taskId) {
        return System.getProperty("java.io.tmpdir")
                + this.workspaceDirectory + File.separator + File.separator + taskId;
    }


    /**
     * Get the deployer kind.
     */
    @Override
    public DeployerKind getDeployerKind() {
        return DeployerKind.TERRAFORM;
    }
}
