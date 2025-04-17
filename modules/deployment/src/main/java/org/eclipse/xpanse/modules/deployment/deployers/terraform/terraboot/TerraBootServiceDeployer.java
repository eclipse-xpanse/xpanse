/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.exceptions.TerraBootRequestFailedException;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.api.TerraformFromGitRepoApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.api.TerraformFromScriptsApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.model.TerraformAsyncRequestWithScripts;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.model.TerraformAsyncRequestWithScriptsGitRepo;
import org.eclipse.xpanse.modules.models.service.deployment.DeployResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/** Bean to manage service deployment via terra-boot. */
@Slf4j
@Component
@Profile("terra-boot")
public class TerraBootServiceDeployer {

    private final TerraformFromScriptsApi terraformFromScriptsApi;
    private final TerraformFromGitRepoApi terraformFromGitRepoApi;
    private final TerraBootHelper terraBootHelper;

    /** Constructor for TerraBootServiceDeployer bean. */
    public TerraBootServiceDeployer(
            TerraformFromScriptsApi terraformFromScriptsApi,
            TerraformFromGitRepoApi terraformFromGitRepoApi,
            TerraBootHelper terraBootHelper) {
        this.terraformFromScriptsApi = terraformFromScriptsApi;
        this.terraformFromGitRepoApi = terraformFromGitRepoApi;
        this.terraBootHelper = terraBootHelper;
    }

    /** method to perform service deployment using scripts provided in OCL. */
    public DeployResult deployFromScripts(DeployTask deployTask) {
        DeployResult result = new DeployResult();
        TerraformAsyncRequestWithScripts request = getDeployFromScriptsRequest(deployTask);
        try {
            terraformFromScriptsApi.asyncDeployWithScripts(request);
            result.setOrderId(deployTask.getOrderId());
            return result;
        } catch (RestClientException e) {
            throw new TerraBootRequestFailedException(getErrorMessage(deployTask, e));
        }
    }

    /** method to perform service deployment using scripts form GIT repo. */
    public DeployResult deployFromGitRepo(DeployTask deployTask) {
        DeployResult result = new DeployResult();
        TerraformAsyncRequestWithScriptsGitRepo request = getDeployFromGitRepoRequest(deployTask);
        try {
            terraformFromGitRepoApi.asyncDeployFromGitRepo(request);
            result.setOrderId(deployTask.getOrderId());
            return result;
        } catch (RestClientException e) {
            throw new TerraBootRequestFailedException(getErrorMessage(deployTask, e));
        }
    }

    private TerraformAsyncRequestWithScripts getDeployFromScriptsRequest(DeployTask task) {
        TerraformAsyncRequestWithScripts request = new TerraformAsyncRequestWithScripts();
        request.setRequestId(task.getOrderId());
        request.setRequestType(TerraformAsyncRequestWithScripts.RequestTypeEnum.DEPLOY);
        request.setTerraformVersion(task.getOcl().getDeployment().getDeployerTool().getVersion());
        request.setIsPlanOnly(false);
        request.setScriptFiles(
                task.getOcl().getDeployment().getTerraformDeployment().getScriptFiles());
        request.setVariables(terraBootHelper.getInputVariables(task, true));
        request.setEnvVariables(terraBootHelper.getEnvironmentVariables(task));
        request.setWebhookConfig(terraBootHelper.getWebhookConfigWithTask(task));
        return request;
    }

    private TerraformAsyncRequestWithScriptsGitRepo getDeployFromGitRepoRequest(DeployTask task) {
        TerraformAsyncRequestWithScriptsGitRepo request =
                new TerraformAsyncRequestWithScriptsGitRepo();
        request.setRequestId(task.getOrderId());
        request.setRequestType(TerraformAsyncRequestWithScriptsGitRepo.RequestTypeEnum.DEPLOY);
        request.setTerraformVersion(task.getOcl().getDeployment().getDeployerTool().getVersion());
        request.setIsPlanOnly(false);
        request.setVariables(terraBootHelper.getInputVariables(task, true));
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
                        "Failed to deploy service %s by order %s using terra-boot. Error: %s",
                        deployTask.getServiceId(), deployTask.getOrderId(), e.getMessage());
        log.error(errorMsg, e);
        return errorMsg;
    }
}
