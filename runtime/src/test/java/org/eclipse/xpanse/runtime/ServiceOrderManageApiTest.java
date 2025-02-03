/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.fasterxml.jackson.core.type.TypeReference;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.models.common.enums.UserOperation;
import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.response.ErrorType;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrderDetails;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrderStatusUpdate;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.eclipse.xpanse.modules.policy.policyman.generated.api.PoliciesEvaluationApi;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.EvalCmdList;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.EvalResult;
import org.eclipse.xpanse.runtime.util.ApisTestCommon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** Test for ServiceDeployerApi. */
@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=oauth,zitadel,zitadel-testbed,test,dev"})
@AutoConfigureMockMvc
class ServiceOrderManageApiTest extends ApisTestCommon {

    @MockitoBean private PoliciesEvaluationApi mockPoliciesEvaluationApi;

    @Test
    @WithJwt(file = "jwt_csp_isv_user.json")
    void testServiceOrderManageApis() throws Exception {
        // Setup
        Ocl ocl =
                new OclLoader()
                        .getOcl(
                                URI.create("file:src/test/resources/ocl_terraform_test.yml")
                                        .toURL());
        ServiceTemplateDetailVo serviceTemplate =
                registerServiceTemplateAndApproveRegistration(ocl);
        if (Objects.isNull(serviceTemplate)) {
            log.error("Register service template failed.");
            return;
        }
        addCredentialForHuaweiCloud();
        testServiceOrderManageApisWell(serviceTemplate);
        testServiceOrderManageApisThrowsException(serviceTemplate);
        deleteServiceTemplate(serviceTemplate.getServiceTemplateId());
    }

