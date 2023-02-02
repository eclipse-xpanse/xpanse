/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

/**
 * Default storage bean used by runtime when plugin has not provided its own storage bean
 * to store runtime information.
 */
@Slf4j
public class FileOrchestratorStorage implements OrchestratorStorage {

    private final Properties properties = new Properties();
    private final File file;

    /**
     * Initialize Storage bean.
     *
     * @param environment Environment bean from SpringContext.
     * @throws IOException Exception when external resource cannot be read.
     */
    public FileOrchestratorStorage(Environment environment) throws IOException {
        log.info("No other storage beans found. Using default file storage.");
        this.file = new File(
                Objects.requireNonNull(environment.getProperty("orchestrator.store.filename")));
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
