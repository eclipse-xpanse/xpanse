/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.exceptions.TerraBootRequestFailedException;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.config.TerraBootConfig;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.model.TerraformScriptsGitRepoDetails;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.model.WebhookConfig;
import org.eclipse.xpanse.modules.deployment.utils.DeployEnvironments;
import org.eclipse.xpanse.modules.models.response.ErrorType;
import org.eclipse.xpanse.modules.models.servicetemplate.ScriptsRepo;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Bean for all helpers methods to interact with terra-boot. */
@Component
@Slf4j
@Profile("terra-boot")
public class TerraBootHelper {

    private static final String SPLIT = "/";
    private final TerraBootConfig terraBootConfig;
    private final DeployEnvironments deployEnvironments;

    @Value("${server.port}")
    private String port;

    /** Constructor for TerraBootHelper. */
    public TerraBootHelper(TerraBootConfig terraBootConfig, DeployEnvironments deployEnvironments) {
        this.terraBootConfig = terraBootConfig;
        this.deployEnvironments = deployEnvironments;
    }

    /** Converts OCL DeployFromGitRepo type to terra-boot TerraformScriptsGitRepoDetails type. */
    public TerraformScriptsGitRepoDetails
            convertTerraformScriptsGitRepoDetailsFromDeployFromGitRepo(ScriptsRepo scriptsRepo) {
        TerraformScriptsGitRepoDetails terraformScriptGitRepoDetails =
                new TerraformScriptsGitRepoDetails();
        terraformScriptGitRepoDetails.setRepoUrl(scriptsRepo.getRepoUrl());
        terraformScriptGitRepoDetails.setBranch(scriptsRepo.getBranch());
        terraformScriptGitRepoDetails.setScriptPath(scriptsRepo.getScriptsPath());
        return terraformScriptGitRepoDetails;
    }

    /** Builds a map of all variables that must be passed to terraform executor. */
    public Map<String, Object> getInputVariables(DeployTask deployTask, boolean isDeployRequest) {
        return this.deployEnvironments.getInputVariables(deployTask, isDeployRequest);
    }

    /**
     * Builds a map of all variables that must be set as environment variables to the terraform
     * executor.
     */
    public Map<String, String> getEnvironmentVariables(DeployTask deployTask) {
        return this.deployEnvironments.getEnvironmentVariables(deployTask);
    }

    /** generates webhook config. */
    public WebhookConfig getWebhookConfigWithTask(DeployTask deployTask) {
        WebhookConfig webhookConfig = new WebhookConfig();
        String callbackUrl = getClientRequestBaseUrl(port) + terraBootConfig.getOrderCallbackUri();
        webhookConfig.setUrl(callbackUrl + SPLIT + deployTask.getOrderId());
        webhookConfig.setAuthType(WebhookConfig.AuthTypeEnum.HMAC);
        return webhookConfig;
    }

    private String getClientRequestBaseUrl(String port) {
        try {
            String clientBaseUri = terraBootConfig.getClientBaseUri();
            if (StringUtils.isBlank(clientBaseUri)) {
                return String.format(
                        "http://%s:%s", InetAddress.getLocalHost().getHostAddress(), port);
            } else {
                return clientBaseUri;
            }
        } catch (UnknownHostException e) {
            log.error(ErrorType.TERRA_BOOT_REQUEST_FAILED.toValue());
            throw new TerraBootRequestFailedException(e.getMessage());
        }
    }
}
