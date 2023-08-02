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
import org.eclipse.xpanse.modules.models.service.register.Ocl;
import org.eclipse.xpanse.modules.models.service.register.enums.ServiceRegistrationState;
import org.eclipse.xpanse.modules.models.service.utils.OclLoader;
import org.eclipse.xpanse.modules.models.service.view.RegisteredServiceVo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
class ServiceRegisterApiTest {

    private static String id;
    private static RegisteredServiceVo registeredServiceVo;
    private static Ocl ocl;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private MockMvc mockMvc;

    @Test
    void testRegisterService() throws Exception {
        testRegister();
        Thread.sleep(3000);
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
                mockMvc.perform(post("/xpanse/services/register")
                                .content(requestBody).contentType("application/x-yaml")
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();
        registeredServiceVo =
                objectMapper.readValue(registerResponse.getContentAsString(),
                        RegisteredServiceVo.class);
        id = registeredServiceVo.getId().toString();

        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), registerResponse.getStatus());
        Assertions.assertEquals(ServiceRegistrationState.REGISTERED,
                registeredServiceVo.getServiceRegistrationState());
        Assertions.assertEquals(ocl.getCategory(), registeredServiceVo.getCategory());
        Assertions.assertEquals(ocl.getCloudServiceProvider().getName(),
                registeredServiceVo.getCsp());
        Assertions.assertEquals(ocl.getName().toLowerCase(Locale.ROOT),
                registeredServiceVo.getName());
        Assertions.assertEquals(ocl.getServiceVersion(), registeredServiceVo.getVersion());
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
                mockMvc.perform(post("/xpanse/services/register")
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
                mockMvc.perform(put("/xpanse/services/register/{id}", id)
                                .content(requestBody).contentType("application/x-yaml")
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        RegisteredServiceVo updatedRegisteredServiceVo =
                objectMapper.readValue(response.getContentAsString(), RegisteredServiceVo.class);
        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assertions.assertEquals(ServiceRegistrationState.UPDATED,
                updatedRegisteredServiceVo.getServiceRegistrationState());
        Assertions.assertEquals(id, updatedRegisteredServiceVo.getId().toString());
        Assertions.assertEquals(updatedRegisteredServiceVo.getOcl().getNamespace(),
                registeredServiceVo.getOcl().getNamespace() + "_update");
        Assertions.assertNotEquals(
                updatedRegisteredServiceVo.getOcl().getDeployment().getDeployer(),
                registeredServiceVo.getOcl().getDeployment().getDeployer());
    }

    void testUpdateThrowsException() throws Exception {
        // Setup
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        ocl = new OclLoader().getOcl(new URL("file:src/test/resources/ocl_test_dummy.yaml"));
        String requestBody = yamlMapper.writeValueAsString(ocl);
        String uuid = UUID.randomUUID().toString();
        Response expectedResponse = Response.errorResponse(ResultType.SERVICE_NOT_REGISTERED,
                Collections.singletonList(
                        String.format("Registered service with id %s not found.", uuid)));
        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(put("/xpanse/services/register/{id}", uuid)
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
                mockMvc.perform(post("/xpanse/services/register/file")
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
        Response expectedResponse = Response.errorResponse(ResultType.SERVICE_NOT_REGISTERED,
                Collections.singletonList(
                        String.format("Registered service with id %s not found.", uuid)));

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(put("/xpanse/services/register/file/{id}", uuid)
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
                        String.format("Unregister registered service using id %s successful.",
                                id)));
        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(delete("/xpanse/services/register/{id}", id)
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
        Response expectedResponse = Response.errorResponse(ResultType.SERVICE_NOT_REGISTERED,
                Collections.singletonList(
                        String.format("Registered service with id %s not found.", uuid)));
        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(delete("/xpanse/services/register/{id}", uuid)
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        Assertions.assertEquals(response.getContentAsString(),
                objectMapper.writeValueAsString(expectedResponse));
    }

    void testListRegisteredServices() throws Exception {
        List<RegisteredServiceVo> registeredServiceVos = List.of(registeredServiceVo);
        String result = objectMapper.writeValueAsString(registeredServiceVos);

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(get("/xpanse/services/register")
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
        final MockHttpServletResponse response = mockMvc.perform(get("/xpanse/services/register")
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
        final MockHttpServletResponse response = mockMvc.perform(get("/xpanse/services/register")
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
        String result = objectMapper.writeValueAsString(registeredServiceVo);

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(get("/xpanse/services/register/{id}", id)
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
        Response expectedResponse = Response.errorResponse(ResultType.SERVICE_NOT_REGISTERED,
                Collections.singletonList(
                        String.format("Registered service with id %s not found.", uuid)));
        String result = objectMapper.writeValueAsString(expectedResponse);

        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(get("/xpanse/services/register/{id}", uuid)
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
                mockMvc.perform(post("/xpanse/services/register/file")
                                .param("oclLocation", fileUrl)
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        registeredServiceVo =
                objectMapper.readValue(fetchResponse.getContentAsString(),
                        RegisteredServiceVo.class);
        id = registeredServiceVo.getId().toString();

        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), fetchResponse.getStatus());
        Assertions.assertEquals(ServiceRegistrationState.REGISTERED,
                registeredServiceVo.getServiceRegistrationState());
        Assertions.assertEquals(ocl.getCategory(), registeredServiceVo.getCategory());
        Assertions.assertEquals(ocl.getCloudServiceProvider().getName(),
                registeredServiceVo.getCsp());
        Assertions.assertEquals(ocl.getName().toLowerCase(Locale.ROOT),
                registeredServiceVo.getName());
        Assertions.assertEquals(ocl.getServiceVersion(), registeredServiceVo.getVersion());
    }

    void testFetchUpdate() throws Exception {
        // Setup
        String fileUrl = new URL("file:src/test/resources/ocl_test_dummy.yaml").toString();

        // Run the test
        final MockHttpServletResponse fetchUpdateResponse =
                mockMvc.perform(put("/xpanse/services/register/file/{id}", id)
                                .param("oclLocation", fileUrl)
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();
        RegisteredServiceVo updatedRegisteredServiceVo =
                objectMapper.readValue(fetchUpdateResponse.getContentAsString(),
                        RegisteredServiceVo.class);
        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), fetchUpdateResponse.getStatus());
        Assertions.assertEquals(ServiceRegistrationState.UPDATED,
                updatedRegisteredServiceVo.getServiceRegistrationState());
        Assertions.assertEquals(id, updatedRegisteredServiceVo.getId().toString());
        Assertions.assertEquals(updatedRegisteredServiceVo.getOcl().getNamespace(),
                registeredServiceVo.getOcl().getNamespace() + "_update");
        Assertions.assertNotEquals(
                updatedRegisteredServiceVo.getOcl().getDeployment().getDeployer(),
                registeredServiceVo.getOcl().getDeployment().getDeployer());
    }
}
