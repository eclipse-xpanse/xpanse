package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.karaf.minho.boot.service.ConfigService;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.AtomBuilder;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.BuilderContext;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.exceptions.BuilderException;
import org.eclipse.osc.services.ocl.loader.Ocl;

@Slf4j
public class HuaweiEnvBuilder extends AtomBuilder {

    public static final String ACCESS_KEY = "HW_ACCESS_KEY";
    public static final String SECRET_KEY = "HW_SECRET_KEY";
    public static final String REGION_NAME = "HW_REGION_NAME";

    public HuaweiEnvBuilder(Ocl ocl) {
        super(ocl);
    }

    @Override
    public String name() {
        return "Huawei-Cloud-env-Builder";
    }

    private boolean needRebuild(BuilderContext ctx) {
        return ctx.get(name()) == null;
    }

    private void prepareEnv(BuilderContext ctx) {
        if (ctx == null) {
            log.error("Builder Context is null.");
            throw new BuilderException(this, "Builder Context is null.");
        }

        if (!needRebuild(ctx)) {
            return;
        }

        ConfigService configCtx = ctx.getConfig();
        if (configCtx == null) {
            log.error("configCtx not found, in BuilderContext.");
            throw new BuilderException(this, "configCtx not found, in BuilderContext.");
        }

        String accessKey = configCtx.getProperty(ACCESS_KEY);
        String secretKey = configCtx.getProperty(SECRET_KEY);
        String region = configCtx.getProperty(REGION_NAME);

        Map<String, String> envCtx = new HashMap<>();
        if (accessKey != null) {
            envCtx.put(ACCESS_KEY, accessKey);
        }
        if (secretKey != null) {
            envCtx.put(SECRET_KEY, secretKey);
        }
        if (region != null) {
            envCtx.put(REGION_NAME, region);
        }

        ctx.put(name(), envCtx);
    }

    @Override
    public boolean create(BuilderContext ctx) {
        log.info("Prepare Huawei Cloud environment.");
        prepareEnv(ctx);
        return true;
    }

    @Override
    public boolean destroy(BuilderContext ctx) {
        log.info("Destroy Huawei Cloud environment.");
        prepareEnv(ctx);
        return true;
    }
}
