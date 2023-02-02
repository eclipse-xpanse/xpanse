/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.osc.modules.ocl.loader.OclLoader;
import org.eclipse.osc.modules.ocl.loader.data.models.Ocl;
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

    private final OrchestratorStorage orchestratorStorage;
    private final OclLoader oclLoader;

    @Getter
    private final List<OrchestratorPlugin> plugins = new ArrayList<>();

    @Autowired
    public OrchestratorService(OclLoader oclLoader, OrchestratorStorage orchestratorStorage) {
        this.oclLoader = oclLoader;
        this.orchestratorStorage = orchestratorStorage;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            ApplicationContext applicationContext =
                    ((ContextRefreshedEvent) event).getApplicationContext();
            plugins.addAll(applicationContext.getBeansOfType(OrchestratorPlugin.class).values());
            if (plugins.size() > 1) {
                throw new RuntimeException(
                        "More than one OSC plugin found. Only one plugin can be active at a time.");
            }
            if (plugins.isEmpty()) {
                log.warn("No OSC plugins loaded by the runtime.");
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
        Ocl ocl = oclLoader.getOcl(new URL(oclLocation));
        registerManagedService(ocl);
    }

    /**
     * Register a managed service on all orchestrator plugins, directly using OCL descriptor.
     *
     * @param ocl the OCL descriptor.
     */
    public void registerManagedService(Ocl ocl) {
        if (ocl.getName() == null) {
            throw new IllegalArgumentException("Managed service name is required");
        }
        plugins.forEach(plugin -> plugin.registerManagedService(ocl));
        orchestratorStorage.store(ocl.getName());
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
        plugins.forEach(plugin -> plugin.updateManagedService(managedServiceName, ocl));
    }

    /**
     * Start (expose to users) a managed service on all orchestrator plugins.
     *
     * @param managedServiceName the managed service name.
     */
    public void startManagedService(String managedServiceName) {
        if (!orchestratorStorage.exists(managedServiceName)) {
            throw new IllegalStateException("Managed service " + managedServiceName + " not found");
        }
        plugins.forEach(plugin -> plugin.startManagedService(managedServiceName));
    }

    /**
     * Stop (managed service is not visible to users anymore) a managed service
     * on all orchestrator plugins.
     *
     * @param managedServiceName the managed service name.
     */
    public void stopManagedService(String managedServiceName) {
        if (!orchestratorStorage.exists(managedServiceName)) {
            throw new IllegalStateException("Managed service " + managedServiceName + " not found");
        }
        plugins.forEach(plugin -> plugin.stopManagedService(managedServiceName));
    }

    /**
     * Unregister a managed service and destroy/clean all associated resources
     * on all orchestrator plugins.
     *
     * @param managedServiceName the managed service name.
     */
    public void unregisterManagedService(String managedServiceName) {
        if (!orchestratorStorage.exists(managedServiceName)) {
            throw new IllegalStateException("Managed service " + managedServiceName + " not found");
        }
        plugins.forEach(plugin -> plugin.unregisterManagedService(managedServiceName));
        orchestratorStorage.remove(managedServiceName);
    }

    /**
     * Get the runtime state of the managed service.
     *
     * @param managedServiceName the managed service name.
     */
    public String getManagedServiceState(String managedServiceName) {
        if (!orchestratorStorage.exists(managedServiceName)) {
            throw new IllegalStateException("Managed service " + managedServiceName + " not found");
        }
        StringBuilder response = new StringBuilder("[\n");
        plugins.forEach(plugin -> {
            if (plugin != null) {
                response.append(orchestratorStorage.getKey(managedServiceName,
                        plugin.getClass().getSimpleName(),
                        "state"));
                response.append("\n");
            }
        });
        response.append("]\n");

        return response.toString();
    }

    public Set<String> getStoredServices() {
        return this.orchestratorStorage.services();
    }

}
