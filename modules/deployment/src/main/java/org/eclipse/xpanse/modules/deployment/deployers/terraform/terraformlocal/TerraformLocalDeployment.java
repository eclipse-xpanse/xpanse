/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformlocal;

import jakarta.annotation.Nullable;
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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.deployment.DeployServiceEntityHandler;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.callbacks.TerraformDeploymentResultCallbackManager;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.exceptions.TerraformExecutorException;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.TerraformBootDeployment;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformResult;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformlocal.config.TerraformLocalConfig;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.utils.TfResourceTransUtils;
import org.eclipse.xpanse.modules.deployment.utils.DeployEnvironments;
import org.eclipse.xpanse.modules.deployment.utils.DeployResultFileUtils;
import org.eclipse.xpanse.modules.deployment.utils.ScriptsGitRepoManage;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.servicetemplate.Deployment;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.deployment.Deployer;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeploymentScriptValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * Implementation of the deployment with terraform.
 */
@Slf4j
@Component
@ConditionalOnMissingBean(TerraformBootDeployment.class)
public class TerraformLocalDeployment implements Deployer {
    public static final String SCRIPT_FILE_NAME = "resources.tf";
    public static final String STATE_FILE_NAME = "terraform.tfstate";
    public static final String TF_DEBUG_FLAG = "TF_LOG";
    private final DeployEnvironments deployEnvironments;
    private final TerraformLocalConfig terraformLocalConfig;
    private final Executor taskExecutor;
    private final TerraformDeploymentResultCallbackManager terraformDeploymentResultCallbackManager;
    private final DeployServiceEntityHandler deployServiceEntityHandler;
    private final ScriptsGitRepoManage scriptsGitRepoManage;
    private final DeployResultFileUtils deployResultFileUtils;

    /**
     * Initializes the Terraform deployer.
     */
    @Autowired
    public TerraformLocalDeployment(DeployEnvironments deployEnvironments,
                                    TerraformLocalConfig terraformLocalConfig,
                                    @Qualifier("xpanseAsyncTaskExecutor") Executor taskExecutor,
                                    TerraformDeploymentResultCallbackManager
                                                terraformDeploymentResultCallbackManager,
                                    DeployServiceEntityHandler deployServiceEntityHandler,
                                    ScriptsGitRepoManage scriptsGitRepoManage,
                                    DeployResultFileUtils deployResultFileUtils) {
        this.deployEnvironments = deployEnvironments;
        this.terraformLocalConfig = terraformLocalConfig;
        this.taskExecutor = taskExecutor;
        this.terraformDeploymentResultCallbackManager = terraformDeploymentResultCallbackManager;
        this.deployServiceEntityHandler = deployServiceEntityHandler;
        this.scriptsGitRepoManage = scriptsGitRepoManage;
        this.deployResultFileUtils = deployResultFileUtils;
    }

    /**
     * Deploy the DeployTask.
     *
     * @param task the task for the deployment.
     */
    @Override
    public DeployResult deploy(DeployTask task) {
        DeployResult deployResult = new DeployResult();
        deployResult.setOrderId(task.getOrderId());
        deployResult.setServiceId(task.getServiceId());
        asyncExecDeploy(task);
        return deployResult;
    }


    /**
     * Destroy the DeployTask.
     *
     * @param task the task for the deployment.
     */
    @Override
    public DeployResult destroy(DeployTask task) {
        DeployServiceEntity deployServiceEntity =
                deployServiceEntityHandler.getDeployServiceEntity(task.getServiceId());
        String resourceState = TfResourceTransUtils.getStoredStateContent(deployServiceEntity);
        if (StringUtils.isBlank(resourceState)) {
            String errorMsg = String.format("tfState of deployed service with id %s not found.",
                    task.getServiceId());
            log.error(errorMsg);
            throw new ServiceNotDeployedException(errorMsg);
        }

        DeployResult destroyResult = new DeployResult();
        destroyResult.setServiceId(task.getServiceId());
        destroyResult.setOrderId(task.getOrderId());
        asyncExecDestroy(task, resourceState);
        return destroyResult;
    }

    /**
     * Modify the DeployTask.
     *
     * @param task the task for the deployment.
     */
    @Override
    public DeployResult modify(DeployTask task) {
        DeployServiceEntity deployServiceEntity =
                deployServiceEntityHandler.getDeployServiceEntity(task.getServiceId());
        String resourceState = TfResourceTransUtils.getStoredStateContent(deployServiceEntity);
        if (StringUtils.isBlank(resourceState)) {
            String errorMsg = String.format("tfState of deployed service with id %s not found.",
                    task.getServiceId());
            log.error(errorMsg);
            throw new ServiceNotDeployedException(errorMsg);
        }
        DeployResult modifyResult = new DeployResult();
        modifyResult.setServiceId(task.getServiceId());
        modifyResult.setOrderId(task.getOrderId());
        asyncExecModify(task, resourceState);
        return modifyResult;
    }

