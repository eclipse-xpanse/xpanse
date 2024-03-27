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

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import java.net.URI;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.service.DatabaseDeployServiceStorage;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.api.OpenTofuFromGitRepoApi;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.api.OpenTofuFromScriptsApi;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuResult;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuValidationResult;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.eclipse.xpanse.runtime.util.ApisTestCommon;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
@SpringBootTest(properties = {
        "spring.profiles.active=oauth,zitadel,zitadel-testbed,tofu-maker"})
@AutoConfigureMockMvc
public class OpenTofuMakerWebhookApiTest extends ApisTestCommon {
    @Resource
    private DatabaseDeployServiceStorage deployServiceStorage;
    @MockBean
    private OpenTofuFromScriptsApi mockOpenTofuFromScriptsApi;
    @MockBean
    private OpenTofuFromGitRepoApi mockOpenTofuFromGitRepoApi;

    void mockOpenTofuMakerServices() {
        OpenTofuValidationResult validationResult = new OpenTofuValidationResult();
        validationResult.setValid(true);
        when(mockOpenTofuFromScriptsApi.validateWithScripts(any(), any()))
                .thenReturn(validationResult);
        doNothing().when(mockOpenTofuFromScriptsApi).asyncDeployWithScripts(any(), any());

        when(mockOpenTofuFromGitRepoApi.validateScriptsFromGitRepo(any(), any()))
                .thenReturn(validationResult);
        doNothing().when(mockOpenTofuFromGitRepoApi).asyncDeployFromGitRepo(any(), any());
    }

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testOpenTofuBootWebhookApis() throws Exception {
        testOpenTofuBootWebhookApisThrowsException();
        testOpenTofuBootWebhookApisWell();
    }

    void testOpenTofuBootWebhookApisThrowsException() throws Exception {
        // Setup
        UUID uuid = UUID.randomUUID();
        OpenTofuResult deployResult = getOpenTofuResultByFile("deploy_success_callback.json");
        Response deployCallbackResult = Response.errorResponse(
                ResultType.SERVICE_DEPLOYMENT_NOT_FOUND,
                Collections.singletonList(String.format("Service with id %s not found.", uuid)));
        // Run the test
        final MockHttpServletResponse deployCallbackResponse = mockMvc.perform(
                        post("/webhook/tofu-maker/deploy/{task_id}", uuid)
                                .content(objectMapper.writeValueAsString(deployResult))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), deployCallbackResponse.getStatus());
        assertEquals(objectMapper.writeValueAsString(deployCallbackResult),
                deployCallbackResponse.getContentAsString());


        // Setup
        OpenTofuResult destroyResult = getOpenTofuResultByFile("destroy_success_callback.json");
        Response destroyCallbackResult = Response.errorResponse(
                ResultType.SERVICE_DEPLOYMENT_NOT_FOUND,
                Collections.singletonList(String.format("Service with id %s not found.", uuid)));
        // Run the test
        final MockHttpServletResponse destroyCallbackResponse = mockMvc.perform(
                        post("/webhook/tofu-maker/destroy/{task_id}", uuid)
                                .content(objectMapper.writeValueAsString(destroyResult))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), destroyCallbackResponse.getStatus());
        assertEquals(objectMapper.writeValueAsString(destroyCallbackResult),
                destroyCallbackResponse.getContentAsString());
    }

    void testOpenTofuBootWebhookApisWell() throws Exception {
        // Setup
        mockOpenTofuMakerServices();
        Ocl ocl = new OclLoader().getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.setName("OpenTofuMakerWebhookApiTest-1");
        ocl.getDeployment().setKind(DeployerKind.OPEN_TOFU);
        testCallbackApiWithOcl(ocl);

        Ocl oclFromGit = new OclLoader().getOcl(
                URI.create("file:src/test/resources/ocl_terraform_from_git_test.yml").toURL());
        oclFromGit.setName("OpenTofuMakerWebhookApiTest-2");
        oclFromGit.getDeployment().setKind(DeployerKind.OPEN_TOFU);
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
        UUID taskId = objectMapper.readValue(deployResponse.getContentAsString(), UUID.class);
        // callback with deploy result
        OpenTofuResult deployResult = getOpenTofuResultByFile("deploy_success_callback.json");
        // Run the test
        final MockHttpServletResponse deployCallbackResponse = mockMvc.perform(
                        post("/webhook/tofu-maker/deploy/{task_id}",
                                taskId)
                                .content(objectMapper.writeValueAsString(deployResult))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        // Verify the results
        assertThat(deployCallbackResponse.getStatus()).isEqualTo(HttpStatus.OK.value());

        // destroy the service
        final MockHttpServletResponse destroyResponse =
                mockMvc.perform(delete("/xpanse/services/{id}", taskId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();
        assertThat(destroyResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());

        // callback with destroy result
        OpenTofuResult destroyResult = getOpenTofuResultByFile("destroy_success_callback.json");
        // Run the test
        final MockHttpServletResponse destroyCallBackResponse = mockMvc.perform(
                        post("/webhook/tofu-maker/destroy/{task_id}",
                                taskId)
                                .content(objectMapper.writeValueAsString(destroyResult))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        // Verify the results
        assertThat(destroyCallBackResponse.getStatus()).isEqualTo(HttpStatus.OK.value());

        unregisterServiceTemplate(serviceTemplate.getId());
        deployServiceStorage.deleteDeployService(
                deployServiceStorage.findDeployServiceById(taskId));

    }

    OpenTofuResult getOpenTofuResultByFile(String resourceFileName) throws Exception {
        ClassPathResource resource = new ClassPathResource(resourceFileName);
        // Read the JSON content
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(resource.getInputStream(), OpenTofuResult.class);
    }
}
