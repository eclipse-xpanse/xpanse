/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceRegistrationState;
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
 * Test for ServiceTemplateManageApi.
 */
@Slf4j
@Transactional
@ExtendWith(SpringExtension.class)
@ActiveProfiles("terraform-boot")
@SpringBootTest(classes = {XpanseApplication.class})
@AutoConfigureMockMvc
class ServiceTemplateApiTest {

    private static String id;
    private static ServiceTemplateVo serviceTemplateVo;
    private static UserAvailableServiceVo userAvailableServiceVo;
    private static Ocl ocl;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private MockMvc mockMvc;

    @Test
    void testManageServiceTemplate() throws Exception {
        testRegister();
        Thread.sleep(1000);
        testDetail();
        testListRegisteredServices();
        testUpdate();
        testUnregister();
    }

    @Test
    void testFetchMethods() throws Exception {
        ocl = new OclLoader().getOcl(new URL("file:src/test/resources/ocl_test.yaml"));
        testFetch();
        testFetchUpdate();
        testUnregister();
    }

    @Test
    void testRegisterServiceThrowsException() throws Exception {
        testRegisterException();
        testDetailThrowsException();
        testUpdateThrowsException();
        testListRegisteredServicesThrowsException();
        testListRegisteredServicesReturnsNoItems();
        testUnregisterThrowsException();
    }

    @Test
    void testFetchMethodsThrowsException() throws Exception {
        testFetchThrowsException();
        testDetailThrowsException();
        testFetchUpdateThrowsException();
        testUnregisterThrowsException();
    }


