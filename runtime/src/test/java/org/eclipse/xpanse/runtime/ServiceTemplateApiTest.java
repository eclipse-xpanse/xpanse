/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceRegistrationState;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test for ServiceTemplateManageApi.
 */
@Slf4j
@Transactional
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=zitadel,zitadel-testbed"})
@AutoConfigureMockMvc
class ServiceTemplateApiTest {

    private static String id;
    private static ServiceTemplateDetailVo serviceTemplateDetailVo;
    private static Ocl ocl;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private MockMvc mockMvc;

    @BeforeAll
    static void configureObjectMapper() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new SimpleModule().addSerializer(OffsetDateTime.class,
                OffsetDateTimeSerializer.INSTANCE));
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);

    }

    @Test
    @WithJwt(file = "jwt_isv.json")
    void testManageServiceTemplate() throws Exception {
        testRegister();
        Thread.sleep(1000);
        testDetail();
        testListRegisteredServices();
        testUpdate();
        testUnregister();
    }

    @Test
    @WithJwt(file = "jwt_isv.json")
    void testFetchMethods() throws Exception {
        ocl = new OclLoader().getOcl(URI.create("file:src/test/resources/ocl_test.yaml").toURL());
        testFetch();
        testFetchUpdate();
        testUnregister();
    }

    @Test
    @WithJwt(file = "jwt_isv.json")
    void testRegisterServiceThrowsException() throws Exception {
        testRegisterThrowsMethodArgumentNotValidException();
        testRegisterThrowsTerraformExecutionException();
        testDetailThrowsException();
        testUpdateThrowsException();
        testListRegisteredServicesThrowsException();
        testListRegisteredServicesReturnsNoItems();
        testUnregisterThrowsException();
    }

    @Test
    @WithJwt(file = "jwt_isv.json")
    void testFetchMethodsThrowsException() throws Exception {
        testFetchThrowsException();
        testDetailThrowsException();
        testFetchUpdateThrowsException();
        testUnregisterThrowsException();
    }


    void testRegister() throws Exception {
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

        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), registerResponse.getStatus());
        Assertions.assertEquals(ServiceRegistrationState.REGISTERED,
                serviceTemplateDetailVo.getServiceRegistrationState());
        Assertions.assertEquals(ocl.getCategory(), serviceTemplateDetailVo.getCategory());
        Assertions.assertEquals(ocl.getCloudServiceProvider().getName(),
                serviceTemplateDetailVo.getCsp());
        Assertions.assertEquals(ocl.getName().toLowerCase(Locale.ROOT),
                serviceTemplateDetailVo.getName());
        Assertions.assertEquals(ocl.getServiceVersion(), serviceTemplateDetailVo.getVersion());
    }

    void testRegisterThrowsMethodArgumentNotValidException() throws Exception {
        // Setup
        ocl = new OclLoader().getOcl(URI.create("file:src/test/resources/ocl_test.yaml").toURL());
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        ocl.setFlavors(null);
        String requestBody = yamlMapper.writeValueAsString(ocl);

        Response expectedResponse = Response.errorResponse(ResultType.UNPROCESSABLE_ENTITY,
                Collections.singletonList(
                        "flavors:must not be null"));

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(post("/xpanse/service_templates")
                                .content(requestBody).contentType("application/x-yaml")
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        Response actualResponse =
                objectMapper.readValue(response.getContentAsString(), Response.class);

        // Verify the results
        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.getStatus());
        Assertions.assertEquals(expectedResponse.getResultType(), actualResponse.getResultType());
        Assertions.assertEquals(expectedResponse.getDetails(), actualResponse.getDetails());
        Assertions.assertEquals(expectedResponse.getSuccess(), actualResponse.getSuccess());
    }

    void testRegisterThrowsTerraformExecutionException() throws Exception {
        // Setup
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        ocl = new OclLoader().getOcl(
                URI.create("file:src/test/resources/ocl_test_dummy.yaml").toURL());
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
        Assertions.assertEquals(expectedResponse.getResultType(),
                actualResponse.getResultType());
        Assertions.assertFalse(actualResponse.getSuccess());
        Assertions.assertEquals(expectedResponse.getSuccess(), actualResponse.getSuccess());
    }

    void testUpdate() throws Exception {
        // Setup
        ocl = new OclLoader().getOcl(URI.create("file:src/test/resources/ocl_test.yaml").toURL());
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        ocl = new OclLoader().getOcl(
                URI.create("file:src/test/resources/ocl_test_dummy.yaml").toURL());
        ocl.setIcon("https://avatars.githubusercontent.com/u/127229590?s=48&v=4");
        String requestBody = yamlMapper.writeValueAsString(ocl);

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(put("/xpanse/service_templates/{id}", id)
                                .content(requestBody).contentType("application/x-yaml")
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        ServiceTemplateDetailVo updatedServiceTemplateDetailVo =
                objectMapper.readValue(response.getContentAsString(),
                        ServiceTemplateDetailVo.class);
        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assertions.assertEquals(ServiceRegistrationState.UPDATED,
                updatedServiceTemplateDetailVo.getServiceRegistrationState());
        Assertions.assertEquals(id, updatedServiceTemplateDetailVo.getId().toString());
        Assertions.assertEquals(updatedServiceTemplateDetailVo.getNamespace(),
                serviceTemplateDetailVo.getNamespace() + "_update");
        Assertions.assertNotEquals(
                updatedServiceTemplateDetailVo.getDeployment().getDeployer(),
                serviceTemplateDetailVo.getDeployment().getDeployer());
    }

    void testUpdateThrowsException() throws Exception {
        // Setup
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        ocl = new OclLoader().getOcl(
                URI.create("file:src/test/resources/ocl_test_dummy.yaml").toURL());
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
        String fileUrl =
                URI.create("file:src/test/resources/ocl_test_update.yaml").toURL().toString();
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
        String fileUrl =
                URI.create("file:src/test/resources/ocl_test_dummy.yaml").toURL().toString();
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
        List<ServiceTemplateDetailVo> serviceTemplateDetailVos = List.of(
                serviceTemplateDetailVo);

        String result = objectMapper.writeValueAsString(serviceTemplateDetailVos);

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
        assertThat(serviceTemplateDetailVos).usingRecursiveFieldByFieldElementComparatorIgnoringFields("lastModifiedTime").isEqualTo(
                Arrays.stream(objectMapper.readValue(response.getContentAsString(), ServiceTemplateDetailVo[].class)).toList());
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
        String result = objectMapper.writeValueAsString(serviceTemplateDetailVo);

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(get("/xpanse/service_templates/{id}", id)
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assertions.assertEquals(result, response.getContentAsString());
    }

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
        String fileUrl = URI.create("file:src/test/resources/ocl_test.yaml").toURL().toString();

        // Run the test
        final MockHttpServletResponse fetchResponse =
                mockMvc.perform(post("/xpanse/service_templates/file")
                                .param("oclLocation", fileUrl)
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        serviceTemplateDetailVo =
                objectMapper.readValue(fetchResponse.getContentAsString(),
                        ServiceTemplateDetailVo.class);
        id = serviceTemplateDetailVo.getId().toString();

        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), fetchResponse.getStatus());
        Assertions.assertEquals(ServiceRegistrationState.REGISTERED,
                serviceTemplateDetailVo.getServiceRegistrationState());
        Assertions.assertEquals(ocl.getCategory(), serviceTemplateDetailVo.getCategory());
        Assertions.assertEquals(ocl.getCloudServiceProvider().getName(),
                serviceTemplateDetailVo.getCsp());
        Assertions.assertEquals(ocl.getName().toLowerCase(Locale.ROOT),
                serviceTemplateDetailVo.getName());
        Assertions.assertEquals(ocl.getServiceVersion(), serviceTemplateDetailVo.getVersion());
    }

    void testFetchUpdate() throws Exception {
        // Setup
        String fileUrl = URI.create("file:src/test/resources/ocl_test_dummy.yaml").toURL().toString();

        // Run the test
        final MockHttpServletResponse fetchUpdateResponse =
                mockMvc.perform(put("/xpanse/service_templates/file/{id}", id)
                                .param("oclLocation", fileUrl)
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();
        ServiceTemplateDetailVo updatedServiceTemplateDetailVo =
                objectMapper.readValue(fetchUpdateResponse.getContentAsString(),
                        ServiceTemplateDetailVo.class);
        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), fetchUpdateResponse.getStatus());
        Assertions.assertEquals(ServiceRegistrationState.UPDATED,
                updatedServiceTemplateDetailVo.getServiceRegistrationState());
        Assertions.assertEquals(id, updatedServiceTemplateDetailVo.getId().toString());
        Assertions.assertEquals(updatedServiceTemplateDetailVo.getNamespace(),
                serviceTemplateDetailVo.getNamespace() + "_update");
        Assertions.assertNotEquals(
                updatedServiceTemplateDetailVo.getDeployment().getDeployer(),
                serviceTemplateDetailVo.getDeployment().getDeployer());
    }
}
