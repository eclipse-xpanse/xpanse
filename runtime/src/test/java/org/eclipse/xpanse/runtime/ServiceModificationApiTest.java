/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.Resource;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.service.DatabaseDeployServiceStorage;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.service.config.ServiceLockConfig;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.eclipse.xpanse.modules.models.service.modify.ModifyRequest;
import org.eclipse.xpanse.modules.models.service.modify.ServiceModificationAuditDetails;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.eclipse.xpanse.runtime.util.ApisTestCommon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Test for ServiceDeployerApi.
 */
@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=oauth,zitadel,zitadel-testbed"})
@AutoConfigureMockMvc
class ServiceModificationApiTest extends ApisTestCommon {
    @Resource
    private DatabaseDeployServiceStorage deployServiceStorage;

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testServiceModificationApis() throws Exception {
        // Setup
        Ocl ocl = new OclLoader().getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.setName("serviceModificationApiTest-1");
        ServiceTemplateDetailVo serviceTemplate = registerServiceTemplate(ocl);
        if (Objects.isNull(serviceTemplate)) {
            log.error("Register service template failed.");
            return;
        }
        approveServiceTemplateRegistration(serviceTemplate.getServiceTemplateId());
        addCredentialForHuaweiCloud();
        UUID serviceId = deployService(serviceTemplate);

        testModificationApisWell(serviceId, serviceTemplate);

        testModificationApisThrowsException(serviceId);
    }

    void testModificationApisWell(UUID serviceId, ServiceTemplateDetailVo serviceTemplate)
            throws Exception {
        if (waitUntilExceptedState(serviceId, ServiceDeploymentState.DEPLOY_SUCCESS)) {
            UUID modifyModificationId = testModify(serviceId, serviceTemplate);
            boolean modifySuccess = waitUntilExceptedState(serviceId,
                    ServiceDeploymentState.MODIFICATION_SUCCESSFUL);
            MockHttpServletResponse getAuditDetailsResponse =
                    getAuditDetailsByModificationId(modifyModificationId);
            assertEquals(getAuditDetailsResponse.getStatus(), HttpStatus.OK.value());

            ServiceModificationAuditDetails auditDetails =
                    objectMapper.readValue(getAuditDetailsResponse.getContentAsString(),
                            ServiceModificationAuditDetails.class);
            assertNotNull(auditDetails);
            assertEquals(modifyModificationId, auditDetails.getServiceModificationRequestId());
            if (modifySuccess) {
                assertEquals(TaskStatus.SUCCESSFUL, auditDetails.getTaskStatus());
            } else {
                assertEquals(TaskStatus.FAILED, auditDetails.getTaskStatus());
            }
            List<ServiceModificationAuditDetails> auditDetailsList =
                    listServiceModificationAudits(serviceId, auditDetails.getTaskStatus());
            assertThat(auditDetailsList).isNotEmpty();
            assertEquals(auditDetailsList.size(), 1);
            assertEquals(auditDetailsList.getFirst(), auditDetails);

            MockHttpServletResponse deleteAuditResponse =
                    deleteAuditByModificationId(modifyModificationId);
            assertEquals(deleteAuditResponse.getStatus(), HttpStatus.NO_CONTENT.value());

            MockHttpServletResponse deleteAuditsResponse =
                    deleteAuditsByServiceId(serviceId);
            assertEquals(deleteAuditsResponse.getStatus(), HttpStatus.NO_CONTENT.value());
        }
    }

    void testModificationApisThrowsException(UUID serviceId) throws Exception {
        testModifyThrowsServiceNotDeployedException();
        testModifyThrowsServiceLockedException(serviceId);
        testModifyThrowsAccessDenied(serviceId);
        testModificationsServiceModificationAuditNotFound();
    }

    UUID testModify(UUID serviceId, ServiceTemplateDetailVo serviceTemplate) throws Exception {
        // SetUp
        ModifyRequest modifyRequest = new ModifyRequest();
        modifyRequest.setFlavor(
                serviceTemplate.getFlavors().getServiceFlavors().getLast().getName());
        Map<String, Object> serviceRequestProperties = new HashMap<>();
        serviceRequestProperties.put("admin_passwd", "2222222222@Qq");
        modifyRequest.setServiceRequestProperties(serviceRequestProperties);

        // Run the test
        final MockHttpServletResponse modifyResponse = mockMvc.perform(
                        put("/xpanse/services/modify/{serviceId}", serviceId)
                                .content(objectMapper.writeValueAsString(modifyRequest))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        UUID modificationId =
                objectMapper.readValue(modifyResponse.getContentAsString(), UUID.class);

        // Verify the results
        assertEquals(HttpStatus.ACCEPTED.value(), modifyResponse.getStatus());
        // Verify the results
        assertNotNull(modificationId);
        assertNotEquals(modificationId, serviceId);
        return modificationId;
    }

    void testModifyThrowsServiceNotDeployedException() throws Exception {
        // SetUp
        UUID uuid = UUID.randomUUID();
        Response expectedResponse = Response.errorResponse(ResultType.SERVICE_DEPLOYMENT_NOT_FOUND,
                Collections.singletonList(String.format("Service with id %s not found.", uuid)));
        String result = objectMapper.writeValueAsString(expectedResponse);
        ModifyRequest modifyRequest = new ModifyRequest();
        modifyRequest.setFlavor("flavor-error-test");
        modifyRequest.setServiceRequestProperties(new HashMap<>());
        // Run the test
        final MockHttpServletResponse modifyResponse = mockMvc.perform(
                        put("/xpanse/services/modify/{serviceId}", uuid)
                                .content(objectMapper.writeValueAsString(modifyRequest))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), modifyResponse.getStatus());
        assertEquals(result, modifyResponse.getContentAsString());
    }

