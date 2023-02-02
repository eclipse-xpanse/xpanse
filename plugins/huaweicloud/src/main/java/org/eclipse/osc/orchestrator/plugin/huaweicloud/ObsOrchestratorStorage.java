/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.huaweicloud;

import java.util.Set;
import org.eclipse.osc.orchestrator.OrchestratorStorage;
import org.springframework.context.annotation.Profile;

/**
 * Storage bean for Huawei cloud plugin based on OBS storage service.
 */
@Profile(value = "huaweicloud")
public class ObsOrchestratorStorage implements OrchestratorStorage {

    public ObsOrchestratorStorage() {
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
