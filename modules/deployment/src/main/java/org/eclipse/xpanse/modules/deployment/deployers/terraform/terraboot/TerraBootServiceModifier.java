/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.deployment.ServiceDeploymentEntityHandler;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.exceptions.TerraBootRequestFailedException;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.api.TerraformFromGitRepoApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.api.TerraformFromScriptsApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.model.TerraformAsyncRequestWithScripts;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.model.TerraformAsyncRequestWithScriptsGitRepo;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.utils.TfResourceTransUtils;
import org.eclipse.xpanse.modules.models.service.deployment.DeployResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/** Bean to manage service modify via terra-boot. */
@Slf4j
@Component
@Profile("terra-boot")
public class TerraBootServiceModifier {

    private final TerraformFromScriptsApi terraformFromScriptsApi;
    private final TerraformFromGitRepoApi terraformFromGitRepoApi;
    private final TerraBootHelper terraBootHelper;
    private final ServiceDeploymentEntityHandler deploymentEntityHandler;

    /** Constructor for TerraBootServiceDestroyer bean. */
    public TerraBootServiceModifier(
            TerraformFromScriptsApi terraformFromScriptsApi,
            TerraformFromGitRepoApi terraformFromGitRepoApi,
            TerraBootHelper terraBootHelper,
            ServiceDeploymentEntityHandler deploymentEntityHandler) {
        this.terraformFromScriptsApi = terraformFromScriptsApi;
        this.terraformFromGitRepoApi = terraformFromGitRepoApi;
        this.terraBootHelper = terraBootHelper;
        this.deploymentEntityHandler = deploymentEntityHandler;
    }

    /** method to perform service modify using scripts provided in OCL. */
    public DeployResult modifyFromScripts(DeployTask deployTask) {
        ServiceDeploymentEntity serviceDeploymentEntity =
                this.deploymentEntityHandler.getServiceDeploymentEntity(deployTask.getServiceId());
        String resourceState = TfResourceTransUtils.getStoredStateContent(serviceDeploymentEntity);
        DeployResult result = new DeployResult();
        result.setOrderId(deployTask.getOrderId());
        TerraformAsyncRequestWithScripts request =
                getModifyFromScriptsRequest(deployTask, resourceState);
        try {
            terraformFromScriptsApi.asyncModifyWithScripts(request);
            return result;
        } catch (RestClientException e) {
            throw new TerraBootRequestFailedException(getErrorMessage(deployTask, e));
        }
    }

    /** method to perform service modify using scripts form GIT repo. */
    public DeployResult modifyFromGitRepo(DeployTask deployTask) {
        ServiceDeploymentEntity serviceDeploymentEntity =
                this.deploymentEntityHandler.getServiceDeploymentEntity(deployTask.getServiceId());
        String resourceState = TfResourceTransUtils.getStoredStateContent(serviceDeploymentEntity);
        DeployResult result = new DeployResult();
        result.setOrderId(deployTask.getOrderId());
        TerraformAsyncRequestWithScriptsGitRepo request =
                getModifyFromGitRepoRequest(deployTask, resourceState);
        try {
            terraformFromGitRepoApi.asyncModifyFromGitRepo(request);
            return result;
        } catch (RestClientException e) {
            throw new TerraBootRequestFailedException(getErrorMessage(deployTask, e));
        }
    }

    private TerraformAsyncRequestWithScripts getModifyFromScriptsRequest(
            DeployTask task, String stateFile) throws TerraBootRequestFailedException {
        TerraformAsyncRequestWithScripts request = new TerraformAsyncRequestWithScripts();
        request.setRequestId(task.getOrderId());
        request.setRequestType(TerraformAsyncRequestWithScripts.RequestTypeEnum.MODIFY);
        request.setTerraformVersion(task.getOcl().getDeployment().getDeployerTool().getVersion());
        request.setScriptFiles(task.getOcl().getDeployment().getScriptFiles());
        request.setTfState(stateFile);
        request.setVariables(terraBootHelper.getInputVariables(task, false));
        request.setEnvVariables(terraBootHelper.getEnvironmentVariables(task));
        request.setWebhookConfig(terraBootHelper.getWebhookConfigWithTask(task));
        return request;
    }

    private TerraformAsyncRequestWithScriptsGitRepo getModifyFromGitRepoRequest(
            DeployTask task, String stateFile) throws TerraBootRequestFailedException {
        TerraformAsyncRequestWithScriptsGitRepo request =
                new TerraformAsyncRequestWithScriptsGitRepo();
        request.setRequestId(task.getOrderId());
        request.setRequestType(TerraformAsyncRequestWithScriptsGitRepo.RequestTypeEnum.MODIFY);
        request.setTerraformVersion(task.getOcl().getDeployment().getDeployerTool().getVersion());
        request.setTfState(stateFile);
        request.setVariables(terraBootHelper.getInputVariables(task, false));
        request.setEnvVariables(terraBootHelper.getEnvironmentVariables(task));
        request.setWebhookConfig(terraBootHelper.getWebhookConfigWithTask(task));
        request.setGitRepoDetails(
                terraBootHelper.convertTerraformScriptsGitRepoDetailsFromDeployFromGitRepo(
                        task.getOcl().getDeployment().getScriptsRepo()));
        return request;
    }

    private String getErrorMessage(DeployTask deployTask, RestClientException e) {
        String errorMsg =
                String.format(
                        "Failed to modify service %s by order %s using terra-boot. Error: %s",
                        deployTask.getServiceId(), deployTask.getOrderId(), e.getMessage());
        log.error(errorMsg, e);
        return errorMsg;
    }
}
