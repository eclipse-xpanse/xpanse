/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator;

import org.eclipse.osc.modules.ocl.loader.data.models.Ocl;

/**
 * This interface describes orchestrator plugin in charge of interacting with
 * backend fundamental APIs.
 */
public interface OrchestratorPlugin {

    /**
     * Register a managed service using the provided OCL descriptor.
     *
     * @param ocl the OCL model describing the managed service.
     */
    void registerManagedService(Ocl ocl);

    /**
     * Update an existing managed service using the provided OCL descriptor.
     *
     * @param managedServiceName the managed service to update, identified by the given name.
     * @param ocl                the OCL descriptor to update the managed service.
     */
    void updateManagedService(String managedServiceName, Ocl ocl);

    /**
     * Start (exposing the managed service to the users) the managed service.
     *
     * @param managedServiceName the service ID to start.
     */
    void startManagedService(String managedServiceName);

    /**
     * Stop (hiding the managed service for the users) the managed service.
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
