package org.eclipse.osc.orchestrator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

@Slf4j
@Component
@ConditionalOnMissingBean(type = "OrchestratorStorage")
public class FileOrchestratorStorage implements OrchestratorStorage {

    private final Properties properties = new Properties();
    private final File file;
    @Autowired
    public FileOrchestratorStorage(Environment environment) throws IOException {
        log.info("No other storage beans found. Using default file storage.");
        this.file = new File(Objects.requireNonNull(environment.getProperty("orchestrator.store.filename")));
        if (file.exists()) {
            try (FileInputStream stream = new FileInputStream(file)) {
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
