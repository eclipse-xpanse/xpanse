/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofulocal;

import static org.eclipse.xpanse.modules.async.TaskConfiguration.ASYNC_EXECUTOR_NAME;

import jakarta.annotation.Resource;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.deployment.ServiceDeploymentEntityHandler;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.callbacks.OpenTofuDeploymentResultCallbackManager;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofulocal.config.OpenTofuLocalConfig;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.TofuMakerDeployment;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuResult;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.utils.TfResourceTransUtils;
import org.eclipse.xpanse.modules.deployment.utils.DeployEnvironments;
import org.eclipse.xpanse.modules.deployment.utils.DeploymentScriptsHelper;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.servicetemplate.Deployment;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.deployment.Deployer;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeploymentScriptValidationResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/** Implementation of the deployment with OpenTofu. */
@Slf4j
@Component
@ConditionalOnMissingBean(TofuMakerDeployment.class)
public class OpenTofuLocalDeployment implements Deployer {
    public static final String TF_DEBUG_FLAG = "TF_LOG";
    @Resource private OpenTofuInstaller openTofuInstaller;
    @Resource private DeployEnvironments deployEnvironments;
    @Resource private OpenTofuLocalConfig openTofuLocalConfig;

    @Resource(name = ASYNC_EXECUTOR_NAME)
    private Executor taskExecutor;

    @Resource private OpenTofuDeploymentResultCallbackManager openTofuResultCallbackManager;
    @Resource private ServiceDeploymentEntityHandler serviceDeploymentEntityHandler;
    @Resource private DeploymentScriptsHelper scriptsHelper;

    /**
     * Deploy the DeployTask.
     *
     * @param task the task for the deployment.
     */
    @Override
    public DeployResult deploy(DeployTask task) {
        DeployResult deployResult = new DeployResult();
        deployResult.setOrderId(task.getOrderId());
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
        ServiceDeploymentEntity serviceDeploymentEntity =
                serviceDeploymentEntityHandler.getServiceDeploymentEntity(task.getServiceId());
        String resourceState = TfResourceTransUtils.getStoredStateContent(serviceDeploymentEntity);
        if (StringUtils.isBlank(resourceState)) {
            String errorMsg =
                    String.format(
                            "tfState of deployed service with id %s not found.",
                            task.getServiceId());
            log.error(errorMsg);
            throw new ServiceNotDeployedException(errorMsg);
        }
        DeployResult destroyResult = new DeployResult();
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
        ServiceDeploymentEntity serviceDeploymentEntity =
                serviceDeploymentEntityHandler.getServiceDeploymentEntity(task.getServiceId());
        String resourceState = TfResourceTransUtils.getStoredStateContent(serviceDeploymentEntity);
        if (StringUtils.isBlank(resourceState)) {
            String errorMsg =
                    String.format(
                            "tfState of service deployment with id %s not found.",
                            task.getServiceId());
            log.error(errorMsg);
            throw new ServiceNotDeployedException(errorMsg);
        }
        DeployResult modifyResult = new DeployResult();
        modifyResult.setOrderId(task.getOrderId());
        asyncExecModify(task, resourceState);
        return modifyResult;
    }

    private void asyncExecDeploy(DeployTask task) {
        String workspace =
                scriptsHelper.createWorkspaceForTask(
                        getDeployerConfigWorkspace(), task.getOrderId());
        List<File> preparedFiles =
                scriptsHelper.prepareDeploymentScripts(
                        workspace, task.getOcl().getDeployment(), null);
        OpenTofuLocalExecutor executor = getExecutorForDeployTask(task, workspace, true);
        // Execute the openTofu command asynchronously.
        taskExecutor.execute(
                () -> {
                    OpenTofuResult openTofuResult = new OpenTofuResult();
                    openTofuResult.setRequestId(task.getOrderId());
                    try {
                        executor.deploy();
                        openTofuResult.setCommandSuccessful(true);
                    } catch (RuntimeException tfEx) {
                        log.error("Execute OpenTofu deploy script failed. {}", tfEx.getMessage());
                        openTofuResult.setCommandSuccessful(false);
                        openTofuResult.setCommandStdError(tfEx.getMessage());
                    }
                    openTofuResult.setTerraformState(
                            scriptsHelper.getTaskTerraformState(executor.getTaskWorkspace()));
                    openTofuResult.setOpenTofuVersionUsed(
                            openTofuInstaller.getExactVersionOfOpenTofu(
                                    executor.getExecutorPath()));
                    openTofuResult.setGeneratedFileContentMap(
                            scriptsHelper.getGeneratedFileContents(
                                    executor.getTaskWorkspace(), preparedFiles));
                    openTofuResultCallbackManager.orderCallback(task.getOrderId(), openTofuResult);
                    scriptsHelper.deleteTaskWorkspace(executor.getTaskWorkspace());
                });
    }

