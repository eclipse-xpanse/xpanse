/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker;

import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.api.OpenTofuFromGitRepoApi;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.api.OpenTofuFromScriptsApi;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuPlan;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuPlanFromGitRepoRequest;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuPlanWithScriptsRequest;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Bean to get openTofu plan using tofu-maker.
 */
@Component
@Profile("tofu-maker")
public class TofuMakerDeploymentPlanManage {

    private final OpenTofuFromScriptsApi openTofuFromScriptsApi;
    private final OpenTofuFromGitRepoApi openTofuFromGitRepoApi;
    private final TofuMakerHelper tofuMakerHelper;

    /**
     * constructor for OpenTofuMakerDeploymentPlanManage.
     */
    public TofuMakerDeploymentPlanManage(OpenTofuFromScriptsApi openTofuFromScriptsApi,
                                         OpenTofuFromGitRepoApi openTofuFromGitRepoApi,
                                         TofuMakerHelper tofuMakerHelper) {
        this.openTofuFromScriptsApi = openTofuFromScriptsApi;
        this.openTofuFromGitRepoApi = openTofuFromGitRepoApi;
        this.tofuMakerHelper = tofuMakerHelper;
    }

    /**
     * Method to get openTofu plan from scripts provided in OCL.
     */
    public OpenTofuPlan getOpenTofuPlanFromScripts(DeployTask deployTask) {
        return openTofuFromScriptsApi.planWithScripts(getPlanWithScriptsRequest(deployTask),
                deployTask.getId());
    }

    /**
     * Method to get openTofu plan from scripts provided in GIT repo.
     */
    public OpenTofuPlan getOpenTofuPlanFromGitRepo(DeployTask deployTask) {
        return openTofuFromGitRepoApi.planFromGitRepo(getPlanFromGitRepoRequest(deployTask),
                deployTask.getId());
    }

    private OpenTofuPlanWithScriptsRequest getPlanWithScriptsRequest(DeployTask task) {
        OpenTofuPlanWithScriptsRequest request = new OpenTofuPlanWithScriptsRequest();
        request.setScripts(tofuMakerHelper.getFiles(task));
        request.setVariables(tofuMakerHelper.getInputVariables(task, true));
        request.setEnvVariables(tofuMakerHelper.getEnvironmentVariables(task));
        return request;
    }

    private OpenTofuPlanFromGitRepoRequest getPlanFromGitRepoRequest(DeployTask task) {
        OpenTofuPlanFromGitRepoRequest request = new OpenTofuPlanFromGitRepoRequest();
        request.setVariables(tofuMakerHelper.getInputVariables(task, true));
        request.setEnvVariables(tofuMakerHelper.getEnvironmentVariables(task));
        request.setGitRepoDetails(
                tofuMakerHelper.convertOpenTofuScriptGitRepoDetailsFromDeployFromGitRepo(
                        task.getOcl().getDeployment().getScriptsRepo()));
        request.getVariables().put("region", task.getDeployRequest().getRegion().getName());
        return request;
    }
}
