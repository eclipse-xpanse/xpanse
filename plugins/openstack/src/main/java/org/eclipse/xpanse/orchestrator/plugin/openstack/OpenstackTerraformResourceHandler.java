/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.openstack;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
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
 * Terraform resource handler for Openstack.
 */
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
        List<DeployResource> xpResourceList = new ArrayList<>();
        TfState tfState;
        try {
            var stateFile = deployResult.getProperty().get("stateFile");
            tfState = objectMapper.readValue(stateFile, TfState.class);
        } catch (IOException ex) {
            log.error("Parse terraform state content failed.");
            throw new TerraformExecutorException("Parse terraform state content failed.", ex);
        }
        for (TfStateResource tfStateResource : tfState.getResources()) {
            DeployResource deployResource = null;
            if (tfStateResource.getType().equals("openstack_compute_instance_v2")) {
                deployResource = new DeployResource();
                for (TfStateResourceInstance instance : tfStateResource.getInstances()) {
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
                }
            }
            if (!Objects.isNull(deployResource)) {
                xpResourceList.add(deployResource);
            }
        }
        deployResult.setResources(xpResourceList);
    }
}
