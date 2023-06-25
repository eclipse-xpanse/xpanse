/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;
import org.eclipse.xpanse.api.exceptions.CommonExceptionHandler;
import org.eclipse.xpanse.api.exceptions.CredentialsExceptionHandler;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.models.credential.exceptions.CredentialCapabilityNotFound;
import org.eclipse.xpanse.modules.models.credential.exceptions.CredentialVariablesNotComplete;
import org.eclipse.xpanse.modules.models.credential.exceptions.CredentialsNotFoundException;
import org.eclipse.xpanse.modules.models.credential.exceptions.NoCredentialDefinitionAvailable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CredentialManageApi.class, CommonExceptionHandler.class,
        CredentialCenter.class, CredentialsExceptionHandler.class})
@WebMvcTest
class CredentialsExceptionHandlerTests {

    @MockBean
    CredentialCenter credentialCenter;

    @InjectMocks
    CredentialManageApi credentialManageApi;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testCredentialCapabilityNotFound() throws Exception {
        when(credentialCenter.getCredentialsByUser(anyString())).thenThrow(
                new CredentialCapabilityNotFound(
                        "test error"));
        this.mockMvc.perform(
                        get("/xpanse/auth/user/credentials?userName=test"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Credential Capability Not Found"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testCredentialsNotFoundException() throws Exception {
        when(credentialCenter.getCredentialsByUser(anyString())).thenThrow(
                new CredentialsNotFoundException(
                        "test error"));
        this.mockMvc.perform(
                        get("/xpanse/auth/user/credentials?userName=test"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Credentials Not Found"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testCredentialVariablesNotComplete() throws Exception {
        when(credentialCenter.getCredentialsByUser(anyString())).thenThrow(
                new CredentialVariablesNotComplete(
                        Set.of("test error")));
        this.mockMvc.perform(
                        get("/xpanse/auth/user/credentials?userName=test"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Credential Variables Not Complete"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testNoCredentialDefinitionAvailable() throws Exception {
        when(credentialCenter.getCredentialsByUser(anyString())).thenThrow(
                new NoCredentialDefinitionAvailable(
                        "test error"));
        this.mockMvc.perform(
                        get("/xpanse/auth/user/credentials?userName=test"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("No Credential Definition Available"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }
}
