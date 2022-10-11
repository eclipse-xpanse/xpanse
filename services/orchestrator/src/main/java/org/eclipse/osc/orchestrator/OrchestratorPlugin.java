package org.eclipse.osc.orchestrator;

import org.eclipse.osc.services.ocl.loader.Ocl;

/**
 * This interface describes orchestrator plugin in charge of interacting with backend fundamental APIs.
 */
public interface OrchestratorPlugin {

    /**
     * Register a managed service using the provided OCL descriptor.
     *
     * @param ocl the OCL model describing the managed service.
     * @return the service ID on the CSP infrastructure.
     */
    String registerManagedService(Ocl ocl);

    /**
     * Start (exposing the managed service to the users) the managed service.
     *
     * @param sid the service ID to start.
     */
    void startManagedService(String sid);

    /**
     * Stop (hidding the managed service for the users) the managed service.
     *
     * @param sid the service ID to stop.
     */
    void stopManagedService(String sid);

    /**
     * Unregister and destroy/clean managed service resources.
     *
     * @param sid the service ID to unregister and destroy.
     */
    void unregisterManagedService(String sid);

}
