/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.c4_soft.springaddons.security.oauth2.test.annotations.OpenIdClaims;
import com.c4_soft.springaddons.security.oauth2.test.annotations.WithMockBearerTokenAuthentication;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CreateCredential;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.security.constant.RoleConstants;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.plugins.huaweicloud.monitor.constant.HuaweiCloudMonitorConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test for CredentialManageApi.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=zitadel,zitadel-testbed"})
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CredentialManageApiTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Resource
    private MockMvc mockMvc;

    @Test
    @WithMockBearerTokenAuthentication(authorities = RoleConstants.ROLE_ADMIN,
            attributes = @OpenIdClaims(sub = "adminId", preferredUsername = "adminName"))
    void testListCredentialTypes() throws Exception {
        // Setup
        List<CredentialType> types = Arrays.asList(CredentialType.values());
        String result = objectMapper.writeValueAsString(types);

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(get("/xpanse/credential_types")
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assertions.assertEquals(result, response.getContentAsString());
    }

    @Test
    @WithMockBearerTokenAuthentication(authorities = RoleConstants.ROLE_ADMIN,
            attributes = @OpenIdClaims(sub = "adminId", preferredUsername = "adminName"))
    void testListCredentialTypes_WithCsp() throws Exception {
        // Setup
        List<CredentialType> types = List.of(CredentialType.VARIABLES);
        String result = objectMapper.writeValueAsString(types);

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(get("/xpanse/credential_types")
                                .param("cspName", "huawei")
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assertions.assertEquals(result, response.getContentAsString());
    }

    @Test
    @WithMockBearerTokenAuthentication(authorities = RoleConstants.ROLE_USER,
            attributes = @OpenIdClaims(sub = "userId", preferredUsername = "userName"))
    void testListCredentialTypes_PluginNotFoundException() throws Exception {
        // Setup
        Response responseModel = Response.errorResponse(ResultType.PLUGIN_NOT_FOUND,
                Collections.singletonList("Can't find suitable plugin for the Csp AWS"));
        String result = objectMapper.writeValueAsString(responseModel);

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(get("/xpanse/credential_types")
                                .param("cspName", "aws")
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        Assertions.assertEquals(result, response.getContentAsString());
    }

    @Test
    @WithMockBearerTokenAuthentication(authorities = RoleConstants.ROLE_USER,
            attributes = @OpenIdClaims(sub = "userId", preferredUsername = "userName"))
    void testListCredentialCapabilities() throws Exception {
        // Setup
        List<CredentialVariable> credentialVariables = new ArrayList<>();
        credentialVariables.add(
                new CredentialVariable(HuaweiCloudMonitorConstants.HW_ACCESS_KEY,
                        "The access key.", true));
        credentialVariables.add(
                new CredentialVariable(HuaweiCloudMonitorConstants.HW_SECRET_KEY,
                        "The security key.", true));
        CredentialVariables accessKey = new CredentialVariables(
                Csp.HUAWEI, CredentialType.VARIABLES, HuaweiCloudMonitorConstants.IAM,
                "Using The access key and security key authentication.", null
                , credentialVariables);

        accessKey.getVariables().forEach(credentialVariable -> credentialVariable.setValue(
                "value to be provided by creating credential or adding environment variables."));
        List<AbstractCredentialInfo> responseModel = List.of(accessKey);
        String result = objectMapper.writeValueAsString(responseModel);

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                        get("/xpanse/credentials/capabilities")
                                .param("cspName", "huawei")
                                .param("type", "VARIABLES")
                                .param("name", "AK_SK")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assertions.assertEquals(result, response.getContentAsString());
    }

    @Test
    @WithMockBearerTokenAuthentication(authorities = RoleConstants.ROLE_USER,
            attributes = @OpenIdClaims(sub = "userId", preferredUsername = "userName"))
    void testListCredentialCapabilities_CredentialCenterReturnsNoItems() throws Exception {
        // Setup
        String result = "[]";

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                        get("/xpanse/credentials/capabilities")
                                .param("cspName", "huawei")
                                .param("type", "VARIABLES")
                                .param("name", "name")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assertions.assertEquals(result, response.getContentAsString());
    }

    @Test
    @Order(1)
    @WithMockBearerTokenAuthentication(authorities = RoleConstants.ROLE_USER,
            attributes = @OpenIdClaims(sub = "userId", preferredUsername = "userName"))
    void testListCredentials_CredentialCenterReturnsNoItems() throws Exception {
        // Setup
        String result = "[]";
        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(get("/xpanse/credentials")
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assertions.assertEquals(result, response.getContentAsString());
    }

    @Test
    @Order(2)
    @WithMockBearerTokenAuthentication(authorities = RoleConstants.ROLE_USER,
            attributes = @OpenIdClaims(sub = "userId", preferredUsername = "userName"))
    void testAddCredentialWithSensitiveIsFalse() throws Exception {
        // Setup
        final CreateCredential createCredential = new CreateCredential();
        createCredential.setCsp(Csp.HUAWEI);
        createCredential.setType(CredentialType.VARIABLES);
        createCredential.setName("AK_SK");
        createCredential.setDescription("description");
        List<CredentialVariable> credentialVariables = new ArrayList<>();
        credentialVariables.add(
                new CredentialVariable(HuaweiCloudMonitorConstants.HW_ACCESS_KEY,
                        "The access key.", true, false, "AK_VALUE"));
        credentialVariables.add(
                new CredentialVariable(HuaweiCloudMonitorConstants.HW_SECRET_KEY,
                        "The security key.", true, false, "SK_VALUE"));
        createCredential.setVariables(credentialVariables);
        createCredential.setTimeToLive(3000);
        String requestBody = objectMapper.writeValueAsString(createCredential);

        String addResult = "";
        createCredential.setUserId("userId");
        CredentialVariables credentialVariables1 = new CredentialVariables(createCredential);
        String queryResult = objectMapper.writeValueAsString(List.of(credentialVariables1));
        // Run the test
        final MockHttpServletResponse addResponse =
                mockMvc.perform(post("/xpanse/credentials")
                                .content(requestBody).contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        final MockHttpServletResponse queryResponse =
                mockMvc.perform(get("/xpanse/credentials")
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.NO_CONTENT.value(), addResponse.getStatus());
        Assertions.assertEquals(addResult, addResponse.getContentAsString());

        Assertions.assertEquals(HttpStatus.OK.value(), queryResponse.getStatus());
        Assertions.assertEquals(queryResult, queryResponse.getContentAsString());
    }

    @Test
    @Order(3)
    @WithMockBearerTokenAuthentication(authorities = RoleConstants.ROLE_USER,
            attributes = @OpenIdClaims(sub = "userId", preferredUsername = "userName"))
    void testAddCredentialWithSensitiveIsTrue() throws Exception {
        // Setup
        final CreateCredential createCredential = new CreateCredential();
        createCredential.setCsp(Csp.HUAWEI);
        createCredential.setType(CredentialType.VARIABLES);
        createCredential.setName("AK_SK");
        createCredential.setDescription("description");
        List<CredentialVariable> credentialVariables = new ArrayList<>();
        credentialVariables.add(
                new CredentialVariable(HuaweiCloudMonitorConstants.HW_ACCESS_KEY,
                        "The access key.", true, true, "AK_VALUE"));
        credentialVariables.add(
                new CredentialVariable(HuaweiCloudMonitorConstants.HW_SECRET_KEY,
                        "The security key.", true, true, "SK_VALUE"));
        createCredential.setVariables(credentialVariables);
        createCredential.setTimeToLive(3000);
        String requestBody = objectMapper.writeValueAsString(createCredential);

        String addResult = "";
        createCredential.setUserId("userId");
        CredentialVariables credentialVariables1 = new CredentialVariables(createCredential);
        String queryResult = objectMapper.writeValueAsString(List.of(credentialVariables1));
        // Run the test
        final MockHttpServletResponse addResponse =
                mockMvc.perform(post("/xpanse/credentials")
                                .content(requestBody).contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        final MockHttpServletResponse queryResponse =
                mockMvc.perform(get("/xpanse/credentials")
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.NO_CONTENT.value(), addResponse.getStatus());
        Assertions.assertEquals(addResult, addResponse.getContentAsString());

        Assertions.assertEquals(HttpStatus.OK.value(), queryResponse.getStatus());
        Assertions.assertNotEquals(queryResult, queryResponse.getContentAsString());
    }

    @Test
    @Order(4)
    @WithMockBearerTokenAuthentication(authorities = RoleConstants.ROLE_USER,
            attributes = @OpenIdClaims(sub = "userId2", preferredUsername = "userName"))
    void testGetCredentials_CredentialCenterReturnsNoItems() throws Exception {
        // Setup
        String result = "[]";

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(get("/xpanse/credentials")
                                .param("cspName", "huawei")
                                .param("type", "VARIABLES")
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assertions.assertEquals(result, response.getContentAsString());
    }

    @Test
    @WithMockBearerTokenAuthentication(authorities = RoleConstants.ROLE_USER,
            attributes = @OpenIdClaims(sub = "userId", preferredUsername = "userName"))
    void testGetCredentialOpenApi() throws Exception {
        // Setup
        Link link = Link.of("http://localhost/openapi/huawei_variables_credentialApi.html",
                "OpenApi");
        String result = objectMapper.writeValueAsString(link);

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                        get("/xpanse/credentials/openapi/{csp}/{type}", Csp.HUAWEI,
                                CredentialType.VARIABLES)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assertions.assertEquals(result, response.getContentAsString());
    }


    @Test
    @Order(5)
    @WithMockBearerTokenAuthentication(authorities = RoleConstants.ROLE_USER,
            attributes = @OpenIdClaims(sub = "userId", preferredUsername = "userName"))
    void testUpdateCredential() throws Exception {
        // Setup
        final CreateCredential updateCredential = new CreateCredential();
        updateCredential.setCsp(Csp.HUAWEI);
        updateCredential.setType(CredentialType.VARIABLES);
        updateCredential.setName("AK_SK");
        updateCredential.setDescription("description");
        List<CredentialVariable> credentialVariables = new ArrayList<>();
        credentialVariables.add(
                new CredentialVariable(HuaweiCloudMonitorConstants.HW_ACCESS_KEY,
                        "The access key.", true, false, "AK_VALUE_2"));
        credentialVariables.add(
                new CredentialVariable(HuaweiCloudMonitorConstants.HW_SECRET_KEY,
                        "The security key.", true, false, "SK_VALUE_2"));
        updateCredential.setVariables(credentialVariables);
        updateCredential.setTimeToLive(3000);

        String requestBody = objectMapper.writeValueAsString(updateCredential);
        String updateResult = "";
        updateCredential.setUserId("userId");
        CredentialVariables credentialVariables1 = new CredentialVariables(updateCredential);
        String queryResult = objectMapper.writeValueAsString(List.of(credentialVariables1));


        // Run the test
        final MockHttpServletResponse updateResponse =
                mockMvc.perform(put("/xpanse/credentials")
                                .content(requestBody).contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        final MockHttpServletResponse queryResponse =
                mockMvc.perform(get("/xpanse/credentials")
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.NO_CONTENT.value(), updateResponse.getStatus());
        Assertions.assertEquals(updateResult, updateResponse.getContentAsString());

        Assertions.assertEquals(HttpStatus.OK.value(), queryResponse.getStatus());
        Assertions.assertEquals(queryResult, queryResponse.getContentAsString());
    }

    @Test
    @Order(6)
    @WithMockBearerTokenAuthentication(authorities = RoleConstants.ROLE_USER,
            attributes = @OpenIdClaims(sub = "userId", preferredUsername = "userName"))
    void testDeleteCredential() throws Exception {
        // Setup
        String deleteResult = "";
        String queryResult = "[]";

        // Run the test
        final MockHttpServletResponse deleteResponse =
                mockMvc.perform(delete("/xpanse/credentials")
                                .param("cspName", "huawei")
                                .param("type", "VARIABLES")
                                .param("name", "AK_SK")
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        final MockHttpServletResponse queryResponse =
                mockMvc.perform(get("/xpanse/credentials", Csp.HUAWEI)
                                .param("type", "VARIABLES")
                                .param("name", "AK_SK")
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.NO_CONTENT.value(), deleteResponse.getStatus());
        Assertions.assertEquals(deleteResult, deleteResponse.getContentAsString());

        Assertions.assertEquals(HttpStatus.OK.value(), queryResponse.getStatus());
        Assertions.assertEquals(queryResult, queryResponse.getContentAsString());
    }

}
