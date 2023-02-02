/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.openstack;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.io.File;
import org.eclipse.osc.modules.ocl.loader.OclLoader;
import org.eclipse.osc.modules.ocl.loader.data.models.Artifact;
import org.eclipse.osc.modules.ocl.loader.data.models.Ocl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openstack4j.api.OSClient;
import org.openstack4j.openstack.identity.v3.domain.KeystoneToken;
import org.openstack4j.openstack.internal.OSClientSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
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
    NovaManager novaManager;

    @Test
    public void onRegisterTest() throws Exception {
        when(this.keystoneManager.getClient()).thenReturn(
                OSClientSession.OSClientSessionV3.createSession(new KeystoneToken()));
        OpenstackOrchestratorPlugin openstackOrchestratorPlugin =
                new OpenstackOrchestratorPlugin(this.keystoneManager, this.novaManager);
        doAnswer(invocationOnMock -> null).when(this.novaManager)
                .createVm(any(OSClient.OSClientV3.class), any(Artifact.class), any(Ocl.class));
        when(this.novaManager.getVmConsoleLog(any(OSClient.OSClientV3.class), anyInt(),
                anyString())).thenReturn("kafka up and running");
        Ocl ocl = oclLoader.getOcl(new File("target/test-classes/kafka-test.json").toURI().toURL());
        openstackOrchestratorPlugin.registerManagedService(ocl);
    }
}
