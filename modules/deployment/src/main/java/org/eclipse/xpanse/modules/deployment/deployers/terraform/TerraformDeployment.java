/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.deployment.Deployment;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.exceptions.TerraformExecutorException;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.resource.TfValidationResult;
import org.eclipse.xpanse.modules.deployment.utils.DeployEnvironments;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.modules.models.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.enums.TerraformExecState;
import org.eclipse.xpanse.modules.models.resource.Ocl;
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
    public static final String STATE_FILE_NAME = "terraform.tfstate";


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
        TerraformExecutor executor = getExecutorForDeployTask(task, workspace);
        executor.deploy();
        String tfState = executor.getTerraformState();

        DeployResult deployResult = new DeployResult();
        if (StringUtils.isBlank(tfState)) {
            deployResult.setState(TerraformExecState.DEPLOY_FAILED);
        } else {
            deployResult.setState(TerraformExecState.DEPLOY_SUCCESS);
            deployResult.getPrivateProperties().put("stateFile", tfState);
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
    public DeployResult destroy(DeployTask task, String tfState) {
        DeployResult result = new DeployResult();
        if (StringUtils.isBlank(tfState)) {
            log.error("Deployed service with tfState not found, id:{}", task.getId());
            result.setId(task.getId());
            result.setState(TerraformExecState.DESTROY_FAILED);
            return result;
        }
        String taskId = task.getId().toString();
        String workspace = getWorkspacePath(taskId);
        try {
            createDestroyScriptFile(task.getCreateRequest().getCsp(),
                    task.getCreateRequest().getRegion(), workspace, tfState);
            TerraformExecutor executor = getExecutorForDeployTask(task, workspace);
            executor.destroy();
            deleteWorkSpace(workspace);
            result.setId(task.getId());
            result.setState(TerraformExecState.DESTROY_SUCCESS);
            return result;
        } catch (Exception e) {
            log.error("Destroy error, {}", e);
            result.setId(task.getId());
            result.setState(TerraformExecState.DESTROY_FAILED);
            return result;
        }
    }

    /**
     * delete workspace.
     */
    private void deleteWorkSpace(String workspace) {
        Path path = Paths.get(workspace);
        try {
            Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile)
                    .forEach(File::delete);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get a TerraformExecutor.
     *
     * @param task      the task for the deployment.
     * @param workspace the workspace of the deployment.
     */
    private TerraformExecutor getExecutorForDeployTask(DeployTask task, String workspace) {
        Map<String, String> envVariables = DeployEnvironments.getEnv(task);
        Map<String, String> inputVariables = DeployEnvironments.getVariables(task);
        // load flavor variables also as input variables for terraform executor.
        inputVariables.putAll(DeployEnvironments.getFlavorVariables(task));
        return getExecutor(envVariables, inputVariables, workspace);
    }

    private TerraformExecutor getExecutor(Map<String, String> envVariables,
                                          Map<String, String> inputVariables, String workspace) {
        return new TerraformExecutor(envVariables, inputVariables, workspace);
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
     * Create terraform workspace and script.
     *
     * @param csp       the cloud service provider.
     * @param workspace the workspace for terraform.
     * @param tfState   the terraform scripts of the tfstate.
     */
    private void createDestroyScriptFile(Csp csp, String region, String workspace, String tfState)
            throws IOException {
        log.info("start create terraform destroy workspace and script");
        File parentPath = new File(workspace);
        if (!parentPath.exists() || !parentPath.isDirectory()) {
            parentPath.mkdirs();
        }
        String verScriptPath = workspace + File.separator + VERSION_FILE_NAME;
        String scriptPath = workspace + File.separator + STATE_FILE_NAME;
        try (FileWriter verWriter = new FileWriter(verScriptPath);
                FileWriter scriptWriter = new FileWriter(scriptPath)) {
            verWriter.write(TerraformProviders.getProvider(csp).getProvider(region));
            scriptWriter.write(tfState);
        }
        log.info("terraform workspace and script create success");
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
                + this.workspaceDirectory + File.separator + taskId;
    }


    /**
     * Get the deployer kind.
     */
    @Override
    public DeployerKind getDeployerKind() {
        return DeployerKind.TERRAFORM;
    }

    /**
     * Validates the Terraform script.
     */
    public TfValidationResult validate(Ocl ocl) {
        String workspace = getWorkspacePath(UUID.randomUUID().toString());
        // Create the workspace.
        buildWorkspace(workspace);
        createScriptFile(ocl.getCloudServiceProvider().getName(),
                ocl.getCloudServiceProvider().getRegions().get(0).getName(), workspace,
                ocl.getDeployment().getDeployer());
        TerraformExecutor executor = getExecutor(new HashMap<>(), new HashMap<>(), workspace);
        return executor.tfValidate();
    }
}
