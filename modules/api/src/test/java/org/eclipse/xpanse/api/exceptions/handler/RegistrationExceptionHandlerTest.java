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
import org.eclipse.xpanse.modules.models.billing.exceptions.InvalidBillingConfigException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.IconProcessingFailedException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.InvalidServiceFlavorsException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.InvalidServiceVersionException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.InvalidValueSchemaException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotRegistered;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateUnavailableException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.TerraformScriptFormatInvalidException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.UnavailableServiceRegionsException;
import org.eclipse.xpanse.modules.models.servicetemplate.request.exceptions.ServiceTemplateRequestNotAllowed;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.security.IdentityProviderManager;
import org.eclipse.xpanse.modules.servicetemplate.ServiceTemplateManage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {
            ServiceTemplateApi.class,
            ServiceTemplateManage.class,
            RegistrationExceptionHandler.class,
            IdentityProviderManager.class,
            OclLoader.class,
            CspPluginValidator.class,
            PluginManager.class
        })
@WebMvcTest
class RegistrationExceptionHandlerTest {

    private final String oclLocation = "file:src/test/resources/ocl_terraform_test.yml";
    @MockitoBean private ServiceTemplateManage serviceTemplateManage;
    @MockitoBean private PluginManager pluginManager;
    @MockitoBean private CspPluginValidator cspPluginValidator;
    @Autowired private WebApplicationContext context;
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void testTerraformScriptFormatInvalidException() throws Exception {
        when(serviceTemplateManage.registerServiceTemplate(any()))
                .thenThrow(new TerraformScriptFormatInvalidException(List.of("test error")));

        this.mockMvc
                .perform(post("/xpanse/service_templates/file").param("oclLocation", oclLocation))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errorType").value("Terraform Script Invalid"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testIconProcessingFailedException() throws Exception {
        when(serviceTemplateManage.registerServiceTemplate(any()))
                .thenThrow(new IconProcessingFailedException("test error"));

        this.mockMvc
                .perform(post("/xpanse/service_templates/file").param("oclLocation", oclLocation))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errorType").value("Icon Processing Failed"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testServiceTemplateNotRegistered() throws Exception {
        when(serviceTemplateManage.registerServiceTemplate(any()))
                .thenThrow(new ServiceTemplateNotRegistered("test error"));

        this.mockMvc
                .perform(post("/xpanse/service_templates/file").param("oclLocation", oclLocation))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errorType").value("Service Template Not Registered"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testUnavailableServiceTemplateException() throws Exception {
        when(serviceTemplateManage.registerServiceTemplate(any()))
                .thenThrow(new ServiceTemplateUnavailableException("test error"));

        this.mockMvc
                .perform(post("/xpanse/service_templates/file").param("oclLocation", oclLocation))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errorType").value("Service Template Unavailable"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testServiceTemplateChangeRequestNotAllowed() throws Exception {
        when(serviceTemplateManage.registerServiceTemplate(any()))
                .thenThrow(new ServiceTemplateRequestNotAllowed("test error"));

        this.mockMvc
                .perform(post("/xpanse/service_templates/file").param("oclLocation", oclLocation))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errorType").value("Service Template Request Not Allowed"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testInvalidValueSchemaException() throws Exception {
        when(serviceTemplateManage.registerServiceTemplate(any()))
                .thenThrow(new InvalidValueSchemaException(List.of("test error")));

        this.mockMvc
                .perform(post("/xpanse/service_templates/file").param("oclLocation", oclLocation))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errorType").value("Variable Schema Definition Invalid"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testInvalidServiceVersionException() throws Exception {
        when(serviceTemplateManage.registerServiceTemplate(any()))
                .thenThrow(new InvalidServiceVersionException("test error"));

        this.mockMvc
                .perform(post("/xpanse/service_templates/file").param("oclLocation", oclLocation))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errorType").value("Invalid Service Version"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testInvalidServiceFlavorsException() throws Exception {
        when(serviceTemplateManage.registerServiceTemplate(any()))
                .thenThrow(new InvalidServiceFlavorsException(List.of("test error")));

        this.mockMvc
                .perform(post("/xpanse/service_templates/file").param("oclLocation", oclLocation))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errorType").value("Invalid Service Flavors"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testUnavailableServiceRegionsException() throws Exception {
        when(serviceTemplateManage.registerServiceTemplate(any()))
                .thenThrow(new UnavailableServiceRegionsException(List.of("test error")));

        this.mockMvc
                .perform(post("/xpanse/service_templates/file").param("oclLocation", oclLocation))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errorType").value("Unavailable Service Regions"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testInvalidBillingConfigException() throws Exception {
        when(serviceTemplateManage.registerServiceTemplate(any()))
                .thenThrow(new InvalidBillingConfigException(List.of("test error")));

        this.mockMvc
                .perform(post("/xpanse/service_templates/file").param("oclLocation", oclLocation))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errorType").value("Invalid Billing Config"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }
}
