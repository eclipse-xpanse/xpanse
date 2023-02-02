/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.terraform;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.osc.modules.ocl.loader.data.models.Ocl;
import org.eclipse.osc.modules.ocl.loader.data.models.OclResources;
import org.eclipse.osc.modules.ocl.loader.data.models.RuntimeBase;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.exceptions.TerraformExecutorException;

/**
 * Terraform executor to install resources based on Ocl.
 */
@Slf4j
public class OclTerraformExecutor extends TerraformExecutor {

    private final Ocl ocl;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OclTerraformExecutor(final Ocl ocl, Map<String, String> env) {
        super(env);
        this.ocl = ocl;
    }

    public void createWorkspace() {
        super.createWorkspace(ocl.getName());
    }

    /**
     * Creates terraform script based on Ocl descriptor.
     */
    public void createTerraformScript() {

        Ocl2Hcl hcl = new Ocl2Hcl(ocl);
        String hclStr = hcl.getHcl();

        super.createTerraformScript(hclStr);
    }

    private void updateOclObject(
            RuntimeBase runtimeObj, String resourceType, String resourceName, TfState tfState) {
        for (var resource : tfState.getResources()) {
            if (resource.getInstances().size() < 1 || !resource.getType().equals(resourceType)) {
                continue;
            }

            TfStateResourceInstance instance = resource.getInstances().get(0);
            if (resource.getName().equals(resourceName)
                    || (instance.attributes.containsKey("name")
                    && instance.attributes.get("name").equals(resourceName))) {
                runtimeObj.setId(instance.attributes.get("id").toString());
                runtimeObj.setState("active");
            }
        }
    }

    private void updateOclRuntime() {
        TfState tfState;
        try {
            tfState = objectMapper.readValue(getTerraformState(), TfState.class);
        } catch (IOException ex) {
            log.error("Parse terraform state content failed.");
            throw new TerraformExecutorException("Parse terraform state content failed.", ex);
        }
        for (var secGroup : ocl.getNetwork().getSecurity()) {
            updateOclObject(
                    secGroup, "huaweicloud_networking_secgroup", secGroup.getName(), tfState);
        }

        for (var subnet : ocl.getNetwork().getSubnet()) {
            updateOclObject(subnet, "huaweicloud_vpc_subnet", subnet.getName(), tfState);
        }

        for (var vm : ocl.getCompute().getVm()) {
            updateOclObject(vm, "huaweicloud_compute_instance", vm.getName(), tfState);
        }

        for (var vpc : ocl.getNetwork().getVpc()) {
            updateOclObject(vpc, "huaweicloud_vpc", vpc.getName(), tfState);
        }
    }

    /**
     * Updates resources requested by Ocl descriptors on the target environment.
     *
     * @param oclResources List of OclResources to be managed by Terraform.
     */
    public void updateOclResources(OclResources oclResources) {
        TfState tfState;
        try {
            tfState = objectMapper.readValue(getTerraformState(), TfState.class);
        } catch (IOException ex) {
            log.error("Parse terraform state content failed.");
            throw new TerraformExecutorException("Parse terraform state content failed.", ex);
        }

        TfResources tfResources = new TfResources();
        tfResources.update(tfState);

        oclResources.getResources().addAll(tfResources.getResources());
    }
}
