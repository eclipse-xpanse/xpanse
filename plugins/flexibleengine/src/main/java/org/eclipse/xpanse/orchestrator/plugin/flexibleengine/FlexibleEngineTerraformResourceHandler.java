/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.flexibleengine;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.deployment.DeployResourceHandler;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.exceptions.TerraformExecutorException;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.resource.TfOutput;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.resource.TfState;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.resource.TfStateResource;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.resource.TfStateResourceInstance;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.utils.TfResourceTransUtils;
import org.eclipse.xpanse.modules.models.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.service.DeployResource;
import org.eclipse.xpanse.modules.models.service.DeployResult;
import org.eclipse.xpanse.modules.models.service.PublicIp;
import org.eclipse.xpanse.modules.models.service.Vm;
import org.eclipse.xpanse.modules.models.service.Volume;
import org.eclipse.xpanse.modules.models.service.Vpc;
import org.eclipse.xpanse.orchestrator.plugin.flexibleengine.models.FlexibleEngineResourceProperty;
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
            var stateFile = deployResult.getPrivateProperties().get("stateFile");
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
            for (TfStateResource tfStateResource : tfState.getResources()) {
                if (tfStateResource.getType().equals("flexibleengine_compute_instance_v2")) {
                    for (TfStateResourceInstance instance : tfStateResource.getInstances()) {
                        DeployResource deployResource = new Vm();
                        deployResource.setKind(DeployResourceKind.VM);
                        TfResourceTransUtils.fillDeployResource(instance, deployResource,
                                FlexibleEngineResourceProperty.getProperties(
                                        DeployResourceKind.VM));
                        deployResourceList.add(deployResource);
                    }
                }
                if (tfStateResource.getType().equals("flexibleengine_vpc_eip")) {
                    for (TfStateResourceInstance instance : tfStateResource.getInstances()) {
                        DeployResource deployResource = new PublicIp();
                        deployResource.setKind(DeployResourceKind.PUBLIC_IP);
                        TfResourceTransUtils.fillDeployResource(instance, deployResource,
                                FlexibleEngineResourceProperty.getProperties(
                                        DeployResourceKind.PUBLIC_IP));
                        deployResourceList.add(deployResource);
                    }
                }
                if (tfStateResource.getType().equals("flexibleengine_vpc_subnet_v1")) {
                    for (TfStateResourceInstance instance : tfStateResource.getInstances()) {
                        DeployResource deployResource = new Vpc();
                        deployResource.setKind(DeployResourceKind.VPC);
                        TfResourceTransUtils.fillDeployResource(instance, deployResource,
                                FlexibleEngineResourceProperty.getProperties(
                                        DeployResourceKind.VPC));
                        deployResourceList.add(deployResource);
                    }
                }
                if (tfStateResource.getType().equals("flexibleengine_blockstorage_volume_v2")) {
                    for (TfStateResourceInstance instance : tfStateResource.getInstances()) {
                        DeployResource deployResource = new Volume();
                        deployResource.setKind(DeployResourceKind.VOLUME);
                        TfResourceTransUtils.fillDeployResource(instance, deployResource,
                                FlexibleEngineResourceProperty.getProperties(
                                        DeployResourceKind.VOLUME));
                        deployResourceList.add(deployResource);
                    }
                }
            }
        }
        deployResult.setResources(deployResourceList);
    }
}
