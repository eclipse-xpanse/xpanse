package org.eclipse.xpanse.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.response.ErrorType;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.ReviewServiceTemplateRequest;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceReviewResult;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceTemplateRegistrationState;
import org.eclipse.xpanse.modules.models.servicetemplate.request.ServiceTemplateRequestInfo;
import org.eclipse.xpanse.modules.models.servicetemplate.request.ServiceTemplateRequestToReview;
import org.eclipse.xpanse.modules.models.servicetemplate.request.enums.ServiceTemplateRequestType;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.eclipse.xpanse.runtime.util.ApisTestCommon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=oauth,zitadel,zitadel-testbed,test"})
@AutoConfigureMockMvc
class CspServiceTemplateApiTest extends ApisTestCommon {

    @Test
    @WithJwt(file = "jwt_csp_isv.json")
    void testCspManageServiceTemplates() throws Exception {
        Ocl ocl =
                new OclLoader()
                        .getOcl(
                                URI.create("file:src/test/resources/ocl_terraform_test.yml")
                                        .toURL());
        ocl.setName(UUID.randomUUID().toString());
        MockHttpServletResponse registerResponse = register(ocl);
        assertEquals(HttpStatus.OK.value(), registerResponse.getStatus());
        ServiceTemplateRequestInfo registerRequestInfo =
                objectMapper.readValue(
                        registerResponse.getContentAsString(), ServiceTemplateRequestInfo.class);
        UUID serviceTemplateId = registerRequestInfo.getServiceTemplateId();
        ServiceTemplateDetailVo serviceTemplate =
                getRegistrationDetailsByServiceTemplateId(serviceTemplateId);
        testListManagedServiceTemplatesWithStateApprovalPending(serviceTemplate);
        testCspManageServiceTemplatesWell(serviceTemplateId, ocl);
        testCspManageServiceTemplatesBelongToOtherCsp(serviceTemplateId);
    }

