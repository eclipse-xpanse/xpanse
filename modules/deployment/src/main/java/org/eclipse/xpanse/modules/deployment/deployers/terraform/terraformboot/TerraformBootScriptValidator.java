/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.exceptions.TerraformBootRequestFailedException;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.api.TerraformFromGitRepoApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.api.TerraformFromScriptsApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformDeployFromGitRepoRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformDeployWithScriptsRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformValidationResult;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployValidationResult;
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
    public DeployValidationResult validateTerraformScripts(Ocl ocl) {
        terraformBootHelper.setHeaderTokenByProfiles();
        DeployValidationResult deployValidationResult = null;
        try {
            TerraformValidationResult validate =
                    terraformFromScriptsApi.validateWithScripts(getValidateScriptsInOclRequest(ocl),
                            Objects.nonNull(MDC.get("TASK_ID"))
                                    ? UUID.fromString(MDC.get("TASK_ID")) : null);
            try {
                deployValidationResult =
                        objectMapper.readValue(objectMapper.writeValueAsString(validate),
                                DeployValidationResult.class);
            } catch (JsonProcessingException e) {
                log.error("JsonProcessingException", e);
            }
        } catch (RestClientException restClientException) {
            log.error("Request to terraform-boot API failed", restClientException);
            throw new TerraformBootRequestFailedException(restClientException.getMessage());
        }
        return deployValidationResult;
    }

    /**
     * Validate scripts in the GIT repo.
     */
    public DeployValidationResult validateTerraformScriptsFromGitRepo(Ocl ocl) {
        terraformBootHelper.setHeaderTokenByProfiles();
        DeployValidationResult deployValidationResult = null;
        try {
            TerraformValidationResult validate =
                    terraformFromGitRepoApi.validateScriptsFromGitRepo(
                            getValidateScriptsInGitRepoRequest(ocl),
                            Objects.nonNull(MDC.get("TASK_ID"))
                                    ? UUID.fromString(MDC.get("TASK_ID")) : null);
            try {
                deployValidationResult =
                        objectMapper.readValue(objectMapper.writeValueAsString(validate),
                                DeployValidationResult.class);
            } catch (JsonProcessingException e) {
                log.error("JsonProcessingException", e);
            }
        } catch (RestClientException restClientException) {
            log.error("Request to terraform-boot API failed", restClientException);
            throw new TerraformBootRequestFailedException(restClientException.getMessage());
        }
        return deployValidationResult;
    }

    private TerraformDeployWithScriptsRequest getValidateScriptsInOclRequest(Ocl ocl) {
        TerraformDeployWithScriptsRequest request =
                new TerraformDeployWithScriptsRequest();
        request.setIsPlanOnly(false);
        request.setScripts(getFilesByOcl(ocl));
        return request;
    }

    private TerraformDeployFromGitRepoRequest getValidateScriptsInGitRepoRequest(Ocl ocl) {
        TerraformDeployFromGitRepoRequest request =
                new TerraformDeployFromGitRepoRequest();
        request.setIsPlanOnly(false);
        request.setGitRepoDetails(
                terraformBootHelper.convertTerraformScriptGitRepoDetailsFromDeployFromGitRepo(
                        ocl.getDeployment().getScriptsRepo()));
        return request;
    }

    private List<String> getFilesByOcl(Ocl ocl) {
        Csp csp = ocl.getCloudServiceProvider().getName();
        String region = ocl.getCloudServiceProvider().getRegions().getFirst().getName();
        String provider = terraformBootHelper.getProvider(csp, region);
        String deployer = ocl.getDeployment().getDeployer();
        return Arrays.asList(provider, deployer);
    }
}
