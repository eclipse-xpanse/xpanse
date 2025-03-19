/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot;

import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.api.TerraformFromGitRepoApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.api.TerraformFromScriptsApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.model.TerraformPlan;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.model.TerraformRequestWithScripts;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.model.TerraformRequestWithScriptsGitRepo;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Bean to get terraform plan using terra-boot. */
@Component
@Profile("terra-boot")
public class TerraBootDeploymentPlanManage {

    private final TerraformFromScriptsApi terraformFromScriptsApi;
    private final TerraformFromGitRepoApi terraformFromGitRepoApi;
    private final TerraBootHelper terraBootHelper;

    /** constructor for TerraBootDeploymentPlanManage. */
    public TerraBootDeploymentPlanManage(
            TerraformFromScriptsApi terraformFromScriptsApi,
            TerraformFromGitRepoApi terraformFromGitRepoApi,
            TerraBootHelper terraBootHelper) {
        this.terraformFromScriptsApi = terraformFromScriptsApi;
        this.terraformFromGitRepoApi = terraformFromGitRepoApi;
        this.terraBootHelper = terraBootHelper;
    }

    /** Method to get terraform plan from scripts provided in OCL. */
    public TerraformPlan getTerraformPlanFromScripts(DeployTask deployTask) {
        return terraformFromScriptsApi.planWithScripts(getPlanWithScripts(deployTask));
    }

    /** Method to get terraform plan from scripts provided in GIT repo. */
    public TerraformPlan getTerraformPlanFromGitRepo(DeployTask deployTask) {
        return terraformFromGitRepoApi.planFromGitRepo(getPlanFromGitRepoRequest(deployTask));
    }

    private TerraformRequestWithScripts getPlanWithScripts(DeployTask task) {
        TerraformRequestWithScripts request = new TerraformRequestWithScripts();
        request.setRequestId(task.getOrderId());
        request.setRequestType(TerraformRequestWithScripts.RequestTypeEnum.PLAN);
        request.setTerraformVersion(task.getOcl().getDeployment().getDeployerTool().getVersion());
        request.setScriptFiles(task.getOcl().getDeployment().getScriptFiles());
        request.setVariables(terraBootHelper.getInputVariables(task, true));
        request.setEnvVariables(terraBootHelper.getEnvironmentVariables(task));
        return request;
    }

    private TerraformRequestWithScriptsGitRepo getPlanFromGitRepoRequest(DeployTask task) {
        TerraformRequestWithScriptsGitRepo request = new TerraformRequestWithScriptsGitRepo();
        request.setRequestId(task.getOrderId());
        request.setRequestType(TerraformRequestWithScriptsGitRepo.RequestTypeEnum.PLAN);
        request.setTerraformVersion(task.getOcl().getDeployment().getDeployerTool().getVersion());
        request.setVariables(terraBootHelper.getInputVariables(task, true));
        request.setEnvVariables(terraBootHelper.getEnvironmentVariables(task));
        request.setGitRepoDetails(
                terraBootHelper.convertTerraformScriptsGitRepoDetailsFromDeployFromGitRepo(
                        task.getOcl().getDeployment().getScriptsRepo()));
        return request;
    }
}
