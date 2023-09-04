/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateVo;
import org.eclipse.xpanse.modules.models.servicetemplate.view.UserAvailableServiceVo;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test for ServiceRegisterApiTest.
 */
@Slf4j
@Transactional
@ExtendWith(SpringExtension.class)
@ActiveProfiles("terraform-boot")
@SpringBootTest(classes = {XpanseApplication.class})
@AutoConfigureMockMvc
class ServiceCatalogApiTest {

    private static String id;
    private static ServiceTemplateVo serviceTemplateVo;
    private static Ocl ocl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private MockMvc mockMvc;

    @Test
    void testAvailableServices() throws Exception {
        registerService();
        Thread.sleep(3000);
        testOpenApi();
        testAvailableServiceDetails();
        testListAvailableServices();
        unregisterService();
    }

    @Test
    void testAvailableServicesThrowsException() throws Exception {
        testAvailableServiceDetailsThrowsException();
        testListAvailableServicesThrowsException();
        testOpenApiThrowsException();
        testListAvailableServicesReturnsNoItems();
    }

    void registerService() throws Exception {
        // Setup
        ocl = new OclLoader().getOcl(new URL("file:src/test/resources/ocl_test.yaml"));
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        String requestBody = yamlMapper.writeValueAsString(ocl);

        // Run the test
        final MockHttpServletResponse registerResponse =
                mockMvc.perform(post("/xpanse/service_templates")
                                .content(requestBody).contentType("application/x-yaml")
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();
        serviceTemplateVo =
                objectMapper.readValue(registerResponse.getContentAsString(),
                        ServiceTemplateVo.class);
        id = serviceTemplateVo.getId().toString();

    }

    void unregisterService() throws Exception {
        final MockHttpServletResponse response =
                mockMvc.perform(delete("/xpanse/service_templates/{id}", id)
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
        serviceTemplateVo = null;
        id = null;
    }

    void testListAvailableServices() throws Exception {
        // Setup
        List<UserAvailableServiceVo> userAvailableServiceVos =
                List.of(transUserAvailableServiceVo(serviceTemplateVo));
        String result = objectMapper.writeValueAsString(userAvailableServiceVos);

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

    UserAvailableServiceVo transUserAvailableServiceVo(ServiceTemplateVo serviceTemplateVo) {
        UserAvailableServiceVo userAvailableServiceVo = new UserAvailableServiceVo();
        BeanUtils.copyProperties(serviceTemplateVo, userAvailableServiceVo);
        userAvailableServiceVo.setIcon(serviceTemplateVo.getOcl().getIcon());
        userAvailableServiceVo.setDescription(serviceTemplateVo.getOcl().getDescription());
        userAvailableServiceVo.setNamespace(serviceTemplateVo.getOcl().getNamespace());
        userAvailableServiceVo.setBilling(serviceTemplateVo.getOcl().getBilling());
        userAvailableServiceVo.setFlavors(serviceTemplateVo.getOcl().getFlavors());
        userAvailableServiceVo.setDeployment(serviceTemplateVo.getOcl().getDeployment());
        userAvailableServiceVo.setVariables(
                serviceTemplateVo.getOcl().getDeployment().getVariables());
        userAvailableServiceVo.setRegions(
                serviceTemplateVo.getOcl().getCloudServiceProvider().getRegions());
        userAvailableServiceVo.add(
                Link.of(String.format("http://localhost/xpanse/catalog/services/%s/openapi",
                        serviceTemplateVo.getId().toString()), "openApi"));

        return userAvailableServiceVo;
    }

    void testListAvailableServicesThrowsException() throws Exception {
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

    void testListAvailableServicesReturnsNoItems() throws Exception {
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

    void testAvailableServiceDetails() throws Exception {
        // Setup

        String result = objectMapper.writeValueAsString(
                transUserAvailableServiceVo(serviceTemplateVo));

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(get("/xpanse/catalog/services/{id}", id)
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assertions.assertEquals(result, response.getContentAsString());
    }

    void testAvailableServiceDetailsThrowsException() throws Exception {
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
