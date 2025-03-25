/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot;

import static org.eclipse.xpanse.modules.logging.LoggingKeyConstant.SERVICE_ID;
import static org.eclipse.xpanse.modules.logging.LoggingKeyConstant.TRACKING_ID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.exceptions.TerraBootRequestFailedException;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.api.TerraformFromGitRepoApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.api.TerraformFromScriptsApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.model.TerraformRequestWithScripts;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.model.TerraformRequestWithScriptsGitRepo;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.model.TerraformValidationResult;
import org.eclipse.xpanse.modules.models.servicetemplate.Deployment;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeploymentScriptValidationResult;
import org.slf4j.MDC;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/** Bean to validate terraform scripts via terra-boot. */
@Slf4j
@Component
@Profile("terra-boot")
public class TerraBootScriptValidator {

    private final TerraformFromScriptsApi terraformFromScriptsApi;
    private final TerraformFromGitRepoApi terraformFromGitRepoApi;
    private final TerraBootHelper terraBootHelper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** constructor for TerraBootScriptValidator. */
    public TerraBootScriptValidator(
            TerraformFromScriptsApi terraformFromScriptsApi,
            TerraformFromGitRepoApi terraformFromGitRepoApi,
            TerraBootHelper terraBootHelper) {
        this.terraformFromScriptsApi = terraformFromScriptsApi;
        this.terraformFromGitRepoApi = terraformFromGitRepoApi;
        this.terraBootHelper = terraBootHelper;
    }

    /** Validate scripts provided in the OCL. */
    public DeploymentScriptValidationResult validateTerraformScripts(Deployment deployment) {
        DeploymentScriptValidationResult deploymentScriptValidationResult = null;
        try {
            TerraformValidationResult validate =
                    terraformFromScriptsApi.validateWithScripts(
                            getValidateScriptsInOclRequest(deployment));
            try {
                deploymentScriptValidationResult =
                        objectMapper.readValue(
                                objectMapper.writeValueAsString(validate),
                                DeploymentScriptValidationResult.class);
                deploymentScriptValidationResult.setDeployerVersionUsed(
                        deployment.getDeployerTool().getVersion());
            } catch (JsonProcessingException e) {
                log.error("JsonProcessingException", e);
            }
        } catch (RestClientException restClientException) {
            log.error("Request to terra-boot API failed", restClientException);
            throw new TerraBootRequestFailedException(restClientException.getMessage());
        }
        return deploymentScriptValidationResult;
    }

    /** Validate scripts in the GIT repo. */
    public DeploymentScriptValidationResult validateTerraformScriptsFromGitRepo(
            Deployment deployment) {
        DeploymentScriptValidationResult deploymentScriptValidationResult = null;
        try {
            TerraformValidationResult validate =
                    terraformFromGitRepoApi.validateScriptsFromGitRepo(
                            getValidateScriptsInGitRepoRequest(deployment));
            try {
                deploymentScriptValidationResult =
                        objectMapper.readValue(
                                objectMapper.writeValueAsString(validate),
                                DeploymentScriptValidationResult.class);
            } catch (JsonProcessingException e) {
                log.error("JsonProcessingException", e);
            }
        } catch (RestClientException restClientException) {
            log.error("Request to terra-boot API failed", restClientException);
            throw new TerraBootRequestFailedException(restClientException.getMessage());
        }
        return deploymentScriptValidationResult;
    }

    private TerraformRequestWithScripts getValidateScriptsInOclRequest(Deployment deployment) {
        TerraformRequestWithScripts request = new TerraformRequestWithScripts();
        request.setRequestId(getRequestId());
        request.setRequestType(TerraformRequestWithScripts.RequestTypeEnum.VALIDATE);
        request.setTerraformVersion(deployment.getDeployerTool().getVersion());
        request.setIsPlanOnly(false);
        request.setScriptFiles(deployment.getScriptFiles());
        return request;
    }

    private TerraformRequestWithScriptsGitRepo getValidateScriptsInGitRepoRequest(
            Deployment deployment) {
        TerraformRequestWithScriptsGitRepo request = new TerraformRequestWithScriptsGitRepo();
        request.setRequestId(getRequestId());
        request.setRequestType(TerraformRequestWithScriptsGitRepo.RequestTypeEnum.VALIDATE);
        request.setTerraformVersion(deployment.getDeployerTool().getVersion());
        request.setIsPlanOnly(false);
        request.setGitRepoDetails(
                terraBootHelper.convertTerraformScriptsGitRepoDetailsFromDeployFromGitRepo(
                        deployment.getScriptsRepo()));
        return request;
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