    MockHttpServletResponse register(Ocl ocl) throws Exception {
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        String requestBody = yamlMapper.writeValueAsString(ocl);
        return mockMvc.perform(
                        post("/xpanse/service_templates")
                                .content(requestBody)
                                .contentType("application/x-yaml")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
    }

    MockHttpServletResponse update(UUID id, boolean isRemoveServiceTemplateUntilApproved, Ocl ocl)
            throws Exception {
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        String requestBody = yamlMapper.writeValueAsString(ocl);
        return mockMvc.perform(
                        put("/xpanse/service_templates/{id}", id)
                                .param(
                                        "isRemoveServiceTemplateUntilApproved",
                                        String.valueOf(isRemoveServiceTemplateUntilApproved))
                                .content(requestBody)
                                .contentType("application/x-yaml")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
    }

    MockHttpServletResponse unregister(UUID id) throws Exception {
        return mockMvc.perform(
                        put("/xpanse/service_templates/remove_from_catalog/{id}", id)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
    }

    MockHttpServletResponse reAddToCatalog(UUID id) throws Exception {
        return mockMvc.perform(
                        put("/xpanse/service_templates/re_add_to_catalog/{id}", id)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
    }

    void testCspManageServiceTemplatesWell(UUID serviceTemplateId, Ocl ocl) throws Exception {
        // List pending service template requests
        List<ServiceTemplateRequestToReview> pendingServiceTemplateRequests =
                listPendingServiceTemplateRequests(serviceTemplateId);
        assertEquals(1, pendingServiceTemplateRequests.size());
        ServiceTemplateRequestToReview registerRequest = pendingServiceTemplateRequests.getFirst();
        assertEquals(ServiceTemplateRequestType.REGISTER, registerRequest.getRequestType());
        UUID registerRequestId = registerRequest.getRequestId();

        // review service template register request
        ReviewServiceTemplateRequest reviewRequest = new ReviewServiceTemplateRequest();
        reviewRequest.setReviewResult(ServiceReviewResult.APPROVED);
        MockHttpServletResponse reviewResponse =
                reviewServiceTemplateRequest(registerRequestId, reviewRequest);
        assertEquals(HttpStatus.NO_CONTENT.value(), reviewResponse.getStatus());

        String errorMessage = "Service template request is not allowed to be reviewed.";
        MockHttpServletResponse reviewAgainResponse =
                reviewServiceTemplateRequest(registerRequestId, reviewRequest);
        assertEquals(HttpStatus.BAD_REQUEST.value(), reviewAgainResponse.getStatus());
        ErrorResponse errorResponse =
                objectMapper.readValue(
                        reviewAgainResponse.getContentAsString(), ErrorResponse.class);
        assertEquals(
                ErrorType.REVIEW_SERVICE_TEMPLATE_REQUEST_NOT_ALLOWED,
                errorResponse.getErrorType());
        assertEquals(errorResponse.getDetails(), List.of(errorMessage));

        // List pending service template requests
        pendingServiceTemplateRequests = listPendingServiceTemplateRequests(serviceTemplateId);
        assertThat(pendingServiceTemplateRequests).isEmpty();

        testListManagedServiceTemplatesWithStateApproved();

        // Update service template
        String descriptionToUpdate = "update-test";
        ocl.setDescription(descriptionToUpdate);
        MockHttpServletResponse updateResponse = update(serviceTemplateId, true, ocl);
        ServiceTemplateRequestInfo updateRequestInfo =
                objectMapper.readValue(
                        updateResponse.getContentAsString(), ServiceTemplateRequestInfo.class);
        // List pending service template requests
        List<ServiceTemplateRequestToReview> pendingServiceTemplateRequests1 =
                listPendingServiceTemplateRequests(serviceTemplateId);
        assertEquals(1, pendingServiceTemplateRequests1.size());
        ServiceTemplateRequestToReview updateRequest = pendingServiceTemplateRequests1.getFirst();
        assertEquals(ServiceTemplateRequestType.UPDATE, updateRequest.getRequestType());
        assertEquals(
                updateRequestInfo.getServiceTemplateId(), updateRequest.getServiceTemplateId());
        assertEquals(updateRequestInfo.getRequestId(), updateRequest.getRequestId());

        // The service template should not be updated yet
        ServiceTemplateDetailVo serviceTemplate =
                getRegistrationDetailsByServiceTemplateId(serviceTemplateId);
        assertEquals(updateRequest.getOcl().getDescription(), descriptionToUpdate);
        assertNotEquals(descriptionToUpdate, serviceTemplate.getDescription());
        assertFalse(serviceTemplate.getIsAvailableInCatalog());

        // review service template update request
        reviewRequest.setReviewComment("Approve to update description");
        reviewResponse = reviewServiceTemplateRequest(updateRequest.getRequestId(), reviewRequest);
        assertEquals(HttpStatus.NO_CONTENT.value(), reviewResponse.getStatus());

        pendingServiceTemplateRequests1 = listPendingServiceTemplateRequests(serviceTemplateId);
        assertThat(pendingServiceTemplateRequests1).isEmpty();
        // After update is approved, the service template should be updated
        serviceTemplate = getRegistrationDetailsByServiceTemplateId(serviceTemplateId);
        assertEquals(updateRequest.getOcl().getDescription(), descriptionToUpdate);
        assertEquals(descriptionToUpdate, serviceTemplate.getDescription());
        assertTrue(serviceTemplate.getIsAvailableInCatalog());

        // Unregister service template
        MockHttpServletResponse unregisterResponse = unregister(serviceTemplateId);
        ServiceTemplateRequestInfo unregisterRequestInfo =
                objectMapper.readValue(
                        unregisterResponse.getContentAsString(), ServiceTemplateRequestInfo.class);

        assertNotNull(unregisterRequestInfo);
        // List pending service template requests is empty. Because of the unregister request is
        // always approved.
        List<ServiceTemplateRequestToReview> pendingServiceTemplateRequests2 =
                listPendingServiceTemplateRequests(serviceTemplateId);
        assertThat(pendingServiceTemplateRequests2).isEmpty();
        serviceTemplate = getRegistrationDetailsByServiceTemplateId(serviceTemplateId);
        assertFalse(serviceTemplate.getIsAvailableInCatalog());

        // re-add_to_catalog service template
        MockHttpServletResponse reAddToCatalogResponse = reAddToCatalog(serviceTemplateId);
        ServiceTemplateRequestInfo reAddToCatalogRequestInfo =
                objectMapper.readValue(
                        reAddToCatalogResponse.getContentAsString(),
                        ServiceTemplateRequestInfo.class);
        // List pending service template requests
        List<ServiceTemplateRequestToReview> pendingServiceTemplateRequests3 =
                listPendingServiceTemplateRequests(serviceTemplateId);
        assertEquals(1, pendingServiceTemplateRequests3.size());
        ServiceTemplateRequestToReview reAddToCatalogRequest =
                pendingServiceTemplateRequests3.getFirst();
        assertEquals(
                ServiceTemplateRequestType.RE_ADD_TO_CATALOG,
                reAddToCatalogRequest.getRequestType());
        assertEquals(
                reAddToCatalogRequestInfo.getServiceTemplateId(),
                reAddToCatalogRequest.getServiceTemplateId());
        assertEquals(
                reAddToCatalogRequestInfo.getRequestId(), reAddToCatalogRequest.getRequestId());

        // review service template re-add_to_catalog request
        reviewRequest.setReviewResult(ServiceReviewResult.REJECTED);
        reviewRequest.setReviewComment("reject the re-add_to_catalog");
        reviewResponse =
                reviewServiceTemplateRequest(reAddToCatalogRequest.getRequestId(), reviewRequest);
        assertEquals(HttpStatus.NO_CONTENT.value(), reviewResponse.getStatus());
        serviceTemplate = getRegistrationDetailsByServiceTemplateId(serviceTemplateId);
        assertFalse(serviceTemplate.getIsAvailableInCatalog());

        pendingServiceTemplateRequests3 = listPendingServiceTemplateRequests(serviceTemplateId);
        assertThat(pendingServiceTemplateRequests3).isEmpty();
    }

    void testCspManageServiceTemplatesBelongToOtherCsp(UUID serviceTemplateId) throws Exception {
        // change csp of service template
        ServiceTemplateEntity serviceTemplate =
                serviceTemplateStorage.getServiceTemplateById(serviceTemplateId);
        serviceTemplate.setCsp(Csp.FLEXIBLE_ENGINE);
        serviceTemplate.getOcl().getCloudServiceProvider().setName(Csp.FLEXIBLE_ENGINE);
        serviceTemplate = serviceTemplateStorage.storeAndFlush(serviceTemplate);

        MockHttpServletResponse reAddToCatalogResponse = reAddToCatalog(serviceTemplateId);
        ServiceTemplateRequestInfo reAddToCatalogRequestInfo =
                objectMapper.readValue(
                        reAddToCatalogResponse.getContentAsString(),
                        ServiceTemplateRequestInfo.class);
        assertNotNull(reAddToCatalogRequestInfo.getRequestId());
        List<ServiceTemplateRequestToReview> pendingServiceTemplateRequests =
                listPendingServiceTemplateRequests(serviceTemplateId);
        assertThat(pendingServiceTemplateRequests).isEmpty();

        testReviewRegistrationThrowsAccessDeniedException(reAddToCatalogRequestInfo.getRequestId());
        testGetRegistrationDetailsThrowsAccessDeniedException(serviceTemplateId);
        testListManagedServiceTemplatesReturnsEmptyList(serviceTemplate);
        deleteServiceTemplate(serviceTemplateId);
    }

    ServiceTemplateDetailVo getRegistrationDetailsByServiceTemplateId(UUID serviceTemplateId)
            throws Exception {
        final MockHttpServletResponse detailResponse = getRegistrationDetails(serviceTemplateId);
        return objectMapper.readValue(
                detailResponse.getContentAsString(), ServiceTemplateDetailVo.class);
    }

    void testGetRegistrationDetailsThrowsAccessDeniedException(UUID serviceTemplateId)
            throws Exception {

        ErrorResponse accessDeniedErrorResponse =
                ErrorResponse.errorResponse(
                        ErrorType.ACCESS_DENIED,
                        Collections.singletonList(
                                "No permissions to review service template "
                                        + "belonging to other cloud service providers."));
        // Run the test detail
        final MockHttpServletResponse detailResponse = getRegistrationDetails(serviceTemplateId);
        // Verify the results
        assertEquals(HttpStatus.FORBIDDEN.value(), detailResponse.getStatus());
        assertEquals(
                objectMapper.writeValueAsString(accessDeniedErrorResponse),
                detailResponse.getContentAsString());
    }

    void testReviewRegistrationThrowsAccessDeniedException(UUID requestId) throws Exception {

        // Setup request
        ErrorResponse accessDeniedErrorResponse =
                ErrorResponse.errorResponse(
                        ErrorType.ACCESS_DENIED,
                        Collections.singletonList(
                                "No permissions to review service template "
                                        + "request belonging to other cloud service providers."));
        ReviewServiceTemplateRequest request = new ReviewServiceTemplateRequest();
        request.setReviewResult(ServiceReviewResult.APPROVED);
        request.setReviewComment("reviewComment");
        // Run the test case 1
        final MockHttpServletResponse response = reviewServiceTemplateRequest(requestId, request);
        // Verify the results
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
        assertEquals(
                objectMapper.writeValueAsString(accessDeniedErrorResponse),
                response.getContentAsString());
    }

    void testListManagedServiceTemplatesWithStateApproved() throws Exception {

        // Setup request 1
        String serviceRegistrationState1 = "errorState";
        String errorMessage = "Failed to convert value of type 'java.lang.String' to required type";

        // Run the test case 1
        MockHttpServletResponse response1 =
                listServiceTemplatesWithParams(null, null, null, null, serviceRegistrationState1);
        // Verify the results 1
        assertThat(response1.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
        assertThat(errorMessage).isSubstringOf(response1.getContentAsString());

        // Setup request 2
        String serviceRegistrationState2 = ServiceTemplateRegistrationState.APPROVED.toValue();
        // Run the test case 2
        MockHttpServletResponse response2 =
                listServiceTemplatesWithParams(null, null, null, null, serviceRegistrationState2);
        List<ServiceTemplateDetailVo> detailsList2 =
                objectMapper.readValue(response2.getContentAsString(), new TypeReference<>() {});
        // Verify the results 2
        assertThat(response2.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(detailsList2).isNotEmpty();
    }

    void testListManagedServiceTemplatesWithStateApprovalPending(
            ServiceTemplateDetailVo serviceTemplateDetailVo) throws Exception {
        // Setup
        String serviceRegistrationState = ServiceTemplateRegistrationState.IN_REVIEW.toValue();
        // Run the test
        MockHttpServletResponse response =
                listServiceTemplatesWithParams(
                        serviceTemplateDetailVo.getCategory().toValue(),
                        serviceTemplateDetailVo.getName(),
                        serviceTemplateDetailVo.getVersion(),
                        serviceTemplateDetailVo.getServiceHostingType().toValue(),
                        serviceRegistrationState);
        List<ServiceTemplateDetailVo> detailsList =
                objectMapper.readValue(response.getContentAsString(), new TypeReference<>() {});
        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(detailsList).isNotEmpty();
    }

    void testListManagedServiceTemplatesReturnsEmptyList(ServiceTemplateEntity serviceTemplate)
            throws Exception {
        // SetupServiceTemplateApiTest
        String serviceRegistrationState = ServiceTemplateRegistrationState.IN_REVIEW.toValue();
        // Run the test
        MockHttpServletResponse response =
                listServiceTemplatesWithParams(
                        serviceTemplate.getCategory().toValue(),
                        serviceTemplate.getName(),
                        serviceTemplate.getVersion(),
                        serviceTemplate.getServiceHostingType().toValue(),
                        serviceRegistrationState);
        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("[]");
    }

    MockHttpServletResponse getRegistrationDetails(UUID id) throws Exception {
        return mockMvc.perform(
                        get("/xpanse/csp/service_templates/{id}", id)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
    }

    MockHttpServletResponse listServiceTemplatesWithParams(
            String categoryName,
            String serviceName,
            String serviceVersion,
            String serviceHostingType,
            String serviceTemplateRegistrationState)
            throws Exception {
        MockHttpServletRequestBuilder getRequestBuilder = get("/xpanse/csp/service_templates");
        if (StringUtils.isNotBlank(categoryName)) {
            getRequestBuilder = getRequestBuilder.param("categoryName", categoryName);
        }
        if (StringUtils.isNotBlank(serviceName)) {
            getRequestBuilder = getRequestBuilder.param("serviceName", serviceName);
        }
        if (StringUtils.isNotBlank(serviceVersion)) {
            getRequestBuilder = getRequestBuilder.param("serviceVersion", serviceVersion);
        }
        if (StringUtils.isNotBlank(serviceHostingType)) {
            getRequestBuilder = getRequestBuilder.param("serviceHostingType", serviceHostingType);
        }
        if (StringUtils.isNotBlank(serviceTemplateRegistrationState)) {
            getRequestBuilder =
                    getRequestBuilder.param(
                            "serviceTemplateRegistrationState", serviceTemplateRegistrationState);
        }
        return mockMvc.perform(getRequestBuilder).andReturn().getResponse();
    }

    MockHttpServletResponse reviewServiceTemplateRequest(
            UUID requestId, ReviewServiceTemplateRequest request) throws Exception {
        String requestBody = objectMapper.writeValueAsString(request);
        return mockMvc.perform(
                        put("/xpanse/csp/service_templates/requests/review/{requestId}", requestId)
                                .content(requestBody)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
    }
}
