package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.terraform;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.osc.modules.ocl.loader.Ocl;
import org.eclipse.osc.modules.ocl.loader.OclResources;
import org.eclipse.osc.modules.ocl.loader.RuntimeBase;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.exceptions.TFExecutorException;

@Slf4j
public class OclTFExecutor extends TFExecutor {

    private final Ocl ocl;

    public OclTFExecutor(final Ocl ocl, Map<String, String> env) {
        super(env);
        this.ocl = ocl;
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void createWorkspace() {
        super.createWorkspace(ocl.getName());
    }

    public void createTFScript() {
        Ocl2Hcl hcl = new Ocl2Hcl(ocl);
        String hclStr = hcl.getHcl();

        super.createTFScript(hclStr);
    }

    private void updateOclObject(
        RuntimeBase runtimeObj, String resourceType, String resourceName, TFState tfState) {
        for (var resource : tfState.getResources()) {
            if (resource.getInstances().size() < 1 || !resource.getType().equals(resourceType)) {
                continue;
            }

            TFStateResourceInstance instance = resource.getInstances().get(0);
            if (resource.getName().equals(resourceName)
                || (instance.attributes.containsKey("name")
                && instance.attributes.get("name").equals(resourceName))) {
                runtimeObj.setId(instance.attributes.get("id").toString());
                runtimeObj.setState("active");
            }
        }
    }

    private void updateOclRuntime() {
        TFState tfState;
        try {
            tfState = objectMapper.readValue(getTFState(), TFState.class);
        } catch (IOException ex) {
            log.error("Parse terraform state content failed.");
            throw new TFExecutorException("Parse terraform state content failed.", ex);
        }
        for (var secGroup : ocl.getNetwork().getSecurity()) {
            updateOclObject(
                secGroup, "huaweicloud_networking_secgroup", secGroup.getName(), tfState);
        }

        for (var subnet : ocl.getNetwork().getSubnet()) {
            updateOclObject(subnet, "huaweicloud_vpc_subnet", subnet.getName(), tfState);
        }

        for (var vm : ocl.getCompute().getVm()) {
            updateOclObject(vm, "huaweicloud_compute_instance", vm.getName(), tfState);
        }

        for (var vpc : ocl.getNetwork().getVpc()) {
            updateOclObject(vpc, "huaweicloud_vpc", vpc.getName(), tfState);
        }
    }

    public void updateOclResources(OclResources oclResources) {
        TFState tfState;
        try {
            tfState = objectMapper.readValue(getTFState(), TFState.class);
        } catch (IOException ex) {
            log.error("Parse terraform state content failed.");
            throw new TFExecutorException("Parse terraform state content failed.", ex);
        }

        TFResources tfResources = new TFResources();
        tfResources.update(tfState);

        oclResources.getResources().addAll(tfResources.getResources());
    }
}
