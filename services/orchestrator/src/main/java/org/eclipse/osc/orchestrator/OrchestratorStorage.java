package org.eclipse.osc.orchestrator;

import java.util.Set;

public interface OrchestratorStorage {

    /**
     * Add a managed service id in the store.
     *
     * @param sid the managed service id.
     */
    void store(String sid);

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
