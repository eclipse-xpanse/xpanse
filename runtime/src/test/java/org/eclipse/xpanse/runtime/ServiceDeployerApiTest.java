/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime;

import static org.eclipse.xpanse.modules.logging.LoggingKeyConstant.HEADER_TRACKING_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import com.fasterxml.jackson.core.type.TypeReference;
import com.huaweicloud.sdk.core.invoker.SyncInvoker;
import com.huaweicloud.sdk.ecs.v2.model.NovaAvailabilityZone;
import com.huaweicloud.sdk.ecs.v2.model.NovaListAvailabilityZonesRequest;
import com.huaweicloud.sdk.ecs.v2.model.NovaListAvailabilityZonesResponse;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.models.billing.enums.BillingMode;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.policy.servicepolicy.ServicePolicyCreateRequest;
import org.eclipse.xpanse.modules.models.policy.userpolicy.UserPolicy;
import org.eclipse.xpanse.modules.models.policy.userpolicy.UserPolicyCreateRequest;
import org.eclipse.xpanse.modules.models.response.OrderFailedResponse;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.service.config.ServiceLockConfig;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.eclipse.xpanse.modules.models.service.modify.ModifyRequest;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
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
@SpringBootTest(properties = {"spring.profiles.active=oauth,zitadel,zitadel-testbed,test",})
@AutoConfigureMockMvc
class ServiceDeployerApiTest extends ApisTestCommon {
    @MockBean
    private PoliciesValidateApi mockPoliciesValidateApi;
    @MockBean
    private PoliciesEvaluationApi mockPoliciesEvaluationApi;

    @Test
    @WithJwt(file = "jwt_user.json")
    void testGetAzsApi() throws Exception {
        if (mockOsFactory != null) {
            mockOsFactory.close();
        }
        mockOsFactory = mockStatic(OSFactory.class);
        testGetAvailabilityZonesApiThrowsException();
        testGetAvailabilityZonesApiWell();
        mockOsFactory.close();
    }

    @Test
    @WithJwt(file = "jwt_all_roles-no-policies.json")
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
        MockHttpServletResponse response = mockMvc.perform(post("/xpanse/service/policies").content(
                                objectMapper.writeValueAsString(serviceFlavorPolicy))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertNotNull(response.getHeader(HEADER_TRACKING_ID));
    }

    UserPolicy addUserPolicy(UserPolicyCreateRequest userPolicy) throws Exception {
        userPolicy.setEnabled(true);
        final MockHttpServletResponse response = mockMvc.perform(
                        post("/xpanse/policies").content(objectMapper.writeValueAsString(userPolicy))
                                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertNotNull(response.getHeader(HEADER_TRACKING_ID));
        return objectMapper.readValue(response.getContentAsString(), UserPolicy.class);
    }

    void deleteUserPolicy(UUID id) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(
                delete("/xpanse/policies/{serviceId}", id).contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatus());
        assertNotNull(response.getHeader(HEADER_TRACKING_ID));
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
        getAvailabilityZonesThrowsClientApiCallFailedException(
                Csp.HUAWEI_CLOUD, "Chinese Mainland", "cn-southwest");
        getAvailabilityZonesThrowsClientApiCallFailedException(
                Csp.FLEXIBLE_ENGINE, "default", "eu-west-0");
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
                getAvailabilityZones(Csp.HUAWEI_CLOUD, "Chinese Mainland", "cn-southwest-2");
        List<String> azs =
                objectMapper.readValue(listAzResponse.getContentAsString(), new TypeReference<>() {
                });
        assertEquals(HttpStatus.OK.value(), listAzResponse.getStatus());
        assertEquals(2, azs.size());
        assertEquals("cn-southwest-2a", azs.getFirst());
        deleteCredential(Csp.HUAWEI_CLOUD, "Chinese Mainland", CredentialType.VARIABLES, "AK_SK");
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
                getAvailabilityZones(Csp.FLEXIBLE_ENGINE, "default", "eu-west-0");
        List<String> azs =
                objectMapper.readValue(listAzResponse.getContentAsString(), new TypeReference<>() {
                });
        assertEquals(HttpStatus.OK.value(), listAzResponse.getStatus());
        assertEquals(2, azs.size());
        assertEquals("eu-west-0a", azs.getFirst());
        deleteCredential(Csp.FLEXIBLE_ENGINE, "default", CredentialType.VARIABLES, "AK_SK");
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
                getAvailabilityZones(Csp.OPENSTACK_TESTLAB, "default", "RegionOne");
        List<String> azNames =
                objectMapper.readValue(listAzResponse.getContentAsString(), new TypeReference<>() {
                });
        assertEquals(HttpStatus.OK.value(), listAzResponse.getStatus());
        assertEquals(1, azNames.size());
        assertEquals("nova", azNames.getFirst());
        deleteCredential(Csp.OPENSTACK_TESTLAB, "default", CredentialType.VARIABLES,
                "USERNAME_PASSWORD");
    }

