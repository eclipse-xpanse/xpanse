package org.eclipse.osc.orchestrator;

import lombok.extern.java.Log;
import org.apache.karaf.minho.boot.service.ConfigService;
import org.apache.karaf.minho.boot.service.ServiceRegistry;
import org.apache.karaf.minho.boot.spi.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

@Log
public class FileOrchestratorStorage implements OrchestratorStorage, Service {

    public final static String DEFAULT_FILENAME = "orchestrator.properties";

    private Properties properties = new Properties();
    private File file;

    @Override
    public String name() {
        return "osc-orchestrator-file-storage";
    }

    @Override
    public void onRegister(ServiceRegistry serviceRegistry) throws IOException {
        file = new File(DEFAULT_FILENAME);
        ConfigService configService = serviceRegistry.get(ConfigService.class);
        if (configService != null
                && configService.properties() != null
                && configService.properties().get("orchestrator.storage.filename") != null) {
            file = new File(configService.properties().get("orchestrator.storage.filename").toString());
        }
        if (file.exists()) {
            properties.load(new FileInputStream(file));
        }
    }

    @Override
    public synchronized void store(String sid) {
        properties.put(sid, sid);
        save();
    }

    @Override
    public boolean exists(String sid) {
        return properties.containsKey(sid);
    }

    @Override
    public Set<String> services() {
        return properties.stringPropertyNames();
    }

    @Override
    public synchronized void remove(String sid) {
        properties.remove(sid);
        save();
    }

    private void save() {
        try {
            properties.save(new FileOutputStream(file), null);
        } catch (Exception e) {
            log.severe("Can't save orchestrator state: " + e.getMessage());
        }
    }

}
