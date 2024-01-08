/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.config.TerraformBootConfig;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.api.TerraformApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.TerraformAsyncDeployFromDirectoryRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.TerraformAsyncDestroyFromDirectoryRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.TerraformDeployWithScriptsRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.TerraformPlan;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.TerraformPlanWithScriptsRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.TerraformValidationResult;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.WebhookConfig;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.WebhookConfig.AuthTypeEnum;
import org.eclipse.xpanse.modules.deployment.utils.DeployEnvironments;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.security.model.CurrentUserInfoHolder;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResult;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.TerraformBootRequestFailedException;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployValidationResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.Deployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;

/**
 * Implementation of the deployment with terraform-boot.
 */
@Slf4j
@Profile("terraform-boot")
@Component
public class TerraformBootDeployment implements Deployment {

    public static final String STATE_FILE_NAME = "terraform.tfstate";
    private static final String ZITADEL_PROFILE_NAME = "zitadel";
    private final DeployEnvironments deployEnvironments;
    private final PluginManager pluginManager;
    private final TerraformBootConfig terraformBootConfig;
    private final String port;
    private final TerraformApi terraformApi;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${spring.profiles.active}")
    private String profiles;

    /**
     * Initializes the TerraformBoot deployer.
     */
    @Autowired
    public TerraformBootDeployment(
            DeployEnvironments deployEnvironments,
            PluginManager pluginManager,
            TerraformBootConfig terraformBootConfig,
            TerraformApi terraformApi,
            @Value("${server.port}") String port) {
        this.deployEnvironments = deployEnvironments;
        this.pluginManager = pluginManager;
        this.terraformBootConfig = terraformBootConfig;
        this.terraformApi = terraformApi;
        this.port = port;
    }

    @Override
    public DeployResult deploy(DeployTask deployTask) {
        DeployResult result = new DeployResult();
        TerraformAsyncDeployFromDirectoryRequest request = getDeployRequest(deployTask);
        try {
            setHeaderTokenByProfiles();
            terraformApi.asyncDeployWithScripts(request, deployTask.getId());
            result.setId(deployTask.getId());
            return result;
        } catch (RestClientException e) {
            log.error("terraform-boot deploy service failed. service id: {} , error:{} ",
                    deployTask.getId(), e.getMessage());
            throw new TerraformBootRequestFailedException(e.getMessage());
        }
    }

    @Override
    public DeployResult destroy(DeployTask task, String stateFile) {
        DeployResult result = new DeployResult();
        TerraformAsyncDestroyFromDirectoryRequest request = getDestroyRequest(task, stateFile);
        try {
            setHeaderTokenByProfiles();
            terraformApi.asyncDestroyWithScripts(request, task.getId());
            result.setId(task.getId());
            return result;
        } catch (RestClientException e) {
            log.error("terraform-boot destroy service failed. service id: {} , error:{} ",
                    task.getId(), e.getMessage());
            throw new TerraformBootRequestFailedException(e.getMessage());
        }
    }

    /**
     * delete workspace,No implementation required.
     */
    @Override
    public void deleteTaskWorkspace(String taskId) {
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
        setHeaderTokenByProfiles();
        DeployValidationResult result = null;
        try {
            TerraformValidationResult validate =
                    terraformApi.validateWithScripts(getDeployWithScriptsRequest(ocl));
            try {
                result = objectMapper.readValue(objectMapper.writeValueAsString(validate),
                        DeployValidationResult.class);
            } catch (JsonProcessingException e) {
                log.error("JsonProcessingException", e);
            }
        } catch (RestClientException restClientException) {
            log.error("Request to terraform-boot API failed", restClientException);
            throw new TerraformBootRequestFailedException(restClientException.getMessage());
        }
        return result;
    }

    @Override
    public String getDeployPlanAsJson(DeployTask task) {
        setHeaderTokenByProfiles();
        TerraformPlan terraformPlan =
                terraformApi.planWithScripts(getPlanWithScriptsRequest(task), task.getId());
        return terraformPlan.getPlan();
    }


