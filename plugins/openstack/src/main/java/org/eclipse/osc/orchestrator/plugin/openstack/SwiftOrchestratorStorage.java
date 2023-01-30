package org.eclipse.osc.orchestrator.plugin.openstack;

import org.eclipse.osc.orchestrator.OrchestratorStorage;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.common.Payloads;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Set;

@Component
@Profile(value = "openstack")
public class SwiftOrchestratorStorage implements OrchestratorStorage {

    private final OSClient.OSClientV3 osClient;
    private final Environment environment;

    @Autowired
    public SwiftOrchestratorStorage(KeystoneManager keystoneManager, Environment environment) {
        this.osClient = keystoneManager.getClient();
        this.environment = environment;
        createObjectStoreOnOpenstack();
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

    private void createObjectStoreOnOpenstack() {
        String containerName = this.environment.getProperty("orchestrator.store.container", "osc");
        String objectName = this.environment.getProperty("orchestrator.store.filename", "orchestrator.properties");
        this.osClient.objectStorage().containers().create("osc");
        this.osClient.objectStorage().objects().put(containerName, objectName, Payloads.create(InputStream.nullInputStream()));
    }
}
