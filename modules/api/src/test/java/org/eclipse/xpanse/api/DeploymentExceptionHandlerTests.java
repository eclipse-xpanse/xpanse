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
import org.eclipse.xpanse.api.exceptions.DeploymentExceptionHandler;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.DeployerNotFoundException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.FlavorInvalidException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.InvalidDeploymentVariableException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.InvalidServiceStateException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.PluginNotFoundException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.TerraformExecutorException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.TerraformProviderNotFoundException;
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
        CredentialCenter.class, DeploymentExceptionHandler.class})
@WebMvcTest
class DeploymentExceptionHandlerTests {

    @MockBean
    CredentialCenter credentialCenter;

    @InjectMocks
    CredentialManageApi credentialManageApi;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testFlavorInvalidException() throws Exception {
        when(credentialCenter.getCredentialsByUser(anyString())).thenThrow(
                new FlavorInvalidException(
                        "test error"));
        this.mockMvc.perform(
                        get("/xpanse/auth/user/credentials?userName=test"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Flavor Invalid"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testTerraformExecutorException() throws Exception {
        when(credentialCenter.getCredentialsByUser(anyString())).thenThrow(
                new TerraformExecutorException(
                        "test error"));
        this.mockMvc.perform(
                        get("/xpanse/auth/user/credentials?userName=test"))
                .andExpect(status().is(502))
                .andExpect(jsonPath("$.resultType").value("Terraform Execution Failed"))
                .andExpect(jsonPath("$.details[0]").value("TFExecutor Exception: test error"));
    }

    @Test
    void testPluginNotFoundException() throws Exception {
        when(credentialCenter.getCredentialsByUser(anyString())).thenThrow(
                new PluginNotFoundException(
                        "test error"));
        this.mockMvc.perform(
                        get("/xpanse/auth/user/credentials?userName=test"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Plugin Not Found"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testDeployerNotFoundException() throws Exception {
        when(credentialCenter.getCredentialsByUser(anyString())).thenThrow(
                new DeployerNotFoundException(
                        "test error"));
        this.mockMvc.perform(
                        get("/xpanse/auth/user/credentials?userName=test"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Deployer Not Found"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testTerraformProviderNotFoundException() throws Exception {
        when(credentialCenter.getCredentialsByUser(anyString())).thenThrow(
                new TerraformProviderNotFoundException(
                        "test error"));
        this.mockMvc.perform(
                        get("/xpanse/auth/user/credentials?userName=test"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Terraform Provider Not Found"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testInvalidServiceStateException() throws Exception {
        when(credentialCenter.getCredentialsByUser(anyString())).thenThrow(
                new InvalidServiceStateException(
                        "test error"));
        this.mockMvc.perform(
                        get("/xpanse/auth/user/credentials?userName=test"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Invalid Service State"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testServiceNotDeployedException() throws Exception {
        when(credentialCenter.getCredentialsByUser(anyString())).thenThrow(
                new ServiceNotDeployedException(
                        "test error"));
        this.mockMvc.perform(
                        get("/xpanse/auth/user/credentials?userName=test"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Service Deployment Not Found"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testInvalidDeploymentVariableException() throws Exception {
        when(credentialCenter.getCredentialsByUser(anyString())).thenThrow(
                new InvalidDeploymentVariableException(
                        "test error"));
        this.mockMvc.perform(
                        get("/xpanse/auth/user/credentials?userName=test"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Deployment Variable Invalid"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }
}
