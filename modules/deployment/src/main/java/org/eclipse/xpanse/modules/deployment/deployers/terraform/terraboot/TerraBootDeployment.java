/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot;

import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.service.deployment.DeployResult;
import org.eclipse.xpanse.modules.models.servicetemplate.Deployment;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.deployment.Deployer;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeploymentScriptValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Implementation of the deployment with terra-boot. */
@Slf4j
@Profile("terra-boot")
@Component
public class TerraBootDeployment implements Deployer {

    private final TerraBootScriptValidator terraBootScriptValidator;
    private final TerraBootDeploymentPlanManage terraBootDeploymentPlanManage;
    private final TerraBootServiceDeployer terraBootServiceDeployer;
    private final TerraBootServiceDestroyer terraBootServiceDestroyer;
    private final TerraBootServiceModifier terraBootServiceModifier;

    /** Initializes the TerraBoot deployer. */
    @Autowired
    public TerraBootDeployment(
            TerraBootScriptValidator terraBootScriptValidator,
            TerraBootDeploymentPlanManage terraBootDeploymentPlanManage,
            TerraBootServiceDeployer terraBootServiceDeployer,
            TerraBootServiceDestroyer terraBootServiceDestroyer,
            TerraBootServiceModifier terraBootServiceModifier) {
        this.terraBootServiceDestroyer = terraBootServiceDestroyer;
        this.terraBootScriptValidator = terraBootScriptValidator;
        this.terraBootDeploymentPlanManage = terraBootDeploymentPlanManage;
        this.terraBootServiceDeployer = terraBootServiceDeployer;
        this.terraBootServiceModifier = terraBootServiceModifier;
    }

    @Override
    public DeployResult deploy(DeployTask deployTask) {
        Map<String, String> scriptsMap = deployTask.getOcl().getDeployment().getScriptFiles();
        if (Objects.nonNull(scriptsMap) && !scriptsMap.isEmpty()) {
            return terraBootServiceDeployer.deployFromScripts(deployTask);
        }
        return terraBootServiceDeployer.deployFromGitRepo(deployTask);
    }

    @Override
    public DeployResult modify(DeployTask deployTask) {
        Map<String, String> scriptsMap = deployTask.getOcl().getDeployment().getScriptFiles();
        if (Objects.nonNull(scriptsMap) && !scriptsMap.isEmpty()) {
            return terraBootServiceModifier.modifyFromScripts(deployTask);
        }
        return terraBootServiceModifier.modifyFromGitRepo(deployTask);
    }

    @Override
    public DeployResult destroy(DeployTask deployTask) {
        Map<String, String> scriptsMap = deployTask.getOcl().getDeployment().getScriptFiles();
        if (Objects.nonNull(scriptsMap) && !scriptsMap.isEmpty()) {
            return terraBootServiceDestroyer.destroyFromScripts(deployTask);
        }
        return terraBootServiceDestroyer.destroyFromGitRepo(deployTask);
    }

    /** Get the deployer kind. */
    @Override
    public DeployerKind getDeployerKind() {
        return DeployerKind.TERRAFORM;
    }

    /** Validates the Terraform script. */
    @Override
    public DeploymentScriptValidationResult validate(Deployment deployment) {
        DeploymentScriptValidationResult result = null;
        Map<String, String> scriptsMap = deployment.getScriptFiles();
        if (Objects.nonNull(scriptsMap) && !scriptsMap.isEmpty()) {
            result = terraBootScriptValidator.validateTerraformScripts(deployment);
        }
        if (Objects.nonNull(deployment.getScriptsRepo())) {
            result = terraBootScriptValidator.validateTerraformScriptsFromGitRepo(deployment);
        }
        return result;
    }

    @Override
    public String getDeploymentPlanAsJson(DeployTask task) {
        Map<String, String> scriptsMap = task.getOcl().getDeployment().getScriptFiles();
        if (Objects.nonNull(scriptsMap) && !scriptsMap.isEmpty()) {
            return terraBootDeploymentPlanManage.getTerraformPlanFromScripts(task).getPlan();
        }
        return terraBootDeploymentPlanManage.getTerraformPlanFromGitRepo(task).getPlan();
    }
}
