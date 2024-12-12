/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformlocal;

import static org.eclipse.xpanse.modules.async.TaskConfiguration.ASYNC_EXECUTOR_NAME;

import jakarta.annotation.Resource;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.deployment.ServiceDeploymentEntityHandler;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.callbacks.TerraformDeploymentResultCallbackManager;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.TerraformBootDeployment;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformResult;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformlocal.config.TerraformLocalConfig;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.utils.TfResourceTransUtils;
import org.eclipse.xpanse.modules.deployment.utils.DeployEnvironments;
import org.eclipse.xpanse.modules.deployment.utils.DeploymentScriptsHelper;
import org.eclipse.xpanse.modules.models.servicetemplate.Deployment;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.deployment.Deployer;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeploymentScriptValidationResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/** Implementation of the deployment with terraform. */
@Slf4j
@Component
@ConditionalOnMissingBean(TerraformBootDeployment.class)
public class TerraformLocalDeployment implements Deployer {
    public static final String TF_DEBUG_FLAG = "TF_LOG";
    @Resource private TerraformInstaller terraformInstaller;
    @Resource private DeployEnvironments deployEnvironments;
    @Resource private TerraformLocalConfig terraformLocalConfig;

    @Resource(name = ASYNC_EXECUTOR_NAME)
    private Executor taskExecutor;

    @Resource private TerraformDeploymentResultCallbackManager terraformResultCallbackManager;
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
        TerraformLocalExecutor executor = getExecutorForDeployTask(task, workspace, true);
        // Execute the terraform command asynchronously.
        taskExecutor.execute(
                () -> {
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
                    terraformResult.setTerraformState(
                            scriptsHelper.getTaskTerraformState(executor.getTaskWorkspace()));
                    terraformResult.setTerraformVersionUsed(
                            terraformInstaller.getExactVersionOfTerraform(
                                    executor.getExecutorPath()));
                    terraformResult.setGeneratedFileContentMap(
                            scriptsHelper.getGeneratedFileContents(
                                    executor.getTaskWorkspace(), preparedFiles));
                    terraformResultCallbackManager.orderCallback(
                            task.getOrderId(), terraformResult);
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
        TerraformLocalExecutor executor = getExecutorForDeployTask(task, workspace, false);
        // Execute the terraform command asynchronously.
        taskExecutor.execute(
                () -> {
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
                    terraformResult.setTerraformState(
                            scriptsHelper.getTaskTerraformState(executor.getTaskWorkspace()));
                    terraformResult.setTerraformVersionUsed(
                            terraformInstaller.getExactVersionOfTerraform(
                                    executor.getExecutorPath()));
                    terraformResult.setGeneratedFileContentMap(
                            scriptsHelper.getGeneratedFileContents(
                                    executor.getTaskWorkspace(), preparedFiles));
                    terraformResultCallbackManager.orderCallback(
                            task.getOrderId(), terraformResult);
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
        TerraformLocalExecutor executor = getExecutorForDeployTask(task, workspace, true);
        // Execute the terraform command asynchronously.
        taskExecutor.execute(
                () -> {
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
                    terraformResult.setTerraformState(
                            scriptsHelper.getTaskTerraformState(executor.getTaskWorkspace()));
                    terraformResult.setTerraformVersionUsed(
                            terraformInstaller.getExactVersionOfTerraform(
                                    executor.getExecutorPath()));
                    terraformResult.setGeneratedFileContentMap(
                            scriptsHelper.getGeneratedFileContents(
                                    executor.getTaskWorkspace(), preparedFiles));
                    terraformResultCallbackManager.orderCallback(
                            task.getOrderId(), terraformResult);
                    scriptsHelper.deleteTaskWorkspace(executor.getTaskWorkspace());
                });
    }

    @Override
    public String getDeploymentPlanAsJson(DeployTask task) {
        String workspace =
                scriptsHelper.createWorkspaceForTask(
                        getDeployerConfigWorkspace(), task.getOrderId());
        scriptsHelper.prepareDeploymentScripts(workspace, task.getOcl().getDeployment(), null);
        // Execute the terraform command.
        TerraformLocalExecutor executor = getExecutorForDeployTask(task, workspace, true);
        return executor.getTerraformPlanAsJson();
    }

    /**
     * Get a TerraformExecutor.
     *
     * @param task the task for the deployment.
     * @param workspace the workspace of the deployment.
     * @param isDeployTask if the task is for deploying a service.
     */
    private TerraformLocalExecutor getExecutorForDeployTask(
            DeployTask task, String workspace, boolean isDeployTask) {
        Map<String, String> envVariables = this.deployEnvironments.getEnvironmentVariables(task);
        Map<String, Object> inputVariables =
                this.deployEnvironments.getInputVariables(task, isDeployTask);
        return getExecutor(envVariables, inputVariables, workspace, task.getOcl().getDeployment());
    }

    private TerraformLocalExecutor getExecutor(
            Map<String, String> envVariables,
            Map<String, Object> inputVariables,
            String workspace,
            Deployment deployment) {
        if (terraformLocalConfig.isDebugEnabled()) {
            log.info(
                    "Debug enabled for Terraform CLI with level {}",
                    terraformLocalConfig.getDebugLogLevel());
            envVariables.put(TF_DEBUG_FLAG, terraformLocalConfig.getDebugLogLevel());
        }
        String executorPath =
                terraformInstaller.getExecutorPathThatMatchesRequiredVersion(
                        deployment.getDeployerTool().getVersion());
        return new TerraformLocalExecutor(
                executorPath,
                envVariables,
                inputVariables,
                scriptsHelper.getScriptsLocationInWorkspace(workspace, deployment));
    }

    /** Get the deployer kind. */
    @Override
    public DeployerKind getDeployerKind() {
        return DeployerKind.TERRAFORM;
    }

    /** Validates the Terraform script. */
    @Override
    public DeploymentScriptValidationResult validate(Deployment deployment) {
        String workspace =
                scriptsHelper.createWorkspaceForTask(
                        getDeployerConfigWorkspace(), UUID.randomUUID());
        scriptsHelper.prepareDeploymentScripts(workspace, deployment, null);
        TerraformLocalExecutor executor =
                getExecutor(new HashMap<>(), new HashMap<>(), workspace, deployment);
        DeploymentScriptValidationResult validationResult = executor.tfValidate();
        validationResult.setDeployerVersionUsed(
                terraformInstaller.getExactVersionOfTerraform(executor.getExecutorPath()));
        return validationResult;
    }

    private String getDeployerConfigWorkspace() {
        return terraformLocalConfig.getWorkspaceDirectory();
    }
}
