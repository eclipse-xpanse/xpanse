/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofumaker;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.exceptions.OpenTofuMakerRequestFailedException;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofumaker.config.OpenTofuMakerConfig;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofumaker.generated.api.AdminApi;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofumaker.generated.api.OpenTofuFromScriptsApi;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofumaker.generated.model.WebhookConfig;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.utils.OpenTofuProviderHelper;
import org.eclipse.xpanse.modules.deployment.utils.DeployEnvironments;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.security.common.CurrentUserInfoHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Bean for all helpers methods to interact with tofu-maker.
 */
@Component
@Slf4j
@Profile("tofu-maker")
public class OpenTofuMakerHelper {

    private static final String ZITADEL_PROFILE_NAME = "zitadel";
    private static final String SPLIT = "/";
    private final OpenTofuMakerConfig openTofuMakerConfig;
    private final OpenTofuFromScriptsApi openTofuFromScriptsApi;
    private final DeployEnvironments deployEnvironments;
    private final OpenTofuProviderHelper openTofuProviderHelper;
    private final AdminApi adminApi;
    @Value("${spring.profiles.active}")
    private String profiles;
    @Value("${server.port}")
    private String port;

    /**
     * Constructor for OpenTofuMakerHelper.
     */
    public OpenTofuMakerHelper(OpenTofuMakerConfig openTofuMakerConfig,
                               OpenTofuFromScriptsApi openTofuFromScriptsApi,
                               DeployEnvironments deployEnvironments,
                               OpenTofuProviderHelper openTofuProviderHelper, AdminApi adminApi) {
        this.openTofuMakerConfig = openTofuMakerConfig;
        this.openTofuFromScriptsApi = openTofuFromScriptsApi;
        this.deployEnvironments = deployEnvironments;
        this.openTofuProviderHelper = openTofuProviderHelper;
        this.adminApi = adminApi;
    }

    /**
     * Sets Oauth access token to tofu-maker request.
     */
    public void setHeaderTokenByProfiles() {
        if (StringUtils.isBlank(profiles)) {
            return;
        }
        List<String> profileList = Arrays.asList(profiles.split(","));
        if (!CollectionUtils.isEmpty(profileList) && profileList.contains(ZITADEL_PROFILE_NAME)) {
            openTofuFromScriptsApi.getApiClient().setAccessToken(CurrentUserInfoHolder.getToken());
            adminApi.getApiClient().setAccessToken(CurrentUserInfoHolder.getToken());
        }
    }

    /**
     * Returns all openTofu script files.
     */
    public List<String> getFiles(DeployTask task) {
        Csp csp = task.getDeployRequest().getCsp();
        String region = task.getDeployRequest().getRegion();
        String provider = openTofuProviderHelper.getProvider(csp, region);
        String deployer = task.getOcl().getDeployment().getDeployer();
        return Arrays.asList(provider, deployer);
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
        inputVariables.put("region", deployTask.getDeployRequest().getRegion());
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
                + (isDestroyTask ? openTofuMakerConfig.getDestroyCallbackUri()
                : openTofuMakerConfig.getDeployCallbackUri());
        webhookConfig.setUrl(callbackUrl + SPLIT + deployTask.getId());
        webhookConfig.setAuthType(WebhookConfig.AuthTypeEnum.NONE);
        return webhookConfig;
    }

    private String getClientRequestBaseUrl(String port) {
        try {
            String clientBaseUri = openTofuMakerConfig.getClientBaseUri();
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
