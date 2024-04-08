/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.CreateCredential;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.view.DeployedService;
import org.eclipse.xpanse.modules.models.service.view.DeployedServiceDetails;
import org.eclipse.xpanse.modules.models.service.view.VendorHostedDeployedServiceDetails;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.eclipse.xpanse.plugins.huaweicloud.monitor.constant.HuaweiCloudMonitorConstants;
import org.eclipse.xpanse.runtime.util.ApisTestCommon;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Test for ServiceDeployerApi.
 */
@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=oauth,zitadel,zitadel-testbed"})
@AutoConfigureMockMvc
class IsvServiceDeployerApiTest extends ApisTestCommon {
    private static final long waitTime = 60 * 1000;

    void addIsvCredential() throws Exception {
        final CreateCredential createCredential = new CreateCredential();
        createCredential.setCsp(Csp.HUAWEI);
        createCredential.setType(CredentialType.VARIABLES);
        createCredential.setName("AK_SK");
        createCredential.setDescription("description");
        List<CredentialVariable> credentialVariables = new ArrayList<>();
        credentialVariables.add(
                new CredentialVariable(HuaweiCloudMonitorConstants.HW_ACCESS_KEY, "The access key.",
                        true, false, "AK_VALUE"));
        credentialVariables.add(new CredentialVariable(HuaweiCloudMonitorConstants.HW_SECRET_KEY,
                "The security key.", true, false, "SK_VALUE"));
        createCredential.setVariables(credentialVariables);
        createCredential.setTimeToLive(30000);
        mockMvc.perform(post("/xpanse/isv/credentials").content(
                                objectMapper.writeValueAsString(createCredential))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }

    void deleteIsvCredential() throws Exception {
        mockMvc.perform(delete("/xpanse/isv/credentials").param("cspName", Csp.HUAWEI.toValue())
                .param("type", CredentialType.VARIABLES.toValue()).param("name", "AK_SK")
                .accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
    }

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testIsvServiceDeployApis() throws Exception {
        // Setup

        Ocl ocl = new OclLoader().getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.setName("IsvServiceDeployApiTest-1");
        ocl.setServiceHostingType(ServiceHostingType.SERVICE_VENDOR);
        ServiceTemplateDetailVo serviceTemplate = registerServiceTemplate(ocl);
        approveServiceTemplateRegistration(serviceTemplate.getId());
        addIsvCredential();

        UUID serviceId = deployService(serviceTemplate);
        DeployedServiceDetails deployedServiceDetails = getDeployedServiceDetailsForIsv(serviceId);
        assertEquals(serviceId, deployedServiceDetails.getId());
        assertEquals(ServiceDeploymentState.DEPLOYING,
                deployedServiceDetails.getServiceDeploymentState());
        if (waitUntilExceptedStateForIsv(serviceId, ServiceDeploymentState.DEPLOY_SUCCESS)) {
            testListDeployedServicesOfIsv();
        }
        if (waitUntilExceptedStateForIsv(serviceId, ServiceDeploymentState.DEPLOY_SUCCESS)) {
            destroyService(serviceId);
        }
        if (waitUntilExceptedStateForIsv(serviceId, ServiceDeploymentState.DESTROY_SUCCESS)) {
            purgeService(serviceId);
        }
        unregisterServiceTemplate(serviceTemplate.getId());
        deleteIsvCredential();
    }

    boolean waitUntilExceptedStateForIsv(UUID id, ServiceDeploymentState targetState)
            throws Exception {
        boolean isDone = false;
        long startTime = System.currentTimeMillis();
        while (!isDone) {
            DeployedService deployedService = getDeployedServiceDetailsForIsv(id);
            if (Objects.nonNull(deployedService)
                    && deployedService.getServiceDeploymentState() == targetState) {
                isDone = true;
            } else {
                if (System.currentTimeMillis() - startTime > 60 * 1000) {
                    break;
                }
                Thread.sleep(5 * 1000);
            }

        }
        return isDone;
    }

    void testListDeployedServicesOfIsv() throws Exception {
        // Run the test
        List<DeployedService> result = listDeployedServicesForIsv();

        List<VendorHostedDeployedServiceDetails> detailsResult =
                listDeployedServicesDetailsForIsv();

        // Verify the results
        Assertions.assertFalse(result.isEmpty());
        assertEquals(result.getFirst().getServiceDeploymentState(),
                ServiceDeploymentState.DEPLOY_SUCCESS);

        Assertions.assertFalse(detailsResult.isEmpty());
        assertEquals(detailsResult.getFirst().getServiceDeploymentState(),
                ServiceDeploymentState.DEPLOY_SUCCESS);
    }

    void destroyService(UUID taskId) throws Exception {
        // SetUp
        String successMsg =
                String.format("Task for destroying managed service %s has started.", taskId);
        Response response = Response.successResponse(Collections.singletonList(successMsg));

        String result = objectMapper.writeValueAsString(response);

        // Run the test
        final MockHttpServletResponse destroyResponse =
                mockMvc.perform(delete("/xpanse/services/{id}", taskId)).andReturn().getResponse();

        // Verify the results
        assertEquals(HttpStatus.ACCEPTED.value(), destroyResponse.getStatus());
        assertEquals(result, destroyResponse.getContentAsString());
    }

    void purgeService(UUID taskId) throws Exception {
        // SetUp
        String successMsg =
                String.format("Purging task for service with ID %s has started.", taskId);
        Response response = Response.successResponse(Collections.singletonList(successMsg));
        String result = objectMapper.writeValueAsString(response);
        // Run the test
        final MockHttpServletResponse purgeResponse =
                mockMvc.perform(delete("/xpanse/services/purge/{id}", taskId)).andReturn()
                        .getResponse();
        assertEquals(HttpStatus.ACCEPTED.value(), purgeResponse.getStatus());
        assertEquals(result, purgeResponse.getContentAsString());

        Thread.sleep(waitTime);
        // SetUp
        String refuseMsg = String.format("Service with id %s not found.", taskId);
        Response detailsErrorResponse =
                Response.errorResponse(ResultType.SERVICE_DEPLOYMENT_NOT_FOUND,
                        Collections.singletonList(refuseMsg));
        String detailsResult = objectMapper.writeValueAsString(detailsErrorResponse);
        final MockHttpServletResponse detailsResponse =
                mockMvc.perform(get("/xpanse/services/details/vendor_hosted/{id}", taskId))
                        .andReturn().getResponse();

        assertEquals(HttpStatus.BAD_REQUEST.value(), detailsResponse.getStatus());
        assertEquals(detailsResult, detailsResponse.getContentAsString());
    }


    List<DeployedService> listDeployedServicesForIsv() throws Exception {

        final MockHttpServletResponse listResponse = mockMvc.perform(
                        get("/xpanse/services/isv").param("serviceState",
                                        ServiceDeploymentState.DEPLOY_SUCCESS.toValue())
                                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        return objectMapper.readValue(listResponse.getContentAsString(), new TypeReference<>() {
        });
    }

    List<VendorHostedDeployedServiceDetails> listDeployedServicesDetailsForIsv() throws Exception {

        final MockHttpServletResponse listResponse = mockMvc.perform(
                get("/xpanse/services/details").contentType(MediaType.APPLICATION_JSON)
                        .param("serviceState", ServiceDeploymentState.DEPLOY_SUCCESS.toValue())
                        .accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        return objectMapper.readValue(listResponse.getContentAsString(), new TypeReference<>() {
        });
    }

    DeployedServiceDetails getDeployedServiceDetailsForIsv(UUID serviceId) throws Exception {
        final MockHttpServletResponse detailResponse =
                mockMvc.perform(get("/xpanse/services/isv/details/vendor_hosted/{id}", serviceId))
                        .andReturn().getResponse();
        try {
            return objectMapper.readValue(detailResponse.getContentAsString(),
                    DeployedServiceDetails.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }


}
