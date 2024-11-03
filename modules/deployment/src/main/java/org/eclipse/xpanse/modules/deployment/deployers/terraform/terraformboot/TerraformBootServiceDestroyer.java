/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
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
        ServiceDeploymentEntity serviceDeploymentEntity =
                this.deployServiceEntityHandler.getDeployServiceEntity(deployTask.getServiceId());
        String resourceState = TfResourceTransUtils.getStoredStateContent(serviceDeploymentEntity);
        DeployResult result = new DeployResult();
        TerraformAsyncDestroyFromScriptsRequest request =
                getDestroyFromScriptsRequest(deployTask, resourceState);
        try {
            terraformFromScriptsApi.asyncDestroyWithScripts(request);
            result.setOrderId(deployTask.getOrderId());
            return result;
        } catch (RestClientException e) {
            log.error("terraform-boot destroy service failed. service id: {} , error:{} ",
                    deployTask.getServiceId(), e.getMessage());
            throw new TerraformBootRequestFailedException(e.getMessage());
        }
    }

    /**
     * method to perform service destroy using scripts form GIT repo.
     */
    public DeployResult destroyFromGitRepo(DeployTask deployTask) {
        ServiceDeploymentEntity serviceDeploymentEntity =
                this.deployServiceEntityHandler.getDeployServiceEntity(deployTask.getServiceId());
        String resourceState = TfResourceTransUtils.getStoredStateContent(serviceDeploymentEntity);
        DeployResult result = new DeployResult();
        TerraformAsyncDestroyFromGitRepoRequest request =
                getDestroyFromGitRepoRequest(deployTask, resourceState);
        try {
            terraformFromGitRepoApi.asyncDestroyFromGitRepo(request);
            result.setOrderId(deployTask.getOrderId());
            return result;
        } catch (RestClientException e) {
            log.error("terraform-boot deploy service failed. service id: {} , error:{} ",
                    deployTask.getServiceId(), e.getMessage());
            throw new TerraformBootRequestFailedException(e.getMessage());
        }
    }

    private TerraformAsyncDestroyFromScriptsRequest getDestroyFromScriptsRequest(DeployTask task,
                                                                                 String stateFile)
            throws TerraformBootRequestFailedException {
        TerraformAsyncDestroyFromScriptsRequest request =
                new TerraformAsyncDestroyFromScriptsRequest();
        request.setRequestId(task.getOrderId());
        request.setTerraformVersion(task.getOcl().getDeployment().getDeployerTool().getVersion());
        request.setScripts(terraformBootHelper.getFiles(task));
        request.setTfState(stateFile);
        request.setVariables(terraformBootHelper.getInputVariables(task, false));
        request.setEnvVariables(terraformBootHelper.getEnvironmentVariables(task));
        request.setWebhookConfig(terraformBootHelper.getWebhookConfigWithTask(task));
        return request;
    }

    private TerraformAsyncDestroyFromGitRepoRequest getDestroyFromGitRepoRequest(DeployTask task,
                                                                                 String stateFile)
            throws TerraformBootRequestFailedException {
        TerraformAsyncDestroyFromGitRepoRequest request =
                new TerraformAsyncDestroyFromGitRepoRequest();
        request.setRequestId(task.getOrderId());
        request.setTerraformVersion(task.getOcl().getDeployment().getDeployerTool().getVersion());
        request.setTfState(stateFile);
        request.setVariables(terraformBootHelper.getInputVariables(task, false));
        request.setEnvVariables(terraformBootHelper.getEnvironmentVariables(task));
        request.setWebhookConfig(terraformBootHelper.getWebhookConfigWithTask(task));
        request.setGitRepoDetails(
                terraformBootHelper.convertTerraformScriptGitRepoDetailsFromDeployFromGitRepo(
                        task.getOcl().getDeployment().getScriptsRepo()));
        return request;
    }

}
