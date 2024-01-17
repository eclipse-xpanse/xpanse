/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.policy.userpolicy.UserPolicy;
import org.eclipse.xpanse.modules.models.policy.userpolicy.UserPolicyCreateRequest;
import org.eclipse.xpanse.modules.models.policy.userpolicy.UserPolicyUpdateRequest;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.policy.policyman.generated.api.PoliciesValidateApi;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.ValidatePolicyList;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.ValidateResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test for UserPolicyManageApi.
 */
@Slf4j
@Transactional
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=zitadel,zitadel-testbed"})
@AutoConfigureMockMvc
class UserPolicyManageApiTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static UUID policyId;
    private static UserPolicy userPolicy;
    private static UUID openStackPolicyId;
    private static UserPolicy openStackUserPolicy;
    @Resource
    private MockMvc mockMvc;
    @MockBean
    private PoliciesValidateApi mockPoliciesValidateApi;

    @BeforeAll
    static void configureObjectMapper() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new SimpleModule().addSerializer(OffsetDateTime.class,
                OffsetDateTimeSerializer.INSTANCE));
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);
    }

    void mockPoliciesValidateRequest(boolean valid) {
        ValidateResponse validateResponse = new ValidateResponse();
        validateResponse.setIsSuccessful(valid);
        when(mockPoliciesValidateApi.validatePoliciesPost(
                any(ValidatePolicyList.class))).thenReturn(validateResponse);
    }

    @Test
    @WithJwt(file = "jwt_user.json")
    void testPoliciesManage() throws Exception {
        testListPolicies_ReturnsEmptyList();
        testAddPolicy();
        testListPolicies();
        testListPolicies_WithCspHuawei();
        testGetPolicyDetails();
        testUpdatePolicy();
        testDeletePolicy();
        testGetPolicyDetails_ThrowsPolicyNotFoundException(policyId);
    }


    @Test
    @WithJwt(file = "jwt_user.json")
    void testPoliciesManage_ThrowsExceptions() throws Exception {
        testListPolicies_ReturnsEmptyList();
        testAddPolicy_ThrowsPoliciesValidationFailed();
        testAddPolicy();
        testAddPolicy_ThrowsPolicyDuplicateException();
        testUpdatePolicy_ThrowsPolicyNotFoundException(UUID.randomUUID());
        testUpdatePolicy_ThrowsPolicyDuplicateException();
        testGetPolicyDetails_ThrowsPolicyNotFoundException(UUID.randomUUID());
        testDeletePolicy_ThrowsPolicyNotFoundException();
        testDeletePolicy();
        testGetPolicyDetails_ThrowsPolicyNotFoundException(openStackPolicyId);
    }


    void testAddPolicy() throws Exception {
        // Setup
        mockPoliciesValidateRequest(true);

        final UserPolicyCreateRequest createRequest = new UserPolicyCreateRequest();
        createRequest.setCsp(Csp.HUAWEI);
        createRequest.setPolicy("huawei_policy");
        String requestBody = objectMapper.writeValueAsString(createRequest);

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(post("/xpanse/policies")
                        .content(requestBody).contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        userPolicy = objectMapper.readValue(response.getContentAsString(), UserPolicy.class);
        policyId = userPolicy.getId();

        // Verify the results
        Assertions.assertEquals(response.getStatus(), HttpStatus.OK.value());
        Assertions.assertNotNull(userPolicy.getId());
        Assertions.assertEquals(userPolicy.getCsp(), Csp.HUAWEI);
        Assertions.assertEquals(userPolicy.getCreateTime(), userPolicy.getLastModifiedTime());
        Assertions.assertEquals(userPolicy.getPolicy(), "huawei_policy");
        Assertions.assertTrue(userPolicy.getEnabled());


        final UserPolicyCreateRequest openStackCreateRequest = new UserPolicyCreateRequest();
        openStackCreateRequest.setCsp(Csp.OPENSTACK);
        openStackCreateRequest.setPolicy("openstack_policy");
        String openStackRequestBody = objectMapper.writeValueAsString(openStackCreateRequest);

        // Run the test
        final MockHttpServletResponse openStackResponse = mockMvc.perform(post("/xpanse/policies")
                        .content(openStackRequestBody).contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        openStackUserPolicy =
                objectMapper.readValue(openStackResponse.getContentAsString(), UserPolicy.class);
        openStackPolicyId = openStackUserPolicy.getId();

        // Verify the results
        Assertions.assertEquals(openStackResponse.getStatus(), HttpStatus.OK.value());
        Assertions.assertNotNull(openStackUserPolicy.getId());
        Assertions.assertEquals(openStackUserPolicy.getCsp(), Csp.OPENSTACK);
        Assertions.assertEquals(openStackUserPolicy.getCreateTime(),
                openStackUserPolicy.getLastModifiedTime());
        Assertions.assertEquals(openStackUserPolicy.getPolicy(), "openstack_policy");
        Assertions.assertTrue(openStackUserPolicy.getEnabled());

    }

    void testGetPolicyDetails() throws Exception {
        // Setup
        String exceptedResult = objectMapper.writeValueAsString(userPolicy);

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(get("/xpanse/policies/{id}", userPolicy.getId())
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(exceptedResult);
    }

    void testGetPolicyDetails_ThrowsPolicyNotFoundException(UUID uuid) throws Exception {
        // Setup
        String errMsg = String.format("The policy with id %s not found.", uuid);
        Response result = Response.errorResponse(ResultType.POLICY_NOT_FOUND, List.of(errMsg));
        String exceptedResult = objectMapper.writeValueAsString(result);

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(get("/xpanse/policies/{id}", uuid)
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST.value());
        Assertions.assertEquals(response.getContentAsString(), exceptedResult);
    }

    void testListPolicies() throws Exception {
        // Setup
        List<UserPolicy> userPolicyList = List.of(userPolicy, openStackUserPolicy);
        String exceptedResult = objectMapper.writeValueAsString(userPolicyList);

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(get("/xpanse/policies")
                        .param("enabled", "true")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(exceptedResult);
    }

    void testListPolicies_WithCspHuawei() throws Exception {
        // Setup
        List<UserPolicy> userPolicyList = List.of(userPolicy);
        String exceptedResult = objectMapper.writeValueAsString(userPolicyList);

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(get("/xpanse/policies")
                        .param("cspName", "HUAWEI")
                        .param("enabled", "true")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(exceptedResult);
    }

    void testListPolicies_ReturnsEmptyList() throws Exception {
        // Setup
        String exceptedResult = "[]";
        // Configure UserPolicyManager.listPolicies(...).
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(get("/xpanse/policies")
                        .param("cspName", "HUAWEI")
                        .param("enabled", "false")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(exceptedResult);
    }

    void testAddPolicy_ThrowsPoliciesValidationFailed() throws Exception {
        // Setup
        mockPoliciesValidateRequest(false);

        final UserPolicyCreateRequest createRequest = new UserPolicyCreateRequest();
        createRequest.setCsp(Csp.HUAWEI);
        createRequest.setPolicy("userPolicy");
        String requestBody = objectMapper.writeValueAsString(createRequest);

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(post("/xpanse/policies")
                        .content(requestBody).contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        Response result = objectMapper.readValue(response.getContentAsString(), Response.class);

        // Verify the results
        Assertions.assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST.value());
        Assertions.assertEquals(result.getResultType(), ResultType.POLICY_VALIDATION_FAILED);
    }

    void testAddPolicy_ThrowsPolicyDuplicateException() throws Exception {

        // Setup
        mockPoliciesValidateRequest(true);
        String errMsg = String.format("The same policy already exists for Csp: %s."
                + " with id: %s", userPolicy.getCsp(), userPolicy.getId());
        Response result = Response.errorResponse(ResultType.POLICY_DUPLICATE, List.of(errMsg));
        String exceptedResult = objectMapper.writeValueAsString(result);

        final UserPolicyCreateRequest createRequest = new UserPolicyCreateRequest();
        createRequest.setCsp(Csp.HUAWEI);
        createRequest.setPolicy("huawei_policy");
        String requestBody = objectMapper.writeValueAsString(createRequest);


        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(post("/xpanse/policies")
                        .content(requestBody).contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST.value());
        Assertions.assertEquals(response.getContentAsString(), exceptedResult);
    }

    void testUpdatePolicy() throws Exception {

        // Setup
        mockPoliciesValidateRequest(true);
        userPolicy.setPolicy("hw_policy_update");
        userPolicy.setEnabled(false);

        final UserPolicyUpdateRequest updateRequest = new UserPolicyUpdateRequest();
        updateRequest.setId(policyId);
        updateRequest.setCsp(Csp.HUAWEI);
        updateRequest.setPolicy("hw_policy_update");
        updateRequest.setEnabled(false);
        String requestBody = objectMapper.writeValueAsString(updateRequest);

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(put("/xpanse/policies")
                        .content(requestBody).contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(response.getStatus(), HttpStatus.OK.value());
        Assertions.assertNotNull(userPolicy.getId());
        Assertions.assertEquals(userPolicy.getPolicy(), "hw_policy_update");
        Assertions.assertFalse(userPolicy.getEnabled());
    }

    void testUpdatePolicy_ThrowsPolicyDuplicateException() throws Exception {
        // Setup
        mockPoliciesValidateRequest(true);
        String errMsg = String.format("The same policy already exists for Csp: %s."
                + " with id: %s", openStackUserPolicy.getCsp(), openStackUserPolicy.getId());
        Response result = Response.errorResponse(ResultType.POLICY_DUPLICATE, List.of(errMsg));
        String exceptedResult = objectMapper.writeValueAsString(result);

        final UserPolicyUpdateRequest updateRequest = new UserPolicyUpdateRequest();
        updateRequest.setId(policyId);
        updateRequest.setCsp(Csp.OPENSTACK);
        updateRequest.setPolicy("openstack_policy");
        String requestBody = objectMapper.writeValueAsString(updateRequest);

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(put("/xpanse/policies")
                        .content(requestBody).contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST.value());
        Assertions.assertEquals(response.getContentAsString(), exceptedResult);
    }

    void testUpdatePolicy_ThrowsPolicyNotFoundException(UUID uuid) throws Exception {
        // Setup
        mockPoliciesValidateRequest(true);
        String errMsg = String.format("The policy with id %s not found.", uuid);
        Response result = Response.errorResponse(ResultType.POLICY_NOT_FOUND, List.of(errMsg));
        String exceptedResult = objectMapper.writeValueAsString(result);

        final UserPolicyUpdateRequest updateRequest = new UserPolicyUpdateRequest();
        updateRequest.setId(uuid);
        updateRequest.setCsp(Csp.HUAWEI);
        updateRequest.setPolicy("userPolicy");
        String requestBody = objectMapper.writeValueAsString(updateRequest);

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(put("/xpanse/policies")
                        .content(requestBody).contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST.value());
        Assertions.assertEquals(response.getContentAsString(), exceptedResult);
    }


    void testDeletePolicy() throws Exception {
        // Setup
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                        delete("/xpanse/policies/{id}", policyId)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        final MockHttpServletResponse response2 = mockMvc.perform(
                        delete("/xpanse/policies/{id}", openStackPolicyId)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(response.getStatus(), HttpStatus.NO_CONTENT.value());
        Assertions.assertEquals(response2.getStatus(), HttpStatus.NO_CONTENT.value());
        userPolicy = null;
        openStackUserPolicy = null;
    }

    void testDeletePolicy_ThrowsPolicyNotFoundException() throws Exception {
        // Setup
        UUID uuid = UUID.randomUUID();
        String errMsg = String.format("The policy with id %s not found.", uuid);
        Response result = Response.errorResponse(ResultType.POLICY_NOT_FOUND, List.of(errMsg));
        String exceptedResult = objectMapper.writeValueAsString(result);

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                        delete("/xpanse/policies/{id}", uuid)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST.value());
        Assertions.assertEquals(response.getContentAsString(), exceptedResult);
    }

}