    private void asyncExecDeploy(DeployTask task) {
        String workspace = getWorkspacePath(task.getServiceId());
        // Create the workspace.
        buildWorkspace(workspace);
        prepareDeployWorkspaceWithScripts(task, workspace);
        TerraformLocalExecutor executor = getExecutorForDeployTask(task, workspace, true);
        // Execute the terraform command asynchronously.
        taskExecutor.execute(() -> {
            TerraformResult terraformResult = new TerraformResult();
            terraformResult.setRequestId(task.getOrderId());
            try {
                executor.deploy();
                terraformResult.setCommandSuccessful(true);
            } catch (RuntimeException tfEx) {
                log.error("Execute Terraform deploy script failed. {}", tfEx.getMessage());
                terraformResult.setCommandSuccessful(false);
                terraformResult.setCommandStdError(tfEx.getMessage());
            }
            terraformResult.setTerraformState(executor.getTerraformState());
            Map<String, String> importantFileContentMap = executor.getImportantFilesContent();
            terraformResult.setImportantFileContentMap(importantFileContentMap);
            terraformDeploymentResultCallbackManager.deployCallback(task.getServiceId(),
                    terraformResult);
            deleteStoredFiles(workspace, importantFileContentMap.keySet());
        });
    }

    private void asyncExecDestroy(DeployTask task, String tfState) {
        String workspace = getWorkspacePath(task.getServiceId());
        prepareDestroyWorkspaceWithScripts(task, workspace, tfState);
        TerraformLocalExecutor executor = getExecutorForDeployTask(task, workspace, false);
        // Execute the terraform command asynchronously.
        taskExecutor.execute(() -> {
            TerraformResult terraformResult = new TerraformResult();
            terraformResult.setRequestId(task.getOrderId());
            try {
                executor.destroy();
                terraformResult.setCommandSuccessful(true);
            } catch (RuntimeException tfEx) {
                log.error("Execute terraform destroy script failed. {}", tfEx.getMessage());
                terraformResult.setCommandSuccessful(false);
                terraformResult.setCommandStdError(tfEx.getMessage());
            }
            terraformResult.setTerraformState(executor.getTerraformState());
            Map<String, String> importantFileContentMap = executor.getImportantFilesContent();
            terraformResult.setImportantFileContentMap(importantFileContentMap);
            terraformDeploymentResultCallbackManager.destroyCallback(task.getServiceId(),
                    terraformResult, task.getDeploymentScenario());
            deleteStoredFiles(workspace, importantFileContentMap.keySet());
        });
    }

    private void asyncExecModify(DeployTask task, String tfState) {
        String workspace = getWorkspacePath(task.getServiceId());
        prepareDestroyWorkspaceWithScripts(task, workspace, tfState);
        prepareDeployWorkspaceWithScripts(task, workspace);
        TerraformLocalExecutor executor = getExecutorForDeployTask(task, workspace, true);
        // Execute the terraform command asynchronously.
        taskExecutor.execute(() -> {
            TerraformResult terraformResult = new TerraformResult();
            terraformResult.setRequestId(task.getOrderId());
            try {
                executor.deploy();
                terraformResult.setCommandSuccessful(true);
            } catch (RuntimeException tfEx) {
                log.error("Execute terraform modify script failed. {}", tfEx.getMessage());
                terraformResult.setCommandSuccessful(false);
                terraformResult.setCommandStdError(tfEx.getMessage());
            }
            terraformResult.setTerraformState(executor.getTerraformState());
            Map<String, String> importantFileContentMap = executor.getImportantFilesContent();
            terraformResult.setImportantFileContentMap(importantFileContentMap);
            terraformDeploymentResultCallbackManager.modifyCallback(task.getServiceId(),
                    terraformResult);
            deleteStoredFiles(workspace, importantFileContentMap.keySet());
        });
    }

    @Override
    public String getDeploymentPlanAsJson(DeployTask task) {
        String workspace = getWorkspacePath(task.getServiceId());
        // Create the workspace.
        buildWorkspace(workspace);
        prepareDeployWorkspaceWithScripts(task, workspace);
        // Execute the terraform command.
        TerraformLocalExecutor executor = getExecutorForDeployTask(task, workspace, true);
        return executor.getTerraformPlanAsJson();
    }

    @Override
    public void deleteTaskWorkspace(UUID taskId) {
        String workspace = getWorkspacePath(taskId);
        deleteWorkspace(workspace);
    }

    /**
     * delete workspace.
     */
    private void deleteWorkspace(String workspace) {
        Path path = Paths.get(workspace);
        try (Stream<Path> pathStream = Files.walk(path)) {
            pathStream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        } catch (Exception e) {
            log.error("Delete workspace:{} error.", workspace, e);
        }
    }

    private void deleteStoredFiles(String workspace, Set<String> fileNames) {
        fileNames.forEach(fileName -> {
            try {
                String path = workspace + File.separator + fileName;
                File file = new File(path);
                Files.deleteIfExists(file.toPath());
            } catch (IOException e) {
                log.error("Delete file with name:{} error.", fileName, e);
            }
        });
    }

