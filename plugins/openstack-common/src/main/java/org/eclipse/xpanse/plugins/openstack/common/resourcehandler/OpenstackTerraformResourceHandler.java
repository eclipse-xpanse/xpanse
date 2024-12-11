/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.openstack.common.resourcehandler;

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

/** Terraform resource handler for Openstack. */
@Component
@Slf4j
public class OpenstackTerraformResourceHandler implements DeployResourceHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Handler of Openstack for the DeployResult.
     *
     * @param deployResult the result of the deployment.
     */
    @Override
    public void handler(DeployResult deployResult) {
        List<DeployResource> deployResourceList = new ArrayList<>();
        TfState tfState;
        try {
            var stateFile = deployResult.getTfStateContent();
            tfState = objectMapper.readValue(stateFile, TfState.class);
        } catch (IOException ex) {
            log.error("Parse terraform state content failed.");
            throw new TerraformExecutorException("Parse terraform state content failed.", ex);
        }
        if (Objects.nonNull(tfState)) {
            if (Objects.nonNull(tfState.getOutputs()) && !tfState.getOutputs().isEmpty()) {
                for (String outputKey : tfState.getOutputs().keySet()) {
                    TfOutput tfOutput = tfState.getOutputs().get(outputKey);
                    deployResult.getOutputProperties().put(outputKey, tfOutput.getValue());
                }
            }
            Set<String> supportTypes =
                    OpenstackTerraformResourceProperties.getTerraformResourceTypes();
            for (TfStateResource tfStateResource : tfState.getResources()) {
                if (!supportTypes.contains(tfStateResource.getType())) {
                    log.info(
                            "The resource type {} is unsupported to parse.",
                            tfStateResource.getType());
                    continue;
                }
                DeployResourceProperties deployResourceProperties =
                        OpenstackTerraformResourceProperties.getDeployResourceProperties(
                                tfStateResource.getType());
                for (TfStateResourceInstance instance : tfStateResource.getInstances()) {
                    DeployResource deployResource = new DeployResource();
                    deployResource.setGroupType(tfStateResource.getType());
                    deployResource.setGroupName(tfStateResource.getName());
                    deployResource.setResourceKind(deployResourceProperties.getResourceKind());
                    TfResourceTransUtils.fillDeployResource(
                            instance,
                            deployResource,
                            deployResourceProperties.getResourceProperties());
                    deployResourceList.add(deployResource);
                }
            }
        }
        deployResult.setResources(deployResourceList);
    }
}