    private void asyncExecDestroy(DeployTask task, String tfState) {
        String workspace =
                scriptsHelper.createWorkspaceForTask(
                        getDeployerConfigWorkspace(), task.getOrderId());
        List<File> preparedFiles =
                scriptsHelper.prepareDeploymentScripts(
                        workspace, task.getOcl().getDeployment(), tfState);
        OpenTofuLocalExecutor executor = getExecutorForDeployTask(task, workspace, false);
        // Execute the openTofu command asynchronously.
        taskExecutor.execute(
                () -> {
                    OpenTofuResult openTofuResult = new OpenTofuResult();
                    openTofuResult.setRequestId(task.getOrderId());
                    try {
                        executor.destroy();
                        openTofuResult.setCommandSuccessful(true);
                    } catch (RuntimeException tfEx) {
                        log.error("Execute openTofu destroy script failed. {}", tfEx.getMessage());
                        openTofuResult.setCommandSuccessful(false);
                        openTofuResult.setCommandStdError(tfEx.getMessage());
                    }
                    openTofuResult.setTerraformState(
                            scriptsHelper.getTaskTerraformState(executor.getTaskWorkspace()));
                    openTofuResult.setOpenTofuVersionUsed(
                            openTofuInstaller.getExactVersionOfOpenTofu(
                                    executor.getExecutorPath()));
                    openTofuResult.setGeneratedFileContentMap(
                            scriptsHelper.getGeneratedFileContents(
                                    executor.getTaskWorkspace(), preparedFiles));
                    openTofuResultCallbackManager.orderCallback(task.getOrderId(), openTofuResult);
                    scriptsHelper.deleteTaskWorkspace(executor.getTaskWorkspace());
                });
    }

    private void asyncExecModify(DeployTask task, String tfState) {
        String workspace =
                scriptsHelper.createWorkspaceForTask(
                        getDeployerConfigWorkspace(), task.getOrderId());
        List<File> preparedFiles =
                scriptsHelper.prepareDeploymentScripts(
                        workspace, task.getOcl().getDeployment(), tfState);
        OpenTofuLocalExecutor executor = getExecutorForDeployTask(task, workspace, true);
        // Execute the terraform command asynchronously.
        taskExecutor.execute(
                () -> {
                    OpenTofuResult openTofuResult = new OpenTofuResult();
                    openTofuResult.setRequestId(task.getOrderId());
                    try {
                        executor.deploy();
                        openTofuResult.setCommandSuccessful(true);
                    } catch (RuntimeException tfEx) {
                        log.error("Execute terraform modify script failed. {}", tfEx.getMessage());
                        openTofuResult.setCommandSuccessful(false);
                        openTofuResult.setCommandStdError(tfEx.getMessage());
                    }
                    openTofuResult.setTerraformState(
                            scriptsHelper.getTaskTerraformState(executor.getTaskWorkspace()));
                    openTofuResult.setOpenTofuVersionUsed(
                            openTofuInstaller.getExactVersionOfOpenTofu(
                                    executor.getExecutorPath()));
                    openTofuResult.setGeneratedFileContentMap(
                            scriptsHelper.getGeneratedFileContents(
                                    executor.getTaskWorkspace(), preparedFiles));
                    openTofuResultCallbackManager.orderCallback(task.getOrderId(), openTofuResult);
                    scriptsHelper.deleteTaskWorkspace(executor.getTaskWorkspace());
                });
    }

    @Override
    public String getDeploymentPlanAsJson(DeployTask task) {
        String workspace =
                scriptsHelper.createWorkspaceForTask(
                        getDeployerConfigWorkspace(), task.getOrderId());
        scriptsHelper.prepareDeploymentScripts(workspace, task.getOcl().getDeployment(), null);
        // Execute the openTofu command.
        OpenTofuLocalExecutor executor = getExecutorForDeployTask(task, workspace, true);
        return executor.getOpenTofuPlanAsJson();
    }

    /**
     * Get an OpenTofuExecutor.
     *
     * @param task the task for the deployment.
     * @param workspace the workspace of the deployment.
     * @param isDeployTask if the task is for deploying a service.
     */
    private OpenTofuLocalExecutor getExecutorForDeployTask(
            DeployTask task, String workspace, boolean isDeployTask) {
        Map<String, String> envVariables = this.deployEnvironments.getEnvironmentVariables(task);
        Map<String, Object> inputVariables =
                this.deployEnvironments.getInputVariables(task, isDeployTask);
        return getExecutor(envVariables, inputVariables, workspace, task.getOcl().getDeployment());
    }

    private OpenTofuLocalExecutor getExecutor(
            Map<String, String> envVariables,
            Map<String, Object> inputVariables,
            String workspace,
            Deployment deployment) {
        if (openTofuLocalConfig.isDebugEnabled()) {
            log.info(
                    "Debug enabled for OpenTofu CLI with level {}",
                    openTofuLocalConfig.getDebugLogLevel());
            envVariables.put(TF_DEBUG_FLAG, openTofuLocalConfig.getDebugLogLevel());
        }
        String executorPath =
                openTofuInstaller.getExecutorPathThatMatchesRequiredVersion(
                        deployment.getDeployerTool().getVersion());
        return new OpenTofuLocalExecutor(
                executorPath,
                envVariables,
                inputVariables,
                scriptsHelper.getScriptsLocationInWorkspace(workspace, deployment));
    }

    /** Get the deployer kind. */
    @Override
    public DeployerKind getDeployerKind() {
        return DeployerKind.OPEN_TOFU;
    }

    /** Validates the OpenTofu script. */
    @Override
    public DeploymentScriptValidationResult validate(Deployment deployment) {
        String workspace =
                scriptsHelper.createWorkspaceForTask(
                        getDeployerConfigWorkspace(), UUID.randomUUID());
        scriptsHelper.prepareDeploymentScripts(workspace, deployment, null);
        OpenTofuLocalExecutor executor =
                getExecutor(new HashMap<>(), new HashMap<>(), workspace, deployment);
        DeploymentScriptValidationResult validationResult = executor.tfValidate();
        validationResult.setDeployerVersionUsed(
                openTofuInstaller.getExactVersionOfOpenTofu(executor.getExecutorPath()));
        return validationResult;
    }

    private String getDeployerConfigWorkspace() {
        return openTofuLocalConfig.getWorkspaceDirectory();
    }
}
