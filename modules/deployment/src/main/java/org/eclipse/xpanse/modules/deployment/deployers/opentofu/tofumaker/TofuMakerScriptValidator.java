/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker;

import static org.eclipse.xpanse.modules.logging.CustomRequestIdGenerator.TASK_ID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
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
import org.eclipse.xpanse.modules.models.servicetemplate.Deployment;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * constructor for OpenTofuMakerScriptValidator.
     */
    public TofuMakerScriptValidator(OpenTofuFromScriptsApi openTofuFromScriptsApi,
                                    OpenTofuFromGitRepoApi openTofuFromGitRepoApi,
                                    TofuMakerHelper tofuMakerHelper) {
        this.openTofuFromScriptsApi = openTofuFromScriptsApi;
        this.openTofuFromGitRepoApi = openTofuFromGitRepoApi;
        this.tofuMakerHelper = tofuMakerHelper;
    }

    /**
     * Validate scripts provided in the OCL.
     */
    public DeploymentScriptValidationResult validateOpenTofuScripts(Deployment deployment) {
        DeploymentScriptValidationResult deployValidationResult = null;
        try {
            OpenTofuValidationResult validate =
                    openTofuFromScriptsApi.validateWithScripts(
                            getValidateScriptsInOclRequest(deployment));
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
    public DeploymentScriptValidationResult validateOpenTofuScriptsFromGitRepo(
            Deployment deployment) {
        DeploymentScriptValidationResult deployValidationResult = null;
        try {
            OpenTofuValidationResult validate =
                    openTofuFromGitRepoApi.validateScriptsFromGitRepo(
                            getValidateScriptsInGitRepoRequest(deployment));
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

    private OpenTofuDeployWithScriptsRequest getValidateScriptsInOclRequest(Deployment deployment) {
        OpenTofuDeployWithScriptsRequest request =
                new OpenTofuDeployWithScriptsRequest();
        UUID uuid = Objects.nonNull(MDC.get(TASK_ID))
                ? UUID.fromString(MDC.get(TASK_ID)) : UUID.randomUUID();
        request.setRequestId(uuid);
        request.setIsPlanOnly(false);
        request.setScripts(getFilesByOcl(deployment));
        return request;
    }

    private OpenTofuDeployFromGitRepoRequest getValidateScriptsInGitRepoRequest(
            Deployment deployment) {
        OpenTofuDeployFromGitRepoRequest request =
                new OpenTofuDeployFromGitRepoRequest();
        UUID uuid = Objects.nonNull(MDC.get(TASK_ID))
                ? UUID.fromString(MDC.get(TASK_ID)) : UUID.randomUUID();
        request.setRequestId(uuid);
        request.setIsPlanOnly(false);
        request.setGitRepoDetails(
                tofuMakerHelper.convertOpenTofuScriptGitRepoDetailsFromDeployFromGitRepo(
                        deployment.getScriptsRepo()));
        return request;
    }

    private List<String> getFilesByOcl(Deployment deployment) {
        String deployer = deployment.getDeployer();
        return Collections.singletonList(deployer);
    }
}
