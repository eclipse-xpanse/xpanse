/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.huaweicloud;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.osc.modules.ocl.loader.OclLoader;
import org.eclipse.osc.orchestrator.OrchestratorService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HuaweiCloudOrchestratorPlugin.class, OrchestratorService.class,
        FileOrchestratorStorage.class, OclLoader.class})
@ActiveProfiles(value = {"huaweicloud", "test"})
public class HuaweiCloudOrchestratorPluginTest {
    @Autowired
    HuaweiCloudOrchestratorPlugin huaweiCloudOrchestratorPlugin;
    @Autowired
    Environment environment;
    @Autowired
    OrchestratorService orchestratorService;

    @Test()
    public void illegalTest() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> huaweiCloudOrchestratorPlugin.registerManagedService(null));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> huaweiCloudOrchestratorPlugin.updateManagedService(null, null));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> huaweiCloudOrchestratorPlugin.startManagedService(null));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> huaweiCloudOrchestratorPlugin.startManagedService(""));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> huaweiCloudOrchestratorPlugin.stopManagedService(null));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> huaweiCloudOrchestratorPlugin.stopManagedService(""));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> huaweiCloudOrchestratorPlugin.unregisterManagedService(null));
        Assertions.assertThrows(IllegalArgumentException.class,
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
        List<String> managedServicesList = new ArrayList<>(
                this.orchestratorService.getStoredServices());
        Assertions.assertEquals("kafka-service", managedServicesList.get(0));

        orchestratorService.startManagedService("kafka-service");
    }
}
