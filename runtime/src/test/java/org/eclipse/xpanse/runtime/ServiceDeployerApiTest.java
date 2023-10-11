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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceRepository;
import org.eclipse.xpanse.modules.models.credential.CreateCredential;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.security.constant.RoleConstants;
import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.CreateRequest;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.view.ServiceDetailVo;
import org.eclipse.xpanse.modules.models.service.view.ServiceVo;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateVo;
import org.eclipse.xpanse.plugins.huaweicloud.monitor.constant.HuaweiCloudMonitorConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.BeanUtils;
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

    private static final String userId = "adminId";
    private static ServiceTemplateVo serviceTemplateVo;
    private static ServiceDetailVo serviceDetailVo;
    private static Ocl ocl;
    private static UUID taskId;
    private static ServiceDeploymentState state;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Resource
    private MockMvc mockMvc;

    @Resource
    private DeployServiceRepository deployServiceRepository;

    @BeforeEach
    void setUp() throws Exception {
        deleteDestroyedServiceRecord();
    }

    @BeforeAll
    static void configureObjectMapper() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new SimpleModule().addSerializer(OffsetDateTime.class,
                OffsetDateTimeSerializer.INSTANCE));
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);

    }

    @Test
    @WithMockJwtAuth(authorities = RoleConstants.ROLE_ADMIN,
            claims = @OpenIdClaims(sub = "adminId", preferredUsername = "adminName"))
    void testServiceDeployer() throws Exception {
        testDeploy();
        boolean deploySuccess = deploySuccess(taskId);

        testGetServiceDetails();
        testListDeployedServices();

        testDestroy(deploySuccess);

        boolean destroySuccess = destroySuccess(taskId);
        if (destroySuccess) {
            testListDeployedServicesReturnsEmptyList();
        } else {
            testGetServiceDetails();
        }

        deleteDestroyedServiceRecord();
    }

    @Test
    @WithMockJwtAuth(authorities = RoleConstants.ROLE_ADMIN,
            claims = @OpenIdClaims(sub = "adminId", preferredUsername = "adminName"))
    void testServiceDeployerThrowsException() throws Exception {
        testDeployThrowsException();
        testGetServiceDetailsThrowsException();
        testDestroyThrowsException();
    }

    @Test
    @WithMockJwtAuth(authorities = RoleConstants.ROLE_ADMIN,
            claims = @OpenIdClaims(sub = "adminId", preferredUsername = "adminName"))
    void testServicePurgeSuccess() throws Exception {
        testDeploy();
        boolean deploySuccess = deploySuccess(taskId);

        testPurgeSuccess();
        deleteDestroyedServiceRecord();
    }

    @Test
    @WithMockJwtAuth(authorities = RoleConstants.ROLE_ADMIN,
            claims = @OpenIdClaims(sub = "adminId", preferredUsername = "adminName"))
    void testServicePurgeRefuse() throws Exception {
        testDeploy();
        deploySuccess(taskId);

        testPurgeRefuse();
        deleteDestroyedServiceRecord();
    }

    void testPurgeRefuse() throws Exception {
        // SetUp
        String refuseMsg = String.format(
                "Service %s is not in the state allowed for purging.", taskId);
        Response response = Response.errorResponse(ResultType.SERVICE_STATE_INVALID,
                Collections.singletonList(refuseMsg));
        String result = objectMapper.writeValueAsString(response);
        DeployServiceEntity referenceById = deployServiceRepository.getReferenceById(taskId);
        referenceById.setServiceDeploymentState(ServiceDeploymentState.DEPLOY_SUCCESS);

        // Run the test
        final MockHttpServletResponse purgeResponse =
                mockMvc.perform(delete("/xpanse/services/purge/{id}", taskId))
                        .andReturn().getResponse();
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), purgeResponse.getStatus());
        Assertions.assertEquals(result, purgeResponse.getContentAsString());
    }

    void testPurgeSuccess() throws Exception {
        // SetUp
        String successMsg = String.format(
                "Purging task for service with ID %s has started.", taskId);
        Response response = Response.successResponse(Collections.singletonList(successMsg));
        String result = objectMapper.writeValueAsString(response);
        DeployServiceEntity referenceById = deployServiceRepository.getReferenceById(taskId);
        referenceById.setServiceDeploymentState(ServiceDeploymentState.MANUAL_CLEANUP_REQUIRED);

        // Run the test
        final MockHttpServletResponse purgeResponse =
                mockMvc.perform(delete("/xpanse/services/purge/{id}", taskId))
                        .andReturn().getResponse();
        Assertions.assertEquals(HttpStatus.ACCEPTED.value(), purgeResponse.getStatus());
        Assertions.assertEquals(result, purgeResponse.getContentAsString());
    }

    void registerServiceTemplate() throws Exception {
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
        Thread.sleep(1000);
    }

    void testListDeployedServices() throws Exception {
        // Set up
        ServiceVo serviceVo = new ServiceVo();
        BeanUtils.copyProperties(serviceDetailVo, serviceVo);
        String result = objectMapper.writeValueAsString(List.of(serviceVo));

        // Run the test
        final MockHttpServletResponse listResponse = mockMvc.perform(
                        get("/xpanse/services")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), listResponse.getStatus());
        Assertions.assertEquals(result, listResponse.getContentAsString());
    }

    void deleteDestroyedServiceRecord() throws Exception {
        taskId = null;
        serviceDetailVo = null;
        state = null;
        deployServiceRepository.deleteAll();
        deployServiceRepository.flush();
    }


    void testListDeployedServicesReturnsEmptyList() throws Exception {
        // Set up
        String result = "[]";

        // Run the test
        final MockHttpServletResponse listResponse =
                mockMvc.perform(get("/xpanse/services")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();
        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), listResponse.getStatus());
        Assertions.assertEquals(result, listResponse.getContentAsString());
    }

    void testDeploy() throws Exception {
        // Setup
        registerServiceTemplate();
        addCredential();

        CreateRequest createRequest = new CreateRequest();
        createRequest.setUserId(userId);
        createRequest.setServiceName(serviceTemplateVo.getName());
        createRequest.setVersion(serviceTemplateVo.getVersion());
        createRequest.setCsp(serviceTemplateVo.getCsp());
        createRequest.setCategory(serviceTemplateVo.getCategory());
        createRequest.setFlavor(serviceTemplateVo.getOcl().getFlavors().get(0).getName());
        createRequest.setRegion(
                serviceTemplateVo.getOcl().getCloudServiceProvider().getRegions().get(0)
                        .toString());
        Map<String, String> serviceRequestProperties = new HashMap<>();
        serviceRequestProperties.put("secgroup_name", "secgroup_name");
        createRequest.setServiceRequestProperties(serviceRequestProperties);
        String requestBody = objectMapper.writeValueAsString(createRequest);

        // Run the test
        final MockHttpServletResponse deployResponse =
                mockMvc.perform(post("/xpanse/services")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                        .andReturn().getResponse();
        taskId = objectMapper.readValue(deployResponse.getContentAsString(), UUID.class);

        // Verify the results
        Assertions.assertEquals(HttpStatus.ACCEPTED.value(), deployResponse.getStatus());
        Assertions.assertTrue(StringUtils.isNotBlank(taskId.toString()));

    }


    void testDeployThrowsException() throws Exception {
        Response expectedResponse = Response.errorResponse(
                ResultType.SERVICE_TEMPLATE_NOT_REGISTERED,
                Collections.singletonList("Service template not found."));
        String result = objectMapper.writeValueAsString(expectedResponse);

        CreateRequest createRequest = new CreateRequest();

        createRequest.setUserId(userId);
        createRequest.setServiceName("redis");
        createRequest.setVersion("v1.0.0");
        createRequest.setCsp(Csp.HUAWEI);
        createRequest.setCategory(Category.AI);
        createRequest.setFlavor("flavor2");
        createRequest.setRegion("region");
        String requestBody = objectMapper.writeValueAsString(createRequest);

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

    void testGetServiceDetails() throws Exception {

        // Run the test
        final MockHttpServletResponse detailResponse =
                mockMvc.perform(get("/xpanse/services/{id}", taskId))
                        .andReturn().getResponse();
        serviceDetailVo = objectMapper.readValue(detailResponse.getContentAsString(),
                ServiceDetailVo.class);

        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), detailResponse.getStatus());
        Assertions.assertEquals(ocl.getCategory(), serviceDetailVo.getCategory());
        Assertions.assertEquals(ocl.getCloudServiceProvider().getName(),
                serviceDetailVo.getCsp());
        Assertions.assertEquals(ocl.getName().toLowerCase(Locale.ROOT),
                serviceDetailVo.getName());
        Assertions.assertEquals(ocl.getServiceVersion(), serviceDetailVo.getVersion());
        Assertions.assertEquals(userId, serviceDetailVo.getUserId());

        if (ServiceDeploymentState.DEPLOY_SUCCESS == serviceDetailVo.getServiceDeploymentState()) {
            Map<String, String> properties = serviceDetailVo.getDeployedServiceProperties();
            Assertions.assertNotNull(properties);
            Assertions.assertFalse(serviceDetailVo.getDeployedServiceProperties().isEmpty());
            Assertions.assertTrue(properties.containsKey("secgroup_name"));
            Assertions.assertEquals("secgroup_name", properties.get("secgroup_name"));
        }
        if (ServiceDeploymentState.DEPLOY_FAILED == serviceDetailVo.getServiceDeploymentState()) {
            Assertions.assertNotNull(serviceDetailVo.getResultMessage());
        }
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
                mockMvc.perform(get("/xpanse/services/{id}", uuid))
                        .andReturn().getResponse();


        // Verify the results
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), detailResponse.getStatus());
        Assertions.assertEquals(result, detailResponse.getContentAsString());


    }


    void testDestroy(boolean deploySuccess) throws Exception {

        // SetUp
        String successMsg = String.format(
                "Task of stop managed service %s start running.", taskId);
        Response response = Response.successResponse(Collections.singletonList(successMsg));

        String result = objectMapper.writeValueAsString(response);

        // Run the test
        final MockHttpServletResponse destroyResponse =
                mockMvc.perform(delete("/xpanse/services/{id}", taskId))
                        .andReturn().getResponse();

        // Verify the results
        if (deploySuccess) {
            Assertions.assertEquals(HttpStatus.ACCEPTED.value(), destroyResponse.getStatus());
            Assertions.assertEquals(result, destroyResponse.getContentAsString());
        } else {
            String errorMsg = String.format("Service with id %s is %s.",
                    taskId, state);
            Response errorResponse = Response.errorResponse(ResultType.SERVICE_STATE_INVALID,
                    Collections.singletonList(errorMsg));
            String errorResult = objectMapper.writeValueAsString(errorResponse);
            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), destroyResponse.getStatus());
            Assertions.assertEquals(errorResult, destroyResponse.getContentAsString());
        }

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
        createCredential.setUserId(userId);
        mockMvc.perform(post("/xpanse/credentials")
                        .content(requestBody).contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }

    boolean deploySuccess(UUID id) throws Exception {
        long start = System.currentTimeMillis();
        boolean deploySuccess = false;
        while (!deploySuccess) {
            Thread.sleep(5000);
            if (System.currentTimeMillis() - start > 60000) {
                break;
            }
            final MockHttpServletResponse detailResponse =
                    mockMvc.perform(get("/xpanse/services/{id}", id))
                            .andReturn().getResponse();
            if (HttpStatus.OK.value() == detailResponse.getStatus()) {
                serviceDetailVo = objectMapper.readValue(detailResponse.getContentAsString(),
                        ServiceDetailVo.class);
                state = serviceDetailVo.getServiceDeploymentState();
                if (ServiceDeploymentState.DEPLOY_SUCCESS == state) {
                    deploySuccess = true;
                } else if (ServiceDeploymentState.DEPLOY_FAILED == state) {
                    return false;
                }
            }
        }
        return deploySuccess;

    }

    boolean destroySuccess(UUID id) throws Exception {
        long start = System.currentTimeMillis();
        boolean destroySuccess = false;
        while (!destroySuccess) {
            Thread.sleep(5000);
            if (System.currentTimeMillis() - start > 60000) {
                break;
            }
            final MockHttpServletResponse detailResponse =
                    mockMvc.perform(get("/xpanse/services/{id}", id))
                            .andReturn().getResponse();
            if (HttpStatus.OK.value() == detailResponse.getStatus()) {
                serviceDetailVo = objectMapper.readValue(detailResponse.getContentAsString(),
                        ServiceDetailVo.class);
                state = serviceDetailVo.getServiceDeploymentState();
                if (ServiceDeploymentState.DESTROY_SUCCESS == state) {
                    destroySuccess = true;
                } else if (ServiceDeploymentState.DESTROY_FAILED == state) {
                    return false;
                }
            }
        }
        return destroySuccess;

    }
}
