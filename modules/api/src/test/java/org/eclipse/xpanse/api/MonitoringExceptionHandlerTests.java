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

import org.eclipse.xpanse.api.exceptions.CommonExceptionHandler;
import org.eclipse.xpanse.api.exceptions.MonitoringExceptionHandler;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.models.monitor.exceptions.ClientApiCallFailedException;
import org.eclipse.xpanse.modules.models.monitor.exceptions.ResourceNotFoundException;
import org.eclipse.xpanse.modules.models.monitor.exceptions.ResourceNotSupportedForMonitoringException;
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
@ContextConfiguration(classes = {CredentialManageApi.class, CommonExceptionHandler.class,
        CredentialCenter.class, MonitoringExceptionHandler.class, IdentityProviderManager.class})
@WebMvcTest
class MonitoringExceptionHandlerTests {

    @MockBean
    CredentialCenter credentialCenter;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();
    }

    @Test
    void testClientApiCallFailedException() throws Exception {
        when(credentialCenter.getCredentialsByUser(anyString())).thenThrow(
                new ClientApiCallFailedException(
                        "test error"));
        this.mockMvc.perform(
                        get("/xpanse/auth/user/credentials?userId=test"))
                .andExpect(status().is(502))
                .andExpect(jsonPath("$.resultType").value("Failure while connecting to backend"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testResourceNotSupportedForMonitoringException() throws Exception {
        when(credentialCenter.getCredentialsByUser(anyString())).thenThrow(
                new ResourceNotSupportedForMonitoringException(
                        "test error"));
        this.mockMvc.perform(
                        get("/xpanse/auth/user/credentials?userId=test"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Resource Invalid For Monitoring"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testResourceNotFoundException() throws Exception {
        when(credentialCenter.getCredentialsByUser(anyString())).thenThrow(
                new ResourceNotFoundException(
                        "test error"));
        this.mockMvc.perform(
                        get("/xpanse/auth/user/credentials?userId=test"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Resource Not Found"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }
}
