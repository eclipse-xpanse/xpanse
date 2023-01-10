package org.eclipse.osc.orchestrator.plugin.huaweicloud;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.apache.karaf.minho.boot.service.ConfigService;
import org.eclipse.osc.modules.ocl.loader.OclResources;

@Data
public class BuilderContext extends HashMap<String, Map<String, String>> {

    private ConfigService config;

    private OclResources oclResources = new OclResources();
}
