/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import jakarta.annotation.Resource;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.deployment.DeployService;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformResult;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.view.DeployedServiceDetails;
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
import org.springframework.web.bind.annotation.CrossOrigin;


/**
 * test for WebhookApi.
 */
@Slf4j
@ExtendWith(SpringExtension.class)
@CrossOrigin
@SpringBootTest(properties = {"spring.profiles.active=zitadel,zitadel-testbed,terraform-boot"})
@AutoConfigureMockMvc
public class TerraformBootWebhookApiTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static DeployedServiceDetails deployedServiceDetails;
    private static final String taskId = "bfdbc175-9f27-4679-8a4d-1f6b8c91386b";
    private static ServiceDeploymentState state;
    @Resource
    private DeployService deployService;

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
    @WithJwt(file = "jwt_admin.json")
    void testDeployCallbackSuccess() throws Exception {
        testDeployCallback();
        deployCallbackSuccess(UUID.fromString(taskId));
    }

    @Test
    @WithJwt(file = "jwt_admin.json")
    void testDeployCallbackSuccessThrowsException() throws Exception {
        deployCallbackThrowsException();
        testGetServiceDetailsThrowsException();
    }

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testDestroyCallbackSuccess() throws Exception {
        testDestroyCallback();
        destroyCallbackSuccess(UUID.fromString(taskId));
    }

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testDestroyCallbackSuccessThrowsException() throws Exception {
        destroyCallbackThrowsException();
        testGetServiceDetailsThrowsException();
    }

    void testDeployCallback() throws Exception {
        // Setup
        TerraformResult result = new TerraformResult();
        result.setCommandStdOutput("commandStdOutput");
        result.setCommandSuccessful(true);
        result.setTerraformState("terraformState");
        result.setCommandStdError("commandStdError");
        Map<String, String> importantFileContentMap = new HashMap<>();
        result.setImportantFileContentMap(importantFileContentMap);
        String requestBody = objectMapper.writeValueAsString(result);

        // Run the test
        final MockHttpServletResponse deployCallbackResponse =
                mockMvc.perform(post("/webhook/deploy/bfdbc175-9f27-4679-8a4d-1f6b8c91386b")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                                .content(requestBody))
                        .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), deployCallbackResponse.getStatus());
    }

    boolean deployCallbackSuccess(UUID id) throws Exception {
        long start = System.currentTimeMillis();
        boolean deployCallbackSuccess = false;
        while (!deployCallbackSuccess) {
            Thread.sleep(5000);
            if (System.currentTimeMillis() - start > 60000) {
                break;
            }
            final MockHttpServletResponse detailResponse =
                    mockMvc.perform(get("/xpanse/services/details/vendor_hosted/{id}", id))
                            .andReturn().getResponse();
            if (HttpStatus.OK.value() == detailResponse.getStatus()) {
                deployedServiceDetails = objectMapper.readValue(detailResponse.getContentAsString(),
                        DeployedServiceDetails.class);
                state = deployedServiceDetails.getServiceDeploymentState();
                if (ServiceDeploymentState.MANUAL_CLEANUP_REQUIRED == state) {
                    deployCallbackSuccess = true;
                } else if (ServiceDeploymentState.DEPLOY_FAILED == state) {
                    return false;
                }
            }
        }
        return deployCallbackSuccess;

    }

    void deployCallbackThrowsException() throws Exception {
        Response expectedResponse = Response.errorResponse(
                ResultType.SERVICE_TEMPLATE_NOT_REGISTERED,
                Collections.singletonList("Service template not found."));
        TerraformResult createRequest = new TerraformResult();
        createRequest.setCommandStdOutput("commandStdOutput");
        createRequest.setCommandSuccessful(true);
        createRequest.setTerraformState("terraformState");
        createRequest.setCommandStdError("commandStdError");
        Map<String, String> importantFileContentMap = new HashMap<>();
        createRequest.setImportantFileContentMap(importantFileContentMap);
        String requestBody = objectMapper.writeValueAsString(createRequest);

        // Run the test
        final MockHttpServletResponse deployCallbackResponse =
                mockMvc.perform(post("/webhook/deploy/bfdbc175-9f27-4679-8a4d-1f6b8c91386b")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                        .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), deployCallbackResponse.getStatus());
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

    void testDestroyCallback() throws Exception {
        // Setup
        TerraformResult result = new TerraformResult();
        result.setCommandStdOutput("commandStdOutput");
        result.setCommandSuccessful(true);
        result.setTerraformState("terraformState");
        result.setCommandStdError("commandStdError");
        Map<String, String> importantFileContentMap = new HashMap<>();
        result.setImportantFileContentMap(importantFileContentMap);
        String requestBody = objectMapper.writeValueAsString(result);

        // Run the test
        final MockHttpServletResponse destroyCallbackResponse =
                mockMvc.perform(post("/webhook/destroy/bfdbc175-9f27-4679-8a4d-1f6b8c91386b")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                        .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(),
                destroyCallbackResponse.getStatus());
    }

    boolean destroyCallbackSuccess(UUID id) throws Exception {
        long start = System.currentTimeMillis();
        boolean deployCallbackSuccess = false;
        while (!deployCallbackSuccess) {
            Thread.sleep(5000);
            if (System.currentTimeMillis() - start > 60000) {
                break;
            }
            final MockHttpServletResponse detailResponse =
                    mockMvc.perform(get("/xpanse/services/details/vendor_hosted/{id}", id))
                            .andReturn().getResponse();
            if (HttpStatus.OK.value() == detailResponse.getStatus()) {
                deployedServiceDetails = objectMapper.readValue(detailResponse.getContentAsString(),
                        DeployedServiceDetails.class);
                state = deployedServiceDetails.getServiceDeploymentState();
                if (ServiceDeploymentState.MANUAL_CLEANUP_REQUIRED == state) {
                    deployCallbackSuccess = true;
                } else if (ServiceDeploymentState.DEPLOY_FAILED == state) {
                    return false;
                }
            }
        }
        return deployCallbackSuccess;

    }

    void destroyCallbackThrowsException() throws Exception {
        Response expectedResponse = Response.errorResponse(
                ResultType.SERVICE_DEPLOYMENT_NOT_FOUND,
                Collections.singletonList("Service template not found."));
        TerraformResult createRequest = new TerraformResult();
        createRequest.setCommandStdOutput("commandStdOutput");
        createRequest.setCommandSuccessful(true);
        createRequest.setTerraformState("terraformState");
        createRequest.setCommandStdError("commandStdError");
        Map<String, String> importantFileContentMap = new HashMap<>();
        createRequest.setImportantFileContentMap(importantFileContentMap);
        String requestBody = objectMapper.writeValueAsString(createRequest);

        // Run the test
        final MockHttpServletResponse destroyCallbackResponse =
                mockMvc.perform(post("/webhook/destroy/bfdbc175-9f27-4679-8a4d-1f6b8c91386b")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                        .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), destroyCallbackResponse.getStatus());

    }
}
