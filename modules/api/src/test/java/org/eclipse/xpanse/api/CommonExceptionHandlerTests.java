/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.List;
import org.eclipse.xpanse.api.exceptions.CommonExceptionHandler;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.models.common.exceptions.ResponseInvalidException;
import org.eclipse.xpanse.modules.models.common.exceptions.SensitiveFieldEncryptionOrDecryptionFailedException;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.eclipse.xpanse.modules.models.common.exceptions.XpanseUnhandledException;
import org.eclipse.xpanse.modules.models.credential.CreateCredential;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CredentialManageApi.class, CommonExceptionHandler.class,
        CredentialCenter.class})
@WebMvcTest
class CommonExceptionHandlerTests {

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
    void testMethodArgumentNotValidException() throws Exception {
        CreateCredential createCredential = Instancio.of(CreateCredential.class).set(Select.field(
                CreateCredential::getType), null).create();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(createCredential);
        this.mockMvc.perform(
                        post("/xpanse/auth/csp/credential")
                                .content(requestJson)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.resultType").value("Unprocessable Entity"))
                .andExpect(jsonPath("$.details[0]").value("type:must not be null"));
    }

    @Test
    void testRuntimeException() throws Exception {
        when(credentialCenter.getCredentialsByUser(anyString())).thenThrow(
                new RuntimeException("test error"));
        this.mockMvc.perform(
                        get("/xpanse/auth/user/credentials?userName=test"))
                .andExpect(status().is(500))
                .andExpect(jsonPath("$.resultType").value("Runtime Error"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testHttpMessageConversionException() throws Exception {
        when(credentialCenter.getCredentialsByUser(anyString())).thenThrow(
                new HttpMessageConversionException("test error"));
        this.mockMvc.perform(
                        get("/xpanse/auth/user/credentials?userName=test"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Parameters Invalid"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testIllegalArgumentException() throws Exception {
        when(credentialCenter.getCredentialsByUser(anyString())).thenThrow(
                new IllegalArgumentException("test error"));
        this.mockMvc.perform(
                        get("/xpanse/auth/user/credentials?userName=test"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Parameters Invalid"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testException() throws Exception {
        given(credentialCenter.getCredentialsByUser(anyString())).willAnswer(invocation -> {
            throw new Exception("test error");
        });
        this.mockMvc.perform(
                        get("/xpanse/auth/user/credentials?userName=test"))
                .andExpect(status().is(500))
                .andExpect(jsonPath("$.resultType").value("Runtime Error"))
                .andExpect(jsonPath("$.details[0]").value("java.lang.Exception:test error"));
    }

    @Test
    void testResponseInvalidException() throws Exception {
        when(credentialCenter.getCredentialsByUser(anyString())).thenThrow(
                new ResponseInvalidException(List.of("test error")));
        this.mockMvc.perform(
                        get("/xpanse/auth/user/credentials?userName=test"))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.resultType").value("Response Not Valid"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testXpanseUnhandledException() throws Exception {
        when(credentialCenter.getCredentialsByUser(anyString())).thenThrow(
                new XpanseUnhandledException("test error"));
        this.mockMvc.perform(
                        get("/xpanse/auth/user/credentials?userName=test"))
                .andExpect(status().is(500))
                .andExpect(jsonPath("$.resultType").value("Unhandled Exception"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }


    @Test
    void testHandleAccessDeniedException() throws Exception {
        when(credentialCenter.getCredentialsByUser(anyString())).thenThrow(
                new AccessDeniedException("Access Denied"));
        this.mockMvc.perform(
                        get("/xpanse/auth/user/credentials?userName=test"))
                .andExpect(status().is(403))
                .andExpect(jsonPath("$.resultType").value("Access Denied"))
                .andExpect(jsonPath("$.details[0]").value("Access Denied"));
    }

    @Test
    void testSensitiveFieldEncryptionOrDecryptionFailedException() throws Exception {
        when(credentialCenter.getCredentialsByUser(anyString())).thenThrow(
                new SensitiveFieldEncryptionOrDecryptionFailedException("test error"));
        this.mockMvc.perform(
                        get("/xpanse/auth/user/credentials?userName=test"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.resultType").value("Sensitive "
                        + "Field Encryption Or Decryption Failed Exception"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testUnsupportedEnumValueException() throws Exception {
        when(credentialCenter.getCredentialsByUser(anyString())).thenThrow(
                new UnsupportedEnumValueException("test error"));
        this.mockMvc.perform(
                        get("/xpanse/auth/user/credentials?userName=test"))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.resultType").value("Unsupported Enum Value"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

}
