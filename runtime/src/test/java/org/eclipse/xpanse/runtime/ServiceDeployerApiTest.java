/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.huaweicloud.sdk.core.invoker.SyncInvoker;
import com.huaweicloud.sdk.ecs.v2.model.NovaAvailabilityZone;
import com.huaweicloud.sdk.ecs.v2.model.NovaListAvailabilityZonesRequest;
import com.huaweicloud.sdk.ecs.v2.model.NovaListAvailabilityZonesResponse;
import jakarta.annotation.Resource;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.service.DatabaseDeployServiceStorage;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.DatabaseServiceTemplateStorage;
import org.eclipse.xpanse.modules.models.billing.enums.BillingMode;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.policy.servicepolicy.ServicePolicyCreateRequest;
import org.eclipse.xpanse.modules.models.policy.userpolicy.UserPolicy;
import org.eclipse.xpanse.modules.models.policy.userpolicy.UserPolicyCreateRequest;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.service.config.ServiceLockConfig;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.modify.ModifyRequest;
import org.eclipse.xpanse.modules.models.service.view.DeployedService;
import org.eclipse.xpanse.modules.models.service.view.DeployedServiceDetails;
import org.eclipse.xpanse.modules.models.servicetemplate.AvailabilityZoneConfig;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceFlavor;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceFlavorWithPrice;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openstack4j.api.OSClient;
import org.openstack4j.openstack.OSFactory;
import org.openstack4j.openstack.networking.domain.NeutronAvailabilityZone;
import org.springframework.beans.BeanUtils;
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
@SuppressWarnings("unchecked")
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
        "spring.profiles.active=oauth,zitadel,zitadel-testbed",
        "http.request.retry.max.attempts=2",
        "http.request.retry.delay.milliseconds=1000",
        "OS_AUTH_URL=http://127.0.0.1/v3/identity"
})
@AutoConfigureMockMvc
class ServiceDeployerApiTest extends ApisTestCommon {
    private static final long waitTime = 60 * 1000;
    @Resource
    private DatabaseDeployServiceStorage deployServiceStorage;
    @Resource
    private DatabaseServiceTemplateStorage serviceTemplateStorage;
    @MockBean
    private PoliciesValidateApi mockPoliciesValidateApi;
    @MockBean
    private PoliciesEvaluationApi mockPoliciesEvaluationApi;

    @Test
    @WithJwt(file = "jwt_user.json")
    void testGetAzsApi() throws Exception {
        mockOsFactory = mockStatic(OSFactory.class);
        testGetAvailabilityZonesApiThrowsException();
        testGetAvailabilityZonesApiWell();
        mockOsFactory.close();
    }

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testDeployApis() throws Exception {
        testDeployApisThrowExceptions();
        testDeployApisWithDeployerTerraformLocal();
        testDeployApisWithDeployerOpenTofuLocal();
    }

    void setMockPoliciesValidateApi() {
        ValidateResponse validateResponse = new ValidateResponse();
        validateResponse.setIsSuccessful(true);
        when(mockPoliciesValidateApi.validatePoliciesPost(
                any(ValidatePolicyList.class))).thenReturn(validateResponse);
    }

    void addServicePolicies(ServiceTemplateDetailVo serviceTemplate) throws Exception {
        ServicePolicyCreateRequest servicePolicy = new ServicePolicyCreateRequest();
        servicePolicy.setServiceTemplateId(serviceTemplate.getServiceTemplateId());
        servicePolicy.setPolicy("servicePolicy");
        servicePolicy.setEnabled(true);
        addServiceFlavorPolicies(servicePolicy);

        ServicePolicyCreateRequest serviceFlavorPolicy = new ServicePolicyCreateRequest();
        serviceFlavorPolicy.setServiceTemplateId(serviceTemplate.getServiceTemplateId());
        List<String> flavors = serviceTemplate.getFlavors().getServiceFlavors().stream().map(
                ServiceFlavor::getName).toList();
        serviceFlavorPolicy.setFlavorNameList(flavors);
        serviceFlavorPolicy.setPolicy("serviceFlavorPolicy");
        serviceFlavorPolicy.setEnabled(true);
        addServiceFlavorPolicies(serviceFlavorPolicy);
    }

