/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.deployment.DeployServiceEntityHandler;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.exceptions.TerraformBootRequestFailedException;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.api.TerraformFromGitRepoApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.api.TerraformFromScriptsApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformAsyncDestroyFromGitRepoRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformAsyncDestroyFromScriptsRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.utils.TfResourceTransUtils;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/**
 * Bean to manage service destroy via terraform-boot.
 */
@Slf4j
@Component
@Profile("terraform-boot")
public class TerraformBootServiceDestroyer {

    private final TerraformFromScriptsApi terraformFromScriptsApi;
    private final TerraformFromGitRepoApi terraformFromGitRepoApi;
    private final TerraformBootHelper terraformBootHelper;
    private final DeployServiceEntityHandler deployServiceEntityHandler;

    /**
     * Constructor for TerraformBootServiceDestroyer bean.
     */
    public TerraformBootServiceDestroyer(TerraformFromScriptsApi terraformFromScriptsApi,
                                         TerraformFromGitRepoApi terraformFromGitRepoApi,
                                         TerraformBootHelper terraformBootHelper,
                                         DeployServiceEntityHandler deployServiceEntityHandler) {
        this.terraformFromScriptsApi = terraformFromScriptsApi;
        this.terraformFromGitRepoApi = terraformFromGitRepoApi;
        this.terraformBootHelper = terraformBootHelper;
        this.deployServiceEntityHandler = deployServiceEntityHandler;
    }

    /**
     * method to perform service destroy using scripts provided in OCL.
     */
    public DeployResult destroyFromScripts(DeployTask deployTask) {
        DeployServiceEntity deployServiceEntity =
                this.deployServiceEntityHandler.getDeployServiceEntity(deployTask.getId());
        String resourceState = TfResourceTransUtils.getStoredStateContent(deployServiceEntity);
        DeployResult result = new DeployResult();
        TerraformAsyncDestroyFromScriptsRequest request =
                getDestroyFromScriptsRequest(deployTask, resourceState);
        try {
            terraformBootHelper.setHeaderTokenByProfiles();
            terraformFromScriptsApi.asyncDestroyWithScripts(request, deployTask.getId());
            result.setId(deployTask.getId());
            return result;
        } catch (RestClientException e) {
            log.error("terraform-boot destroy service failed. service id: {} , error:{} ",
                    deployTask.getId(), e.getMessage());
            throw new TerraformBootRequestFailedException(e.getMessage());
        }
    }

    /**
     * method to perform service destroy using scripts form GIT repo.
     */
    public DeployResult destroyFromGitRepo(DeployTask deployTask) {
        DeployServiceEntity deployServiceEntity =
                this.deployServiceEntityHandler.getDeployServiceEntity(deployTask.getId());
        String resourceState = TfResourceTransUtils.getStoredStateContent(deployServiceEntity);
        DeployResult result = new DeployResult();
        TerraformAsyncDestroyFromGitRepoRequest request =
                getDestroyFromGitRepoRequest(deployTask, resourceState);
        try {
            terraformBootHelper.setHeaderTokenByProfiles();
            terraformFromGitRepoApi.asyncDestroyFromGitRepo(request, deployTask.getId());
            result.setId(deployTask.getId());
            return result;
        } catch (RestClientException e) {
            log.error("terraform-boot deploy service failed. service id: {} , error:{} ",
                    deployTask.getId(), e.getMessage());
            throw new TerraformBootRequestFailedException(e.getMessage());
        }
    }

    private TerraformAsyncDestroyFromScriptsRequest getDestroyFromScriptsRequest(DeployTask task,
                                                                                 String stateFile)
            throws TerraformBootRequestFailedException {
        TerraformAsyncDestroyFromScriptsRequest request =
                new TerraformAsyncDestroyFromScriptsRequest();
        request.setScripts(terraformBootHelper.getFiles(task));
        request.setTfState(stateFile);
        request.setVariables(terraformBootHelper.getInputVariables(task, false));
        request.setEnvVariables(terraformBootHelper.getEnvironmentVariables(task));
        request.setWebhookConfig(terraformBootHelper.getWebhookConfig(task, true));
        request.setDestroyScenario(
                TerraformAsyncDestroyFromScriptsRequest.DestroyScenarioEnum.fromValue(
                        task.getDestroyScenario().toValue()));
        return request;
    }

    private TerraformAsyncDestroyFromGitRepoRequest getDestroyFromGitRepoRequest(DeployTask task,
                                                                                 String stateFile)
            throws TerraformBootRequestFailedException {
        TerraformAsyncDestroyFromGitRepoRequest request =
                new TerraformAsyncDestroyFromGitRepoRequest();
        request.setTfState(stateFile);
        request.setVariables(terraformBootHelper.getInputVariables(task, false));
        request.setEnvVariables(terraformBootHelper.getEnvironmentVariables(task));
        request.setWebhookConfig(terraformBootHelper.getWebhookConfig(task, true));
        request.setDestroyScenario(
                TerraformAsyncDestroyFromGitRepoRequest.DestroyScenarioEnum.fromValue(
                        task.getDestroyScenario().toValue()));
        return request;
    }

}
