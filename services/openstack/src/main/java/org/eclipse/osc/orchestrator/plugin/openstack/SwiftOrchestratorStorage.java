package org.eclipse.osc.orchestrator.plugin.openstack;

import org.apache.karaf.minho.boot.service.ServiceRegistry;
import org.apache.karaf.minho.boot.spi.Service;
import org.eclipse.osc.orchestrator.OrchestratorStorage;

import java.util.Set;

public class SwiftOrchestratorStorage implements OrchestratorStorage, Service {

    @Override
    public String name() {
        return "osc-openstack-swift-orchestrator-storage";
    }

    @Override
    public void onRegister(ServiceRegistry serviceRegistry) {
        // TODO init swift storage here
    }

    @Override
    public void store(String sid) {

    }

    @Override
    public boolean exists(String sid) {
        return false;
    }

    @Override
    public Set<String> services() {
        return null;
    }

    @Override
    public void remove(String sid) {

    }
}
