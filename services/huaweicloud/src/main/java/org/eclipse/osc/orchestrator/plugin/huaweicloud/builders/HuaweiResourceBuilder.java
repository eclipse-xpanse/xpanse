package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.AtomBuilder;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.BuilderContext;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.terraform.TFExecutor;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.exceptions.BuilderException;
import org.eclipse.osc.services.ocl.loader.Ocl;

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
            log.error("Dependent builder: {} must build first.", new HuaweiEnvBuilder(ocl).name());
            throw new BuilderException(this, "Builder context is null.");
        }
        TFExecutor tfExecutor = new TFExecutor(ocl, ctx.get(new HuaweiEnvBuilder(ocl).name()));

        tfExecutor.createWorkspace();
        tfExecutor.createTFScript();

        if (!tfExecutor.tfInit()) {
            throw new BuilderException(this, "TFExecutor.tfInit failed." + name());
        }
        if (!tfExecutor.tfPlan()) {
            throw new BuilderException(this, "TFExecutor.tfPlan failed." + name());
        }
        if (!tfExecutor.tfApply()) {
            throw new BuilderException(this, "TFExecutor.tfApply failed." + name());
        }
        return true;
    }

    @Override
    public boolean destroy(BuilderContext ctx) {
        log.info("Destroying Huawei Cloud resources.");
        TFExecutor tfExecutor = new TFExecutor(ocl, ctx.get(new HuaweiEnvBuilder(ocl).name()));

        tfExecutor.createWorkspace();
        tfExecutor.createTFScript();

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
