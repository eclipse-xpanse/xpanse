package org.eclipse.osc.orchestrator.plugin.k8s;

import org.apache.karaf.minho.boot.service.ConfigService;
import org.apache.karaf.minho.boot.service.ServiceRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class MinioOrchestratorStorageTest {

    @Test
    @Disabled("Require concrete Minio service running")
    public void test() throws Exception {
        MinioOrchestratorStorage storage = new MinioOrchestratorStorage();

        ConfigService configService = new ConfigService();
        ServiceRegistry serviceRegistry = new ServiceRegistry();
        serviceRegistry.add(configService);

        storage.onRegister(serviceRegistry);

        Assertions.assertEquals(0, storage.services().size());

        storage.store("test");

        Assertions.assertEquals(1, storage.services().size());

        Assertions.assertTrue(storage.exists("test"));

        storage.remove("test");

        Assertions.assertEquals(0, storage.services().size());
        Assertions.assertFalse(storage.exists("test"));
    }

}
