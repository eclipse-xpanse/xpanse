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
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.policy.servicepolicy.ServicePolicy;
import org.eclipse.xpanse.modules.models.policy.servicepolicy.ServicePolicyCreateRequest;
import org.eclipse.xpanse.modules.models.policy.servicepolicy.ServicePolicyUpdateRequest;
import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.response.ErrorType;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceFlavor;
import org.eclipse.xpanse.modules.models.servicetemplate.request.ServiceTemplateRequestInfo;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
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

/** Test for ServicePolicyManageApi. */
@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=oauth,zitadel,zitadel-testbed,test,dev"})
@AutoConfigureMockMvc
class ServicePolicyManageApiTest extends ApisTestCommon {

    @MockitoBean private PoliciesValidateApi mockPoliciesValidateApi;

    void mockPoliciesValidateRequest(boolean valid) {
        ValidateResponse validateResponse = new ValidateResponse();
        validateResponse.setIsSuccessful(valid);
        when(mockPoliciesValidateApi.validatePoliciesPost(any(ValidatePolicyList.class)))
                .thenReturn(validateResponse);
    }

    @Test
    @WithJwt(file = "jwt_isv.json")
    void testServicePoliciesManageApis() throws Exception {
        Ocl ocl =
                new OclLoader()
                        .getOcl(
                                URI.create("file:src/test/resources/ocl_terraform_test.yml")
                                        .toURL());
        ServiceTemplateRequestInfo requestInfo = registerServiceTemplate(ocl);
        ServiceTemplateDetailVo serviceTemplate =
                getServiceTemplateDetailsVo(requestInfo.getServiceTemplateId());
        testServicePoliciesManageApisWell(serviceTemplate);
        testServicePoliciesManage_ThrowsExceptions(serviceTemplate);
        deleteServiceTemplate(serviceTemplate.getServiceTemplateId());
    }

    void testServicePoliciesManageApisWell(ServiceTemplateDetailVo serviceTemplate)
            throws Exception {

        List<String> flavorNames =
                serviceTemplate.getFlavors().getServiceFlavors().stream()
                        .map(ServiceFlavor::getName)
                        .toList();
        testListServicePoliciesReturnsEmptyList(serviceTemplate.getServiceTemplateId());
        ServicePolicyCreateRequest createRequest = new ServicePolicyCreateRequest();
        createRequest.setFlavorNameList(flavorNames);
        createRequest.setServiceTemplateId(serviceTemplate.getServiceTemplateId());
        createRequest.setPolicy("servicePolicy-1");
        ServicePolicy servicePolicy = addServicePolicy(createRequest);
        testListServicePolicies(servicePolicy);
        testGetServicePolicyDetails(servicePolicy);
        testUpdateServicePolicy(servicePolicy);
        testDeleteServicePolicy(servicePolicy.getServicePolicyId());
        testListServicePoliciesReturnsEmptyList(serviceTemplate.getServiceTemplateId());
    }

    void testServicePoliciesManage_ThrowsExceptions(ServiceTemplateDetailVo serviceTemplate)
            throws Exception {

        List<String> flavorNames =
                serviceTemplate.getFlavors().getServiceFlavors().stream()
                        .map(ServiceFlavor::getName)
                        .toList();
        ServicePolicyCreateRequest createRequest = new ServicePolicyCreateRequest();
        createRequest.setFlavorNameList(flavorNames);
        createRequest.setServiceTemplateId(serviceTemplate.getServiceTemplateId());
        createRequest.setPolicy("servicePolicy-2");
        testAddServicePolicy_ThrowsPoliciesValidationFailed(createRequest);
        ServicePolicy servicePolicy = addServicePolicy(createRequest);
        testAddServicePolicy_ThrowsPolicyDuplicateException(servicePolicy);
        testAddServicePolicy_ThrowsFlavorInValidationException(serviceTemplate);
        testUpdateServicePolicy_ThrowsPolicyNotFoundException(UUID.randomUUID());
        testGetServicePolicyDetails_ThrowsPolicyNotFoundException(UUID.randomUUID());
        testDeleteServicePolicy_ThrowsPolicyNotFoundException();
        testDeleteServicePolicy(servicePolicy.getServicePolicyId());
        testGetServicePolicyDetails_ThrowsPolicyNotFoundException(
                servicePolicy.getServicePolicyId());
    }

