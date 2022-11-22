package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.karaf.minho.boot.service.ConfigService;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.AtomBuilder;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.BuilderContext;
import org.eclipse.osc.services.ocl.loader.Ocl;

@Slf4j
public class HuaweiEnvBuilder extends AtomBuilder {

    public HuaweiEnvBuilder(Ocl ocl) {
        super(ocl);
        setTimeout(TimeUnit.MICROSECONDS.toSeconds(100));
    }

    @Override
    public String name() {
        return "Huawei-Cloud-env-Builder";
    }

    @Override
    public boolean create(BuilderContext ctx) {
        log.info("Prepare Huawei Cloud environment.");
        if (ctx == null) {
            log.error("Builder Context is null.");
            return false;
        }

        ConfigService configCtx = ctx.getConfig();
        if (configCtx == null) {
            log.error("configCtx not found, in BuilderContext.");
            return false;
        }

        String accessKey = configCtx.getProperty("HW_ACCESS_KEY");
        String secretKey = configCtx.getProperty("HW_SECRET_KEY");
        String region = configCtx.getProperty("HW_REGION_NAME");

        Map<String, String> envCtx = new HashMap<>();
        envCtx.put("HW_ACCESS_KEY", accessKey);
        envCtx.put("HW_SECRET_KEY", secretKey);
        envCtx.put("HW_REGION_NAME", region);

        ctx.put(name(), envCtx);

        return true;
    }

    @Override
    public boolean destroy(BuilderContext ctx) {
        log.info("Destroy Huawei Cloud environment.");
        return true;
    }
}
