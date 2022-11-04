package org.eclipse.osc.orchestrator;

import lombok.Data;
import lombok.extern.java.Log;
import org.apache.karaf.minho.boot.service.LifeCycleService;
import org.apache.karaf.minho.boot.service.ServiceRegistry;
import org.apache.karaf.minho.boot.spi.Service;
import org.eclipse.osc.services.ocl.loader.Ocl;
import org.eclipse.osc.services.ocl.loader.OclLoader;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Log
@Data
public class OrchestratorService implements Service {

    private List<OrchestratorPlugin> plugins = new ArrayList<>();

    private OrchestratorStorage storage;

    private OclLoader oclLoader;

    @Override
    public String name() {
        return "osc-orchestrator";
    }

    @Override
    public void onRegister(ServiceRegistry serviceRegistry) throws Exception {
        log.info("Registering OSC orchestrator service ...");

        oclLoader = serviceRegistry.get(OclLoader.class);
        if (oclLoader == null) {
            throw new IllegalStateException("OCL Loader service is not present");
        }

        storage = serviceRegistry.get(OrchestratorStorage.class);
        if (storage == null) {
            log.warning("No orchestrator storage service found in the service registry, using default in-memory orchestrator storage");
            storage = new FileOrchestratorStorage();
        }

        LifeCycleService lifeCycleService = serviceRegistry.get(LifeCycleService.class);

        lifeCycleService.onStart(() -> {
            log.info("Loading OSC orchestrator plugins");
            plugins = serviceRegistry.getAll().values().stream().filter(service -> service instanceof OrchestratorPlugin).map(service -> (OrchestratorPlugin) service).collect(Collectors.toList());
        });
    }

    /**
     * Register a managed service on all orchestrator plugins.
     *
     * @param oclLocation the location of the OCL descriptor.
     * @throws Exception if registration fails.
     */
    public void registerManagedService(String oclLocation) throws Exception {
        Ocl ocl = oclLoader.getOcl(new URL(oclLocation));
        if (ocl.getName() == null) {
            throw new IllegalArgumentException("Managed service name is required");
        }
        plugins.forEach(plugin -> {
            plugin.registerManagedService(ocl);
        });
        storage.store(ocl.getName());
    }

    /**
     * Update existing managed service with a new OCL descriptor.
     *
     * @param managedServiceName the managed service to update, identified by the given name.
     * @param oclLocation the new OCL descriptor.
     * @throws Exception if the update fails.
     */
    public void updateManagedService(String managedServiceName, String oclLocation) throws Exception {
        Ocl ocl = oclLoader.getOcl(new URL(oclLocation));
        plugins.forEach(plugin -> {
            plugin.updateManagedService(managedServiceName, ocl);
        });
    }

    /**
     * Start (expose to users) a managed service on all orchestrator plugins.
     *
     * @param managedServiceName the managed service name.
     * @throws Exception if start fails.
     */
    public void startManagedService(String managedServiceName) throws Exception {
        if (!storage.exists(managedServiceName)) {
            throw new IllegalStateException("Managed service " + managedServiceName + " not found");
        }
        plugins.forEach(plugin -> {
            plugin.startManagedService(managedServiceName);
        });
    }

    /**
     * Stop (managed service is not visible to users anymore) a managed service on all orchestrator plugins.
     *
     * @param managedServiceName the managed service name.
     * @throws Exception if stop fails.
     */
    public void stopManagedService(String managedServiceName) throws Exception {
        if (!storage.exists(managedServiceName)) {
            throw new IllegalStateException("Managed service " + managedServiceName + " not found");
        }
        plugins.forEach(plugin -> {
            plugin.stopManagedService(managedServiceName);
        });
    }

    /**
     * Unregister a managed service and destroy/clean all associated resources on all orchestrator plugins.
     *
     * @param managedServiceName the managed service name.
     * @throws Exception if unregister fails.
     */
    public void unregisterManagedService(String managedServiceName) throws Exception {
        if (!storage.exists(managedServiceName)) {
            throw new IllegalStateException("Managed service " + managedServiceName + " not found");
        }
        plugins.forEach(plugin -> {
            plugin.unregisterManagedService(managedServiceName);
        });
        storage.remove(managedServiceName);
    }

}
