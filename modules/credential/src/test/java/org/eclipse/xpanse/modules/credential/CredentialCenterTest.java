/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.credential;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.security.config.AesUtil;
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
                credentialCenter.getAvailableCredentialTypesByCsp(csp);

        assertEquals(expectedCredentialTypes, actualCredentialTypes);
    }

    @Test
    public void testGetCredentialCapabilitiesByCsp_WithValidType() {
        Csp csp = Csp.OPENSTACK;
        CredentialType credentialType = CredentialType.VARIABLES;
        final List<AbstractCredentialInfo> credentialVariables =
                List.of(new CredentialVariables(Csp.OPENSTACK, "userName", "name",
                        "description",
                        CredentialType.VARIABLES,
                        List.of(new CredentialVariable("name", "description", true, false),
                                new CredentialVariable("id", "description", true, false))));
        when(mockPluginManager.getOrchestratorPlugin(csp)).thenReturn(orchestratorPlugin);
        when(orchestratorPlugin.getCredentialDefinitions()).thenReturn(credentialVariables);

        List<AbstractCredentialInfo> credentials =
                this.credentialCenter.getCredentialCapabilitiesByCsp(csp, credentialType);

        Assertions.assertTrue(Objects.nonNull(credentials));
        assertEquals(credentialVariables, credentials);
    }

    @Test
    public void testGetCredentialCapabilitiesByCsp_WithNullType() {
        Csp csp = Csp.OPENSTACK;
        final List<AbstractCredentialInfo> credentialVariables =
                List.of(new CredentialVariables(Csp.OPENSTACK, "userName", "name",
                        "description",
                        CredentialType.VARIABLES,
                        List.of(new CredentialVariable("name", "description", true, false),
                                new CredentialVariable("id", "description", true, false))));
        when(mockPluginManager.getOrchestratorPlugin(csp)).thenReturn(orchestratorPlugin);
        when(orchestratorPlugin.getCredentialDefinitions()).thenReturn(credentialVariables);

        List<AbstractCredentialInfo> credentials =
                credentialCenter.getCredentialCapabilitiesByCsp(csp, null);

        assertEquals(credentialVariables, credentials);
    }

    @Test
    public void testGetCredentialCapabilitiesByCsp_WithNoMatchingType() {
        Csp csp = Csp.OPENSTACK;
        CredentialType credentialType = CredentialType.API_KEY;
        final List<AbstractCredentialInfo> credentialVariables =
                List.of(new CredentialVariables(Csp.OPENSTACK, "userName", "name",
                        "description",
                        CredentialType.VARIABLES,
                        List.of(new CredentialVariable("name", "description", true, false),
                                new CredentialVariable("id", "description", true, false))));
        when(mockPluginManager.getOrchestratorPlugin(csp)).thenReturn(orchestratorPlugin);
        when(orchestratorPlugin.getCredentialDefinitions()).thenReturn(credentialVariables);

        List<AbstractCredentialInfo> credentials =
                credentialCenter.getCredentialCapabilitiesByCsp(csp, credentialType);

        assertEquals(Collections.emptyList(), credentials);
    }

    @Test
    public void testGetCredentialOpenApiUrl() {
        Csp csp = Csp.OPENSTACK;
        CredentialType credentialType = CredentialType.VARIABLES;
        String expectedUrl = "http://example.com/credential/openapi";

        when(credentialOpenApiGenerator.getCredentialOpenApiUrl(csp, credentialType))
                .thenReturn(expectedUrl);

        String credentialOpenApiUrl = credentialCenter.getCredentialOpenApiUrl(csp, credentialType);

        assertEquals(expectedUrl, credentialOpenApiUrl);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetCredentialsByUser_WithValidUser() {
        String xpanseUser = "userName";

        Csp csp1 = Csp.OPENSTACK;
        Csp csp2 = Csp.FLEXIBLE_ENGINE;
        Csp csp3 = Csp.HUAWEI;
        CredentialType type = CredentialType.VARIABLES;

        final AbstractCredentialInfo credential1 =
                new CredentialVariables(Csp.OPENSTACK, xpanseUser, "name",
                        "description",
                        CredentialType.VARIABLES,
                        List.of(new CredentialVariable("name", "description1", true, false),
                                new CredentialVariable("id", "description", true, false)));
        final AbstractCredentialInfo credential2 =
                new CredentialVariables(Csp.OPENSTACK, xpanseUser, "name",
                        "description",
                        CredentialType.VARIABLES,
                        List.of(new CredentialVariable("name", "description1", true, false),
                                new CredentialVariable("id", "description", true, false)));
        final AbstractCredentialInfo credential3 =
                new CredentialVariables(Csp.FLEXIBLE_ENGINE, xpanseUser, "name",
                        "description",
                        CredentialType.VARIABLES,
                        List.of(new CredentialVariable("name", "description2", true, false),
                                new CredentialVariable("id", "description", true, false)));
        final AbstractCredentialInfo credential4 =
                new CredentialVariables(Csp.HUAWEI, xpanseUser, "name",
                        "description",
                        CredentialType.VARIABLES,
                        List.of(new CredentialVariable("name", "description3", true, false),
                                new CredentialVariable("id", "description", true, false)));

        HashMap<Csp, OrchestratorPlugin> cspMap = new HashMap<>();
        cspMap.put(csp1, mockPluginManager.getOrchestratorPlugin(csp1));
        cspMap.put(csp2, mockPluginManager.getOrchestratorPlugin(csp2));
        cspMap.put(csp3, mockPluginManager.getOrchestratorPlugin(csp3));
        List<AbstractCredentialInfo> credentials3 = Arrays.asList(credential1, credential2);
        List<AbstractCredentialInfo> credentials4 = Collections.singletonList(credential3);
        List<AbstractCredentialInfo> expectedCredentials =
                Arrays.asList(credential1, credential2, credential3, credential4);

        when(mockPluginManager.getPluginsMap()).thenReturn(cspMap);
        when(mockPluginManager.getOrchestratorPlugin(csp1)).thenReturn(orchestratorPlugin);
        when(mockPluginManager.getOrchestratorPlugin(csp2)).thenReturn(orchestratorPlugin);
        when(mockPluginManager.getOrchestratorPlugin(csp3)).thenReturn(orchestratorPlugin);
        when(orchestratorPlugin.getCredentialDefinitions()).thenReturn(credentials3, credentials4);
        when(credentialsStore.getCredential(csp1, type, xpanseUser)).thenReturn(credential1);
        when(credentialsStore.getCredential(csp2, type, xpanseUser)).thenReturn(credential3);
        when(credentialsStore.getCredential(csp3, type, xpanseUser)).thenReturn(credential4);

        // Call the method being tested
        List<AbstractCredentialInfo> result = credentialCenter.getCredentialsByUser(xpanseUser);

        Assertions.assertTrue(Objects.nonNull(result));
        assertEquals(expectedCredentials.size(), result.size());
    }

    @Test
    public void testGetCredentialsByUser_WithNullUser() {

        List<AbstractCredentialInfo> credentials = credentialCenter.getCredentialsByUser("");

        assertEquals(Collections.emptyList(), credentials);
    }

    @Test
    public void testGetCredentialsByUser_WithNoMatchingUser() {
        String xpanseUser1 = "user1";
        String xpanseUser2 = "user2";

        List<AbstractCredentialInfo> credentials1 =
                credentialCenter.getCredentialsByUser(xpanseUser2);
        List<AbstractCredentialInfo> credentials2 =
                credentialCenter.getCredentialsByUser(xpanseUser1);

        assertEquals(Collections.emptyList(), credentials1);
        assertEquals(Collections.emptyList(), credentials2);
    }

    @Test
    void testGetCredentials_WithRequestedCredentialType() {
        Csp csp = Csp.OPENSTACK;
        String xpanseUser = "user1";
        CredentialType requestedCredentialType = CredentialType.VARIABLES;

        final AbstractCredentialInfo credential =
                new CredentialVariables(csp, xpanseUser, "name",
                        "description",
                        CredentialType.VARIABLES,
                        List.of(new CredentialVariable("name", "description1", true, false),
                                new CredentialVariable("id", "description", true, false)));
        when(credentialsStore.getCredential(csp, requestedCredentialType, xpanseUser))
                .thenReturn(credential);


        List<AbstractCredentialInfo> result = credentialCenter.getCredentials(
                csp, xpanseUser, requestedCredentialType);


        credentialsStore.getCredential(csp, requestedCredentialType, xpanseUser);

        assertNotEquals(Collections.singletonList(credential), result);

        AbstractCredentialInfo abstractCredentialInfoResult = result.get(0);

        assertEquals(credential.getCsp(), abstractCredentialInfoResult.getCsp());
        assertEquals(credential.getXpanseUser(), abstractCredentialInfoResult.getXpanseUser());
        assertEquals(credential.getName(), abstractCredentialInfoResult.getName());
        assertEquals(credential.getDescription(), abstractCredentialInfoResult.getDescription());
        assertEquals(credential.getType(), abstractCredentialInfoResult.getType());
    }

    @Test
    void testGetCredentials_WithoutRequestedCredentialType() {
        Csp csp = Csp.OPENSTACK;
        String xpanseUser = "user1";
        CredentialType requestedCredentialType = null;

        when(mockPluginManager.getOrchestratorPlugin(csp)).thenReturn(orchestratorPlugin);

        final List<AbstractCredentialInfo> credentialVariables =
                List.of(new CredentialVariables(Csp.OPENSTACK, xpanseUser, "name",
                        "description",
                        CredentialType.VARIABLES,
                        List.of(new CredentialVariable("name", "description", true, false),
                                new CredentialVariable("id", "description", true, false))));
        when(orchestratorPlugin.getCredentialDefinitions()).thenReturn(credentialVariables);

        final AbstractCredentialInfo credential1 =
                new CredentialVariables(Csp.OPENSTACK, xpanseUser, "name",
                        "description",
                        CredentialType.VARIABLES,
                        List.of(new CredentialVariable("name", "description", true, false),
                                new CredentialVariable("id", "description", true, false)));

        when(credentialsStore.getCredential(csp, CredentialType.VARIABLES, xpanseUser))
                .thenReturn(credential1);


        List<AbstractCredentialInfo> result = credentialCenter.getCredentials(
                csp, xpanseUser, requestedCredentialType);

        mockPluginManager.getOrchestratorPlugin(csp);

        orchestratorPlugin.getCredentialDefinitions();

        credentialsStore.getCredential(csp, CredentialType.VARIABLES, xpanseUser);

        Assertions.assertNotEquals(List.of(credential1), result);
        AbstractCredentialInfo abstractCredentialInfoResult = result.get(0);

        assertEquals(credential1.getCsp(), abstractCredentialInfoResult.getCsp());
        assertEquals(credential1.getXpanseUser(), abstractCredentialInfoResult.getXpanseUser());
        assertEquals(credential1.getName(), abstractCredentialInfoResult.getName());
        assertEquals(credential1.getDescription(), abstractCredentialInfoResult.getDescription());
        assertEquals(credential1.getType(), abstractCredentialInfoResult.getType());
    }

    @Test
    void testGetCredentials_WithoutXpanseUser() {
        Csp csp = Csp.OPENSTACK;
        String xpanseUser = "";
        CredentialType requestedCredentialType = CredentialType.VARIABLES;

        List<AbstractCredentialInfo> result = credentialCenter.getCredentials(
                csp, xpanseUser, requestedCredentialType);

        assertEquals(Collections.emptyList(), result);
    }

    @Test
    void testDeleteCredential() {
        Csp csp = Csp.OPENSTACK;
        String xpanseUser = "user123";
        CredentialType credentialType = CredentialType.API_KEY;

        credentialCenter.deleteCredential(csp, xpanseUser, credentialType);

        verify(credentialsStore).deleteCredential(eq(csp), eq(credentialType), eq(xpanseUser));
    }

    @Test
    void testDeleteCredentialByType() {
        Csp csp = Csp.OPENSTACK;
        String xpanseUser = "user456";
        CredentialType credentialType = CredentialType.VARIABLES;

        credentialCenter.deleteCredentialByType(csp, xpanseUser, credentialType);

        verify(credentialsStore).deleteCredential(eq(csp), eq(credentialType), eq(xpanseUser));
    }

    @Test
    void testCreateCredential() {
        final AbstractCredentialInfo credentialInfo =
                new CredentialVariables(Csp.OPENSTACK, "userName", "name",
                        "description",
                        CredentialType.VARIABLES,
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
        inputCredential.setName("name");
        inputCredential.setVariables(
                List.of(new CredentialVariable("name", "description", true, false, "value"),
                        new CredentialVariable("id", "description", true, false, "value")));

        final List<AbstractCredentialInfo> credentialVariables =
                List.of(new CredentialVariables(Csp.OPENSTACK, "userName", "name",
                        "description",
                        CredentialType.VARIABLES,
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
        inputCredential.setName("name");
        inputCredential.setVariables(
                List.of(new CredentialVariable("name", "description", true, false, "value"),
                        new CredentialVariable("id", "description", true, false, "value")));

        final List<AbstractCredentialInfo> credentialVariables =
                List.of(new CredentialVariables(Csp.OPENSTACK, "userName", "name",
                        "description",
                        CredentialType.API_KEY,
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
                List.of(new CredentialVariables(Csp.OPENSTACK, "userName", "name",
                        "description",
                        CredentialType.VARIABLES,
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
        inputCredential.setName("name");
        inputCredential.setVariables(
                List.of(new CredentialVariable("name", "description", true, false),
                        new CredentialVariable("id1", "description", true, false)));

        final List<AbstractCredentialInfo> credentialVariables =
                List.of(new CredentialVariables(Csp.OPENSTACK, "userName", "name",
                        "description",
                        CredentialType.VARIABLES,
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
        inputCredential.setName("name");
        inputCredential.setVariables(
                List.of(new CredentialVariable("name", "description", true, false),
                        new CredentialVariable("id1", "description", true, false)));

        final List<AbstractCredentialInfo> credentialVariables =
                List.of(new CredentialVariables(Csp.OPENSTACK, "userName", "name",
                        "description",
                        CredentialType.VARIABLES,
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
        inputCredential.setName("name");

        final List<AbstractCredentialInfo> credentialVariables =
                List.of(new CredentialVariables(Csp.OPENSTACK, "userName", "name",
                        "description",
                        CredentialType.VARIABLES,
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
        inputCredential.setName("name");
        inputCredential.setXpanseUser("user");
        inputCredential.setTimeToLive(5);
        inputCredential.setDescription("user");
        inputCredential.setVariables(
                List.of(new CredentialVariable("name", "description", true, false, "value"),
                        new CredentialVariable("id", "description", true, false, "value")));

        CredentialVariables credentialVariable = new CredentialVariables(
                inputCredential.getCsp(), inputCredential.getXpanseUser(),
                inputCredential.getName(), inputCredential.getDescription(),
                CredentialType.VARIABLES, inputCredential.getVariables());
        credentialVariable.setTimeToLive(5);

        final List<AbstractCredentialInfo> credentialVariables =
                List.of(new CredentialVariables(Csp.OPENSTACK, "userName", "name",
                        "description",
                        CredentialType.VARIABLES,
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
                credentialCenter.getCredential(Csp.OPENSTACK, "user", CredentialType.VARIABLES)));
    }

    @Test
    void testAddCredential_WithoutCredentialType() {
        CreateCredential inputCredential = new CreateCredential();
        inputCredential.setCsp(Csp.OPENSTACK);
        inputCredential.setType(CredentialType.API_KEY);
        inputCredential.setName("name");
        inputCredential.setXpanseUser("user");
        inputCredential.setTimeToLive(5);
        inputCredential.setDescription("user");
        inputCredential.setVariables(
                List.of(new CredentialVariable("name", "description", true, false, "value"),
                        new CredentialVariable("id", "description", true, false, "value")));

        CredentialVariables credentialVariable = new CredentialVariables(
                inputCredential.getCsp(), inputCredential.getXpanseUser(),
                inputCredential.getName(), inputCredential.getDescription(),
                CredentialType.API_KEY, inputCredential.getVariables());
        credentialVariable.setTimeToLive(5);

        final List<AbstractCredentialInfo> credentialVariables =
                List.of(new CredentialVariables(Csp.OPENSTACK, "userName", "name",
                        "description",
                        CredentialType.API_KEY,
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
        inputCredential.setName("name");
        inputCredential.setXpanseUser("user");
        inputCredential.setTimeToLive(5);
        inputCredential.setDescription("user");
        inputCredential.setVariables(
                List.of(new CredentialVariable("name", "description", true, false, "value"),
                        new CredentialVariable("id", "description", true, false, "value")));

        CredentialVariables credentialVariable = new CredentialVariables(
                inputCredential.getCsp(), inputCredential.getXpanseUser(),
                inputCredential.getName(), inputCredential.getDescription(),
                CredentialType.VARIABLES, inputCredential.getVariables());
        credentialVariable.setTimeToLive(5);

        final List<AbstractCredentialInfo> credentialVariables =
                List.of(new CredentialVariables(Csp.OPENSTACK, "userName", "name",
                        "description",
                        CredentialType.VARIABLES,
                        new ArrayList<>()));

        List<CredentialType> credentialTypes = new ArrayList<>();
        credentialTypes.add(CredentialType.VARIABLES);
        when(mockPluginManager.getOrchestratorPlugin(inputCredential.getCsp())).thenReturn(
                orchestratorPlugin);
        when(orchestratorPlugin.getCredentialDefinitions()).thenReturn(credentialVariables);
        credentialCenter.checkInputCredentialIsValid(inputCredential);

        credentialsStore.deleteCredential(credentialVariable.getCsp(), credentialVariable.getType(),
                credentialVariable.getXpanseUser());

        credentialsStore.storeCredential(credentialVariable);
        credentialCenter.createCredential(credentialVariable);
        credentialCenter.updateCredential(inputCredential);


        Assertions.assertTrue(Objects.nonNull(
                credentialCenter.getCredential(Csp.OPENSTACK, "user", CredentialType.VARIABLES)));
    }

    @Test
    void testUpdateCredential_WithoutCredentialType() {
        CreateCredential inputCredential = new CreateCredential();
        inputCredential.setCsp(Csp.OPENSTACK);
        inputCredential.setType(CredentialType.API_KEY);
        inputCredential.setName("name");
        inputCredential.setXpanseUser("user");
        inputCredential.setTimeToLive(5);
        inputCredential.setDescription("user");
        inputCredential.setVariables(
                List.of(new CredentialVariable("name", "description", true, false, "value"),
                        new CredentialVariable("id", "description", true, false, "value")));

        CredentialVariables credentialVariable = new CredentialVariables(
                inputCredential.getCsp(), inputCredential.getXpanseUser(),
                inputCredential.getName(), inputCredential.getDescription(),
                CredentialType.API_KEY, inputCredential.getVariables());
        credentialVariable.setTimeToLive(5);

        final List<AbstractCredentialInfo> credentialVariables =
                List.of(new CredentialVariables(Csp.OPENSTACK, "userName", "name",
                        "description",
                        CredentialType.API_KEY,
                        new ArrayList<>()));

        List<CredentialType> credentialTypes = new ArrayList<>();
        credentialTypes.add(CredentialType.VARIABLES);

        when(mockPluginManager.getOrchestratorPlugin(inputCredential.getCsp())).thenReturn(
                orchestratorPlugin);
        when(orchestratorPlugin.getCredentialDefinitions()).thenReturn(credentialVariables);
        credentialCenter.checkInputCredentialIsValid(inputCredential);

        credentialsStore.storeCredential(credentialVariable);
        credentialCenter.createCredential(credentialVariable);

        assertThrows(CredentialCapabilityNotFound.class,
                () -> credentialCenter.updateCredential(inputCredential));
    }
}
