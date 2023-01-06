package org.eclipse.osc.orchestrator;

import lombok.extern.slf4j.Slf4j;
import org.apache.karaf.minho.boot.service.ConfigService;
import org.apache.karaf.minho.boot.service.ServiceRegistry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

@Slf4j
public class FileOrchestratorStorage implements OrchestratorStorage {

    public static final String DEFAULT_FILENAME = "orchestrator.properties";

    private Properties properties = new Properties();
    private File file = new File(DEFAULT_FILENAME);

    public FileOrchestratorStorage(ServiceRegistry serviceRegistry) throws IOException {
        ConfigService configService = serviceRegistry.get(ConfigService.class);
        file = new File(configService.getProperty("orchestrator.store.filename", DEFAULT_FILENAME));
        if (file.exists()) {
            try (var stream = new FileInputStream(file)) {
                properties.load(stream);
            }
        }
    }

    @Override
    public synchronized void store(String sid) {
        properties.put(sid, sid);
        save();
    }

    @Override
    public void store(String sid, String pluginName, String key, String value) {
        String propertyKey = sid + "__" + pluginName + "__" + key;
        properties.put(propertyKey, value);
        save();
    }

    @Override
    public String getKey(String sid, String pluginName, String key) {
        String propertyKey = sid + "__" + pluginName + "__" + key;
        return properties.getProperty(propertyKey, "");
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
        try (var stream = new FileOutputStream(file)) {
            properties.store(stream, null);
        } catch (IOException ex) {
            log.warn("Can't save orchestrator state", ex);
        }
    }

}
