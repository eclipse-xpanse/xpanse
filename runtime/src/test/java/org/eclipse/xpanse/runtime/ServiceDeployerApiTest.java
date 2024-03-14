/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
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
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.CreateCredential;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.policy.servicepolicy.ServicePolicyCreateRequest;
import org.eclipse.xpanse.modules.models.policy.userpolicy.UserPolicy;
import org.eclipse.xpanse.modules.models.policy.userpolicy.UserPolicyCreateRequest;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.view.DeployedService;
import org.eclipse.xpanse.modules.models.service.view.DeployedServiceDetails;
import org.eclipse.xpanse.modules.models.servicetemplate.AvailabilityZoneConfig;
import org.eclipse.xpanse.modules.models.servicetemplate.Flavor;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;
import org.eclipse.xpanse.modules.models.servicetemplate.ReviewRegistrationRequest;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceReviewResult;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.eclipse.xpanse.modules.policy.policyman.generated.api.PoliciesEvaluationApi;
import org.eclipse.xpanse.modules.policy.policyman.generated.api.PoliciesValidateApi;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.EvalCmdList;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.EvalResult;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.ValidatePolicyList;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.ValidateResponse;
import org.eclipse.xpanse.plugins.huaweicloud.monitor.constant.HuaweiCloudMonitorConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test for ServiceDeployerApi.
 */
@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=oauth,zitadel,zitadel-testbed"})
@AutoConfigureMockMvc
class ServiceDeployerApiTest {

    private static final String regionName = "us-east-1";
    private static final String areaName = "Asia China";
    private static final long waitTime = 60 * 1000;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Resource
    private MockMvc mockMvc;
    @MockBean
    private PoliciesValidateApi mockPoliciesValidateApi;
    @MockBean
    private PoliciesEvaluationApi mockPoliciesEvaluationApi;

