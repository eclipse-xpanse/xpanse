package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders;

import lombok.extern.slf4j.Slf4j;

import org.eclipse.osc.orchestrator.plugin.huaweicloud.AtomBuilder;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.BuilderContext;
import org.eclipse.osc.services.ocl.loader.Ocl;

@Slf4j
public class HuaweiImageBuilder extends AtomBuilder {
    @Override
    public String name() {
        return "Huawei-Cloud-image-Builder";
    }

    public HuaweiImageBuilder(Ocl ocl) {
        super(ocl);
    }

    public boolean create(BuilderContext ctx) {
        log.info("Creating Huawei Cloud Image.");
        return true;
    }

    public boolean destroy(BuilderContext ctx) {
        log.info("Destroying Huawei Cloud Image.");
        return true;
    }
}
