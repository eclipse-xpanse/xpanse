package org.eclipse.osc.orchestrator.plugin.openstack;

import lombok.extern.java.Log;
import org.apache.karaf.minho.boot.service.ServiceRegistry;
import org.apache.karaf.minho.boot.spi.Service;
import org.eclipse.osc.orchestrator.OrchestratorPlugin;
import org.eclipse.osc.services.ocl.loader.Ocl;

@Log
public class OpenstackOrchestratorPlugin implements OrchestratorPlugin, Service {

    @Override
    public String name() {
        return "osc-orchestrator-openstack";
    }

    @Override
    public void onRegister(ServiceRegistry serviceRegistry) {
        // TODO implement
    }

    @Override
    public void registerManagedService(Ocl ocl) {
        log.info("Register managed service, creating openstack resource");
    }

    @Override
    public void updateManagedService(String managedServiceName, Ocl ocl) {
        log.info("Updating managed service " + managedServiceName + " on openstack");
    }

    @Override
    public void startManagedService(String managedServiceName) {
        log.info("Start managed service " + managedServiceName + " on openstack");
    }

    @Override
    public void stopManagedService(String managedServiceName) {
        log.info("Stop managed service " + managedServiceName + " on openstack");
    }

    @Override
    public void unregisterManagedService(String managedServiceName) {
        log.info("Destroy managed service " + managedServiceName + " from openstack");
    }
}
