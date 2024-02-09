/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofulocal;

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
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.deployment.DeployServiceEntityHandler;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.callbacks.OpenTofuDeploymentResultCallbackManager;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.exceptions.OpenTofuExecutorException;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofulocal.config.OpenTofuLocalConfig;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofumaker.OpenTofuMakerDeployment;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofumaker.generated.model.OpenTofuResult;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.utils.OpenTofuProviderHelper;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.utils.TfResourceTransUtils;
import org.eclipse.xpanse.modules.deployment.utils.DeployEnvironments;
import org.eclipse.xpanse.modules.deployment.utils.ScriptsGitRepoManage;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
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
 * Implementation of the deployment with OpenTofu.
 */
@Slf4j
@Component
@ConditionalOnMissingBean(OpenTofuMakerDeployment.class)
public class OpenTofuLocalDeployment implements Deployer {

    public static final String VERSION_FILE_NAME = "version.tf";
    public static final String SCRIPT_FILE_NAME = "resources.tf";
    public static final String STATE_FILE_NAME = "terraform.tfstate";
    public static final String TF_DEBUG_FLAG = "TF_LOG";
    private final DeployEnvironments deployEnvironments;
    private final OpenTofuLocalConfig openTofuLocalConfig;
    private final OpenTofuProviderHelper openTofuProviderHelper;
    private final Executor taskExecutor;
    private final OpenTofuDeploymentResultCallbackManager openTofuDeploymentResultCallbackManager;
    private final DeployServiceEntityHandler deployServiceEntityHandler;
    private final ScriptsGitRepoManage scriptsGitRepoManage;

    /**
     * Initializes the OpenTofu deployer.
     */
    @Autowired
    public OpenTofuLocalDeployment(DeployEnvironments deployEnvironments,
                                   OpenTofuLocalConfig openTofuLocalConfig,
                                   OpenTofuProviderHelper openTofuProviderHelper,
                                   @Qualifier("xpanseAsyncTaskExecutor") Executor taskExecutor,
                                   OpenTofuDeploymentResultCallbackManager
                                               openTofuDeploymentResultCallbackManager,
                                   DeployServiceEntityHandler deployServiceEntityHandler,
                                   ScriptsGitRepoManage scriptsGitRepoManage) {
        this.deployEnvironments = deployEnvironments;
        this.openTofuLocalConfig = openTofuLocalConfig;
        this.openTofuProviderHelper = openTofuProviderHelper;
        this.taskExecutor = taskExecutor;
        this.openTofuDeploymentResultCallbackManager = openTofuDeploymentResultCallbackManager;
        this.deployServiceEntityHandler = deployServiceEntityHandler;
        this.scriptsGitRepoManage = scriptsGitRepoManage;
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
        prepareDeployWorkspaceWithScripts(task, workspace);
        OpenTofuLocalExecutor executor = getExecutorForDeployTask(task, workspace, true);
        // Execute the openTofu command asynchronously.
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
        prepareDestroyWorkspaceWithScripts(task, workspace, tfState);
        OpenTofuLocalExecutor executor = getExecutorForDeployTask(task, workspace, false);
        // Execute the openTofu command asynchronously.
        taskExecutor.execute(() -> {
            OpenTofuResult openTofuResult = new OpenTofuResult();
            openTofuResult.setDestroyScenario(OpenTofuResult.DestroyScenarioEnum.fromValue(
                    task.getDestroyScenario().toValue()));
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
        prepareDeployWorkspaceWithScripts(task, workspace);
        // Execute the openTofu command.
        OpenTofuLocalExecutor executor = getExecutorForDeployTask(task, workspace, true);
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
        try (Stream<Path> pathStream = Files.walk(path)) {
            pathStream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
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
    private OpenTofuLocalExecutor getExecutorForDeployTask(DeployTask task, String workspace,
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
        return getExecutor(envVariables, inputVariables, workspace, task.getOcl());
    }

    private OpenTofuLocalExecutor getExecutor(Map<String, String> envVariables,
                                              Map<String, Object> inputVariables, String workspace,
                                              Ocl ocl) {
        if (openTofuLocalConfig.isDebugEnabled()) {
            log.info("Debug enabled for OpenTofu CLI with level {}",
                    openTofuLocalConfig.getDebugLogLevel());
            envVariables.put(TF_DEBUG_FLAG, openTofuLocalConfig.getDebugLogLevel());
        }
        return new OpenTofuLocalExecutor(envVariables, inputVariables, workspace,
                getSubDirectory(ocl));
    }

    private void prepareDeployWorkspaceWithScripts(DeployTask deployTask, String workspace) {
        if (Objects.nonNull(deployTask.getOcl().getDeployment().getDeployer())) {
            createScriptFile(deployTask.getDeployRequest().getCsp(),
                    deployTask.getDeployRequest().getRegion(), workspace,
                    deployTask.getOcl().getDeployment().getDeployer());
        }
        if (Objects.nonNull(deployTask.getOcl().getDeployment().getScriptsRepo())) {
            scriptsGitRepoManage.checkoutScripts(workspace,
                    deployTask.getOcl().getDeployment().getScriptsRepo());
        }
    }

    private void prepareDestroyWorkspaceWithScripts(DeployTask deployTask, String workspace,
                                                    String tfState) {
        log.info("start create open tofu destroy workspace and script");
        File parentPath = new File(workspace);
        if (!parentPath.exists() || !parentPath.isDirectory()) {
            parentPath.mkdirs();
        }
        if (Objects.nonNull(deployTask.getOcl().getDeployment().getDeployer())) {
            createDestroyScriptFile(deployTask.getDeployRequest().getCsp(),
                    deployTask.getDeployRequest().getRegion(), workspace, tfState);
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
     * Create OpenTofu script.
     *
     * @param csp       the cloud service provider.
     * @param workspace the workspace for OpenTofu.
     * @param script    OpenTofu scripts of the task.
     */
    private void createScriptFile(Csp csp, String region, String workspace, String script) {
        log.info("start create OpenTofu script");
        String verScript = openTofuProviderHelper.getProvider(csp, region);
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

        String verScript = openTofuProviderHelper.getProvider(csp, region);
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
    public DeploymentScriptValidationResult validate(Ocl ocl) {
        String workspace = getWorkspacePath(UUID.randomUUID());
        // Create the workspace.
        buildWorkspace(workspace);
        if (Objects.nonNull(ocl.getDeployment().getDeployer())) {
            createScriptFile(ocl.getCloudServiceProvider().getName(),
                    ocl.getCloudServiceProvider().getRegions().getFirst().getName(), workspace,
                    ocl.getDeployment().getDeployer());
        } else {
            scriptsGitRepoManage.checkoutScripts(workspace, ocl.getDeployment().getScriptsRepo());
        }
        OpenTofuLocalExecutor executor =
                getExecutor(new HashMap<>(), new HashMap<>(), workspace, ocl);
        return executor.tfValidate();
    }

    private @Nullable String getSubDirectory(Ocl ocl) {
        if (Objects.nonNull(ocl.getDeployment().getDeployer())) {
            return null;
        } else if (Objects.nonNull(ocl.getDeployment().getScriptsRepo())) {
            return ocl.getDeployment().getScriptsRepo().getScriptsPath();
        }
        return null;
    }
}