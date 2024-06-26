/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.credential;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.org.webcompere.systemstubs.SystemStubs.withEnvironmentVariable;
import static uk.org.webcompere.systemstubs.SystemStubs.withEnvironmentVariables;

import java.util.List;
import java.util.Objects;
import org.eclipse.xpanse.modules.credential.cache.CaffeineCredentialCacheManager;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.credential.exceptions.CredentialVariablesNotComplete;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.security.common.AesUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {CaffeineCredentialCacheManager.class, CredentialsStore.class,
        AesUtil.class})
@ExtendWith(SpringExtension.class)
class CredentialSearchTests {

    @Mock
    AesUtil aesUtil;

    @Mock
    CredentialsStore credentialsStore;

    @Mock
    PluginManager pluginManager;

    @InjectMocks
    CredentialCenter credentialCenter;

    @Test
    void testCredentialVariablesNotCompleteIsThrownWhenMandatoryVariablesAreMissing() {
        when(pluginManager.getOrchestratorPlugin(any())).thenReturn(new DummyPluginImpl());
        final List<AbstractCredentialInfo> credentialVariables =
                List.of(new CredentialVariables(Csp.OPENSTACK_TESTLAB, CredentialType.VARIABLES, "name",
                        "description",
                        "userId",
                        List.of(new CredentialVariable("name", "description", false))));
        when(credentialsStore.getCredential(any(), any(), any(), any())).thenReturn(
                credentialVariables.getFirst());
        Assertions.assertThrows(CredentialVariablesNotComplete.class,
                () -> this.credentialCenter.getCredential(Csp.OPENSTACK_TESTLAB,
                        CredentialType.VARIABLES, "user"));
    }

    @Test
    void testSuccessfulCredentialsGet() {
        when(pluginManager.getOrchestratorPlugin(any())).thenReturn(new DummyPluginImpl());
        final List<AbstractCredentialInfo> credentialVariables =
                List.of(new CredentialVariables(Csp.OPENSTACK_TESTLAB, CredentialType.VARIABLES, "name",
                        "description",
                        "userId",
                        List.of(new CredentialVariable("name", "description", false, false),
                                new CredentialVariable("id", "description", true, false,
                                        "testName"))));
        when(credentialsStore.getCredential(any(), any(), any(), any())).thenReturn(
                credentialVariables.getFirst());
        AbstractCredentialInfo abstractCredentialInfo =
                this.credentialCenter.getCredential(Csp.OPENSTACK_TESTLAB,
                        CredentialType.VARIABLES, "user");
        Assertions.assertTrue(Objects.nonNull(abstractCredentialInfo));
        Assertions.assertSame(Csp.OPENSTACK_TESTLAB, abstractCredentialInfo.getCsp());
        Assertions.assertSame(CredentialType.VARIABLES, abstractCredentialInfo.getType());
    }

    @Test
    void testJoinSuccessfulCredentialsGet() throws Exception {
        when(pluginManager.getOrchestratorPlugin(any())).thenReturn(new DummyPluginImpl());
        withEnvironmentVariable("name", "testUser").execute(() -> {
            final List<AbstractCredentialInfo> credentialVariables =
                    List.of(new CredentialVariables(Csp.OPENSTACK_TESTLAB, CredentialType.VARIABLES, "name",
                            "description",
                            "userId",
                            List.of(new CredentialVariable("name", "description", true, false),
                                    new CredentialVariable("id", "description", true, false,
                                            "testName"))));
            when(credentialsStore.getCredential(any(), any(), any(), any())).thenReturn(
                    credentialVariables.getFirst());
            AbstractCredentialInfo abstractCredentialInfo =
                    this.credentialCenter.getCredential(Csp.OPENSTACK_TESTLAB,
                            CredentialType.VARIABLES, "user");
            Assertions.assertTrue(Objects.nonNull(abstractCredentialInfo));
            Assertions.assertSame(Csp.OPENSTACK_TESTLAB, abstractCredentialInfo.getCsp());
            Assertions.assertSame(CredentialType.VARIABLES, abstractCredentialInfo.getType());
            Assertions.assertEquals(2,
                    ((CredentialVariables) abstractCredentialInfo).getVariables().stream()
                            .filter(credentialVariable -> Objects.nonNull(
                                    credentialVariable.getValue())).count());
        });
    }

    @Test
    void testGetFullCredentialConfigFromEnv() throws Exception {
        when(pluginManager.getOrchestratorPlugin(any())).thenReturn(new DummyPluginImpl());
        withEnvironmentVariables("name", "testUser", "id", "testId").execute(() -> {
            final List<AbstractCredentialInfo> credentialVariables =
                    List.of(new CredentialVariables(Csp.OPENSTACK_TESTLAB, CredentialType.VARIABLES, "name",
                            "description",
                            "userId",
                            List.of(new CredentialVariable("name", "description", true, false),
                                    new CredentialVariable("id", "description", true, false))));
            when(credentialsStore.getCredential(any(), any(), any(), any())).thenReturn(
                    credentialVariables.getFirst());
            AbstractCredentialInfo abstractCredentialInfo =
                    this.credentialCenter.getCredential(Csp.OPENSTACK_TESTLAB,
                            CredentialType.VARIABLES, "user");
            Assertions.assertTrue(Objects.nonNull(abstractCredentialInfo));
            Assertions.assertSame(Csp.OPENSTACK_TESTLAB, abstractCredentialInfo.getCsp());
            Assertions.assertSame(CredentialType.VARIABLES, abstractCredentialInfo.getType());
            Assertions.assertEquals(2,
                    ((CredentialVariables) abstractCredentialInfo).getVariables().stream()
                            .filter(credentialVariable -> Objects.nonNull(
                                    credentialVariable.getValue())).count());
        });
    }
}
