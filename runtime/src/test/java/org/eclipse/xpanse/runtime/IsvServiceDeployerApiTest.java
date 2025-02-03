/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime;

import static org.eclipse.xpanse.modules.logging.LoggingKeyConstant.HEADER_TRACKING_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.CreateCredential;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.response.ErrorType;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
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

/** Test for ServiceDeployerApi. */
@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=oauth,zitadel,zitadel-testbed,test,dev"})
@AutoConfigureMockMvc
class IsvServiceDeployerApiTest extends ApisTestCommon {
    void addIsvCredential() throws Exception {
        final CreateCredential createCredential = new CreateCredential();
        createCredential.setCsp(Csp.HUAWEI_CLOUD);
        createCredential.setSite("Chinese Mainland");
        createCredential.setType(CredentialType.VARIABLES);
        createCredential.setName("AK_SK");
        createCredential.setDescription("description");
        List<CredentialVariable> credentialVariables = new ArrayList<>();
        credentialVariables.add(
                new CredentialVariable(
                        HuaweiCloudMonitorConstants.HW_ACCESS_KEY,
                        "The access key.",
                        true,
                        false,
                        "AK_VALUE"));
        credentialVariables.add(
                new CredentialVariable(
                        HuaweiCloudMonitorConstants.HW_SECRET_KEY,
                        "The security key.",
                        true,
                        false,
                        "SK_VALUE"));
        createCredential.setVariables(credentialVariables);
        createCredential.setTimeToLive(30000);
        mockMvc.perform(
                        post("/xpanse/isv/credentials")
                                .content(objectMapper.writeValueAsString(createCredential))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
    }

    void deleteIsvCredential() throws Exception {
        mockMvc.perform(
                        delete("/xpanse/isv/credentials")
                                .param("cspName", Csp.HUAWEI_CLOUD.toValue())
                                .param("siteName", "Chinese Mainland")
                                .param("type", CredentialType.VARIABLES.toValue())
                                .param("name", "AK_SK")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
    }

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testIsvServiceDeployApis() throws Exception {
        // Setup
        Ocl ocl =
                new OclLoader()
                        .getOcl(
                                URI.create("file:src/test/resources/ocl_terraform_test.yml")
                                        .toURL());
        ocl.setServiceHostingType(ServiceHostingType.SERVICE_VENDOR);
        ocl.setServiceVersion("1.0.1");
        ServiceTemplateDetailVo serviceTemplate =
                registerServiceTemplateAndApproveRegistration(ocl);
        addIsvCredential();

        ServiceOrder serviceOrder = deployService(serviceTemplate);
        UUID serviceId = serviceOrder.getServiceId();
        DeployedServiceDetails deployedServiceDetails = getDeployedServiceDetailsForIsv(serviceId);
        assertEquals(serviceId, deployedServiceDetails.getServiceId());
        assertEquals(
                ServiceDeploymentState.DEPLOYING,
                deployedServiceDetails.getServiceDeploymentState());
        if (waitServiceDeploymentIsCompleted(serviceId)) {
            testListDeployedServicesOfIsv();
            destroyService(serviceId);
            purgeService(serviceId);
        }
        deleteServiceTemplate(serviceTemplate.getServiceTemplateId());
        deleteIsvCredential();
    }

    void testListDeployedServicesOfIsv() throws Exception {
        // Run the test
        List<DeployedService> result = listDeployedServicesForIsv();

        List<VendorHostedDeployedServiceDetails> detailsResult =
                listDeployedServicesDetailsForIsv();

        // Verify the results
        Assertions.assertFalse(result.isEmpty());
        assertEquals(
                ServiceDeploymentState.DEPLOY_SUCCESS,
                result.getFirst().getServiceDeploymentState());

        Assertions.assertFalse(detailsResult.isEmpty());
        assertEquals(
                ServiceDeploymentState.DEPLOY_SUCCESS,
                detailsResult.getFirst().getServiceDeploymentState());
    }

    void destroyService(UUID serviceId) throws Exception {
        // SetUp
        // Run the test
        final MockHttpServletResponse destroyResponse =
                mockMvc.perform(delete("/xpanse/services/{serviceId}", serviceId))
                        .andReturn()
                        .getResponse();

        // Verify the results
        assertEquals(HttpStatus.ACCEPTED.value(), destroyResponse.getStatus());
        ServiceOrder serviceOrder =
                objectMapper.readValue(destroyResponse.getContentAsString(), ServiceOrder.class);
        assertNotNull(serviceOrder.getOrderId());
        assertTrue(waitServiceOrderIsCompleted(serviceOrder.getOrderId()));
    }

    void purgeService(UUID serviceId) throws Exception {
        // Run the test
        final MockHttpServletResponse purgeResponse =
                mockMvc.perform(delete("/xpanse/services/purge/{serviceId}", serviceId))
                        .andReturn()
                        .getResponse();
        assertEquals(HttpStatus.ACCEPTED.value(), purgeResponse.getStatus());

        // SetUp
        String refuseMsg = String.format("Service with id %s not found.", serviceId);
        final MockHttpServletResponse detailsResponse =
                mockMvc.perform(get("/xpanse/services/details/vendor_hosted/{id}", serviceId))
                        .andReturn()
                        .getResponse();

        assertEquals(HttpStatus.BAD_REQUEST.value(), detailsResponse.getStatus());
        assertNotNull(detailsResponse.getHeader(HEADER_TRACKING_ID));
        ErrorResponse errorResponse =
                objectMapper.readValue(detailsResponse.getContentAsString(), ErrorResponse.class);

        assertEquals(ErrorType.SERVICE_DEPLOYMENT_NOT_FOUND, errorResponse.getErrorType());
        assertEquals(List.of(refuseMsg), errorResponse.getDetails());
    }

    List<DeployedService> listDeployedServicesForIsv() throws Exception {

        final MockHttpServletResponse listResponse =
                mockMvc.perform(
                                get("/xpanse/services/isv")
                                        .param(
                                                "serviceState",
                                                ServiceDeploymentState.DEPLOY_SUCCESS.toValue())
                                        .param("serviceVersion", "1.0.1")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();
        return objectMapper.readValue(listResponse.getContentAsString(), new TypeReference<>() {});
    }

    List<VendorHostedDeployedServiceDetails> listDeployedServicesDetailsForIsv() throws Exception {

        final MockHttpServletResponse listResponse =
                mockMvc.perform(
                                get("/xpanse/services/details")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .param(
                                                "serviceState",
                                                ServiceDeploymentState.DEPLOY_SUCCESS.toValue())
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();
        return objectMapper.readValue(listResponse.getContentAsString(), new TypeReference<>() {});
    }

    DeployedServiceDetails getDeployedServiceDetailsForIsv(UUID serviceId) throws Exception {
        final MockHttpServletResponse detailResponse =
                mockMvc.perform(get("/xpanse/services/isv/details/vendor_hosted/{id}", serviceId))
                        .andReturn()
                        .getResponse();
        try {
            return objectMapper.readValue(
                    detailResponse.getContentAsString(), DeployedServiceDetails.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
