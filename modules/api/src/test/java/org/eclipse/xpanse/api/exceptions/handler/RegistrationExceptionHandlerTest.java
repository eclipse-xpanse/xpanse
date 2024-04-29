/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.exceptions.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.eclipse.xpanse.api.config.CspPluginValidator;
import org.eclipse.xpanse.api.controllers.ServiceTemplateApi;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.IconProcessingFailedException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.InvalidServiceFlavorsException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.InvalidServiceVersionException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.InvalidValueSchemaException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateAlreadyRegistered;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateAlreadyReviewed;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotApproved;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotRegistered;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateUpdateNotAllowed;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.TerraformScriptFormatInvalidException;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.security.IdentityProviderManager;
import org.eclipse.xpanse.modules.servicetemplate.ServiceTemplateManage;
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
@ContextConfiguration(classes = {ServiceTemplateApi.class, ServiceTemplateManage.class,
        RegistrationExceptionHandler.class, IdentityProviderManager.class, OclLoader.class,
        CspPluginValidator.class, PluginManager.class})
@WebMvcTest
class RegistrationExceptionHandlerTest {

    private final String oclLocation = "file:src/test/resources/ocl_terraform_test.yml";
    @MockBean
    private ServiceTemplateManage serviceTemplateManage;
    @MockBean
    private PluginManager pluginManager;
    @MockBean
    private CspPluginValidator cspPluginValidator;
    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void testTerraformScriptFormatInvalidException() throws Exception {
        when(serviceTemplateManage.registerServiceTemplate(any())).thenThrow(
                new TerraformScriptFormatInvalidException(List.of("test error")));

        this.mockMvc.perform(
                        post("/xpanse/service_templates/file").param("oclLocation", oclLocation))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Terraform Script Invalid"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testServiceTemplateAlreadyRegistered() throws Exception {
        when(serviceTemplateManage.registerServiceTemplate(any())).thenThrow(
                new ServiceTemplateAlreadyRegistered("test error"));

        this.mockMvc.perform(
                        post("/xpanse/service_templates/file").param("oclLocation", oclLocation))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Service Template Already Registered"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testIconProcessingFailedException() throws Exception {
        when(serviceTemplateManage.registerServiceTemplate(any())).thenThrow(
                new IconProcessingFailedException("test error"));

        this.mockMvc.perform(
                        post("/xpanse/service_templates/file").param("oclLocation", oclLocation))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Icon Processing Failed"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testServiceTemplateNotRegistered() throws Exception {
        when(serviceTemplateManage.registerServiceTemplate(any())).thenThrow(
                new ServiceTemplateNotRegistered("test error"));

        this.mockMvc.perform(
                        post("/xpanse/service_templates/file").param("oclLocation", oclLocation))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Service Template Not Registered"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testServiceTemplateNotApproved() throws Exception {
        when(serviceTemplateManage.registerServiceTemplate(any())).thenThrow(
                new ServiceTemplateNotApproved("test error"));

        this.mockMvc.perform(
                        post("/xpanse/service_templates/file").param("oclLocation", oclLocation))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Service Template Not Approved"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testServiceTemplateAlreadyReviewed() throws Exception {
        when(serviceTemplateManage.registerServiceTemplate(any())).thenThrow(
                new ServiceTemplateAlreadyReviewed("test error"));

        this.mockMvc.perform(
                        post("/xpanse/service_templates/file").param("oclLocation", oclLocation))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Service Template Already Reviewed"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testServiceTemplateUpdateNotAllowed() throws Exception {
        when(serviceTemplateManage.registerServiceTemplate(any())).thenThrow(
                new ServiceTemplateUpdateNotAllowed("test error"));

        this.mockMvc.perform(
                        post("/xpanse/service_templates/file").param("oclLocation", oclLocation))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Service Template Update Not Allowed"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testInvalidValueSchemaException() throws Exception {
        when(serviceTemplateManage.registerServiceTemplate(any())).thenThrow(
                new InvalidValueSchemaException(List.of("test error")));

        this.mockMvc.perform(
                        post("/xpanse/service_templates/file").param("oclLocation", oclLocation))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Variable Schema Definition Invalid"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testInvalidServiceVersionException() throws Exception {
        when(serviceTemplateManage.registerServiceTemplate(any())).thenThrow(
                new InvalidServiceVersionException("test error"));

        this.mockMvc.perform(
                        post("/xpanse/service_templates/file").param("oclLocation", oclLocation))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Invalid Service Version"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testInvalidServiceFlavorsException() throws Exception {
        when(serviceTemplateManage.registerServiceTemplate(any())).thenThrow(
                new InvalidServiceFlavorsException(List.of("test error")));

        this.mockMvc.perform(
                        post("/xpanse/service_templates/file").param("oclLocation", oclLocation))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Invalid Service Flavors"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }
}
