package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.AtomBuilder;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.BuilderContext;
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
            log.error("Dependent builder: {} must build first.", "Huawei-Cloud-env-Builder");
            return false;
        }

        return true;
    }

    @Override
    public boolean destroy(BuilderContext ctx) {
        log.info("Destroying Huawei Cloud resources.");
        return true;
    }
}
