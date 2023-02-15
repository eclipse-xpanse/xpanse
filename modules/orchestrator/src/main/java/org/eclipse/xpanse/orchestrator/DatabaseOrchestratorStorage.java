/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.ServiceStatusEntity;
import org.eclipse.xpanse.modules.database.ServiceStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Bean to manage all requests to database.
 */
@Component
@Slf4j
public class DatabaseOrchestratorStorage implements OrchestratorStorage {

    ServiceStatusRepository serviceStatusRepository;

    @Autowired
    public DatabaseOrchestratorStorage(ServiceStatusRepository serviceStatusRepository) {
        this.serviceStatusRepository = serviceStatusRepository;
    }

    public void store(ServiceStatusEntity serviceStatusEntity) {
        serviceStatusRepository.save(serviceStatusEntity);
    }

    /**
     * Method to check if a service exists in the store already.
     *
     * @param managedServiceName Name of the managed service.
     * @return true if a service with given name exists in the database.
     */
    public boolean isExists(String managedServiceName) {
        List<ServiceStatusEntity> serviceStatusEntities = services();
        for (ServiceStatusEntity serviceStatusEntity : serviceStatusEntities) {
            if (serviceStatusEntity.getServiceName().equals(managedServiceName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method to get database entry based on service name and plugin.
     *
     * @param managedServiceName Name of the managed service.
     * @param orchestratorPlugin Name of the plugin used to deploy the managed service.
     * @return Returns the database entry for the provided arguments.
     */
    public ServiceStatusEntity getServiceDetailsByNameAndPlugin(
            String managedServiceName, OrchestratorPlugin orchestratorPlugin) {
        List<ServiceStatusEntity> serviceStatusEntities = services();
        for (ServiceStatusEntity serviceStatusEntity : serviceStatusEntities) {
            if (serviceStatusEntity.getServiceName().equals(managedServiceName)
                    && serviceStatusEntity.getPluginName()
                            .equals(orchestratorPlugin.getClass().getSimpleName())) {
                return serviceStatusEntity;
            }
        }
        throw new RuntimeException("No service found in database with name " + managedServiceName
               + " for plugin " + orchestratorPlugin.getClass().getSimpleName());

    }

    /**
     * Method to get database entry based on the service name.
     *
     * @param managedServiceName Name of the managed service.
     * @return Returns the database entry for the provided arguments.
     */
    public ServiceStatusEntity getServiceDetailsByName(String managedServiceName) {
        List<ServiceStatusEntity> serviceStatusEntities = services();
        for (ServiceStatusEntity serviceStatusEntity : serviceStatusEntities) {
            if (serviceStatusEntity.getServiceName().equals(managedServiceName)) {
                return serviceStatusEntity;
            }
        }
        throw new RuntimeException("No service found in database with name " + managedServiceName);

    }

    /**
     * Method to get all stored database entries.
     *
     * @return Returns all rows from the service status database table.
     */
    public List<ServiceStatusEntity> services() {
        return serviceStatusRepository.findAll();
    }

    public void remove(String managedServiceName, OrchestratorPlugin orchestratorPlugin) {
        serviceStatusRepository.deleteById(
                getServiceDetailsByNameAndPlugin(managedServiceName, orchestratorPlugin).getId());
    }

    /**
     * Method to get database entry based on service name and plugin.
     *
     * @param managedServiceName Name of the managed service.
     * @param orchestratorPlugin Name of the plugin used to deploy the managed service.
     * @return Returns the database entry for the provided arguments.
     */
    public boolean isManagedServiceByNameAndPluginExists(
            String managedServiceName, OrchestratorPlugin orchestratorPlugin) {
        List<ServiceStatusEntity> serviceStatusEntities = services();
        for (ServiceStatusEntity serviceStatusEntity : serviceStatusEntities) {
            if (serviceStatusEntity.getServiceName().equals(managedServiceName)
                    && serviceStatusEntity.getPluginName()
                    .equals(orchestratorPlugin.getClass().getSimpleName())) {
                return true;
            }
        }
        return false;

    }
}
