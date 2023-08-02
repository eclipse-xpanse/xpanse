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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.register.Ocl;
import org.eclipse.xpanse.modules.models.service.register.Region;
import org.eclipse.xpanse.modules.models.service.utils.OclLoader;
import org.eclipse.xpanse.modules.models.service.view.CategoryOclVo;
import org.eclipse.xpanse.modules.models.service.view.ProviderOclVo;
import org.eclipse.xpanse.modules.models.service.view.RegisteredServiceVo;
import org.eclipse.xpanse.modules.models.service.view.UserAvailableServiceVo;
import org.eclipse.xpanse.modules.models.service.view.VersionOclVo;
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
@ActiveProfiles("default")
@SpringBootTest(classes = {XpanseApplication.class})
@AutoConfigureMockMvc
class ServiceCatalogApiTest {

    private static String id;
    private static RegisteredServiceVo registeredServiceVo;
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
        testGetAvailableServicesTree();
        unregisterService();
    }

    @Test
    void testAvailableServicesThrowsException() throws Exception {
        testAvailableServiceDetailsThrowsException();
        testListAvailableServicesThrowsException();
        testOpenApiThrowsException();
        testListAvailableServicesReturnsNoItems();
        testGetAvailableServicesTreeReturnsNoItems();
    }

    void registerService() throws Exception {
        // Setup
        ocl = new OclLoader().getOcl(new URL("file:src/test/resources/ocl_test.yaml"));
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        String requestBody = yamlMapper.writeValueAsString(ocl);

        // Run the test
        final MockHttpServletResponse registerResponse =
                mockMvc.perform(post("/xpanse/services/register")
                                .content(requestBody).contentType("application/x-yaml")
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();
        registeredServiceVo =
                objectMapper.readValue(registerResponse.getContentAsString(),
                        RegisteredServiceVo.class);
        id = registeredServiceVo.getId().toString();

    }

    void unregisterService() throws Exception {
        final MockHttpServletResponse response =
                mockMvc.perform(delete("/xpanse/services/register/{id}", id)
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
        registeredServiceVo = null;
        id = null;
    }

    void testListAvailableServices() throws Exception {
        // Setup
        List<UserAvailableServiceVo> userAvailableServiceVos =
                List.of(transUserAvailableServiceVo(registeredServiceVo, true));
        String result = objectMapper.writeValueAsString(userAvailableServiceVos);

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(get("/xpanse/services/available")
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

    UserAvailableServiceVo transUserAvailableServiceVo(RegisteredServiceVo registeredServiceVo,
                                                       boolean addLinkApi) {
        UserAvailableServiceVo userAvailableServiceVo = new UserAvailableServiceVo();
        BeanUtils.copyProperties(registeredServiceVo, userAvailableServiceVo);
        userAvailableServiceVo.setIcon(registeredServiceVo.getOcl().getIcon());
        userAvailableServiceVo.setDescription(registeredServiceVo.getOcl().getDescription());
        userAvailableServiceVo.setNamespace(registeredServiceVo.getOcl().getNamespace());
        userAvailableServiceVo.setBilling(registeredServiceVo.getOcl().getBilling());
        userAvailableServiceVo.setFlavors(registeredServiceVo.getOcl().getFlavors());
        userAvailableServiceVo.setDeployment(registeredServiceVo.getOcl().getDeployment());
        userAvailableServiceVo.setVariables(
                registeredServiceVo.getOcl().getDeployment().getVariables());
        userAvailableServiceVo.setRegions(
                registeredServiceVo.getOcl().getCloudServiceProvider().getRegions());
        if (addLinkApi) {
            userAvailableServiceVo.add(
                    Link.of(String.format("http://localhost/xpanse/services/available/%s/openapi",
                            registeredServiceVo.getId().toString()), "openApi"));
        } else {
            userAvailableServiceVo.add(
                    Link.of(String.format("http://localhost/openapi/%s.html",
                            registeredServiceVo.getId().toString()), "openApi"));
        }

        return userAvailableServiceVo;
    }

    List<CategoryOclVo> getServiceTree(List<RegisteredServiceVo> registeredServiceVos) {
        List<CategoryOclVo> serviceTree = new ArrayList<>();
        Map<String, List<RegisteredServiceVo>> nameListMap =
                registeredServiceVos.stream()
                        .collect(Collectors.groupingBy(RegisteredServiceVo::getName));
        nameListMap.forEach((name, nameList) -> {
            CategoryOclVo categoryOclVo = new CategoryOclVo();
            categoryOclVo.setName(name);
            List<VersionOclVo> versionVoList = new ArrayList<>();
            Map<String, List<RegisteredServiceVo>> versionListMap =
                    nameList.stream()
                            .collect(Collectors.groupingBy(RegisteredServiceVo::getVersion));
            versionListMap.forEach((version, versionList) -> {
                VersionOclVo versionOclVo = new VersionOclVo();
                versionOclVo.setVersion(version);
                List<ProviderOclVo> cspVoList = new ArrayList<>();
                Map<Csp, List<RegisteredServiceVo>> cspListMap =
                        versionList.stream()
                                .collect(Collectors.groupingBy(RegisteredServiceVo::getCsp));
                cspListMap.forEach((csp, cspList) -> {
                    ProviderOclVo providerOclVo = new ProviderOclVo();
                    providerOclVo.setName(csp);
                    List<UserAvailableServiceVo> details = cspList.stream()
                            .map(serviceVo -> transUserAvailableServiceVo(serviceVo, false))
                            .collect(Collectors.toList());
                    providerOclVo.setDetails(details);
                    List<Region> regions = new ArrayList<>();
                    for (UserAvailableServiceVo userAvailableServiceVo : details) {
                        regions.addAll(userAvailableServiceVo.getRegions());
                    }
                    providerOclVo.setRegions(regions);
                    cspVoList.add(providerOclVo);
                });
                List<ProviderOclVo> sortedCspOclList =
                        cspVoList.stream().sorted(
                                        Comparator.comparingInt(o -> o.getName().ordinal()))
                                .collect(Collectors.toList());
                versionOclVo.setCloudProvider(sortedCspOclList);
                versionVoList.add(versionOclVo);
            });
            categoryOclVo.setVersions(versionVoList);
            serviceTree.add(categoryOclVo);
        });
        return serviceTree;
    }

    void testListAvailableServicesThrowsException() throws Exception {
        // Setup
        String errorMessage = "Failed to convert value of type 'java.lang.String' to required type";
        Response expectedResponse =
                Response.errorResponse(ResultType.UNPROCESSABLE_ENTITY, List.of(errorMessage));

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(get("/xpanse/services/available")
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
        Assertions.assertEquals(expectedResponse.getResultType(), resultResponse.getResultType());
    }

    void testListAvailableServicesReturnsNoItems() throws Exception {
        // Setup
        String result = "[]";
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(get("/xpanse/services/available")
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

    void testGetAvailableServicesTree() throws Exception {
        // Setup
        List<CategoryOclVo> serviceTree =
                getServiceTree(List.of(registeredServiceVo));
        String result = objectMapper.writeValueAsString(serviceTree);

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                        get("/xpanse/services/available/category/{categoryName}", Category.MIDDLEWARE)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assertions.assertEquals(result, response.getContentAsString());
    }

    void testGetAvailableServicesTreeReturnsNoItems() throws Exception {
        // Setup
        String result = "[]";
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                        get("/xpanse/services/available/category/{categoryName}", Category.AI)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assertions.assertEquals(result, response.getContentAsString());
    }

    void testAvailableServiceDetails() throws Exception {
        // Setup

        String result = objectMapper.writeValueAsString(
                transUserAvailableServiceVo(registeredServiceVo, true));

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(get("/xpanse/services/available/{id}", id)
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assertions.assertEquals(result, response.getContentAsString());
    }

    void testAvailableServiceDetailsThrowsException() throws Exception {
        // Setup
        String uuid = UUID.randomUUID().toString();
        Response expectedResponse = Response.errorResponse(ResultType.SERVICE_NOT_REGISTERED,
                Collections.singletonList(
                        String.format("Registered service with id %s not found.", uuid)));
        String result = objectMapper.writeValueAsString(expectedResponse);

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(get("/xpanse/services/available/{id}", uuid)
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
                        get("/xpanse/services/available/{id}/openapi",
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
        Response expectedResponse = Response.errorResponse(ResultType.SERVICE_NOT_REGISTERED,
                Collections.singletonList(
                        String.format("Registered service with id %s not found.", uuid)));

        String result = objectMapper.writeValueAsString(expectedResponse);

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                        get("/xpanse/services/available/{id}/openapi",
                                uuid)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        Assertions.assertEquals(result, response.getContentAsString());
    }
}
