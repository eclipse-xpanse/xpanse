/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator;

import java.util.List;
import org.eclipse.xpanse.modules.database.ServiceStatusEntity;

/**
 * Interface to be implemented by all runtime storage providers.
 */
public interface OrchestratorStorage {

    /**
     * Add or upate managed service data to database.
     *
     * @param serviceStatusEntity the managed service id.
     */
    void store(ServiceStatusEntity serviceStatusEntity);

    /**
     * Add a plugin level key-value pair in the store.
     *
     * @param managedServiceName name of the managed service stored in DB.
     */
    boolean isExists(String managedServiceName);

    boolean isManagedServiceByNameAndPluginExists(
            String managedServiceName, OrchestratorPlugin orchestratorPlugin);

    /**
     * Method to get database entry based on service name and plugin.
     *
     * @param managedServiceName Name of the managed service.
     * @param orchestratorPlugin Name of the plugin used to deploy the managed service.
     * @return Returns the database entry for the provided arguments.
     */
    ServiceStatusEntity getServiceDetailsByNameAndPlugin(
            String managedServiceName, OrchestratorPlugin orchestratorPlugin);

    /**
     * Method to get database entry based on the service name.
     *
     * @param managedServiceName Name of the managed service.
     * @return Returns the database entry for the provided arguments.
     */
    ServiceStatusEntity getServiceDetailsByName(String managedServiceName);

    /**
     * Method to get all stored database entries.
     *
     * @return Returns all rows from the service status database table.
     */
    List<ServiceStatusEntity> services();

    void remove(String managedServiceName, OrchestratorPlugin orchestratorPlugin);

}
