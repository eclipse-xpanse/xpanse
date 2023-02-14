/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.huaweicloud;

import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.xpanse.modules.database.ServiceStatusRepository;
import org.eclipse.xpanse.modules.ocl.loader.OclLoader;
import org.eclipse.xpanse.modules.ocl.loader.data.models.ServiceStatus;
import org.eclipse.xpanse.orchestrator.DatabaseOrchestratorStorage;
import org.eclipse.xpanse.orchestrator.OrchestratorService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HuaweiCloudOrchestratorPlugin.class, OrchestratorService.class,
        DatabaseOrchestratorStorage.class, OclLoader.class})
@ActiveProfiles(value = {"huaweicloud", "test"})
@SpringBootTest
public class HuaweiCloudOrchestratorPluginTest {
    @Autowired
    HuaweiCloudOrchestratorPlugin huaweiCloudOrchestratorPlugin;
    @Autowired
    Environment environment;
    @Autowired
    OrchestratorService orchestratorService;

    @MockBean
    ServiceStatusRepository serviceStatusRepository;

    @Test()
    public void illegalTest() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> huaweiCloudOrchestratorPlugin.updateManagedService(null, null));
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> huaweiCloudOrchestratorPlugin.startManagedService(null));
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> huaweiCloudOrchestratorPlugin.startManagedService(""));
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> huaweiCloudOrchestratorPlugin.stopManagedService(null));
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> huaweiCloudOrchestratorPlugin.stopManagedService(""));
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> huaweiCloudOrchestratorPlugin.unregisterManagedService(null));
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> huaweiCloudOrchestratorPlugin.unregisterManagedService(""));
    }

    @Disabled
    @Test
    public void basicManagedServiceTest() throws Exception {

        Assertions.assertEquals(1, this.orchestratorService.getPlugins().size());
        Assertions.assertTrue(
                orchestratorService.getPlugins().get(0) instanceof HuaweiCloudOrchestratorPlugin);

        orchestratorService.registerManagedService("file:./target/test-classes/huawei_test.json");

        Assertions.assertEquals(1, this.orchestratorService.getStoredServices().size());
        List<ServiceStatus> managedServicesList = new ArrayList<>(
                this.orchestratorService.getStoredServices());
        Assertions.assertEquals("kafka-service", managedServicesList.get(0).getServiceName());

        orchestratorService.startManagedService("kafka-service");
    }
}
