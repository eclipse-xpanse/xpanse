/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentStorage;
import org.eclipse.xpanse.modules.database.service.ServiceQueryModel;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.models.billing.PriceWithRegion;
import org.eclipse.xpanse.modules.models.billing.enums.BillingMode;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.common.enums.UserOperation;
import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.response.ErrorType;
import org.eclipse.xpanse.modules.models.servicetemplate.AvailabilityZoneConfig;
import org.eclipse.xpanse.modules.models.servicetemplate.DeployVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.ModificationImpact;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceFlavorWithPrice;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployVariableDataType;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployVariableKind;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceTemplateRegistrationState;
import org.eclipse.xpanse.modules.models.servicetemplate.request.ServiceTemplateRequestHistory;
import org.eclipse.xpanse.modules.models.servicetemplate.request.ServiceTemplateRequestInfo;
import org.eclipse.xpanse.modules.models.servicetemplate.request.enums.ServiceTemplateRequestStatus;
import org.eclipse.xpanse.modules.models.servicetemplate.request.enums.ServiceTemplateRequestType;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.eclipse.xpanse.runtime.util.ApisTestCommon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.semver4j.Semver;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** Test for ServiceTemplateManageApi. */
@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(
        properties = {
            "spring.profiles.active=oauth,zitadel,zitadel-testbed,test,dev",
            "huaweicloud.auto.approve.service.template.enabled=false"
        })
@AutoConfigureMockMvc
class ServiceTemplateApiTest extends ApisTestCommon {

    private final OclLoader oclLoader = new OclLoader();
    @MockitoBean private ServiceDeploymentStorage mockServiceDeploymentStorage;

    @Test
    @WithJwt(file = "jwt_csp_isv.json")
    void testManageApisWorkWell() throws Exception {
        testServiceTemplateApisWorkWell();
        testFetchApisWorkWell();
    }

