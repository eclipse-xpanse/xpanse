package org.eclipse.osc.orchestrator;

import lombok.Data;
import lombok.extern.java.Log;
import org.apache.karaf.boot.service.KarafLifeCycleService;
import org.apache.karaf.boot.service.ServiceRegistry;
import org.apache.karaf.boot.spi.Service;
import org.eclipse.osc.services.ocl.loader.Ocl;
import org.eclipse.osc.services.ocl.loader.OclLoader;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Log
@Data
public class OrchestratorService implements Service {

    private List<OrchestratorPlugin> plugins = new ArrayList<>();
    private Set<String> managedServices = new HashSet<>();

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

        KarafLifeCycleService karafLifeCycleService = serviceRegistry.get(KarafLifeCycleService.class);

        karafLifeCycleService.onStart(() -> {
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
        plugins.forEach(plugin -> {
            managedServices.add(plugin.registerManagedService(ocl));
        });
    }

    /**
     * Start (expose to users) a managed service on all orchestrator plugins.
     *
     * @param sid the managed service ID.
     * @throws Exception if start fails.
     */
    public void startManagedService(String sid) throws Exception {
        if (!managedServices.contains(sid)) {
            throw new IllegalStateException("Managed service " + sid + " not found");
        }
        plugins.forEach(plugin -> {
            plugin.startManagedService(sid);
        });
    }

    /**
     * Stop (managed service is not visible to users anymore) a managed service on all orchestrator plugins.
     *
     * @param sid the managed service ID.
     * @throws Exception if stop fails.
     */
    public void stopManagedService(String sid) throws Exception {
        if (!managedServices.contains(sid)) {
            throw new IllegalStateException("Managed service " + sid + " not found");
        }
        plugins.forEach(plugin -> {
            plugin.stopManagedService(sid);
        });
    }

    /**
     * Unregister a managed service and destroy/clean all associated resources on all orchestrator plugins.
     *
     * @param sid the managed service ID.
     * @throws Exception if unregister fails.
     */
    public void unregisterManagedService(String sid) throws Exception {
        if (!managedServices.contains(sid)) {
            throw new IllegalStateException("Managed service " + sid + " not found");
        }
        plugins.forEach(plugin -> {
            plugin.unregisterManagedService(sid);
        });
        managedServices.remove(sid);
    }

}
