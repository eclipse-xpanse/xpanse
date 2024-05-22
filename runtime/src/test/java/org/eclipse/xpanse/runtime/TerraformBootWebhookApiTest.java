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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import java.net.URI;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.service.DatabaseDeployServiceStorage;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.api.TerraformFromGitRepoApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.api.TerraformFromScriptsApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformResult;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformValidationResult;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.modify.ModifyRequest;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.eclipse.xpanse.runtime.util.ApisTestCommon;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.bind.annotation.CrossOrigin;


/**
 * test for WebhookApi.
 */
@Slf4j
@ExtendWith(SpringExtension.class)
@CrossOrigin
@SpringBootTest(properties =
        {"spring.profiles.active=oauth,zitadel,zitadel-testbed,terraform-boot"})
@AutoConfigureMockMvc
public class TerraformBootWebhookApiTest extends ApisTestCommon {
    @Resource
    private DatabaseDeployServiceStorage deployServiceStorage;
    @MockBean
    private TerraformFromScriptsApi mockTerraformFromScriptsApi;
    @MockBean
    private TerraformFromGitRepoApi mockTerraformFromGitRepoApi;

    void mockTerraformBootServices() {
        TerraformValidationResult validationResult = new TerraformValidationResult();
        validationResult.setValid(true);
        when(mockTerraformFromScriptsApi.validateWithScripts(any()))
                .thenReturn(validationResult);
        doNothing().when(mockTerraformFromScriptsApi).asyncDeployWithScripts(any());

        when(mockTerraformFromGitRepoApi.validateScriptsFromGitRepo(any()))
                .thenReturn(validationResult);
        doNothing().when(mockTerraformFromGitRepoApi).asyncDeployFromGitRepo(any());
    }

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testTerraformBootWebhookApis() throws Exception {
        testTerraformBootWebhookApisThrowsException();
        testTerraformBootWebhookApisWell();
    }

    void testTerraformBootWebhookApisThrowsException() throws Exception {
        // Setup
        UUID uuid = UUID.randomUUID();
        TerraformResult deployResult = getTerraformResultByFile("deploy_success_callback.json");
        Response deployCallbackResult = Response.errorResponse(
                ResultType.SERVICE_DEPLOYMENT_NOT_FOUND,
                Collections.singletonList(String.format("Service with id %s not found.", uuid)));
        // Run the test
        final MockHttpServletResponse deployCallbackResponse = mockMvc.perform(
                        post("/webhook/terraform-boot/deploy/{serviceId}", uuid)
                                .content(objectMapper.writeValueAsString(deployResult))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), deployCallbackResponse.getStatus());
        assertEquals(objectMapper.writeValueAsString(deployCallbackResult),
                deployCallbackResponse.getContentAsString());

