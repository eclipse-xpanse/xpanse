package org.eclipse.osc.orchestrator;

import lombok.extern.java.Log;
import org.apache.karaf.minho.boot.spi.Service;
import org.eclipse.osc.modules.ocl.loader.Ocl;

@Log
public class PluginTest implements OrchestratorPlugin, Service {

    private Ocl ocl;

    @Override
    public String name() {
        return "osc-orchestrator-plugin-test";
    }

    @Override
    public void registerManagedService(Ocl ocl) {
        log.info("OSC Test Plugin :: Registering managed service");
        this.ocl = ocl;
    }

    public void updateManagedService(String managedServiceName, Ocl ocl) {
        log.info("OSC Test Plugin :: Updating managed service " + managedServiceName);
    }

    @Override
    public void startManagedService(String managedServiceName) {
        log.info("OSC Test Plugin :: Starting managed service " + managedServiceName);
    }

    @Override
    public void stopManagedService(String managedServiceName) {
        log.info("OSC Test Plugin ::Stopping managed service " + managedServiceName);
    }

    @Override
    public void unregisterManagedService(String managedServiceName) {
        log.info("OSC Test Plugin :: Unregistering managed service " + managedServiceName);
        this.ocl = null;
    }

    public Ocl getOcl() {
        return this.ocl;
    }

}
