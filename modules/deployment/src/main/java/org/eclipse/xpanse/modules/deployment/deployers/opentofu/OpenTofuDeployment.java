/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu;

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
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.deployment.DeployServiceEntityHandler;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.config.OpenTofuLocalConfig;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.exceptions.OpenTofuExecutorException;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.exceptions.OpenTofuProviderNotFoundException;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.utils.TfResourceTransUtils;
import org.eclipse.xpanse.modules.deployment.utils.DeployEnvironments;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployValidationResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.Deployer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Implementation of the deployment with OpenTofu.
 */
@Slf4j
@Component
public class OpenTofuDeployment implements Deployer {

    public static final String VERSION_FILE_NAME = "version.tf";
    public static final String SCRIPT_FILE_NAME = "resources.tf";
    public static final String STATE_FILE_NAME = "terraform.tfstate";
    public static final String TF_DEBUG_FLAG = "TF_LOG";
    private final DeployEnvironments deployEnvironments;
    private final OpenTofuLocalConfig openTofuLocalConfig;
    private final PluginManager pluginManager;
    private final Executor taskExecutor;
    private final OpenTofuDeploymentResultCallbackManager openTofuDeploymentResultCallbackManager;
    private final DeployServiceEntityHandler deployServiceEntityHandler;

    /**
     * Initializes the OpenTofu deployer.
     */
    @Autowired
    public OpenTofuDeployment(DeployEnvironments deployEnvironments,
                              OpenTofuLocalConfig openTofuLocalConfig, PluginManager pluginManager,
                              @Qualifier("xpanseAsyncTaskExecutor") Executor taskExecutor,
                              OpenTofuDeploymentResultCallbackManager
                                      openTofuDeploymentResultCallbackManager,
                              DeployServiceEntityHandler deployServiceEntityHandler) {
        this.deployEnvironments = deployEnvironments;
        this.openTofuLocalConfig = openTofuLocalConfig;
        this.pluginManager = pluginManager;
        this.taskExecutor = taskExecutor;
        this.openTofuDeploymentResultCallbackManager = openTofuDeploymentResultCallbackManager;
        this.deployServiceEntityHandler = deployServiceEntityHandler;
    }

    /**
     * Deploy the DeployTask.
     *
     * @param task the task for the deployment.
     */
    @Override
    public DeployResult deploy(DeployTask task) {
        DeployResult deployResult = new DeployResult();
        deployResult.setId(task.getId());
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
                deployServiceEntityHandler.getDeployServiceEntity(task.getId());
        String resourceState = TfResourceTransUtils.getStoredStateContent(deployServiceEntity);
        if (StringUtils.isBlank(resourceState)) {
            String errorMsg = String.format("tfState of deployed service with id %s not found.",
                    task.getId());
            log.error(errorMsg);
            throw new ServiceNotDeployedException(errorMsg);
        }
        DeployResult destroyResult = new DeployResult();
        destroyResult.setId(task.getId());
        asyncExecDestroy(task, resourceState);
        return destroyResult;
    }

    private void asyncExecDeploy(DeployTask task) {
        String workspace = getWorkspacePath(task.getId());
        // Create the workspace.
        buildWorkspace(workspace);
        createScriptFile(task.getDeployRequest().getCsp(), task.getDeployRequest().getRegion(),
                workspace, task.getOcl().getDeployment().getDeployer());
        OpenTofuExecutor executor = getExecutorForDeployTask(task, workspace, true);
        // Execute the OpenTofu command asynchronously.
        taskExecutor.execute(() -> {
            OpenTofuResult openTofuResult = new OpenTofuResult();
            try {
                executor.deploy();
                openTofuResult.setCommandSuccessful(true);
            } catch (OpenTofuExecutorException tfEx) {
                log.error("Execute OpenTofu deploy script failed. {}", tfEx.getMessage());
                openTofuResult.setCommandSuccessful(false);
                openTofuResult.setCommandStdError(tfEx.getMessage());
            }
            openTofuResult.setTerraformState(executor.getTerraformState());
            openTofuResult.setImportantFileContentMap(executor.getImportantFilesContent());
            openTofuDeploymentResultCallbackManager.deployCallback(task.getId(), openTofuResult);
        });
    }

