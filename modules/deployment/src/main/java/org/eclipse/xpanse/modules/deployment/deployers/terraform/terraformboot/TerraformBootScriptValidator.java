/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot;

import static org.eclipse.xpanse.modules.logging.LoggingKeyConstant.SERVICE_ID;
import static org.eclipse.xpanse.modules.logging.LoggingKeyConstant.TRACKING_ID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.exceptions.TerraformBootRequestFailedException;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.api.TerraformFromGitRepoApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.api.TerraformFromScriptsApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformDeployFromGitRepoRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformDeployWithScriptsRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformValidationResult;
import org.eclipse.xpanse.modules.models.servicetemplate.Deployment;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeploymentScriptValidationResult;
import org.slf4j.MDC;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/**
 * Bean to validate terraform scripts via terraform-boot.
 */
@Slf4j
@Component
@Profile("terraform-boot")
public class TerraformBootScriptValidator {

    private final TerraformFromScriptsApi terraformFromScriptsApi;
    private final TerraformFromGitRepoApi terraformFromGitRepoApi;
    private final TerraformBootHelper terraformBootHelper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * constructor for TerraformBootScriptValidator.
     */
    public TerraformBootScriptValidator(TerraformFromScriptsApi terraformFromScriptsApi,
                                        TerraformFromGitRepoApi terraformFromGitRepoApi,
                                        TerraformBootHelper terraformBootHelper) {
        this.terraformFromScriptsApi = terraformFromScriptsApi;
        this.terraformFromGitRepoApi = terraformFromGitRepoApi;
        this.terraformBootHelper = terraformBootHelper;
    }

    /**
     * Validate scripts provided in the OCL.
     */
    public DeploymentScriptValidationResult validateTerraformScripts(Deployment deployment) {
        DeploymentScriptValidationResult deploymentScriptValidationResult = null;
        try {
            TerraformValidationResult validate =
                    terraformFromScriptsApi.validateWithScripts(
                            getValidateScriptsInOclRequest(deployment));
            try {
                deploymentScriptValidationResult =
                        objectMapper.readValue(objectMapper.writeValueAsString(validate),
                                DeploymentScriptValidationResult.class);
            } catch (JsonProcessingException e) {
                log.error("JsonProcessingException", e);
            }
        } catch (RestClientException restClientException) {
            log.error("Request to terraform-boot API failed", restClientException);
            throw new TerraformBootRequestFailedException(restClientException.getMessage());
        }
        return deploymentScriptValidationResult;
    }

    /**
     * Validate scripts in the GIT repo.
     */
    public DeploymentScriptValidationResult validateTerraformScriptsFromGitRepo(
            Deployment deployment) {
        DeploymentScriptValidationResult deploymentScriptValidationResult = null;
        try {
            TerraformValidationResult validate =
                    terraformFromGitRepoApi.validateScriptsFromGitRepo(
                            getValidateScriptsInGitRepoRequest(deployment));
            try {
                deploymentScriptValidationResult =
                        objectMapper.readValue(objectMapper.writeValueAsString(validate),
                                DeploymentScriptValidationResult.class);
            } catch (JsonProcessingException e) {
                log.error("JsonProcessingException", e);
            }
        } catch (RestClientException restClientException) {
            log.error("Request to terraform-boot API failed", restClientException);
            throw new TerraformBootRequestFailedException(restClientException.getMessage());
        }
        return deploymentScriptValidationResult;
    }

    private TerraformDeployWithScriptsRequest getValidateScriptsInOclRequest(
            Deployment deployment) {
        TerraformDeployWithScriptsRequest request = new TerraformDeployWithScriptsRequest();
        request.setRequestId(getRequestId());
        request.setIsPlanOnly(false);
        request.setScripts(getFilesByOcl(deployment));
        return request;
    }

    private TerraformDeployFromGitRepoRequest getValidateScriptsInGitRepoRequest(
            Deployment deployment) {
        TerraformDeployFromGitRepoRequest request = new TerraformDeployFromGitRepoRequest();
        request.setRequestId(getRequestId());
        request.setIsPlanOnly(false);
        request.setGitRepoDetails(
                terraformBootHelper.convertTerraformScriptGitRepoDetailsFromDeployFromGitRepo(
                        deployment.getScriptsRepo()));
        return request;
    }

    private List<String> getFilesByOcl(Deployment deployment) {
        String deployer = deployment.getDeployer();
        return Collections.singletonList(deployer);
    }

    private UUID getRequestId() {
        if (StringUtils.isNotBlank(MDC.get(TRACKING_ID))) {
            try {
                return UUID.fromString(MDC.get(SERVICE_ID));
            } catch (Exception e) {
                return UUID.randomUUID();
            }
        } else {
            return UUID.randomUUID();
        }
    }
}
