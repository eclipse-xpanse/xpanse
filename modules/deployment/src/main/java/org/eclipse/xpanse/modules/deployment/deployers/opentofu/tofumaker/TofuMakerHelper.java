/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.exceptions.OpenTofuMakerRequestFailedException;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.config.TofuMakerConfig;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuScriptGitRepoDetails;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.WebhookConfig;
import org.eclipse.xpanse.modules.deployment.utils.DeployEnvironments;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.servicetemplate.ScriptsRepo;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Bean for all helpers methods to interact with tofu-maker.
 */
@Component
@Slf4j
@Profile("tofu-maker")
public class TofuMakerHelper {

    private static final String SPLIT = "/";
    private final TofuMakerConfig tofuMakerConfig;
    private final DeployEnvironments deployEnvironments;
    @Value("${server.port}")
    private String port;

    /**
     * Constructor for OpenTofuMakerHelper.
     */
    public TofuMakerHelper(TofuMakerConfig tofuMakerConfig,
                           DeployEnvironments deployEnvironments) {
        this.tofuMakerConfig = tofuMakerConfig;
        this.deployEnvironments = deployEnvironments;
    }

    /**
     * Converts OCL DeployFromGitRepo type to tofu-maker OpenTofuScriptGitRepoDetails type.
     */
    public OpenTofuScriptGitRepoDetails convertOpenTofuScriptGitRepoDetailsFromDeployFromGitRepo(
            ScriptsRepo scriptsRepo) {
        OpenTofuScriptGitRepoDetails openTofuScriptGitRepoDetails =
                new OpenTofuScriptGitRepoDetails();
        openTofuScriptGitRepoDetails.setRepoUrl(scriptsRepo.getRepoUrl());
        openTofuScriptGitRepoDetails.setBranch(scriptsRepo.getBranch());
        openTofuScriptGitRepoDetails.setScriptPath(scriptsRepo.getScriptsPath());
        return openTofuScriptGitRepoDetails;
    }

    /**
     * Returns all openTofu script files.
     */
    public List<String> getFiles(DeployTask task) {
        String deployer = task.getOcl().getDeployment().getDeployer();
        return Collections.singletonList(deployer);
    }

    /**
     * Builds a map of all variables that must be passed to openTofu executor.
     */
    public Map<String, Object> getInputVariables(DeployTask deployTask, boolean isDeployRequest) {
        Map<String, Object> inputVariables = new HashMap<>();
        inputVariables.putAll(this.deployEnvironments.getVariablesFromDeployTask(
                deployTask, isDeployRequest));
        inputVariables.putAll(this.deployEnvironments.getFlavorVariables(deployTask));
        // we additionally pass the region as var since the provider information is
        // also taken from GIT repo.
        inputVariables.put("region", deployTask.getDeployRequest().getRegion().getName());
        return inputVariables;
    }

    /**
     * Builds a map of all variables that must be set as environment variables to the
     * openTofu executor.
     */
    public Map<String, String> getEnvironmentVariables(DeployTask deployTask) {
        Map<String, String> envVariables = new HashMap<>();
        envVariables.putAll(this.deployEnvironments.getEnvFromDeployTask(deployTask));
        envVariables.putAll(
                this.deployEnvironments.getCredentialVariablesByHostingType(
                        deployTask.getDeployRequest().getServiceHostingType(),
                        deployTask.getOcl().getDeployment().getCredentialType(),
                        deployTask.getDeployRequest().getCsp(),
                        deployTask.getDeployRequest().getUserId()));
        envVariables.putAll(this.deployEnvironments.getPluginMandatoryVariables(
                deployTask.getDeployRequest().getCsp()));
        return envVariables;
    }

    /**
     * generates webhook config.
     */
    public WebhookConfig getWebhookConfig(DeployTask deployTask, boolean isDestroyTask) {
        WebhookConfig webhookConfig = new WebhookConfig();
        String callbackUrl = getClientRequestBaseUrl(port)
                + (isDestroyTask ? tofuMakerConfig.getDestroyCallbackUri()
                : tofuMakerConfig.getDeployCallbackUri());
        webhookConfig.setUrl(callbackUrl + SPLIT + deployTask.getId());
        webhookConfig.setAuthType(WebhookConfig.AuthTypeEnum.NONE);
        return webhookConfig;
    }

    private String getClientRequestBaseUrl(String port) {
        try {
            String clientBaseUri = tofuMakerConfig.getClientBaseUri();
            if (StringUtils.isBlank(clientBaseUri)) {
                return String.format("http://%s:%s", InetAddress.getLocalHost().getHostAddress(),
                        port);
            } else {
                return clientBaseUri;
            }
        } catch (UnknownHostException e) {
            log.error(ResultType.TOFU_MAKER_REQUEST_FAILED.toValue());
            throw new OpenTofuMakerRequestFailedException(e.getMessage());
        }
    }
}
