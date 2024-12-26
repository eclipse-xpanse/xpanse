/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker;

import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.servicetemplate.Deployment;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.deployment.Deployer;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeploymentScriptValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Implementation of the deployment with tofu-maker. */
@Slf4j
@Profile("tofu-maker")
@Component
public class TofuMakerDeployment implements Deployer {

    private final TofuMakerScriptValidator tofuMakerScriptValidator;
    private final TofuMakerDeploymentPlanManage tofuMakerDeploymentPlanManage;
    private final TofuMakerServiceDeployer tofuMakerServiceDeployer;
    private final TofuMakerServiceDestroyer tofuMakerServiceDestroyer;
    private final TofuMakerServiceModifier tofuMakerServiceModifier;

    /** Initializes the OpenTofuBoot deployer. */
    @Autowired
    public TofuMakerDeployment(
            TofuMakerScriptValidator tofuMakerScriptValidator,
            TofuMakerDeploymentPlanManage tofuMakerDeploymentPlanManage,
            TofuMakerServiceDeployer tofuMakerServiceDeployer,
            TofuMakerServiceDestroyer tofuMakerServiceDestroyer,
            TofuMakerServiceModifier tofuMakerServiceModifier) {
        this.tofuMakerServiceDestroyer = tofuMakerServiceDestroyer;
        this.tofuMakerScriptValidator = tofuMakerScriptValidator;
        this.tofuMakerDeploymentPlanManage = tofuMakerDeploymentPlanManage;
        this.tofuMakerServiceDeployer = tofuMakerServiceDeployer;
        this.tofuMakerServiceModifier = tofuMakerServiceModifier;
    }

    @Override
    public DeployResult deploy(DeployTask deployTask) {
        Map<String, String> scriptsMap = deployTask.getOcl().getDeployment().getScriptFiles();
        if (Objects.nonNull(scriptsMap) && !scriptsMap.isEmpty()) {
            return tofuMakerServiceDeployer.deployFromScripts(deployTask);
        }
        return tofuMakerServiceDeployer.deployFromGitRepo(deployTask);
    }

    @Override
    public DeployResult destroy(DeployTask deployTask) {
        Map<String, String> scriptsMap = deployTask.getOcl().getDeployment().getScriptFiles();
        if (Objects.nonNull(scriptsMap) && !scriptsMap.isEmpty()) {
            return tofuMakerServiceDestroyer.destroyFromScripts(deployTask);
        }
        return tofuMakerServiceDestroyer.destroyFromGitRepo(deployTask);
    }

    @Override
    public DeployResult modify(DeployTask deployTask) {
        Map<String, String> scriptsMap = deployTask.getOcl().getDeployment().getScriptFiles();
        if (Objects.nonNull(scriptsMap) && !scriptsMap.isEmpty()) {
            return tofuMakerServiceModifier.modifyFromScripts(deployTask);
        }
        return tofuMakerServiceModifier.modifyFromGitRepo(deployTask);
    }

    /** Get the deployer kind. */
    @Override
    public DeployerKind getDeployerKind() {
        return DeployerKind.OPEN_TOFU;
    }

    /** Validates the OpenTofu script. */
    @Override
    public DeploymentScriptValidationResult validate(Deployment deployment) {
        DeploymentScriptValidationResult result = null;
        Map<String, String> scriptsMap = deployment.getScriptFiles();
        if (Objects.nonNull(scriptsMap) && !scriptsMap.isEmpty()) {
            result = tofuMakerScriptValidator.validateOpenTofuScripts(deployment);
        }
        if (Objects.nonNull(deployment.getScriptsRepo())) {
            result = tofuMakerScriptValidator.validateOpenTofuScriptsFromGitRepo(deployment);
        }
        return result;
    }

    @Override
    public String getDeploymentPlanAsJson(DeployTask task) {
        Map<String, String> scriptsMap = task.getOcl().getDeployment().getScriptFiles();
        if (Objects.nonNull(scriptsMap) && !scriptsMap.isEmpty()) {
            return tofuMakerDeploymentPlanManage.getOpenTofuPlanFromScripts(task).getPlan();
        }
        return tofuMakerDeploymentPlanManage.getOpenTofuPlanFromGitRepo(task).getPlan();
    }
}
