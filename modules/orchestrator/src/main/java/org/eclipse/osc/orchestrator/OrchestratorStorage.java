/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator;

import java.util.Set;

/**
 * Interface to be implemented by all runtime storage providers.
 */
public interface OrchestratorStorage {

    /**
     * Add a managed service id in the store.
     *
     * @param sid the managed service id.
     */
    void store(String sid);

    /**
     * Add a plugin level key-value pair in the store.
     *
     * @param pluginName the name of the OrchestratorPlugin.
     * @param sid        the managed service id.
     * @param key        the property key to store
     * @param value      the property value to store
     */
    void store(String sid, String pluginName, String key, String value);

    /**
     * Add a plugin level key-value pair in the store.
     *
     * @param pluginName the name of the OrchestratorPlugin.
     * @param sid        the managed service id.
     * @param key        the property key to store
     */
    String getKey(String sid, String pluginName, String key);

    /**
     * Check if a managed service id is present in the store.
     *
     * @param sid the managed service id.
     * @return true if the service is present in the store, false else.
     */
    boolean exists(String sid);

    /**
     * Get the list of managed service id from the store.
     *
     * @return the list of managed service id.
     */
    Set<String> services();

    /**
     * Remove a managed service id from the store.
     *
     * @param sid the managed service id.
     */
    void remove(String sid);

}
