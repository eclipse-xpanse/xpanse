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
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.config.TerraformLocalConfig;
import org.eclipse.xpanse.modules.deployment.utils.DeployEnvironments;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResult;
import org.eclipse.xpanse.modules.models.service.deploy.enums.TerraformExecState;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.TerraformExecutorException;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployValidationResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.Deployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * Implementation of the deployment with terraform.
 */
@Slf4j
@Component
@ConditionalOnMissingBean(TerraformBootDeployment.class)
public class TerraformDeployment implements Deployment {

    public static final String VERSION_FILE_NAME = "version.tf";
    public static final String SCRIPT_FILE_NAME = "resources.tf";
    public static final String STATE_FILE_NAME = "terraform.tfstate";
    public static final String TF_DEBUG_FLAG = "TF_LOG";
    private final DeployEnvironments deployEnvironments;
    private final TerraformLocalConfig terraformLocalConfig;
    private final PluginManager pluginManager;

    /**
     * Initializes the Terraform deployer.
     */
    @Autowired
    public TerraformDeployment(
            DeployEnvironments deployEnvironments,
            TerraformLocalConfig terraformLocalConfig, PluginManager pluginManager) {
        this.deployEnvironments = deployEnvironments;
        this.terraformLocalConfig = terraformLocalConfig;
        this.pluginManager = pluginManager;
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
        createScriptFile(task.getDeployRequest().getCsp(), task.getDeployRequest().getRegion(),
                workspace, task.getOcl().getDeployment().getDeployer());
        // Execute the terraform command.
        TerraformExecutor executor = getExecutorForDeployTask(task, workspace, true);
        DeployResult deployResult = new DeployResult();
        deployResult.setId(task.getId());
        try {
            executor.deploy();
            deployResult.setState(TerraformExecState.DEPLOY_SUCCESS);
            deployResult.setMessage(TerraformExecState.DEPLOY_SUCCESS.toValue());
        } catch (TerraformExecutorException tfEx) {
            log.error("Execute terraform deploy script failed. {}", tfEx.getMessage());
            deployResult.setState(TerraformExecState.DEPLOY_FAILED);
            deployResult.setMessage(tfEx.getMessage());
        }
        handleTerraformExecuteResult(executor, task, deployResult,
                TerraformExecState.DEPLOY_FAILED);
        return deployResult;
    }


    /**
     * Destroy the DeployTask.
     *
     * @param task the task for the deployment.
     */
    @Override
    public DeployResult destroy(DeployTask task, String tfState) {
        if (StringUtils.isBlank(tfState)) {
            String errorMsg = String.format("tfState of deployed service with id %s not found.",
                    task.getId());
            log.error(errorMsg);
            throw new ServiceNotDeployedException(errorMsg);
        }
        String taskId = task.getId().toString();
        String workspace = getWorkspacePath(taskId);
        createDestroyScriptFile(task.getDeployRequest().getCsp(),
                task.getDeployRequest().getRegion(), workspace, tfState);
        TerraformExecutor executor = getExecutorForDeployTask(task, workspace, false);
        DeployResult destroyResult = new DeployResult();
        destroyResult.setId(task.getId());
        try {
            executor.destroy();
            destroyResult.setState(TerraformExecState.DESTROY_SUCCESS);
            destroyResult.setMessage(TerraformExecState.DESTROY_SUCCESS.toValue());
        } catch (TerraformExecutorException tfEx) {
            log.error("Execute terraform deploy script failed. {}", tfEx.getMessage());
            destroyResult.setState(TerraformExecState.DESTROY_FAILED);
            destroyResult.setMessage(tfEx.getMessage());
        }
        handleTerraformExecuteResult(executor, task, destroyResult,
                TerraformExecState.DESTROY_FAILED);
        return destroyResult;
    }

    private void handleTerraformExecuteResult(TerraformExecutor executor, DeployTask task,
                                              DeployResult result,
                                              TerraformExecState state) {
        Map<String, String> importantFileContentMap = executor.getImportantFilesContent();
        if (!importantFileContentMap.isEmpty()) {
            result.getPrivateProperties().putAll(importantFileContentMap);
        }
        String tfState = null;
        try {
            tfState = executor.getTerraformState();
        } catch (TerraformExecutorException tfEx) {
            log.error("Read terraform state file failed. {}", tfEx.getMessage());
            result.setState(state);
            result.setMessage(tfEx.getMessage());
        }
        if (StringUtils.isNotEmpty(tfState)) {
            result.getPrivateProperties().put(STATE_FILE_NAME, tfState);
        }

        if (Objects.nonNull(task.getDeployResourceHandler())) {
            try {
                task.getDeployResourceHandler().handler(result);
            } catch (TerraformExecutorException tfEx) {
                log.error("Handle terraform resources failed. {}", tfEx.getMessage());
                result.setState(TerraformExecState.DEPLOY_FAILED);
                result.setMessage(tfEx.getMessage());
            }
        }
    }

