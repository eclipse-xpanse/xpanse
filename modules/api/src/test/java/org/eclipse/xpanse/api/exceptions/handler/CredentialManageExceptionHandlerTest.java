/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.exceptions.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import java.util.Set;
import org.eclipse.xpanse.api.config.CspPluginValidator;
import org.eclipse.xpanse.api.controllers.UserCloudCredentialsApi;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.models.credential.exceptions.CredentialCapabilityNotFound;
import org.eclipse.xpanse.modules.models.credential.exceptions.CredentialVariablesNotComplete;
import org.eclipse.xpanse.modules.models.credential.exceptions.CredentialsNotFoundException;
import org.eclipse.xpanse.modules.models.credential.exceptions.NoCredentialDefinitionAvailable;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.security.IdentityProviderManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {UserCloudCredentialsApi.class, CommonExceptionHandler.class,
        CredentialCenter.class, CredentialManageExceptionHandler.class,
        IdentityProviderManager.class, CspPluginValidator.class, PluginManager.class})
@WebMvcTest
class CredentialManageExceptionHandlerTest {
    private final String userId = "defaultUserId";
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext context;
    @MockBean
    private PluginManager pluginManager;
    @MockBean
    private CspPluginValidator cspPluginValidator;
    @MockBean
    private CredentialCenter credentialCenter;
    @MockBean
    private IdentityProviderManager identityProviderManager;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void testCredentialCapabilityNotFound() throws Exception {
        when(credentialCenter.listCredentials(any(), any(), anyString()))
                .thenThrow(new CredentialCapabilityNotFound("test error"));
        when(identityProviderManager.getCurrentLoginUserId()).thenReturn(Optional.of(userId));
        this.mockMvc.perform(get("/xpanse/user/credentials"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Credential Capability Not Found"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testCredentialsNotFoundException() throws Exception {
        when(credentialCenter.listCredentials(any(), any(), anyString()))
                .thenThrow(new CredentialsNotFoundException("test error"));
        when(identityProviderManager.getCurrentLoginUserId()).thenReturn(Optional.of(userId));
        this.mockMvc.perform(get("/xpanse/user/credentials"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Credentials Not Found"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testCredentialVariablesNotComplete() throws Exception {
        when(credentialCenter.listCredentials(any(), any(), anyString()))
                .thenThrow(new CredentialVariablesNotComplete(Set.of("test error")));
        when(identityProviderManager.getCurrentLoginUserId()).thenReturn(Optional.of(userId));
        this.mockMvc.perform(get("/xpanse/user/credentials"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Credential Variables Not Complete"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testNoCredentialDefinitionAvailable() throws Exception {
        when(credentialCenter.listCredentials(any(), any(), anyString()))
                .thenThrow(new NoCredentialDefinitionAvailable("test error"));
        when(identityProviderManager.getCurrentLoginUserId()).thenReturn(Optional.of(userId));
        this.mockMvc.perform(get("/xpanse/user/credentials"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("No Credential Definition Available"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }


    @Test
    void testHandleUserNoLoginException() throws Exception {
        when(credentialCenter.listCredentials(any(), any(), anyString()))
                .thenThrow(new NoCredentialDefinitionAvailable("test error"));
        this.mockMvc.perform(get("/xpanse/user/credentials"))
                .andExpect(status().is(401))
                .andExpect(jsonPath("$.resultType").value("Current Login User No Found"))
                .andExpect(
                        jsonPath("$.details[0]").value("Unable to get current login information"));
    }
}
