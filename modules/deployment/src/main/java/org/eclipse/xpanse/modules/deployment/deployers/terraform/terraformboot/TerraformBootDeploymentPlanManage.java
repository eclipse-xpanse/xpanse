/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot;

import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.api.TerraformFromGitRepoApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.api.TerraformFromScriptsApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformPlan;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformPlanFromGitRepoRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformPlanWithScriptsRequest;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Bean to get terraform plan using terraform-boot. */
@Component
@Profile("terraform-boot")
public class TerraformBootDeploymentPlanManage {

    private final TerraformFromScriptsApi terraformFromScriptsApi;
    private final TerraformFromGitRepoApi terraformFromGitRepoApi;
    private final TerraformBootHelper terraformBootHelper;

    /** constructor for TerraformBootDeploymentPlanManage. */
    public TerraformBootDeploymentPlanManage(
            TerraformFromScriptsApi terraformFromScriptsApi,
            TerraformFromGitRepoApi terraformFromGitRepoApi,
            TerraformBootHelper terraformBootHelper) {
        this.terraformFromScriptsApi = terraformFromScriptsApi;
        this.terraformFromGitRepoApi = terraformFromGitRepoApi;
        this.terraformBootHelper = terraformBootHelper;
    }

    /** Method to get terraform plan from scripts provided in OCL. */
    public TerraformPlan getTerraformPlanFromScripts(DeployTask deployTask) {
        return terraformFromScriptsApi.planWithScripts(getPlanWithScriptsRequest(deployTask));
    }

    /** Method to get terraform plan from scripts provided in GIT repo. */
    public TerraformPlan getTerraformPlanFromGitRepo(DeployTask deployTask) {
        return terraformFromGitRepoApi.planFromGitRepo(getPlanFromGitRepoRequest(deployTask));
    }

    private TerraformPlanWithScriptsRequest getPlanWithScriptsRequest(DeployTask task) {
        TerraformPlanWithScriptsRequest request = new TerraformPlanWithScriptsRequest();
        request.setRequestId(task.getOrderId());
        request.setTerraformVersion(task.getOcl().getDeployment().getDeployerTool().getVersion());
        request.setScripts(terraformBootHelper.getFiles(task));
        request.setVariables(terraformBootHelper.getInputVariables(task, true));
        request.setEnvVariables(terraformBootHelper.getEnvironmentVariables(task));
        return request;
    }

    private TerraformPlanFromGitRepoRequest getPlanFromGitRepoRequest(DeployTask task) {
        TerraformPlanFromGitRepoRequest request = new TerraformPlanFromGitRepoRequest();
        request.setRequestId(task.getOrderId());
        request.setTerraformVersion(task.getOcl().getDeployment().getDeployerTool().getVersion());
        request.setVariables(terraformBootHelper.getInputVariables(task, true));
        request.setEnvVariables(terraformBootHelper.getEnvironmentVariables(task));
        request.setGitRepoDetails(
                terraformBootHelper.convertTerraformScriptGitRepoDetailsFromDeployFromGitRepo(
                        task.getOcl().getDeployment().getScriptsRepo()));
        return request;
    }
}