    /**
     * Get a TerraformExecutor.
     *
     * @param task         the task for the deployment.
     * @param workspace    the workspace of the deployment.
     * @param isDeployTask if the task is for deploying a service.
     */
    private TerraformLocalExecutor getExecutorForDeployTask(DeployTask task, String workspace,
                                                            boolean isDeployTask) {
        Map<String, String> envVariables = this.deployEnvironments.getEnvironmentVariables(task);
        Map<String, Object> inputVariables = this.deployEnvironments.getInputVariables(
                task, isDeployTask);
        return getExecutor(envVariables, inputVariables, workspace, task.getOcl().getDeployment());
    }

    private TerraformLocalExecutor getExecutor(Map<String, String> envVariables,
                                               Map<String, Object> inputVariables, String workspace,
                                               Deployment deployment) {
        if (terraformLocalConfig.isDebugEnabled()) {
            log.info("Debug enabled for Terraform CLI with level {}",
                    terraformLocalConfig.getDebugLogLevel());
            envVariables.put(TF_DEBUG_FLAG, terraformLocalConfig.getDebugLogLevel());
        }
        return new TerraformLocalExecutor(envVariables, inputVariables, workspace,
                getSubDirectory(deployment), deployResultFileUtils);
    }

    private void prepareDeployWorkspaceWithScripts(DeployTask deployTask, String workspace) {
        if (Objects.nonNull(deployTask.getOcl().getDeployment().getDeployer())) {
            createScriptFile(workspace, deployTask.getOcl().getDeployment().getDeployer());
        }
        if (Objects.nonNull(deployTask.getOcl().getDeployment().getScriptsRepo())) {
            scriptsGitRepoManage.checkoutScripts(workspace,
                    deployTask.getOcl().getDeployment().getScriptsRepo());
        }
    }

    private void prepareDestroyWorkspaceWithScripts(DeployTask deployTask, String workspace,
                                                    String tfState) {
        log.info("start create terraform destroy workspace and script");
        File parentPath = new File(workspace);
        if (!parentPath.exists() || !parentPath.isDirectory()) {
            parentPath.mkdirs();
        }
        if (Objects.nonNull(deployTask.getOcl().getDeployment().getDeployer())) {
            createDestroyScriptFile(workspace, tfState);
        } else if (Objects.nonNull(deployTask.getOcl().getDeployment().getScriptsRepo())) {
            scriptsGitRepoManage.checkoutScripts(workspace,
                    deployTask.getOcl().getDeployment().getScriptsRepo());
            String scriptPath = workspace + File.separator + deployTask.getOcl().getDeployment()
                    .getScriptsRepo().getScriptsPath() + File.separator + STATE_FILE_NAME;
            try (FileWriter scriptWriter = new FileWriter(scriptPath)) {
                scriptWriter.write(tfState);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Create terraform script.
     *
     * @param workspace the workspace for terraform.
     * @param script    terraform scripts of the task.
     */
    private void createScriptFile(String workspace, String script) {
        log.info("start create terraform script");
        String scriptPath = workspace + File.separator + SCRIPT_FILE_NAME;
        try (FileWriter scriptWriter = new FileWriter(scriptPath)) {
            scriptWriter.write(script);
            log.info("Terraform script create success");
        } catch (IOException ex) {
            log.error("create version file failed.", ex);
            throw new TerraformExecutorException("create version file failed.", ex);
        }
    }

    /**
     * Create terraform workspace and script.
     *
     * @param workspace the workspace for terraform.
     * @param tfState   terraform file tfstate of the task.
     */
    private void createDestroyScriptFile(String workspace, String tfState) {

        String scriptPath = workspace + File.separator + STATE_FILE_NAME;
        try (FileWriter scriptWriter = new FileWriter(scriptPath)) {
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
        log.info("start creating workspace");
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
    private String getWorkspacePath(UUID taskId) {
        return System.getProperty("java.io.tmpdir") + File.separator
                + terraformLocalConfig.getWorkspaceDirectory() + File.separator + taskId.toString();
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
    @Override
    public DeploymentScriptValidationResult validate(Deployment deployment) {
        String workspace = getWorkspacePath(UUID.randomUUID());
        // Create the workspace.
        buildWorkspace(workspace);
        if (Objects.nonNull(deployment.getDeployer())) {
            createScriptFile(workspace, deployment.getDeployer());
        } else {
            scriptsGitRepoManage.checkoutScripts(workspace, deployment.getScriptsRepo());
        }
        TerraformLocalExecutor executor =
                getExecutor(new HashMap<>(), new HashMap<>(), workspace, deployment);
        return executor.tfValidate();
    }

    @Nullable
    private String getSubDirectory(Deployment deployment) {
        if (Objects.nonNull(deployment.getDeployer())) {
            return null;
        } else if (Objects.nonNull(deployment.getScriptsRepo())) {
            return deployment.getScriptsRepo().getScriptsPath();
        }
        return null;
    }
}
