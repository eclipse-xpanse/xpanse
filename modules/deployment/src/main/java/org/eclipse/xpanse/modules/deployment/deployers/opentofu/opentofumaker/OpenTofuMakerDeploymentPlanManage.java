/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofumaker;

import org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofumaker.generated.api.OpenTofuFromScriptsApi;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofumaker.generated.model.OpenTofuPlan;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofumaker.generated.model.OpenTofuPlanWithScriptsRequest;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Bean to get openTofu plan using tofu-maker.
 */
@Component
@Profile("tofu-maker")
public class OpenTofuMakerDeploymentPlanManage {

    private final OpenTofuFromScriptsApi openTofuFromScriptsApi;
    private final OpenTofuMakerHelper openTofuMakerHelper;

    /**
     * constructor for OpenTofuBootDeploymentPlanManage.
     */
    public OpenTofuMakerDeploymentPlanManage(OpenTofuFromScriptsApi openTofuFromScriptsApi,
                                             OpenTofuMakerHelper openTofuMakerHelper) {
        this.openTofuFromScriptsApi = openTofuFromScriptsApi;
        this.openTofuMakerHelper = openTofuMakerHelper;
    }

    /**
     * Method to get openTofu plan from scripts provided in OCL.
     */
    public OpenTofuPlan getOpenTofuPlanFromScripts(DeployTask deployTask) {
        openTofuMakerHelper.setHeaderTokenByProfiles();
        return openTofuFromScriptsApi.planWithScripts(getPlanWithScriptsRequest(deployTask),
                deployTask.getId());
    }

    private OpenTofuPlanWithScriptsRequest getPlanWithScriptsRequest(DeployTask task) {
        OpenTofuPlanWithScriptsRequest request = new OpenTofuPlanWithScriptsRequest();
        request.setScripts(openTofuMakerHelper.getFiles(task));
        request.setVariables(openTofuMakerHelper.getInputVariables(task, true));
        request.setEnvVariables(openTofuMakerHelper.getEnvironmentVariables(task));
        return request;
    }
}
