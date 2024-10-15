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
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformAsyncModifyFromGitRepoRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformAsyncModifyFromScriptsRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.utils.TfResourceTransUtils;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/**
 * Bean to manage service modify via terraform-boot.
 */
@Slf4j
@Component
@Profile("terraform-boot")
public class TerraformBootServiceModifier {

    private final TerraformFromScriptsApi terraformFromScriptsApi;
    private final TerraformFromGitRepoApi terraformFromGitRepoApi;
    private final TerraformBootHelper terraformBootHelper;
    private final DeployServiceEntityHandler deployServiceEntityHandler;

    /**
     * Constructor for TerraformBootServiceDestroyer bean.
     */
    public TerraformBootServiceModifier(TerraformFromScriptsApi terraformFromScriptsApi,
                                        TerraformFromGitRepoApi terraformFromGitRepoApi,
                                        TerraformBootHelper terraformBootHelper,
                                        DeployServiceEntityHandler deployServiceEntityHandler) {
        this.terraformFromScriptsApi = terraformFromScriptsApi;
        this.terraformFromGitRepoApi = terraformFromGitRepoApi;
        this.terraformBootHelper = terraformBootHelper;
        this.deployServiceEntityHandler = deployServiceEntityHandler;
    }

    /**
     * method to perform service modify using scripts provided in OCL.
     */
    public DeployResult modifyFromScripts(DeployTask deployTask) {
        DeployServiceEntity deployServiceEntity =
                this.deployServiceEntityHandler.getDeployServiceEntity(deployTask.getServiceId());
        String resourceState = TfResourceTransUtils.getStoredStateContent(deployServiceEntity);
        DeployResult result = new DeployResult();
        result.setServiceId(deployTask.getServiceId());
        result.setOrderId(deployTask.getOrderId());
        TerraformAsyncModifyFromScriptsRequest request =
                getModifyFromScriptsRequest(deployTask, resourceState);
        try {
            terraformFromScriptsApi.asyncModifyWithScripts(request);
            return result;
        } catch (RestClientException e) {
            log.error("terraform-boot modify service failed. service id: {} , error:{} ",
                    deployTask.getServiceId(), e.getMessage());
            throw new TerraformBootRequestFailedException(e.getMessage());
        }
    }

    /**
     * method to perform service modify using scripts form GIT repo.
     */
    public DeployResult modifyFromGitRepo(DeployTask deployTask) {
        DeployServiceEntity deployServiceEntity =
                this.deployServiceEntityHandler.getDeployServiceEntity(deployTask.getServiceId());
        String resourceState = TfResourceTransUtils.getStoredStateContent(deployServiceEntity);
        DeployResult result = new DeployResult();
        result.setServiceId(deployTask.getServiceId());
        result.setOrderId(deployTask.getOrderId());
        TerraformAsyncModifyFromGitRepoRequest request =
                getModifyFromGitRepoRequest(deployTask, resourceState);
        try {
            terraformFromGitRepoApi.asyncModifyFromGitRepo(request);
            return result;
        } catch (RestClientException e) {
            log.error("terraform-boot modify service failed. service id: {} , error:{} ",
                    deployTask.getServiceId(), e.getMessage());
            throw new TerraformBootRequestFailedException(e.getMessage());
        }
    }

    private TerraformAsyncModifyFromScriptsRequest getModifyFromScriptsRequest(DeployTask task,
                                                                               String stateFile)
            throws TerraformBootRequestFailedException {
        TerraformAsyncModifyFromScriptsRequest request =
                new TerraformAsyncModifyFromScriptsRequest();
        request.setRequestId(task.getOrderId());
        request.setTerraformVersion(task.getOcl().getDeployment().getDeployerTool().getVersion());
        request.setScripts(terraformBootHelper.getFiles(task));
        request.setTfState(stateFile);
        request.setVariables(terraformBootHelper.getInputVariables(task, false));
        request.setEnvVariables(terraformBootHelper.getEnvironmentVariables(task));
        request.setWebhookConfig(terraformBootHelper.getWebhookConfigWithTask(task));
        return request;
    }

    private TerraformAsyncModifyFromGitRepoRequest getModifyFromGitRepoRequest(DeployTask task,
                                                                               String stateFile)
            throws TerraformBootRequestFailedException {
        TerraformAsyncModifyFromGitRepoRequest request =
                new TerraformAsyncModifyFromGitRepoRequest();
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
