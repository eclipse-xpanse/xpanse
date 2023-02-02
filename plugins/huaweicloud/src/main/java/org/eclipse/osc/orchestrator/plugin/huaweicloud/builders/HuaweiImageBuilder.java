/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.osc.modules.ocl.loader.data.models.Ocl;
import org.eclipse.osc.modules.ocl.loader.data.models.OclResource;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.AtomBuilder;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.BuilderContext;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.packer.PackerExecutor;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.packer.PackerVars;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.terraform.TerraformExecutor;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.terraform.TfState;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.terraform.TfStateResourceInstance;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.exceptions.BuilderException;

/**
 * Class to orchestrate building VM image for Huawei cloud.
 */
@Slf4j
public class HuaweiImageBuilder extends AtomBuilder {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public HuaweiImageBuilder(Ocl ocl) {
        super(ocl);
    }

    @Override
    public String name() {
        return "Huawei-Cloud-image-Builder";
    }

    private void addImageToCtx(BuilderContext ctx, String imageName, String imageId) {
        OclResource oclResource = new OclResource();
        oclResource.setId(imageId);
        oclResource.setType("image");
        oclResource.setState("active");
        oclResource.setName(imageName);
        ctx.getOclResources().getResources().add(oclResource);
    }

    @Override
    public boolean create(BuilderContext ctx) {
        log.info("Creating Huawei Cloud Image.");
        if (ctx == null) {
            log.error("Dependent builder: {} must build first.", new HuaweiEnvBuilder(ocl).name());
            throw new BuilderException(this, "Builder context is null.");
        }

        if (ocl == null || ocl.getImage() == null || ocl.getImage().getArtifacts() == null) {
            log.info("There's no image artifacts need to be built.");
            return true;
        }

        TerraformExecutor tfExecutor = prepareExecutor(ctx);
        // Create resources for packer through terraform
        PackerVars packerVars = prepareEnv(tfExecutor);

        Map<String, String> imageCtx = new HashMap<>();

        for (var artifact : ocl.getImage().getArtifacts()) {
            PackerExecutor packerExecutor =
                    new PackerExecutor(ocl, artifact, ctx.get(new HuaweiEnvBuilder(ocl).name()));

            packerExecutor.createWorkspace();
            packerExecutor.createInstallScript();
            packerExecutor.createPackerScript(packerVars);

            String imageId = packerExecutor.packerBuild();
            addImageToCtx(ctx, artifact.getName(), imageId);

            if (!packerExecutor.packerInit()) {
                throw new BuilderException(this, "PackerExecutor.packerInit failed." + name());
            }
            if (imageId.isEmpty()) {
                throw new BuilderException(
                        this, "PackerExecutor.packerBuild failed: imageId not found." + name());
            } else {
                imageCtx.put(artifact.getName(), imageId);
            }
        }
        // save the result to context
        ctx.put(name(), imageCtx);
        // destroy packer resources through terraform
        destroyEnv(tfExecutor);
        return true;
    }

    @Override
    public boolean destroy(BuilderContext ctx) {
        log.info("Destroying Huawei Cloud Image.");
        // TODO: destroy the temporary images here or after resources created.
        TerraformExecutor tfExecutor = prepareExecutor(ctx);
        destroyEnv(tfExecutor);
        return true;
    }

    protected TerraformExecutor prepareExecutor(BuilderContext ctx) {
        if (ctx == null) {
            log.error("BuilderContext is invalid.");
            throw new BuilderException(this, "Builder context is null.");
        }
        Map<String, String> envCtx = ctx.get(new HuaweiEnvBuilder(ocl).name());
        if (envCtx == null) {
            log.error("Dependent builder: {} must build first.", new HuaweiEnvBuilder(ocl).name());
            throw new BuilderException(this, "HuaweiEnvBuilder context is null.");
        }
        TerraformExecutor executor = new TerraformExecutor(envCtx);
        executor.createWorkspace(name());
        executor.createTerraformScript(String.format(""
                + "resource \"huaweicloud_vpc\" \"vpc\" {\n"
                + "  name = \"osc-packer-%s\"\n"
                + "  cidr = \"192.168.0.0/16\"\n"
                + "}\n"
                + "\n"
                + "resource \"huaweicloud_vpc_subnet\" \"subnet\" {\n"
                + "  name       = \"osc-packer-%s\"\n"
                + "  cidr       = \"192.168.1.0/24\"\n"
                + "  gateway_ip = \"192.168.1.1\"\n"
                + "  vpc_id     = huaweicloud_vpc.vpc.id\n"
                + "}\n"
                + "\n"
                + "resource \"huaweicloud_networking_secgroup\" \"secgroup\" {\n"
                + "  name        = \"osc-packer-secgroup_%s\"\n"
                + "  description = \"Osc security group\"\n"
                + "}\n"
                + "\n"
                + "resource \"huaweicloud_networking_secgroup_rule\" \"test\" {\n"
                + "  security_group_id = huaweicloud_networking_secgroup.secgroup.id\n"
                + "  direction         = \"ingress\"\n"
                + "  ethertype         = \"IPv4\"\n"
                + "  protocol          = \"tcp\"\n"
                + "  port_range_min    = 22\n"
                + "  port_range_max    = 22\n"
                + "  remote_ip_prefix  = \"159.138.32.195/32\"\n"
                + "}", ocl.getName(), ocl.getName(), ocl.getName()));

        return executor;
    }

    /**
     * Prepares environment variables for packer image build.
     *
     * @param tfExecutor TerraformExecutor class
     * @return Environment variables needed for packer execution,
     */
    public PackerVars prepareEnv(TerraformExecutor tfExecutor) {
        log.info("Creating Huawei Cloud resources.");

        tfExecutor.tfInit();
        tfExecutor.tfPlan();
        tfExecutor.tfApply();
        String stateContent = tfExecutor.getTerraformState();

        PackerVars packerVars = new PackerVars();
        try {
            TfState tfState = objectMapper.readValue(stateContent, TfState.class);
            for (var resource : tfState.getResources()) {
                if (resource.getInstances().size() != 1) {
                    continue;
                }
                TfStateResourceInstance instance = resource.getInstances().get(0);
                if (Objects.equals(resource.getType(), "huaweicloud_networking_secgroup")) {
                    if (instance.attributes.containsKey("name")) {
                        packerVars.setSecGroupName(instance.attributes.get("name").toString());
                        packerVars.setSecGroupId(instance.attributes.get("id").toString());
                    }
                }
                if (Objects.equals(resource.getType(), "huaweicloud_vpc_subnet")) {
                    if (instance.attributes.containsKey("vpc_id")) {
                        packerVars.setVpcId(instance.attributes.get("vpc_id").toString());
                    }
                    if (instance.attributes.containsKey("id")) {
                        packerVars.setSubnetId(instance.attributes.get("id").toString());
                    }
                }
            }


        } catch (Exception ex) {
            throw new BuilderException(this, "Prepare Image environment failed.", ex);
        }

        return packerVars;
    }

    public void destroyEnv(TerraformExecutor tfExecutor) {
        tfExecutor.tfDestroy();
    }
}
