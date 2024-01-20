/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.exceptions.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.eclipse.xpanse.api.controllers.ServiceTemplateApi;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.IconProcessingFailedException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.InvalidValueSchemaException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateAlreadyRegistered;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotRegistered;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateUpdateNotAllowed;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.TerraformScriptFormatInvalidException;
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
        RegistrationExceptionHandler.class, IdentityProviderManager.class})
@WebMvcTest
class RegistrationExceptionHandlerTest {

    @Autowired
    private WebApplicationContext context;
    @MockBean
    private ServiceTemplateManage serviceTemplateManage;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void testTerraformScriptFormatInvalidException() throws Exception {
        when(serviceTemplateManage.listServiceTemplates(
                any(), any(), any(), any(), any()))
                .thenThrow(new TerraformScriptFormatInvalidException(List.of("test error")));

        this.mockMvc.perform(get("/xpanse/service_templates"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Terraform Script Invalid"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testServiceTemplateAlreadyRegistered() throws Exception {
        when(serviceTemplateManage.listServiceTemplates(any(), any(), any(), any(), any()))
                .thenThrow(new ServiceTemplateAlreadyRegistered("test error"));

        this.mockMvc.perform(get("/xpanse/service_templates"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Service Template Already Registered"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testIconProcessingFailedException() throws Exception {
        when(serviceTemplateManage.listServiceTemplates(any(), any(), any(), any(), any()))
                .thenThrow(new IconProcessingFailedException("test error"));

        this.mockMvc.perform(get("/xpanse/service_templates"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Icon Processing Failed"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testServiceTemplateNotRegistered() throws Exception {
        when(serviceTemplateManage.listServiceTemplates(any(), any(), any(), any(), any()))
                .thenThrow(new ServiceTemplateNotRegistered("test error"));

        this.mockMvc.perform(get("/xpanse/service_templates"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Service Template Not Registered"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testServiceTemplateUpdateNotAllowed() throws Exception {
        when(serviceTemplateManage.listServiceTemplates(any(), any(), any(), any(), any()))
                .thenThrow(new ServiceTemplateUpdateNotAllowed("test error"));

        this.mockMvc.perform(get("/xpanse/service_templates"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Service Template Update Not Allowed"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testInvalidValueSchemaException() throws Exception {
        when(serviceTemplateManage.listServiceTemplates(any(), any(), any(), any(), any()))
                .thenThrow(new InvalidValueSchemaException(List.of("test error")));

        this.mockMvc.perform(get("/xpanse/service_templates"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Variable Schema Definition Invalid"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }
}
