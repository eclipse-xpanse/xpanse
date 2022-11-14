package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders;

import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

import org.eclipse.osc.orchestrator.plugin.huaweicloud.AtomBuilder;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.BuilderContext;
import org.eclipse.osc.services.ocl.loader.Ocl;

@Slf4j
public class HuaweiEnvBuilder extends AtomBuilder {
    @Override
    public String name() {
        return "Huawei-Cloud-env-Builder";
    }

    public HuaweiEnvBuilder(Ocl ocl) {
        super(ocl);
        setTimeout(TimeUnit.MICROSECONDS.toSeconds(100));
    }

    public boolean create(BuilderContext ctx) {
        log.info("Prepare Huawei Cloud environment.");
        return true;
    }

    public boolean destroy(BuilderContext ctx) {
        log.info("Destroy Huawei Cloud environment.");
        return true;
    }
}
