/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.exceptions.TerraformBootRequestFailedException;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.config.TerraformBootConfig;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformScriptGitRepoDetails;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.WebhookConfig;
import org.eclipse.xpanse.modules.deployment.utils.DeployEnvironments;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.servicetemplate.ScriptsRepo;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeploymentScenario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Bean for all helpers methods to interact with terraform-boot.
 */
@Component
@Slf4j
@Profile("terraform-boot")
public class TerraformBootHelper {

    private static final String SPLIT = "/";
    private final TerraformBootConfig terraformBootConfig;
    private final DeployEnvironments deployEnvironments;

    @Value("${server.port}")
    private String port;

    /**
     * Constructor for TerraformBootHelper.
     */
    public TerraformBootHelper(TerraformBootConfig terraformBootConfig,
                               DeployEnvironments deployEnvironments) {
        this.terraformBootConfig = terraformBootConfig;
        this.deployEnvironments = deployEnvironments;
    }

    /**
     * Converts OCL DeployFromGitRepo type to terraform-boot TerraformScriptGitRepoDetails type.
     */
    public TerraformScriptGitRepoDetails convertTerraformScriptGitRepoDetailsFromDeployFromGitRepo(
            ScriptsRepo scriptsRepo) {
        TerraformScriptGitRepoDetails terraformScriptGitRepoDetails =
                new TerraformScriptGitRepoDetails();
        terraformScriptGitRepoDetails.setRepoUrl(scriptsRepo.getRepoUrl());
        terraformScriptGitRepoDetails.setBranch(scriptsRepo.getBranch());
        terraformScriptGitRepoDetails.setScriptPath(scriptsRepo.getScriptsPath());
        return terraformScriptGitRepoDetails;
    }

    /**
     * Returns all terraform script files.
     */
    public List<String> getFiles(DeployTask task) {
        String deployer = task.getOcl().getDeployment().getDeployer();
        return Collections.singletonList(deployer);
    }

    /**
     * Builds a map of all variables that must be passed to terraform executor.
     */
    public Map<String, Object> getInputVariables(DeployTask deployTask, boolean isDeployRequest) {
        return this.deployEnvironments.getInputVariables(deployTask, isDeployRequest);
    }

    /**
     * Builds a map of all variables that must be set as environment variables to the
     * terraform executor.
     */
    public Map<String, String> getEnvironmentVariables(DeployTask deployTask) {
        return this.deployEnvironments.getEnvironmentVariables(deployTask);
    }

    /**
     * generates webhook config.
     */
    public WebhookConfig getWebhookConfigWithTask(DeployTask deployTask) {
        WebhookConfig webhookConfig = new WebhookConfig();
        String callbackUrl = getClientRequestBaseUrl(port)
                + getDeployerTaskCallbackUrl(deployTask.getDeploymentScenario());
        webhookConfig.setUrl(callbackUrl + SPLIT + deployTask.getServiceId());
        webhookConfig.setAuthType(WebhookConfig.AuthTypeEnum.NONE);
        return webhookConfig;
    }

    private String getClientRequestBaseUrl(String port) {
        try {
            String clientBaseUri = terraformBootConfig.getClientBaseUri();
            if (StringUtils.isBlank(clientBaseUri)) {
                return String.format("http://%s:%s", InetAddress.getLocalHost().getHostAddress(),
                        port);
            } else {
                return clientBaseUri;
            }
        } catch (UnknownHostException e) {
            log.error(ResultType.TERRAFORM_BOOT_REQUEST_FAILED.toValue());
            throw new TerraformBootRequestFailedException(e.getMessage());
        }
    }

    private String getDeployerTaskCallbackUrl(DeploymentScenario deploymentScenario) {
        return switch (deploymentScenario) {
            case DEPLOY -> terraformBootConfig.getDeployCallbackUri();
            case MODIFY -> terraformBootConfig.getModifyCallbackUri();
            case DESTROY -> terraformBootConfig.getDestroyCallbackUri();
            case ROLLBACK -> terraformBootConfig.getRollbackCallbackUri();
            case PURGE -> terraformBootConfig.getPurgeCallbackUri();

        };
    }
}