    private void asyncExecDestroy(DeployTask task, String tfState) {
        String workspace = getWorkspacePath(task.getId());
        createDestroyScriptFile(task.getDeployRequest().getCsp(),
                task.getDeployRequest().getRegion(), workspace, tfState);
        OpenTofuExecutor executor = getExecutorForDeployTask(task, workspace, false);
        // Execute the openTofu command asynchronously.
        taskExecutor.execute(() -> {
            OpenTofuResult openTofuResult = new OpenTofuResult();
            openTofuResult.setDestroyScenario(task.getDestroyScenario());
            try {
                executor.destroy();
                openTofuResult.setCommandSuccessful(true);
            } catch (OpenTofuExecutorException tfEx) {
                log.error("Execute openTofu destroy script failed. {}", tfEx.getMessage());
                openTofuResult.setCommandSuccessful(false);
                openTofuResult.setCommandStdError(tfEx.getMessage());
            }
            openTofuResult.setTerraformState(executor.getTerraformState());
            openTofuResult.setImportantFileContentMap(executor.getImportantFilesContent());
            openTofuDeploymentResultCallbackManager.destroyCallback(task.getId(), openTofuResult);
        });
    }

    @Override
    public String getDeploymentPlanAsJson(DeployTask task) {
        String workspace = getWorkspacePath(task.getId());
        // Create the workspace.
        buildWorkspace(workspace);
        createScriptFile(task.getDeployRequest().getCsp(), task.getDeployRequest().getRegion(),
                workspace, task.getOcl().getDeployment().getDeployer());
        // Execute the OpenTofu command.
        OpenTofuExecutor executor = getExecutorForDeployTask(task, workspace, true);
        return executor.getOpenTofuPlanAsJson();
    }