    void testModifyThrowsAccessDenied(UUID serviceId) throws Exception {
        // SetUp
        DeployServiceEntity deployServiceEntity = deployServiceStorage.findDeployServiceById(
                serviceId);
        deployServiceEntity.setUserId("unknown");
        deployServiceStorage.storeAndFlush(deployServiceEntity);
        Response expectedResponse = Response.errorResponse(ResultType.ACCESS_DENIED,
                Collections.singletonList(
                        "No permissions to modify services belonging to other users."));
        String result = objectMapper.writeValueAsString(expectedResponse);
        ModifyRequest modifyRequest = new ModifyRequest();
        modifyRequest.setFlavor("flavor-error-test");
        modifyRequest.setServiceRequestProperties(new HashMap<>());
        // Run the test
        final MockHttpServletResponse modifyResponse = mockMvc.perform(
                        put("/xpanse/services/modify/{serviceId}", serviceId)
                                .content(objectMapper.writeValueAsString(modifyRequest))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify the results
        assertEquals(HttpStatus.FORBIDDEN.value(), modifyResponse.getStatus());
        assertEquals(result, modifyResponse.getContentAsString());
    }

    void testModifyThrowsServiceLockedException(UUID serviceId) throws Exception {
        // SetUp
        ServiceLockConfig serviceLockConfig = new ServiceLockConfig();
        serviceLockConfig.setDestroyLocked(true);
        serviceLockConfig.setModifyLocked(true);
        DeployServiceEntity deployServiceEntity = deployServiceStorage.findDeployServiceById(
                serviceId);
        deployServiceEntity.setLockConfig(serviceLockConfig);
        deployServiceStorage.storeAndFlush(deployServiceEntity);
        String message =
                String.format("Service with id %s is locked from modification.", serviceId);
        Response expectedResponse = Response.errorResponse(ResultType.SERVICE_LOCKED,
                Collections.singletonList(message));
        String result = objectMapper.writeValueAsString(expectedResponse);
        ModifyRequest modifyRequest = new ModifyRequest();
        modifyRequest.setFlavor("flavor-error-test");
        modifyRequest.setServiceRequestProperties(new HashMap<>());
        // Run the test
        final MockHttpServletResponse modifyResponse = mockMvc.perform(
                        put("/xpanse/services/modify/{serviceId}", serviceId)
                                .content(objectMapper.writeValueAsString(modifyRequest))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), modifyResponse.getStatus());
        assertEquals(result, modifyResponse.getContentAsString());
    }

    void testModificationsServiceModificationAuditNotFound() throws Exception {
        // SetUp
        UUID uuid = UUID.randomUUID();
        Response expectedResponse =
                Response.errorResponse(ResultType.SERVICE_MODIFICATION_AUDIT_NOT_FOUND,
                        Collections.singletonList(
                                String.format("Service modification audit with id %s not found.",
                                        uuid)));
        String result = objectMapper.writeValueAsString(expectedResponse);

        MockHttpServletResponse getAuditResponse = getAuditDetailsByModificationId(uuid);
        assertEquals(HttpStatus.BAD_REQUEST.value(), getAuditResponse.getStatus());
        assertEquals(result, getAuditResponse.getContentAsString());

        MockHttpServletResponse deleteAuditResponse = deleteAuditByModificationId(uuid);
        assertEquals(HttpStatus.BAD_REQUEST.value(), deleteAuditResponse.getStatus());
        assertEquals(result, deleteAuditResponse.getContentAsString());

    }


    List<ServiceModificationAuditDetails> listServiceModificationAudits(UUID serviceId,
                                                                        TaskStatus taskStatus)
            throws Exception {
        MockHttpServletRequestBuilder listRequestBuilder =
                get("/xpanse/services/{serviceId}/modifications", serviceId)
                        .accept(MediaType.APPLICATION_JSON);
        if (taskStatus != null) {
            listRequestBuilder.param("taskStatus", taskStatus.toValue());
        }
        MockHttpServletResponse listTasksResponse =
                mockMvc.perform(listRequestBuilder).andReturn().getResponse();
        assertThat(listTasksResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        return objectMapper.readValue(listTasksResponse.getContentAsString(),
                new TypeReference<>() {
                });
    }

    MockHttpServletResponse deleteAuditsByServiceId(UUID serviceId) throws Exception {
        return mockMvc.perform(
                delete("/xpanse/services/{serviceId}/modifications", serviceId).accept(
                        MediaType.APPLICATION_JSON)).andReturn().getResponse();
    }

    MockHttpServletResponse getAuditDetailsByModificationId(UUID modificationId) throws Exception {
        return mockMvc.perform(
                        get("/xpanse/services/modifications/{modificationId}", modificationId).accept(
                                MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }

    MockHttpServletResponse deleteAuditByModificationId(UUID modificationId)
            throws Exception {
        return mockMvc.perform(
                        delete("/xpanse/services/modifications/{modificationId}", modificationId).accept(
                                MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }

}
