package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.terraform;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.osc.modules.ocl.loader.Ocl;

@Slf4j
public class OclTFExecutor extends TFExecutor {

    private final Ocl ocl;

    public OclTFExecutor(final Ocl ocl, Map<String, String> env) {
        super(env);
        this.ocl = ocl;
    }

    public void createWorkspace() {
        super.createWorkspace(ocl.getName());
    }

    public void createTFScript() {
        Ocl2Hcl hcl = new Ocl2Hcl(ocl);
        String hclStr = hcl.getHcl();

        super.createTFScript(hclStr);
    }
}
