package org.eclipse.osc.orchestrator;

import lombok.extern.java.Log;
import org.apache.karaf.boot.spi.Service;
import org.eclipse.osc.services.ocl.loader.Ocl;

@Log
public class PluginTest implements OrchestratorPlugin, Service {

    @Override
    public String name() {
        return "osc-orchestrator-plugin-test";
    }

    @Override
    public String registerManagedService(Ocl ocl) {
        log.info("OSC Test Plugin :: Registering managed service");
        return "1";
    }

    @Override
    public void startManagedService(String sid) {
        log.info("OSC Test Plugin :: Starting managed service " + sid);
    }

    @Override
    public void stopManagedService(String sid) {
        log.info("OSC Test Plugin ::Stopping managed service " + sid);
    }

    @Override
    public void unregisterManagedService(String sid) {
        log.info("OSC Test Plugin :: Unregistering managed service " + sid);
    }

}
