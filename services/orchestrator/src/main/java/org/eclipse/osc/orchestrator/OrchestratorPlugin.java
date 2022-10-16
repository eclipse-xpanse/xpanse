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
     */
    void registerManagedService(Ocl ocl);

    /**
     * Start (exposing the managed service to the users) the managed service.
     *
     * @param managedServiceName the service ID to start.
     */
    void startManagedService(String managedServiceName);

    /**
     * Stop (hidding the managed service for the users) the managed service.
     *
     * @param managedServiceName the service ID to stop.
     */
    void stopManagedService(String managedServiceName);

    /**
     * Unregister and destroy/clean managed service resources.
     *
     * @param managedServiceName the service ID to unregister and destroy.
     */
    void unregisterManagedService(String managedServiceName);

}