    void getAvailabilityZonesThrowsClientApiCallFailedException(
            Csp csp, String siteName, String regionName) throws Exception {
        final MockHttpServletResponse listAzResponse =
                getAvailabilityZones(csp, siteName, regionName);
        assertEquals(HttpStatus.BAD_REQUEST.value(), listAzResponse.getStatus());
        assertEquals(listAzResponse.getHeader("Cache-Control"), "no-cache");
    }

    MockHttpServletResponse getAvailabilityZones(Csp csp, String siteName, String regionName)
            throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/xpanse/csp/region/azs")
                        .param("cspName", csp.toValue())
                        .param("siteName", siteName)
                        .param("regionName", regionName)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertNotNull(response.getHeader(HEADER_TRACKING_ID));
        return response;
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

        listDeployedServices();
        testGetComputeResourcesOfService(serviceId);
        List<DeployedServiceDetails> deployedServiceDetailsList =
                listDeployedServicesDetails(ServiceDeploymentState.DEPLOY_SUCCESS);
        Assertions.assertFalse(deployedServiceDetailsList.isEmpty());

        ServiceLockConfig serviceLockConfig = new ServiceLockConfig();
        serviceLockConfig.setDestroyLocked(false);
        serviceLockConfig.setModifyLocked(false);
        testChangeLockConfig(serviceId, serviceLockConfig);

