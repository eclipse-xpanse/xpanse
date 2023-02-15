/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.xpanse.modules.database.ServiceStatusEntity;
import org.eclipse.xpanse.modules.database.ServiceStatusRepository;
import org.eclipse.xpanse.modules.ocl.loader.OclLoader;
import org.eclipse.xpanse.modules.ocl.loader.data.models.ServiceStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@TestPropertySource(locations = "classpath:application-test.properties")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PluginTest.class, OclLoader.class, DatabaseOrchestratorStorage.class,
        OrchestratorService.class})
public class OrchestratorServiceTest {

    @Autowired
    OrchestratorService orchestratorService;

    @MockBean
    ServiceStatusRepository serviceStatusRepository;

    @Test
    public void loadPluginTest() throws Exception {

        Assertions.assertEquals(1, orchestratorService.getPlugins().size());
        Assertions.assertTrue(orchestratorService.getPlugins().get(0) instanceof PluginTest);

        PluginTest pluginTest = (PluginTest) orchestratorService.getPlugins().get(0);

        orchestratorService.registerManagedService("file:./target/test-classes/test.json");
        ServiceStatusEntity serviceStatusEntity = new ServiceStatusEntity();
        serviceStatusEntity.setServiceName("test-service");
        serviceStatusEntity.setPluginName(PluginTest.class.getSimpleName());
        when(this.serviceStatusRepository.findAll()).thenReturn(List.of(serviceStatusEntity));
        Assertions.assertEquals(1, orchestratorService.getStoredServices().size());
        List<ServiceStatus> managedServicesList =
                new ArrayList<>(orchestratorService.getStoredServices());
        Assertions.assertEquals("test-service", managedServicesList.get(0).getServiceName());

        Assertions.assertNotNull(pluginTest.getOcl());
        Assertions.assertEquals("test-service", pluginTest.getOcl().getName());

        orchestratorService.startManagedService("test-service");
        orchestratorService.stopManagedService("test-service");

        try {
            orchestratorService.startManagedService("43421");
            Assertions.fail("IllegalStateException expected before");
        } catch (Exception e) {
            // good
        }

        orchestratorService.unregisterManagedService("test-service");

        Assertions.assertNull(pluginTest.getOcl());
    }

    @Test
    public void loadWithCustomStorageTest() throws Exception {
        Set<String> services = new HashSet<>();
        orchestratorService.registerManagedService("file:./target/test-classes/test.json");

        Assertions.assertEquals(1, orchestratorService.getPlugins().size());
    }

}
