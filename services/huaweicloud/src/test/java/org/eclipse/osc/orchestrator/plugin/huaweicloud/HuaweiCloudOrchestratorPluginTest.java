package org.eclipse.osc.orchestrator.plugin.huaweicloud;

import java.util.stream.Stream;
import org.apache.karaf.minho.boot.Minho;
import org.apache.karaf.minho.boot.service.ConfigService;
import org.apache.karaf.minho.boot.service.ServiceRegistry;
import org.eclipse.osc.services.ocl.loader.Ocl;
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
        Assertions.assertDoesNotThrow(() -> plugin.registerManagedService(null));
        Assertions.assertDoesNotThrow(() -> plugin.registerManagedService(new Ocl()));
        Assertions.assertDoesNotThrow(() -> plugin.updateManagedService(null, null));
        Assertions.assertDoesNotThrow(() -> plugin.updateManagedService("", new Ocl()));
        Assertions.assertDoesNotThrow(() -> plugin.startManagedService(null));
        Assertions.assertDoesNotThrow(() -> plugin.startManagedService(""));
        Assertions.assertDoesNotThrow(() -> plugin.stopManagedService(null));
        Assertions.assertDoesNotThrow(() -> plugin.stopManagedService(""));
        Assertions.assertDoesNotThrow(() -> plugin.unregisterManagedService(null));
        Assertions.assertDoesNotThrow(() -> plugin.unregisterManagedService(""));
    }
}
