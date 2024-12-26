/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.exceptions.OpenTofuMakerRequestFailedException;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.api.OpenTofuFromGitRepoApi;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.api.OpenTofuFromScriptsApi;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuAsyncDeployFromGitRepoRequest;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuAsyncDeployFromScriptsRequest;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/** Bean to manage service deployment via tofu-maker. */
@Slf4j
@Component
@Profile("tofu-maker")
public class TofuMakerServiceDeployer {

    private final OpenTofuFromScriptsApi openTofuFromScriptsApi;
    private final OpenTofuFromGitRepoApi openTofuFromGitRepoApi;
    private final TofuMakerHelper tofuMakerHelper;

    /** Constructor for OpenTofuMakerServiceDeployer bean. */
    public TofuMakerServiceDeployer(
            OpenTofuFromScriptsApi openTofuFromScriptsApi,
            OpenTofuFromGitRepoApi openTofuFromGitRepoApi,
            TofuMakerHelper tofuMakerHelper) {
        this.openTofuFromScriptsApi = openTofuFromScriptsApi;
        this.openTofuFromGitRepoApi = openTofuFromGitRepoApi;
        this.tofuMakerHelper = tofuMakerHelper;
    }

    /** method to perform service deployment using scripts provided in OCL. */
    public DeployResult deployFromScripts(DeployTask deployTask) {
        DeployResult result = new DeployResult();
        OpenTofuAsyncDeployFromScriptsRequest request = getDeployFromScriptsRequest(deployTask);
        try {
            openTofuFromScriptsApi.asyncDeployWithScripts(request);
            result.setOrderId(deployTask.getOrderId());
            return result;
        } catch (RestClientException e) {
            log.error(
                    "tofu-maker deploy service failed. service id: {} , error:{} ",
                    deployTask.getServiceId(),
                    e.getMessage());
            throw new OpenTofuMakerRequestFailedException(e.getMessage());
        }
    }

    /** method to perform service deployment using scripts form GIT repo. */
    public DeployResult deployFromGitRepo(DeployTask deployTask) {
        DeployResult result = new DeployResult();
        OpenTofuAsyncDeployFromGitRepoRequest request = getDeployFromGitRepoRequest(deployTask);
        try {
            openTofuFromGitRepoApi.asyncDeployFromGitRepo(request);
            result.setOrderId(deployTask.getOrderId());
            return result;
        } catch (RestClientException e) {
            log.error(
                    "tofu-maker deploy service failed. service id: {} , error:{} ",
                    deployTask.getServiceId(),
                    e.getMessage());
            throw new OpenTofuMakerRequestFailedException(e.getMessage());
        }
    }

    private OpenTofuAsyncDeployFromScriptsRequest getDeployFromScriptsRequest(DeployTask task) {
        OpenTofuAsyncDeployFromScriptsRequest request = new OpenTofuAsyncDeployFromScriptsRequest();
        request.setRequestId(task.getOrderId());
        request.setOpenTofuVersion(task.getOcl().getDeployment().getDeployerTool().getVersion());
        request.setIsPlanOnly(false);
        request.setScriptFiles(task.getOcl().getDeployment().getScriptFiles());
        request.setVariables(tofuMakerHelper.getInputVariables(task, true));
        request.setEnvVariables(tofuMakerHelper.getEnvironmentVariables(task));
        request.setWebhookConfig(tofuMakerHelper.getWebhookConfigWithTask(task));
        return request;
    }

    private OpenTofuAsyncDeployFromGitRepoRequest getDeployFromGitRepoRequest(DeployTask task) {
        OpenTofuAsyncDeployFromGitRepoRequest request = new OpenTofuAsyncDeployFromGitRepoRequest();
        request.setRequestId(task.getOrderId());
        request.setOpenTofuVersion(task.getOcl().getDeployment().getDeployerTool().getVersion());
        request.setIsPlanOnly(false);
        request.setVariables(tofuMakerHelper.getInputVariables(task, true));
        request.setEnvVariables(tofuMakerHelper.getEnvironmentVariables(task));
        request.setWebhookConfig(tofuMakerHelper.getWebhookConfigWithTask(task));
        request.setGitRepoDetails(
                tofuMakerHelper.convertOpenTofuScriptGitRepoDetailsFromDeployFromGitRepo(
                        task.getOcl().getDeployment().getScriptsRepo()));
        return request;
    }
}