    @Override
    public void deleteTaskWorkspace(UUID taskId) {
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
     * Get an OpenTofuExecutor.
     *
     * @param task         the task for the deployment.
     * @param workspace    the workspace of the deployment.
     * @param isDeployTask if the task is for deploying a service.
     */
    private OpenTofuExecutor getExecutorForDeployTask(DeployTask task, String workspace,
                                                      boolean isDeployTask) {
        Map<String, String> envVariables = this.deployEnvironments.getEnvFromDeployTask(task);
        Map<String, Object> inputVariables =
                this.deployEnvironments.getVariablesFromDeployTask(task, isDeployTask);
        // load flavor variables also as input variables for OpenTofu executor.
        inputVariables.putAll(this.deployEnvironments.getFlavorVariables(task));
        // load credential variables also as env variables for OpenTofu executor.
        envVariables.putAll(this.deployEnvironments.getCredentialVariablesByHostingType(
                task.getDeployRequest().getServiceHostingType(),
                task.getOcl().getDeployment().getCredentialType(), task.getDeployRequest().getCsp(),
                task.getDeployRequest().getUserId()));
        envVariables.putAll(this.deployEnvironments.getPluginMandatoryVariables(
                task.getDeployRequest().getCsp()));
        return getExecutor(envVariables, inputVariables, workspace);
    }

    private OpenTofuExecutor getExecutor(Map<String, String> envVariables,
                                         Map<String, Object> inputVariables, String workspace) {
        if (openTofuLocalConfig.isDebugEnabled()) {
            log.info("Debug enabled for OpenTofu CLI with level {}",
                    openTofuLocalConfig.getDebugLogLevel());
            envVariables.put(TF_DEBUG_FLAG, openTofuLocalConfig.getDebugLogLevel());
        }
        return new OpenTofuExecutor(envVariables, inputVariables, workspace);
    }

    /**
     * Create OpenTofu script.
     *
     * @param csp       the cloud service provider.
     * @param workspace the workspace for OpenTofu.
     * @param script    OpenTofu scripts of the task.
     */
    private void createScriptFile(Csp csp, String region, String workspace, String script) {
        log.info("start create OpenTofu script");
        String verScript = getProvider(csp, region);
        String verScriptPath = workspace + File.separator + VERSION_FILE_NAME;
        String scriptPath = workspace + File.separator + SCRIPT_FILE_NAME;
        try (FileWriter verWriter = new FileWriter(verScriptPath);
                FileWriter scriptWriter = new FileWriter(scriptPath)) {
            verWriter.write(verScript);
            scriptWriter.write(script);
            log.info("OpenTofu script create success");
        } catch (IOException ex) {
            log.error("create version file failed.", ex);
            throw new OpenTofuExecutorException("create version file failed.", ex);
        }
    }

    /**
     * Create OpenTofu workspace and script.
     *
     * @param csp       the cloud service provider.
     * @param workspace the workspace for OpenTofu.
     * @param tfState   OpenTofu file tfstate of the task.
     */
    private void createDestroyScriptFile(Csp csp, String region, String workspace, String tfState) {
        log.info("start create OpenTofu destroy workspace and script");
        File parentPath = new File(workspace);
        if (!parentPath.exists() || !parentPath.isDirectory()) {
            parentPath.mkdirs();
        }
        String verScript = getProvider(csp, region);
        String verScriptPath = workspace + File.separator + VERSION_FILE_NAME;
        String scriptPath = workspace + File.separator + STATE_FILE_NAME;
        try (FileWriter verWriter = new FileWriter(verScriptPath);
                FileWriter scriptWriter = new FileWriter(scriptPath)) {
            verWriter.write(verScript);
            scriptWriter.write(tfState);
            log.info("create OpenTofu destroy workspace and script success.");
        } catch (IOException e) {
            log.error("create OpenTofu destroy workspace and script failed.", e);
            throw new OpenTofuExecutorException(
                    "create OpenTofu destroy workspace and script failed.", e);
        }

    }

    private String getProvider(Csp csp, String region) {
        String provider = pluginManager.getDeployerProvider(csp, DeployerKind.OPEN_TOFU, region);
        if (StringUtils.isBlank(provider)) {
            String errMsg = String.format("OpenTofu provider for Csp %s not found.", csp);
            throw new OpenTofuProviderNotFoundException(errMsg);
        }
        return provider;
    }

    /**
     * Build workspace of the `OpenTofu`.
     *
     * @param workspace The workspace of the task.
     */
    private void buildWorkspace(String workspace) {
        log.info("start creating workspace");
        File ws = new File(workspace);
        if (!ws.exists() && !ws.mkdirs()) {
            throw new OpenTofuExecutorException(
                    "Create workspace failed, File path not created: " + ws.getAbsolutePath());
        }
        log.info("workspace create success,Working directory is " + ws.getAbsolutePath());
    }

    /**
     * Get the workspace path for OpenTofu.
     *
     * @param taskId The id of the task.
     */
    private String getWorkspacePath(UUID taskId) {
        return System.getProperty("java.io.tmpdir") + File.separator
                + openTofuLocalConfig.getWorkspaceDirectory() + File.separator + taskId;
    }


    /**
     * Get the deployer kind.
     */
    @Override
    public DeployerKind getDeployerKind() {
        return DeployerKind.OPEN_TOFU;
    }

    /**
     * Validates the OpenTofu script.
     */
    public DeployValidationResult validate(Ocl ocl) {
        String workspace = getWorkspacePath(UUID.randomUUID());
        // Create the workspace.
        buildWorkspace(workspace);
        createScriptFile(ocl.getCloudServiceProvider().getName(),
                ocl.getCloudServiceProvider().getRegions().getFirst().getName(), workspace,
                ocl.getDeployment().getDeployer());
        OpenTofuExecutor executor = getExecutor(new HashMap<>(), new HashMap<>(), workspace);
        return executor.tfValidate();
    }
}