    @BeforeAll
    static void configureObjectMapper() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new SimpleModule().addSerializer(OffsetDateTime.class,
                OffsetDateTimeSerializer.INSTANCE));
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);
    }

    ServiceTemplateDetailVo registerServiceTemplate(Ocl ocl) throws Exception {
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        String requestBody = yamlMapper.writeValueAsString(ocl);
        final MockHttpServletResponse registerResponse =
                mockMvc.perform(post("/xpanse/service_templates")
                                .content(requestBody).contentType("application/x-yaml")
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();
        if (registerResponse.getStatus() == HttpStatus.OK.value()) {
            return objectMapper.readValue(registerResponse.getContentAsString(),
                    ServiceTemplateDetailVo.class);
        } else {
            log.error("Register service template failed, response: {}",
                    registerResponse.getContentAsString());
            return null;
        }
    }

    void unregisterServiceTemplate(UUID id) throws Exception {
        mockMvc.perform(
                        delete("/xpanse/service_templates/{id}", id).accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }

    void approvedServiceTemplateRegistration(UUID id) throws Exception {
        ReviewRegistrationRequest request = new ReviewRegistrationRequest();
        request.setReviewResult(ServiceReviewResult.APPROVED);
        request.setReviewComment("Approved");
        String requestBody = objectMapper.writeValueAsString(request);
        mockMvc.perform(put("/xpanse/service_templates/review/{id}", id)
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }

    void setMockPoliciesValidateApi() {
        ValidateResponse validateResponse = new ValidateResponse();
        validateResponse.setIsSuccessful(true);
        when(mockPoliciesValidateApi.validatePoliciesPost(
                any(ValidatePolicyList.class))).thenReturn(validateResponse);
    }

    void addServicePolicies(ServiceTemplateDetailVo serviceTemplate) throws Exception {
        ServicePolicyCreateRequest servicePolicy = new ServicePolicyCreateRequest();
        servicePolicy.setServiceTemplateId(serviceTemplate.getId());
        servicePolicy.setPolicy("servicePolicy");
        servicePolicy.setEnabled(true);
        mockMvc.perform(post("/xpanse/service/policies")
                        .content(objectMapper.writeValueAsString(servicePolicy))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        ServicePolicyCreateRequest serviceFlavorPolicy = new ServicePolicyCreateRequest();
        serviceFlavorPolicy.setServiceTemplateId(serviceTemplate.getId());
        List<String> flavors =
                serviceTemplate.getFlavors().stream().map(Flavor::getName).toList();
        serviceFlavorPolicy.setFlavorNameList(flavors);
        serviceFlavorPolicy.setPolicy("serviceFlavorPolicy");
        serviceFlavorPolicy.setEnabled(true);
        mockMvc.perform(post("/xpanse/service/policies")
                        .content(objectMapper.writeValueAsString(serviceFlavorPolicy))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

    }

    UserPolicy addUserPolicy(UserPolicyCreateRequest userPolicy) throws Exception {
        userPolicy.setEnabled(true);
        final MockHttpServletResponse response = mockMvc.perform(post("/xpanse/policies")
                        .content(objectMapper.writeValueAsString(userPolicy))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        return objectMapper.readValue(response.getContentAsString(), UserPolicy.class);
    }

    void deleteUserPolicy(UUID id) throws Exception {
        mockMvc.perform(delete("/xpanse/policies/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
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
        createCredential.setTimeToLive(30000);
        mockMvc.perform(post("/xpanse/user/credentials")
                        .content(objectMapper.writeValueAsString(createCredential))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }

    void mockPolicyEvaluationResult(boolean isSuccessful) {
        final EvalResult evalResult = new EvalResult();
        evalResult.setIsSuccessful(isSuccessful);
        evalResult.setPolicy("policy");
        // Configure PoliciesEvaluationApi.evaluatePoliciesPost(...).
        when(mockPoliciesEvaluationApi.evaluatePoliciesPost(any(EvalCmdList.class)))
                .thenReturn(evalResult);
    }

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testDeployApisWellWithDeployerTerraformLocal() throws Exception {
        // Setup
        Ocl ocl = new OclLoader().getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.setName("serviceDeployApiTest-1");
        ServiceTemplateDetailVo serviceTemplate = registerServiceTemplate(ocl);
        approvedServiceTemplateRegistration(serviceTemplate.getId());
        setMockPoliciesValidateApi();
        UserPolicyCreateRequest userPolicyCreateRequest =  new UserPolicyCreateRequest();
        userPolicyCreateRequest.setCsp(serviceTemplate.getCsp());
        userPolicyCreateRequest.setPolicy("userPolicy-1");
        UserPolicy userPolicy = addUserPolicy(userPolicyCreateRequest);
        addServicePolicies(serviceTemplate);
        addCredential();
        mockPolicyEvaluationResult(true);

        UUID serviceId = testDeploy(serviceTemplate);
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
        unregisterServiceTemplate(serviceTemplate.getId());
        deleteUserPolicy(userPolicy.getId());
    }

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testDeployApisWellWithDeployerOpenTofuLocal() throws Exception {
        // Setup
        Ocl ocl = new OclLoader().getOcl(
                URI.create("file:src/test/resources/ocl_opentofu_test.yml").toURL());
        ocl.setName("serviceDeployApiTest-2");
        ServiceTemplateDetailVo serviceTemplate = registerServiceTemplate(ocl);
        approvedServiceTemplateRegistration(serviceTemplate.getId());
        setMockPoliciesValidateApi();
        UserPolicyCreateRequest userPolicyCreateRequest =  new UserPolicyCreateRequest();
        userPolicyCreateRequest.setCsp(serviceTemplate.getCsp());
        userPolicyCreateRequest.setPolicy("userPolicy-2");
        UserPolicy userPolicy = addUserPolicy(userPolicyCreateRequest);
        addServicePolicies(serviceTemplate);
        addCredential();
        mockPolicyEvaluationResult(true);
        UUID serviceId = testDeploy(serviceTemplate);
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
        unregisterServiceTemplate(serviceTemplate.getId());
        deleteUserPolicy(userPolicy.getId());
    }

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testDeployApisThrowsException() throws Exception {
        Ocl ocl = new OclLoader().getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.setName("serviceDeployApiTest-3");
        ServiceTemplateDetailVo serviceTemplate = registerServiceTemplate(ocl);
        testDeployThrowsServiceTemplateNotRegistered();
        testDeployThrowsServiceTemplateNotApproved(serviceTemplate);
        testGetServiceDetailsThrowsException();
        testDestroyThrowsException();
        testPurgeThrowsException();
        unregisterServiceTemplate(serviceTemplate.getId());
    }

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testDeployApiFailedWithDeployerOpenTofuLocal() throws Exception {
        Ocl ocl = new OclLoader().getOcl(
                URI.create("file:src/test/resources/ocl_opentofu_test.yml").toURL());
        ocl.setName("serviceDeployApiTest-4");
        ServiceTemplateDetailVo serviceTemplate = registerServiceTemplate(ocl);
        testDeployThrowsPolicyEvaluationFailedException(serviceTemplate);
    }

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testDeployApiFailedWithDeployerTerraformLocal() throws Exception {
        Ocl ocl = new OclLoader().getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.setName("serviceDeployApiTest-5");
        ServiceTemplateDetailVo serviceTemplate = registerServiceTemplate(ocl);
        testDeployThrowsPolicyEvaluationFailedException(serviceTemplate);
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
                Thread.sleep(5 * 1000);
            }

        }
        return isDone;
    }

    UUID testDeploy(ServiceTemplateDetailVo serviceTemplateDetailVo) throws Exception {
        DeployRequest deployRequest = getDeployRequest(serviceTemplateDetailVo);
        // Run the test
        final MockHttpServletResponse deployResponse =
                mockMvc.perform(post("/xpanse/services")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(deployRequest)))
                        .andReturn().getResponse();
        UUID taskId = objectMapper.readValue(deployResponse.getContentAsString(), UUID.class);

        // Verify the results
        assertEquals(HttpStatus.ACCEPTED.value(), deployResponse.getStatus());
        Assertions.assertNotNull(taskId);
        return taskId;

    }

    void testListDeployedServices() throws Exception {
        // Run the test
        List<DeployedService> result = listDeployedServices();

        // Verify the results
        Assertions.assertFalse(result.isEmpty());
        assertEquals(result.getFirst().getServiceDeploymentState(),
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
        assertEquals(HttpStatus.ACCEPTED.value(), destroyResponse.getStatus());
        assertEquals(result, destroyResponse.getContentAsString());
    }

    void testDeployThrowsServiceTemplateNotApproved(ServiceTemplateDetailVo serviceTemplate)
            throws Exception {
        // SetUp
        Response expectedResponse = Response.errorResponse(
                ResultType.SERVICE_TEMPLATE_NOT_APPROVED,
                Collections.singletonList("No available service templates found."));
        String result = objectMapper.writeValueAsString(expectedResponse);

        DeployRequest deployRequest = getDeployRequest(serviceTemplate);
        // Run the test
        final MockHttpServletResponse deployResponse =
                mockMvc.perform(post("/xpanse/services")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(deployRequest)))
                        .andReturn().getResponse();

        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), deployResponse.getStatus());
        assertEquals(result, deployResponse.getContentAsString());
        unregisterServiceTemplate(serviceTemplate.getId());
    }

    void testDeployThrowsPolicyEvaluationFailedException(ServiceTemplateDetailVo serviceTemplate)
            throws Exception {
        approvedServiceTemplateRegistration(serviceTemplate.getId());
        setMockPoliciesValidateApi();
        UserPolicyCreateRequest userPolicyCreateRequest =  new UserPolicyCreateRequest();
        userPolicyCreateRequest.setCsp(serviceTemplate.getCsp());
        userPolicyCreateRequest.setPolicy("userPolicy-3");
        UserPolicy userPolicy = addUserPolicy(userPolicyCreateRequest);
        addServicePolicies(serviceTemplate);
        addCredential();
        mockPolicyEvaluationResult(false);
        testDeploy(serviceTemplate);
        deleteUserPolicy(userPolicy.getId());
        unregisterServiceTemplate(serviceTemplate.getId());
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
        assertEquals(HttpStatus.BAD_REQUEST.value(), purgeResponse.getStatus());
        assertEquals(result, purgeResponse.getContentAsString());
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
        assertEquals(HttpStatus.ACCEPTED.value(), purgeResponse.getStatus());
        assertEquals(result, purgeResponse.getContentAsString());

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

        assertEquals(HttpStatus.BAD_REQUEST.value(), detailsResponse.getStatus());
        assertEquals(detailsResult, detailsResponse.getContentAsString());
    }


    List<DeployedService> listDeployedServices() throws Exception {

        final MockHttpServletResponse listResponse =
                mockMvc.perform(get("/xpanse/services")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .param("page", "0")
                        )

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

    void testDeployThrowsServiceTemplateNotRegistered() throws Exception {
        Response expectedResponse = Response.errorResponse(
                ResultType.SERVICE_TEMPLATE_NOT_REGISTERED,
                Collections.singletonList("No available service templates found."));
        String result = objectMapper.writeValueAsString(expectedResponse);

        DeployRequest deployRequest = new DeployRequest();

        deployRequest.setServiceName("redis");
        deployRequest.setVersion("v1.0.0");
        deployRequest.setCsp(Csp.HUAWEI);
        deployRequest.setCategory(Category.AI);
        deployRequest.setFlavor("flavor2");
        Region region = new Region();
        region.setName(regionName);
        region.setArea(areaName);
        deployRequest.setRegion(region);
        deployRequest.setServiceHostingType(ServiceHostingType.SELF);
        String requestBody = objectMapper.writeValueAsString(deployRequest);

        // Run the test
        final MockHttpServletResponse deployResponse =
                mockMvc.perform(post("/xpanse/services")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                        .andReturn().getResponse();

        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), deployResponse.getStatus());
        assertEquals(result, deployResponse.getContentAsString());

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
        assertEquals(HttpStatus.BAD_REQUEST.value(), detailResponse.getStatus());
        assertEquals(result, detailResponse.getContentAsString());


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
        assertEquals(HttpStatus.BAD_REQUEST.value(), destroyResponse.getStatus());
        assertEquals(result, destroyResponse.getContentAsString());

    }

    DeployRequest getDeployRequest(ServiceTemplateDetailVo serviceTemplate) {
        DeployRequest deployRequest = new DeployRequest();
        deployRequest.setServiceName(serviceTemplate.getName());
        deployRequest.setVersion(serviceTemplate.getVersion());
        deployRequest.setCsp(serviceTemplate.getCsp());
        deployRequest.setCategory(serviceTemplate.getCategory());
        deployRequest.setFlavor(serviceTemplate.getFlavors().getFirst().getName());
        deployRequest.setRegion(serviceTemplate.getRegions().getFirst());
        deployRequest.setServiceHostingType(serviceTemplate.getServiceHostingType());

        Map<String, Object> serviceRequestProperties = new HashMap<>();
        serviceRequestProperties.put("admin_passwd", "111111111@Qq");
        deployRequest.setServiceRequestProperties(serviceRequestProperties);

        List<AvailabilityZoneConfig> availabilityZoneConfigs =
                serviceTemplate.getDeployment().getServiceAvailability();
        Map<String, String> availabilityZones = new HashMap<>();
        availabilityZoneConfigs.forEach(availabilityZoneConfig -> {
            availabilityZones.put(availabilityZoneConfig.getVarName(),
                    availabilityZoneConfig.getDisplayName());
        });
        deployRequest.setAvailabilityZones(availabilityZones);
        return deployRequest;
    }

}
