package org.eclipse.osc.orchestrator.plugin.huaweicloud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.karaf.minho.boot.Minho;
import org.apache.karaf.minho.boot.service.ConfigService;
import org.apache.karaf.minho.boot.service.LifeCycleService;
import org.apache.karaf.minho.boot.service.ServiceRegistry;
import org.eclipse.osc.orchestrator.FileOrchestratorStorage;
import org.eclipse.osc.orchestrator.OrchestratorService;
import org.eclipse.osc.services.ocl.loader.OclLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class HuaweiCloudOrchestratorPluginTest {

    @Test()
    public void onRegisterTest() {
        HuaweiCloudOrchestratorPlugin plugin = new HuaweiCloudOrchestratorPlugin();
        Minho minho = Minho.builder()
            .loader(() -> Stream.of(new ConfigService(), new OclLoader()))
            .build()
            .start();
        ServiceRegistry registry = minho.getServiceRegistry();
        Assertions.assertDoesNotThrow(() -> plugin.onRegister(registry));
    }

    @Test()
    public void illegalTest() {
        HuaweiCloudOrchestratorPlugin plugin = new HuaweiCloudOrchestratorPlugin();

        Assertions.assertThrows(IllegalStateException.class, () -> plugin.onRegister(null));
        Assertions.assertThrows(
            IllegalStateException.class, () -> plugin.onRegister(new ServiceRegistry()));
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> plugin.registerManagedService(null));
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> plugin.updateManagedService(null, null));
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> plugin.startManagedService(null));
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> plugin.startManagedService(""));
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> plugin.stopManagedService(null));
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> plugin.stopManagedService(""));
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> plugin.unregisterManagedService(null));
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> plugin.unregisterManagedService(""));
    }

    @Disabled
    @Test
    public void basicManagedServiceTest() throws Exception {
        ConfigService configService = new ConfigService();
        Map<String, String> properties = new HashMap<>();
        properties.put("orchestrator.store.filename", "target/test-classes/test.properties");
        configService.setProperties(properties);

        Minho minho = Minho.builder().loader(
            () -> Stream.of(configService, new LifeCycleService(), new OclLoader(),
                new OrchestratorService(), new HuaweiCloudOrchestratorPlugin(),
                new FileOrchestratorStorage(configService))).build().start();

        ServiceRegistry serviceRegistry = minho.getServiceRegistry();
        OrchestratorService orchestratorService = serviceRegistry.get(OrchestratorService.class);

        Assertions.assertEquals(1, orchestratorService.getPlugins().size());
        Assertions.assertTrue(
            orchestratorService.getPlugins().get(0) instanceof HuaweiCloudOrchestratorPlugin);

        orchestratorService.registerManagedService("file:./target/test-classes/huawei_test.json");

        Assertions.assertEquals(1, orchestratorService.getStorage().services().size());
        List<String> managedServicesList = new ArrayList<>(
            orchestratorService.getStorage().services());
        Assertions.assertEquals("kafka-service", managedServicesList.get(0));

        orchestratorService.startManagedService("kafka-service");
    }
}