    void testServiceOrderManageApisWell(ServiceTemplateDetailVo serviceTemplate) throws Exception {
        mockPolicyEvaluationResult(true);
        ServiceOrder serviceOrder = deployService(serviceTemplate);
        UUID orderId = serviceOrder.getOrderId();
        UUID serviceId = serviceOrder.getServiceId();
        assertNotNull(orderId);
        assertNotNull(serviceId);
        ServiceOrderStatusUpdate statusUpdatedResult =
                getLatestServiceOrderStatus(orderId, TaskStatus.CREATED);
        assertThat(statusUpdatedResult).isNotNull();
        assertThat(statusUpdatedResult.getIsOrderCompleted()).isFalse();

        ServiceOrderStatusUpdate orderIsCompletedResult =
                getLatestServiceOrderStatus(orderId, null);
        assertThat(orderIsCompletedResult).isNotNull();
        assertThat(orderIsCompletedResult.getIsOrderCompleted()).isTrue();

        MockHttpServletResponse orderDetailsResponse = getOrderDetailsByOrderId(orderId);
        assertEquals(HttpStatus.OK.value(), orderDetailsResponse.getStatus());
        ServiceOrderDetails orderDetails =
                objectMapper.readValue(
                        orderDetailsResponse.getContentAsString(), ServiceOrderDetails.class);

        assertThat(orderDetails.getOrderId()).isEqualTo(orderId);
        assertThat(orderDetails.getTaskStatus()).isEqualTo(TaskStatus.SUCCESSFUL);
        assertThat(orderDetails.getTaskType()).isEqualTo(ServiceOrderType.DEPLOY);
        assertThat(orderDetails.getServiceId()).isEqualTo(serviceId);

        MockHttpServletResponse serviceOrdersResponse =
                listServiceOrders(serviceId, ServiceOrderType.DEPLOY, TaskStatus.SUCCESSFUL);

        assertThat(serviceOrdersResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        List<ServiceOrderDetails> serviceOrders =
                objectMapper.readValue(
                        serviceOrdersResponse.getContentAsString(), new TypeReference<>() {});
        assertThat(serviceOrders).isNotEmpty();

        assertThat(serviceOrders.getFirst()).isEqualTo(orderDetails);

        deleteServiceDeployment(serviceId);

        MockHttpServletResponse deleteOrderResponse = deleteOrderByOrderId(orderId);
        assertThat(deleteOrderResponse.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        MockHttpServletResponse deleteServiceOrdersResponse = deleteOrdersByServiceId(serviceId);
        assertThat(deleteServiceOrdersResponse.getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST.value());

        MockHttpServletResponse getOrderDetailsResponse = getOrderDetailsByOrderId(orderId);
        assertThat(getOrderDetailsResponse.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        MockHttpServletResponse listServiceOrdersResponse =
                listServiceOrders(serviceId, ServiceOrderType.DEPLOY, TaskStatus.SUCCESSFUL);

        assertThat(listServiceOrdersResponse.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    void mockPolicyEvaluationResult(boolean isSuccessful) {
        final EvalResult evalResult = new EvalResult();
        evalResult.setIsSuccessful(isSuccessful);
        evalResult.setPolicy("policy");
        // Configure PoliciesEvaluationApi.evaluatePoliciesPost(...).
        when(mockPoliciesEvaluationApi.evaluatePoliciesPost(any(EvalCmdList.class)))
                .thenReturn(evalResult);
    }

    void testServiceOrderManageApisThrowsException(ServiceTemplateDetailVo serviceTemplate)
            throws Exception {
        testApisThrowServiceNotFound();
        testApisThrowServiceOrderNotFound();
        testApisThrowAccessDenied(serviceTemplate);
    }

    private void testApisThrowAccessDenied(ServiceTemplateDetailVo serviceTemplate)
            throws Exception {
        // Set up
        ServiceOrder serviceOrder = deployService(serviceTemplate);
        UUID orderId = serviceOrder.getOrderId();
        UUID serviceId = serviceOrder.getServiceId();
        assertNotNull(orderId);
        assertNotNull(serviceId);
        String errorUserId = "errorUserId";
        ServiceDeploymentEntity serviceDeploymentEntity =
                serviceDeploymentStorage.findServiceDeploymentById(serviceId);
        serviceDeploymentEntity.setUserId(errorUserId);
        serviceDeploymentStorage.storeAndFlush(serviceDeploymentEntity);
        ServiceOrderEntity serviceOrderEntity = serviceOrderStorage.getEntityById(orderId);
        serviceOrderEntity.setUserId(errorUserId);
        serviceOrderStorage.storeAndFlush(serviceOrderEntity);

        String errorMsg =
                String.format(
                        "No permission to %s owned by other users.",
                        UserOperation.VIEW_ORDERS_OF_SERVICE.toValue());
        ErrorResponse expectedErrorResponse =
                ErrorResponse.errorResponse(
                        ErrorType.ACCESS_DENIED, Collections.singletonList(errorMsg));
        String result = objectMapper.writeValueAsString(expectedErrorResponse);

        MockHttpServletResponse listOrdersResponse = listServiceOrders(serviceId, null, null);
        assertEquals(HttpStatus.FORBIDDEN.value(), listOrdersResponse.getStatus());
        assertEquals(result, listOrdersResponse.getContentAsString());

        errorMsg =
                String.format(
                        "No permission to %s owned by other users.",
                        UserOperation.DELETE_ORDERS_OF_SERVICE.toValue());
        expectedErrorResponse =
                ErrorResponse.errorResponse(
                        ErrorType.ACCESS_DENIED, Collections.singletonList(errorMsg));
        result = objectMapper.writeValueAsString(expectedErrorResponse);
        MockHttpServletResponse deleteOrdersResponse = deleteOrdersByServiceId(serviceId);
        assertEquals(HttpStatus.FORBIDDEN.value(), deleteOrdersResponse.getStatus());
        assertEquals(result, deleteOrdersResponse.getContentAsString());

        MockHttpServletResponse deleteOrderResponse = deleteOrderByOrderId(orderId);
        assertEquals(HttpStatus.FORBIDDEN.value(), deleteOrderResponse.getStatus());
        assertEquals(result, deleteOrderResponse.getContentAsString());

        deleteServiceDeployment(serviceId);
    }

    void testApisThrowServiceOrderNotFound() throws Exception {
        // SetUp
        UUID orderId = UUID.randomUUID();
        ErrorResponse expectedErrorResponse =
                ErrorResponse.errorResponse(
                        ErrorType.SERVICE_ORDER_NOT_FOUND,
                        Collections.singletonList(
                                String.format("Service order with id %s not found.", orderId)));
        String result = objectMapper.writeValueAsString(expectedErrorResponse);

        MockHttpServletResponse getAuditResponse = getOrderDetailsByOrderId(orderId);
        assertEquals(HttpStatus.BAD_REQUEST.value(), getAuditResponse.getStatus());
        assertEquals(result, getAuditResponse.getContentAsString());

        MockHttpServletResponse deleteAuditResponse = deleteOrderByOrderId(orderId);
        assertEquals(HttpStatus.BAD_REQUEST.value(), deleteAuditResponse.getStatus());
        assertEquals(result, deleteAuditResponse.getContentAsString());
    }

    void testApisThrowServiceNotFound() throws Exception {
        // SetUp
        UUID serviceId = UUID.randomUUID();
        ErrorResponse expectedErrorResponse =
                ErrorResponse.errorResponse(
                        ErrorType.SERVICE_DEPLOYMENT_NOT_FOUND,
                        Collections.singletonList(
                                String.format("Service with id %s not found.", serviceId)));
        String result = objectMapper.writeValueAsString(expectedErrorResponse);

        MockHttpServletResponse listOrdersResponse = listServiceOrders(serviceId, null, null);
        assertEquals(HttpStatus.BAD_REQUEST.value(), listOrdersResponse.getStatus());
        assertEquals(result, listOrdersResponse.getContentAsString());

        MockHttpServletResponse deleteOrdersResponse = deleteOrdersByServiceId(serviceId);
        assertEquals(HttpStatus.BAD_REQUEST.value(), deleteOrdersResponse.getStatus());
        assertEquals(result, deleteOrdersResponse.getContentAsString());
    }

    MockHttpServletResponse listServiceOrders(
            UUID serviceId, ServiceOrderType taskType, TaskStatus taskStatus) throws Exception {
        MockHttpServletRequestBuilder listRequestBuilder =
                get("/xpanse/services/{serviceId}/orders", serviceId)
                        .accept(MediaType.APPLICATION_JSON);
        if (taskType != null) {
            listRequestBuilder.param("taskType", taskType.toValue());
        }
        if (taskStatus != null) {
            listRequestBuilder.param("taskStatus", taskStatus.toValue());
        }
        return mockMvc.perform(listRequestBuilder).andReturn().getResponse();
    }

    MockHttpServletResponse deleteOrdersByServiceId(UUID serviceId) throws Exception {
        return mockMvc.perform(
                        delete("/xpanse/services/{serviceId}/orders", serviceId)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
    }

    MockHttpServletResponse getOrderDetailsByOrderId(UUID orderId) throws Exception {
        return mockMvc.perform(
                        get("/xpanse/services/orders/{orderId}", orderId)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
    }

    MockHttpServletResponse deleteOrderByOrderId(UUID orderId) throws Exception {
        return mockMvc.perform(
                        delete("/xpanse/services/orders/{orderId}", orderId)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
    }
}