    private TerraformDeployWithScriptsRequest getDeployWithScriptsRequest(Ocl ocl) {
        TerraformDeployWithScriptsRequest request =
                new TerraformDeployWithScriptsRequest();
        request.setIsPlanOnly(false);
        request.setScripts(getFilesByOcl(ocl));
        return request;

    }


    private TerraformPlanWithScriptsRequest getPlanWithScriptsRequest(DeployTask task) {
        TerraformPlanWithScriptsRequest request = new TerraformPlanWithScriptsRequest();
        request.setScripts(getFiles(task));
        request.setVariables(getInputVariables(task, true));
        request.setEnvVariables(getEnvironmentVariables(task));
        return request;
    }


    private TerraformAsyncDestroyFromDirectoryRequest getDestroyRequest(DeployTask task,
                                                                        String stateFile)
            throws TerraformBootRequestFailedException {
        TerraformAsyncDestroyFromDirectoryRequest request =
                new TerraformAsyncDestroyFromDirectoryRequest();
        request.setScripts(getFiles(task));
        request.setTfState(stateFile);
        request.setVariables(getInputVariables(task, false));
        request.setEnvVariables(getEnvironmentVariables(task));
        WebhookConfig hookConfig = new WebhookConfig();
        String callbackUrl = getClientRequestBaseUrl(port)
                + terraformBootConfig.getDestroyCallbackUri();
        hookConfig.setUrl(callbackUrl + task.getId());
        hookConfig.setAuthType(AuthTypeEnum.NONE);
        request.setWebhookConfig(hookConfig);
        return request;
    }

    private TerraformAsyncDeployFromDirectoryRequest getDeployRequest(DeployTask task) {
        TerraformAsyncDeployFromDirectoryRequest request =
                new TerraformAsyncDeployFromDirectoryRequest();
        request.setIsPlanOnly(false);
        request.setScripts(getFiles(task));
        request.setVariables(getInputVariables(task, true));
        request.setEnvVariables(getEnvironmentVariables(task));
        WebhookConfig hookConfig = new WebhookConfig();
        String callbackUrl = getClientRequestBaseUrl(port)
                + terraformBootConfig.getDeployCallbackUri();
        hookConfig.setUrl(callbackUrl + task.getId());
        hookConfig.setAuthType(AuthTypeEnum.NONE);
        request.setWebhookConfig(hookConfig);
        return request;
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

    private List<String> getFiles(DeployTask task) {
        Csp csp = task.getDeployRequest().getCsp();
        String region = task.getDeployRequest().getRegion();
        String provider = this.pluginManager.getTerraformProviderForRegionByCsp(csp, region);
        String deployer = task.getOcl().getDeployment().getDeployer();
        return Arrays.asList(provider, deployer);
    }

    private List<String> getFilesByOcl(Ocl ocl) {
        Csp csp = ocl.getCloudServiceProvider().getName();
        String region = ocl.getCloudServiceProvider().getRegions().get(0).getName();
        String provider = this.pluginManager.getTerraformProviderForRegionByCsp(csp, region);
        String deployer = ocl.getDeployment().getDeployer();
        return Arrays.asList(provider, deployer);
    }

    private Map<String, Object> getInputVariables(DeployTask deployTask, boolean isDeployRequest) {
        Map<String, Object> inputVariables = new HashMap<>();
        inputVariables.putAll(this.deployEnvironments.getVariablesFromDeployTask(
                deployTask, isDeployRequest));
        inputVariables.putAll(this.deployEnvironments.getFlavorVariables(deployTask));
        return inputVariables;
    }

    private Map<String, String> getEnvironmentVariables(DeployTask deployTask) {
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

    private void setHeaderTokenByProfiles() {
        if (StringUtils.isBlank(profiles)) {
            return;
        }
        List<String> profileList = Arrays.asList(profiles.split(","));
        if (!CollectionUtils.isEmpty(profileList) && profileList.contains(ZITADEL_PROFILE_NAME)) {
            terraformApi.getApiClient().setAccessToken(CurrentUserInfoHolder.getToken());
        }
    }
}
