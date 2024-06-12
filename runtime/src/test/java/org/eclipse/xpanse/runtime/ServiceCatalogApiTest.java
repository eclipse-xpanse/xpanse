/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import jakarta.transaction.Transactional;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceFlavor;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.eclipse.xpanse.modules.models.servicetemplate.view.UserOrderableServiceVo;
import org.eclipse.xpanse.runtime.util.ApisTestCommon;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Test for ServiceRegisterApiTest.
 */
@Slf4j
@Transactional
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=oauth,zitadel,zitadel-testbed"})
@AutoConfigureMockMvc
class ServiceCatalogApiTest extends ApisTestCommon {

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testServiceCatalogServices() throws Exception {
        Ocl ocl = new OclLoader().getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.setName("serviceCatalogApiTest-1");
        ServiceTemplateDetailVo serviceTemplate = registerServiceTemplate(ocl);
        testGetOrderableServiceDetailsThrowsException(serviceTemplate);
        testOpenApi(serviceTemplate);
        testListOrderableServices(serviceTemplate);
        testGetOrderableServiceDetails(serviceTemplate);
        unregisterServiceTemplate(serviceTemplate.getServiceTemplateId());
    }


    private UserOrderableServiceVo approveServiceTemplateRegistration(
            ServiceTemplateDetailVo serviceTemplateDetailVo)
            throws Exception {
        UUID id = serviceTemplateDetailVo.getServiceTemplateId();
        approveServiceTemplateRegistration(id);
        final MockHttpServletResponse response = getOrderableServiceDetailsWithId(id);
        return objectMapper.readValue(response.getContentAsString(),
                UserOrderableServiceVo.class);
    }

    void testListOrderableServices(ServiceTemplateDetailVo serviceTemplateDetailVo)
            throws Exception {
        // Setup request 1
        String result1 = "[]";
        // Run the test case 1
        final MockHttpServletResponse response1 = listOrderableServicesWithParams(null, null,
                "errorValue",
                null, null);
        // Verify the result 1
        Assertions.assertEquals(HttpStatus.OK.value(), response1.getStatus());
        Assertions.assertEquals(result1, response1.getContentAsString());

        // Setup request 2
        String errorMessage = "Failed to convert value of type 'java.lang.String' to required type";
        // Run the test case 2
        final MockHttpServletResponse response2 = listOrderableServicesWithParams("errorValue",
                Csp.HUAWEI.toValue(), null, null, null);
        // Verify the result 2
        assertThat(response2.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
        assertThat(errorMessage).isSubstringOf(response2.getContentAsString());

        // Setup request 3
        UserOrderableServiceVo userOrderableServiceVo =
                approveServiceTemplateRegistration(serviceTemplateDetailVo);

        String result3 = objectMapper.writeValueAsString(List.of(userOrderableServiceVo));
        // Run the test case 3
        final MockHttpServletResponse response3 =
                listOrderableServicesWithParams(serviceTemplateDetailVo.getCategory().toValue(),
                        serviceTemplateDetailVo.getCsp().toValue(),
                        serviceTemplateDetailVo.getName(),
                        serviceTemplateDetailVo.getVersion(),
                        serviceTemplateDetailVo.getServiceHostingType().toValue());
        // Verify the result 3
        Assertions.assertEquals(HttpStatus.OK.value(), response3.getStatus());
        Assertions.assertEquals(result3, response3.getContentAsString());
    }

    UserOrderableServiceVo transToUserOrderableServiceVo(
            ServiceTemplateDetailVo serviceTemplateDetailVo) {
        UserOrderableServiceVo userOrderableServiceVo = new UserOrderableServiceVo();
        BeanUtils.copyProperties(serviceTemplateDetailVo, userOrderableServiceVo);

        List<ServiceFlavor> flavorBasics = serviceTemplateDetailVo.getFlavors()
                .getServiceFlavors().stream().map(flavor -> {
                    ServiceFlavor flavorBasic = new ServiceFlavor();
                    BeanUtils.copyProperties(flavor, flavorBasic);
                    return flavorBasic;
                }).toList();
        userOrderableServiceVo.setFlavors(flavorBasics);
        userOrderableServiceVo.setServiceAvailabilityConfigs(serviceTemplateDetailVo.getDeployment()
                .getServiceAvailabilityConfigs());
        userOrderableServiceVo.add(
                Link.of(String.format("http://localhost/xpanse/catalog/services/%s/openapi",
                        serviceTemplateDetailVo.getServiceTemplateId().toString()), "openApi"));

        return userOrderableServiceVo;
    }

    void testGetOrderableServiceDetails(ServiceTemplateDetailVo serviceTemplateDetailVo)
            throws Exception {

        // Setup request 1
        UUID id1 = UUID.randomUUID();
        Response expectedResponse1 = Response.errorResponse(
                ResultType.SERVICE_TEMPLATE_NOT_REGISTERED,
                Collections.singletonList(
                        String.format("Service template with id %s not found.", id1)));
        String result1 = objectMapper.writeValueAsString(expectedResponse1);
        // Run the test case 1
        final MockHttpServletResponse response1 = getOrderableServiceDetailsWithId(id1);
        // Verify the result 1
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response1.getStatus());
        Assertions.assertEquals(result1, response1.getContentAsString());

        // Setup request 2
        UUID id2 = serviceTemplateDetailVo.getServiceTemplateId();
        UserOrderableServiceVo expectedResponse2 =
                transToUserOrderableServiceVo(serviceTemplateDetailVo);
        // Run the test case 2
        final MockHttpServletResponse response2 = getOrderableServiceDetailsWithId(id2);
        UserOrderableServiceVo result2 = objectMapper.readValue(response2.getContentAsString(),
                UserOrderableServiceVo.class);
        // Verify the results 2
        Assertions.assertEquals(HttpStatus.OK.value(), response2.getStatus());
        Assertions.assertEquals(result2, expectedResponse2);
    }