    @Override
    public String getDeployPlanAsJson(DeployTask task) {
        String workspace = getWorkspacePath(task.getId().toString());
        // Create the workspace.
        buildWorkspace(workspace);
        createScriptFile(task.getDeployRequest().getCsp(), task.getDeployRequest().getRegion(),
                workspace, task.getOcl().getDeployment().getDeployer());
        // Execute the terraform command.
        TerraformExecutor executor = getExecutorForDeployTask(task, workspace, true);
        return executor.getTerraformPlanAsJson();
    }

    @Override
    public void deleteTaskWorkspace(String taskId) {
        String workspace = getWorkspacePath(taskId);
        deleteWorkSpace(workspace);
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
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Get a TerraformExecutor.
     *
     * @param task         the task for the deployment.
     * @param workspace    the workspace of the deployment.
     * @param isDeployTask if the task is for deploying a service.
     */
    private TerraformExecutor getExecutorForDeployTask(DeployTask task,
                                                       String workspace,
                                                       boolean isDeployTask) {
        Map<String, String> envVariables = this.deployEnvironments.getEnv(task);
        Map<String, Object> inputVariables = this.deployEnvironments.getVariables(
                task, isDeployTask);
        // load flavor variables also as input variables for terraform executor.
        inputVariables.putAll(this.deployEnvironments.getFlavorVariables(task));
        // load credential variables also as env variables for terraform executor.
        envVariables.putAll(this.deployEnvironments.getCredentialVariablesByHostingType(task));
        envVariables.putAll(this.deployEnvironments.getPluginMandatoryVariables(task));
        return getExecutor(envVariables, inputVariables, workspace);
    }

    private TerraformExecutor getExecutor(Map<String, String> envVariables,
                                          Map<String, Object> inputVariables, String workspace) {
        if (terraformLocalConfig.isDebugEnabled()) {
            log.info("Debug enabled for Terraform CLI with level {}",
                    terraformLocalConfig.getDebugLogLevel());
            envVariables.put(TF_DEBUG_FLAG, terraformLocalConfig.getDebugLogLevel());
        }
        return new TerraformExecutor(envVariables, inputVariables, workspace);
    }

    /**
     * Create terraform script.
     *
     * @param csp       the cloud service provider.
     * @param workspace the workspace for terraform.
     * @param script    terraform scripts of the task.
     */
    private void createScriptFile(Csp csp, String region, String workspace, String script) {
        log.info("start create terraform script");
        String verScriptPath = workspace + File.separator + VERSION_FILE_NAME;
        String scriptPath = workspace + File.separator + SCRIPT_FILE_NAME;
        try (FileWriter verWriter = new FileWriter(verScriptPath);
                FileWriter scriptWriter = new FileWriter(scriptPath)) {
            verWriter.write(pluginManager.getTerraformProviderForRegionByCsp(csp, region));
            scriptWriter.write(script);
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
     * @param tfState   terraform file tfstate of the task.
     */
    private void createDestroyScriptFile(Csp csp, String region, String workspace, String tfState) {
        log.info("start create terraform destroy workspace and script");
        File parentPath = new File(workspace);
        if (!parentPath.exists() || !parentPath.isDirectory()) {
            parentPath.mkdirs();
        }
        String verScriptPath = workspace + File.separator + VERSION_FILE_NAME;
        String scriptPath = workspace + File.separator + STATE_FILE_NAME;
        try (FileWriter verWriter = new FileWriter(verScriptPath);
                FileWriter scriptWriter = new FileWriter(scriptPath)) {
            verWriter.write(pluginManager.getTerraformProviderForRegionByCsp(csp, region));
            scriptWriter.write(tfState);
            log.info("create terraform destroy workspace and script success.");
        } catch (IOException e) {
            log.error("create terraform destroy workspace and script failed.", e);
            throw new TerraformExecutorException(
                    "create terraform destroy workspace and script failed.", e);
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
        return System.getProperty("java.io.tmpdir") + File.separator
                + terraformLocalConfig.getWorkspaceDirectory() + File.separator + taskId;
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
    public DeployValidationResult validate(Ocl ocl) {
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
