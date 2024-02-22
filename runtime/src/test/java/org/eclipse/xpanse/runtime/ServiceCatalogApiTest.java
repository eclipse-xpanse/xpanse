/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.servicetemplate.DatabaseServiceTemplateStorage;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.servicetemplate.FlavorBasic;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceRegistrationState;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.eclipse.xpanse.modules.models.servicetemplate.view.UserOrderableServiceVo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
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
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test for ServiceRegisterApiTest.
 */
@Slf4j
@Transactional
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=zitadel,zitadel-testbed"})
@AutoConfigureMockMvc
class ServiceCatalogApiTest {

    private final static ObjectMapper objectMapper = new ObjectMapper();
    private static String id;
    private static ServiceTemplateDetailVo serviceTemplateDetailVo;
    private static Ocl ocl;
    @Resource
    private MockMvc mockMvc;

    @Resource
    private DatabaseServiceTemplateStorage serviceTemplateStorage;

    @BeforeAll
    static void configureObjectMapper() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new SimpleModule().addSerializer(OffsetDateTime.class,
                OffsetDateTimeSerializer.INSTANCE));
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);

    }

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testOrderableServices() throws Exception {
        registerServiceTemplate();
        Thread.sleep(3000);
        approveServiceTemplateRegistration();
        testOpenApi();
        testOrderableServiceDetails();
        testListOrderableServices();
        unregisterService();
    }

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testOrderableServicesThrowsException() throws Exception {
        testOrderableServiceDetailsThrowsServiceTemplateNotRegistered();
        testListOrderableServicesThrowsException();
        testOpenApiThrowsException();
        testListOrderableServicesReturnsNoItems();
    }

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testGetOrderableServiceDetailsThrowsException() throws Exception {
        testOrderableServiceDetailsThrowsServiceTemplateNotApproved();
    }

    void registerServiceTemplate() throws Exception {
        // Setup
        ocl = new OclLoader().getOcl(URI.create("file:src/test/resources/ocl_test.yaml").toURL());
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        String requestBody = yamlMapper.writeValueAsString(ocl);

        // Run the test
        final MockHttpServletResponse registerResponse =
                mockMvc.perform(post("/xpanse/service_templates")
                                .content(requestBody).contentType("application/x-yaml")
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();
        serviceTemplateDetailVo =
                objectMapper.readValue(registerResponse.getContentAsString(),
                        ServiceTemplateDetailVo.class);
        id = serviceTemplateDetailVo.getId().toString();
    }

    void approveServiceTemplateRegistration() throws Exception {
        if (serviceTemplateDetailVo == null || StringUtils.isBlank(id)) {
            registerServiceTemplate();
        }
        ServiceTemplateEntity serviceTemplateEntity =
                serviceTemplateStorage.getServiceTemplateById(UUID.fromString(id));
        serviceTemplateEntity.setServiceRegistrationState(ServiceRegistrationState.APPROVED);
        serviceTemplateStorage.storeAndFlush(serviceTemplateEntity);
    }


    void unregisterService() throws Exception {
        final MockHttpServletResponse response =
                mockMvc.perform(delete("/xpanse/service_templates/{id}", id)
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
        serviceTemplateDetailVo = null;
        id = null;
    }

    void testListOrderableServices() throws Exception {
        // Setup
        List<UserOrderableServiceVo> userOrderableServiceVos =
                List.of(transToUserOrderableServiceVo(serviceTemplateDetailVo));
        String result = objectMapper.writeValueAsString(userOrderableServiceVos);

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(get("/xpanse/catalog/services")
                        .param("categoryName", "middleware")
                        .param("cspName", "huawei")
                        .param("serviceName", "kafka-cluster")
                        .param("serviceVersion", "v3.3.2")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assertions.assertEquals(result, response.getContentAsString());
    }

    UserOrderableServiceVo transToUserOrderableServiceVo(
            ServiceTemplateDetailVo serviceTemplateDetailVo) {
        UserOrderableServiceVo userOrderableServiceVo = new UserOrderableServiceVo();
        BeanUtils.copyProperties(serviceTemplateDetailVo, userOrderableServiceVo);

        List<FlavorBasic> flavorBasics = serviceTemplateDetailVo.getFlavors()
                .stream().map(flavor -> {
                    FlavorBasic flavorBasic = new FlavorBasic();
                    BeanUtils.copyProperties(flavor, flavorBasic);
                    return flavorBasic;
                }).toList();
        userOrderableServiceVo.setFlavors(flavorBasics);
        userOrderableServiceVo.add(
                Link.of(String.format("http://localhost/xpanse/catalog/services/%s/openapi",
                        serviceTemplateDetailVo.getId().toString()), "openApi"));

        return userOrderableServiceVo;
    }

    void testListOrderableServicesThrowsException() throws Exception {
        // Setup
        String errorMessage = "Failed to convert value of type 'java.lang.String' to required type";
        Response expectedResponse =
                Response.errorResponse(ResultType.UNPROCESSABLE_ENTITY, List.of(errorMessage));

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(get("/xpanse/catalog/services")
                        .param("categoryName", "errorCategoryName")
                        .param("cspName", "cspName")
                        .param("serviceName", "serviceName")
                        .param("serviceVersion", "serviceVersion")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        Response resultResponse =
                objectMapper.readValue(response.getContentAsString(), Response.class);

        // Verify the results
        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.getStatus());
        Assertions.assertFalse(resultResponse.getSuccess());
        Assertions.assertEquals(expectedResponse.getSuccess(), resultResponse.getSuccess());
    }

    void testListOrderableServicesReturnsNoItems() throws Exception {
        // Setup
        String result = "[]";
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(get("/xpanse/catalog/services")
                        .param("categoryName", "AI")
                        .param("cspName", "huawei")
                        .param("serviceName", "serviceName")
                        .param("serviceVersion", "serviceVersion")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assertions.assertEquals(result, response.getContentAsString());
    }

    void testOrderableServiceDetails() throws Exception {
        // Setup

        String result = objectMapper.writeValueAsString(
                transToUserOrderableServiceVo(serviceTemplateDetailVo));

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(get("/xpanse/catalog/services/{id}", id)
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assertions.assertEquals(result, response.getContentAsString());
    }

    void testOrderableServiceDetailsThrowsServiceTemplateNotRegistered() throws Exception {
        // Setup
        String uuid = UUID.randomUUID().toString();
        Response expectedResponse = Response.errorResponse(
                ResultType.SERVICE_TEMPLATE_NOT_REGISTERED,
                Collections.singletonList(
                        String.format("Service template with id %s not found.", uuid)));
        String result = objectMapper.writeValueAsString(expectedResponse);

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(get("/xpanse/catalog/services/{id}", uuid)
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        Assertions.assertEquals(result, response.getContentAsString());
    }

    void testOrderableServiceDetailsThrowsServiceTemplateNotApproved() throws Exception {
        // Setup
        if (serviceTemplateDetailVo == null || StringUtils.isBlank(id)) {
            registerServiceTemplate();
        }
        Response expectedResponse = Response.errorResponse(
                ResultType.SERVICE_TEMPLATE_NOT_APPROVED,
                Collections.singletonList(
                        String.format("Service template with id %s not approved.", id)));
        String result = objectMapper.writeValueAsString(expectedResponse);

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(get("/xpanse/catalog/services/{id}", id)
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        Assertions.assertEquals(result, response.getContentAsString());
    }

    void testOpenApi() throws Exception {
        // Setup

        Link link = Link.of(String.format("http://localhost/openapi/%s.html", id), "OpenApi");

        String result = objectMapper.writeValueAsString(link);

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                        get("/xpanse/catalog/services/{id}/openapi",
                                id)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assertions.assertEquals(result, response.getContentAsString());
    }

    void testOpenApiThrowsException() throws Exception {
        // Setup
        String uuid = UUID.randomUUID().toString();
        Response expectedResponse = Response.errorResponse(
                ResultType.SERVICE_TEMPLATE_NOT_REGISTERED,
                Collections.singletonList(
                        String.format("Service template with id %s not found.", uuid)));

        String result = objectMapper.writeValueAsString(expectedResponse);

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                        get("/xpanse/catalog/services/{id}/openapi",
                                uuid)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        Assertions.assertEquals(result, response.getContentAsString());
    }
}
