/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.credential;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.eclipse.xpanse.modules.credential.cache.CaffeineCredentialCacheManager;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CreateCredential;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.credential.exceptions.CredentialCapabilityNotFound;
import org.eclipse.xpanse.modules.models.credential.exceptions.CredentialVariablesNotComplete;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.security.common.AesUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;

/**
 * Test of CredentialCenter.
 */
@ContextConfiguration(classes = {CaffeineCredentialCacheManager.class, CredentialsStore.class})
@ExtendWith(MockitoExtension.class)
class CredentialCenterTest {

    String credentialName = "AK_SK";
    @Mock
    private AesUtil aesUtil;
    @Mock
    private PluginManager mockPluginManager;
    @Mock
    private CredentialsStore credentialsStore;
    @Mock
    private DummyPluginImpl orchestratorPlugin;
    @Mock
    private CredentialOpenApiGenerator credentialOpenApiGenerator;
    private CredentialCenter credentialCenter;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        credentialCenter = new CredentialCenter(aesUtil, mockPluginManager, credentialsStore,
                credentialOpenApiGenerator);
    }

    @Test
    void testGetAvailableCredentialTypesByCsp() {
        Csp csp = Csp.OPENSTACK;
        List<CredentialType> expectedCredentialTypes = new ArrayList<>();
        expectedCredentialTypes.add(CredentialType.VARIABLES);
        expectedCredentialTypes.add(CredentialType.API_KEY);
        when(mockPluginManager.getOrchestratorPlugin(csp)).thenReturn(orchestratorPlugin);
        when(mockPluginManager.getOrchestratorPlugin(csp).getAvailableCredentialTypes()).thenReturn(
                expectedCredentialTypes);

        List<CredentialType> actualCredentialTypes =
                credentialCenter.listAvailableCredentialTypesByCsp(csp);

        assertEquals(expectedCredentialTypes, actualCredentialTypes);
    }

    @Test
    public void testListCredentialCapabilities_WithValidType() {
        Csp csp = Csp.OPENSTACK;
        CredentialType credentialType = CredentialType.VARIABLES;

        final List<AbstractCredentialInfo> credentialVariables =
                List.of(new CredentialVariables(Csp.OPENSTACK, CredentialType.VARIABLES,
                        credentialName, "description", "userId",
                        List.of(new CredentialVariable("name", "description", true, false),
                                new CredentialVariable("id", "description", true, false))));
        when(mockPluginManager.getOrchestratorPlugin(csp)).thenReturn(orchestratorPlugin);
        when(orchestratorPlugin.getCredentialDefinitions()).thenReturn(credentialVariables);

        List<AbstractCredentialInfo> credentials =
                this.credentialCenter.listCredentialCapabilities(csp, credentialType,
                        credentialName);

        Assertions.assertTrue(Objects.nonNull(credentials));
        assertEquals(credentialVariables, credentials);
    }

    @Test
    public void testListCredentialCapabilities_WithNullType() {
        Csp csp = Csp.OPENSTACK;
        final List<AbstractCredentialInfo> credentialVariables =
                List.of(new CredentialVariables(Csp.OPENSTACK, CredentialType.VARIABLES,
                        credentialName, "description", "userId",
                        List.of(new CredentialVariable("name", "description", true, false),
                                new CredentialVariable("id", "description", true, false))));
        when(mockPluginManager.getOrchestratorPlugin(csp)).thenReturn(orchestratorPlugin);
        when(orchestratorPlugin.getCredentialDefinitions()).thenReturn(credentialVariables);

        List<AbstractCredentialInfo> credentials =
                credentialCenter.listCredentialCapabilities(csp, null, null);

        assertEquals(credentialVariables, credentials);
    }

    @Test
    public void testListCredentialCapabilities_WithNoMatchingType() {
        Csp csp = Csp.OPENSTACK;
        CredentialType credentialType = CredentialType.API_KEY;
        final List<AbstractCredentialInfo> credentialVariables =
                List.of(new CredentialVariables(Csp.OPENSTACK, CredentialType.VARIABLES,
                        credentialName, "description", "userId",
                        List.of(new CredentialVariable("name", "description", true, false),
                                new CredentialVariable("id", "description", true, false))));
        when(mockPluginManager.getOrchestratorPlugin(csp)).thenReturn(orchestratorPlugin);
        when(orchestratorPlugin.getCredentialDefinitions()).thenReturn(credentialVariables);

        List<AbstractCredentialInfo> credentials =
                credentialCenter.listCredentialCapabilities(csp, credentialType, "name");

        assertEquals(Collections.emptyList(), credentials);
    }

    @Test
    public void testGetCredentialOpenApiUrl() {
        Csp csp = Csp.OPENSTACK;
        CredentialType credentialType = CredentialType.VARIABLES;
        String expectedUrl = "https://example.com/credential/openapi";

        when(credentialOpenApiGenerator.getCredentialOpenApiUrl(csp, credentialType))
                .thenReturn(expectedUrl);

        String credentialOpenApiUrl = credentialCenter.getCredentialOpenApiUrl(csp, credentialType);

        assertEquals(expectedUrl, credentialOpenApiUrl);
    }

    @Test
    public void testListCredentials_WithNullUser() {

        List<AbstractCredentialInfo> credentials =
                credentialCenter.listCredentials(null, null, null);

        assertEquals(Collections.emptyList(), credentials);
    }

    @Test
    public void testListCredentialsOnlyUserId() {
        String userId1 = "userId1";
        String userId2 = "userId2";

        List<AbstractCredentialInfo> credentials1 =
                credentialCenter.listCredentials(null, null, userId1);
        List<AbstractCredentialInfo> credentials2 =
                credentialCenter.listCredentials(null, null, userId2);

        assertEquals(Collections.emptyList(), credentials1);
        assertEquals(Collections.emptyList(), credentials2);
    }

    @Test
    void testListCredentials_WithoutuserId() {
        Csp csp = Csp.OPENSTACK;
        String userId = "";
        CredentialType requestedCredentialType = CredentialType.VARIABLES;

        List<AbstractCredentialInfo> result = credentialCenter.listCredentials(
                csp, requestedCredentialType, userId);

        assertEquals(Collections.emptyList(), result);
    }

    @Test
    void testDeleteCredential() {
        Csp csp = Csp.OPENSTACK;
        String userId = "user123";
        CredentialType credentialType = CredentialType.API_KEY;

        credentialCenter.deleteCredential(csp, credentialType, "AK_SK", userId);

        verify(credentialsStore).deleteCredential(eq(csp), eq(credentialType), eq("AK_SK"),
                eq(userId));
    }

    @Test
    void testCreateCredential() {
        final AbstractCredentialInfo credentialInfo =
                new CredentialVariables(Csp.OPENSTACK, CredentialType.VARIABLES,
                        credentialName, "description", "userId",
                        List.of(new CredentialVariable("name", "description1", true, false),
                                new CredentialVariable("id", "description", true, false)));

        credentialCenter.createCredential(credentialInfo);

        credentialsStore.storeCredential(eq(credentialInfo));
    }

    @Test
    void testCheckInputCredentialIsValid_WithCredentialTypeValid() {

        CreateCredential inputCredential = new CreateCredential();
        inputCredential.setCsp(Csp.OPENSTACK);
        inputCredential.setType(CredentialType.VARIABLES);
        inputCredential.setName(credentialName);
        inputCredential.setVariables(
                List.of(new CredentialVariable("name", "description", true, false, "value"),
                        new CredentialVariable("id", "description", true, false, "value")));

        final List<AbstractCredentialInfo> credentialVariables =
                List.of(new CredentialVariables(Csp.OPENSTACK, CredentialType.VARIABLES,
                        credentialName, "description", "userId",
                        List.of(new CredentialVariable("name", "description", true, false, "value"),
                                new CredentialVariable("id", "description", true, false,
                                        "value"))));
        when(mockPluginManager.getOrchestratorPlugin(inputCredential.getCsp())).thenReturn(
                orchestratorPlugin);
        when(orchestratorPlugin.getCredentialDefinitions()).thenReturn(credentialVariables);
        credentialCenter.checkInputCredentialIsValid(inputCredential);
    }

    @Test
    void testCheckInputCredentialIsValid_WithCredentialTypeNotEquals() {

        CreateCredential inputCredential = new CreateCredential();
        inputCredential.setCsp(Csp.OPENSTACK);
        inputCredential.setType(CredentialType.VARIABLES);
        inputCredential.setName(credentialName);
        inputCredential.setVariables(
                List.of(new CredentialVariable("name", "description", true, false, "value"),
                        new CredentialVariable("id", "description", true, false, "value")));

        final List<AbstractCredentialInfo> credentialVariables =
                List.of(new CredentialVariables(Csp.OPENSTACK, CredentialType.API_KEY,
                        credentialName, "description", "userId",
                        List.of(new CredentialVariable("name", "description", true, false, "value"),
                                new CredentialVariable("id", "description", true, false,
                                        "value"))));
        when(mockPluginManager.getOrchestratorPlugin(inputCredential.getCsp())).thenReturn(
                orchestratorPlugin);
        when(orchestratorPlugin.getCredentialDefinitions()).thenReturn(credentialVariables);
        assertThrows(CredentialCapabilityNotFound.class,
                () -> credentialCenter.checkInputCredentialIsValid(inputCredential));


    }

    @Test
    void testCheckInputCredentialIsValid_WithCredentialTypeValidNameNotValid() {

        CreateCredential inputCredential = new CreateCredential();
        inputCredential.setCsp(Csp.OPENSTACK);
        inputCredential.setType(CredentialType.VARIABLES);
        inputCredential.setName("name1");

        final List<AbstractCredentialInfo> credentialVariables =
                List.of(new CredentialVariables(Csp.OPENSTACK, CredentialType.VARIABLES,
                        credentialName, "description", "userId",
                        List.of(new CredentialVariable("name", "description", true, false),
                                new CredentialVariable("id", "description", true, false))));
        when(mockPluginManager.getOrchestratorPlugin(inputCredential.getCsp())).thenReturn(
                orchestratorPlugin);
        when(orchestratorPlugin.getCredentialDefinitions()).thenReturn(credentialVariables);

        assertThrows(CredentialCapabilityNotFound.class,
                () -> credentialCenter.checkInputCredentialIsValid(inputCredential));
    }

    @Test
    void testCheckInputCredentialIsValid_WithCredentialTypeValidIsMandatoryValid() {

        CreateCredential inputCredential = new CreateCredential();
        inputCredential.setCsp(Csp.OPENSTACK);
        inputCredential.setType(CredentialType.VARIABLES);
        inputCredential.setName(credentialName);
        inputCredential.setVariables(
                List.of(new CredentialVariable("name", "description", true, false),
                        new CredentialVariable("id1", "description", true, false)));

        final List<AbstractCredentialInfo> credentialVariables =
                List.of(new CredentialVariables(Csp.OPENSTACK, CredentialType.VARIABLES,
                        credentialName, "description", "userId",
                        List.of(new CredentialVariable("name", "description", true, false),
                                new CredentialVariable("id", "description", true, false))));
        when(mockPluginManager.getOrchestratorPlugin(inputCredential.getCsp())).thenReturn(
                orchestratorPlugin);
        when(orchestratorPlugin.getCredentialDefinitions()).thenReturn(credentialVariables);

        assertThrows(CredentialVariablesNotComplete.class,
                () -> credentialCenter.checkInputCredentialIsValid(inputCredential));
    }

    @Test
    void testCheckInputCredentialIsValid_WithCredentialTypeValidIsMandatoryNotValid() {

        CreateCredential inputCredential = new CreateCredential();
        inputCredential.setCsp(Csp.OPENSTACK);
        inputCredential.setType(CredentialType.VARIABLES);
        inputCredential.setName(credentialName);
        inputCredential.setVariables(
                List.of(new CredentialVariable("name", "description", true, false),
                        new CredentialVariable("id1", "description", true, false)));

        final List<AbstractCredentialInfo> credentialVariables =
                List.of(new CredentialVariables(Csp.OPENSTACK, CredentialType.VARIABLES,
                        credentialName, "description", "userId",
                        List.of(new CredentialVariable("name", "description", false, false),
                                new CredentialVariable("id", "description", true, false))));
        when(mockPluginManager.getOrchestratorPlugin(inputCredential.getCsp())).thenReturn(
                orchestratorPlugin);
        when(orchestratorPlugin.getCredentialDefinitions()).thenReturn(credentialVariables);

        assertThrows(CredentialVariablesNotComplete.class,
                () -> credentialCenter.checkInputCredentialIsValid(inputCredential));
    }

    @Test
    void testCheckInputCredentialIsValid_WithNotMatchingCredential() {

        CreateCredential inputCredential = new CreateCredential();
        inputCredential.setCsp(Csp.OPENSTACK);
        inputCredential.setType(CredentialType.API_KEY);
        inputCredential.setName(credentialName);

        final List<AbstractCredentialInfo> credentialVariables =
                List.of(new CredentialVariables(Csp.OPENSTACK, CredentialType.VARIABLES,
                        credentialName, "description", "userId",
                        List.of(new CredentialVariable("name", "description", true, false),
                                new CredentialVariable("id", "description", true, false))));
        when(mockPluginManager.getOrchestratorPlugin(inputCredential.getCsp())).thenReturn(
                orchestratorPlugin);
        when(orchestratorPlugin.getCredentialDefinitions()).thenReturn(credentialVariables);


        assertThrows(CredentialCapabilityNotFound.class,
                () -> credentialCenter.checkInputCredentialIsValid(inputCredential));
    }

    @Test
    void testAddCredential_WithCredentialTypeValid() {
        CreateCredential inputCredential = new CreateCredential();
        inputCredential.setCsp(Csp.OPENSTACK);
        inputCredential.setType(CredentialType.VARIABLES);
        inputCredential.setName(credentialName);
        inputCredential.setUserId("userId");
        inputCredential.setTimeToLive(5);
        inputCredential.setDescription("user");
        inputCredential.setVariables(
                List.of(new CredentialVariable("name", "description", true, false, "value"),
                        new CredentialVariable("id", "description", true, false, "value")));

        CredentialVariables credentialVariable = new CredentialVariables(
                inputCredential.getCsp(), CredentialType.VARIABLES,
                inputCredential.getName(), inputCredential.getDescription(),
                inputCredential.getUserId(), inputCredential.getVariables());
        credentialVariable.setTimeToLive(5);

        final List<AbstractCredentialInfo> credentialVariables =
                List.of(new CredentialVariables(Csp.OPENSTACK, CredentialType.VARIABLES,
                        credentialName, "description", "userId",
                        new ArrayList<>()));

        List<CredentialType> credentialTypes = new ArrayList<>();
        credentialTypes.add(CredentialType.VARIABLES);
        when(mockPluginManager.getOrchestratorPlugin(inputCredential.getCsp())).thenReturn(
                orchestratorPlugin);
        when(orchestratorPlugin.getCredentialDefinitions()).thenReturn(credentialVariables);
        credentialCenter.checkInputCredentialIsValid(inputCredential);

        when(orchestratorPlugin.getAvailableCredentialTypes()).thenReturn(credentialTypes);
        credentialsStore.storeCredential(credentialVariable);
        credentialCenter.createCredential(credentialVariable);
        credentialCenter.addCredential(inputCredential);

        assertTrue(credentialTypes.contains(inputCredential.getType()));

        Assertions.assertTrue(Objects.nonNull(
                credentialCenter.getCredential(Csp.OPENSTACK, CredentialType.VARIABLES,
                        "user")));
    }

    @Test
    void testAddCredential_WithoutCredentialType() {
        CreateCredential inputCredential = new CreateCredential();
        inputCredential.setCsp(Csp.OPENSTACK);
        inputCredential.setType(CredentialType.API_KEY);
        inputCredential.setName(credentialName);
        inputCredential.setUserId("userId");
        inputCredential.setTimeToLive(5);
        inputCredential.setDescription("user");
        inputCredential.setVariables(
                List.of(new CredentialVariable("name", "description", true, false, "value"),
                        new CredentialVariable("id", "description", true, false, "value")));

        CredentialVariables credentialVariable = new CredentialVariables(
                inputCredential.getCsp(), CredentialType.API_KEY,
                inputCredential.getName(), inputCredential.getDescription(),
                inputCredential.getUserId(), inputCredential.getVariables());
        credentialVariable.setTimeToLive(5);

        final List<AbstractCredentialInfo> credentialVariables =
                List.of(new CredentialVariables(Csp.OPENSTACK, CredentialType.API_KEY,
                        credentialName, "description", "userId",
                        new ArrayList<>()));

        List<CredentialType> credentialTypes = new ArrayList<>();
        credentialTypes.add(CredentialType.VARIABLES);

        when(mockPluginManager.getOrchestratorPlugin(inputCredential.getCsp())).thenReturn(
                orchestratorPlugin);
        when(orchestratorPlugin.getCredentialDefinitions()).thenReturn(credentialVariables);
        credentialCenter.checkInputCredentialIsValid(inputCredential);

        when(orchestratorPlugin.getAvailableCredentialTypes()).thenReturn(credentialTypes);
        credentialsStore.storeCredential(credentialVariable);
        credentialCenter.createCredential(credentialVariable);

        assertThrows(CredentialCapabilityNotFound.class,
                () -> credentialCenter.addCredential(inputCredential));
    }

    @Test
    void testUpdateCredential_WithCredentialTypeValid() {
        CreateCredential inputCredential = new CreateCredential();
        inputCredential.setCsp(Csp.OPENSTACK);
        inputCredential.setType(CredentialType.VARIABLES);
        inputCredential.setName(credentialName);
        inputCredential.setUserId("userId");
        inputCredential.setTimeToLive(5);
        inputCredential.setDescription("user");
        inputCredential.setVariables(
                List.of(new CredentialVariable("name", "description", true, false, "value"),
                        new CredentialVariable("id", "description", true, false, "value")));

        CredentialVariables credentialVariable = new CredentialVariables(
                inputCredential.getCsp(), CredentialType.VARIABLES,
                inputCredential.getName(), inputCredential.getDescription(),
                inputCredential.getUserId(), inputCredential.getVariables());
        credentialVariable.setTimeToLive(5);

        final List<AbstractCredentialInfo> credentialVariables =
                List.of(new CredentialVariables(Csp.OPENSTACK, CredentialType.VARIABLES,
                        credentialName, "description", "userId",
                        new ArrayList<>()));

        List<CredentialType> credentialTypes = new ArrayList<>();
        credentialTypes.add(CredentialType.VARIABLES);
        when(mockPluginManager.getOrchestratorPlugin(inputCredential.getCsp())).thenReturn(
                orchestratorPlugin);
        when(orchestratorPlugin.getCredentialDefinitions()).thenReturn(credentialVariables);
        credentialCenter.checkInputCredentialIsValid(inputCredential);

        credentialsStore.deleteCredential(credentialVariable.getCsp(), credentialVariable.getType(),
                credentialVariable.getName(), credentialVariable.getUserId());

        credentialsStore.storeCredential(credentialVariable);
        credentialCenter.createCredential(credentialVariable);
        credentialCenter.updateCredential(inputCredential);


        Assertions.assertTrue(Objects.nonNull(
                credentialCenter.getCredential(Csp.OPENSTACK, CredentialType.VARIABLES,
                        "user")));
    }

    @Test
    void testUpdateCredential_WithoutCredentialType() {
        CreateCredential inputCredential = new CreateCredential();
        inputCredential.setCsp(Csp.OPENSTACK);
        inputCredential.setType(CredentialType.API_KEY);
        inputCredential.setName(credentialName);
        inputCredential.setUserId("userId");
        inputCredential.setTimeToLive(5);
        inputCredential.setDescription("user");
        inputCredential.setVariables(
                List.of(new CredentialVariable("name", "description", true, false, "value"),
                        new CredentialVariable("id", "description", true, false, "value")));

        CredentialVariables credentialVariable = new CredentialVariables(
                inputCredential.getCsp(), CredentialType.VARIABLES,
                inputCredential.getName(), inputCredential.getDescription(),
                inputCredential.getUserId(), inputCredential.getVariables());
        credentialVariable.setTimeToLive(5);

        final List<AbstractCredentialInfo> credentialVariables =
                List.of(new CredentialVariables(Csp.OPENSTACK, CredentialType.VARIABLES,
                        credentialName, "description", "userId",
                        new ArrayList<>()));

        List<CredentialType> credentialTypes = new ArrayList<>();
        credentialTypes.add(CredentialType.VARIABLES);

        when(mockPluginManager.getOrchestratorPlugin(inputCredential.getCsp())).thenReturn(
                orchestratorPlugin);
        when(orchestratorPlugin.getCredentialDefinitions()).thenReturn(credentialVariables);

        assertThrows(CredentialCapabilityNotFound.class,
                () -> credentialCenter.updateCredential(inputCredential));
    }
}
