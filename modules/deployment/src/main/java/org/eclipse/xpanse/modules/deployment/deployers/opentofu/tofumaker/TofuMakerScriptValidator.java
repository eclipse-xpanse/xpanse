/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.exceptions.OpenTofuMakerRequestFailedException;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.api.OpenTofuFromGitRepoApi;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.api.OpenTofuFromScriptsApi;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuDeployFromGitRepoRequest;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuDeployWithScriptsRequest;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuValidationResult;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.utils.OpenTofuProviderHelper;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeploymentScriptValidationResult;
import org.slf4j.MDC;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/**
 * Bean to validate openTofu scripts via tofu-maker.
 */
@Slf4j
@Component
@Profile("tofu-maker")
public class TofuMakerScriptValidator {

    private final OpenTofuFromScriptsApi openTofuFromScriptsApi;
    private final OpenTofuFromGitRepoApi openTofuFromGitRepoApi;
    private final TofuMakerHelper tofuMakerHelper;
    private final OpenTofuProviderHelper openTofuProviderHelper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * constructor for OpenTofuMakerScriptValidator.
     */
    public TofuMakerScriptValidator(OpenTofuFromScriptsApi openTofuFromScriptsApi,
                                    OpenTofuFromGitRepoApi openTofuFromGitRepoApi,
                                    TofuMakerHelper tofuMakerHelper,
                                    OpenTofuProviderHelper openTofuProviderHelper) {
        this.openTofuFromScriptsApi = openTofuFromScriptsApi;
        this.openTofuFromGitRepoApi = openTofuFromGitRepoApi;
        this.tofuMakerHelper = tofuMakerHelper;
        this.openTofuProviderHelper = openTofuProviderHelper;
    }

    /**
     * Validate scripts provided in the OCL.
     */
    public DeploymentScriptValidationResult validateOpenTofuScripts(Ocl ocl) {
        DeploymentScriptValidationResult deployValidationResult = null;
        try {
            OpenTofuValidationResult validate =
                    openTofuFromScriptsApi.validateWithScripts(getValidateScriptsInOclRequest(ocl),
                            Objects.nonNull(MDC.get("TASK_ID"))
                                    ? UUID.fromString(MDC.get("TASK_ID")) : null);
            try {
                deployValidationResult =
                        objectMapper.readValue(objectMapper.writeValueAsString(validate),
                                DeploymentScriptValidationResult.class);
            } catch (JsonProcessingException e) {
                log.error("JsonProcessingException", e);
            }
        } catch (RestClientException restClientException) {
            log.error("Request to tofu-maker API failed", restClientException);
            throw new OpenTofuMakerRequestFailedException(restClientException.getMessage());
        }
        return deployValidationResult;
    }

    /**
     * Validate scripts in the GIT repo.
     */
    public DeploymentScriptValidationResult validateOpenTofuScriptsFromGitRepo(Ocl ocl) {
        DeploymentScriptValidationResult deployValidationResult = null;
        try {
            OpenTofuValidationResult validate =
                    openTofuFromGitRepoApi.validateScriptsFromGitRepo(
                            getValidateScriptsInGitRepoRequest(ocl),
                            Objects.nonNull(MDC.get("TASK_ID"))
                                    ? UUID.fromString(MDC.get("TASK_ID")) : null);
            try {
                deployValidationResult =
                        objectMapper.readValue(objectMapper.writeValueAsString(validate),
                                DeploymentScriptValidationResult.class);
            } catch (JsonProcessingException e) {
                log.error("JsonProcessingException", e);
            }
        } catch (RestClientException restClientException) {
            log.error("Request to tofu-maker API failed", restClientException);
            throw new OpenTofuMakerRequestFailedException(restClientException.getMessage());
        }
        return deployValidationResult;
    }

    private OpenTofuDeployWithScriptsRequest getValidateScriptsInOclRequest(Ocl ocl) {
        OpenTofuDeployWithScriptsRequest request =
                new OpenTofuDeployWithScriptsRequest();
        request.setIsPlanOnly(false);
        request.setScripts(getFilesByOcl(ocl));
        return request;
    }

    private OpenTofuDeployFromGitRepoRequest getValidateScriptsInGitRepoRequest(Ocl ocl) {
        OpenTofuDeployFromGitRepoRequest request =
                new OpenTofuDeployFromGitRepoRequest();
        request.setIsPlanOnly(false);
        request.setGitRepoDetails(
                tofuMakerHelper.convertOpenTofuScriptGitRepoDetailsFromDeployFromGitRepo(
                        ocl.getDeployment().getScriptsRepo()));
        return request;
    }

    private List<String> getFilesByOcl(Ocl ocl) {
        Csp csp = ocl.getCloudServiceProvider().getName();
        String region = ocl.getCloudServiceProvider().getRegions().getFirst().getName();
        String provider = openTofuProviderHelper.getProvider(csp, region);
        String deployer = ocl.getDeployment().getDeployer();
        return Arrays.asList(provider, deployer);
    }
}
