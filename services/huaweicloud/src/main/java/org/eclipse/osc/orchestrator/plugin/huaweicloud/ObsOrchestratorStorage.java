package org.eclipse.osc.orchestrator.plugin.huaweicloud;

import java.util.Set;
import org.apache.karaf.minho.boot.service.ServiceRegistry;
import org.apache.karaf.minho.boot.spi.Service;
import org.eclipse.osc.orchestrator.OrchestratorStorage;

public class ObsOrchestratorStorage implements OrchestratorStorage, Service {

    @Override
    public String name() {
        return "osc-huaweicloud-obs-orchestrator-storage";
    }

    @Override
    public void onRegister(ServiceRegistry serviceRegistry) {
        // TODO init obs storage here
    }

    @Override
    public void store(String sid) {

    }

    @Override
    public void store(String sid, String pluginName, String key, String value) {

    }

    @Override
    public String getKey(String sid, String pluginName, String key) {
        return "";
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
