package org.eclipse.osc.orchestrator.plugin.openstack;

import org.apache.karaf.minho.boot.service.ConfigService;
import org.apache.karaf.minho.boot.service.ServiceRegistry;
import org.apache.karaf.minho.boot.spi.Service;
import org.eclipse.osc.orchestrator.OrchestratorStorage;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.common.Payloads;

import java.io.InputStream;
import java.util.Set;

public class SwiftOrchestratorStorage implements OrchestratorStorage, Service {

    private OSClient.OSClientV3 osClient;
    @Override
    public String name() {
        return "osc-openstack-swift-orchestrator-storage";
    }

    @Override
    public void onRegister(ServiceRegistry serviceRegistry) {
        this.osClient = KeystoneManager.getClient(serviceRegistry);
        createObjectStoreOnOpenstack(serviceRegistry);
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

    private void createObjectStoreOnOpenstack(ServiceRegistry serviceRegistry) {
        ConfigService configService = serviceRegistry.get(ConfigService.class);
        String containerName = configService.getProperty("orchestrator.store.container", "osc");
        String objectName = configService.getProperty("orchestrator.store.filename", "orchestrator.properties");
        this.osClient.objectStorage().containers().create("osc");
        this.osClient.objectStorage().objects().put(containerName, objectName, Payloads.create(InputStream.nullInputStream()));
    }
}
