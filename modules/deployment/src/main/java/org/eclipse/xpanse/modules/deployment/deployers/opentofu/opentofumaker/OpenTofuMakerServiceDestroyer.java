/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofumaker;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.deployment.DeployServiceEntityHandler;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.exceptions.OpenTofuMakerRequestFailedException;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofumaker.generated.api.OpenTofuFromGitRepoApi;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofumaker.generated.api.OpenTofuFromScriptsApi;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofumaker.generated.model.OpenTofuAsyncDestroyFromGitRepoRequest;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofumaker.generated.model.OpenTofuAsyncDestroyFromScriptsRequest;
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
public class OpenTofuMakerServiceDestroyer {

    private final OpenTofuFromScriptsApi openTofuFromScriptsApi;
    private final OpenTofuFromGitRepoApi openTofuFromGitRepoApi;
    private final OpenTofuMakerHelper openTofuMakerHelper;
    private final DeployServiceEntityHandler deployServiceEntityHandler;

    /**
     * Constructor for OpenTofuMakerServiceDestroyer bean.
     */
    public OpenTofuMakerServiceDestroyer(OpenTofuFromScriptsApi openTofuFromScriptsApi,
                                         OpenTofuFromGitRepoApi openTofuFromGitRepoApi,
                                         OpenTofuMakerHelper openTofuMakerHelper,
                                         DeployServiceEntityHandler deployServiceEntityHandler) {
        this.openTofuFromScriptsApi = openTofuFromScriptsApi;
        this.openTofuFromGitRepoApi = openTofuFromGitRepoApi;
        this.openTofuMakerHelper = openTofuMakerHelper;
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
        OpenTofuAsyncDestroyFromScriptsRequest request =
                getDestroyFromScriptsRequest(deployTask, resourceState);
        try {
            openTofuMakerHelper.setHeaderTokenByProfiles();
            openTofuFromScriptsApi.asyncDestroyWithScripts(request, deployTask.getId());
            result.setId(deployTask.getId());
            return result;
        } catch (RestClientException e) {
            log.error("tofu-maker destroy service failed. service id: {} , error:{} ",
                    deployTask.getId(), e.getMessage());
            throw new OpenTofuMakerRequestFailedException(e.getMessage());
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
        OpenTofuAsyncDestroyFromGitRepoRequest request =
                getDestroyFromGitRepoRequest(deployTask, resourceState);
        try {
            openTofuMakerHelper.setHeaderTokenByProfiles();
            openTofuFromGitRepoApi.asyncDestroyFromGitRepo(request, deployTask.getId());
            result.setId(deployTask.getId());
            return result;
        } catch (RestClientException e) {
            log.error("tofu-maker deploy service failed. service id: {} , error:{} ",
                    deployTask.getId(), e.getMessage());
            throw new OpenTofuMakerRequestFailedException(e.getMessage());
        }
    }

    private OpenTofuAsyncDestroyFromScriptsRequest getDestroyFromScriptsRequest(DeployTask task,
                                                                                String stateFile)
            throws OpenTofuMakerRequestFailedException {
        OpenTofuAsyncDestroyFromScriptsRequest request =
                new OpenTofuAsyncDestroyFromScriptsRequest();
        request.setScripts(openTofuMakerHelper.getFiles(task));
        request.setTfState(stateFile);
        request.setVariables(openTofuMakerHelper.getInputVariables(task, false));
        request.setEnvVariables(openTofuMakerHelper.getEnvironmentVariables(task));
        request.setWebhookConfig(openTofuMakerHelper.getWebhookConfig(task, true));
        request.setDestroyScenario(
                OpenTofuAsyncDestroyFromScriptsRequest.DestroyScenarioEnum.fromValue(
                        task.getDestroyScenario().toValue()));
        return request;
    }

    private OpenTofuAsyncDestroyFromGitRepoRequest getDestroyFromGitRepoRequest(DeployTask task,
                                                                                String stateFile)
            throws OpenTofuMakerRequestFailedException {
        OpenTofuAsyncDestroyFromGitRepoRequest request =
                new OpenTofuAsyncDestroyFromGitRepoRequest();
        request.setTfState(stateFile);
        request.setVariables(openTofuMakerHelper.getInputVariables(task, false));
        request.setEnvVariables(openTofuMakerHelper.getEnvironmentVariables(task));
        request.setWebhookConfig(openTofuMakerHelper.getWebhookConfig(task, true));
        request.setDestroyScenario(
                OpenTofuAsyncDestroyFromGitRepoRequest.DestroyScenarioEnum.fromValue(
                        task.getDestroyScenario().toValue()));
        return request;
    }

}
