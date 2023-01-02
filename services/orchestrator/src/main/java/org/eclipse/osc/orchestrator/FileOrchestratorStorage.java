package org.eclipse.osc.orchestrator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.karaf.minho.boot.service.ConfigService;

@Slf4j
public class FileOrchestratorStorage implements OrchestratorStorage {

    public static final String DEFAULT_FILENAME = "orchestrator.properties";

    private final Properties properties = new Properties();
    private File file = new File(DEFAULT_FILENAME);

    public FileOrchestratorStorage(ConfigService configService) {
        file = new File(configService.getProperty("orchestrator.store.filename", DEFAULT_FILENAME));
        if (file.exists()) {
            try {
                try (var stream = new FileInputStream(file)) {
                    properties.load(stream);
                }
            } catch (IOException ex) {
                throw new IllegalStateException("File storage load failed.");
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