        // Run the test
        final MockHttpServletResponse modifyCallbackResponse = mockMvc.perform(
                        post("/webhook/terraform-boot/modify/{serviceId}", uuid)
                                .content(objectMapper.writeValueAsString(deployResult))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), modifyCallbackResponse.getStatus());
        assertEquals(objectMapper.writeValueAsString(deployCallbackResult),
                modifyCallbackResponse.getContentAsString());

        // Setup
        TerraformResult destroyResult = getTerraformResultByFile("destroy_success_callback.json");
        Response destroyCallbackResult = Response.errorResponse(
                ResultType.SERVICE_DEPLOYMENT_NOT_FOUND,
                Collections.singletonList(String.format("Service with id %s not found.", uuid)));
        // Run the test
        final MockHttpServletResponse destroyCallbackResponse = mockMvc.perform(
                        post("/webhook/terraform-boot/destroy/{serviceId}", uuid)
                                .content(objectMapper.writeValueAsString(destroyResult))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), destroyCallbackResponse.getStatus());
        assertEquals(objectMapper.writeValueAsString(destroyCallbackResult),
                destroyCallbackResponse.getContentAsString());

        // Run the test
        final MockHttpServletResponse rollbackCallbackResponse = mockMvc.perform(
                        post("/webhook/terraform-boot/rollback/{serviceId}", uuid)
                                .content(objectMapper.writeValueAsString(destroyResult))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), rollbackCallbackResponse.getStatus());
        assertEquals(objectMapper.writeValueAsString(destroyCallbackResult),
                rollbackCallbackResponse.getContentAsString());


        // Run the test
        final MockHttpServletResponse purgeCallbackResponse = mockMvc.perform(
                        post("/webhook/terraform-boot/purge/{serviceId}", uuid)
                                .content(objectMapper.writeValueAsString(destroyResult))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), purgeCallbackResponse.getStatus());
        assertEquals(objectMapper.writeValueAsString(destroyCallbackResult),
                purgeCallbackResponse.getContentAsString());
    }

    void testTerraformBootWebhookApisWell() throws Exception {
        // Setup
        mockTerraformBootServices();
        Ocl ocl = new OclLoader().getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.setName("TerraformBootWebhookApiTest-1");
        testCallbackApiWithOcl(ocl);

        Ocl oclFromGit = new OclLoader().getOcl(
                URI.create("file:src/test/resources/ocl_terraform_from_git_test.yml").toURL());
        oclFromGit.setName("TerraformBootWebhookApiTest-2");
        testCallbackApiWithOcl(oclFromGit);
    }

    void testCallbackApiWithOcl(Ocl ocl) throws Exception {
        // Setup
        ServiceTemplateDetailVo serviceTemplate = registerServiceTemplate(ocl);
        if (Objects.isNull(serviceTemplate)) {
            Assertions.fail("Failed to register service template.");
        }
        approveServiceTemplateRegistration(serviceTemplate.getId());

        // deploy a service
        DeployRequest deployRequest = getDeployRequest(serviceTemplate);
        final MockHttpServletResponse deployResponse =
                mockMvc.perform(post("/xpanse/services")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(deployRequest)))
                        .andReturn().getResponse();
        UUID serviceId = objectMapper.readValue(deployResponse.getContentAsString(), UUID.class);
        // callback with deploy result
        TerraformResult deployResult = getTerraformResultByFile("deploy_success_callback.json");
        // Run the test
        final MockHttpServletResponse deployCallbackResponse = mockMvc.perform(
                        post("/webhook/terraform-boot/deploy/{serviceId}",
                                serviceId)
                                .content(objectMapper.writeValueAsString(deployResult))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        // Verify the results
        assertThat(deployCallbackResponse.getStatus()).isEqualTo(HttpStatus.OK.value());

        // Modify the service
        ModifyRequest modifyRequest = new ModifyRequest();
        BeanUtils.copyProperties(deployRequest, modifyRequest);
        modifyRequest.getServiceRequestProperties().put("admin_passwd", "2222222222@Qq");

        final MockHttpServletResponse modifyResponse =
                mockMvc.perform(put("/xpanse/services/modify/{serviceId}", serviceId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(modifyRequest)))
                        .andReturn().getResponse();
        UUID modificationId = objectMapper.readValue(modifyResponse.getContentAsString(),
                UUID.class);
        assertThat(modificationId).isNotNull();
        assertThat(modificationId).isNotEqualTo(serviceId);
        deployResult.setRequestId(modificationId);
        // Run the test
        final MockHttpServletResponse modifyCallbackResponse = mockMvc.perform(
                        post("/webhook/terraform-boot/modify/{serviceId}",
                                serviceId)
                                .content(objectMapper.writeValueAsString(deployResult))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        // Verify the results
        assertThat(modifyCallbackResponse.getStatus()).isEqualTo(HttpStatus.OK.value());

        // destroy the service
        final MockHttpServletResponse destroyResponse =
                mockMvc.perform(delete("/xpanse/services/{id}", serviceId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();
        assertThat(destroyResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());

        // callback with destroy result
        TerraformResult destroyResult = getTerraformResultByFile("destroy_success_callback.json");
        // Run the test
        final MockHttpServletResponse destroyCallBackResponse = mockMvc.perform(
                        post("/webhook/terraform-boot/destroy/{serviceId}",
                                serviceId)
                                .content(objectMapper.writeValueAsString(destroyResult))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        // Verify the results
        assertThat(destroyCallBackResponse.getStatus()).isEqualTo(HttpStatus.OK.value());

        // Run the test
        final MockHttpServletResponse rollbackCallBackResponse = mockMvc.perform(
                        post("/webhook/terraform-boot/rollback/{serviceId}",
                                serviceId)
                                .content(objectMapper.writeValueAsString(destroyResult))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        // Verify the results
        assertThat(rollbackCallBackResponse.getStatus()).isEqualTo(HttpStatus.OK.value());

        // Run the test
        final MockHttpServletResponse purgeCallBackResponse = mockMvc.perform(
                        post("/webhook/terraform-boot/purge/{serviceId}",
                                serviceId)
                                .content(objectMapper.writeValueAsString(destroyResult))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        // Verify the results
        assertThat(purgeCallBackResponse.getStatus()).isEqualTo(HttpStatus.OK.value());

        unregisterServiceTemplate(serviceTemplate.getId());
        deployServiceStorage.deleteDeployService(
                deployServiceStorage.findDeployServiceById(serviceId));
    }

    TerraformResult getTerraformResultByFile(String resourceFileName) throws Exception {
        ClassPathResource resource = new ClassPathResource(resourceFileName);
        // Read the JSON content
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(resource.getInputStream(), TerraformResult.class);
    }
}
