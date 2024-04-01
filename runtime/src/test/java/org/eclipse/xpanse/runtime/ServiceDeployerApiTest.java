/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.fasterxml.jackson.core.type.TypeReference;
import com.huaweicloud.sdk.ecs.v2.model.NovaAvailabilityZone;
import com.huaweicloud.sdk.ecs.v2.model.NovaListAvailabilityZonesResponse;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
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
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.eclipse.xpanse.modules.policy.policyman.generated.api.PoliciesEvaluationApi;
import org.eclipse.xpanse.modules.policy.policyman.generated.api.PoliciesValidateApi;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.EvalCmdList;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.EvalResult;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.ValidatePolicyList;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.ValidateResponse;
import org.eclipse.xpanse.runtime.util.ApisTestCommon;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openstack4j.api.OSClient;
import org.openstack4j.openstack.OSFactory;
import org.openstack4j.openstack.networking.domain.NeutronAvailabilityZone;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Test for ServiceDeployerApi.
 */
@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=oauth,zitadel,zitadel-testbed",
        "OS_AUTH_URL=http://127.0.0.1/v3/identity"})
@AutoConfigureMockMvc
class ServiceDeployerApiTest extends ApisTestCommon {
    private static final long waitTime = 60 * 1000;

    @MockBean
    private PoliciesValidateApi mockPoliciesValidateApi;
    @MockBean
    private PoliciesEvaluationApi mockPoliciesEvaluationApi;

    @BeforeEach
    void setUp() {
        mockOsFactory = mockStatic(OSFactory.class);
    }

