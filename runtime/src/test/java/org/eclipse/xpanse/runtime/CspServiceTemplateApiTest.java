package org.eclipse.xpanse.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.fasterxml.jackson.core.type.TypeReference;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.response.ErrorType;
import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.ReviewRegistrationRequest;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceReviewResult;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceTemplateRegistrationState;
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
    @WithJwt(file = "jwt_admin_csp.json")
    void testCspManageServiceTemplates() throws Exception {
        Ocl ocl = new OclLoader().getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.setName("cspServiceTemplateApiTest-1");
        ServiceTemplateDetailVo serviceTemplate = registerServiceTemplate(ocl);
        if (Objects.isNull(serviceTemplate)) {
            return;
        }
        testGetRegistrationDetails(serviceTemplate);
        testListManagedServiceTemplatesWithStateApprovalPending(serviceTemplate);
        testReviewRegistration(serviceTemplate);
        final MockHttpServletResponse registrationDetails =
                getRegistrationDetails(serviceTemplate.getServiceTemplateId());
        serviceTemplate = objectMapper.readValue(registrationDetails.getContentAsString()
                , ServiceTemplateDetailVo.class);
        testListManagedServiceTemplatesWithStateApproved();
        deleteServiceTemplate(serviceTemplate.getServiceTemplateId());
    }

    @Test
    @WithJwt(file = "jwt_admin_csp.json")
    void testCspManageServiceTemplatesWithoutCsp() throws Exception {
        Ocl ocl = new OclLoader().getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.getCloudServiceProvider().setName(Csp.FLEXIBLE_ENGINE);
        ocl.setName("cspServiceTemplateApiTest-2");
        ServiceTemplateDetailVo serviceTemplate = registerServiceTemplate(ocl);
        if (Objects.isNull(serviceTemplate)) {
            return;
        }
        testGetRegistrationDetailsThrowsAccessDeniedException(serviceTemplate);
        testReviewRegistrationThrowsAccessDeniedException(serviceTemplate);
        testListManagedServiceTemplatesReturnsEmptyList(serviceTemplate);
        deleteServiceTemplate(serviceTemplate.getServiceTemplateId());
    }

    void testGetRegistrationDetails(ServiceTemplateDetailVo serviceTemplateDetailVo)
            throws Exception {
        // Setup detail request
        UUID id = serviceTemplateDetailVo.getServiceTemplateId();
        String result = objectMapper.writeValueAsString(serviceTemplateDetailVo);
        // Run the test
        final MockHttpServletResponse detailResponse = getRegistrationDetails(id);
        // Verify the results
        assertEquals(HttpStatus.OK.value(), detailResponse.getStatus());
        assertEquals(result, detailResponse.getContentAsString());
    }

    void testGetRegistrationDetailsThrowsAccessDeniedException(
            ServiceTemplateDetailVo serviceTemplateDetailVo)
            throws Exception {

        ErrorResponse accessDeniedErrorResponse = ErrorResponse.errorResponse(ErrorType.ACCESS_DENIED,
                Collections.singletonList("No permissions to review service template "
                        + "belonging to other cloud service providers."));
        // Setup detail request
        UUID id = serviceTemplateDetailVo.getServiceTemplateId();
        // Run the test detail
        final MockHttpServletResponse detailResponse = getRegistrationDetails(id);
        // Verify the results
        assertEquals(HttpStatus.FORBIDDEN.value(), detailResponse.getStatus());
        assertEquals(objectMapper.writeValueAsString(accessDeniedErrorResponse),
                detailResponse.getContentAsString());
    }

    void testReviewRegistration(ServiceTemplateDetailVo serviceTemplateDetailVo)
            throws Exception {
        // Setup request 1
        UUID id1 = serviceTemplateDetailVo.getServiceTemplateId();
        ReviewRegistrationRequest request1 = new ReviewRegistrationRequest();
        request1.setReviewResult(ServiceReviewResult.APPROVED);
        request1.setReviewComment("reviewComment");
        // Run the test case 1
        final MockHttpServletResponse response1 =
                reviewServiceRegistrationWithParams(id1, request1);
        // Verify the result 1
        assertThat(response1.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
        assertThat(response1.getContentAsString()).isEmpty();

        // Setup request 2
        ReviewRegistrationRequest request2 = new ReviewRegistrationRequest();
        request2.setReviewResult(ServiceReviewResult.REJECTED);
        request2.setReviewComment("reviewComment");
        ErrorResponse expectedErrorResponse2 =
                ErrorResponse.errorResponse(ErrorType.SERVICE_TEMPLATE_ALREADY_REVIEWED,
                        Collections.singletonList(
                                String.format("Service template with id %s already reviewed.",
                                        id1)));
        String result2 = objectMapper.writeValueAsString(expectedErrorResponse2);
        // Run the test case 2
        final MockHttpServletResponse response2 =
                reviewServiceRegistrationWithParams(serviceTemplateDetailVo.getServiceTemplateId(),
                        request2);
        // Verify the result 2
        assertThat(response2.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response2.getContentAsString()).isEqualTo(result2);

        // Setup request 3
        UUID id3 = UUID.randomUUID();
        ReviewRegistrationRequest request3 = new ReviewRegistrationRequest();
        request3.setReviewResult(ServiceReviewResult.APPROVED);
        request3.setReviewComment("reviewComment");
        ErrorResponse expectedErrorResponse3 =
                ErrorResponse.errorResponse(ErrorType.SERVICE_TEMPLATE_NOT_REGISTERED,
                        Collections.singletonList(
                                String.format("Service template with id %s not found.", id3)));
        String result3 = objectMapper.writeValueAsString(expectedErrorResponse3);
        // Run the test case 3
        final MockHttpServletResponse response3 =
                reviewServiceRegistrationWithParams(id3, request3);
        // Verify the result 3
        assertThat(response3.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response3.getContentAsString()).isEqualTo(result3);
    }


    void testReviewRegistrationThrowsAccessDeniedException(
            ServiceTemplateDetailVo serviceTemplateDetailVo)
            throws Exception {

        // Setup request
        ErrorResponse accessDeniedErrorResponse = ErrorResponse.errorResponse(ErrorType.ACCESS_DENIED,
                Collections.singletonList("No permissions to review service template "
                        + "belonging to other cloud service providers."));
        UUID id = serviceTemplateDetailVo.getServiceTemplateId();
        ReviewRegistrationRequest request = new ReviewRegistrationRequest();
        request.setReviewResult(ServiceReviewResult.APPROVED);
        request.setReviewComment("reviewComment");
        // Run the test case 1
        final MockHttpServletResponse response =
                reviewServiceRegistrationWithParams(id, request);
        // Verify the result 1
        // Verify the results
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
        assertEquals(objectMapper.writeValueAsString(accessDeniedErrorResponse),
                response.getContentAsString());
    }

    void testListManagedServiceTemplatesWithStateApproved() throws Exception {

        // Setup request 1
        String serviceRegistrationState1 = "errorState";
        String errorMessage = "Failed to convert value of type 'java.lang.String' to required type";

        // Run the test case 1
        MockHttpServletResponse response1 = listServiceTemplatesWithParams(
                null, null, null, null, serviceRegistrationState1);
        // Verify the results 1
        assertThat(response1.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
        assertThat(errorMessage).isSubstringOf(response1.getContentAsString());

        // Setup request 2
        String serviceRegistrationState2 = ServiceTemplateRegistrationState.APPROVED.toValue();
        // Run the test case 2
        MockHttpServletResponse response2 = listServiceTemplatesWithParams(
                null, null, null, null, serviceRegistrationState2);
        List<ServiceTemplateDetailVo> detailsList2 =
                objectMapper.readValue(response2.getContentAsString(),
                        new TypeReference<>() {
                        });
        // Verify the results 2
        assertThat(response2.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(detailsList2).isNotEmpty();
    }

    void testListManagedServiceTemplatesWithStateApprovalPending(
            ServiceTemplateDetailVo serviceTemplateDetailVo) throws Exception {
        // Setup
        String serviceRegistrationState = ServiceTemplateRegistrationState.IN_PROGRESS.toValue();
        // Run the test
        MockHttpServletResponse response = listServiceTemplatesWithParams(
                serviceTemplateDetailVo.getCategory().toValue(),
                serviceTemplateDetailVo.getName(),
                serviceTemplateDetailVo.getVersion(),
                serviceTemplateDetailVo.getServiceHostingType().toValue(),
                serviceRegistrationState);
        List<ServiceTemplateDetailVo> detailsList =
                objectMapper.readValue(response.getContentAsString(),
                        new TypeReference<>() {
                        });
        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(detailsList).isNotEmpty();
    }

    void testListManagedServiceTemplatesReturnsEmptyList(
            ServiceTemplateDetailVo serviceTemplateDetailVo) throws Exception {
        // Setup
        String serviceRegistrationState = ServiceTemplateRegistrationState.IN_PROGRESS.toValue();
        // Run the test
        MockHttpServletResponse response = listServiceTemplatesWithParams(
                serviceTemplateDetailVo.getCategory().toValue(),
                serviceTemplateDetailVo.getName(),
                serviceTemplateDetailVo.getVersion(),
                serviceTemplateDetailVo.getServiceHostingType().toValue(),
                serviceRegistrationState);
        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("[]");
    }

    MockHttpServletResponse getRegistrationDetails(UUID id) throws Exception {
        return mockMvc.perform(get("/xpanse/csp/service_templates/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }

    MockHttpServletResponse listServiceTemplatesWithParams(String categoryName,
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
            getRequestBuilder = getRequestBuilder.param("serviceTemplateRegistrationState",
                    serviceTemplateRegistrationState);
        }
        return mockMvc.perform(getRequestBuilder).andReturn().getResponse();
    }

    MockHttpServletResponse reviewServiceRegistrationWithParams(UUID id,
                                                                ReviewRegistrationRequest request)
            throws Exception {
        String requestBody = objectMapper.writeValueAsString(request);
        return mockMvc.perform(put("/xpanse/service_templates/review/{id}", id).content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }
}
