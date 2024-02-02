/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot;

import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployValidationResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.Deployer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Implementation of the deployment with terraform-boot.
 */
@Slf4j
@Profile("terraform-boot")
@Component
public class TerraformBootDeployment implements Deployer {

    private final TerraformBootScriptValidator terraformBootScriptValidator;
    private final TerraformBootDeploymentPlanManage terraformBootDeploymentPlanManage;
    private final TerraformBootServiceDeployer terraformBootServiceDeployer;
    private final TerraformBootServiceDestroyer terraformBootServiceDestroyer;

    /**
     * Initializes the TerraformBoot deployer.
     */
    @Autowired
    public TerraformBootDeployment(
            TerraformBootScriptValidator terraformBootScriptValidator,
            TerraformBootDeploymentPlanManage terraformBootDeploymentPlanManage,
            TerraformBootServiceDeployer terraformBootServiceDeployer,
            TerraformBootServiceDestroyer terraformBootServiceDestroyer) {
        this.terraformBootServiceDestroyer = terraformBootServiceDestroyer;
        this.terraformBootScriptValidator = terraformBootScriptValidator;
        this.terraformBootDeploymentPlanManage = terraformBootDeploymentPlanManage;
        this.terraformBootServiceDeployer = terraformBootServiceDeployer;
    }

    @Override
    public DeployResult deploy(DeployTask deployTask) {
        if (Objects.nonNull(deployTask.getOcl().getDeployment().getDeployer())) {
            return terraformBootServiceDeployer.deployFromScripts(deployTask);
        }
        return terraformBootServiceDeployer.deployFromGitRepo(deployTask);
    }

    @Override
    public DeployResult destroy(DeployTask deployTask) {
        if (Objects.nonNull(deployTask.getOcl().getDeployment().getDeployer())) {
            return terraformBootServiceDestroyer.destroyFromScripts(deployTask);
        }
        return terraformBootServiceDestroyer.destroyFromGitRepo(deployTask);
    }

    /**
     * delete workspace,No implementation required.
     */
    @Override
    public void deleteTaskWorkspace(UUID taskId) {
        // No workspace created from xpanse.
    }

    /**
     * Get the deployer kind.
     */
    @Override
    public DeployerKind getDeployerKind() {
        return DeployerKind.TERRAFORM;
    }

    /**
     * Validates the Terraform script.
     */
    @Override
    public DeployValidationResult validate(Ocl ocl) {
        DeployValidationResult result = null;
        if (Objects.nonNull(ocl.getDeployment().getDeployer())) {
            result = terraformBootScriptValidator.validateTerraformScripts(ocl);
        }
        if (Objects.nonNull(ocl.getDeployment().getScriptsRepo())) {
            result = terraformBootScriptValidator.validateTerraformScriptsFromGitRepo(ocl);
        }
        return result;
    }

    @Override
    public String getDeploymentPlanAsJson(DeployTask task) {
        if (Objects.nonNull(task.getOcl().getDeployment().getDeployer())) {
            return terraformBootDeploymentPlanManage.getTerraformPlanFromScripts(task).getPlan();
        }
        return terraformBootDeploymentPlanManage.getTerraformPlanFromGitRepo(task).getPlan();
    }

}
