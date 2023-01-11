package org.eclipse.osc.orchestrator.plugin.huaweicloud;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.karaf.minho.boot.service.ConfigService;
import org.apache.karaf.minho.boot.service.ServiceRegistry;
import org.apache.karaf.minho.boot.spi.Service;
import org.eclipse.osc.modules.ocl.loader.Ocl;
import org.eclipse.osc.modules.ocl.loader.OclResources;
import org.eclipse.osc.orchestrator.OrchestratorPlugin;
import org.eclipse.osc.orchestrator.OrchestratorStorage;

@Slf4j
public class HuaweiCloudOrchestratorPlugin implements OrchestratorPlugin, Service {

    private final Map<String, Ocl> managedOcl = new HashMap<>();

    private ConfigService config;

    OrchestratorStorage storage;

    ObjectMapper objectMapper = new ObjectMapper();

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

        storage = serviceRegistry.get(OrchestratorStorage.class);

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

        Ocl ocl = managedOcl.get(managedServiceName).deepCopy();
        if (ocl == null) {
            throw new IllegalStateException("Ocl object is null.");
        }

        BuilderFactory factory = new BuilderFactory();
        Optional<AtomBuilder> optionalAtomBuilder = factory.createBuilder(
            BuilderFactory.BASIC_BUILDER, ocl);

        BuilderContext ctx = new BuilderContext();
        ctx.setConfig(config);

        if (optionalAtomBuilder.isEmpty()) {
            throw new IllegalStateException("Builder not found.");
        }
        OclResources oclResources = getOclResources(managedServiceName);
        if (oclResources != null && oclResources.getState().equals("active")) {
            log.info("Managed service {} already in active.", managedServiceName);
            return;
        }

        ctx.getOclResources().setState("building");
        storeOclResources(managedServiceName, ctx.getOclResources());

        try {
            optionalAtomBuilder.get().build(ctx);
        } catch (Exception ex) {
            optionalAtomBuilder.get().rollback(ctx);
            throw ex;
        }
        ctx.getOclResources().setState("active");
        storeOclResources(managedServiceName, ctx.getOclResources());
    }

    @Override
    public void stopManagedService(String managedServiceName) {
        log.info("Stop managed service {} on Huawei Cloud", managedServiceName);
        if (!managedOcl.containsKey(managedServiceName)) {
            throw new IllegalArgumentException("Service:" + managedServiceName + "not registered.");
        }
        Optional<AtomBuilder> optionalAtomBuilder = createBuilder(managedServiceName);

        BuilderContext ctx = new BuilderContext();
        ctx.setConfig(config);

        if (optionalAtomBuilder.isEmpty()) {
            throw new IllegalStateException("Builder not found.");
        }
        optionalAtomBuilder.get().rollback(ctx);

        storeOclResources(managedServiceName, new OclResources());
    }

    @Override
    public void unregisterManagedService(String managedServiceName) {
        log.info("Destroy managed service {} from Huawei Cloud", managedServiceName);
        if (!managedOcl.containsKey(managedServiceName)) {
            throw new IllegalArgumentException("Service:" + managedServiceName + "not registered.");
        }
        managedOcl.remove(managedServiceName);
    }

    private Optional<AtomBuilder> createBuilder(String managedServiceName) {
        Ocl ocl = managedOcl.get(managedServiceName).deepCopy();
        if (ocl == null) {
            throw new IllegalStateException("Ocl object is null.");
        }

        BuilderFactory factory = new BuilderFactory();
        return factory.createBuilder(BuilderFactory.BASIC_BUILDER, ocl);
    }

    private void storeOclResources(String managedServiceName, OclResources oclResources) {
        String oclResourceStr;
        try {
            oclResourceStr = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(oclResources);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Serial OCL object to json failed.", ex);
        }

        if (storage != null) {
            storage.store(managedServiceName, name(), "state", oclResourceStr);
        } else {
            log.warn("storage is null.");
        }
    }

    private OclResources getOclResources(String managedServiceName) {
        OclResources oclResources;
        String oclResourceStr;
        if (storage != null) {
            oclResourceStr = storage.getKey(managedServiceName, name(), "state");
        } else {
            return null;
        }

        try {
            oclResources = objectMapper.readValue(oclResourceStr, OclResources.class);
        } catch (JsonProcessingException ex) {
            log.error("Serial OCL object to json failed.", ex);
            oclResources = new OclResources();
        }

        return oclResources;
    }
}
