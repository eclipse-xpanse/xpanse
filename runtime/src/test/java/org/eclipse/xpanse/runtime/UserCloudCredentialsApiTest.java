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

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.CreateCredential;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.plugins.huaweicloud.monitor.constant.HuaweiCloudMonitorConstants;
import org.eclipse.xpanse.runtime.util.ApisTestCommon;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
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

/** Test for UserCloudCredentialsApi. */
@ExtendWith(SpringExtension.class)
@Transactional
@SpringBootTest(properties = {"spring.profiles.active=oauth,zitadel,zitadel-testbed,test,dev"})
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserCloudCredentialsApiTest extends ApisTestCommon {

    private final Csp csp = Csp.HUAWEI_CLOUD;
    private final String site = "Chinese Mainland";
    private final String credentialName = "AK_SK";
    private final CredentialType credentialType = CredentialType.VARIABLES;

    @Test
    @WithJwt(file = "jwt_user.json")
    void testUserCloudCredentialApis() throws Exception {
        testAddCredentialWithSensitiveIsFalse();
        testAddCredentialWithSensitiveIsTrue();
        testGetCredentialOpenApi();
        testUpdateCredential();
        testDeleteCredential();
        testListCredentials_CredentialCenterReturnsNoItems();
    }

    private CreateCredential getCreateCredential(boolean isSensitive) {
        final CreateCredential createCredential = new CreateCredential();
        createCredential.setCsp(csp);
        createCredential.setSite(site);
        createCredential.setType(credentialType);
        createCredential.setName(credentialName);
        createCredential.setDescription("description");
        List<CredentialVariable> credentialVariables = new ArrayList<>();
        credentialVariables.add(
                new CredentialVariable(
                        HuaweiCloudMonitorConstants.HW_ACCESS_KEY,
                        "The access key.",
                        true,
                        isSensitive,
                        "AK_VALUE"));
        credentialVariables.add(
                new CredentialVariable(
                        HuaweiCloudMonitorConstants.HW_SECRET_KEY,
                        "The security key.",
                        true,
                        isSensitive,
                        "SK_VALUE"));
        createCredential.setVariables(credentialVariables);
        createCredential.setTimeToLive(300);
        return createCredential;
    }

    void testListCredentials_CredentialCenterReturnsNoItems() throws Exception {
        // Setup
        String result = "[]";
        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(
                                get("/xpanse/user/credentials")
                                        .param("cspName", csp.toValue())
                                        .param("type", credentialType.toValue())
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assertions.assertEquals(result, response.getContentAsString());
    }

    void testAddCredentialWithSensitiveIsFalse() throws Exception {
        // Setup
        final CreateCredential createCredential = getCreateCredential(false);
        String requestBody = objectMapper.writeValueAsString(createCredential);

        String addResult = "";
        createCredential.setUserId("userId");
        CredentialVariables credentialVariables1 = new CredentialVariables(createCredential);
        String queryResult = objectMapper.writeValueAsString(List.of(credentialVariables1));
        // Run the test
        final MockHttpServletResponse addResponse =
                mockMvc.perform(
                                post("/xpanse/user/credentials")
                                        .content(requestBody)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        final MockHttpServletResponse queryResponse =
                mockMvc.perform(
                                get("/xpanse/user/credentials")
                                        .param("cspName", csp.toValue())
                                        .param("type", credentialType.toValue())
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.NO_CONTENT.value(), addResponse.getStatus());
        Assertions.assertEquals(addResult, addResponse.getContentAsString());

        Assertions.assertEquals(HttpStatus.OK.value(), queryResponse.getStatus());
        Assertions.assertEquals(queryResult, queryResponse.getContentAsString());
    }

    void testAddCredentialWithSensitiveIsTrue() throws Exception {
        // Setup
        final CreateCredential createCredential = getCreateCredential(true);
        String requestBody = objectMapper.writeValueAsString(createCredential);
        String addResult = "";
        createCredential.setUserId("userId");
        CredentialVariables credentialVariables1 = new CredentialVariables(createCredential);
        String queryResult = objectMapper.writeValueAsString(List.of(credentialVariables1));
        // Run the test
        final MockHttpServletResponse addResponse =
                mockMvc.perform(
                                post("/xpanse/user/credentials")
                                        .content(requestBody)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        final MockHttpServletResponse queryResponse =
                mockMvc.perform(
                                get("/xpanse/user/credentials")
                                        .param("cspName", csp.toValue())
                                        .param("type", credentialType.toValue())
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.NO_CONTENT.value(), addResponse.getStatus());
        Assertions.assertEquals(addResult, addResponse.getContentAsString());

        Assertions.assertEquals(HttpStatus.OK.value(), queryResponse.getStatus());
        Assertions.assertNotEquals(queryResult, queryResponse.getContentAsString());
    }

    void testGetCredentialOpenApi() throws Exception {
        // Setup
        Link link =
                Link.of(
                        "http://localhost/openapi/HuaweiCloud_variables_credentialApi.html",
                        "OpenApi");
        String result = objectMapper.writeValueAsString(link);

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(
                                get(
                                                "/xpanse/credentials/openapi/{csp}/{type}",
                                                Csp.HUAWEI_CLOUD,
                                                CredentialType.VARIABLES)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assertions.assertEquals(result, response.getContentAsString());
    }

    void testUpdateCredential() throws Exception {
        // Setup
        final CreateCredential updateCredential = getCreateCredential(false);

        String requestBody = objectMapper.writeValueAsString(updateCredential);
        String updateResult = "";
        updateCredential.setUserId("userId");
        CredentialVariables credentialVariables1 = new CredentialVariables(updateCredential);
        String queryResult = objectMapper.writeValueAsString(List.of(credentialVariables1));

        // Run the test
        final MockHttpServletResponse updateResponse =
                mockMvc.perform(
                                put("/xpanse/user/credentials")
                                        .content(requestBody)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        final MockHttpServletResponse queryResponse =
                mockMvc.perform(
                                get("/xpanse/user/credentials")
                                        .param("cspName", csp.toValue())
                                        .param("type", credentialType.toValue())
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.NO_CONTENT.value(), updateResponse.getStatus());
        Assertions.assertEquals(updateResult, updateResponse.getContentAsString());

        Assertions.assertEquals(HttpStatus.OK.value(), queryResponse.getStatus());
        Assertions.assertEquals(queryResult, queryResponse.getContentAsString());
    }

    void testDeleteCredential() throws Exception {
        // Setup
        String deleteResult = "";
        String queryResult = "[]";

        // Run the test
        final MockHttpServletResponse deleteResponse =
                mockMvc.perform(
                                delete("/xpanse/user/credentials")
                                        .param("cspName", csp.toValue())
                                        .param("siteName", site)
                                        .param("type", credentialType.toValue())
                                        .param("name", "AK_SK")
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        final MockHttpServletResponse queryResponse =
                mockMvc.perform(
                                get("/xpanse/user/credentials")
                                        .param("cspName", csp.toValue())
                                        .param("type", credentialType.toValue())
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.NO_CONTENT.value(), deleteResponse.getStatus());
        Assertions.assertEquals(deleteResult, deleteResponse.getContentAsString());

        Assertions.assertEquals(HttpStatus.OK.value(), queryResponse.getStatus());
        Assertions.assertEquals(queryResult, queryResponse.getContentAsString());
    }
}
