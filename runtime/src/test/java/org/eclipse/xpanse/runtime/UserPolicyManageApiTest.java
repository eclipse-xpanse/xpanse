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
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.policy.userpolicy.UserPolicy;
import org.eclipse.xpanse.modules.models.policy.userpolicy.UserPolicyCreateRequest;
import org.eclipse.xpanse.modules.models.policy.userpolicy.UserPolicyUpdateRequest;
import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.response.ErrorType;
import org.eclipse.xpanse.modules.policy.policyman.generated.api.PoliciesValidateApi;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.ValidatePolicyList;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.ValidateResponse;
import org.eclipse.xpanse.runtime.util.ApisTestCommon;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/** Test for UserPolicyManageApi. */
@Slf4j
@Transactional
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=oauth,zitadel,zitadel-testbed,test,dev"})
@AutoConfigureMockMvc
class UserPolicyManageApiTest extends ApisTestCommon {

    @MockitoBean private PoliciesValidateApi mockPoliciesValidateApi;

    void mockPoliciesValidateRequest(boolean valid) {
        ValidateResponse validateResponse = new ValidateResponse();
        validateResponse.setIsSuccessful(valid);
        when(mockPoliciesValidateApi.validatePoliciesPost(any(ValidatePolicyList.class)))
                .thenReturn(validateResponse);
    }

    @Test
    @WithJwt(file = "jwt_user.json")
    void testPoliciesManageApisWell() throws Exception {
        UserPolicyCreateRequest createRequest = new UserPolicyCreateRequest();
        createRequest.setCsp(Csp.OPENSTACK_TESTLAB);
        createRequest.setPolicy("userPolicy");
        UserPolicy userPolicy = addUserPolicy(createRequest);
        testListUserPolicies(userPolicy);
        testGetPolicyDetails(userPolicy);
        testUpdatePolicy(userPolicy);
        testDeletePolicy(userPolicy.getUserPolicyId());
        testListPoliciesReturnsEmptyList(createRequest.getCsp());
    }

    @Test
    @WithJwt(file = "jwt_user.json")
    void testPoliciesManage_ThrowsExceptions() throws Exception {
        testAddPolicy_ThrowsPoliciesValidationFailed();
        UserPolicyCreateRequest createRequest = new UserPolicyCreateRequest();
        createRequest.setCsp(Csp.OPENSTACK_TESTLAB);
        createRequest.setPolicy("userPolicy");
        UserPolicy userPolicy = addUserPolicy(createRequest);
        testAddPolicy_ThrowsPolicyDuplicateException(userPolicy);
        testUpdatePolicy_ThrowsPolicyNotFoundException(UUID.randomUUID());
        testGetPolicyDetails_ThrowsPolicyNotFoundException(UUID.randomUUID());
        testDeletePolicy_ThrowsPolicyNotFoundException();
        testDeletePolicy(userPolicy.getUserPolicyId());
        testGetPolicyDetails_ThrowsPolicyNotFoundException(userPolicy.getUserPolicyId());
    }

