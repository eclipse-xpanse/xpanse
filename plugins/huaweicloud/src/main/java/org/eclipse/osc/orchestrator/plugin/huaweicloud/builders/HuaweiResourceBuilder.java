/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.osc.modules.ocl.loader.data.models.Artifact;
import org.eclipse.osc.modules.ocl.loader.data.models.Ocl;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.AtomBuilder;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.BuilderContext;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.terraform.OclTerraformExecutor;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.exceptions.BuilderException;

/**
 * Class to build all resources on Huawei cloud.
 */
@Slf4j
public class HuaweiResourceBuilder extends AtomBuilder {

    public HuaweiResourceBuilder(Ocl ocl) {
        super(ocl);
    }

    @Override
    public String name() {
        return "Huawei-Cloud-resource-Builder";
    }

    @Override
    public boolean create(BuilderContext ctx) {
        log.info("Creating Huawei Cloud resources.");
        if (ctx == null) {
            log.error("BuilderContext is invalid.");
            throw new BuilderException(this, "Builder context is null.");
        }
        Map<String, String> imageCtx = ctx.get(new HuaweiImageBuilder(ocl).name());
        Map<String, String> envCtx = ctx.get(new HuaweiEnvBuilder(ocl).name());
        if (envCtx == null) {
            log.error("Dependent builder: {} must build first.", new HuaweiEnvBuilder(ocl).name());
            throw new BuilderException(this, "HuaweiEnvBuilder context is null.");
        }
        if (imageCtx == null) {
            log.error("Dependent builder: {} must build first.",
                    new HuaweiImageBuilder(ocl).name());
            throw new BuilderException(this, "HuaweiImageBuilder context is null.");
        }

        for (Artifact artifact : ocl.getImage().getArtifacts()) {
            if (imageCtx.containsKey(artifact.getName())) {
                artifact.setId(imageCtx.get(artifact.getName()));
            }
        }

        OclTerraformExecutor tfExecutor = new OclTerraformExecutor(ocl, envCtx);

        tfExecutor.createWorkspace();
        tfExecutor.createTerraformScript();

        if (!tfExecutor.tfInit()) {
            throw new BuilderException(this, "TFExecutor.tfInit failed." + name());
        }
        if (!tfExecutor.tfPlan()) {
            throw new BuilderException(this, "TFExecutor.tfPlan failed." + name());
        }
        if (!tfExecutor.tfApply()) {
            throw new BuilderException(this, "TFExecutor.tfApply failed." + name());
        }

        tfExecutor.updateOclResources(ctx.getOclResources());

        return true;
    }

    @Override
    public boolean destroy(BuilderContext ctx) {
        log.info("Destroying Huawei Cloud resources.");
        OclTerraformExecutor tfExecutor = new OclTerraformExecutor(ocl,
                ctx.get(new HuaweiEnvBuilder(ocl).name()));

        tfExecutor.createWorkspace();
        tfExecutor.createTerraformScript();

        if (!tfExecutor.tfInit()) {
            log.error("ResourceBuilder Init failed {}.", name());
            throw new BuilderException(this, "TFExecutor.tfInit failed " + name());
        }
        if (!tfExecutor.tfPlan()) {
            throw new BuilderException(this, "TFExecutor.tfPlan failed." + name());
        }
        if (!tfExecutor.tfDestroy()) {
            log.error("ResourceBuilder destroy failed {}.", name());
            return false;
        }

        return true;
    }

}
