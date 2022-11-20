package org.eclipse.osc.orchestrator.plugin.huaweicloud;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

import org.apache.karaf.minho.boot.service.ServiceRegistry;
import org.apache.karaf.minho.boot.spi.Service;
import org.eclipse.osc.orchestrator.OrchestratorPlugin;
import org.eclipse.osc.services.ocl.loader.Ocl;

@Slf4j
public class HuaweiCloudOrchestratorPlugin implements OrchestratorPlugin, Service {
    private final Map<String, Ocl> managedOcl = new HashMap<>();

    @Override
    public String name() {
        return "osc-orchestrator-huaweicloud";
    }

    @Override
    public void onRegister(ServiceRegistry serviceRegistry) {
        log.info("Registering Huawei Cloud Orchestrator ...");
        // nothing to do on registration
    }

    @Override
    public void registerManagedService(Ocl ocl) {
        managedOcl.put(ocl.getName(), ocl);
        log.info("Register managed service, creating  Huawei Cloud resource");
    }

    @Override
    public void updateManagedService(String managedServiceName, Ocl ocl) {
        managedOcl.put(managedServiceName, ocl);
        log.info("Updating managed service {} on Huawei Cloud", managedServiceName);
    }

    @Override
    public void startManagedService(String managedServiceName) {
        if (!managedOcl.containsKey(managedServiceName)) {
            log.warn("Service: {} not registered.", managedServiceName);
            return;
        }
        BuilderFactory factory = new BuilderFactory();
        Optional<AtomBuilder> optionalAtomBuilder = factory.createBuilder("basic", managedOcl.get(managedServiceName));
        BuilderContext ctx = new BuilderContext();

        if (optionalAtomBuilder.isEmpty()) {
            log.warn("Builder not found.");
            return;
        }
        optionalAtomBuilder.get().build(ctx);

        log.info("Start managed service {} on Huawei Cloud", managedServiceName);
    }

    @Override
    public void stopManagedService(String managedServiceName) {
        if (!managedOcl.containsKey(managedServiceName)) {
            log.warn("Service: {} not registered.", managedServiceName);
            return;
        }
        log.info("Stop managed service {} on Huawei Cloud", managedServiceName);
    }

    @Override
    public void unregisterManagedService(String managedServiceName) {
        if (!managedOcl.containsKey(managedServiceName)) {
            log.warn("Service: {} not registered.", managedServiceName);
            return;
        }
        log.info("Destroy managed service {} from Huawei Cloud", managedServiceName);
    }
}
