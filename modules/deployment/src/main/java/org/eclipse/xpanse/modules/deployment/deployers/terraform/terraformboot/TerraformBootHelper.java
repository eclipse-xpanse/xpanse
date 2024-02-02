/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.exceptions.TerraformBootRequestFailedException;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.config.TerraformBootConfig;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.api.AdminApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.api.TerraformFromGitRepoApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.api.TerraformFromScriptsApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformScriptGitRepoDetails;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.WebhookConfig;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.utils.TerraformProviderHelper;
import org.eclipse.xpanse.modules.deployment.utils.DeployEnvironments;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.servicetemplate.ScriptsRepo;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.security.common.CurrentUserInfoHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Bean for all helpers methods to interact with terraform-boot.
 */
@Component
@Slf4j
@Profile("terraform-boot")
public class TerraformBootHelper {

    private static final String ZITADEL_PROFILE_NAME = "zitadel";
    private static final String SPLIT = "/";

    @Value("${spring.profiles.active}")
    private String profiles;

    @Value("${server.port}")
    private String port;

    private final TerraformBootConfig terraformBootConfig;
    private final TerraformProviderHelper terraformProviderHelper;
    private final TerraformFromScriptsApi terraformFromScriptsApi;
    private final TerraformFromGitRepoApi terraformFromGitRepoApi;
    private final DeployEnvironments deployEnvironments;
    private final AdminApi adminApi;

    /**
     * Constructor for TerraformBootHelper.
     */
    public TerraformBootHelper(TerraformBootConfig terraformBootConfig,
                               TerraformProviderHelper terraformProviderHelper,
                               TerraformFromScriptsApi terraformFromScriptsApi,
                               TerraformFromGitRepoApi terraformFromGitRepoApi,
                               DeployEnvironments deployEnvironments, AdminApi adminApi) {
        this.terraformBootConfig = terraformBootConfig;
        this.terraformProviderHelper = terraformProviderHelper;
        this.terraformFromScriptsApi = terraformFromScriptsApi;
        this.terraformFromGitRepoApi = terraformFromGitRepoApi;
        this.deployEnvironments = deployEnvironments;
        this.adminApi = adminApi;
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
     * Sets Oauth access token to terraform-boot request.
     */
    public void setHeaderTokenByProfiles() {
        if (StringUtils.isBlank(profiles)) {
            return;
        }
        List<String> profileList = Arrays.asList(profiles.split(","));
        if (!CollectionUtils.isEmpty(profileList) && profileList.contains(ZITADEL_PROFILE_NAME)) {
            terraformFromScriptsApi.getApiClient().setAccessToken(CurrentUserInfoHolder.getToken());
            terraformFromGitRepoApi.getApiClient().setAccessToken(CurrentUserInfoHolder.getToken());
            adminApi.getApiClient().setAccessToken(CurrentUserInfoHolder.getToken());
        }
    }

    /**
     * Returns all terraform script files.
     */
    public List<String> getFiles(DeployTask task) {
        Csp csp = task.getDeployRequest().getCsp();
        String region = task.getDeployRequest().getRegion();
        String provider = terraformProviderHelper.getProvider(csp, region);
        String deployer = task.getOcl().getDeployment().getDeployer();
        return Arrays.asList(provider, deployer);
    }

    /**
     * Builds a map of all variables that must be passed to terraform executor.
     */
    public Map<String, Object> getInputVariables(DeployTask deployTask, boolean isDeployRequest) {
        Map<String, Object> inputVariables = new HashMap<>();
        inputVariables.putAll(this.deployEnvironments.getVariablesFromDeployTask(
                deployTask, isDeployRequest));
        inputVariables.putAll(this.deployEnvironments.getFlavorVariables(deployTask));
        // we additionally pass the region as var since the provider information is
        // also taken from GIT repo.
        inputVariables.put("region", deployTask.getDeployRequest().getRegion());
        return inputVariables;
    }

    /**
     * Builds a map of all variables that must be set as environment variables to the
     * terraform executor.
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
                + (isDestroyTask ? terraformBootConfig.getDestroyCallbackUri()
                : terraformBootConfig.getDeployCallbackUri());
        webhookConfig.setUrl(callbackUrl + SPLIT + deployTask.getId());
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
}
