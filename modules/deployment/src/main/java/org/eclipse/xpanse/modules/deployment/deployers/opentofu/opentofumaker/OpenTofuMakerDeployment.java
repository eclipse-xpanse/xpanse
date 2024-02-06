/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofumaker;

import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.deployment.Deployer;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeploymentScriptValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Implementation of the deployment with tofu-maker.
 */
@Slf4j
@Profile("tofu-maker")
@Component
public class OpenTofuMakerDeployment implements Deployer {

    private final OpenTofuMakerScriptValidator openTofuMakerScriptValidator;
    private final OpenTofuMakerDeploymentPlanManage openTofuMakerDeploymentPlanManage;
    private final OpenTofuMakerServiceDeployer openTofuMakerServiceDeployer;
    private final OpenTofuMakerServiceDestroyer openTofuMakerServiceDestroyer;

    /**
     * Initializes the OpenTofuBoot deployer.
     */
    @Autowired
    public OpenTofuMakerDeployment(OpenTofuMakerScriptValidator openTofuMakerScriptValidator,
                                   OpenTofuMakerDeploymentPlanManage
                                           openTofuMakerDeploymentPlanManage,
                                   OpenTofuMakerServiceDeployer openTofuMakerServiceDeployer,
                                   OpenTofuMakerServiceDestroyer openTofuMakerServiceDestroyer) {
        this.openTofuMakerServiceDestroyer = openTofuMakerServiceDestroyer;
        this.openTofuMakerScriptValidator = openTofuMakerScriptValidator;
        this.openTofuMakerDeploymentPlanManage = openTofuMakerDeploymentPlanManage;
        this.openTofuMakerServiceDeployer = openTofuMakerServiceDeployer;
    }

    @Override
    public DeployResult deploy(DeployTask deployTask) {
        if (Objects.nonNull(deployTask.getOcl().getDeployment().getDeployer())) {
            return openTofuMakerServiceDeployer.deployFromScripts(deployTask);
        }
        return openTofuMakerServiceDeployer.deployFromGitRepo(deployTask);
    }

    @Override
    public DeployResult destroy(DeployTask deployTask) {
        if (Objects.nonNull(deployTask.getOcl().getDeployment().getDeployer())) {
            return openTofuMakerServiceDestroyer.destroyFromScripts(deployTask);
        }
        return openTofuMakerServiceDestroyer.destroyFromGitRepo(deployTask);
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
        return DeployerKind.OPEN_TOFU;
    }

    /**
     * Validates the OpenTofu script.
     */
    @Override
    public DeploymentScriptValidationResult validate(Ocl ocl) {
        DeploymentScriptValidationResult result = null;
        if (Objects.nonNull(ocl.getDeployment().getDeployer())) {
            result = openTofuMakerScriptValidator.validateOpenTofuScripts(ocl);
        }
        if (Objects.nonNull(ocl.getDeployment().getScriptsRepo())) {
            result = openTofuMakerScriptValidator.validateOpenTofuScriptsFromGitRepo(ocl);
        }
        return result;
    }

    @Override
    public String getDeploymentPlanAsJson(DeployTask task) {
        if (Objects.nonNull(task.getOcl().getDeployment().getDeployer())) {
            return openTofuMakerDeploymentPlanManage.getOpenTofuPlanFromScripts(task).getPlan();
        }
        return openTofuMakerDeploymentPlanManage.getOpenTofuPlanFromGitRepo(task).getPlan();
    }

}