    void addServiceFlavorPolicies(ServicePolicyCreateRequest serviceFlavorPolicy)
            throws Exception {
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

    void testGetAvailabilityZonesApiWell() throws Exception {
        testGetAvailabilityZonesForHuaweiCloud();
        testGetAvailabilityZonesForFlexibleEngine();
        testGetAvailabilityZonesForOpenstack();
    }

    void testGetAvailabilityZonesApiThrowsException() throws Exception {
        getAvailabilityZonesThrowsClientApiCallFailedException(Csp.HUAWEI_CLOUD, "cn-southwest");
        getAvailabilityZonesThrowsClientApiCallFailedException(Csp.FLEXIBLE_ENGINE, "eu-west");
        getAvailabilityZonesThrowsClientApiCallFailedException(Csp.OPENSTACK_TESTLAB, "RegionOne");
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
        mockListAvailabilityZonesInvoker(response);
        // Run the test
        final MockHttpServletResponse listAzResponse =
                getAvailabilityZones(Csp.HUAWEI_CLOUD, "cn-southwest-2");
        List<String> azs =
                objectMapper.readValue(listAzResponse.getContentAsString(), new TypeReference<>() {
                });
        Assertions.assertEquals(HttpStatus.OK.value(), listAzResponse.getStatus());
        Assertions.assertEquals(2, azs.size());
        Assertions.assertEquals("cn-southwest-2a", azs.getFirst());
        deleteCredential(Csp.HUAWEI_CLOUD, CredentialType.VARIABLES, "AK_SK");
    }

    void mockListAvailabilityZonesInvoker(NovaListAvailabilityZonesResponse mockResponse) {
        SyncInvoker<NovaListAvailabilityZonesRequest, NovaListAvailabilityZonesResponse>
                mockInvoker = mock(SyncInvoker.class);
        when(mockEcsClient.novaListAvailabilityZonesInvoker(any())).thenReturn(mockInvoker);
        when(mockInvoker.retryTimes(anyInt())).thenReturn(mockInvoker);
        when(mockInvoker.retryCondition(any())).thenReturn(mockInvoker);
        when(mockInvoker.backoffStrategy(any())).thenReturn(mockInvoker);
        when(mockInvoker.invoke()).thenReturn(mockResponse);
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
        mockListAvailabilityZonesInvoker(response);
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
        addCredentialForOpenstack(Csp.OPENSTACK_TESTLAB);
        OSClient.OSClientV3 mockOsClient = getMockOsClientWithMockServices();
        File azsjonFile = new File("src/test/resources/openstack/network/availability_zones.json");
        NeutronAvailabilityZone.AvailabilityZones azResponse =
                objectMapper.readValue(azsjonFile, NeutronAvailabilityZone.AvailabilityZones.class);
        when((List<NeutronAvailabilityZone>) mockOsClient.networking().availabilityzone()
                .list()).thenReturn(azResponse.getList());
        // Run the test
        final MockHttpServletResponse listAzResponse =
                getAvailabilityZones(Csp.OPENSTACK_TESTLAB, "RegionOne");
        List<String> azNames =
                objectMapper.readValue(listAzResponse.getContentAsString(), new TypeReference<>() {
                });
        Assertions.assertEquals(HttpStatus.OK.value(), listAzResponse.getStatus());
        Assertions.assertEquals(1, azNames.size());
        Assertions.assertEquals("nova", azNames.getFirst());
        deleteCredential(Csp.OPENSTACK_TESTLAB, CredentialType.VARIABLES, "USERNAME_PASSWORD");
    }

    void getAvailabilityZonesThrowsClientApiCallFailedException(Csp csp, String regionName)
            throws Exception {
        final MockHttpServletResponse listAzResponse = getAvailabilityZones(csp, regionName);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), listAzResponse.getStatus());
        Assertions.assertEquals(listAzResponse.getContentAsString(), "[]");
        Assertions.assertEquals(listAzResponse.getHeader("Cache-Control"),"no-cache");
    }

    MockHttpServletResponse getAvailabilityZones(Csp csp, String regionName) throws Exception {
        return mockMvc.perform(get("/xpanse/csp/region/azs").param("cspName", csp.toValue())
                        .param("regionName", regionName).accept(MediaType.APPLICATION_JSON)).andReturn()
                .getResponse();
    }


    void testDeployApisWithDeployerTerraformLocal() throws Exception {
        // Setup
        Ocl ocl = new OclLoader().getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        testDeployerWithOclAndPolicy(ocl);

        Ocl oclFromGit = new OclLoader().getOcl(
                URI.create("file:src/test/resources/ocl_terraform_from_git_test.yml").toURL());
        testDeployerWithOclAndPolicy(oclFromGit);
    }

    void testDeployerWithOclAndPolicy(Ocl ocl) throws Exception {
        UUID uuid = UUID.randomUUID();
        ocl.setName("test-" + uuid);
        ServiceTemplateDetailVo serviceTemplate = registerServiceTemplate(ocl);
        if (Objects.isNull(serviceTemplate)) {
            return;
        }
        approveServiceTemplateRegistration(serviceTemplate.getServiceTemplateId());
        setMockPoliciesValidateApi();
        UserPolicyCreateRequest userPolicyCreateRequest = new UserPolicyCreateRequest();
        userPolicyCreateRequest.setCsp(serviceTemplate.getCsp());
        userPolicyCreateRequest.setPolicy("policy-" + uuid);
        UserPolicy userPolicy = addUserPolicy(userPolicyCreateRequest);
        addServicePolicies(serviceTemplate);
        addCredentialForHuaweiCloud();
        // Set up the deployment with policy evaluation failed.
        mockPolicyEvaluationResult(false);
        MockHttpServletResponse response = deployService(getDeployRequest(serviceTemplate));
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        List<DeployedServiceDetails> deployedServices =
                listDeployedServicesDetails(ServiceDeploymentState.DEPLOY_FAILED);
        Assertions.assertNotNull(deployedServices);
        UUID serviceId = deployedServices.getFirst().getServiceId();
        // Set up the deployment with policy evaluation successful and redeploy.
        mockPolicyEvaluationResult(true);
        testRedeploy(serviceId);
        Assertions.assertTrue(
                waitUntilExceptedState(serviceId, ServiceDeploymentState.DEPLOY_SUCCESS));

        ServiceLockConfig serviceLockConfig = new ServiceLockConfig();
        serviceLockConfig.setDestroyLocked(false);
        serviceLockConfig.setModifyLocked(false);
        testChangeLockConfig(serviceId, serviceLockConfig);
        if (waitUntilExceptedState(serviceId, ServiceDeploymentState.DEPLOY_SUCCESS)) {
            List<DeployedServiceDetails> deployedServiceDetailsList =
                    listDeployedServicesDetails(ServiceDeploymentState.DEPLOY_SUCCESS);
            Assertions.assertFalse(deployedServiceDetailsList.isEmpty());
            listDeployedServices();
        }
        if (waitUntilExceptedState(serviceId, ServiceDeploymentState.DEPLOY_SUCCESS)) {
            testDestroy(serviceId);
            if (waitUntilExceptedState(serviceId, ServiceDeploymentState.DESTROY_SUCCESS)) {
                testPurge(serviceId);
            }
        } else {
            testPurge(serviceId);
        }
        serviceTemplateStorage.removeById(serviceTemplate.getServiceTemplateId());
        deleteUserPolicy(userPolicy.getUserPolicyId());
    }

    void testDeployApisWithDeployerOpenTofuLocal() throws Exception {
        // Setup
        Ocl ocl = new OclLoader().getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.getDeployment().setKind(DeployerKind.OPEN_TOFU);
        testDeployerWithOclAndPolicy(ocl);

        Ocl oclFromGit = new OclLoader().getOcl(
                URI.create("file:src/test/resources/ocl_terraform_from_git_test.yml").toURL());
        oclFromGit.getDeployment().setKind(DeployerKind.OPEN_TOFU);
        testDeployerWithOclAndPolicy(oclFromGit);
    }

    void testDeployApisThrowExceptions() throws Exception {
        Ocl ocl = new OclLoader().getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.setName("error-test-" + UUID.randomUUID());
        ocl.getFlavors().setIsDowngradeAllowed(false);
        ServiceFlavorWithPrice defaultFlavor = ocl.getFlavors().getServiceFlavors().getFirst();
        ServiceFlavorWithPrice lowerPriorityFlavor = new ServiceFlavorWithPrice();
        BeanUtils.copyProperties(defaultFlavor, lowerPriorityFlavor);
        lowerPriorityFlavor.setName("lower-priority-flavor");
        lowerPriorityFlavor.setPriority(10);
        ocl.getFlavors().getServiceFlavors().add(lowerPriorityFlavor);
        ServiceTemplateDetailVo serviceTemplate = registerServiceTemplate(ocl);
        testDeployThrowsServiceTemplateNotRegistered();
        testDeployThrowsServiceTemplateNotApproved(serviceTemplate);
        approveServiceTemplateRegistration(serviceTemplate.getServiceTemplateId());
        setMockPoliciesValidateApi();
        addServicePolicies(serviceTemplate);
        testDeployThrowPolicyEvaluationFailed(serviceTemplate);
        addCredentialForHuaweiCloud();
        mockPolicyEvaluationResult(true);
        UUID serviceId = deployService(serviceTemplate);
        if (waitUntilExceptedState(serviceId, ServiceDeploymentState.DEPLOY_SUCCESS)) {
            testApisThrowServiceFlavorDowngradeNotAllowed(serviceId, defaultFlavor,
                    lowerPriorityFlavor);
        }
        testDeployApiFailedWithVariableInvalidException();
        testApisThrowsServiceDeploymentNotFoundException();
        testApisThrowsInvalidServiceStateException(serviceId);
        testApisThrowsServiceLockedException(serviceId);
        testApisThrowsAccessDeniedException(serviceId);
        deployServiceStorage.deleteDeployService(
                deployServiceStorage.findDeployServiceById(serviceId));
        serviceTemplateStorage.removeById(serviceTemplate.getServiceTemplateId());
    }

    void testDeployThrowPolicyEvaluationFailed(ServiceTemplateDetailVo serviceTemplate)
            throws Exception {
        mockPolicyEvaluationResult(false);
        MockHttpServletResponse response = deployService(getDeployRequest(serviceTemplate));
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(),
                response.getStatus());
    }

    void testApisThrowServiceFlavorDowngradeNotAllowed(UUID serviceId,
                                                       ServiceFlavorWithPrice originalServiceFlavor,
                                                       ServiceFlavorWithPrice lowerPriorityFlavor)
            throws Exception {
        // SetUp
        String errorMsg = String.format("Downgrading of flavors is not allowed. New flavor"
                        + " priority %d is lower than the original flavor priority %d.",
                lowerPriorityFlavor.getPriority(), originalServiceFlavor.getPriority());
        Response expectedResponse =
                Response.errorResponse(ResultType.SERVICE_FLAVOR_DOWNGRADE_NOT_ALLOWED,
                        Collections.singletonList(errorMsg));
        String result = objectMapper.writeValueAsString(expectedResponse);
        ModifyRequest modifyRequest = new ModifyRequest();
        modifyRequest.setFlavor(lowerPriorityFlavor.getName());
        modifyRequest.setServiceRequestProperties(new HashMap<>());
        // Run the test
        final MockHttpServletResponse modifyResponse = modifyService(serviceId, modifyRequest);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), modifyResponse.getStatus());
        assertEquals(result, modifyResponse.getContentAsString());
    }

    void testApisThrowsServiceLockedException(UUID serviceId) throws Exception {
        ServiceLockConfig serviceLockConfig = new ServiceLockConfig();
        serviceLockConfig.setDestroyLocked(true);
        serviceLockConfig.setModifyLocked(true);
        testChangeLockConfig(serviceId, serviceLockConfig);
        testModifyThrowsServiceLockedException(serviceId);
        testDestroyThrowsServiceLockedException(serviceId);
    }

    void testApisThrowsServiceDeploymentNotFoundException() throws Exception {
        UUID serviceId = UUID.randomUUID();
        Response expectedResponse = Response.errorResponse(ResultType.SERVICE_DEPLOYMENT_NOT_FOUND,
                Collections.singletonList(
                        String.format("Service with id %s not found.", serviceId)));
        String result = objectMapper.writeValueAsString(expectedResponse);

        // Run the test
        final MockHttpServletResponse detailResponse = getServiceDetails(serviceId);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), detailResponse.getStatus());
        assertEquals(result, detailResponse.getContentAsString());

        ServiceLockConfig lockConfig = new ServiceLockConfig();
        lockConfig.setDestroyLocked(true);
        lockConfig.setModifyLocked(true);
        // Run the test
        final MockHttpServletResponse changeLockResponse = changeLockConfig(serviceId, lockConfig);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), changeLockResponse.getStatus());
        assertEquals(result, changeLockResponse.getContentAsString());

        // Run the test
        final MockHttpServletResponse modifyResponse = redeployService(serviceId);
        assertEquals(HttpStatus.BAD_REQUEST.value(), modifyResponse.getStatus());
        assertEquals(result, modifyResponse.getContentAsString());

        // Run the test
        final MockHttpServletResponse redeployResponse = redeployService(serviceId);
        assertEquals(HttpStatus.BAD_REQUEST.value(), redeployResponse.getStatus());
        assertEquals(result, redeployResponse.getContentAsString());

        // Run the test
        final MockHttpServletResponse destroyResponse = destroyService(serviceId);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), destroyResponse.getStatus());
        assertEquals(result, destroyResponse.getContentAsString());

        // Run the test
        final MockHttpServletResponse purgeResponse = purgeService(serviceId);
        assertEquals(HttpStatus.BAD_REQUEST.value(), purgeResponse.getStatus());
        assertEquals(result, purgeResponse.getContentAsString());
    }

    void testApisThrowsInvalidServiceStateException(UUID serviceId) throws Exception {

        // SetUp modify
        String modifyResult = setInvalidStateAndGetExceptedResult(serviceId,
                ServiceDeploymentState.DESTROYING, "modify");
        ModifyRequest modifyRequest = new ModifyRequest();
        modifyRequest.setFlavor("flavor-error");

        // Run the test
        final MockHttpServletResponse modifyResponse = modifyService(serviceId, modifyRequest);
        assertEquals(HttpStatus.BAD_REQUEST.value(), modifyResponse.getStatus());
        assertEquals(modifyResult, modifyResponse.getContentAsString());

        // SetUp redeploy
        String redeployResult = setInvalidStateAndGetExceptedResult(serviceId,
                ServiceDeploymentState.DEPLOY_SUCCESS, "redeploy");
        // Run the test
        final MockHttpServletResponse redeployResponse = redeployService(serviceId);
        assertEquals(HttpStatus.BAD_REQUEST.value(), redeployResponse.getStatus());
        assertEquals(redeployResult, redeployResponse.getContentAsString());

        // SetUp destroy
        String destroyResult = setInvalidStateAndGetExceptedResult(serviceId,
                ServiceDeploymentState.DEPLOYING, "destroy");
        // Run the test
        final MockHttpServletResponse destroyResponse = destroyService(serviceId);
        assertEquals(HttpStatus.BAD_REQUEST.value(), destroyResponse.getStatus());
        assertEquals(destroyResult, destroyResponse.getContentAsString());

        // SetUp purge
        String purgeResult = setInvalidStateAndGetExceptedResult(serviceId,
                ServiceDeploymentState.DEPLOYING, "purge");
        // Run the test
        final MockHttpServletResponse purgeResponse = purgeService(serviceId);
        assertEquals(HttpStatus.BAD_REQUEST.value(), purgeResponse.getStatus());
        assertEquals(purgeResult, purgeResponse.getContentAsString());
    }


    String setInvalidStateAndGetExceptedResult(UUID serviceId,
                                               ServiceDeploymentState state, String action)
            throws JsonProcessingException {
        DeployServiceEntity deployServiceEntity =
                deployServiceStorage.findDeployServiceById(serviceId);
        deployServiceEntity.setServiceDeploymentState(state);
        deployServiceStorage.storeAndFlush(deployServiceEntity);
        String errorMsg = String.format("Service %s with the state %s is not allowed to %s.",
                serviceId, state, action);
        return objectMapper.writeValueAsString(
                Response.errorResponse(ResultType.SERVICE_STATE_INVALID,
                        Collections.singletonList(errorMsg)));
    }


    void testApisThrowsAccessDeniedException(UUID serviceId) throws Exception {
        // SetUp
        DeployServiceEntity deployServiceEntity =
                deployServiceStorage.findDeployServiceById(serviceId);
        deployServiceEntity.setUserId("invalid-user-id");
        deployServiceStorage.storeAndFlush(deployServiceEntity);

        // SetUp changeLockConfig
        String changeLockResult = getAccessDeniedExceptedResult("change lock config of");
        ServiceLockConfig lockConfig = new ServiceLockConfig();
        lockConfig.setDestroyLocked(true);
        lockConfig.setModifyLocked(true);
        // Run the test
        final MockHttpServletResponse changeLockResponse = changeLockConfig(serviceId, lockConfig);
        // Verify the results
        assertEquals(HttpStatus.FORBIDDEN.value(), changeLockResponse.getStatus());
        assertEquals(changeLockResult, changeLockResponse.getContentAsString());

        // SetUp modify
        String modifyResult = getAccessDeniedExceptedResult("modify");
        ModifyRequest modifyRequest = new ModifyRequest();
        modifyRequest.setFlavor("flavor-error");
        // Run the test
        final MockHttpServletResponse modifyResponse = modifyService(serviceId, modifyRequest);
        assertEquals(HttpStatus.FORBIDDEN.value(), modifyResponse.getStatus());
        assertEquals(modifyResult, modifyResponse.getContentAsString());

        // SetUp redeploy
        String redeployResult = getAccessDeniedExceptedResult("redeploy");
        // Run the test
        final MockHttpServletResponse redeployResponse = redeployService(serviceId);
        assertEquals(HttpStatus.FORBIDDEN.value(), redeployResponse.getStatus());
        assertEquals(redeployResult, redeployResponse.getContentAsString());

        // SetUp destroy
        String destroyResult = getAccessDeniedExceptedResult("destroy");
        // Run the test
        final MockHttpServletResponse destroyResponse = destroyService(serviceId);
        assertEquals(HttpStatus.FORBIDDEN.value(), destroyResponse.getStatus());
        assertEquals(destroyResult, destroyResponse.getContentAsString());

        // SetUp purge
        String purgeResult = getAccessDeniedExceptedResult("purge");
        // Run the test
        final MockHttpServletResponse purgeResponse = purgeService(serviceId);
        assertEquals(HttpStatus.FORBIDDEN.value(), purgeResponse.getStatus());
        assertEquals(purgeResult, purgeResponse.getContentAsString());
    }

    String getAccessDeniedExceptedResult(String action) throws Exception {
        String errorMsg = String.format("No permissions to %s services belonging to other users.",
                action);
        Response expectedResponse = Response.errorResponse(ResultType.ACCESS_DENIED,
                Collections.singletonList(errorMsg));
        return objectMapper.writeValueAsString(expectedResponse);
    }

    void testDeployApiFailedWithVariableInvalidException() throws Exception {
        // Setup
        Ocl ocl = new OclLoader().getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.setName("error-test-" + UUID.randomUUID());
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
        ocl.getDeployment().setServiceAvailabilityConfigs(zoneConfigs);
        ServiceTemplateDetailVo serviceTemplate = registerServiceTemplate(ocl);
        approveServiceTemplateRegistration(serviceTemplate.getServiceTemplateId());
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

        final MockHttpServletResponse deployResponse1 = deployService(deployRequest1);
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

        final MockHttpServletResponse deployResponse2 = deployService(deployRequest2);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), deployResponse2.getStatus());
        assertEquals(result2, deployResponse2.getContentAsString());
        serviceTemplateStorage.removeById(serviceTemplate.getServiceTemplateId());
    }

    void testRedeploy(UUID serviceId)
            throws Exception {
        String successMsg = String.format("Task for redeploying managed service %s has started.",
                serviceId);
        Response response = Response.successResponse(Collections.singletonList(successMsg));
        String result = objectMapper.writeValueAsString(response);
        // Run the test
        final MockHttpServletResponse redeployResponse = redeployService(serviceId);
        assertEquals(HttpStatus.ACCEPTED.value(), redeployResponse.getStatus());
        assertEquals(result, redeployResponse.getContentAsString());
    }

    void testChangeLockConfig(UUID serviceId, ServiceLockConfig lockConfig)
            throws Exception {
        // Run the test
        final MockHttpServletResponse changeLockResponse = changeLockConfig(serviceId, lockConfig);
        assertEquals(HttpStatus.NO_CONTENT.value(), changeLockResponse.getStatus());
    }


    void testDestroy(UUID serviceId) throws Exception {
        // SetUp
        String successMsg =
                String.format("Task for destroying managed service %s has started.", serviceId);
        Response response = Response.successResponse(Collections.singletonList(successMsg));
        String result = objectMapper.writeValueAsString(response);
        // Run the test
        final MockHttpServletResponse destroyResponse = destroyService(serviceId);
        // Verify the results
        assertEquals(HttpStatus.ACCEPTED.value(), destroyResponse.getStatus());
        assertEquals(result, destroyResponse.getContentAsString());
    }


    void testDestroyThrowsServiceLockedException(UUID serviceId) throws Exception {
        // SetUp
        String message = String.format("Service with id %s is locked from deletion.", serviceId);
        Response expectedResponse = Response.errorResponse(ResultType.SERVICE_LOCKED,
                Collections.singletonList(message));
        String result = objectMapper.writeValueAsString(expectedResponse);
        // Run the test
        final MockHttpServletResponse destroyResponse = destroyService(serviceId);
        assertEquals(HttpStatus.BAD_REQUEST.value(), destroyResponse.getStatus());
        assertEquals(result, destroyResponse.getContentAsString());
    }

    void testModifyThrowsServiceLockedException(UUID serviceId) throws Exception {
        // SetUp
        String message = String.format("Service with id %s is locked from modification.",
                serviceId);
        Response expectedResponse = Response.errorResponse(ResultType.SERVICE_LOCKED,
                Collections.singletonList(message));
        String result = objectMapper.writeValueAsString(expectedResponse);
        ModifyRequest modifyRequest = new ModifyRequest();
        modifyRequest.setFlavor("flavor-error");
        // Run the test
        final MockHttpServletResponse destroyResponse =
                modifyService(serviceId, modifyRequest);
        assertEquals(HttpStatus.BAD_REQUEST.value(), destroyResponse.getStatus());
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
        final MockHttpServletResponse deployResponse = deployService(deployRequest);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), deployResponse.getStatus());
        assertEquals(result, deployResponse.getContentAsString());
    }


    void testPurge(UUID serviceId) throws Exception {
        // SetUp
        String successMsg =
                String.format("Task for purging managed service %s has started.", serviceId);
        Response response = Response.successResponse(Collections.singletonList(successMsg));
        String result = objectMapper.writeValueAsString(response);
        // Run the test
        final MockHttpServletResponse purgeResponse = purgeService(serviceId);
        assertEquals(HttpStatus.ACCEPTED.value(), purgeResponse.getStatus());
        assertEquals(result, purgeResponse.getContentAsString());

        Thread.sleep(waitTime);
        // SetUp
        String refuseMsg = String.format("Service with id %s not found.", serviceId);
        Response detailsErrorResponse =
                Response.errorResponse(ResultType.SERVICE_DEPLOYMENT_NOT_FOUND,
                        Collections.singletonList(refuseMsg));
        String detailsResult = objectMapper.writeValueAsString(detailsErrorResponse);
        final MockHttpServletResponse detailsResponse =
                mockMvc.perform(get("/xpanse/services/details/self_hosted/{id}", serviceId))
                        .andReturn().getResponse();

        assertEquals(HttpStatus.BAD_REQUEST.value(), detailsResponse.getStatus());
        assertEquals(detailsResult, detailsResponse.getContentAsString());
    }


    void listDeployedServices() throws Exception {

        final MockHttpServletResponse listResponse = mockMvc.perform(
                        get("/xpanse/services").contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))

                .andReturn().getResponse();
        List<DeployedService> deployedServices =
                objectMapper.readValue(listResponse.getContentAsString(),
                        new TypeReference<>() {
                        });
        assertEquals(HttpStatus.OK.value(), listResponse.getStatus());
        assertFalse(deployedServices.isEmpty());
    }

    List<DeployedServiceDetails> listDeployedServicesDetails(ServiceDeploymentState state)
            throws Exception {

        final MockHttpServletResponse listResponse = mockMvc.perform(
                        get("/xpanse/services/details").param("serviceState", state.toValue())
                                .contentType(MediaType.APPLICATION_JSON)
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
        deployRequest.setCsp(Csp.HUAWEI_CLOUD);
        deployRequest.setCategory(Category.AI);
        deployRequest.setFlavor("flavor2");
        Region region = new Region();
        region.setName("regionName");
        region.setArea("areaName");
        deployRequest.setRegion(region);
        deployRequest.setServiceHostingType(ServiceHostingType.SELF);
        deployRequest.setBillingMode(BillingMode.FIXED);
        // Run the test
        final MockHttpServletResponse deployResponse = deployService(deployRequest);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), deployResponse.getStatus());
        assertEquals(result, deployResponse.getContentAsString());

    }


    MockHttpServletResponse getServiceDetails(UUID serviceId) throws Exception {
        return mockMvc.perform(get("/xpanse/services/details/self_hosted/{id}", serviceId)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();
    }


    MockHttpServletResponse deployService(DeployRequest deployRequest) throws Exception {
        return mockMvc.perform(post("/xpanse/services")
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deployRequest))).andReturn()
                .getResponse();
    }


    MockHttpServletResponse changeLockConfig(UUID serviceId, ServiceLockConfig lockConfig)
            throws Exception {
        return mockMvc.perform(put("/xpanse/services/changelock/{id}", serviceId)
                        .content(objectMapper.writeValueAsString(lockConfig))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }

    MockHttpServletResponse modifyService(UUID serviceId, ModifyRequest modifyRequest)
            throws Exception {
        return mockMvc.perform(put("/xpanse/services/modify/{id}", serviceId)
                        .content(objectMapper.writeValueAsString(modifyRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }

    MockHttpServletResponse destroyService(UUID serviceId) throws Exception {
        return mockMvc.perform(delete("/xpanse/services/{id}", serviceId)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();
    }

    MockHttpServletResponse purgeService(UUID serviceId) throws Exception {
        return mockMvc.perform(delete("/xpanse/services/purge/{id}", serviceId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
    }

    MockHttpServletResponse redeployService(UUID serviceId) throws Exception {
        return mockMvc.perform(put("/xpanse/services/deploy/retry/{id}", serviceId)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();
    }
}
