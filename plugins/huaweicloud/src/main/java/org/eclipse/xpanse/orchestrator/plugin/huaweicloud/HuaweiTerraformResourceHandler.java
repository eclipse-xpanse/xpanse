/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.huaweicloud;

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
 * Terraform resource handler for Huawei.
 */
@Component
@Slf4j
public class HuaweiTerraformResourceHandler implements DeployResourceHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Handler of HuaweiCloud for the DeployResult.
     *
     * @param deployResult the result of the deployment.
     */
    @Override
    public void handler(DeployResult deployResult) {
        List<DeployResource> xpResourceList = new ArrayList<>();
        TfState tfState;
        try {
            var stateFile = deployResult.getRawResources().get("stateFile");
            if (!stateFile.getClass().equals(String.class)) {
                throw new RuntimeException("stateFile is unsupported.");
            }
            tfState = objectMapper.readValue(stateFile.toString(), TfState.class);
        } catch (IOException ex) {
            log.error("Parse terraform state content failed.");
            throw new TerraformExecutorException("Parse terraform state content failed.", ex);
        }
        for (TfStateResource tfStateResource : tfState.getResources()) {
            DeployResource deployResource = new DeployResource();
            if (tfStateResource.getType().equals("huaweicloud_compute_instance")) {
                for (TfStateResourceInstance instance : tfStateResource.getInstances()) {
                    deployResource.setKind(DeployResourceKind.Vm);
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
            if (tfStateResource.getType().equals("huaweicloud_compute_eip_associate")) {
                for (TfStateResourceInstance instance : tfStateResource.getInstances()) {
                    deployResource.setProperty(new HashMap<>());
                    deployResource.getProperty()
                            .put("ip", (String) instance.getAttributes().get("public_ip"));
                    deployResource.setKind(DeployResourceKind.PublicIp);
                }
            }
            if (tfStateResource.getType().equals("huaweicloud_vpc")) {
                for (TfStateResourceInstance instance : tfStateResource.getInstances()) {
                    DeployResource xpResource = new DeployResource();
                    xpResource.setKind(DeployResourceKind.Vpc);
                    xpResource.setResourceId((String) instance.getAttributes().get("id"));
                    xpResource.setName((String) instance.getAttributes().get("name"));
                }
            }
            xpResourceList.add(deployResource);
        }

        deployResult.setResources(xpResourceList);
    }
}
