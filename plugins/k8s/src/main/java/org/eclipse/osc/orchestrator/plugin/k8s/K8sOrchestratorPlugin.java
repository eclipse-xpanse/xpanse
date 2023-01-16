package org.eclipse.osc.orchestrator.plugin.k8s;

import lombok.extern.slf4j.Slf4j;
import org.apache.karaf.minho.boot.service.ServiceRegistry;
import org.apache.karaf.minho.boot.spi.Service;
import org.eclipse.osc.orchestrator.OrchestratorPlugin;
import org.eclipse.osc.modules.ocl.loader.Ocl;

@Slf4j
public class K8sOrchestratorPlugin implements Service, OrchestratorPlugin {

    @Override
    public String name() {
        return "osc-k8s-plugin";
    }

    @Override
    public void onRegister(ServiceRegistry serviceRegistry) {
        log.info("Starting the OSC K8S Orchestrator plugin");
    }

    @Override
    public void registerManagedService(Ocl ocl) {

    }

    @Override
    public void updateManagedService(String managedServiceName, Ocl ocl) {

    }

    @Override
    public void startManagedService(String managedServiceName) {

    }

    @Override
    public void stopManagedService(String managedServiceName) {

    }

    @Override
    public void unregisterManagedService(String managedServiceName) {

    }
}
