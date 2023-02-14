/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator;

import jakarta.persistence.EntityNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.ServiceStatusEntity;
import org.eclipse.xpanse.modules.ocl.loader.OclLoader;
import org.eclipse.xpanse.modules.ocl.loader.data.models.Ocl;
import org.eclipse.xpanse.modules.ocl.loader.data.models.ServiceStatus;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.ServiceState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Main class which orchestrates the OCL request processing. Calls the available plugins to deploy
 * managed service in the respective infrastructure as defined in the OCL.
 */
@Slf4j
@Component
public class OrchestratorService implements ApplicationListener<ApplicationEvent> {

    private final DatabaseOrchestratorStorage databaseOrchestratorStorage;
    private final OclLoader oclLoader;

    @Getter
    private final List<OrchestratorPlugin> plugins = new ArrayList<>();

    @Autowired
    public OrchestratorService(OclLoader oclLoader,
                               DatabaseOrchestratorStorage databaseOrchestratorStorage) {
        this.oclLoader = oclLoader;
        this.databaseOrchestratorStorage = databaseOrchestratorStorage;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            ApplicationContext applicationContext =
                    ((ContextRefreshedEvent) event).getApplicationContext();
            plugins.addAll(applicationContext.getBeansOfType(OrchestratorPlugin.class).values());
            if (plugins.size() > 1) {
                throw new RuntimeException("More than one xpanse plugin found. "
                        + "Only one plugin can be active at a time.");
            }
            if (plugins.isEmpty()) {
                log.warn("No xpanse plugins loaded by the runtime.");
            }
        }
    }

    /**
     * Register a managed service on all orchestrator plugins, using OCL descriptor location.
     *
     * @param oclLocation the location of the OCL descriptor.
     * @throws Exception if registration fails.
     */
    public void registerManagedService(String oclLocation) throws Exception {
        Ocl ocl = this.oclLoader.getOcl(new URL(oclLocation));
        registerManagedService(ocl);
    }

    /**
     * Register a managed service on all orchestrator plugins, directly using OCL descriptor.
     *
     * @param ocl the OCL descriptor.
     */
    public void registerManagedService(Ocl ocl) {
        if (plugins.isEmpty()) {
            log.warn("No plugins available. Request ignored.");
            return;
        }

        for (OrchestratorPlugin plugin : plugins) {
            try {
                plugin.registerManagedService(ocl);
                this.databaseOrchestratorStorage.store(getNewServiceStatusEntity(plugin, ocl));
            } catch (RuntimeException exception) {
                this.databaseOrchestratorStorage.store(
                        getFailedServiceStatusEntity(plugin, ocl, exception));
                throw exception;
            }
        }
    }

    /**
     * Update existing managed service with a new/updated OCL descriptor, at the given location.
     *
     * @param managedServiceName the managed service to update, identified by the given name.
     * @param oclLocation        the new/updated OCL descriptor location.
     * @throws Exception if the update fails.
     */
    public void updateManagedService(String managedServiceName, String oclLocation)
            throws Exception {
        Ocl ocl = oclLoader.getOcl(new URL(oclLocation));
        updateManagedService(managedServiceName, ocl);
    }

    /**
     * Update existing managed service with a new/updated OCL descriptor.
     *
     * @param managedServiceName the managed service to update, identified by the given name.
     * @param ocl                the new/update OCL descriptor.
     */
    public void updateManagedService(String managedServiceName, Ocl ocl) {
        if (!this.databaseOrchestratorStorage.isExists(managedServiceName)) {
            throw new EntityNotFoundException(
                    "Managed service " + managedServiceName + " not found");
        }
        for (OrchestratorPlugin orchestratorPlugin : plugins) {
            try {
                ServiceStatusEntity serviceStatusEntity =
                        this.databaseOrchestratorStorage.getServiceDetailsByNameAndPlugin(
                                managedServiceName,
                                orchestratorPlugin);
                serviceStatusEntity.setServiceState(ServiceState.UPDATING);
                this.databaseOrchestratorStorage.store(serviceStatusEntity);
                orchestratorPlugin.updateManagedService(managedServiceName, ocl);
                serviceStatusEntity.setServiceState(ServiceState.UPDATED);
                this.databaseOrchestratorStorage.store(serviceStatusEntity);
            } catch (RuntimeException exception) {
                this.databaseOrchestratorStorage.store(getFailedServiceStatusEntity(
                        orchestratorPlugin, ocl, exception));
                throw exception;
            }
        }
    }

    /**
     * Start (expose to users) a managed service on all orchestrator plugins.
     *
     * @param managedServiceName the managed service name.
     */
    public void startManagedService(String managedServiceName) {
        if (!this.databaseOrchestratorStorage.isExists(managedServiceName)) {
            throw new EntityNotFoundException(
                    "Managed service " + managedServiceName + " not found");
        }
        for (OrchestratorPlugin orchestratorPlugin : plugins) {
            ServiceStatusEntity serviceStatusEntity =
                    this.databaseOrchestratorStorage.getServiceDetailsByNameAndPlugin(
                            managedServiceName,
                            orchestratorPlugin);
            try {
                serviceStatusEntity.setServiceState(ServiceState.STARTING);
                this.databaseOrchestratorStorage.store(serviceStatusEntity);
                orchestratorPlugin.startManagedService(managedServiceName);
                serviceStatusEntity.setServiceState(ServiceState.STARTED);
                this.databaseOrchestratorStorage.store(serviceStatusEntity);

            } catch (RuntimeException exception) {
                this.databaseOrchestratorStorage.store(getFailedServiceStatusEntity(
                        orchestratorPlugin, serviceStatusEntity.getOcl(), exception));
                throw exception;
            }
        }
    }

    /**
     * Stop (managed service is not visible to users anymore) a managed service on all orchestrator
     * plugins.
     *
     * @param managedServiceName the managed service name.
     */
    public void stopManagedService(String managedServiceName) {
        if (!this.databaseOrchestratorStorage.isExists(managedServiceName)) {
            throw new EntityNotFoundException(
                    "Managed service " + managedServiceName + " not found");
        }
        for (OrchestratorPlugin orchestratorPlugin : plugins) {
            ServiceStatusEntity serviceStatusEntity =
                    this.databaseOrchestratorStorage.getServiceDetailsByNameAndPlugin(
                            managedServiceName,
                            orchestratorPlugin);
            try {
                orchestratorPlugin.stopManagedService(managedServiceName);
                serviceStatusEntity.setServiceState(ServiceState.STOPPED);
                this.databaseOrchestratorStorage.store(serviceStatusEntity);
            } catch (RuntimeException exception) {
                this.databaseOrchestratorStorage.store(getFailedServiceStatusEntity(
                        orchestratorPlugin, serviceStatusEntity.getOcl(), exception));
                throw exception;
            }

        }
    }

    /**
     * Unregister a managed service and destroy/clean all associated resources on all orchestrator
     * plugins.
     *
     * @param managedServiceName the managed service name.
     */
    public void unregisterManagedService(String managedServiceName) {
        if (!this.databaseOrchestratorStorage.isExists(managedServiceName)) {
            throw new EntityNotFoundException(
                    "Managed service " + managedServiceName + " not found");
        }
        for (OrchestratorPlugin orchestratorPlugin : plugins) {
            ServiceStatusEntity serviceStatusEntity =
                    this.databaseOrchestratorStorage.getServiceDetailsByNameAndPlugin(
                            managedServiceName,
                            orchestratorPlugin);
            try {
                serviceStatusEntity.setServiceState(ServiceState.DELETING);
                this.databaseOrchestratorStorage.store(serviceStatusEntity);
                orchestratorPlugin.unregisterManagedService(managedServiceName);
                this.databaseOrchestratorStorage.remove(managedServiceName, orchestratorPlugin);
            } catch (RuntimeException exception) {
                this.databaseOrchestratorStorage.store(getFailedServiceStatusEntity(
                        orchestratorPlugin, serviceStatusEntity.getOcl(), exception));
                throw exception;
            }

        }
    }

    /**
     * Get the runtime state of the managed service.
     *
     * @param managedServiceName the managed service name.
     */
    public ServiceStatus getManagedServiceState(String managedServiceName) {
        if (!this.databaseOrchestratorStorage.isExists(managedServiceName)) {
            throw new EntityNotFoundException(
                    "Managed service " + managedServiceName + " not found");
        }

        return getServiceStatusFromEntity(
                this.databaseOrchestratorStorage.getServiceDetailsByName(managedServiceName));
    }

    /**
     * Method to get all ServiceStatus objects stored in database.
     *
     * @return returns all ServiceStatus objects stored in database
     */
    public List<ServiceStatus> getStoredServices() {
        List<ServiceStatus> serviceStatuses = new ArrayList<>(Collections.emptyList());
        this.databaseOrchestratorStorage.services()
                .forEach(serviceStatusEntity -> serviceStatuses.add(
                        getServiceStatusFromEntity(serviceStatusEntity)));
        return serviceStatuses;
    }

    private ServiceStatusEntity getNewServiceStatusEntity(OrchestratorPlugin orchestratorPlugin,
                                                          Ocl ocl) {
        ServiceStatusEntity serviceStatusEntity = new ServiceStatusEntity();
        serviceStatusEntity.setServiceName(ocl.getName());
        serviceStatusEntity.setServiceState(ServiceState.REGISTERED);
        serviceStatusEntity.setOcl(ocl);
        serviceStatusEntity.setPluginName(orchestratorPlugin.getClass().getSimpleName());
        return serviceStatusEntity;
    }

    private ServiceStatusEntity getFailedServiceStatusEntity(OrchestratorPlugin orchestratorPlugin,
                                                             Ocl ocl, Exception exception) {
        ServiceStatusEntity serviceStatusEntity = new ServiceStatusEntity();
        serviceStatusEntity.setServiceName(ocl.getName());
        serviceStatusEntity.setServiceState(ServiceState.FAILED);
        serviceStatusEntity.setOcl(ocl);
        serviceStatusEntity.setPluginName(orchestratorPlugin.getClass().getSimpleName());
        serviceStatusEntity.setStatusMessage(exception.getMessage());
        return serviceStatusEntity;
    }

    private ServiceStatus getServiceStatusFromEntity(ServiceStatusEntity serviceStatusEntity) {
        ServiceStatus serviceStatus = new ServiceStatus();
        serviceStatus.setServiceName(serviceStatusEntity.getServiceName());
        serviceStatus.setServiceState(serviceStatusEntity.getServiceState());
        serviceStatus.setStatusMessage(serviceStatusEntity.getStatusMessage());
        return serviceStatus;
    }
}