    @AfterEach
    void tearDown() {
        mockOsFactory.close();
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
        mockMvc.perform(post("/xpanse/service/policies").content(
                                objectMapper.writeValueAsString(servicePolicy))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        ServicePolicyCreateRequest serviceFlavorPolicy = new ServicePolicyCreateRequest();
        serviceFlavorPolicy.setServiceTemplateId(serviceTemplate.getId());
        List<String> flavors = serviceTemplate.getFlavors().stream().map(Flavor::getName).toList();
        serviceFlavorPolicy.setFlavorNameList(flavors);
        serviceFlavorPolicy.setPolicy("serviceFlavorPolicy");
        serviceFlavorPolicy.setEnabled(true);
        mockMvc.perform(post("/xpanse/service/policies").content(
                                objectMapper.writeValueAsString(serviceFlavorPolicy))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

    }

    UserPolicy addUserPolicy(UserPolicyCreateRequest userPolicy) throws Exception {
        userPolicy.setEnabled(true);
        final MockHttpServletResponse response = mockMvc.perform(
                        post("/xpanse/policies").content(objectMapper.writeValueAsString(userPolicy))
                                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        return objectMapper.readValue(response.getContentAsString(), UserPolicy.class);
    }

    void deleteUserPolicy(UUID id) throws Exception {
        mockMvc.perform(delete("/xpanse/policies/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
    }

    void mockPolicyEvaluationResult(boolean isSuccessful) {
        final EvalResult evalResult = new EvalResult();
        evalResult.setIsSuccessful(isSuccessful);
        evalResult.setPolicy("policy");
        // Configure PoliciesEvaluationApi.evaluatePoliciesPost(...).
        when(mockPoliciesEvaluationApi.evaluatePoliciesPost(any(EvalCmdList.class))).thenReturn(
                evalResult);
    }

    @Test
    @WithJwt(file = "jwt_user.json")
    void testGetAvailabilityZonesWell() throws Exception {
        testGetAvailabilityZonesForHuaweiCloud();
        testGetAvailabilityZonesForFlexibleEngine();
        testGetAvailabilityZonesForOpenstack();
        testGetAvailabilityZonesForScs();
    }

    void testGetAvailabilityZonesForHuaweiCloud() throws Exception {
        // Setup
        addCredentialForHuaweiCloud();
        mockSdkClientsForHuaweiCloud();

        NovaAvailabilityZone azA = new NovaAvailabilityZone().withZoneName("cn-southwest-2a");
        NovaAvailabilityZone azD = new NovaAvailabilityZone().withZoneName("cn-southwest-2d");
        NovaListAvailabilityZonesResponse response =
                new NovaListAvailabilityZonesResponse().withAvailabilityZoneInfo(List.of(azA, azD));
        response.setHttpStatusCode(200);
        when(mockEcsClient.novaListAvailabilityZones(any())).thenReturn(response);
        // Run the test
        final MockHttpServletResponse listAzResponse =
                getAvailabilityZones(Csp.HUAWEI, "cn-southwest-2");
        List<String> azs =
                objectMapper.readValue(listAzResponse.getContentAsString(), new TypeReference<>() {
                });
        Assertions.assertEquals(HttpStatus.OK.value(), listAzResponse.getStatus());
        Assertions.assertEquals(2, azs.size());
        Assertions.assertEquals("cn-southwest-2a", azs.getFirst());
        deleteCredential(Csp.HUAWEI, CredentialType.VARIABLES, "AK_SK");
    }

    void testGetAvailabilityZonesForFlexibleEngine() throws Exception {
        // Setup
        addCredentialForFlexibleEngine();
        mockSdkClientsForFlexibleEngine();

        NovaAvailabilityZone azA = new NovaAvailabilityZone().withZoneName("eu-west-0a");
        NovaAvailabilityZone azD = new NovaAvailabilityZone().withZoneName("eu-west-0b");
        NovaListAvailabilityZonesResponse response =
                new NovaListAvailabilityZonesResponse().withAvailabilityZoneInfo(List.of(azA, azD));
        response.setHttpStatusCode(200);
        when(mockEcsClient.novaListAvailabilityZones(any())).thenReturn(response);
        // Run the test
        final MockHttpServletResponse listAzResponse =
                getAvailabilityZones(Csp.FLEXIBLE_ENGINE, "eu-west-0");
        List<String> azs =
                objectMapper.readValue(listAzResponse.getContentAsString(), new TypeReference<>() {
                });
        Assertions.assertEquals(HttpStatus.OK.value(), listAzResponse.getStatus());
        Assertions.assertEquals(2, azs.size());
        Assertions.assertEquals("eu-west-0a", azs.getFirst());
        deleteCredential(Csp.FLEXIBLE_ENGINE, CredentialType.VARIABLES, "AK_SK");
    }

    void testGetAvailabilityZonesForOpenstack() throws Exception {
        // Setup
        addCredentialForOpenstack();
        OSClient.OSClientV3 mockOsClient = getMockOsClientWithMockServices();
        File azsjonFile = new File("src/test/resources/openstack/network/availability_zones.json");
        NeutronAvailabilityZone.AvailabilityZones azResponse =
                objectMapper.readValue(azsjonFile, NeutronAvailabilityZone.AvailabilityZones.class);
        when((List<NeutronAvailabilityZone>) mockOsClient.networking().availabilityzone()
                .list()).thenReturn(azResponse.getList());
        // Run the test
        final MockHttpServletResponse listAzResponse =
                getAvailabilityZones(Csp.OPENSTACK, "RegionOne");
        List<String> azNames =
                objectMapper.readValue(listAzResponse.getContentAsString(), new TypeReference<>() {
                });
        Assertions.assertEquals(HttpStatus.OK.value(), listAzResponse.getStatus());
        Assertions.assertEquals(1, azNames.size());
        Assertions.assertEquals("nova", azNames.getFirst());
        deleteCredential(Csp.OPENSTACK, CredentialType.VARIABLES, "USERNAME_PASSWORD");
    }

    void testGetAvailabilityZonesForScs() throws Exception {
        // Setup
        addCredentialForScs();
        OSClient.OSClientV3 mockOsClient = getMockOsClientWithMockServices();
        File azsjonFile = new File("src/test/resources/openstack/network/availability_zones.json");
        NeutronAvailabilityZone.AvailabilityZones azResponse =
                objectMapper.readValue(azsjonFile, NeutronAvailabilityZone.AvailabilityZones.class);
        when((List<NeutronAvailabilityZone>) mockOsClient.networking().availabilityzone()
                .list()).thenReturn(azResponse.getList());
        // Run the test
        final MockHttpServletResponse listAzResponse = getAvailabilityZones(Csp.SCS, "RegionOne");
        List<String> azNames =
                objectMapper.readValue(listAzResponse.getContentAsString(), new TypeReference<>() {
                });
        Assertions.assertEquals(HttpStatus.OK.value(), listAzResponse.getStatus());
        Assertions.assertEquals(1, azNames.size());
        Assertions.assertEquals("nova", azNames.getFirst());
        deleteCredential(Csp.SCS, CredentialType.VARIABLES, "USERNAME_PASSWORD");
    }

    @Test
    @WithJwt(file = "jwt_user.json")
    void testGetAvailabilityZonesReturnsEmptyList() throws Exception {
        // Run the test
        when(huaweiCloudClient.getEcsClient(any(), any())).thenReturn(mockEcsClient);
        NovaListAvailabilityZonesResponse response = new NovaListAvailabilityZonesResponse();
        response.setHttpStatusCode(500);
        when(mockEcsClient.novaListAvailabilityZones(any())).thenReturn(response);
        final MockHttpServletResponse listHuaweiResponse =
                getAvailabilityZones(Csp.HUAWEI, "cn-test");
        // Verify the results
        assertThat(listHuaweiResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(listHuaweiResponse.getContentAsString()).isEqualTo("[]");

        // Run the test
        when(flexibleEngineClient.getEcsClient(any(), any())).thenReturn(mockEcsClient);
        NovaListAvailabilityZonesResponse response1 = new NovaListAvailabilityZonesResponse();
        response1.setHttpStatusCode(500);
        when(mockEcsClient.novaListAvailabilityZones(any())).thenReturn(response1);
        final MockHttpServletResponse listFlexibleEngineResponse =
                getAvailabilityZones(Csp.HUAWEI, "eu-test");
        // Verify the results
        assertThat(listFlexibleEngineResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(listFlexibleEngineResponse.getContentAsString()).isEqualTo("[]");

        // Run the test
        final MockHttpServletResponse listOpenstackResponse =
                getAvailabilityZones(Csp.OPENSTACK, "RegionOne");
        // Verify the results
        assertThat(listOpenstackResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(listOpenstackResponse.getContentAsString()).isEqualTo("[]");

        // Run the test
        final MockHttpServletResponse listScsResponse = getAvailabilityZones(Csp.SCS, "RegionOne");
        // Verify the results
        assertThat(listScsResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(listScsResponse.getContentAsString()).isEqualTo("[]");
    }

    MockHttpServletResponse getAvailabilityZones(Csp csp, String regionName) throws Exception {
        return mockMvc.perform(get("/xpanse/csp/region/azs").param("cspName", csp.toValue())
                        .param("regionName", regionName).accept(MediaType.APPLICATION_JSON)).andReturn()
                .getResponse();
    }


    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testDeployApisWellWithDeployerTerraformLocal() throws Exception {
        // Setup
        Ocl ocl = new OclLoader().getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.setName("serviceDeployApiTest-1");
        testDeployerWithOclAndPolicy(ocl, "policy-1");


        Ocl oclFromGit = new OclLoader().getOcl(
                URI.create("file:src/test/resources/ocl_terraform_from_git_test.yml").toURL());
        oclFromGit.setName("serviceDeployApiTest-2");
        testDeployerWithOclAndPolicy(ocl, "policy-2");
    }

    void testDeployerWithOclAndPolicy(Ocl ocl, String policy) throws Exception {
        ServiceTemplateDetailVo serviceTemplate = registerServiceTemplate(ocl);
        if (Objects.isNull(serviceTemplate)) {
            log.error("Register service template failed.");
            return;
        }
        approveServiceTemplateRegistration(serviceTemplate.getId());
        setMockPoliciesValidateApi();
        UserPolicyCreateRequest userPolicyCreateRequest = new UserPolicyCreateRequest();
        userPolicyCreateRequest.setCsp(serviceTemplate.getCsp());
        userPolicyCreateRequest.setPolicy(policy);
        UserPolicy userPolicy = addUserPolicy(userPolicyCreateRequest);
        addServicePolicies(serviceTemplate);
        addCredentialForHuaweiCloud();
        mockPolicyEvaluationResult(true);

        UUID serviceId = deployService(serviceTemplate);
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
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.setName("serviceDeployApiTest-3");
        ocl.getDeployment().setKind(DeployerKind.OPEN_TOFU);
        testDeployerWithOclAndPolicy(ocl, "policy-3");

        Ocl oclFromGit = new OclLoader().getOcl(
                URI.create("file:src/test/resources/ocl_terraform_from_git_test.yml").toURL());
        oclFromGit.setName("serviceDeployApiTest-4");
        oclFromGit.getDeployment().setKind(DeployerKind.OPEN_TOFU);
        testDeployerWithOclAndPolicy(oclFromGit, "policy-4");
    }

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testDeployApisThrowsException() throws Exception {
        Ocl ocl = new OclLoader().getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.setName("serviceDeployApiTest-5");
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
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.setName("serviceDeployApiTest-6");
        ocl.getDeployment().setKind(DeployerKind.OPEN_TOFU);
        ServiceTemplateDetailVo serviceTemplate = registerServiceTemplate(ocl);
        testDeployThrowsPolicyEvaluationFailedException(serviceTemplate);
    }

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testDeployApiFailedWithDeployerTerraformLocal() throws Exception {
        Ocl ocl = new OclLoader().getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.setName("serviceDeployApiTest-7");
        ServiceTemplateDetailVo serviceTemplate = registerServiceTemplate(ocl);
        testDeployThrowsPolicyEvaluationFailedException(serviceTemplate);
    }

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testDeployApiFailedWithVariableInvalidException() throws Exception {
        // Setup
        Ocl ocl = new OclLoader().getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.setName("serviceDeployApiTest-8");
        ocl.getDeployment().getVariables().getLast().setMandatory(true);
        AvailabilityZoneConfig zoneConfig = new AvailabilityZoneConfig();
        zoneConfig.setDisplayName("Primary AZ");
        zoneConfig.setVarName("primary_az");
        zoneConfig.setMandatory(true);
        AvailabilityZoneConfig zoneConfig2 = new AvailabilityZoneConfig();
        zoneConfig2.setDisplayName("Secondary AZ");
        zoneConfig2.setVarName("secondary_az");
        zoneConfig2.setMandatory(true);
        List<AvailabilityZoneConfig> zoneConfigs = List.of(zoneConfig, zoneConfig2);
        ocl.getDeployment().setServiceAvailability(zoneConfigs);
        ServiceTemplateDetailVo serviceTemplate = registerServiceTemplate(ocl);
        approveServiceTemplateRegistration(serviceTemplate.getId());
        // Run the test
        DeployRequest deployRequest1 = getDeployRequest(serviceTemplate);
        deployRequest1.getServiceRequestProperties().clear();
        // SetUp
        String refuseMsg1 =
                String.format("Variable validation failed:" + " [required property '%s' not found]",
                        ocl.getDeployment().getVariables().getLast().getName());
        Response response1 = Response.errorResponse(ResultType.VARIABLE_VALIDATION_FAILED,
                Collections.singletonList(refuseMsg1));
        String result1 = objectMapper.writeValueAsString(response1);

        final MockHttpServletResponse deployResponse1 = mockMvc.perform(
                        post("/xpanse/services").contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(deployRequest1))).andReturn()
                .getResponse();

        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), deployResponse1.getStatus());
        assertEquals(result1, deployResponse1.getContentAsString());


        // Setup
        DeployRequest deployRequest2 = getDeployRequest(serviceTemplate);
        deployRequest2.getAvailabilityZones().clear();
        List<String> requiredZoneVarNames =
                zoneConfigs.stream().filter(AvailabilityZoneConfig::getMandatory)
                        .map(AvailabilityZoneConfig::getVarName).toList();
        List<String> errorMessages = new ArrayList<>();
        requiredZoneVarNames.forEach(varName -> errorMessages.add(
                String.format("required availability zone property '%s' not found", varName)));
        String refuseMsg2 =
                String.format("Variable validation failed: %s", StringUtils.join(errorMessages));
        Response response2 =
                Response.errorResponse(ResultType.VARIABLE_VALIDATION_FAILED, List.of(refuseMsg2));
        String result2 = objectMapper.writeValueAsString(response2);

        final MockHttpServletResponse deployResponse2 = mockMvc.perform(
                        post("/xpanse/services").contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(deployRequest2))).andReturn()
                .getResponse();

        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), deployResponse2.getStatus());
        assertEquals(result2, deployResponse2.getContentAsString());
        unregisterServiceTemplate(serviceTemplate.getId());
    }

    void testListDeployedServices() throws Exception {
        // Run the test
        List<DeployedService> result = listDeployedServices();
        List<DeployedServiceDetails> detailsResult = listDeployedServicesDetails();

        // Verify the results
        Assertions.assertFalse(result.isEmpty());
        assertEquals(result.getFirst().getServiceDeploymentState(),
                ServiceDeploymentState.DEPLOY_SUCCESS);

        Assertions.assertFalse(detailsResult.isEmpty());
        assertEquals(detailsResult.getFirst().getServiceDeploymentState(),
                ServiceDeploymentState.DEPLOY_SUCCESS);
    }

    void testDestroy(UUID taskId) throws Exception {
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

    void testDeployThrowsServiceTemplateNotApproved(ServiceTemplateDetailVo serviceTemplate)
            throws Exception {
        // SetUp
        Response expectedResponse = Response.errorResponse(ResultType.SERVICE_TEMPLATE_NOT_APPROVED,
                Collections.singletonList("No available service templates found."));
        String result = objectMapper.writeValueAsString(expectedResponse);

        DeployRequest deployRequest = getDeployRequest(serviceTemplate);
        // Run the test
        final MockHttpServletResponse deployResponse = mockMvc.perform(
                        post("/xpanse/services").contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(deployRequest))).andReturn()
                .getResponse();

        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), deployResponse.getStatus());
        assertEquals(result, deployResponse.getContentAsString());
        unregisterServiceTemplate(serviceTemplate.getId());
    }

    void testDeployThrowsPolicyEvaluationFailedException(ServiceTemplateDetailVo serviceTemplate)
            throws Exception {
        approveServiceTemplateRegistration(serviceTemplate.getId());
        setMockPoliciesValidateApi();
        UserPolicyCreateRequest userPolicyCreateRequest = new UserPolicyCreateRequest();
        userPolicyCreateRequest.setCsp(serviceTemplate.getCsp());
        userPolicyCreateRequest.setPolicy("userPolicy-3");
        UserPolicy userPolicy = addUserPolicy(userPolicyCreateRequest);
        addServicePolicies(serviceTemplate);
        addCredentialForHuaweiCloud();
        mockPolicyEvaluationResult(false);
        deployService(serviceTemplate);
        deleteUserPolicy(userPolicy.getId());
        unregisterServiceTemplate(serviceTemplate.getId());
    }

    void testPurgeThrowsException() throws Exception {
        UUID serviceId = UUID.randomUUID();
        // SetUp
        String refuseMsg = String.format("Service with id %s not found.", serviceId);
        Response response = Response.errorResponse(ResultType.SERVICE_DEPLOYMENT_NOT_FOUND,
                Collections.singletonList(refuseMsg));
        String result = objectMapper.writeValueAsString(response);
        // Run the test
        final MockHttpServletResponse purgeResponse =
                mockMvc.perform(delete("/xpanse/services/purge/{id}", serviceId)).andReturn()
                        .getResponse();
        assertEquals(HttpStatus.BAD_REQUEST.value(), purgeResponse.getStatus());
        assertEquals(result, purgeResponse.getContentAsString());
    }

    void testPurge(UUID taskId) throws Exception {
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
                mockMvc.perform(get("/xpanse/services/details/self_hosted/{id}", taskId))
                        .andReturn().getResponse();

        assertEquals(HttpStatus.BAD_REQUEST.value(), detailsResponse.getStatus());
        assertEquals(detailsResult, detailsResponse.getContentAsString());
    }


    List<DeployedService> listDeployedServices() throws Exception {

        final MockHttpServletResponse listResponse = mockMvc.perform(
                        get("/xpanse/services").contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))

                .andReturn().getResponse();
        return objectMapper.readValue(listResponse.getContentAsString(), new TypeReference<>() {
        });
    }

