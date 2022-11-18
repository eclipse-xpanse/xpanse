package org.eclipse.osc.orchestrator.plugin.huaweicloud;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.karaf.minho.boot.service.ConfigService;
import org.apache.karaf.minho.boot.service.ServiceRegistry;
import org.apache.karaf.minho.boot.spi.Service;
import org.eclipse.osc.orchestrator.OrchestratorPlugin;
import org.eclipse.osc.services.ocl.loader.Ocl;

@Slf4j
public class HuaweiCloudOrchestratorPlugin implements OrchestratorPlugin, Service {

    private final Map<String, Ocl> managedOcl = new HashMap<>();

    private ConfigService config;

    @Override
    public String name() {
        return "osc-orchestrator-huaweicloud";
    }

    @Override
    public void onRegister(ServiceRegistry serviceRegistry) {
        log.info("Registering Huawei Cloud Orchestrator ...");
        if (serviceRegistry == null) {
            log.error("ServiceRegistry is null");
            throw new IllegalStateException("ServiceRegistry is null");
        }
        ConfigService configService = serviceRegistry.get(ConfigService.class);
        if (configService == null) {
            log.error("Config service is not present in the registry");
            throw new IllegalStateException("Config service is not present in the registry");
        }

        config = configService;
    }

    @Override
    public void registerManagedService(Ocl ocl) {
        log.info("Register managed service, creating  Huawei Cloud resource");
        if (ocl == null) {
            log.error("registering invalid ocl. ocl = null");
            return;
        }
        managedOcl.put(ocl.getName(), ocl);
    }

    @Override
    public void updateManagedService(String managedServiceName, Ocl ocl) {
        log.info("Updating managed service {} on Huawei Cloud", managedServiceName);
        managedOcl.put(managedServiceName, ocl);
    }

    @Override
    public void startManagedService(String managedServiceName) {
        log.info("Start managed service {} on Huawei Cloud", managedServiceName);
        if (!managedOcl.containsKey(managedServiceName)) {
            log.error("Service: {} not registered.", managedServiceName);
            return;
        }

        // TODO: need to do a deep copy for ocl.
        Ocl ocl = managedOcl.get(managedServiceName);
        if (ocl == null) {
            log.error("Ocl object is null.");
            return;
        }

        BuilderFactory factory = new BuilderFactory();
        Optional<AtomBuilder> optionalAtomBuilder = factory.createBuilder("basic", ocl);

        BuilderContext ctx = new BuilderContext();
        ctx.setConfig(config);

        if (optionalAtomBuilder.isEmpty()) {
            log.error("Builder not found.");
            return;
        }
        optionalAtomBuilder.get().build(ctx);
    }

    @Override
    public void stopManagedService(String managedServiceName) {
        log.info("Stop managed service {} on Huawei Cloud", managedServiceName);
        if (!managedOcl.containsKey(managedServiceName)) {
            log.error("Service: {} not registered.", managedServiceName);
        }
    }

    @Override
    public void unregisterManagedService(String managedServiceName) {
        log.info("Destroy managed service {} from Huawei Cloud", managedServiceName);
        if (!managedOcl.containsKey(managedServiceName)) {
            log.error("Service: {} not registered.", managedServiceName);
        }
    }
}
