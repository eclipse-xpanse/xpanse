package org.eclipse.osc.orchestrator.plugin.huaweicloud;

import lombok.extern.java.Log;
import org.apache.karaf.minho.boot.service.ServiceRegistry;
import org.apache.karaf.minho.boot.spi.Service;
import org.eclipse.osc.orchestrator.OrchestratorPlugin;
import org.eclipse.osc.services.ocl.loader.Ocl;

import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Level;

@Log
public class HuaweiCloudOrchestratorPlugin implements OrchestratorPlugin, Service {

    private final HashMap<String, Ocl> managedOcl = new HashMap<String, Ocl>();

    @Override
    public String name() {
        return "osc-orchestrator-huaweicloud";
    }

    @Override
    public void onRegister(ServiceRegistry serviceRegistry) {
        // TODO implement
    }

    @Override
    public void registerManagedService(Ocl ocl) {
        managedOcl.put(ocl.getName(), ocl);
        log.info("Register managed service, creating  Huawei Cloud resource");
    }

    @Override
    public void updateManagedService(String managedServiceName, Ocl ocl) {
        managedOcl.put(managedServiceName, ocl);
        log.info("Updating managed service " + managedServiceName + " on Huawei Cloud");
    }

    @Override
    public void startManagedService(String managedServiceName) {
        if (!managedOcl.containsKey(managedServiceName)) {
            log.log(Level.WARNING, "Service: " + managedServiceName +" not registered.");
            return;
        }
        BuilderFactory factory = new BuilderFactory();
        Optional<AtomBuilder> optionalAtomBuilder = factory.createBuilder("basic", managedOcl.get(managedServiceName));
        BuilderContext ctx = new BuilderContext();

        if (optionalAtomBuilder.isEmpty()) {
            log.log(Level.WARNING, "Builder not found.");
            return;
        }
        optionalAtomBuilder.get().build(ctx);

        log.info("Start managed service " + managedServiceName + " on Huawei Cloud");
    }

    @Override
    public void stopManagedService(String managedServiceName) {
        if (!managedOcl.containsKey(managedServiceName)) {
            log.log(Level.WARNING, "Service: " + managedServiceName +" not registered.");
            return;
        }
        log.info("Stop managed service " + managedServiceName + " on Huawei Cloud");
    }

    @Override
    public void unregisterManagedService(String managedServiceName) {
        if (!managedOcl.containsKey(managedServiceName)) {
            log.log(Level.WARNING, "Service: " + managedServiceName +" not registered.");
            return;
        }
        log.info("Destroy managed service " + managedServiceName + " from Huawei Cloud");
    }
}