    UserPolicy addUserPolicy(UserPolicyCreateRequest createRequest) throws Exception {
        // Setup
        mockPoliciesValidateRequest(true);
        String requestBody = objectMapper.writeValueAsString(createRequest);
        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(
                                post("/xpanse/policies")
                                        .content(requestBody)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();
        UserPolicy userPolicy =
                objectMapper.readValue(response.getContentAsString(), UserPolicy.class);

        // Verify the results
        Assertions.assertEquals(response.getStatus(), HttpStatus.OK.value());
        Assertions.assertNotNull(userPolicy.getUserPolicyId());
        Assertions.assertEquals(userPolicy.getCsp(), createRequest.getCsp());
        Assertions.assertEquals(userPolicy.getCreateTime(), userPolicy.getLastModifiedTime());
        Assertions.assertEquals(userPolicy.getPolicy(), createRequest.getPolicy());
        Assertions.assertTrue(userPolicy.getEnabled());

        return userPolicy;
    }

    void testGetPolicyDetails(UserPolicy userPolicy) throws Exception {
        // Setup
        String exceptedResult = objectMapper.writeValueAsString(userPolicy);

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(
                                get("/xpanse/policies/{id}", userPolicy.getUserPolicyId())
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(exceptedResult);
    }

    void testGetPolicyDetails_ThrowsPolicyNotFoundException(UUID uuid) throws Exception {
        // Setup
        String errMsg = String.format("The user policy with id %s not found.", uuid);
        ErrorResponse result =
                ErrorResponse.errorResponse(ErrorType.POLICY_NOT_FOUND, List.of(errMsg));
        String exceptedResult = objectMapper.writeValueAsString(result);

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(
                                get("/xpanse/policies/{id}", uuid)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        // Verify the results
        Assertions.assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST.value());
        Assertions.assertEquals(response.getContentAsString(), exceptedResult);
    }

    void testListUserPolicies(UserPolicy userPolicy) throws Exception {
        // Setup
        List<UserPolicy> userPolicyList = List.of(userPolicy);
        String exceptedResult = objectMapper.writeValueAsString(userPolicyList);

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(
                                get("/xpanse/policies")
                                        .param("cspName", userPolicy.getCsp().toValue())
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(exceptedResult);
    }

    void testListPoliciesReturnsEmptyList(Csp csp) throws Exception {
        // Setup
        String exceptedResult = "[]";
        // Configure UserPolicyManager.listPolicies(...).
        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(
                                get("/xpanse/policies")
                                        .param("cspName", csp.toValue())
                                        .param("enabled", "false")
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(exceptedResult);
    }

    void testAddPolicy_ThrowsPoliciesValidationFailed() throws Exception {
        // Setup
        mockPoliciesValidateRequest(false);

        final UserPolicyCreateRequest createRequest = new UserPolicyCreateRequest();
        createRequest.setCsp(Csp.HUAWEI_CLOUD);
        createRequest.setPolicy("userPolicy");
        String requestBody = objectMapper.writeValueAsString(createRequest);

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(
                                post("/xpanse/policies")
                                        .content(requestBody)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        ErrorResponse result =
                objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);

        // Verify the results
        Assertions.assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST.value());
        Assertions.assertEquals(ErrorType.POLICY_VALIDATION_FAILED, result.getErrorType());
    }

    void testAddPolicy_ThrowsPolicyDuplicateException(UserPolicy userPolicy) throws Exception {

        // Setup
        mockPoliciesValidateRequest(true);
        String errMsg =
                String.format(
                        "The same user policy already exists for Csp %s with id %s",
                        userPolicy.getCsp(), userPolicy.getUserPolicyId());
        ErrorResponse result =
                ErrorResponse.errorResponse(ErrorType.POLICY_DUPLICATE, List.of(errMsg));
        String exceptedResult = objectMapper.writeValueAsString(result);

        final UserPolicyCreateRequest createRequest = new UserPolicyCreateRequest();
        createRequest.setCsp(userPolicy.getCsp());
        createRequest.setPolicy(userPolicy.getPolicy());
        String requestBody = objectMapper.writeValueAsString(createRequest);

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(
                                post("/xpanse/policies")
                                        .content(requestBody)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        // Verify the results
        Assertions.assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST.value());
        Assertions.assertEquals(response.getContentAsString(), exceptedResult);
    }

    void testUpdatePolicy(UserPolicy userPolicy) throws Exception {

        // Setup
        mockPoliciesValidateRequest(true);

        final UserPolicyUpdateRequest updateRequest = new UserPolicyUpdateRequest();
        updateRequest.setCsp(Csp.OPENSTACK_TESTLAB);
        updateRequest.setPolicy("userPolicyUpdate");
        updateRequest.setEnabled(true);
        String requestBody = objectMapper.writeValueAsString(updateRequest);

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(
                                put("/xpanse/policies/{id}", userPolicy.getUserPolicyId())
                                        .content(requestBody)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        UserPolicy updatedUserPolicy =
                objectMapper.readValue(response.getContentAsString(), UserPolicy.class);

        // Verify the results
        Assertions.assertEquals(response.getStatus(), HttpStatus.OK.value());
        Assertions.assertEquals(updatedUserPolicy.getUserPolicyId(), userPolicy.getUserPolicyId());
        Assertions.assertEquals(Csp.OPENSTACK_TESTLAB, updatedUserPolicy.getCsp());
        Assertions.assertEquals("userPolicyUpdate", updatedUserPolicy.getPolicy());
        Assertions.assertTrue(updatedUserPolicy.getEnabled());
    }

    void testUpdatePolicy_ThrowsPolicyNotFoundException(UUID uuid) throws Exception {
        // Setup
        mockPoliciesValidateRequest(true);
        String errMsg = String.format("The user policy with id %s not found.", uuid);
        ErrorResponse result =
                ErrorResponse.errorResponse(ErrorType.POLICY_NOT_FOUND, List.of(errMsg));
        String exceptedResult = objectMapper.writeValueAsString(result);

        final UserPolicyUpdateRequest updateRequest = new UserPolicyUpdateRequest();
        updateRequest.setCsp(Csp.HUAWEI_CLOUD);
        updateRequest.setPolicy("userPolicy");
        String requestBody = objectMapper.writeValueAsString(updateRequest);

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(
                                put("/xpanse/policies/{id}", uuid)
                                        .content(requestBody)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        // Verify the results
        Assertions.assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST.value());
        Assertions.assertEquals(response.getContentAsString(), exceptedResult);
    }

    void testDeletePolicy(UUID policyId) throws Exception {
        // Setup
        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(
                                delete("/xpanse/policies/{id}", policyId)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        // Verify the results
        Assertions.assertEquals(response.getStatus(), HttpStatus.NO_CONTENT.value());
    }

    void testDeletePolicy_ThrowsPolicyNotFoundException() throws Exception {
        // Setup
        UUID uuid = UUID.randomUUID();
        String errMsg = String.format("The user policy with id %s not found.", uuid);
        ErrorResponse result =
                ErrorResponse.errorResponse(ErrorType.POLICY_NOT_FOUND, List.of(errMsg));
        String exceptedResult = objectMapper.writeValueAsString(result);

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(
                                delete("/xpanse/policies/{id}", uuid)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        // Verify the results
        Assertions.assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST.value());
        Assertions.assertEquals(response.getContentAsString(), exceptedResult);
    }
}
