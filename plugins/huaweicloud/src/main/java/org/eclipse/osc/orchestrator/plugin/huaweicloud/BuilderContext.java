package org.eclipse.osc.orchestrator.plugin.huaweicloud;

import lombok.Data;
import org.eclipse.osc.modules.ocl.loader.data.models.OclResources;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

@Data
public class BuilderContext extends HashMap<String, Map<String, String>> {

    private Environment environment;

    private OclResources oclResources = new OclResources();
}
