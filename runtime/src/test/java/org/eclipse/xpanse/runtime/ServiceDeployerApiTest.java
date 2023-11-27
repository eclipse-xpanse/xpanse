/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.c4_soft.springaddons.security.oauth2.test.annotations.OpenIdClaims;
import com.c4_soft.springaddons.security.oauth2.test.annotations.WithMockJwtAuth;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.credential.CreateCredential;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.security.constant.RoleConstants;
import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.view.DeployedService;
import org.eclipse.xpanse.modules.models.service.view.DeployedServiceDetails;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.eclipse.xpanse.plugins.huaweicloud.monitor.constant.HuaweiCloudMonitorConstants;
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
 * Test for ServiceDeployerApi.
 */
@Slf4j
@Transactional
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=zitadel,zitadel-testbed"})
@AutoConfigureMockMvc
class ServiceDeployerApiTest {

    private static final long waitTime = 60 * 1000;
    static ServiceTemplateDetailVo serviceTemplateDetailVo;
    static Ocl ocl;
    static boolean credentialReady = false;
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
        serviceTemplateDetailVo = objectMapper.readValue(registerResponse.getContentAsString(),
                ServiceTemplateDetailVo.class);
    }

    void addCredential() throws Exception {

        final CreateCredential createCredential = new CreateCredential();
        createCredential.setCsp(Csp.HUAWEI);
        createCredential.setType(CredentialType.VARIABLES);
        createCredential.setName("AK_SK");
        createCredential.setDescription("description");
        List<CredentialVariable> credentialVariables = new ArrayList<>();
        credentialVariables.add(
                new CredentialVariable(HuaweiCloudMonitorConstants.HW_ACCESS_KEY,
                        "The access key.", true, false, "AK_VALUE"));
        credentialVariables.add(
                new CredentialVariable(HuaweiCloudMonitorConstants.HW_SECRET_KEY,
                        "The security key.", true, false, "SK_VALUE"));
        createCredential.setVariables(credentialVariables);
        createCredential.setTimeToLive(3000);
        String requestBody = objectMapper.writeValueAsString(createCredential);
        final MockHttpServletResponse response = mockMvc.perform(post("/xpanse/user/credentials")
                        .content(requestBody).contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        if (HttpStatus.NO_CONTENT.value() == response.getStatus()) {
            credentialReady = true;
        }
    }

    @Test
    @WithMockJwtAuth(authorities = RoleConstants.ROLE_ADMIN,
            claims = @OpenIdClaims(sub = "adminId", preferredUsername = "adminName"))
    void testServiceDeployerWell() throws Exception {
        if (!credentialReady) {
            addCredential();
        }
        if (Objects.isNull(serviceTemplateDetailVo)) {
            registerServiceTemplate();
        }
        UUID serviceId = testDeploy();
        if (waitUntilExceptedState(serviceId, ServiceDeploymentState.DEPLOY_SUCCESS)) {
            listDeployedServices();
        }
        testListDeployedServices();
        if (waitUntilExceptedState(serviceId, ServiceDeploymentState.DEPLOY_SUCCESS)) {
            testDestroy(serviceId);

        }
        if (waitUntilExceptedState(serviceId, ServiceDeploymentState.DESTROY_SUCCESS)) {
            testPurge(serviceId);
        }

    }

    boolean waitUntilExceptedState(UUID id, ServiceDeploymentState targetState) throws Exception {
        boolean isDone = false;
        long startTime = System.currentTimeMillis();
        while (!isDone) {
            DeployedService deployedService = getDeployedServiceDetails(id);
            if (Objects.nonNull(deployedService) &&
                    deployedService.getServiceDeploymentState() == targetState) {
                isDone = true;
            } else {
                if (System.currentTimeMillis() - startTime > waitTime) {
                    break;
                }
                Thread.sleep(5 * 3000);
            }

        }
        return isDone;
    }

    UUID testDeploy() throws Exception {
        DeployRequest deployRequest = new DeployRequest();
        deployRequest.setServiceName(serviceTemplateDetailVo.getName());
        deployRequest.setVersion(serviceTemplateDetailVo.getVersion());
        deployRequest.setCsp(serviceTemplateDetailVo.getCsp());
        deployRequest.setCategory(serviceTemplateDetailVo.getCategory());
        deployRequest.setFlavor(serviceTemplateDetailVo.getFlavors().get(0).getName());
        deployRequest.setRegion(serviceTemplateDetailVo.getRegions().get(0).toString());
        deployRequest.setServiceHostingType(serviceTemplateDetailVo.getServiceHostingType());
        Map<String, Object> serviceRequestProperties = new HashMap<>();
        serviceRequestProperties.put("admin_passwd", "111111111@Qq");
        deployRequest.setServiceRequestProperties(serviceRequestProperties);
        String requestBody = objectMapper.writeValueAsString(deployRequest);

        // Run the test
        final MockHttpServletResponse deployResponse =
                mockMvc.perform(post("/xpanse/services")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                        .andReturn().getResponse();
        UUID taskId = objectMapper.readValue(deployResponse.getContentAsString(), UUID.class);

        // Verify the results
        Assertions.assertEquals(HttpStatus.ACCEPTED.value(), deployResponse.getStatus());
        Assertions.assertNotNull(taskId);
        return taskId;

    }

    void testListDeployedServices() throws Exception {
        // Run the test
        List<DeployedService> result = listDeployedServices();

        // Verify the results
        Assertions.assertTrue(result.size() >= 1);
        Assertions.assertEquals(result.get(0).getServiceDeploymentState(),
                ServiceDeploymentState.DEPLOY_SUCCESS);
    }

    void testDestroy(UUID taskId) throws Exception {
        // SetUp
        String successMsg = String.format(
                "Task for destroying managed service %s has started.", taskId);
        Response response = Response.successResponse(Collections.singletonList(successMsg));

        String result = objectMapper.writeValueAsString(response);

        // Run the test
        final MockHttpServletResponse destroyResponse =
                mockMvc.perform(delete("/xpanse/services/{id}", taskId))
                        .andReturn().getResponse();


        // Verify the results
        Assertions.assertEquals(HttpStatus.ACCEPTED.value(), destroyResponse.getStatus());
        Assertions.assertEquals(result, destroyResponse.getContentAsString());
    }


    @Test
    @WithMockJwtAuth(authorities = RoleConstants.ROLE_ADMIN,
            claims = @OpenIdClaims(sub = "adminId", preferredUsername = "adminName"))
    void testServiceDeployerThrowsException() throws Exception {
        testDeployThrowsException();
        testGetServiceDetailsThrowsException();
        testDestroyThrowsException();
        testPurgeThrowsException();
    }

    void testPurgeThrowsException() throws Exception {
        UUID serviceId = UUID.randomUUID();
        // SetUp
        String refuseMsg = String.format(
                "Service with id %s not found.", serviceId);
        Response response = Response.errorResponse(ResultType.SERVICE_DEPLOYMENT_NOT_FOUND,
                Collections.singletonList(refuseMsg));
        String result = objectMapper.writeValueAsString(response);
        // Run the test
        final MockHttpServletResponse purgeResponse =
                mockMvc.perform(delete("/xpanse/services/purge/{id}", serviceId))
                        .andReturn().getResponse();
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), purgeResponse.getStatus());
        Assertions.assertEquals(result, purgeResponse.getContentAsString());
    }

    void testPurge(UUID taskId) throws Exception {
        // SetUp
        String successMsg = String.format(
                "Purging task for service with ID %s has started.", taskId);
        Response response = Response.successResponse(Collections.singletonList(successMsg));
        String result = objectMapper.writeValueAsString(response);
        // Run the test
        final MockHttpServletResponse purgeResponse =
                mockMvc.perform(delete("/xpanse/services/purge/{id}", taskId))
                        .andReturn().getResponse();
        Assertions.assertEquals(HttpStatus.ACCEPTED.value(), purgeResponse.getStatus());
        Assertions.assertEquals(result, purgeResponse.getContentAsString());

        Thread.sleep(waitTime);


        // SetUp
        String refuseMsg = String.format(
                "Service with id %s not found.", taskId);
        Response detailsErrorResponse =
                Response.errorResponse(ResultType.SERVICE_DEPLOYMENT_NOT_FOUND,
                        Collections.singletonList(refuseMsg));
        String detailsResult = objectMapper.writeValueAsString(detailsErrorResponse);
        final MockHttpServletResponse detailsResponse =
                mockMvc.perform(get("/xpanse/services/details/self_hosted/{id}", taskId))
                        .andReturn().getResponse();

        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), detailsResponse.getStatus());
        Assertions.assertEquals(detailsResult, detailsResponse.getContentAsString());
    }

    List<DeployedService> listDeployedServices() throws Exception {

        final MockHttpServletResponse listResponse =
                mockMvc.perform(get("/xpanse/services")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();
        return objectMapper.readValue(listResponse.getContentAsString(),
                new TypeReference<>() {
                });
    }

    DeployedServiceDetails getDeployedServiceDetails(UUID serviceId) throws Exception {
        final MockHttpServletResponse detailResponse =
                mockMvc.perform(get("/xpanse/services/details/self_hosted/{id}", serviceId))
                        .andReturn().getResponse();
        try {
            return objectMapper.readValue(detailResponse.getContentAsString(),
                    DeployedServiceDetails.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    void testDeployThrowsException() throws Exception {
        Response expectedResponse = Response.errorResponse(
                ResultType.SERVICE_TEMPLATE_NOT_REGISTERED,
                Collections.singletonList("Service template not found."));
        String result = objectMapper.writeValueAsString(expectedResponse);

        DeployRequest deployRequest = new DeployRequest();

        deployRequest.setServiceName("redis");
        deployRequest.setVersion("v1.0.0");
        deployRequest.setCsp(Csp.HUAWEI);
        deployRequest.setCategory(Category.AI);
        deployRequest.setFlavor("flavor2");
        deployRequest.setRegion("region");
        deployRequest.setServiceHostingType(ServiceHostingType.SELF);
        deployRequest.setOcl(ocl);
        String requestBody = objectMapper.writeValueAsString(deployRequest);

        // Run the test
        final MockHttpServletResponse deployResponse =
                mockMvc.perform(post("/xpanse/services")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                        .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), deployResponse.getStatus());
        Assertions.assertEquals(result, deployResponse.getContentAsString());

    }


    void testGetServiceDetailsThrowsException() throws Exception {
        String uuid = UUID.randomUUID().toString();
        Response expectedResponse = Response.errorResponse(
                ResultType.SERVICE_DEPLOYMENT_NOT_FOUND,
                Collections.singletonList(
                        String.format("Service with id %s not found.", uuid)));
        String result = objectMapper.writeValueAsString(expectedResponse);

        // Run the test
        final MockHttpServletResponse detailResponse =
                mockMvc.perform(get("/xpanse/services/details/self_hosted/{id}", uuid))
                        .andReturn().getResponse();


        // Verify the results
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), detailResponse.getStatus());
        Assertions.assertEquals(result, detailResponse.getContentAsString());


    }


    void testDestroyThrowsException() throws Exception {
        UUID uuid = UUID.randomUUID();
        Response expectedResponse = Response.errorResponse(
                ResultType.SERVICE_DEPLOYMENT_NOT_FOUND,
                Collections.singletonList(String.format("Service with id %s not found.", uuid)));
        String result = objectMapper.writeValueAsString(expectedResponse);

        // Run the test
        final MockHttpServletResponse destroyResponse =
                mockMvc.perform(delete("/xpanse/services/{id}", uuid)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), destroyResponse.getStatus());
        Assertions.assertEquals(result, destroyResponse.getContentAsString());

    }

}
