/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.openstack;

import java.io.InputStream;
import java.util.Set;
import org.eclipse.osc.orchestrator.OrchestratorStorage;
import org.openstack4j.model.common.Payloads;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

/**
 * Storage implementation on Openstack cloud using Swift API.
 */
// @Component
@Profile(value = "openstack")
public class SwiftOrchestratorStorage implements OrchestratorStorage {

    private final KeystoneManager keystoneManager;
    private final Environment environment;

    /**
     * Initiates the Storage bean.
     *
     * @param keystoneManager KeystoneManager bean.
     * @param environment     Environment bean.
     */
    @Autowired
    public SwiftOrchestratorStorage(KeystoneManager keystoneManager, Environment environment) {
        this.keystoneManager = keystoneManager;
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
        String objectName = this.environment.getProperty("orchestrator.store.filename",
                "orchestrator.properties");
        this.keystoneManager.getClient().objectStorage().containers().create("osc");
        this.keystoneManager.getClient().objectStorage().objects()
                .put(containerName, objectName, Payloads.create(InputStream.nullInputStream()));
    }
}