    List<DeployedServiceDetails> listDeployedServicesDetails() throws Exception {

        final MockHttpServletResponse listResponse = mockMvc.perform(
                        get("/xpanse/services/details").contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        return objectMapper.readValue(listResponse.getContentAsString(),
                new TypeReference<>() {
                });
    }

    void testDeployThrowsServiceTemplateNotRegistered() throws Exception {
        Response expectedResponse =
                Response.errorResponse(ResultType.SERVICE_TEMPLATE_NOT_REGISTERED,
                        Collections.singletonList("No available service templates found."));
        String result = objectMapper.writeValueAsString(expectedResponse);

        DeployRequest deployRequest = new DeployRequest();

        deployRequest.setServiceName("redis");
        deployRequest.setVersion("1.0.0");
        deployRequest.setCsp(Csp.HUAWEI);
        deployRequest.setCategory(Category.AI);
        deployRequest.setFlavor("flavor2");
        Region region = new Region();
        region.setName("regionName");
        region.setArea("areaName");
        deployRequest.setRegion(region);
        deployRequest.setServiceHostingType(ServiceHostingType.SELF);
        String requestBody = objectMapper.writeValueAsString(deployRequest);

        // Run the test
        final MockHttpServletResponse deployResponse = mockMvc.perform(
                        post("/xpanse/services").contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON).content(requestBody)).andReturn()
                .getResponse();

        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), deployResponse.getStatus());
        assertEquals(result, deployResponse.getContentAsString());

    }


    void testGetServiceDetailsThrowsException() throws Exception {
        String uuid = UUID.randomUUID().toString();
        Response expectedResponse = Response.errorResponse(ResultType.SERVICE_DEPLOYMENT_NOT_FOUND,
                Collections.singletonList(String.format("Service with id %s not found.", uuid)));
        String result = objectMapper.writeValueAsString(expectedResponse);

        // Run the test
        final MockHttpServletResponse detailResponse =
                mockMvc.perform(get("/xpanse/services/details/self_hosted/{id}", uuid)).andReturn()
                        .getResponse();


        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), detailResponse.getStatus());
        assertEquals(result, detailResponse.getContentAsString());


    }


    void testDestroyThrowsException() throws Exception {
        UUID uuid = UUID.randomUUID();
        Response expectedResponse = Response.errorResponse(ResultType.SERVICE_DEPLOYMENT_NOT_FOUND,
                Collections.singletonList(String.format("Service with id %s not found.", uuid)));
        String result = objectMapper.writeValueAsString(expectedResponse);

        // Run the test
        final MockHttpServletResponse destroyResponse = mockMvc.perform(
                delete("/xpanse/services/{id}", uuid).contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();

        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), destroyResponse.getStatus());
        assertEquals(result, destroyResponse.getContentAsString());

    }
}
