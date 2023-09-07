/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform;

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
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.TerraformValidationResult;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.WebhookConfig;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.WebhookConfig.AuthTypeEnum;
import org.eclipse.xpanse.modules.deployment.utils.DeployEnvironments;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResult;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.TerraformBootRequestFailedException;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployValidationResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.Deployment;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Implementation of the deployment with terraform-boot.
 */
@Slf4j
@Profile("terraform-boot")
@Component
public class TerraformBootDeployment implements Deployment {

    public static final String STATE_FILE_NAME = "terraform.tfstate";
    private final DeployEnvironments deployEnvironments;
    private final TerraformVersionProvider terraformVersionProvider;
    private final TerraformBootConfig terraformBootConfig;
    private final String port;
    private final TerraformApi terraformApi;

    /**
     * Initializes the TerraformBoot deployer.
     */
    @Autowired
    public TerraformBootDeployment(
            DeployEnvironments deployEnvironments,
            TerraformVersionProvider terraformVersionProvider,
            TerraformBootConfig terraformBootConfig,
            TerraformApi terraformApi,
            @Value("${server.port}") String port) {
        this.deployEnvironments = deployEnvironments;
        this.terraformVersionProvider = terraformVersionProvider;
        this.terraformBootConfig = terraformBootConfig;
        this.terraformApi = terraformApi;
        this.port = port;
    }

    @Override
    public DeployResult deploy(DeployTask deployTask) {
        DeployResult result = new DeployResult();
        TerraformAsyncDeployFromDirectoryRequest request = getDeployRequest(deployTask);
        terraformApi.asyncDeployWithScripts(request);
        return result;
    }

    @Override
    public DeployResult destroy(DeployTask task, String stateFile) {
        DeployResult result = new DeployResult();
        TerraformAsyncDestroyFromDirectoryRequest request = getDestroyRequest(task, stateFile);
        terraformApi.asyncDestroyWithScripts(request);
        return result;
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
        TerraformValidationResult validate =
                terraformApi.validateWithScripts(getDeployWithScriptsRequest(ocl));
        DeployValidationResult result = new DeployValidationResult();
        BeanUtils.copyProperties(validate, result);
        return result;
    }

    private TerraformDeployWithScriptsRequest getDeployWithScriptsRequest(Ocl ocl) {
        TerraformDeployWithScriptsRequest request =
                new TerraformDeployWithScriptsRequest();
        request.setIsPlanOnly(false);
        request.setScripts(getFilesByOcl(ocl));
        return request;

    }

    private TerraformAsyncDestroyFromDirectoryRequest getDestroyRequest(DeployTask task,
            String stateFile)
            throws TerraformBootRequestFailedException {
        TerraformAsyncDestroyFromDirectoryRequest request =
                new TerraformAsyncDestroyFromDirectoryRequest();
        request.setScripts(getFiles(task));
        request.setTfState(stateFile);
        request.setVariables(getInputVariables(task));
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
        request.setVariables(getInputVariables(task));
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
        Csp csp = task.getCreateRequest().getCsp();
        String version = terraformVersionProvider.getTerraformVersionByCsp(csp);
        String region = task.getCreateRequest().getRegion();
        String provider = TerraformProviders.getProvider(csp).getProvider(version, region);
        String deployer = task.getOcl().getDeployment().getDeployer();
        return Arrays.asList(provider, deployer);
    }

    private List<String> getFilesByOcl(Ocl ocl) {
        Csp csp = ocl.getCloudServiceProvider().getName();
        String version =
                terraformVersionProvider.getTerraformVersionByCsp(
                        ocl.getCloudServiceProvider().getName());
        String region = ocl.getCloudServiceProvider().getRegions().get(0).getName();
        String provider = TerraformProviders.getProvider(csp).getProvider(version, region);
        String deployer = ocl.getDeployment().getDeployer();
        return Arrays.asList(provider, deployer);
    }

    private Map<String, Object> getInputVariables(DeployTask deployTask) {
        Map<String, Object> inputVariables = new HashMap<>();
        inputVariables.putAll(this.deployEnvironments.getVariables(deployTask));
        inputVariables.putAll(this.deployEnvironments.getFlavorVariables(deployTask));
        return inputVariables;
    }

    private Map<String, String> getEnvironmentVariables(DeployTask deployTask) {
        return deployEnvironments.getEnv(deployTask);
    }
}
