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

import java.util.List;
import org.eclipse.xpanse.api.exceptions.CommonExceptionHandler;
import org.eclipse.xpanse.api.exceptions.RegistrationExceptionHandler;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.models.service.register.exceptions.IconProcessingFailedException;
import org.eclipse.xpanse.modules.models.service.register.exceptions.ServiceAlreadyRegisteredException;
import org.eclipse.xpanse.modules.models.service.register.exceptions.ServiceNotRegisteredException;
import org.eclipse.xpanse.modules.models.service.register.exceptions.ServiceUpdateNotAllowed;
import org.eclipse.xpanse.modules.models.service.register.exceptions.TerraformScriptFormatInvalidException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
        CredentialCenter.class, RegistrationExceptionHandler.class})
@WebMvcTest
class RegistrationExceptionHandlerTests {

    @MockBean
    CredentialCenter credentialCenter;

    @InjectMocks
    CredentialManageApi credentialManageApi;

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
    void testTerraformScriptFormatInvalidException() throws Exception {
        when(credentialCenter.getCredentialsByUser(anyString())).thenThrow(
                new TerraformScriptFormatInvalidException(
                        List.of("test error")));
        this.mockMvc.perform(
                        get("/xpanse/auth/user/credentials?userName=test"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Terraform Script Invalid"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testServiceAlreadyRegisteredException() throws Exception {
        when(credentialCenter.getCredentialsByUser(anyString())).thenThrow(
                new ServiceAlreadyRegisteredException(
                        "test error"));
        this.mockMvc.perform(
                        get("/xpanse/auth/user/credentials?userName=test"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Service Already Registered"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testIconProcessingFailedException() throws Exception {
        when(credentialCenter.getCredentialsByUser(anyString())).thenThrow(
                new IconProcessingFailedException(
                        "test error"));
        this.mockMvc.perform(
                        get("/xpanse/auth/user/credentials?userName=test"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Icon Processing Failed"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testServiceNotRegisteredException() throws Exception {
        when(credentialCenter.getCredentialsByUser(anyString())).thenThrow(
                new ServiceNotRegisteredException(
                        "test error"));
        this.mockMvc.perform(
                        get("/xpanse/auth/user/credentials?userName=test"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Service Not Registered"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testServiceUpdateNotAllowed() throws Exception {
        when(credentialCenter.getCredentialsByUser(anyString())).thenThrow(
                new ServiceUpdateNotAllowed(
                        "test error"));
        this.mockMvc.perform(
                        get("/xpanse/auth/user/credentials?userName=test"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Service Update Not Allowed"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }
}
