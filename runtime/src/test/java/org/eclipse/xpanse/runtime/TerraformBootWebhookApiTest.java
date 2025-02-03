/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.deployment.PolicyValidator;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformResult;
import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.response.ErrorType;
import org.eclipse.xpanse.modules.models.service.deployment.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deployment.ModifyRequest;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.eclipse.xpanse.runtime.util.ApisTestCommon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.bind.annotation.CrossOrigin;

/** test for WebhookApi. */
@Slf4j
@ExtendWith(SpringExtension.class)
@CrossOrigin
@SpringBootTest(
        properties = {
            "spring.profiles.active=oauth,zitadel,zitadel-testbed,terraform-boot,test,dev"
        })
@AutoConfigureMockMvc
public class TerraformBootWebhookApiTest extends ApisTestCommon {

    @MockitoBean private PolicyValidator mockPolicyValidator;

    void mockDeploymentWitPolicies() {
        doNothing().when(mockPolicyValidator).validateDeploymentWithPolicies(any());
    }

    @Test
    @WithJwt(file = "jwt_all_roles-no-policies.json")
    void testTerraformBootWebhookApis() throws Exception {
        testTerraformBootWebhookApisThrowsException();
        testTerraformBootWebhookApisWell();
    }

    void testTerraformBootWebhookApisThrowsException() throws Exception {
        // Setup
        UUID uuid = UUID.randomUUID();
        ErrorType expectedErrorType = ErrorType.SERVICE_ORDER_NOT_FOUND;
        String errorMsg = String.format("Service order with id %s not found.", uuid);
        List<String> expectedDetails = Collections.singletonList(errorMsg);
        TerraformResult deployResult = getTerraformResultByFile("deploy_success_callback.json");
        deployResult.setRequestId(uuid);
        // Run the test
        final MockHttpServletResponse deployCallbackResponse = orderCallback(uuid, deployResult);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), deployCallbackResponse.getStatus());
        ErrorResponse deployCallbackResult =
                objectMapper.readValue(
                        deployCallbackResponse.getContentAsString(), ErrorResponse.class);
        assertEquals(expectedErrorType, deployCallbackResult.getErrorType());
        assertEquals(deployCallbackResult.getDetails(), expectedDetails);
    }

    void testTerraformBootWebhookApisWell() throws Exception {
        addCredentialForHuaweiCloud();
        // Setup
        mockDeploymentWitPolicies();
        addCredentialForHuaweiCloud();
        Ocl ocl =
                new OclLoader()
                        .getOcl(
                                URI.create("file:src/test/resources/ocl_terraform_test.yml")
                                        .toURL());
        ocl.setName("TerraformBootWebhookApiTest-1");
        testCallbackApiWithOcl(ocl);

        Ocl oclFromGit =
                new OclLoader()
                        .getOcl(
                                URI.create(
                                                "file:src/test/resources/ocl_terraform_from_git_test.yml")
                                        .toURL());
        oclFromGit.setName("TerraformBootWebhookApiTest-2");
        testCallbackApiWithOcl(oclFromGit);
    }

    void testCallbackApiWithOcl(Ocl ocl) throws Exception {
        // Setup
        ServiceTemplateDetailVo serviceTemplate =
                registerServiceTemplateAndApproveRegistration(ocl);
        if (Objects.isNull(serviceTemplate)) {
            log.error("Failed to register service template.");
            return;
        }

        // deploy a service
        DeployRequest deployRequest = getDeployRequest(serviceTemplate);
        final MockHttpServletResponse deployResponse =
                mockMvc.perform(
                                post("/xpanse/services")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(deployRequest)))
                        .andReturn()
                        .getResponse();
        assertThat(deployResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        ServiceOrder serviceOrder =
                objectMapper.readValue(deployResponse.getContentAsString(), ServiceOrder.class);
        UUID serviceId = serviceOrder.getServiceId();
        UUID orderId = serviceOrder.getOrderId();
        assertThat(serviceId).isNotNull();
        assertThat(orderId).isNotNull();
        // callback with deploy result
        TerraformResult deployResult = getTerraformResultByFile("deploy_success_callback.json");
        deployResult.setRequestId(orderId);
        // Run the test
        final MockHttpServletResponse deployCallbackResponse = orderCallback(orderId, deployResult);
        // Verify the results
        assertThat(deployCallbackResponse.getStatus()).isEqualTo(HttpStatus.OK.value());

        // Modify the service
        ModifyRequest modifyRequest = new ModifyRequest();
        BeanUtils.copyProperties(deployRequest, modifyRequest);
        modifyRequest.getServiceRequestProperties().put("admin_passwd", "2222222222@Qq");
        final MockHttpServletResponse modifyResponse =
                mockMvc.perform(
                                put("/xpanse/services/modify/{serviceId}", serviceId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(modifyRequest)))
                        .andReturn()
                        .getResponse();
        assertThat(modifyResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        ServiceOrder serviceModifyOrder =
                objectMapper.readValue(modifyResponse.getContentAsString(), ServiceOrder.class);
        UUID modifyOrderId = serviceModifyOrder.getOrderId();
        UUID modifyServiceId = serviceModifyOrder.getServiceId();
        assertThat(modifyOrderId).isNotNull();
        assertThat(modifyServiceId).isEqualTo(serviceId);
        deployResult.setRequestId(modifyOrderId);
        // Run the test
        final MockHttpServletResponse modifyCallbackResponse =
                orderCallback(modifyOrderId, deployResult);
        // Verify the results
        assertThat(modifyCallbackResponse.getStatus()).isEqualTo(HttpStatus.OK.value());

        // destroy the service
        final MockHttpServletResponse destroyResponse =
                mockMvc.perform(
                                delete("/xpanse/services/{id}", serviceId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();
        assertThat(destroyResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        ServiceOrder serviceDestroyOrder =
                objectMapper.readValue(destroyResponse.getContentAsString(), ServiceOrder.class);
        UUID destroyServiceId = serviceDestroyOrder.getServiceId();
        UUID destroyOrderId = serviceDestroyOrder.getOrderId();
        assertThat(destroyServiceId).isEqualTo(serviceId);
        assertThat(destroyOrderId).isNotNull();
        // callback with destroy result
        TerraformResult destroyResult = getTerraformResultByFile("destroy_success_callback.json");
        destroyResult.setRequestId(destroyOrderId);
        // Run the test
        final MockHttpServletResponse destroyCallBackResponse =
                orderCallback(destroyOrderId, destroyResult);
        // Verify the results
        assertThat(destroyCallBackResponse.getStatus()).isEqualTo(HttpStatus.OK.value());

        // Run the test
        final MockHttpServletResponse rollbackCallBackResponse =
                orderCallback(orderId, destroyResult);
        // Verify the results
        assertThat(rollbackCallBackResponse.getStatus()).isEqualTo(HttpStatus.OK.value());

        final MockHttpServletResponse purgeResponse =
                mockMvc.perform(
                                delete("/xpanse/services/{id}", serviceId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();
        assertThat(purgeResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        ServiceOrder servicePurgeOrder =
                objectMapper.readValue(purgeResponse.getContentAsString(), ServiceOrder.class);
        UUID purgeServiceId = servicePurgeOrder.getServiceId();
        UUID purgeOrderId = servicePurgeOrder.getOrderId();
        assertThat(purgeServiceId).isEqualTo(serviceId);
        assertThat(purgeOrderId).isNotNull();
        destroyResult.setRequestId(purgeOrderId);

        // Run the test
        final MockHttpServletResponse purgeCallBackResponse =
                orderCallback(purgeOrderId, destroyResult);
        // Verify the results
        assertThat(purgeCallBackResponse.getStatus()).isEqualTo(HttpStatus.OK.value());

        deleteServiceDeployment(serviceId);
        deleteServiceTemplate(serviceTemplate.getServiceTemplateId());
    }

    TerraformResult getTerraformResultByFile(String resourceFileName) throws Exception {
        ClassPathResource resource = new ClassPathResource(resourceFileName);
        // Read the JSON content
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(resource.getInputStream(), TerraformResult.class);
    }

    MockHttpServletResponse orderCallback(UUID orderId, TerraformResult deployResult)
            throws Exception {
        return mockMvc.perform(
                        post("/webhook/terraform-boot/order/{orderId}", orderId)
                                .content(objectMapper.writeValueAsString(deployResult))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
    }
}
