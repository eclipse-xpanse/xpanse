/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofumaker;

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
    public OpenTofuMakerDeployment(
            OpenTofuMakerScriptValidator openTofuMakerScriptValidator,
            OpenTofuMakerDeploymentPlanManage openTofuMakerDeploymentPlanManage,
            OpenTofuMakerServiceDeployer openTofuMakerServiceDeployer,
            OpenTofuMakerServiceDestroyer openTofuMakerServiceDestroyer) {
        this.openTofuMakerServiceDestroyer = openTofuMakerServiceDestroyer;
        this.openTofuMakerScriptValidator = openTofuMakerScriptValidator;
        this.openTofuMakerDeploymentPlanManage = openTofuMakerDeploymentPlanManage;
        this.openTofuMakerServiceDeployer = openTofuMakerServiceDeployer;
    }

    @Override
    public DeployResult deploy(DeployTask deployTask) {
        return openTofuMakerServiceDeployer.deployFromScripts(deployTask);
    }

    @Override
    public DeployResult destroy(DeployTask deployTask) {
        return openTofuMakerServiceDestroyer.destroyFromScripts(deployTask);
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
        return openTofuMakerScriptValidator.validateOpenTofuScripts(ocl);
    }

    @Override
    public String getDeploymentPlanAsJson(DeployTask task) {
        return openTofuMakerDeploymentPlanManage.getOpenTofuPlanFromScripts(task).getPlan();
    }

}
