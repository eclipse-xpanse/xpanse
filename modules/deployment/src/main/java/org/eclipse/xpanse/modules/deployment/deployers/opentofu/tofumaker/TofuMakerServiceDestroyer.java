/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.deployment.DeployServiceEntityHandler;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.exceptions.OpenTofuMakerRequestFailedException;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.api.OpenTofuFromGitRepoApi;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.api.OpenTofuFromScriptsApi;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuAsyncDestroyFromGitRepoRequest;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuAsyncDestroyFromScriptsRequest;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.utils.TfResourceTransUtils;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/**
 * Bean to manage service destroy via tofu-maker.
 */
@Slf4j
@Component
@Profile("tofu-maker")
public class TofuMakerServiceDestroyer {

    private final OpenTofuFromScriptsApi openTofuFromScriptsApi;
    private final OpenTofuFromGitRepoApi openTofuFromGitRepoApi;
    private final TofuMakerHelper tofuMakerHelper;
    private final DeployServiceEntityHandler deployServiceEntityHandler;

    /**
     * Constructor for OpenTofuMakerServiceDestroyer bean.
     */
    public TofuMakerServiceDestroyer(OpenTofuFromScriptsApi openTofuFromScriptsApi,
                                     OpenTofuFromGitRepoApi openTofuFromGitRepoApi,
                                     TofuMakerHelper tofuMakerHelper,
                                     DeployServiceEntityHandler deployServiceEntityHandler) {
        this.openTofuFromScriptsApi = openTofuFromScriptsApi;
        this.openTofuFromGitRepoApi = openTofuFromGitRepoApi;
        this.tofuMakerHelper = tofuMakerHelper;
        this.deployServiceEntityHandler = deployServiceEntityHandler;
    }

    /**
     * method to perform service destroy using scripts provided in OCL.
     */
    public DeployResult destroyFromScripts(DeployTask deployTask) {
        DeployServiceEntity deployServiceEntity =
                this.deployServiceEntityHandler.getDeployServiceEntity(deployTask.getServiceId());
        String resourceState = TfResourceTransUtils.getStoredStateContent(deployServiceEntity);
        DeployResult result = new DeployResult();
        OpenTofuAsyncDestroyFromScriptsRequest request =
                getDestroyFromScriptsRequest(deployTask, resourceState);
        try {
            openTofuFromScriptsApi.asyncDestroyWithScripts(request);
            result.setOrderId(deployTask.getOrderId());
            result.setServiceId(deployTask.getServiceId());
            return result;
        } catch (RestClientException e) {
            log.error("tofu-maker destroy service failed. service id: {} , error:{} ",
                    deployTask.getServiceId(), e.getMessage());
            throw new OpenTofuMakerRequestFailedException(e.getMessage());
        }
    }

    /**
     * method to perform service destroy using scripts form GIT repo.
     */
    public DeployResult destroyFromGitRepo(DeployTask deployTask) {
        DeployServiceEntity deployServiceEntity =
                this.deployServiceEntityHandler.getDeployServiceEntity(deployTask.getServiceId());
        String resourceState = TfResourceTransUtils.getStoredStateContent(deployServiceEntity);
        DeployResult result = new DeployResult();
        OpenTofuAsyncDestroyFromGitRepoRequest request =
                getDestroyFromGitRepoRequest(deployTask, resourceState);
        try {
            openTofuFromGitRepoApi.asyncDestroyFromGitRepo(request);
            result.setOrderId(deployTask.getOrderId());
            result.setServiceId(deployTask.getServiceId());
            return result;
        } catch (RestClientException e) {
            log.error("tofu-maker deploy service failed. service id: {} , error:{} ",
                    deployTask.getServiceId(), e.getMessage());
            throw new OpenTofuMakerRequestFailedException(e.getMessage());
        }
    }

    private OpenTofuAsyncDestroyFromScriptsRequest getDestroyFromScriptsRequest(DeployTask task,
             String stateFile) throws OpenTofuMakerRequestFailedException {
        OpenTofuAsyncDestroyFromScriptsRequest request =
                new OpenTofuAsyncDestroyFromScriptsRequest();
        request.setRequestId(task.getOrderId());
        request.setScripts(tofuMakerHelper.getFiles(task));
        request.setTfState(stateFile);
        request.setVariables(tofuMakerHelper.getInputVariables(task, false));
        request.setEnvVariables(tofuMakerHelper.getEnvironmentVariables(task));
        request.setWebhookConfig(tofuMakerHelper.getWebhookConfigWithTask(task));
        return request;
    }

    private OpenTofuAsyncDestroyFromGitRepoRequest getDestroyFromGitRepoRequest(DeployTask task,
                                                                                String stateFile)
            throws OpenTofuMakerRequestFailedException {
        OpenTofuAsyncDestroyFromGitRepoRequest request =
                new OpenTofuAsyncDestroyFromGitRepoRequest();
        request.setRequestId(task.getOrderId());
        request.setTfState(stateFile);
        request.setVariables(tofuMakerHelper.getInputVariables(task, false));
        request.setEnvVariables(tofuMakerHelper.getEnvironmentVariables(task));
        request.setWebhookConfig(tofuMakerHelper.getWebhookConfigWithTask(task));
        request.setGitRepoDetails(
                tofuMakerHelper.convertOpenTofuScriptGitRepoDetailsFromDeployFromGitRepo(
                        task.getOcl().getDeployment().getScriptsRepo()));
        return request;
    }

}