    void testServiceTemplateApisWorkWell() throws Exception {
        // Setup register request
        Ocl ocl =
                oclLoader.getOcl(
                        URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.setName(UUID.randomUUID().toString());
        // Run the test
        final MockHttpServletResponse registerResponse = register(ocl);
        ServiceTemplateRequestInfo registerRequestInfo =
                objectMapper.readValue(
                        registerResponse.getContentAsString(), ServiceTemplateRequestInfo.class);
        // Verify the results
        assertEquals(HttpStatus.OK.value(), registerResponse.getStatus());
        assertNotNull(registerRequestInfo.getServiceTemplateId());
        assertNotNull(registerRequestInfo.getRequestId());
        assertTrue(registerRequestInfo.isRequestSubmittedForReview());
        UUID serviceTemplateId = registerRequestInfo.getServiceTemplateId();

        // Setup detail request
        final MockHttpServletResponse detailResponse = detail(serviceTemplateId);
        assertEquals(HttpStatus.OK.value(), detailResponse.getStatus());
        ServiceTemplateDetailVo serviceTemplateDetailVo =
                objectMapper.readValue(
                        detailResponse.getContentAsString(), ServiceTemplateDetailVo.class);
        assertEquals(ocl.getCategory(), serviceTemplateDetailVo.getCategory());
        assertEquals(ocl.getCloudServiceProvider().getName(), serviceTemplateDetailVo.getCsp());
        assertEquals(ocl.getName().toLowerCase(Locale.ROOT), serviceTemplateDetailVo.getName());
        assertEquals(
                new Semver(ocl.getServiceVersion()).getVersion(),
                serviceTemplateDetailVo.getVersion());
        assertEquals(
                ServiceTemplateRegistrationState.IN_REVIEW,
                serviceTemplateDetailVo.getServiceTemplateRegistrationState());
        assertTrue(serviceTemplateDetailVo.getIsReviewInProgress());
        assertFalse(serviceTemplateDetailVo.getIsAvailableInCatalog());

        // Setup listRequests
        final MockHttpServletResponse listRegisterHistoryResponse =
                listServiceTemplateHistoryWithParams(
                        serviceTemplateId,
                        ServiceTemplateRequestType.REGISTER.toValue(),
                        ServiceTemplateRequestStatus.IN_REVIEW.toValue());
        assertEquals(HttpStatus.OK.value(), listRegisterHistoryResponse.getStatus());
        List<ServiceTemplateRequestHistory> registerHistoryVos =
                objectMapper.readValue(
                        listRegisterHistoryResponse.getContentAsString(), new TypeReference<>() {});
        assertEquals(1, registerHistoryVos.size());
        assertEquals(
                registerRequestInfo.getRequestId(), registerHistoryVos.getFirst().getRequestId());

        // Setup list history
        // Run the test
        final MockHttpServletResponse getHistoryRequestResponse =
                getServiceTemplateRequestByRequestId(registerRequestInfo.getRequestId());
        // Verify the results
        assertEquals(HttpStatus.OK.value(), getHistoryRequestResponse.getStatus());
        Ocl requestOcl =
                objectMapper.readValue(getHistoryRequestResponse.getContentAsString(), Ocl.class);
        assertNotNull(requestOcl);
        assertEquals(ocl.getCategory(), requestOcl.getCategory());
        assertEquals(
                ocl.getCloudServiceProvider().getName(),
                requestOcl.getCloudServiceProvider().getName());
        assertEquals(ocl.getName(), requestOcl.getName());
        assertEquals(ocl.getServiceVersion(), requestOcl.getServiceVersion());

        // Setup list request
        List<ServiceTemplateDetailVo> serviceTemplateDetailVos = List.of(serviceTemplateDetailVo);
        // Run the test
        final MockHttpServletResponse response =
                listServiceTemplatesWithParams(
                        ocl.getCategory().toValue(),
                        serviceTemplateDetailVo.getCsp().toValue(),
                        ocl.getName(),
                        serviceTemplateDetailVo.getVersion(),
                        ocl.getServiceHostingType().toValue(),
                        ServiceTemplateRegistrationState.IN_REVIEW.toValue(),
                        false,
                        true);
        // Verify the results
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertThat(serviceTemplateDetailVos)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("lastModifiedTime")
                .isEqualTo(
                        Arrays.stream(
                                        objectMapper.readValue(
                                                response.getContentAsString(),
                                                ServiceTemplateDetailVo[].class))
                                .toList());

        // Setup update request when the register request is in-review
        ocl.setDescription("update-test");
        // Run the update test with 'isUnpublishUntilApproved' is false
        boolean isUnpublishUntilApproved = true;
        final MockHttpServletResponse updateResponse =
                update(serviceTemplateId, isUnpublishUntilApproved, ocl);
        // Verify the results
        assertEquals(HttpStatus.OK.value(), updateResponse.getStatus());
        ServiceTemplateRequestInfo updateRequestInfo =
                objectMapper.readValue(
                        updateResponse.getContentAsString(), ServiceTemplateRequestInfo.class);
        assertTrue(updateRequestInfo.isRequestSubmittedForReview());
        ServiceTemplateDetailVo updatedServiceTemplateDetailVo =
                getServiceTemplateDetailsVo(updateRequestInfo.getServiceTemplateId());
        assertTrue(updatedServiceTemplateDetailVo.getIsReviewInProgress());
        assertFalse(updatedServiceTemplateDetailVo.getIsAvailableInCatalog());
        assertEquals(
                ServiceTemplateRegistrationState.IN_REVIEW,
                serviceTemplateDetailVo.getServiceTemplateRegistrationState());

        // Setup list request history.
        final MockHttpServletResponse listRequestHistoryResponse =
                listServiceTemplateHistoryWithParams(serviceTemplateId, null, null);
        assertEquals(HttpStatus.OK.value(), listRequestHistoryResponse.getStatus());
        List<ServiceTemplateRequestHistory> requestHistoryVos =
                objectMapper.readValue(
                        listRequestHistoryResponse.getContentAsString(), new TypeReference<>() {});
        assertEquals(2, requestHistoryVos.size());

        ServiceTemplateRequestHistory oldRegisterHistory = requestHistoryVos.getLast();
        assertEquals(registerRequestInfo.getRequestId(), oldRegisterHistory.getRequestId());
        assertEquals(ServiceTemplateRequestStatus.CANCELLED, oldRegisterHistory.getRequestStatus());

        ServiceTemplateRequestHistory newRegisterHistory = requestHistoryVos.getFirst();
        assertEquals(updateRequestInfo.getRequestId(), newRegisterHistory.getRequestId());
        assertEquals(ServiceTemplateRequestStatus.IN_REVIEW, newRegisterHistory.getRequestStatus());

        // approve update (new register) request
        reviewServiceTemplateRequest(updateRequestInfo.getRequestId(), true);

        // Setup unpublish request
        // Run the test
        final MockHttpServletResponse unpublishResponse = unpublish(serviceTemplateId);
        // Verify the results
        assertEquals(HttpStatus.OK.value(), unpublishResponse.getStatus());
        ServiceTemplateRequestInfo unpublishRequestInfo =
                objectMapper.readValue(
                        unpublishResponse.getContentAsString(), ServiceTemplateRequestInfo.class);
        assertFalse(unpublishRequestInfo.isRequestSubmittedForReview());
        ServiceTemplateDetailVo unpublishedServiceTemplateDetailVo =
                getServiceTemplateDetailsVo(unpublishRequestInfo.getServiceTemplateId());
        assertFalse(unpublishedServiceTemplateDetailVo.getIsAvailableInCatalog());

        // Setup listRequests request
        final MockHttpServletResponse listUnregisterHistoryResponse =
                listServiceTemplateHistoryWithParams(
                        serviceTemplateId,
                        ServiceTemplateRequestType.UNPUBLISH.toValue(),
                        ServiceTemplateRequestStatus.ACCEPTED.toValue());
        assertEquals(HttpStatus.OK.value(), listUnregisterHistoryResponse.getStatus());
        List<ServiceTemplateRequestHistory> unpublishHistoryVos =
                objectMapper.readValue(
                        listUnregisterHistoryResponse.getContentAsString(),
                        new TypeReference<>() {});
        assertEquals(1, unpublishHistoryVos.size());
        assertEquals(
                unpublishRequestInfo.getRequestId(), unpublishHistoryVos.getFirst().getRequestId());

        // Setup republish request
        // Run the test
        final MockHttpServletResponse republishResponse = republish(serviceTemplateId);
        // Verify the results
        assertEquals(HttpStatus.OK.value(), republishResponse.getStatus());
        ServiceTemplateRequestInfo republishRequestInfo =
                objectMapper.readValue(
                        republishResponse.getContentAsString(), ServiceTemplateRequestInfo.class);
        assertTrue(republishRequestInfo.isRequestSubmittedForReview());
        ServiceTemplateDetailVo republishedServiceTemplateDetailVo =
                getServiceTemplateDetailsVo(republishRequestInfo.getServiceTemplateId());
        assertFalse(republishedServiceTemplateDetailVo.getIsAvailableInCatalog());

        // Setup listRequests request
        final MockHttpServletResponse republishHistoryResponse =
                listServiceTemplateHistoryWithParams(
                        serviceTemplateId,
                        ServiceTemplateRequestType.REPUBLISH.toValue(),
                        ServiceTemplateRequestStatus.IN_REVIEW.toValue());
        assertEquals(HttpStatus.OK.value(), republishHistoryResponse.getStatus());
        List<ServiceTemplateRequestHistory> republishHistoryVos =
                objectMapper.readValue(
                        republishHistoryResponse.getContentAsString(), new TypeReference<>() {});
        assertEquals(1, republishHistoryVos.size());
        assertEquals(
                republishRequestInfo.getRequestId(), republishHistoryVos.getFirst().getRequestId());
        // reject republish request
        reviewServiceTemplateRequest(republishRequestInfo.getRequestId(), false);
        // Setup delete request
        // Run the test
        final MockHttpServletResponse deleteResponse = deleteTemplate(serviceTemplateId);
        // Verify the results
        assertEquals(HttpStatus.NO_CONTENT.value(), deleteResponse.getStatus());
    }

    void updateServiceTemplateState(
            UUID id, ServiceTemplateRegistrationState state, boolean isAvailableInCatalog) {
        ServiceTemplateEntity serviceTemplateEntity =
                serviceTemplateStorage.getServiceTemplateById(id);
        serviceTemplateEntity.setServiceTemplateRegistrationState(state);
        serviceTemplateEntity.setIsAvailableInCatalog(isAvailableInCatalog);
        serviceTemplateStorage.storeAndFlush(serviceTemplateEntity);
    }

    void testFetchApisWorkWell() throws Exception {
        // Setup fetch request
        URL url = URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL();
        Ocl ocl = oclLoader.getOcl(url);
        // Run the test
        final MockHttpServletResponse fetchRegisterResponse = fetch(url.toString());
        ServiceTemplateRequestInfo registerRequestInfo =
                objectMapper.readValue(
                        fetchRegisterResponse.getContentAsString(),
                        ServiceTemplateRequestInfo.class);
        // Verify the results
        assertEquals(HttpStatus.OK.value(), fetchRegisterResponse.getStatus());
        assertTrue(registerRequestInfo.isRequestSubmittedForReview());
        UUID serviceTemplateId = registerRequestInfo.getServiceTemplateId();

        // Setup detail request
        final MockHttpServletResponse detailResponse = detail(serviceTemplateId);
        // Verify the results
        assertEquals(HttpStatus.OK.value(), detailResponse.getStatus());
        ServiceTemplateDetailVo serviceTemplateDetailVo =
                objectMapper.readValue(
                        detailResponse.getContentAsString(), ServiceTemplateDetailVo.class);
        assertEquals(
                ServiceTemplateRegistrationState.IN_REVIEW,
                serviceTemplateDetailVo.getServiceTemplateRegistrationState());
        assertEquals(ocl.getCategory(), serviceTemplateDetailVo.getCategory());
        assertEquals(ocl.getCloudServiceProvider().getName(), serviceTemplateDetailVo.getCsp());
        assertEquals(ocl.getName().toLowerCase(Locale.ROOT), serviceTemplateDetailVo.getName());
        assertEquals(ocl.getServiceVersion(), serviceTemplateDetailVo.getVersion());
        assertTrue(serviceTemplateDetailVo.getIsReviewInProgress());
        assertFalse(serviceTemplateDetailVo.getIsAvailableInCatalog());

        // Setup list request
        final MockHttpServletResponse listRegisterHistoryResponse =
                listServiceTemplateHistoryWithParams(
                        serviceTemplateId,
                        ServiceTemplateRequestType.REGISTER.toValue(),
                        ServiceTemplateRequestStatus.IN_REVIEW.toValue());
        assertEquals(HttpStatus.OK.value(), listRegisterHistoryResponse.getStatus());
        List<ServiceTemplateRequestHistory> registerHistoryVos =
                objectMapper.readValue(
                        listRegisterHistoryResponse.getContentAsString(), new TypeReference<>() {});
        assertEquals(1, registerHistoryVos.size());
        ServiceTemplateRequestHistory registerHistoryVo = registerHistoryVos.getFirst();
        assertEquals(registerRequestInfo.getRequestId(), registerHistoryVo.getRequestId());
        assertEquals(
                registerRequestInfo.getServiceTemplateId(),
                registerHistoryVo.getServiceTemplateId());
        assertEquals(ServiceTemplateRequestStatus.IN_REVIEW, registerHistoryVo.getRequestStatus());

        // cancel the register request
        final MockHttpServletResponse cancelRegisterResponse =
                cancelServiceTemplateRequestByRequestId(registerRequestInfo.getRequestId());
        assertEquals(HttpStatus.NO_CONTENT.value(), cancelRegisterResponse.getStatus());

        // list history again
        final MockHttpServletResponse listRegisterHistoryResponse1 =
                listServiceTemplateHistoryWithParams(
                        serviceTemplateId,
                        ServiceTemplateRequestType.REGISTER.toValue(),
                        ServiceTemplateRequestStatus.CANCELLED.toValue());
        assertEquals(HttpStatus.OK.value(), listRegisterHistoryResponse1.getStatus());
        List<ServiceTemplateRequestHistory> registerHistoryVos1 =
                objectMapper.readValue(
                        listRegisterHistoryResponse1.getContentAsString(),
                        new TypeReference<>() {});
        assertEquals(1, registerHistoryVos1.size());
        ServiceTemplateRequestHistory registerHistoryVo1 = registerHistoryVos1.getFirst();
        assertEquals(registerRequestInfo.getRequestId(), registerHistoryVo1.getRequestId());
        assertEquals(
                registerRequestInfo.getServiceTemplateId(),
                registerHistoryVo1.getServiceTemplateId());
        assertEquals(ServiceTemplateRequestStatus.CANCELLED, registerHistoryVo1.getRequestStatus());

        // Setup detail request again.
        final MockHttpServletResponse detailResponse1 = detail(serviceTemplateId);
        // Verify the results
        assertEquals(HttpStatus.OK.value(), detailResponse1.getStatus());
        ServiceTemplateDetailVo serviceTemplateDetailVo1 =
                objectMapper.readValue(
                        detailResponse1.getContentAsString(), ServiceTemplateDetailVo.class);
        assertEquals(
                ServiceTemplateRegistrationState.CANCELLED,
                serviceTemplateDetailVo1.getServiceTemplateRegistrationState());
        assertFalse(serviceTemplateDetailVo1.getIsReviewInProgress());
        assertFalse(serviceTemplateDetailVo1.getIsAvailableInCatalog());

        // Setup fetch update request when the service template is cancelled.
        URL updateUrl = URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL();
        // Run the test
        boolean isUnpublishUntilApproved = false;
        final MockHttpServletResponse fetchUpdateResponse =
                fetchUpdate(serviceTemplateId, isUnpublishUntilApproved, updateUrl.toString());
        // Verify the results
        assertEquals(HttpStatus.OK.value(), fetchUpdateResponse.getStatus());
        ServiceTemplateRequestInfo updateRequestInfo =
                objectMapper.readValue(
                        fetchUpdateResponse.getContentAsString(), ServiceTemplateRequestInfo.class);
        assertTrue(updateRequestInfo.isRequestSubmittedForReview());
        ServiceTemplateDetailVo updatedServiceTemplateDetailVo =
                getServiceTemplateDetailsVo(updateRequestInfo.getServiceTemplateId());
        assertTrue(updatedServiceTemplateDetailVo.getIsReviewInProgress());
        assertFalse(updatedServiceTemplateDetailVo.getIsAvailableInCatalog());

        // Setup list requests
        final MockHttpServletResponse listRequestsResponse =
                listServiceTemplateHistoryWithParams(serviceTemplateId, null, null);
        assertEquals(HttpStatus.OK.value(), listRequestsResponse.getStatus());
        List<ServiceTemplateRequestHistory> requests =
                objectMapper.readValue(
                        listRequestsResponse.getContentAsString(), new TypeReference<>() {});
        assertEquals(2, requests.size());
        ServiceTemplateRequestHistory inReviewRequest = requests.getFirst();
        assertEquals(updateRequestInfo.getRequestId(), inReviewRequest.getRequestId());
        assertEquals(ServiceTemplateRequestType.REGISTER, inReviewRequest.getRequestType());
        assertEquals(ServiceTemplateRequestStatus.IN_REVIEW, inReviewRequest.getRequestStatus());

        ServiceTemplateRequestHistory cancelledRequest = requests.getLast();
        assertEquals(registerRequestInfo.getRequestId(), cancelledRequest.getRequestId());
        assertEquals(ServiceTemplateRequestType.REGISTER, cancelledRequest.getRequestType());
        assertEquals(ServiceTemplateRequestStatus.CANCELLED, cancelledRequest.getRequestStatus());

        MockHttpServletResponse cancelRegisterAgainResponse =
                cancelServiceTemplateRequestByRequestId(registerRequestInfo.getRequestId());
        assertEquals(HttpStatus.BAD_REQUEST.value(), cancelRegisterAgainResponse.getStatus());
        ErrorResponse errorResponse =
                objectMapper.readValue(
                        cancelRegisterAgainResponse.getContentAsString(), ErrorResponse.class);
        assertEquals(ErrorType.SERVICE_TEMPLATE_REQUEST_NOT_ALLOWED, errorResponse.getErrorType());
        String expectedMessage =
                "The request status is not in-review, the request is not allowed to cancel.";
        assertEquals(errorResponse.getDetails(), List.of(expectedMessage));
        deleteServiceTemplate(serviceTemplateId);
    }

    @Test
    @WithJwt(file = "jwt_csp_isv.json")
    void testManageApisThrowsException() throws Exception {
        testManageApisThrowsServiceTemplateNotRegistered();
        testManageApisThrowsAccessDeniedException();
        testManageApisThrowsRequestNotAllowedException();

        testRegisterThrowsMethodArgumentNotValidException();
        testRegisterThrowsPluginNotFoundException();
        testRegisterThrowsTerraformExecutionException();
        testRegisterThrowsInvalidValueSchemaException();
        testRegisterThrowsInvalidServiceVersionException();
        testRegisterThrowsInvalidServiceFlavorsException();
        testRegisterThrowsInvalidBillingConfigException();
        testRegisterThrowsUnavailableServiceRegionsException();

        testFetchThrowsRuntimeException();
        testListServiceTemplatesThrowsException();
        testGetHistoryDetailsThrowsServiceTemplateRequestNotFound();
    }

    void testManageApisThrowsServiceTemplateNotRegistered() throws Exception {
        // Setup
        UUID id = UUID.randomUUID();
        ErrorResponse expectedResponse =
                ErrorResponse.errorResponse(
                        ErrorType.SERVICE_TEMPLATE_NOT_REGISTERED,
                        Collections.singletonList(
                                String.format("Service template with id %s not found.", id)));
        String result = objectMapper.writeValueAsString(expectedResponse);

        // Run the test -detail
        final MockHttpServletResponse detailResponse = detail(id);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), detailResponse.getStatus());
        assertEquals(result, detailResponse.getContentAsString());

