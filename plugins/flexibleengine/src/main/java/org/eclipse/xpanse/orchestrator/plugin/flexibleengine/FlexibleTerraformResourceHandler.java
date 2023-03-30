/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.flexibleengine;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.deployment.DeployResourceHandler;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.exceptions.TerraformExecutorException;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.resource.TfState;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.resource.TfStateResource;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.resource.TfStateResourceInstance;
import org.eclipse.xpanse.modules.models.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.service.DeployResource;
import org.eclipse.xpanse.modules.models.service.DeployResult;
import org.springframework.stereotype.Component;

/**
 * Terraform resource handler for FlexibleEngine.
 */
@Component
@Slf4j
public class FlexibleTerraformResourceHandler implements DeployResourceHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Handler of FlexibleEngineCloud for the DeployResult.
     *
     * @param deployResult the result of the deployment.
     */
    @Override
    public void handler(DeployResult deployResult) {
        List<DeployResource> xpResourceList = new ArrayList<>();
        TfState tfState;
        try {
            var stateFile = deployResult.getProperty().get("stateFile");
            tfState = objectMapper.readValue(stateFile, TfState.class);
            deployResult.getProperty().remove("stateFile");
        } catch (IOException ex) {
            log.error("Parse terraform state content failed.");
            throw new TerraformExecutorException("Parse terraform state content failed.", ex);
        }
        for (TfStateResource tfStateResource : tfState.getResources()) {
            if (tfStateResource.getType().equals("flexibleengine_compute_instance_v2")) {
                for (TfStateResourceInstance instance : tfStateResource.getInstances()) {
                    DeployResource deployResource = new DeployResource();
                    deployResource.setKind(DeployResourceKind.VM);
                    deployResource.setResourceId((String) instance.getAttributes().get("id"));
                    deployResource.setName((String) instance.getAttributes().get("name"));

                    deployResource.setProperty(new HashMap<>());
                    deployResource.getProperty()
                            .put("ipv4", (String) instance.getAttributes().get("access_ip_v4"));
                    deployResource.getProperty()
                            .put("image_id", (String) instance.getAttributes().get("image_id"));
                    deployResource.getProperty()
                            .put("image_name", (String) instance.getAttributes().get("image_name"));
                    deployResource.getProperty()
                            .put("region", (String) instance.getAttributes().get("region"));
                    xpResourceList.add(deployResource);
                }
            }
            if (tfStateResource.getType().equals("flexibleengine_vpc_eip")) {
                for (TfStateResourceInstance instance : tfStateResource.getInstances()) {
                    DeployResource deployResource = new DeployResource();
                    deployResource.setProperty(new HashMap<>());
                    deployResource.getProperty()
                            .put("ip", (String) instance.getAttributes().get("public_ip"));
                    deployResource.setKind(DeployResourceKind.PUBLIC_IP);
                    xpResourceList.add(deployResource);
                }
            }
            if (tfStateResource.getType().equals("flexibleengine_vpc_v1")) {
                for (TfStateResourceInstance instance : tfStateResource.getInstances()) {
                    DeployResource xpResource = new DeployResource();
                    xpResource.setKind(DeployResourceKind.VPC);
                    xpResource.setResourceId((String) instance.getAttributes().get("id"));
                    xpResource.setName((String) instance.getAttributes().get("name"));
                    xpResourceList.add(xpResource);
                }
            }

            if (tfStateResource.getType().equals("flexibleengine_blockstorage_volume_v2")) {
                for (TfStateResourceInstance instance : tfStateResource.getInstances()) {
                    DeployResource xpResource = new DeployResource();
                    xpResource.setResourceId((String) instance.getAttributes().get("id"));
                    xpResource.setName((String) instance.getAttributes().get("name"));
                    xpResourceList.add(xpResource);
                }
            }
        }
        deployResult.setResources(xpResourceList);
    }
}
