package org.eclipse.osc.orchestrator;

import org.apache.karaf.boot.Karaf;
import org.apache.karaf.boot.service.KarafLifeCycleService;
import org.apache.karaf.boot.service.ServiceRegistry;
import org.eclipse.osc.services.ocl.loader.OclLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class OrchestratorServiceTest {

    @Test
    public void loadPluginTest() throws Exception {
        Karaf karaf = Karaf.builder().loader(() -> Stream.of(new KarafLifeCycleService(), new OclLoader(), new PluginTest(), new OrchestratorService())).build().start();

        ServiceRegistry serviceRegistry = karaf.getServiceRegistry();
        OrchestratorService orchestratorService = serviceRegistry.get(OrchestratorService.class);

        Assertions.assertEquals(1, orchestratorService.getPlugins().size());
        Assertions.assertTrue(orchestratorService.getPlugins().get(0) instanceof PluginTest);

        orchestratorService.registerManagedService("file:./target/test-classes/test.json");

        Assertions.assertEquals(1, orchestratorService.getManagedServices().size());
        List<String> managedServicesList = new ArrayList<>(orchestratorService.getManagedServices());
        Assertions.assertEquals("1", managedServicesList.get(0));

        orchestratorService.startManagedService("1");
        orchestratorService.stopManagedService("1");

        try {
            orchestratorService.startManagedService("43421");
            Assertions.fail("IllegalStateException expected before");
        } catch (Exception e) {
            // good
        }

        orchestratorService.unregisterManagedService("1");

        Assertions.assertEquals(0, orchestratorService.getManagedServices().size());
    }

}
