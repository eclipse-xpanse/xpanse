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
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.common.exceptions.ResponseInvalidException;
import org.eclipse.xpanse.modules.models.common.exceptions.SensitiveFieldEncryptionOrDecryptionFailedException;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.eclipse.xpanse.modules.models.common.exceptions.XpanseUnhandledException;
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
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ServiceTemplateApi.class, ServiceTemplateManage.class,
        IdentityProviderManager.class, CommonExceptionHandler.class, OclLoader.class,
        CspPluginValidator.class, PluginManager.class})
@WebMvcTest
class CommonExceptionHandlerTest {

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
    void testHttpMessageConversionException() throws Exception {
        when(serviceTemplateManage.registerServiceTemplate(any())).thenThrow(
                new HttpMessageConversionException("test error"));

        this.mockMvc.perform(
                        post("/xpanse/service_templates/file").param("oclLocation", oclLocation))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Parameters Invalid"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testIllegalArgumentException() throws Exception {
        when(serviceTemplateManage.registerServiceTemplate(any())).thenThrow(
                new IllegalArgumentException("test error"));

        this.mockMvc.perform(
                        post("/xpanse/service_templates/file").param("oclLocation", oclLocation))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Parameters Invalid"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testResponseInvalidException() throws Exception {
        when(serviceTemplateManage.registerServiceTemplate(any())).thenThrow(
                new ResponseInvalidException(List.of("test error")));

        this.mockMvc.perform(
                        post("/xpanse/service_templates/file").param("oclLocation", oclLocation))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.resultType").value("Response Not Valid"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testXpanseUnhandledException() throws Exception {
        when(serviceTemplateManage.registerServiceTemplate(any())).thenThrow(
                new XpanseUnhandledException("test error"));

        this.mockMvc.perform(
                        post("/xpanse/service_templates/file").param("oclLocation", oclLocation))
                .andExpect(status().is(500))
                .andExpect(jsonPath("$.resultType").value("Unhandled Exception"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }


    @Test
    void testHandleAccessDeniedException() throws Exception {
        when(serviceTemplateManage.registerServiceTemplate(any())).thenThrow(
                new AccessDeniedException("test error"));

        this.mockMvc.perform(
                        post("/xpanse/service_templates/file").param("oclLocation", oclLocation))
                .andExpect(status().is(403))
                .andExpect(jsonPath("$.resultType").value("Access Denied"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testSensitiveFieldEncryptionOrDecryptionFailedException() throws Exception {
        when(serviceTemplateManage.registerServiceTemplate(any())).thenThrow(
                new SensitiveFieldEncryptionOrDecryptionFailedException("test error"));

        this.mockMvc.perform(
                        post("/xpanse/service_templates/file").param("oclLocation", oclLocation))
                .andExpect(status().is(400)).andExpect(jsonPath("$.resultType").value(
                        "Sensitive " + "Field Encryption Or Decryption Failed Exception"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testUnsupportedEnumValueException() throws Exception {
        when(serviceTemplateManage.registerServiceTemplate(any())).thenThrow(
                new UnsupportedEnumValueException("test error"));

        this.mockMvc.perform(
                        post("/xpanse/service_templates/file").param("oclLocation", oclLocation))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.resultType").value("Unsupported Enum Value"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testMethodArgumentTypeMismatchException() throws Exception {
        when(serviceTemplateManage.registerServiceTemplate(any())).thenThrow(
                new MethodArgumentTypeMismatchException("errorValue", Csp.class, null, null, null));

        this.mockMvc.perform(
                        post("/xpanse/service_templates/file").param("oclLocation", oclLocation))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.resultType").value("Unprocessable Entity")).andExpect(
                        jsonPath("$.details[0]").value(
                                "Method parameter 'null': Failed to convert value of type" +
                                        " 'java.lang.String' to required type 'org.eclipse.xpanse.modules.models.common.enums.Csp'"));
    }

}