    void testRegister() throws Exception {
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

        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), registerResponse.getStatus());
        Assertions.assertEquals(ServiceRegistrationState.REGISTERED,
                serviceTemplateVo.getServiceRegistrationState());
        Assertions.assertEquals(ocl.getCategory(), serviceTemplateVo.getCategory());
        Assertions.assertEquals(ocl.getCloudServiceProvider().getName(),
                serviceTemplateVo.getCsp());
        Assertions.assertEquals(ocl.getName().toLowerCase(Locale.ROOT),
                serviceTemplateVo.getName());
        Assertions.assertEquals(ocl.getServiceVersion(), serviceTemplateVo.getVersion());
    }

    void testRegisterException() throws Exception {
        // Setup
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        ocl = new OclLoader().getOcl(new URL("file:src/test/resources/ocl_test_dummy.yaml"));
        ocl.getDeployment().setDeployer("error_" + ocl.getDeployment().getDeployer());
        String requestBody = yamlMapper.writeValueAsString(ocl);
        Response expectedResponse = Response.errorResponse(ResultType.TERRAFORM_EXECUTION_FAILED,
                Collections.singletonList(
                        "Executor Exception:TFExecutor.tfInit failed"));
        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(post("/xpanse/service_templates")
                                .content(requestBody).contentType("application/x-yaml")
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        Response actualResponse =
                objectMapper.readValue(response.getContentAsString(), Response.class);

        // Verify the results
        Assertions.assertEquals(HttpStatus.BAD_GATEWAY.value(), response.getStatus());
        Assertions.assertEquals(expectedResponse.getResultType(), actualResponse.getResultType());
        Assertions.assertFalse(actualResponse.getSuccess());
        Assertions.assertEquals(expectedResponse.getSuccess(), actualResponse.getSuccess());
    }

    void testUpdate() throws Exception {
        // Setup
        ocl = new OclLoader().getOcl(new URL("file:src/test/resources/ocl_test.yaml"));
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        ocl = new OclLoader().getOcl(new URL("file:src/test/resources/ocl_test_dummy.yaml"));
        String requestBody = yamlMapper.writeValueAsString(ocl);

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(put("/xpanse/service_templates/{id}", id)
                                .content(requestBody).contentType("application/x-yaml")
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        ServiceTemplateVo updatedServiceTemplateVo =
                objectMapper.readValue(response.getContentAsString(), ServiceTemplateVo.class);
        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assertions.assertEquals(ServiceRegistrationState.UPDATED,
                updatedServiceTemplateVo.getServiceRegistrationState());
        Assertions.assertEquals(id, updatedServiceTemplateVo.getId().toString());
        Assertions.assertEquals(updatedServiceTemplateVo.getOcl().getNamespace(),
                serviceTemplateVo.getOcl().getNamespace() + "_update");
        Assertions.assertNotEquals(
                updatedServiceTemplateVo.getOcl().getDeployment().getDeployer(),
                serviceTemplateVo.getOcl().getDeployment().getDeployer());
    }

    void testUpdateThrowsException() throws Exception {
        // Setup
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        ocl = new OclLoader().getOcl(new URL("file:src/test/resources/ocl_test_dummy.yaml"));
        String requestBody = yamlMapper.writeValueAsString(ocl);
        String uuid = UUID.randomUUID().toString();
        Response expectedResponse = Response.errorResponse(
                ResultType.SERVICE_TEMPLATE_NOT_REGISTERED,
                Collections.singletonList(
                        String.format("Service template with id %s not found.", uuid)));
        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(put("/xpanse/service_templates/{id}", uuid)
                                .content(requestBody).contentType("application/x-yaml")
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        Assertions.assertEquals(response.getContentAsString(),
                objectMapper.writeValueAsString(expectedResponse));
    }

    void testFetchThrowsException() throws Exception {
        // Setup
        String fileUrl = new URL("file:src/test/resources/ocl_test_update.yaml").toString();
        Response expectedResponse = Response.errorResponse(ResultType.RUNTIME_ERROR,
                Collections.singletonList("java.io.FileNotFoundException:"));

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(post("/xpanse/service_templates/file")
                                .param("oclLocation", fileUrl)
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();
        Response responseModel =
                objectMapper.readValue(response.getContentAsString(), Response.class);

        // Verify the results
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());
        Assertions.assertEquals(responseModel.getResultType(), expectedResponse.getResultType());
        Assertions.assertEquals(responseModel.getSuccess(), expectedResponse.getSuccess());
    }

    void testFetchUpdateThrowsException() throws Exception {
        // Setup
        String fileUrl = new URL("file:src/test/resources/ocl_test_dummy.yaml").toString();
        String uuid = UUID.randomUUID().toString();
        Response expectedResponse = Response.errorResponse(
                ResultType.SERVICE_TEMPLATE_NOT_REGISTERED,
                Collections.singletonList(
                        String.format("Service template with id %s not found.", uuid)));

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(put("/xpanse/service_templates/file/{id}", uuid)
                                .param("oclLocation", fileUrl)
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        Assertions.assertEquals(response.getContentAsString(),
                objectMapper.writeValueAsString(expectedResponse));
    }


    void testUnregister() throws Exception {
        // Setup
        Response expectedResponse = Response.successResponse(
                Collections.singletonList(
                        String.format("Unregister service template using id %s successful.",
                                id)));
        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(delete("/xpanse/service_templates/{id}", id)
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assertions.assertEquals(response.getContentAsString(),
                objectMapper.writeValueAsString(expectedResponse));
    }

    void testUnregisterThrowsException() throws Exception {
        // Setup
        String uuid = UUID.randomUUID().toString();
        Response expectedResponse = Response.errorResponse(
                ResultType.SERVICE_TEMPLATE_NOT_REGISTERED,
                Collections.singletonList(
                        String.format("Service template with id %s not found.", uuid)));
        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(delete("/xpanse/service_templates/{id}", uuid)
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        Assertions.assertEquals(response.getContentAsString(),
                objectMapper.writeValueAsString(expectedResponse));
    }

    void testListRegisteredServices() throws Exception {
        List<UserAvailableServiceVo> serviceTemplateCatalogs = List.of(convertToServiceTemplateVo(
                serviceTemplateVo));
        String result = objectMapper.writeValueAsString(serviceTemplateCatalogs);

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(get("/xpanse/service_templates")
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

    void testListRegisteredServicesThrowsException() throws Exception {
        // Setup
        String errorMessage = "Failed to convert value of type 'java.lang.String' to required type";
        Response expectedResponse =
                Response.errorResponse(ResultType.UNPROCESSABLE_ENTITY, List.of(errorMessage));

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(get("/xpanse/service_templates")
                        .param("categoryName", Category.AI.toValue())
                        .param("cspName", "errorCspName")
                        .param("serviceName", "kafka-cluster")
                        .param("serviceVersion", "v3.3.2")
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


    void testListRegisteredServicesReturnsNoItems() throws Exception {
        // Setup
        String result = "[]";

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(get("/xpanse/service_templates")
                        .param("categoryName", "middleware")
                        .param("cspName", "aws")
                        .param("serviceName", "kafka-cluster")
                        .param("serviceVersion", "v3.3.2")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assertions.assertEquals(result, response.getContentAsString());
    }


    void testDetail() throws Exception {
        // Setup
        String result = objectMapper.writeValueAsString(serviceTemplateVo);

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(get("/xpanse/service_templates/{id}", id)
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assertions.assertEquals(result, response.getContentAsString());
    }

    @Test
    void testDetailThrowsException() throws Exception {
        // Setup
        String uuid = UUID.randomUUID().toString();
        Response expectedResponse = Response.errorResponse(
                ResultType.SERVICE_TEMPLATE_NOT_REGISTERED,
                Collections.singletonList(
                        String.format("Service template with id %s not found.", uuid)));
        String result = objectMapper.writeValueAsString(expectedResponse);

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(get("/xpanse/service_templates/{id}", uuid)
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        Assertions.assertEquals(result, response.getContentAsString());
    }

    void testFetch() throws Exception {
        // Setup
        String fileUrl = new URL("file:src/test/resources/ocl_test.yaml").toString();

        // Run the test
        final MockHttpServletResponse fetchResponse =
                mockMvc.perform(post("/xpanse/service_templates/file")
                                .param("oclLocation", fileUrl)
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        serviceTemplateVo =
                objectMapper.readValue(fetchResponse.getContentAsString(),
                        ServiceTemplateVo.class);
        id = serviceTemplateVo.getId().toString();

        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), fetchResponse.getStatus());
        Assertions.assertEquals(ServiceRegistrationState.REGISTERED,
                serviceTemplateVo.getServiceRegistrationState());
        Assertions.assertEquals(ocl.getCategory(), serviceTemplateVo.getCategory());
        Assertions.assertEquals(ocl.getCloudServiceProvider().getName(),
                serviceTemplateVo.getCsp());
        Assertions.assertEquals(ocl.getName().toLowerCase(Locale.ROOT),
                serviceTemplateVo.getName());
        Assertions.assertEquals(ocl.getServiceVersion(), serviceTemplateVo.getVersion());
    }

    void testFetchUpdate() throws Exception {
        // Setup
        String fileUrl = new URL("file:src/test/resources/ocl_test_dummy.yaml").toString();

        // Run the test
        final MockHttpServletResponse fetchUpdateResponse =
                mockMvc.perform(put("/xpanse/service_templates/file/{id}", id)
                                .param("oclLocation", fileUrl)
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();
        ServiceTemplateVo updatedServiceTemplateVo =
                objectMapper.readValue(fetchUpdateResponse.getContentAsString(),
                        ServiceTemplateVo.class);
        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), fetchUpdateResponse.getStatus());
        Assertions.assertEquals(ServiceRegistrationState.UPDATED,
                updatedServiceTemplateVo.getServiceRegistrationState());
        Assertions.assertEquals(id, updatedServiceTemplateVo.getId().toString());
        Assertions.assertEquals(updatedServiceTemplateVo.getOcl().getNamespace(),
                serviceTemplateVo.getOcl().getNamespace() + "_update");
        Assertions.assertNotEquals(
                updatedServiceTemplateVo.getOcl().getDeployment().getDeployer(),
                serviceTemplateVo.getOcl().getDeployment().getDeployer());
    }

    UserAvailableServiceVo convertToServiceTemplateVo(
            ServiceTemplateVo serviceTemplateVo) {
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
}
