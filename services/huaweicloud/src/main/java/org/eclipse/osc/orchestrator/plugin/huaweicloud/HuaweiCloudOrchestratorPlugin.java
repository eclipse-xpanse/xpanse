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
            throw new IllegalStateException("ServiceRegistry is null");
        }
        ConfigService configService = serviceRegistry.get(ConfigService.class);
        if (configService == null) {
            throw new IllegalStateException("Config service is not present in the registry");
        }

        config = configService;
    }

    @Override
    public void registerManagedService(Ocl ocl) {
        log.info("Register managed service, creating  Huawei Cloud resource");
        if (ocl == null) {
            throw new IllegalArgumentException("registering invalid ocl. ocl = null");
        }
        managedOcl.put(ocl.getName(), ocl);
    }

    @Override
    public void updateManagedService(String managedServiceName, Ocl ocl) {
        log.info("Updating managed service {} on Huawei Cloud", managedServiceName);
        if (ocl == null) {
            throw new IllegalArgumentException("Invalid ocl. ocl = null");
        }
        managedOcl.put(managedServiceName, ocl);
    }

    @Override
    public void startManagedService(String managedServiceName) {
        log.info("Start managed service {} on Huawei Cloud", managedServiceName);
        if (!managedOcl.containsKey(managedServiceName)) {
            throw new IllegalArgumentException("Service:" + managedServiceName + "not registered.");
        }

        Optional<AtomBuilder> optionalAtomBuilder = createBuiler(managedServiceName);

        BuilderContext ctx = new BuilderContext();
        ctx.setConfig(config);

        if (optionalAtomBuilder.isEmpty()) {
            throw new IllegalStateException("Builder not found.");
        }
        optionalAtomBuilder.get().build(ctx);
    }

    @Override
    public void stopManagedService(String managedServiceName) {
        log.info("Stop managed service {} on Huawei Cloud", managedServiceName);
        if (!managedOcl.containsKey(managedServiceName)) {
            throw new IllegalArgumentException("Service:" + managedServiceName + "not registered.");
        }
        Optional<AtomBuilder> optionalAtomBuilder = createBuiler(managedServiceName);

        BuilderContext ctx = new BuilderContext();
        ctx.setConfig(config);

        if (optionalAtomBuilder.isEmpty()) {
            throw new IllegalStateException("Builder not found.");
        }
        optionalAtomBuilder.get().rollback(ctx);
    }

    @Override
    public void unregisterManagedService(String managedServiceName) {
        log.info("Destroy managed service {} from Huawei Cloud", managedServiceName);
        if (!managedOcl.containsKey(managedServiceName)) {
            throw new IllegalArgumentException("Service:" + managedServiceName + "not registered.");
        }
        managedOcl.remove(managedServiceName);
    }

    private Optional<AtomBuilder> createBuiler(String managedServiceName) {
        Ocl ocl = managedOcl.get(managedServiceName).deepCopy();
        if (ocl == null) {
            throw new IllegalStateException("Ocl object is null.");
        }

        BuilderFactory factory = new BuilderFactory();
        return factory.createBuilder(BuilderFactory.BASIC_BUILDER, ocl);
    }
}
