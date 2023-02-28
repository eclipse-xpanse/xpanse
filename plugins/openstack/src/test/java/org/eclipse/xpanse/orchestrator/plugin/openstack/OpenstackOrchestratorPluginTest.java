/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.openstack;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import org.eclipse.xpanse.modules.database.ServiceStatusEntity;
import org.eclipse.xpanse.modules.ocl.loader.OclLoader;
import org.eclipse.xpanse.modules.ocl.loader.data.models.Ocl;
import org.eclipse.xpanse.orchestrator.OrchestratorStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openstack4j.openstack.identity.v3.domain.KeystoneToken;
import org.openstack4j.openstack.internal.OSClientSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {OclLoader.class})
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles(value = {"openstack", "test"})
public class OpenstackOrchestratorPluginTest {

    @Autowired
    OclLoader oclLoader;

    @MockBean
    KeystoneManager keystoneManager;

    @MockBean
    ApplicationContext applicationContext;

    @MockBean
    OrchestratorStorage orchestratorStorage;

    @Test
    public void onRegisterTest() throws Exception {
        when(this.keystoneManager.getClient()).thenReturn(
                OSClientSession.OSClientSessionV3.createSession(new KeystoneToken()));
        OpenstackOrchestratorPlugin openstackOrchestratorPlugin =
                new OpenstackOrchestratorPlugin(this.keystoneManager, this.orchestratorStorage,
                        this.applicationContext);
        when(this.orchestratorStorage.isManagedServiceByNameAndPluginExists(
                any(String.class), any())).thenReturn(true);
        ServiceStatusEntity serviceStatusEntity = new ServiceStatusEntity();
        serviceStatusEntity.setServiceName("kafka");
        Ocl ocl = oclLoader.getOcl(
                new File("target/test-classes/kafka-test.yaml").toURI().toURL());
        serviceStatusEntity.setOcl(ocl);
        when(this.orchestratorStorage.getServiceDetailsByNameAndPlugin(
                any(String.class), any())).thenReturn(serviceStatusEntity);

        openstackOrchestratorPlugin.registerManagedService(ocl);
        openstackOrchestratorPlugin.startManagedService(ocl.getName());
    }
}
