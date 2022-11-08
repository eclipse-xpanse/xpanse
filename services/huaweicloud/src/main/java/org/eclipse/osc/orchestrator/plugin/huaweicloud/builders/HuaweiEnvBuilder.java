package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders;

import java.util.concurrent.TimeUnit;
import lombok.extern.java.Log;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.AtomBuilder;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.BuilderContext;
import org.eclipse.osc.services.ocl.loader.Ocl;

@Log
public class HuaweiEnvBuilder extends AtomBuilder {
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
