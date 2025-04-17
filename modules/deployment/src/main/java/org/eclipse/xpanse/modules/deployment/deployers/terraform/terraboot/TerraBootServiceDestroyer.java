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

/** Bean to manage service destroy via terra-boot. */
@Slf4j
@Component
@Profile("terra-boot")
public class TerraBootServiceDestroyer {

    private final TerraformFromScriptsApi terraformFromScriptsApi;
    private final TerraformFromGitRepoApi terraformFromGitRepoApi;
    private final TerraBootHelper terraBootHelper;
    private final ServiceDeploymentEntityHandler deploymentEntityHandler;

    /** Constructor for TerraBootServiceDestroyer bean. */
    public TerraBootServiceDestroyer(
            TerraformFromScriptsApi terraformFromScriptsApi,
            TerraformFromGitRepoApi terraformFromGitRepoApi,
            TerraBootHelper terraBootHelper,
            ServiceDeploymentEntityHandler deploymentEntityHandler) {
        this.terraformFromScriptsApi = terraformFromScriptsApi;
        this.terraformFromGitRepoApi = terraformFromGitRepoApi;
        this.terraBootHelper = terraBootHelper;
        this.deploymentEntityHandler = deploymentEntityHandler;
    }

    /** method to perform service destroy using scripts provided in OCL. */
    public DeployResult destroyFromScripts(DeployTask deployTask) {
        ServiceDeploymentEntity serviceDeploymentEntity =
                deploymentEntityHandler.getServiceDeploymentEntity(deployTask.getServiceId());
        String resourceState = TfResourceTransUtils.getStoredStateContent(serviceDeploymentEntity);
        DeployResult result = new DeployResult();
        TerraformAsyncRequestWithScripts request =
                getDestroyFromScriptsRequest(deployTask, resourceState);
        try {
            terraformFromScriptsApi.asyncDestroyWithScripts(request);
            result.setOrderId(deployTask.getOrderId());
            return result;
        } catch (RestClientException e) {
            throw new TerraBootRequestFailedException(getErrorMessage(deployTask, e));
        }
    }

    /** method to perform service destroy using scripts form GIT repo. */
    public DeployResult destroyFromGitRepo(DeployTask deployTask) {
        ServiceDeploymentEntity serviceDeploymentEntity =
                deploymentEntityHandler.getServiceDeploymentEntity(deployTask.getServiceId());
        String resourceState = TfResourceTransUtils.getStoredStateContent(serviceDeploymentEntity);
        DeployResult result = new DeployResult();
        TerraformAsyncRequestWithScriptsGitRepo request =
                getDestroyFromGitRepoRequest(deployTask, resourceState);
        try {
            terraformFromGitRepoApi.asyncDestroyFromGitRepo(request);
            result.setOrderId(deployTask.getOrderId());
            return result;
        } catch (RestClientException e) {
            throw new TerraBootRequestFailedException(getErrorMessage(deployTask, e));
        }
    }

    private TerraformAsyncRequestWithScripts getDestroyFromScriptsRequest(
            DeployTask task, String stateFile) throws TerraBootRequestFailedException {
        TerraformAsyncRequestWithScripts request = new TerraformAsyncRequestWithScripts();
        request.setRequestId(task.getOrderId());
        request.setRequestType(TerraformAsyncRequestWithScripts.RequestTypeEnum.DESTROY);
        request.setTerraformVersion(task.getOcl().getDeployment().getDeployerTool().getVersion());
        request.setScriptFiles(
                task.getOcl().getDeployment().getTerraformDeployment().getScriptFiles());
        request.setTfState(stateFile);
        request.setVariables(terraBootHelper.getInputVariables(task, false));
        request.setEnvVariables(terraBootHelper.getEnvironmentVariables(task));
        request.setWebhookConfig(terraBootHelper.getWebhookConfigWithTask(task));
        return request;
    }

    private TerraformAsyncRequestWithScriptsGitRepo getDestroyFromGitRepoRequest(
            DeployTask task, String stateFile) throws TerraBootRequestFailedException {
        TerraformAsyncRequestWithScriptsGitRepo request =
                new TerraformAsyncRequestWithScriptsGitRepo();
        request.setRequestId(task.getOrderId());
        request.setRequestType(TerraformAsyncRequestWithScriptsGitRepo.RequestTypeEnum.DESTROY);
        request.setTerraformVersion(task.getOcl().getDeployment().getDeployerTool().getVersion());
        request.setTfState(stateFile);
        request.setVariables(terraBootHelper.getInputVariables(task, false));
        request.setEnvVariables(terraBootHelper.getEnvironmentVariables(task));
        request.setWebhookConfig(terraBootHelper.getWebhookConfigWithTask(task));
        request.setGitRepoDetails(
                terraBootHelper.convertTerraformScriptsGitRepoDetailsFromDeployFromGitRepo(
                        task.getOcl().getDeployment().getTerraformDeployment().getScriptsRepo()));
        return request;
    }

    private String getErrorMessage(DeployTask deployTask, RestClientException e) {
        String errorMsg =
                String.format(
                        "Failed to destroy service %s by order %s using terra-boot. Error: %s",
                        deployTask.getServiceId(), deployTask.getOrderId(), e.getMessage());
        log.error(errorMsg, e);
        return errorMsg;
    }
}
