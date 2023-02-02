/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.huaweicloud;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.osc.orchestrator.OrchestratorStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * File system storage bean for huawei plugin.
 */
@Slf4j
@Component
@Profile("huaweicloud")
public class FileOrchestratorStorage implements OrchestratorStorage {

    public static final String DEFAULT_FILENAME = "orchestrator.properties";
    private final Properties properties = new Properties();
    private final File file;

    /**
     * Constructor to instantiate FileOrchestratorStorage bean.
     *
     * @param environment Environment bean autowired by spring.
     */
    @Autowired
    public FileOrchestratorStorage(Environment environment) {
        log.info("Using FileOrchestratorStorage from Huaweicloud plugin");
        file = new File(environment.getProperty("orchestrator.store.filename", DEFAULT_FILENAME));
        if (file.exists()) {
            try {
                try (var stream = new FileInputStream(file)) {
                    properties.load(stream);
                }
            } catch (IOException ex) {
                throw new IllegalStateException("Read file failed " + file, ex);
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
