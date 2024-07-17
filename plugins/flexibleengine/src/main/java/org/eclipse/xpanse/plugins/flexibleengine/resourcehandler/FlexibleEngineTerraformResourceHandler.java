/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.flexibleengine.resourcehandler;

import static org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformlocal.TerraformLocalDeployment.STATE_FILE_NAME;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.exceptions.TerraformExecutorException;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.resources.TfOutput;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.resources.TfState;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.resources.TfStateResource;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.resources.TfStateResourceInstance;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.utils.TfResourceTransUtils;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResourceHandler;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResourceProperties;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResult;
import org.springframework.stereotype.Component;

/**
 * Terraform resource handler for FlexibleEngine.
 */
@Component
@Slf4j
public class FlexibleEngineTerraformResourceHandler implements DeployResourceHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Handler of FlexibleEngineCloud for the DeployResult.
     *
     * @param deployResult the result of the deployment.
     */
    @Override
    public void handler(DeployResult deployResult) {
        List<DeployResource> deployResourceList = new ArrayList<>();
        TfState tfState;
        try {
            var stateFile = deployResult.getPrivateProperties().get(STATE_FILE_NAME);
            tfState = objectMapper.readValue(stateFile, TfState.class);
        } catch (IOException ex) {
            log.error("Parse terraform state content failed.");
            throw new TerraformExecutorException("Parse terraform state content failed.", ex);
        }
        if (Objects.nonNull(tfState)) {
            if (Objects.nonNull(tfState.getOutputs()) && !tfState.getOutputs().isEmpty()) {
                for (String outputKey : tfState.getOutputs().keySet()) {
                    TfOutput tfOutput = tfState.getOutputs().get(outputKey);
                    deployResult.getProperties().put(outputKey, tfOutput.getValue());
                }
            }
            Set<String> supportTypes =
                    FlexibleEngineTerraformResourceProperties.getTerraformResourceTypes();
            for (TfStateResource tfStateResource : tfState.getResources()) {
                if (!supportTypes.contains(tfStateResource.getType())) {
                    log.info("The resource type {} is unsupported to parse.",
                            tfStateResource.getType());
                    continue;
                }
                DeployResourceProperties deployResourceProperties =
                        FlexibleEngineTerraformResourceProperties.getDeployResourceProperties(
                                tfStateResource.getType());
                for (TfStateResourceInstance instance : tfStateResource.getInstances()) {
                    DeployResource deployResource = new DeployResource();
                    deployResource.setGroupType(tfStateResource.getType());
                    deployResource.setGroupName(tfStateResource.getName());
                    deployResource.setResourceKind(deployResourceProperties.getResourceKind());
                    TfResourceTransUtils.fillDeployResource(instance, deployResource,
                            deployResourceProperties.getResourceProperties());
                    deployResourceList.add(deployResource);
                }
            }
        }
        deployResult.setResources(deployResourceList);
    }
}
