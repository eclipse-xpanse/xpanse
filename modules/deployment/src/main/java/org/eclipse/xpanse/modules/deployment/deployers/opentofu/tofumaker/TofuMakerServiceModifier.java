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
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuAsyncModifyFromGitRepoRequest;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuAsyncModifyFromScriptsRequest;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.utils.TfResourceTransUtils;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/**
 * Bean to manage service modify via tofu-maker.
 */
@Slf4j
@Component
@Profile("tofu-maker")
public class TofuMakerServiceModifier {
    private final OpenTofuFromScriptsApi openTofuFromScriptsApi;
    private final OpenTofuFromGitRepoApi openTofuFromGitRepoApi;
    private final TofuMakerHelper tofuMakerHelper;
    private final DeployServiceEntityHandler deployServiceEntityHandler;

    /**
     * Constructor for OpenTofuMakerServiceModifyer bean.
     */
    public TofuMakerServiceModifier(OpenTofuFromScriptsApi openTofuFromScriptsApi,
                                     OpenTofuFromGitRepoApi openTofuFromGitRepoApi,
                                     TofuMakerHelper tofuMakerHelper,
                                     DeployServiceEntityHandler deployServiceEntityHandler) {
        this.openTofuFromScriptsApi = openTofuFromScriptsApi;
        this.openTofuFromGitRepoApi = openTofuFromGitRepoApi;
        this.tofuMakerHelper = tofuMakerHelper;
        this.deployServiceEntityHandler = deployServiceEntityHandler;
    }

    /**
     * method to perform service modify using scripts provided in OCL.
     */
    public DeployResult modifyFromScripts(DeployTask deployTask) {
        DeployServiceEntity deployServiceEntity =
                this.deployServiceEntityHandler.getDeployServiceEntity(deployTask.getId());
        String resourceState = TfResourceTransUtils.getStoredStateContent(deployServiceEntity);
        DeployResult result = new DeployResult();
        OpenTofuAsyncModifyFromScriptsRequest request =
                getModifyFromScriptsRequest(deployTask, resourceState);
        try {
            openTofuFromScriptsApi.asyncModifyWithScripts(request, deployTask.getId());
            result.setId(deployTask.getId());
            return result;
        } catch (RestClientException e) {
            log.error("tofu-maker modify service failed. service id: {} , error:{} ",
                    deployTask.getId(), e.getMessage());
            throw new OpenTofuMakerRequestFailedException(e.getMessage());
        }
    }

    /**
     * method to perform service modify using scripts form GIT repo.
     */
    public DeployResult modifyFromGitRepo(DeployTask deployTask) {
        DeployServiceEntity deployServiceEntity =
                this.deployServiceEntityHandler.getDeployServiceEntity(deployTask.getId());
        String resourceState = TfResourceTransUtils.getStoredStateContent(deployServiceEntity);
        DeployResult result = new DeployResult();
        OpenTofuAsyncModifyFromGitRepoRequest request =
                getModifyFromGitRepoRequest(deployTask, resourceState);
        try {
            openTofuFromGitRepoApi.asyncModifyFromGitRepo(request, deployTask.getId());
            result.setId(deployTask.getId());
            return result;
        } catch (RestClientException e) {
            log.error("tofu-maker deploy service failed. service id: {} , error:{} ",
                    deployTask.getId(), e.getMessage());
            throw new OpenTofuMakerRequestFailedException(e.getMessage());
        }
    }

    private OpenTofuAsyncModifyFromScriptsRequest getModifyFromScriptsRequest(DeployTask task,
                                                                                String stateFile)
            throws OpenTofuMakerRequestFailedException {
        OpenTofuAsyncModifyFromScriptsRequest request =
                new OpenTofuAsyncModifyFromScriptsRequest();
        request.setScripts(tofuMakerHelper.getFiles(task));
        request.setTfState(stateFile);
        request.setVariables(tofuMakerHelper.getInputVariables(task, false));
        request.setEnvVariables(tofuMakerHelper.getEnvironmentVariables(task));
        request.setWebhookConfig(tofuMakerHelper.getModifyWebhookConfig(task));
        return request;
    }

    private OpenTofuAsyncModifyFromGitRepoRequest getModifyFromGitRepoRequest(DeployTask task,
                                                                                String stateFile)
            throws OpenTofuMakerRequestFailedException {
        OpenTofuAsyncModifyFromGitRepoRequest request =
                new OpenTofuAsyncModifyFromGitRepoRequest();
        request.setTfState(stateFile);
        request.setVariables(tofuMakerHelper.getInputVariables(task, false));
        request.setEnvVariables(tofuMakerHelper.getEnvironmentVariables(task));
        request.setWebhookConfig(tofuMakerHelper.getModifyWebhookConfig(task));
        request.setGitRepoDetails(
                tofuMakerHelper.convertOpenTofuScriptGitRepoDetailsFromDeployFromGitRepo(
                        task.getOcl().getDeployment().getScriptsRepo()));
        return request;
    }
}
