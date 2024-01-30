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
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.deployment.DeployServiceEntityHandler;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.config.TerraformBootConfig;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.exceptions.TerraformBootRequestFailedException;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.exceptions.TerraformProviderNotFoundException;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.api.TerraformFromScriptsApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformAsyncDeployFromScriptsRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformAsyncDestroyFromScriptsRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformDeployWithScriptsRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformPlan;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformPlanWithScriptsRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformValidationResult;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.WebhookConfig;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.utils.TfResourceTransUtils;
import org.eclipse.xpanse.modules.deployment.utils.DeployEnvironments;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployValidationResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.Deployer;
import org.eclipse.xpanse.modules.security.common.CurrentUserInfoHolder;
import org.slf4j.MDC;
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
public class TerraformBootDeployment implements Deployer {

    private static final String ZITADEL_PROFILE_NAME = "zitadel";
    private static final String SPLIT = "/";
    private final DeployEnvironments deployEnvironments;
    private final PluginManager pluginManager;
    private final TerraformBootConfig terraformBootConfig;
    private final String port;
    private final TerraformFromScriptsApi terraformFromScriptsApi;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DeployServiceEntityHandler deployServiceEntityHandler;

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
            TerraformFromScriptsApi terraformFromScriptsApi,
            @Value("${server.port}") String port,
            DeployServiceEntityHandler deployServiceEntityHandler) {
        this.deployEnvironments = deployEnvironments;
        this.pluginManager = pluginManager;
        this.terraformBootConfig = terraformBootConfig;
        this.terraformFromScriptsApi = terraformFromScriptsApi;
        this.port = port;
        this.deployServiceEntityHandler = deployServiceEntityHandler;
    }

    @Override
    public DeployResult deploy(DeployTask deployTask) {
        DeployResult result = new DeployResult();
        TerraformAsyncDeployFromScriptsRequest request = getDeployRequest(deployTask);
        try {
            setHeaderTokenByProfiles();
            terraformFromScriptsApi.asyncDeployWithScripts(request, deployTask.getId());
            result.setId(deployTask.getId());
            return result;
        } catch (RestClientException e) {
            log.error("terraform-boot deploy service failed. service id: {} , error:{} ",
                    deployTask.getId(), e.getMessage());
            throw new TerraformBootRequestFailedException(e.getMessage());
        }
    }

    @Override
    public DeployResult destroy(DeployTask task) {
        DeployResult result = new DeployResult();
        DeployServiceEntity deployServiceEntity =
                this.deployServiceEntityHandler.getDeployServiceEntity(task.getId());
        String resourceState = TfResourceTransUtils.getStoredStateContent(deployServiceEntity);
        TerraformAsyncDestroyFromScriptsRequest request = getDestroyRequest(task, resourceState);
        try {
            setHeaderTokenByProfiles();
            terraformFromScriptsApi.asyncDestroyWithScripts(request, task.getId());
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
                    terraformFromScriptsApi.validateWithScripts(getValidateRequest(ocl),
                            Objects.nonNull(MDC.get("TASK_ID"))
                                    ? UUID.fromString(MDC.get("TASK_ID")) : null);
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
    public String getDeploymentPlanAsJson(DeployTask task) {
        setHeaderTokenByProfiles();
        TerraformPlan terraformPlan =
                terraformFromScriptsApi.planWithScripts(getPlanWithScriptsRequest(task),
                        task.getId());
        return terraformPlan.getPlan();
    }

    private TerraformDeployWithScriptsRequest getValidateRequest(Ocl ocl) {
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


    private TerraformAsyncDestroyFromScriptsRequest getDestroyRequest(DeployTask task,
                                                                      String stateFile)
            throws TerraformBootRequestFailedException {
        TerraformAsyncDestroyFromScriptsRequest request =
                new TerraformAsyncDestroyFromScriptsRequest();
        request.setScripts(getFiles(task));
        request.setTfState(stateFile);
        request.setVariables(getInputVariables(task, false));
        request.setEnvVariables(getEnvironmentVariables(task));
        WebhookConfig hookConfig = new WebhookConfig();
        String callbackUrl = getClientRequestBaseUrl(port)
                + terraformBootConfig.getDestroyCallbackUri();
        hookConfig.setUrl(callbackUrl + SPLIT + task.getId());
        hookConfig.setAuthType(WebhookConfig.AuthTypeEnum.NONE);
        request.setWebhookConfig(hookConfig);
        return request;
    }

    private TerraformAsyncDeployFromScriptsRequest getDeployRequest(DeployTask task) {
        TerraformAsyncDeployFromScriptsRequest request =
                new TerraformAsyncDeployFromScriptsRequest();
        request.setIsPlanOnly(false);
        request.setScripts(getFiles(task));
        request.setVariables(getInputVariables(task, true));
        request.setEnvVariables(getEnvironmentVariables(task));
        WebhookConfig hookConfig = new WebhookConfig();
        String callbackUrl = getClientRequestBaseUrl(port)
                + terraformBootConfig.getDeployCallbackUri();
        hookConfig.setUrl(callbackUrl + SPLIT + task.getId());
        hookConfig.setAuthType(WebhookConfig.AuthTypeEnum.NONE);
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
        String provider = getProvider(csp, region);
        String deployer = task.getOcl().getDeployment().getDeployer();
        return Arrays.asList(provider, deployer);
    }

    private List<String> getFilesByOcl(Ocl ocl) {
        Csp csp = ocl.getCloudServiceProvider().getName();
        String region = ocl.getCloudServiceProvider().getRegions().getFirst().getName();
        String provider = getProvider(csp, region);
        String deployer = ocl.getDeployment().getDeployer();
        return Arrays.asList(provider, deployer);
    }

    private String getProvider(Csp csp, String region) {
        String provider = pluginManager.getDeployerProvider(csp, DeployerKind.TERRAFORM, region);
        if (StringUtils.isBlank(provider)) {
            String errMsg = String.format("Terraform provider for Csp %s not found.", csp);
            throw new TerraformProviderNotFoundException(errMsg);
        }
        return provider;
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
            terraformFromScriptsApi.getApiClient().setAccessToken(CurrentUserInfoHolder.getToken());
        }
    }

}