        // Run the test list service template history
        final MockHttpServletResponse listRequestsResponse =
                listServiceTemplateHistoryWithParams(id, "register", "in-review");

        assertEquals(HttpStatus.BAD_REQUEST.value(), listRequestsResponse.getStatus());
        assertEquals(
                objectMapper.writeValueAsString(expectedResponse),
                listRequestsResponse.getContentAsString());

        URL updateUrl =
                URI.create("file:src/test/resources/ocl_terraform_from_git_test.yml").toURL();
        // Run the test -update
        Ocl updateOcl = oclLoader.getOcl(updateUrl);
        final MockHttpServletResponse updateResponse = update(id, false, updateOcl);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), updateResponse.getStatus());
        assertEquals(result, updateResponse.getContentAsString());

        // Run the test -fetchUpdate
        final MockHttpServletResponse fetchUpdateResponse =
                fetchUpdate(id, false, updateUrl.toString());
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), fetchUpdateResponse.getStatus());
        assertEquals(result, fetchUpdateResponse.getContentAsString());

        // Run the test -unpublish
        final MockHttpServletResponse unpublishResponse = unpublish(id);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), unpublishResponse.getStatus());
        assertEquals(result, unpublishResponse.getContentAsString());
    }

    void testGetHistoryDetailsThrowsServiceTemplateRequestNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        ErrorResponse expectedResponse =
                ErrorResponse.errorResponse(
                        ErrorType.SERVICE_TEMPLATE_REQUEST_NOT_FOUND,
                        Collections.singletonList(
                                String.format(
                                        "Service template request with id %s not found.", id)));
        // Run the test -getRequestByRequestId()
        final MockHttpServletResponse getRequestByRequestIdResponse =
                getServiceTemplateRequestByRequestId(id);
        assertEquals(HttpStatus.BAD_REQUEST.value(), getRequestByRequestIdResponse.getStatus());
        assertEquals(
                objectMapper.writeValueAsString(expectedResponse),
                getRequestByRequestIdResponse.getContentAsString());
    }

    void testManageApisThrowsAccessDeniedException() throws Exception {

        // Setup
        UserOperation userOperation = UserOperation.REGISTER_SERVICE_TEMPLATE;
        String errorMsg =
                String.format(
                        "No permission to %s owned by other service vendors.",
                        userOperation.toValue());
        ErrorResponse accessDeniedResponse =
                ErrorResponse.errorResponse(
                        ErrorType.ACCESS_DENIED, Collections.singletonList(errorMsg));
        Ocl ocl =
                oclLoader.getOcl(
                        URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.setName(UUID.randomUUID().toString());
        String originalServiceVendor = ocl.getServiceVendor();
        ocl.setServiceVendor("test");
        MockHttpServletResponse registerErrorResponse = register(ocl);
        assertEquals(HttpStatus.FORBIDDEN.value(), registerErrorResponse.getStatus());
        assertEquals(
                objectMapper.writeValueAsString(accessDeniedResponse),
                registerErrorResponse.getContentAsString());

        ocl.setServiceVendor(originalServiceVendor);
        MockHttpServletResponse registerWell = register(ocl);
        ServiceTemplateRequestInfo serviceTemplateRequestInfo =
                objectMapper.readValue(
                        registerWell.getContentAsString(), ServiceTemplateRequestInfo.class);

        // Setup service template with service vendor 'test' to test access denied exception
        UUID serviceTemplateId = serviceTemplateRequestInfo.getServiceTemplateId();
        ServiceTemplateEntity serviceTemplateEntity =
                serviceTemplateStorage.getServiceTemplateById(serviceTemplateId);
        serviceTemplateEntity.setServiceVendor("test");
        serviceTemplateStorage.storeAndFlush(serviceTemplateEntity);

        // Run the test detail
        userOperation = UserOperation.VIEW_DETAILS_OF_SERVICE_TEMPLATE;
        errorMsg =
                String.format(
                        "No permission to %s owned by other service vendors.",
                        userOperation.toValue());
        accessDeniedResponse.setDetails(List.of(errorMsg));
        final MockHttpServletResponse detailResponse = detail(serviceTemplateId);
        // Verify the results
        assertEquals(HttpStatus.FORBIDDEN.value(), detailResponse.getStatus());
        assertEquals(
                objectMapper.writeValueAsString(accessDeniedResponse),
                detailResponse.getContentAsString());

        // Run the test list service template history
        userOperation = UserOperation.VIEW_REQUEST_HISTORY_OF_SERVICE_TEMPLATE;
        errorMsg =
                String.format(
                        "No permission to %s owned by other service vendors.",
                        userOperation.toValue());
        accessDeniedResponse.setDetails(List.of(errorMsg));
        final MockHttpServletResponse listRequestsResponse =
                listServiceTemplateHistoryWithParams(serviceTemplateId, "register", null);

        assertEquals(HttpStatus.FORBIDDEN.value(), listRequestsResponse.getStatus());
        assertEquals(
                objectMapper.writeValueAsString(accessDeniedResponse),
                listRequestsResponse.getContentAsString());

        UUID requestId = serviceTemplateRequestInfo.getRequestId();
        // Run the test -getRequestByRequestId()
        userOperation = UserOperation.VIEW_REQUEST_DETAILS_OF_SERVICE_TEMPLATE;
        errorMsg =
                String.format(
                        "No permission to %s owned by other service vendors.",
                        userOperation.toValue());
        accessDeniedResponse.setDetails(List.of(errorMsg));
        final MockHttpServletResponse getRequestByRequestIdResponse =
                getServiceTemplateRequestByRequestId(requestId);
        assertEquals(HttpStatus.FORBIDDEN.value(), getRequestByRequestIdResponse.getStatus());
        assertEquals(
                objectMapper.writeValueAsString(accessDeniedResponse),
                getRequestByRequestIdResponse.getContentAsString());

        // Run the test cancelRequestByRequestId()
        userOperation = UserOperation.CANCEL_REQUEST_OF_SERVICE_TEMPLATE;
        errorMsg =
                String.format(
                        "No permission to %s owned by other service vendors.",
                        userOperation.toValue());
        accessDeniedResponse.setDetails(List.of(errorMsg));
        final MockHttpServletResponse cancelResponse =
                cancelServiceTemplateRequestByRequestId(requestId);
        assertEquals(HttpStatus.FORBIDDEN.value(), cancelResponse.getStatus());
        assertEquals(
                objectMapper.writeValueAsString(accessDeniedResponse),
                cancelResponse.getContentAsString());

        // Setup request update
        URL updateUrl =
                URI.create("file:src/test/resources/ocl_terraform_from_git_test.yml").toURL();
        Ocl updateOcl = oclLoader.getOcl(updateUrl);
        updateOcl.setName("serviceTemplateApiTest-02");
        userOperation = UserOperation.UPDATE_SERVICE_TEMPLATE;
        errorMsg =
                String.format(
                        "No permission to %s owned by other service vendors.",
                        userOperation.toValue());
        accessDeniedResponse.setDetails(List.of(errorMsg));
        // Run the test update
        final MockHttpServletResponse updateResponse = update(serviceTemplateId, false, updateOcl);
        // Verify the results
        assertEquals(HttpStatus.FORBIDDEN.value(), updateResponse.getStatus());
        assertEquals(
                objectMapper.writeValueAsString(accessDeniedResponse),
                updateResponse.getContentAsString());

        // Run the test -fetchUpdate
        final MockHttpServletResponse fetchUpdateResponse =
                fetchUpdate(serviceTemplateId, false, updateUrl.toString());
        // Verify the results
        assertEquals(HttpStatus.FORBIDDEN.value(), fetchUpdateResponse.getStatus());
        assertEquals(
                objectMapper.writeValueAsString(accessDeniedResponse),
                fetchUpdateResponse.getContentAsString());

        // Run the test unpublish
        userOperation = UserOperation.UNPUBLISH_SERVICE_TEMPLATE;
        errorMsg =
                String.format(
                        "No permission to %s owned by other service vendors.",
                        userOperation.toValue());
        accessDeniedResponse.setDetails(List.of(errorMsg));
        final MockHttpServletResponse unpublishResponse = unpublish(serviceTemplateId);
        // Verify the results
        assertEquals(HttpStatus.FORBIDDEN.value(), unpublishResponse.getStatus());
        assertEquals(
                objectMapper.writeValueAsString(accessDeniedResponse),
                unpublishResponse.getContentAsString());

        // Run the test republish
        userOperation = UserOperation.REPUBLISH_SERVICE_TEMPLATE;
        errorMsg =
                String.format(
                        "No permission to %s owned by other service vendors.",
                        userOperation.toValue());
        accessDeniedResponse.setDetails(List.of(errorMsg));
        final MockHttpServletResponse republishResponse = republish(serviceTemplateId);
        // Verify the results
        assertEquals(HttpStatus.FORBIDDEN.value(), republishResponse.getStatus());
        assertEquals(
                objectMapper.writeValueAsString(accessDeniedResponse),
                republishResponse.getContentAsString());

        // Run the test deleteTemplate
        userOperation = UserOperation.DELETE_SERVICE_TEMPLATE;
        errorMsg =
                String.format(
                        "No permission to %s owned by other service vendors.",
                        userOperation.toValue());
        accessDeniedResponse.setDetails(List.of(errorMsg));
        final MockHttpServletResponse deleteResponse = deleteTemplate(serviceTemplateId);
        // Verify the results
        assertEquals(HttpStatus.FORBIDDEN.value(), deleteResponse.getStatus());
        assertEquals(
                objectMapper.writeValueAsString(accessDeniedResponse),
                deleteResponse.getContentAsString());

        deleteServiceTemplate(serviceTemplateId);
    }

    void testRegisterThrowsMethodArgumentNotValidException() throws Exception {
        // Setup
        Ocl ocl =
                oclLoader.getOcl(
                        URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.setCategory(null);
        BillingMode duplicateBillingMode = ocl.getBilling().getBillingModes().getFirst();
        ocl.getBilling().getBillingModes().add(duplicateBillingMode);
        Region duplicateRegion = ocl.getCloudServiceProvider().getRegions().getFirst();
        ocl.getCloudServiceProvider().getRegions().add(duplicateRegion);
        AvailabilityZoneConfig duplicateAvailabilityZoneConfig =
                ocl.getDeployment().getServiceAvailabilityConfig().getFirst();
        ocl.getDeployment().getServiceAvailabilityConfig().add(duplicateAvailabilityZoneConfig);
        DeployVariable duplicateDeployVariable = ocl.getDeployment().getVariables().getFirst();
        ocl.getDeployment().getVariables().add(duplicateDeployVariable);
        ServiceFlavorWithPrice duplicateFlavor = ocl.getFlavors().getServiceFlavors().getFirst();
        ocl.getFlavors().getServiceFlavors().add(duplicateFlavor);
        String duplicateEmail = ocl.getServiceProviderContactDetails().getEmails().getFirst();
        ocl.getServiceProviderContactDetails().getEmails().add(duplicateEmail);
        String duplicatePhone = ocl.getServiceProviderContactDetails().getPhones().getFirst();
        ocl.getServiceProviderContactDetails().getPhones().add(duplicatePhone);
        String duplicateWebsite = ocl.getServiceProviderContactDetails().getWebsites().getFirst();
        ocl.getServiceProviderContactDetails().getWebsites().add(duplicateWebsite);
        String duplicateAddress = ocl.getServiceProviderContactDetails().getChats().getFirst();
        ocl.getServiceProviderContactDetails().getChats().add(duplicateAddress);
        // Run the test
        final MockHttpServletResponse response = register(ocl);
        ErrorResponse result =
                objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
        // Verify the results
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.getStatus());
        assertEquals(ErrorType.UNPROCESSABLE_ENTITY, result.getErrorType());
        assertFalse(result.getDetails().isEmpty());

        Ocl oclTest =
                oclLoader.getOcl(
                        URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        oclTest.getDeployment().getDeployerTool().setVersion(null);
        // Run the test
        final MockHttpServletResponse response1 = register(oclTest);
        ErrorResponse result1 =
                objectMapper.readValue(response1.getContentAsString(), ErrorResponse.class);
        // Verify the results
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response1.getStatus());
        assertEquals(ErrorType.UNPROCESSABLE_ENTITY, result1.getErrorType());
        assertFalse(result1.getDetails().isEmpty());

        oclTest.getDeployment().getDeployerTool().setVersion("> v1.6.0");
        // Run the test
        final MockHttpServletResponse response2 = register(oclTest);
        ErrorResponse result2 =
                objectMapper.readValue(response2.getContentAsString(), ErrorResponse.class);
        // Verify the results
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response2.getStatus());
        assertEquals(ErrorType.UNPROCESSABLE_ENTITY, result2.getErrorType());
        assertFalse(result2.getDetails().isEmpty());
    }

    void testRegisterThrowsPluginNotFoundException() throws Exception {
        // Setup
        Ocl ocl =
                oclLoader.getOcl(
                        URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        Csp csp = Csp.AWS;
        ocl.getCloudServiceProvider().setName(csp);
        ErrorResponse expectedResponse =
                ErrorResponse.errorResponse(
                        ErrorType.PLUGIN_NOT_FOUND,
                        Collections.singletonList(
                                String.format(
                                        "Can't find suitable plugin for the Csp %s",
                                        csp.toValue())));
        // Run the test
        final MockHttpServletResponse response = register(ocl);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        assertEquals(
                objectMapper.writeValueAsString(expectedResponse), response.getContentAsString());
    }

    void testRegisterThrowsTerraformExecutionException() throws Exception {
        // Setup
        Ocl ocl =
                oclLoader.getOcl(
                        URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.getDeployment().getScriptFiles().put("error.tf", "error_script");
        ErrorResponse expectedResponse =
                ErrorResponse.errorResponse(
                        ErrorType.TERRAFORM_EXECUTION_FAILED,
                        Collections.singletonList("Executor Exception:TFExecutor.tfInit failed"));
        // Run the test
        final MockHttpServletResponse response = register(ocl);
        ErrorResponse responseModel =
                objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
        // Verify the results
        assertEquals(HttpStatus.BAD_GATEWAY.value(), response.getStatus());
        assertEquals(responseModel.getErrorType(), expectedResponse.getErrorType());
    }

    void testRegisterThrowsInvalidValueSchemaException() throws Exception {
        // Setup
        Ocl ocl =
                oclLoader.getOcl(
                        URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());

        ocl.getDeployment().setServiceAvailabilityConfig(null);
        DeployVariable deployVariable = ocl.getDeployment().getVariables().getLast();
        DeployVariable deployVariableWithRepeatName = new DeployVariable();
        BeanUtils.copyProperties(deployVariable, deployVariableWithRepeatName);
        deployVariableWithRepeatName.setValue("newValue");
        ocl.getDeployment().getVariables().add(deployVariableWithRepeatName);

        String errorMessage =
                String.format(
                        "The deploy variable configuration list with duplicated variable name %s",
                        deployVariableWithRepeatName.getName());
        ErrorResponse expectedResponse =
                ErrorResponse.errorResponse(
                        ErrorType.VARIABLE_SCHEMA_DEFINITION_INVALID,
                        Collections.singletonList(errorMessage));
        // Run the test
        final MockHttpServletResponse response = register(ocl);

        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        assertEquals(
                objectMapper.writeValueAsString(expectedResponse), response.getContentAsString());

        // Setup
        Ocl ocl2 =
                oclLoader.getOcl(
                        URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        AvailabilityZoneConfig availabilityZoneConfig =
                ocl2.getDeployment().getServiceAvailabilityConfig().getFirst();
        AvailabilityZoneConfig availabilityZoneConfigWithRepeatName = new AvailabilityZoneConfig();
        BeanUtils.copyProperties(availabilityZoneConfig, availabilityZoneConfigWithRepeatName);
        availabilityZoneConfigWithRepeatName.setDisplayName("newDisplayName");
        ocl2.getDeployment()
                .getServiceAvailabilityConfig()
                .add(availabilityZoneConfigWithRepeatName);

        String errorMessage2 =
                String.format(
                        "The availability zone configuration list with duplicated variable name %s",
                        availabilityZoneConfig.getVarName());
        ErrorResponse expectedResponse2 =
                ErrorResponse.errorResponse(
                        ErrorType.VARIABLE_SCHEMA_DEFINITION_INVALID,
                        Collections.singletonList(errorMessage2));
        // Run the test
        final MockHttpServletResponse response2 = register(ocl2);

        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), response2.getStatus());
        assertEquals(
                objectMapper.writeValueAsString(expectedResponse2), response2.getContentAsString());

        // Setup
        Ocl ocl3 =
                oclLoader.getOcl(
                        URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        DeployVariable errorVariable = new DeployVariable();
        errorVariable.setKind(DeployVariableKind.VARIABLE);
        errorVariable.setDataType(DeployVariableDataType.STRING);
        errorVariable.setMandatory(true);
        errorVariable.setName("errorVarName");
        errorVariable.setDescription("description");
        errorVariable.setExample("example");
        String errorSchemaKey = "errorSchemaKey";
        errorVariable.setValue("errorValue");
        errorVariable.setValueSchema(Collections.singletonMap(errorSchemaKey, "errorSchemaValue"));
        ModificationImpact modificationImpact = new ModificationImpact();
        modificationImpact.setIsDataLost(true);
        modificationImpact.setIsServiceInterrupted(true);
        errorVariable.setModificationImpact(modificationImpact);
        ocl3.getDeployment().setVariables(List.of(errorVariable));

        String errorMessage3 =
                String.format(
                        "Value schema key %s in deploy variable %s is invalid",
                        errorSchemaKey, errorVariable.getName());
        ErrorResponse expectedResponse3 =
                ErrorResponse.errorResponse(
                        ErrorType.VARIABLE_SCHEMA_DEFINITION_INVALID,
                        Collections.singletonList(errorMessage3));
        // Run the test
        final MockHttpServletResponse response3 = register(ocl3);

        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), response3.getStatus());
        assertEquals(
                objectMapper.writeValueAsString(expectedResponse3), response3.getContentAsString());
    }

    void testRegisterThrowsInvalidServiceVersionException() throws Exception {
        // Setup
        Ocl ocl =
                oclLoader.getOcl(
                        URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        String existingServiceVersion = "1.0.0";
        ocl.setServiceVersion(existingServiceVersion);
        ocl.setName(UUID.randomUUID().toString());
        final MockHttpServletResponse registerResponse = register(ocl);
        assertEquals(HttpStatus.OK.value(), registerResponse.getStatus());
        ServiceTemplateRequestInfo registerRequestInfo =
                objectMapper.readValue(
                        registerResponse.getContentAsString(), ServiceTemplateRequestInfo.class);
        UUID serviceTemplateId = registerRequestInfo.getServiceTemplateId();

        // Setup with invalid service version
        String serviceVersion = "ErrorVersion";
        ocl.setServiceVersion(serviceVersion);
        String errorMsg1 =
                String.format(
                        "The service version %s is a invalid semver version.", serviceVersion);
        ErrorResponse expectedResponse1 =
                ErrorResponse.errorResponse(
                        ErrorType.INVALID_SERVICE_VERSION, Collections.singletonList(errorMsg1));
        // Run the test
        final MockHttpServletResponse registerResponse1 = register(ocl);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), registerResponse1.getStatus());
        assertEquals(
                registerResponse1.getContentAsString(),
                objectMapper.writeValueAsString(expectedResponse1));

        // Setup lower version
        String lowerVersion = "0.0.1";
        ocl.setServiceVersion(lowerVersion);
        String errorMsg2 =
                String.format(
                        "The version %s of service must be higher than the"
                                + " highest version %s of the registered services with same name",
                        lowerVersion, existingServiceVersion);
        ErrorResponse expectedResponse2 =
                ErrorResponse.errorResponse(
                        ErrorType.INVALID_SERVICE_VERSION, Collections.singletonList(errorMsg2));
        // Run the test
        final MockHttpServletResponse registerResponse2 = register(ocl);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), registerResponse2.getStatus());
        assertEquals(
                registerResponse2.getContentAsString(),
                objectMapper.writeValueAsString(expectedResponse2));

        unpublish(serviceTemplateId);
        deleteTemplate(serviceTemplateId);
    }

    void testRegisterThrowsInvalidBillingConfigException() throws Exception {
        Ocl ocl =
                oclLoader.getOcl(
                        URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ServiceFlavorWithPrice errorPriceFlavor = ocl.getFlavors().getServiceFlavors().getFirst();
        // set duplicated region for markUpPrices
        PriceWithRegion duplicatedRegionMarkUpPrice =
                errorPriceFlavor.getPricing().getResourceUsage().getMarkUpPrices().getFirst();
        errorPriceFlavor
                .getPricing()
                .getResourceUsage()
                .getMarkUpPrices()
                .add(duplicatedRegionMarkUpPrice);
        String errorMsg1 =
                String.format(
                        "Duplicated items with regionName: %s and siteName: "
                                + "%s in markUpPrices for the flavor with name: %s.",
                        duplicatedRegionMarkUpPrice.getRegionName(),
                        duplicatedRegionMarkUpPrice.getSiteName(),
                        errorPriceFlavor.getName());

        // set duplicated region for licensePrices
        PriceWithRegion duplicatedRegionLicensePrice =
                errorPriceFlavor.getPricing().getResourceUsage().getLicensePrices().getFirst();
        errorPriceFlavor
                .getPricing()
                .getResourceUsage()
                .getLicensePrices()
                .add(duplicatedRegionLicensePrice);
        String errorMsg2 =
                String.format(
                        "Duplicated items with regionName: %s and siteName: "
                                + "%s in licensePrices for the flavor with name: %s.",
                        duplicatedRegionLicensePrice.getRegionName(),
                        duplicatedRegionLicensePrice.getSiteName(),
                        errorPriceFlavor.getName());

        // set duplicated region for fixedPrices
        PriceWithRegion duplicatedRegionFixedPrice =
                errorPriceFlavor.getPricing().getFixedPrices().getLast();
        errorPriceFlavor.getPricing().getFixedPrices().add(duplicatedRegionFixedPrice);
        String errorMsg4 =
                String.format(
                        "Duplicated items with regionName: %s and siteName: "
                                + "%s in fixedPrices for the flavor with name: %s.",
                        duplicatedRegionFixedPrice.getRegionName(),
                        duplicatedRegionFixedPrice.getSiteName(),
                        errorPriceFlavor.getName());

        // set null resourceUsage flavor
        ServiceFlavorWithPrice errorBillingFlavor = ocl.getFlavors().getServiceFlavors().getLast();
        errorBillingFlavor.getPricing().setResourceUsage(null);
        String errorMsg3 =
                String.format(
                        "Service flavor %s has no 'resourceUsage' defined in "
                                + "'pricing' for the billing mode 'pay-per-use'.",
                        errorBillingFlavor.getName());

        // set null fixedPrices flavor
        errorBillingFlavor.getPricing().setFixedPrices(null);
        String errorMsg5 =
                String.format(
                        "Service flavor %s has no 'fixedPrices' defined in "
                                + "'pricing' for the billing mode 'fixed'.",
                        errorBillingFlavor.getName());

        List<String> expectedDetails =
                Arrays.asList(errorMsg1, errorMsg2, errorMsg3, errorMsg4, errorMsg5);
        // Run the test
        final MockHttpServletResponse registerResponse = register(ocl);
        ErrorResponse errorResponse =
                objectMapper.readValue(registerResponse.getContentAsString(), ErrorResponse.class);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), registerResponse.getStatus());
        assertEquals(ErrorType.INVALID_BILLING_CONFIG, errorResponse.getErrorType());
        assertTrue(errorResponse.getDetails().containsAll(expectedDetails));
    }

    void testRegisterThrowsInvalidServiceFlavorsException() throws Exception {
        Ocl ocl =
                oclLoader.getOcl(
                        URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        // set duplicate flavor name
        String duplicatedFlavorName = ocl.getFlavors().getServiceFlavors().getFirst().getName();
        ocl.getFlavors().getServiceFlavors().getLast().setName(duplicatedFlavorName);
        String errorMsg =
                String.format("Duplicate flavor with name %s in service.", duplicatedFlavorName);

        List<String> expectedDetails = Collections.singletonList(errorMsg);
        // Run the test
        final MockHttpServletResponse registerResponse = register(ocl);
        ErrorResponse errorResponse =
                objectMapper.readValue(registerResponse.getContentAsString(), ErrorResponse.class);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), registerResponse.getStatus());
        assertEquals(ErrorType.INVALID_SERVICE_FLAVORS, errorResponse.getErrorType());
        assertTrue(errorResponse.getDetails().containsAll(expectedDetails));
    }

    void testRegisterThrowsUnavailableServiceRegionsException() throws Exception {
        // Setup
        Ocl ocl =
                oclLoader.getOcl(
                        URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());

        Csp csp = Csp.HUAWEI_CLOUD;
        String errorRegionName = "ErrorRegion";
        ocl.getCloudServiceProvider().setName(csp);
        ocl.getCloudServiceProvider().getRegions().getFirst().setName(errorRegionName);
        String errorMsg =
                String.format(
                        "Region with name %s is unavailable in Csp %s.",
                        errorRegionName, ocl.getCloudServiceProvider().getName().toValue());
        // Run the test
        final MockHttpServletResponse registerResponse = register(ocl);
        ErrorResponse errorResponse =
                objectMapper.readValue(registerResponse.getContentAsString(), ErrorResponse.class);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), registerResponse.getStatus());
        assertEquals(ErrorType.UNAVAILABLE_SERVICE_REGIONS, errorResponse.getErrorType());
        assertEquals(errorResponse.getDetails(), Collections.singletonList(errorMsg));
    }

    void testManageApisThrowsRequestNotAllowedException() throws Exception {
        // Setup
        Ocl ocl =
                oclLoader.getOcl(
                        URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.setName(UUID.randomUUID().toString());
        // Run the test
        final MockHttpServletResponse registerResponse = register(ocl);
        ServiceTemplateRequestInfo registerRequestInfo =
                objectMapper.readValue(
                        registerResponse.getContentAsString(), ServiceTemplateRequestInfo.class);
        UUID serviceTemplateId = registerRequestInfo.getServiceTemplateId();
        // Run the unpublish test
        String errorMsg =
                String.format(
                        "The registration of service template with id %s not approved. The request"
                                + " to unpublish is not allowed.",
                        serviceTemplateId);
        final MockHttpServletResponse unpublishResponse = unpublish(serviceTemplateId);
        ErrorResponse errorResponse =
                objectMapper.readValue(unpublishResponse.getContentAsString(), ErrorResponse.class);
        assertEquals(HttpStatus.BAD_REQUEST.value(), unpublishResponse.getStatus());
        assertEquals(ErrorType.SERVICE_TEMPLATE_REQUEST_NOT_ALLOWED, errorResponse.getErrorType());
        assertEquals(errorResponse.getDetails(), Collections.singletonList(errorMsg));

        errorMsg =
                String.format(
                        "The registration of service template with id %s not approved. The request"
                                + " to republish is not allowed.",
                        serviceTemplateId);
        // Run the republish test
        final MockHttpServletResponse republishResponse = republish(serviceTemplateId);
        errorResponse =
                objectMapper.readValue(republishResponse.getContentAsString(), ErrorResponse.class);
        assertEquals(HttpStatus.BAD_REQUEST.value(), republishResponse.getStatus());
        assertEquals(ErrorType.SERVICE_TEMPLATE_REQUEST_NOT_ALLOWED, errorResponse.getErrorType());
        assertEquals(errorResponse.getDetails(), Collections.singletonList(errorMsg));

        // Set up the register same
        errorMsg =
                String.format(
                        "Service template already registered with id %s. The register request is"
                                + " not allowed.",
                        serviceTemplateId);
        // Run the test
        final MockHttpServletResponse registerSameResponse = register(ocl);
        errorResponse =
                objectMapper.readValue(
                        registerSameResponse.getContentAsString(), ErrorResponse.class);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), registerSameResponse.getStatus());
        assertEquals(ErrorType.SERVICE_TEMPLATE_REQUEST_NOT_ALLOWED, errorResponse.getErrorType());
        assertEquals(errorResponse.getDetails(), Collections.singletonList(errorMsg));
        // approve the registration.
        reviewServiceTemplateRequest(registerRequestInfo.getRequestId(), true);

        // Setup update request
        boolean isRemoveFromCatalogUtilApproved = false;
        ocl.setDescription("update-test-01");
        final MockHttpServletResponse updateResponse =
                update(serviceTemplateId, isRemoveFromCatalogUtilApproved, ocl);
        assertEquals(HttpStatus.OK.value(), updateResponse.getStatus());
        ServiceTemplateRequestInfo updateRequestInfo =
                objectMapper.readValue(
                        updateResponse.getContentAsString(), ServiceTemplateRequestInfo.class);

        errorMsg =
                String.format(
                        "The request with id %s to the service template is waiting for review."
                                + " The new request is not allowed.",
                        updateRequestInfo.getRequestId());
        final MockHttpServletResponse updateResponse1 =
                update(serviceTemplateId, isRemoveFromCatalogUtilApproved, ocl);
        assertEquals(HttpStatus.BAD_REQUEST.value(), updateResponse1.getStatus());
        errorResponse =
                objectMapper.readValue(updateResponse1.getContentAsString(), ErrorResponse.class);
        assertEquals(ErrorType.SERVICE_TEMPLATE_REQUEST_NOT_ALLOWED, errorResponse.getErrorType());
        assertEquals(errorResponse.getDetails(), Collections.singletonList(errorMsg));

        // Run the republish test
        final MockHttpServletResponse republishResponse1 = republish(serviceTemplateId);
        errorResponse =
                objectMapper.readValue(
                        republishResponse1.getContentAsString(), ErrorResponse.class);
        assertEquals(HttpStatus.BAD_REQUEST.value(), republishResponse1.getStatus());
        assertEquals(ErrorType.SERVICE_TEMPLATE_REQUEST_NOT_ALLOWED, errorResponse.getErrorType());
        assertEquals(errorResponse.getDetails(), Collections.singletonList(errorMsg));

        // Run the delete test
        errorMsg =
                String.format(
                        "Service template with id %s is still in catalog. The request to delete is"
                                + " not allowed.",
                        serviceTemplateId);
        MockHttpServletResponse deleteResponse = deleteTemplate(serviceTemplateId);
        errorResponse =
                objectMapper.readValue(deleteResponse.getContentAsString(), ErrorResponse.class);
        assertEquals(HttpStatus.BAD_REQUEST.value(), republishResponse.getStatus());
        assertEquals(ErrorType.SERVICE_TEMPLATE_REQUEST_NOT_ALLOWED, errorResponse.getErrorType());
        assertEquals(errorResponse.getDetails(), Collections.singletonList(errorMsg));

        errorMsg =
                String.format(
                        "Service template with id %s is still in use. The request to delete is not"
                                + " allowed.",
                        serviceTemplateId);
        updateServiceTemplateState(
                serviceTemplateId, ServiceTemplateRegistrationState.APPROVED, false);
        when(mockServiceDeploymentStorage.listServices(any(ServiceQueryModel.class)))
                .thenReturn(List.of(new ServiceDeploymentEntity()));
        MockHttpServletResponse deleteResponse2 = deleteTemplate(serviceTemplateId);
        errorResponse =
                objectMapper.readValue(deleteResponse2.getContentAsString(), ErrorResponse.class);
        assertEquals(HttpStatus.BAD_REQUEST.value(), deleteResponse2.getStatus());
        assertEquals(ErrorType.SERVICE_TEMPLATE_REQUEST_NOT_ALLOWED, errorResponse.getErrorType());
        assertEquals(errorResponse.getDetails(), Collections.singletonList(errorMsg));

        // clear data
        deleteTemplateById(serviceTemplateId);
    }

    private void deleteTemplateById(UUID serviceTemplateId) {
        serviceTemplateStorage.deleteServiceTemplate(
                serviceTemplateStorage.getServiceTemplateById(serviceTemplateId));
    }

    void testFetchThrowsRuntimeException() throws Exception {
        // Setup
        String fileUrl =
                URI.create("file:src/test/resources/ocl_terraform_error.yml").toURL().toString();
        ErrorResponse expectedErrorResponse =
                ErrorResponse.errorResponse(
                        ErrorType.RUNTIME_ERROR,
                        Collections.singletonList("java.io.FileNotFoundException:"));

        // Run the test
        final MockHttpServletResponse response = fetch(fileUrl);
        ErrorResponse errorResponseModel =
                objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
        // Verify the results
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());
        assertEquals(errorResponseModel.getErrorType(), expectedErrorResponse.getErrorType());
    }

    void testListServiceTemplatesThrowsException() throws Exception {
        // Setup
        String errorMessage =
                "Failed to convert value of type 'java.lang.String' to required " + "type";
        ErrorResponse expectedResponse =
                ErrorResponse.errorResponse(ErrorType.UNPROCESSABLE_ENTITY, List.of(errorMessage));

        // Run the test
        final MockHttpServletResponse response =
                listServiceTemplatesWithParams(
                        "errorCategory", null, null, null, null, null, null, null);
        ErrorResponse resultResponse =
                objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);

        // Verify the results
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.getStatus());
        assertEquals(expectedResponse.getErrorType(), resultResponse.getErrorType());

        final MockHttpServletResponse historyResponse =
                listServiceTemplateHistoryWithParams(UUID.randomUUID(), "errorStatus", null);
        ErrorResponse resultResponse1 =
                objectMapper.readValue(historyResponse.getContentAsString(), ErrorResponse.class);
        // Verify the results
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), historyResponse.getStatus());
        assertEquals(expectedResponse.getErrorType(), resultResponse1.getErrorType());
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

    MockHttpServletResponse update(UUID id, boolean isUnpublishUntilApproved, Ocl ocl)
            throws Exception {
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        String requestBody = yamlMapper.writeValueAsString(ocl);
        return mockMvc.perform(
                        put("/xpanse/service_templates/{id}", id)
                                .param(
                                        "isUnpublishUntilApproved",
                                        String.valueOf(isUnpublishUntilApproved))
                                .content(requestBody)
                                .contentType("application/x-yaml")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
    }

    MockHttpServletResponse fetch(String url) throws Exception {
        return mockMvc.perform(
                        post("/xpanse/service_templates/file")
                                .param("oclLocation", url)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
    }

    MockHttpServletResponse fetchUpdate(UUID id, boolean isUnpublishUntilApproved, String url)
            throws Exception {
        return mockMvc.perform(
                        put("/xpanse/service_templates/file/{id}", id)
                                .param(
                                        "isUnpublishUntilApproved",
                                        String.valueOf(isUnpublishUntilApproved))
                                .param("oclLocation", url)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
    }

    MockHttpServletResponse detail(UUID id) throws Exception {
        return mockMvc.perform(
                        get("/xpanse/service_templates/{id}", id)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
    }

    MockHttpServletResponse unpublish(UUID id) throws Exception {
        return mockMvc.perform(
                        put("/xpanse/service_templates/unpublish/{id}", id)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
    }

    MockHttpServletResponse republish(UUID id) throws Exception {
        return mockMvc.perform(
                        put("/xpanse/service_templates/republish/{id}", id)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
    }

    MockHttpServletResponse deleteTemplate(UUID id) throws Exception {
        return mockMvc.perform(
                        delete("/xpanse/service_templates/{id}", id)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
    }

    MockHttpServletResponse listServiceTemplatesWithParams(
            String categoryName,
            String cspName,
            String serviceName,
            String serviceVersion,
            String serviceHostingType,
            String serviceTemplateRegistrationState,
            Boolean isAvailableInCatalog,
            Boolean isReviewInProgress)
            throws Exception {
        MockHttpServletRequestBuilder getRequestBuilder = get("/xpanse/service_templates");
        if (StringUtils.isNotBlank(categoryName)) {
            getRequestBuilder = getRequestBuilder.param("categoryName", categoryName);
        }
        if (StringUtils.isNotBlank(cspName)) {
            getRequestBuilder = getRequestBuilder.param("cspName", cspName);
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
        if (Objects.nonNull(isAvailableInCatalog)) {
            getRequestBuilder =
                    getRequestBuilder.param(
                            "isAvailableInCatalog", isAvailableInCatalog.toString());
        }
        if (Objects.nonNull(isReviewInProgress)) {
            getRequestBuilder =
                    getRequestBuilder.param("isReviewInProgress", isReviewInProgress.toString());
        }
        return mockMvc.perform(getRequestBuilder).andReturn().getResponse();
    }

    MockHttpServletResponse listServiceTemplateHistoryWithParams(
            UUID serviceTemplateId, String requestType, String changeStatus) throws Exception {
        MockHttpServletRequestBuilder getRequestBuilder =
                get("/xpanse/service_templates/{serviceTemplateId}/requests", serviceTemplateId);
        if (Objects.nonNull(changeStatus)) {
            getRequestBuilder = getRequestBuilder.param("changeStatus", changeStatus);
        }
        if (Objects.nonNull(requestType)) {
            getRequestBuilder = getRequestBuilder.param("requestType", requestType);
        }
        return mockMvc.perform(getRequestBuilder).andReturn().getResponse();
    }

    MockHttpServletResponse getServiceTemplateRequestByRequestId(UUID RequestId) throws Exception {
        return mockMvc.perform(
                        get("/xpanse/service_templates/requests/{requestId}", RequestId)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
    }

    MockHttpServletResponse cancelServiceTemplateRequestByRequestId(UUID RequestId)
            throws Exception {
        return mockMvc.perform(
                        put("/xpanse/service_templates/requests/cancel/{requestId}", RequestId)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
    }
}