    void testGetOrderableServiceDetailsThrowsException(
            ServiceTemplateDetailVo serviceTemplateDetailVo) throws Exception {
        // Setup request 1
        UUID id1 = serviceTemplateDetailVo.getServiceTemplateId();
        Response expectedResponse1 = Response.errorResponse(
                ResultType.SERVICE_TEMPLATE_NOT_APPROVED,
                Collections.singletonList(
                        String.format("Service template with id %s not approved.", id1)));
        String result1 = objectMapper.writeValueAsString(expectedResponse1);
        // Run the test case 1
        final MockHttpServletResponse response1 = getOrderableServiceDetailsWithId(id1);
        // Verify the result 1
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response1.getStatus());
        Assertions.assertEquals(result1, response1.getContentAsString());
    }

    MockHttpServletResponse getOrderableServiceDetailsWithId(UUID id) throws Exception {
        return mockMvc.perform(get("/xpanse/catalog/services/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }

    void testOpenApi(ServiceTemplateDetailVo serviceTemplateDetailVo) throws Exception {
        // Setup request 1
        UUID id1 = serviceTemplateDetailVo.getServiceTemplateId();
        Link link = Link.of(String.format("http://localhost/openapi/%s.html", id1), "OpenApi");
        String result1 = objectMapper.writeValueAsString(link);
        // Run the test case 1
        final MockHttpServletResponse response1 = mockMvc.perform(
                        get("/xpanse/catalog/services/{id}/openapi", id1)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        // Verify the result 1
        Assertions.assertEquals(HttpStatus.OK.value(), response1.getStatus());
        Assertions.assertEquals(result1, response1.getContentAsString());

        // Setup request 2
        UUID id2 = UUID.randomUUID();
        Response expectedResponse = Response.errorResponse(
                ResultType.SERVICE_TEMPLATE_NOT_REGISTERED,
                Collections.singletonList(
                        String.format("Service template with id %s not found.", id2)));
        String result2 = objectMapper.writeValueAsString(expectedResponse);
        // Run the test case 2
        final MockHttpServletResponse response2 = mockMvc.perform(
                        get("/xpanse/catalog/services/{id}/openapi", id2)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        // Verify the result 2
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response2.getStatus());
        Assertions.assertEquals(result2, response2.getContentAsString());
    }

    MockHttpServletResponse listOrderableServicesWithParams(String categoryName, String cspName,
                                                            String serviceName,
                                                            String serviceVersion,
                                                            String serviceHostingType)
            throws Exception {
        MockHttpServletRequestBuilder getRequestBuilder = get("/xpanse/catalog/services");
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
        return mockMvc.perform(getRequestBuilder).andReturn().getResponse();
    }
}
