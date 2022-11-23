package org.eclipse.osc.orchestrator.plugin.huaweicloud;

import java.util.stream.Stream;
import org.apache.karaf.minho.boot.Minho;
import org.apache.karaf.minho.boot.service.ConfigService;
import org.apache.karaf.minho.boot.service.ServiceRegistry;
import org.eclipse.osc.services.ocl.loader.OclLoader;
import org.junit.jupiter.api.Assertions;
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
}
