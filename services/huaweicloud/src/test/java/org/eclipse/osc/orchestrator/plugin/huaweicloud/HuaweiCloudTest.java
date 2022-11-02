package org.eclipse.osc.orchestrator.plugin.huaweicloud;

import org.apache.karaf.minho.boot.Minho;
import org.apache.karaf.minho.boot.service.LifeCycleService;
import org.apache.karaf.minho.boot.service.ServiceRegistry;
import org.eclipse.osc.orchestrator.OrchestratorService;
import org.eclipse.osc.services.ocl.loader.OclLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;


import org.eclipse.osc.services.ocl.loader.OclLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HuaweiCloudTest {

    @Test
    public void loadPluginTest() throws Exception {
        Minho karaf = Minho.builder().loader(() -> Stream.of(new LifeCycleService(), new OclLoader(), new OrchestratorService(), new HuaweiCloudOrchestratorPlugin())).build().start();

        ServiceRegistry serviceRegistry = karaf.getServiceRegistry();
        OrchestratorService orchestratorService = serviceRegistry.get(OrchestratorService.class);

        Assertions.assertEquals(1, orchestratorService.getPlugins().size());
        Assertions.assertTrue(orchestratorService.getPlugins().get(0) instanceof HuaweiCloudOrchestratorPlugin);

        HuaweiCloudOrchestratorPlugin huaweiPlugin = (HuaweiCloudOrchestratorPlugin) orchestratorService.getPlugins().get(0);

        orchestratorService.registerManagedService("file:./target/test-classes/huawei_test.json");

        orchestratorService.startManagedService("my-service");
    }

}