    ServicePolicy addServicePolicy(ServicePolicyCreateRequest createRequest) throws Exception {
        // Setup
        mockPoliciesValidateRequest(true);
        String requestBody = objectMapper.writeValueAsString(createRequest);
        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(
                                post("/xpanse/service/policies")
                                        .content(requestBody)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();
        ServicePolicy servicePolicy =
                objectMapper.readValue(response.getContentAsString(), ServicePolicy.class);

        // Verify the results
        Assertions.assertEquals(response.getStatus(), HttpStatus.OK.value());
        Assertions.assertNotNull(servicePolicy.getServicePolicyId());
        Assertions.assertEquals(servicePolicy.getCreateTime(), servicePolicy.getLastModifiedTime());
        Assertions.assertEquals(servicePolicy.getPolicy(), createRequest.getPolicy());
        Assertions.assertTrue(servicePolicy.getEnabled());

        return servicePolicy;
    }

    void testGetServicePolicyDetails(ServicePolicy servicePolicy) throws Exception {
        // Setup
        String exceptedResult = objectMapper.writeValueAsString(servicePolicy);

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(
                                get(
                                                "/xpanse/service/policies/{id}",
                                                servicePolicy.getServicePolicyId())
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(exceptedResult);
    }

    void testGetServicePolicyDetails_ThrowsPolicyNotFoundException(UUID uuid) throws Exception {
        // Setup
        String errMsg = String.format("The service policy with id %s not found.", uuid);
        ErrorResponse result =
                ErrorResponse.errorResponse(ErrorType.POLICY_NOT_FOUND, List.of(errMsg));
        String exceptedResult = objectMapper.writeValueAsString(result);

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(
                                get("/xpanse/service/policies/{id}", uuid)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        // Verify the results
        Assertions.assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST.value());
        Assertions.assertEquals(response.getContentAsString(), exceptedResult);
    }

    void testListServicePolicies(ServicePolicy servicePolicy) throws Exception {
        // Setup
        List<ServicePolicy> servicePolicyList = List.of(servicePolicy);
        String exceptedResult = objectMapper.writeValueAsString(servicePolicyList);

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(
                                get("/xpanse/service/policies")
                                        .param(
                                                "serviceTemplateId",
                                                servicePolicy.getServiceTemplateId().toString())
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(exceptedResult);
    }

    void testListServicePoliciesReturnsEmptyList(UUID serviceTemplateId) throws Exception {
        // Setup
        String exceptedResult = "[]";
        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(
                                get("/xpanse/service/policies")
                                        .param("serviceTemplateId", serviceTemplateId.toString())
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(exceptedResult);
    }

    void testAddServicePolicy_ThrowsPoliciesValidationFailed(
            ServicePolicyCreateRequest createRequest) throws Exception {
        // Setup
        mockPoliciesValidateRequest(false);
        String requestBody = objectMapper.writeValueAsString(createRequest);

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(
                                post("/xpanse/service/policies")
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

    void testAddServicePolicy_ThrowsFlavorInValidationException(
            ServiceTemplateDetailVo serviceTemplate) throws Exception {

        // Setup
        mockPoliciesValidateRequest(true);
        String errorFlavorName = "error_flavor_name";
        String errMsg =
                String.format(
                        "Flavor name %s is not valid for service template with id %s.",
                        errorFlavorName, serviceTemplate.getServiceTemplateId());
        ErrorResponse result =
                ErrorResponse.errorResponse(ErrorType.FLAVOR_NOT_FOUND, List.of(errMsg));
        String exceptedResult = objectMapper.writeValueAsString(result);

        final ServicePolicyCreateRequest createRequest = new ServicePolicyCreateRequest();
        createRequest.setServiceTemplateId(serviceTemplate.getServiceTemplateId());
        createRequest.setPolicy("servicePolicy");
        createRequest.setFlavorNameList(List.of(errorFlavorName));
        String requestBody = objectMapper.writeValueAsString(createRequest);

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(
                                post("/xpanse/service/policies")
                                        .content(requestBody)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        // Verify the results
        Assertions.assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST.value());
        Assertions.assertEquals(response.getContentAsString(), exceptedResult);
    }

    void testAddServicePolicy_ThrowsPolicyDuplicateException(ServicePolicy servicePolicy)
            throws Exception {

        // Setup
        mockPoliciesValidateRequest(true);
        String errMsg =
                String.format(
                        "The same policy already exists with id: %s for "
                                + "the registered service template with id: %s.",
                        servicePolicy.getServicePolicyId(), servicePolicy.getServiceTemplateId());
        ErrorResponse result =
                ErrorResponse.errorResponse(ErrorType.POLICY_DUPLICATE, List.of(errMsg));
        String exceptedResult = objectMapper.writeValueAsString(result);

        final ServicePolicyCreateRequest createRequest = new ServicePolicyCreateRequest();
        createRequest.setServiceTemplateId(servicePolicy.getServiceTemplateId());
        createRequest.setPolicy(servicePolicy.getPolicy());
        String requestBody = objectMapper.writeValueAsString(createRequest);

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(
                                post("/xpanse/service/policies")
                                        .content(requestBody)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        // Verify the results
        Assertions.assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST.value());
        Assertions.assertEquals(response.getContentAsString(), exceptedResult);
    }

    void testUpdateServicePolicy(ServicePolicy servicePolicy) throws Exception {

        // Setup
        mockPoliciesValidateRequest(true);

        final ServicePolicyUpdateRequest updateRequest = new ServicePolicyUpdateRequest();
        updateRequest.setPolicy("servicePolicyUpdate");
        updateRequest.setEnabled(true);
        String requestBody = objectMapper.writeValueAsString(updateRequest);

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(
                                put(
                                                "/xpanse/service/policies/{id}",
                                                servicePolicy.getServicePolicyId())
                                        .content(requestBody)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        ServicePolicy updatedServicePolicy =
                objectMapper.readValue(response.getContentAsString(), ServicePolicy.class);

        // Verify the results
        Assertions.assertEquals(response.getStatus(), HttpStatus.OK.value());
        Assertions.assertEquals(
                updatedServicePolicy.getServicePolicyId(), servicePolicy.getServicePolicyId());
        Assertions.assertEquals("servicePolicyUpdate", updatedServicePolicy.getPolicy());
        Assertions.assertTrue(updatedServicePolicy.getEnabled());
    }

    void testUpdateServicePolicy_ThrowsPolicyNotFoundException(UUID uuid) throws Exception {
        // Setup
        mockPoliciesValidateRequest(true);
        String errMsg = String.format("The service policy with id %s not found.", uuid);
        ErrorResponse result =
                ErrorResponse.errorResponse(ErrorType.POLICY_NOT_FOUND, List.of(errMsg));
        String exceptedResult = objectMapper.writeValueAsString(result);

        final ServicePolicyUpdateRequest updateRequest = new ServicePolicyUpdateRequest();
        updateRequest.setPolicy("servicePolicyUpdate");
        String requestBody = objectMapper.writeValueAsString(updateRequest);

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(
                                put("/xpanse/service/policies/{id}", uuid)
                                        .content(requestBody)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        // Verify the results
        Assertions.assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST.value());
        Assertions.assertEquals(response.getContentAsString(), exceptedResult);
    }

    void testDeleteServicePolicy(UUID policyId) throws Exception {
        // Setup
        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(
                                delete("/xpanse/service/policies/{id}", policyId)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        // Verify the results
        Assertions.assertEquals(response.getStatus(), HttpStatus.NO_CONTENT.value());
    }

    void testDeleteServicePolicy_ThrowsPolicyNotFoundException() throws Exception {
        // Setup
        UUID uuid = UUID.randomUUID();
        String errMsg = String.format("The service policy with id %s not found.", uuid);
        ErrorResponse result =
                ErrorResponse.errorResponse(ErrorType.POLICY_NOT_FOUND, List.of(errMsg));
        String exceptedResult = objectMapper.writeValueAsString(result);

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(
                                delete("/xpanse/service/policies/{id}", uuid)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        // Verify the results
        Assertions.assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST.value());
        Assertions.assertEquals(response.getContentAsString(), exceptedResult);
    }
}