        testModify(serviceId, serviceTemplate);
        if (waitServiceDeploymentIsCompleted(serviceId)) {
            testDestroy(serviceId);
            if (waitServiceDeploymentIsCompleted(serviceId)) {
                testPurge(serviceId);
            }
        } else {
            testPurge(serviceId);
        }
        deleteUserPolicy(userPolicy.getUserPolicyId());
        deleteServiceTemplate(serviceTemplate.getServiceTemplateId());
    }

    void testDeployApisWithDeployerOpenTofuLocal() throws Exception {
        // Setup
        Ocl ocl = new OclLoader().getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.getDeployment().getDeployerTool().setKind(DeployerKind.OPEN_TOFU);
        testDeployerWithOclAndPolicy(ocl);

        Ocl oclFromGit = new OclLoader().getOcl(
                URI.create("file:src/test/resources/ocl_terraform_from_git_test.yml").toURL());
        oclFromGit.getDeployment().getDeployerTool().setKind(DeployerKind.OPEN_TOFU);
        testDeployerWithOclAndPolicy(oclFromGit);
    }

    void testDeployApisThrowExceptions() throws Exception {
        Ocl ocl = new OclLoader().getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
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
        ServiceOrder serviceOrder = deployService(serviceTemplate);
        UUID serviceId = serviceOrder.getServiceId();
        UUID orderId = serviceOrder.getOrderId();
        if (waitServiceOrderIsCompleted(orderId)) {
            testApisThrowServiceFlavorDowngradeNotAllowed(serviceId, defaultFlavor,
                    lowerPriorityFlavor);
        }
        testDeployApiFailedWithVariableInvalidException();
        testApisThrowsServiceDeploymentNotFoundException();
        testApisThrowsInvalidServiceStateException(serviceId);
        testApisThrowsServiceLockedException(serviceId);
        testApisThrowsAccessDeniedException(serviceId);

        deleteServiceDeployment(serviceId);
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
        ModifyRequest modifyRequest = new ModifyRequest();
        modifyRequest.setFlavor(lowerPriorityFlavor.getName());
        modifyRequest.setServiceRequestProperties(new HashMap<>());
        // Run the test
        final MockHttpServletResponse modifyResponse = modifyService(serviceId, modifyRequest);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), modifyResponse.getStatus());
        assertNotNull(modifyResponse.getHeader(HEADER_TRACKING_ID));
        OrderFailedResponse orderFailedResponse = objectMapper.readValue(
                modifyResponse.getContentAsString(), OrderFailedResponse.class);
        assertFalse(orderFailedResponse.getSuccess());
        assertEquals(orderFailedResponse.getResultType(),
                ResultType.SERVICE_FLAVOR_DOWNGRADE_NOT_ALLOWED);
        assertEquals(orderFailedResponse.getDetails(), List.of(errorMsg));
        assertEquals(orderFailedResponse.getServiceId(), serviceId.toString());
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
        String errorMsg = String.format("Service with id %s not found.", serviceId);

        // Run the test
        final MockHttpServletResponse detailResponse = getServiceDetails(serviceId);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), detailResponse.getStatus());
        Response response =
                objectMapper.readValue(detailResponse.getContentAsString(), Response.class);
        assertFalse(response.getSuccess());
        assertEquals(response.getResultType(), ResultType.SERVICE_DEPLOYMENT_NOT_FOUND);
        assertEquals(response.getDetails(), List.of(errorMsg));

        // SetUp getComputeResourceInventoryOfService
        final MockHttpServletResponse getResourcesResponse =
                getComputeResourceInventoryOfService(serviceId);
        assertEquals(HttpStatus.BAD_REQUEST.value(), getResourcesResponse.getStatus());
        response =
                objectMapper.readValue(getResourcesResponse.getContentAsString(), Response.class);
        assertFalse(response.getSuccess());
        assertEquals(response.getResultType(), ResultType.SERVICE_DEPLOYMENT_NOT_FOUND);
        assertEquals(response.getDetails(), List.of(errorMsg));

        ServiceLockConfig lockConfig = new ServiceLockConfig();
        lockConfig.setDestroyLocked(true);
        lockConfig.setModifyLocked(true);
        // Run the test
        final MockHttpServletResponse changeLockResponse = changeLockConfig(serviceId, lockConfig);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), changeLockResponse.getStatus());
        response =
                objectMapper.readValue(changeLockResponse.getContentAsString(), Response.class);
        assertFalse(response.getSuccess());
        assertEquals(response.getResultType(), ResultType.SERVICE_DEPLOYMENT_NOT_FOUND);
        assertEquals(response.getDetails(), List.of(errorMsg));

        // Run the test
        ModifyRequest modifyRequest = new ModifyRequest();
        modifyRequest.setFlavor("flavor-new");
        final MockHttpServletResponse modifyResponse = modifyService(serviceId, modifyRequest);
        assertEquals(HttpStatus.BAD_REQUEST.value(), modifyResponse.getStatus());
        OrderFailedResponse orderFailedResponse = objectMapper.readValue(
                modifyResponse.getContentAsString(), OrderFailedResponse.class);
        assertFalse(orderFailedResponse.getSuccess());
        assertEquals(orderFailedResponse.getResultType(), ResultType.SERVICE_DEPLOYMENT_NOT_FOUND);
        assertEquals(orderFailedResponse.getDetails(), List.of(errorMsg));
        assertEquals(orderFailedResponse.getServiceId(), serviceId.toString());

        // Run the test
        final MockHttpServletResponse redeployResponse = redeployService(serviceId);
        assertEquals(HttpStatus.BAD_REQUEST.value(), redeployResponse.getStatus());
        orderFailedResponse = objectMapper.readValue(
                redeployResponse.getContentAsString(), OrderFailedResponse.class);
        assertFalse(orderFailedResponse.getSuccess());
        assertEquals(orderFailedResponse.getResultType(), ResultType.SERVICE_DEPLOYMENT_NOT_FOUND);
        assertEquals(orderFailedResponse.getDetails(), List.of(errorMsg));
        assertEquals(orderFailedResponse.getServiceId(), serviceId.toString());

        // Run the test
        final MockHttpServletResponse destroyResponse = destroyService(serviceId);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), destroyResponse.getStatus());
        orderFailedResponse = objectMapper.readValue(
                destroyResponse.getContentAsString(), OrderFailedResponse.class);
        assertFalse(orderFailedResponse.getSuccess());
        assertEquals(orderFailedResponse.getResultType(), ResultType.SERVICE_DEPLOYMENT_NOT_FOUND);
        assertEquals(orderFailedResponse.getDetails(), List.of(errorMsg));
        assertEquals(orderFailedResponse.getServiceId(), serviceId.toString());

        // Run the test
        final MockHttpServletResponse purgeResponse = purgeService(serviceId);
        assertEquals(HttpStatus.BAD_REQUEST.value(), purgeResponse.getStatus());
        orderFailedResponse = objectMapper.readValue(
                purgeResponse.getContentAsString(), OrderFailedResponse.class);
        assertFalse(orderFailedResponse.getSuccess());
        assertEquals(orderFailedResponse.getResultType(), ResultType.SERVICE_DEPLOYMENT_NOT_FOUND);
        assertEquals(orderFailedResponse.getDetails(), List.of(errorMsg));
        assertEquals(orderFailedResponse.getServiceId(), serviceId.toString());
    }

    void testApisThrowsInvalidServiceStateException(UUID serviceId) throws Exception {

        // SetUp modify
        String errorMsg = setInvalidStateAndGetExceptedErrorMsg(serviceId,
                ServiceDeploymentState.DESTROYING, "modify");
        ModifyRequest modifyRequest = new ModifyRequest();
        modifyRequest.setFlavor("flavor-error");

        // Run the test
        final MockHttpServletResponse modifyResponse = modifyService(serviceId, modifyRequest);
        assertEquals(HttpStatus.BAD_REQUEST.value(), modifyResponse.getStatus());
        OrderFailedResponse orderFailedResponse = objectMapper.readValue(
                modifyResponse.getContentAsString(), OrderFailedResponse.class);
        assertFalse(orderFailedResponse.getSuccess());
        assertEquals(orderFailedResponse.getResultType(), ResultType.SERVICE_STATE_INVALID);
        assertEquals(orderFailedResponse.getDetails(), List.of(errorMsg));
        assertEquals(orderFailedResponse.getServiceId(), serviceId.toString());

        // SetUp redeploy
        errorMsg = setInvalidStateAndGetExceptedErrorMsg(serviceId,
                ServiceDeploymentState.DEPLOY_SUCCESS, "redeploy");
        // Run the test
        final MockHttpServletResponse redeployResponse = redeployService(serviceId);
        assertEquals(HttpStatus.BAD_REQUEST.value(), redeployResponse.getStatus());
        orderFailedResponse = objectMapper.readValue(
                redeployResponse.getContentAsString(), OrderFailedResponse.class);
        assertFalse(orderFailedResponse.getSuccess());
        assertEquals(orderFailedResponse.getResultType(), ResultType.SERVICE_STATE_INVALID);
        assertEquals(orderFailedResponse.getDetails(), List.of(errorMsg));
        assertEquals(orderFailedResponse.getServiceId(), serviceId.toString());

        // SetUp destroy
        errorMsg = setInvalidStateAndGetExceptedErrorMsg(serviceId,
                ServiceDeploymentState.DEPLOYING, "destroy");
        // Run the test
        final MockHttpServletResponse destroyResponse = destroyService(serviceId);
        assertEquals(HttpStatus.BAD_REQUEST.value(), destroyResponse.getStatus());
        orderFailedResponse = objectMapper.readValue(
                destroyResponse.getContentAsString(), OrderFailedResponse.class);
        assertFalse(orderFailedResponse.getSuccess());
        assertEquals(orderFailedResponse.getResultType(), ResultType.SERVICE_STATE_INVALID);
        assertEquals(orderFailedResponse.getDetails(), List.of(errorMsg));
        assertEquals(orderFailedResponse.getServiceId(), serviceId.toString());

        // SetUp purge
        errorMsg = setInvalidStateAndGetExceptedErrorMsg(serviceId,
                ServiceDeploymentState.DEPLOYING, "purge");
        // Run the test
        final MockHttpServletResponse purgeResponse = purgeService(serviceId);
        assertEquals(HttpStatus.BAD_REQUEST.value(), purgeResponse.getStatus());
        orderFailedResponse = objectMapper.readValue(
                purgeResponse.getContentAsString(), OrderFailedResponse.class);
        assertFalse(orderFailedResponse.getSuccess());
        assertEquals(orderFailedResponse.getResultType(), ResultType.SERVICE_STATE_INVALID);
        assertEquals(orderFailedResponse.getDetails(), List.of(errorMsg));
        assertEquals(orderFailedResponse.getServiceId(), serviceId.toString());
    }


    String setInvalidStateAndGetExceptedErrorMsg(UUID serviceId,
                                                 ServiceDeploymentState state, String action) {
        ServiceDeploymentEntity serviceDeploymentEntity =
                serviceDeploymentStorage.findServiceDeploymentById(serviceId);
        serviceDeploymentEntity.setServiceDeploymentState(state);
        serviceDeploymentStorage.storeAndFlush(serviceDeploymentEntity);
        return String.format("Service %s with the state %s is not allowed to %s.",
                serviceId, state, action);
    }


    void testApisThrowsAccessDeniedException(UUID serviceId) throws Exception {
        // SetUp
        ServiceDeploymentEntity serviceDeploymentEntity =
                serviceDeploymentStorage.findServiceDeploymentById(serviceId);
        serviceDeploymentEntity.setUserId("invalid-user-id");
        serviceDeploymentStorage.storeAndFlush(serviceDeploymentEntity);

        // SetUp changeLockConfig
        String errorMsg1 =
                "No permissions to change lock config of services belonging to other users.";
        ServiceLockConfig lockConfig = new ServiceLockConfig();
        lockConfig.setDestroyLocked(true);
        lockConfig.setModifyLocked(true);
        // Run the test
        final MockHttpServletResponse changeLockResponse = changeLockConfig(serviceId, lockConfig);
        // Verify the results
        assertEquals(HttpStatus.FORBIDDEN.value(), changeLockResponse.getStatus());
        Response response =
                objectMapper.readValue(changeLockResponse.getContentAsString(), Response.class);
        assertFalse(response.getSuccess());
        assertEquals(ResultType.ACCESS_DENIED, response.getResultType());
        assertEquals(List.of(errorMsg1), response.getDetails());

        // SetUp getComputeResourceInventoryOfService
        String errorMsg2 = "No permissions to view resources of services belonging to other users.";
        final MockHttpServletResponse getResourcesResponse =
                getComputeResourceInventoryOfService(serviceId);
        assertEquals(HttpStatus.FORBIDDEN.value(), getResourcesResponse.getStatus());
        response =
                objectMapper.readValue(getResourcesResponse.getContentAsString(), Response.class);
        assertFalse(response.getSuccess());
        assertEquals(ResultType.ACCESS_DENIED, response.getResultType());
        assertEquals(List.of(errorMsg2), response.getDetails());

        // SetUp modify
        String errorMsg3 = "No permissions to modify services belonging to other users.";
        ModifyRequest modifyRequest = new ModifyRequest();
        modifyRequest.setFlavor("flavor-error");
        // Run the test
        final MockHttpServletResponse modifyResponse = modifyService(serviceId, modifyRequest);
        assertEquals(HttpStatus.FORBIDDEN.value(), modifyResponse.getStatus());
        OrderFailedResponse orderFailedResponse = objectMapper.readValue(
                modifyResponse.getContentAsString(), OrderFailedResponse.class);
        assertFalse(orderFailedResponse.getSuccess());
        assertEquals(ResultType.ACCESS_DENIED, orderFailedResponse.getResultType());
        assertEquals(List.of(errorMsg3), orderFailedResponse.getDetails());
        assertEquals(orderFailedResponse.getServiceId(), serviceId.toString());

        // SetUp redeploy
        String errorMsg4 = "No permissions to redeploy services belonging to other users.";
        // Run the test
        final MockHttpServletResponse redeployResponse = redeployService(serviceId);
        assertEquals(HttpStatus.FORBIDDEN.value(), redeployResponse.getStatus());
        orderFailedResponse = objectMapper.readValue(
                redeployResponse.getContentAsString(), OrderFailedResponse.class);
        assertFalse(orderFailedResponse.getSuccess());
        assertEquals(ResultType.ACCESS_DENIED, orderFailedResponse.getResultType());
        assertEquals(List.of(errorMsg4), orderFailedResponse.getDetails());
        assertEquals(orderFailedResponse.getServiceId(), serviceId.toString());

        // SetUp destroy
        String errorMsg5 = "No permissions to destroy services belonging to other users.";
        // Run the test
        final MockHttpServletResponse destroyResponse = destroyService(serviceId);
        assertEquals(HttpStatus.FORBIDDEN.value(), destroyResponse.getStatus());
        orderFailedResponse = objectMapper.readValue(
                destroyResponse.getContentAsString(), OrderFailedResponse.class);
        assertFalse(orderFailedResponse.getSuccess());
        assertEquals(ResultType.ACCESS_DENIED, orderFailedResponse.getResultType());
        assertEquals(List.of(errorMsg5), orderFailedResponse.getDetails());
        assertEquals(orderFailedResponse.getServiceId(), serviceId.toString());

        // SetUp purge
        String errorMsg6 = "No permissions to purge services belonging to other users.";
        // Run the test
        final MockHttpServletResponse purgeResponse = purgeService(serviceId);
        assertEquals(HttpStatus.FORBIDDEN.value(), purgeResponse.getStatus());
        orderFailedResponse = objectMapper.readValue(
                purgeResponse.getContentAsString(), OrderFailedResponse.class);
        assertFalse(orderFailedResponse.getSuccess());
        assertEquals(ResultType.ACCESS_DENIED, orderFailedResponse.getResultType());
        assertEquals(List.of(errorMsg6), orderFailedResponse.getDetails());
        assertEquals(orderFailedResponse.getServiceId(), serviceId.toString());
    }

    void testDeployApiFailedWithVariableInvalidException() throws Exception {
        // Setup
        Ocl ocl = new OclLoader().getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
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
        ocl.getDeployment().setServiceAvailabilityConfig(zoneConfigs);
        ServiceTemplateDetailVo serviceTemplate = registerServiceTemplate(ocl);
        approveServiceTemplateRegistration(serviceTemplate.getServiceTemplateId());
        // Run the test
        DeployRequest deployRequest1 = getDeployRequest(serviceTemplate);
        deployRequest1.getServiceRequestProperties().clear();
        // SetUp
        String refuseMsg1 =
                String.format("Variable validation failed:" + " [required property '%s' not found]",
                        ocl.getDeployment().getVariables().getLast().getName());

        final MockHttpServletResponse deployResponse1 = deployService(deployRequest1);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), deployResponse1.getStatus());
        OrderFailedResponse response =
                objectMapper.readValue(deployResponse1.getContentAsString(),
                        OrderFailedResponse.class);
        assertFalse(response.getSuccess());
        assertEquals(response.getResultType(), ResultType.VARIABLE_VALIDATION_FAILED);
        assertEquals(response.getDetails(), List.of(refuseMsg1));

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
        Response.errorResponse(ResultType.VARIABLE_VALIDATION_FAILED, List.of(refuseMsg2));

        final MockHttpServletResponse deployResponse2 = deployService(deployRequest2);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), deployResponse2.getStatus());
        Response response2 =
                objectMapper.readValue(deployResponse2.getContentAsString(), Response.class);
        assertFalse(response2.getSuccess());
        assertEquals(response2.getResultType(), ResultType.VARIABLE_VALIDATION_FAILED);
        assertEquals(response2.getDetails(), List.of(refuseMsg2));
        deleteServiceTemplate(serviceTemplate.getServiceTemplateId());
    }

    void testRedeploy(UUID serviceId) throws Exception {
        // Run the test
        final MockHttpServletResponse redeployResponse = redeployService(serviceId);
        assertEquals(HttpStatus.ACCEPTED.value(), redeployResponse.getStatus());
        ServiceOrder serviceOrder =
                objectMapper.readValue(redeployResponse.getContentAsString(), ServiceOrder.class);
        assertEquals(serviceId, serviceOrder.getServiceId());
        assertNotNull(serviceOrder.getOrderId());
        assertTrue(waitServiceOrderIsCompleted(serviceOrder.getOrderId()));
        assertTrue(waitServiceDeploymentIsCompleted(serviceId));
    }

    void testModify(UUID serviceId, ServiceTemplateDetailVo serviceTemplate) throws Exception {
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
        // Verify the results
        assertEquals(HttpStatus.ACCEPTED.value(), modifyResponse.getStatus());
        ServiceOrder serviceOrder =
                objectMapper.readValue(modifyResponse.getContentAsString(), ServiceOrder.class);
        assertEquals(serviceId, serviceOrder.getServiceId());
        assertNotNull(serviceOrder.getOrderId());
        assertTrue(waitServiceOrderIsCompleted(serviceOrder.getOrderId()));
        assertTrue(waitServiceDeploymentIsCompleted(serviceId));
    }


    void testChangeLockConfig(UUID serviceId, ServiceLockConfig lockConfig) throws Exception {
        // Run the test
        final MockHttpServletResponse changeLockResponse = changeLockConfig(serviceId, lockConfig);
        assertEquals(HttpStatus.OK.value(), changeLockResponse.getStatus());
        ServiceOrder serviceOrder =
                objectMapper.readValue(changeLockResponse.getContentAsString(), ServiceOrder.class);
        assertEquals(serviceId, serviceOrder.getServiceId());
        ServiceOrderEntity serviceOrderEntity =
                serviceOrderStorage.getEntityById(serviceOrder.getOrderId());
        assertEquals(serviceOrderEntity.getTaskType(), ServiceOrderType.LOCK_CHANGE);
        assertEquals(serviceOrderEntity.getTaskStatus(), TaskStatus.SUCCESSFUL);
    }


    void testDestroy(UUID serviceId) throws Exception {
        // Run the test
        final MockHttpServletResponse destroyResponse = destroyService(serviceId);
        // Verify the results
        assertEquals(HttpStatus.ACCEPTED.value(), destroyResponse.getStatus());
        ServiceOrder serviceOrder =
                objectMapper.readValue(destroyResponse.getContentAsString(), ServiceOrder.class);
        assertEquals(serviceId, serviceOrder.getServiceId());
        assertNotNull(serviceOrder.getOrderId());
        assertTrue(waitServiceOrderIsCompleted(serviceOrder.getOrderId()));
        assertTrue(waitServiceDeploymentIsCompleted(serviceId));
    }


    void testDestroyThrowsServiceLockedException(UUID serviceId) throws Exception {
        // SetUp
        String errorMsg = String.format("Service %s is locked from deletion.", serviceId);
        // Run the test
        final MockHttpServletResponse destroyResponse = destroyService(serviceId);
        assertEquals(HttpStatus.BAD_REQUEST.value(), destroyResponse.getStatus());
        OrderFailedResponse orderFailedResponse = objectMapper.readValue(
                destroyResponse.getContentAsString(), OrderFailedResponse.class);
        assertFalse(orderFailedResponse.getSuccess());
        assertEquals(orderFailedResponse.getResultType(), ResultType.SERVICE_LOCKED);
        assertEquals(orderFailedResponse.getDetails(), List.of(errorMsg));
        assertEquals(orderFailedResponse.getServiceId(), serviceId.toString());
    }

    void testModifyThrowsServiceLockedException(UUID serviceId) throws Exception {
        // SetUp
        String errorMsg = String.format("Service %s is locked from modification.", serviceId);
        ModifyRequest modifyRequest = new ModifyRequest();
        modifyRequest.setFlavor("flavor-error");
        // Run the test
        final MockHttpServletResponse modifyResponse =
                modifyService(serviceId, modifyRequest);
        assertEquals(HttpStatus.BAD_REQUEST.value(), modifyResponse.getStatus());
        OrderFailedResponse orderFailedResponse = objectMapper.readValue(
                modifyResponse.getContentAsString(), OrderFailedResponse.class);
        assertFalse(orderFailedResponse.getSuccess());
        assertEquals(orderFailedResponse.getResultType(), ResultType.SERVICE_LOCKED);
        assertEquals(orderFailedResponse.getDetails(), List.of(errorMsg));
        assertEquals(orderFailedResponse.getServiceId(), serviceId.toString());
    }

    void testDeployThrowsServiceTemplateNotApproved(ServiceTemplateDetailVo serviceTemplate)
            throws Exception {
        // SetUp
        String errorMsg = String.format("Found service template with id %s but not approved.",
                serviceTemplate.getServiceTemplateId());
        DeployRequest deployRequest = getDeployRequest(serviceTemplate);
        // Run the test
        final MockHttpServletResponse deployResponse = deployService(deployRequest);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), deployResponse.getStatus());
        OrderFailedResponse response = objectMapper.readValue(
                deployResponse.getContentAsString(), OrderFailedResponse.class);
        assertFalse(response.getSuccess());
        assertEquals(response.getResultType(), ResultType.SERVICE_TEMPLATE_NOT_APPROVED);
        assertEquals(response.getDetails(), List.of(errorMsg));
    }


    void testPurge(UUID serviceId) throws Exception {
        // Run the test
        final MockHttpServletResponse purgeResponse = purgeService(serviceId);
        assertEquals(HttpStatus.ACCEPTED.value(), purgeResponse.getStatus());
        ServiceOrder serviceOrder =
                objectMapper.readValue(purgeResponse.getContentAsString(), ServiceOrder.class);
        assertEquals(serviceId, serviceOrder.getServiceId());
        assertNotNull(serviceOrder.getOrderId());
        // SetUp
        String refuseMsg = String.format("Service with id %s not found.", serviceId);
        final MockHttpServletResponse detailsResponse = mockMvc.perform(
                        get("/xpanse/services/details/self_hosted/{serviceId}", serviceId))
                .andReturn().getResponse();

        assertEquals(HttpStatus.BAD_REQUEST.value(), detailsResponse.getStatus());
        Response response =
                objectMapper.readValue(detailsResponse.getContentAsString(), Response.class);
        assertFalse(response.getSuccess());
        assertEquals(response.getResultType(), ResultType.SERVICE_DEPLOYMENT_NOT_FOUND);
        assertEquals(response.getDetails(), List.of(refuseMsg));
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

    void testGetComputeResourcesOfService(UUID serviceId) throws Exception {
        MockHttpServletResponse getComputeResourcesResponse =
                getComputeResourceInventoryOfService(serviceId);
        assertEquals(HttpStatus.OK.value(), getComputeResourcesResponse.getStatus());
        List<DeployResource> deployResources =
                objectMapper.readValue(getComputeResourcesResponse.getContentAsString(),
                        new TypeReference<>() {
                        });
        assertTrue(deployResources.isEmpty());
    }

    List<DeployedServiceDetails> listDeployedServicesDetails(ServiceDeploymentState state)
            throws Exception {

        final MockHttpServletResponse listResponse = mockMvc.perform(
                        get("/xpanse/services/details").param("serviceState", state.toValue())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(HttpStatus.OK.value(), listResponse.getStatus());
        assertNotNull(listResponse.getHeader(HEADER_TRACKING_ID));
        return objectMapper.readValue(listResponse.getContentAsString(),
                new TypeReference<>() {
                });
    }

    void testDeployThrowsServiceTemplateNotRegistered() throws Exception {
        String errorMsg = "No available service templates found.";
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
        OrderFailedResponse response = objectMapper.readValue(
                deployResponse.getContentAsString(), OrderFailedResponse.class);
        assertFalse(response.getSuccess());
        assertEquals(response.getResultType(), ResultType.SERVICE_TEMPLATE_NOT_REGISTERED);
        assertEquals(response.getDetails(), List.of(errorMsg));
    }


    MockHttpServletResponse getServiceDetails(UUID serviceId) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(
                get("/xpanse/services/details/self_hosted/{serviceId}", serviceId)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();
        assertNotNull(response.getHeader(HEADER_TRACKING_ID));
        return response;
    }


    MockHttpServletResponse deployService(DeployRequest deployRequest) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(post("/xpanse/services")
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deployRequest))).andReturn()
                .getResponse();
        assertNotNull(response.getHeader(HEADER_TRACKING_ID));
        return response;
    }


    MockHttpServletResponse changeLockConfig(UUID serviceId, ServiceLockConfig lockConfig)
            throws Exception {
        MockHttpServletResponse response = mockMvc.perform(
                        put("/xpanse/services/changelock/{serviceId}", serviceId)
                                .content(objectMapper.writeValueAsString(lockConfig))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertNotNull(response.getHeader(HEADER_TRACKING_ID));
        return response;
    }

    MockHttpServletResponse modifyService(UUID serviceId, ModifyRequest modifyRequest)
            throws Exception {
        MockHttpServletResponse response = mockMvc.perform(
                        put("/xpanse/services/modify/{serviceId}", serviceId)
                                .content(objectMapper.writeValueAsString(modifyRequest))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertNotNull(response.getHeader(HEADER_TRACKING_ID));
        return response;
    }

    MockHttpServletResponse destroyService(UUID serviceId) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(
                delete("/xpanse/services/{serviceId}", serviceId)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();
        assertNotNull(response.getHeader(HEADER_TRACKING_ID));
        return response;
    }

    MockHttpServletResponse purgeService(UUID serviceId) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(
                delete("/xpanse/services/purge/{serviceId}", serviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        assertNotNull(response.getHeader(HEADER_TRACKING_ID));
        return response;
    }

    MockHttpServletResponse redeployService(UUID serviceId) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(
                put("/xpanse/services/deploy/retry/{serviceId}", serviceId)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();
        assertNotNull(response.getHeader(HEADER_TRACKING_ID));
        return response;
    }

    MockHttpServletResponse getComputeResourceInventoryOfService(UUID serviceId) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(
                get("/xpanse/services/{serviceId}/resources/compute", serviceId)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();
        assertNotNull(response.getHeader(HEADER_TRACKING_ID));
        return response;
    }

}
