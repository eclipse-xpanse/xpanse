/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.deployment.ServiceDeploymentEntityHandler;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.exceptions.OpenTofuMakerRequestFailedException;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.api.OpenTofuFromGitRepoApi;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.api.OpenTofuFromScriptsApi;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuAsyncRequestWithScripts;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuAsyncRequestWithScriptsGitRepo;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.utils.TfResourceTransUtils;
import org.eclipse.xpanse.modules.models.service.deployment.DeployResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/** Bean to manage service destroy via tofu-maker. */
@Slf4j
@Component
@Profile("tofu-maker")
public class TofuMakerServiceDestroyer {

    private final OpenTofuFromScriptsApi openTofuFromScriptsApi;
    private final OpenTofuFromGitRepoApi openTofuFromGitRepoApi;
    private final TofuMakerHelper tofuMakerHelper;
    private final ServiceDeploymentEntityHandler deploymentEntityHandler;

    /** Constructor for OpenTofuMakerServiceDestroyer bean. */
    public TofuMakerServiceDestroyer(
            OpenTofuFromScriptsApi openTofuFromScriptsApi,
            OpenTofuFromGitRepoApi openTofuFromGitRepoApi,
            TofuMakerHelper tofuMakerHelper,
            ServiceDeploymentEntityHandler deploymentEntityHandler) {
        this.openTofuFromScriptsApi = openTofuFromScriptsApi;
        this.openTofuFromGitRepoApi = openTofuFromGitRepoApi;
        this.tofuMakerHelper = tofuMakerHelper;
        this.deploymentEntityHandler = deploymentEntityHandler;
    }

    /** method to perform service destroy using scripts provided in OCL. */
    public DeployResult destroyFromScripts(DeployTask deployTask) {
        ServiceDeploymentEntity serviceDeploymentEntity =
                deploymentEntityHandler.getServiceDeploymentEntity(deployTask.getServiceId());
        String resourceState = TfResourceTransUtils.getStoredStateContent(serviceDeploymentEntity);
        DeployResult result = new DeployResult();
        OpenTofuAsyncRequestWithScripts request =
                getDestroyFromScriptsRequest(deployTask, resourceState);
        try {
            openTofuFromScriptsApi.asyncDestroyWithScripts(request);
            result.setOrderId(deployTask.getOrderId());
            return result;
        } catch (RestClientException e) {
            throw new OpenTofuMakerRequestFailedException(getErrorMessage(deployTask, e));
        }
    }

    /** method to perform service destroy using scripts form GIT repo. */
    public DeployResult destroyFromGitRepo(DeployTask deployTask) {
        ServiceDeploymentEntity serviceDeploymentEntity =
                deploymentEntityHandler.getServiceDeploymentEntity(deployTask.getServiceId());
        String resourceState = TfResourceTransUtils.getStoredStateContent(serviceDeploymentEntity);
        DeployResult result = new DeployResult();
        OpenTofuAsyncRequestWithScriptsGitRepo request =
                getDestroyFromGitRepoRequest(deployTask, resourceState);
        try {
            openTofuFromGitRepoApi.asyncDestroyFromGitRepo(request);
            result.setOrderId(deployTask.getOrderId());
            return result;
        } catch (RestClientException e) {
            throw new OpenTofuMakerRequestFailedException(getErrorMessage(deployTask, e));
        }
    }

    private OpenTofuAsyncRequestWithScripts getDestroyFromScriptsRequest(
            DeployTask task, String stateFile) throws OpenTofuMakerRequestFailedException {
        OpenTofuAsyncRequestWithScripts request = new OpenTofuAsyncRequestWithScripts();
        request.setRequestType(OpenTofuAsyncRequestWithScripts.RequestTypeEnum.DESTROY);
        request.setRequestId(task.getOrderId());
        request.setOpenTofuVersion(task.getOcl().getDeployment().getDeployerTool().getVersion());
        request.setScriptFiles(task.getOcl().getDeployment().getScriptFiles());
        request.setTfState(stateFile);
        request.setVariables(tofuMakerHelper.getInputVariables(task, false));
        request.setEnvVariables(tofuMakerHelper.getEnvironmentVariables(task));
        request.setWebhookConfig(tofuMakerHelper.getWebhookConfigWithTask(task));
        return request;
    }

    private OpenTofuAsyncRequestWithScriptsGitRepo getDestroyFromGitRepoRequest(
            DeployTask task, String stateFile) throws OpenTofuMakerRequestFailedException {
        OpenTofuAsyncRequestWithScriptsGitRepo request =
                new OpenTofuAsyncRequestWithScriptsGitRepo();
        request.setRequestType(OpenTofuAsyncRequestWithScriptsGitRepo.RequestTypeEnum.DESTROY);
        request.setRequestId(task.getOrderId());
        request.setOpenTofuVersion(task.getOcl().getDeployment().getDeployerTool().getVersion());
        request.setTfState(stateFile);
        request.setVariables(tofuMakerHelper.getInputVariables(task, false));
        request.setEnvVariables(tofuMakerHelper.getEnvironmentVariables(task));
        request.setWebhookConfig(tofuMakerHelper.getWebhookConfigWithTask(task));
        request.setGitRepoDetails(
                tofuMakerHelper.convertOpenTofuScriptsGitRepoDetailsFromDeployFromGitRepo(
                        task.getOcl().getDeployment().getScriptsRepo()));
        return request;
    }

    private String getErrorMessage(DeployTask deployTask, RestClientException e) {
        String errorMsg =
                String.format(
                        "Failed to destroy service %s by order %s using tofu-maker. Error: %s",
                        deployTask.getServiceId(), deployTask.getOrderId(), e.getMessage());
        log.error(errorMsg, e);
        return errorMsg;
    }
}
