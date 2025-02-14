package org.eclipse.xpanse.runtime.util;

import static org.eclipse.xpanse.modules.logging.LoggingKeyConstant.HEADER_TRACKING_ID;
import static org.eclipse.xpanse.plugins.openstack.common.auth.constants.OpenstackCommonEnvironmentConstants.OPENSTACK_TESTLAB_AUTH_URL;
import static org.eclipse.xpanse.plugins.openstack.common.auth.constants.OpenstackCommonEnvironmentConstants.PLUS_SERVER_AUTH_URL;
import static org.eclipse.xpanse.plugins.openstack.common.auth.constants.OpenstackCommonEnvironmentConstants.REGIO_CLOUD_AUTH_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import com.huaweicloud.sdk.bss.v2.BssClient;
import com.huaweicloud.sdk.bssintl.v2.BssintlClient;
import com.huaweicloud.sdk.ecs.v2.EcsClient;
import com.huaweicloud.sdk.eip.v2.EipClient;
import com.huaweicloud.sdk.evs.v2.EvsClient;
import com.huaweicloud.sdk.iam.v3.IamClient;
import com.huaweicloud.sdk.vpc.v2.VpcClient;
import jakarta.annotation.Resource;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentStorage;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderStorage;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.database.servicetemplaterequest.ServiceTemplateRequestHistoryStorage;
import org.eclipse.xpanse.modules.deployment.DeployService;
import org.eclipse.xpanse.modules.deployment.ServiceOrderManager;
import org.eclipse.xpanse.modules.deployment.ServiceResultReFetchManager;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.TofuMakerResultRefetchManager;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.TerraBootResultRefetchManager;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.CreateCredential;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.service.deployment.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deployment.DeploymentStatusUpdate;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrderStatusUpdate;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.ReviewServiceTemplateRequest;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceReviewResult;
import org.eclipse.xpanse.modules.models.servicetemplate.request.ServiceTemplateRequestInfo;
import org.eclipse.xpanse.modules.models.servicetemplate.request.ServiceTemplateRequestToReview;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.eclipse.xpanse.plugins.flexibleengine.common.FlexibleEngineClient;
import org.eclipse.xpanse.plugins.flexibleengine.monitor.constant.FlexibleEngineMonitorConstants;
import org.eclipse.xpanse.plugins.huaweicloud.common.HuaweiCloudClient;
import org.eclipse.xpanse.plugins.huaweicloud.monitor.constant.HuaweiCloudMonitorConstants;
import org.eclipse.xpanse.plugins.openstack.common.auth.constants.OpenstackCommonEnvironmentConstants;
import org.eclipse.xpanse.runtime.testContainers.ZitadelTestContainer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.MockedStatic;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.client.IOSClientBuilder;
import org.openstack4j.api.networking.NetFloatingIPService;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.openstack.OSFactory;
import org.openstack4j.openstack.compute.internal.ComputeServiceImpl;
import org.openstack4j.openstack.compute.internal.KeypairServiceImpl;
import org.openstack4j.openstack.compute.internal.ServerServiceImpl;
import org.openstack4j.openstack.networking.internal.AvailabilityZoneServiceImpl;
import org.openstack4j.openstack.networking.internal.NetworkServiceImpl;
import org.openstack4j.openstack.networking.internal.NetworkingServiceImpl;
import org.openstack4j.openstack.networking.internal.SecurityGroupRuleServiceImpl;
import org.openstack4j.openstack.networking.internal.SecurityGroupServiceImpl;
import org.openstack4j.openstack.networking.internal.SubnetServiceImpl;
import org.openstack4j.openstack.storage.block.internal.BlockStorageServiceImpl;
import org.openstack4j.openstack.storage.block.internal.BlockVolumeServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.request.async.DeferredResult;

/** Test base class. */
@Slf4j
public class ApisTestCommon {

    protected static final ObjectMapper objectMapper = new ObjectMapper();
    protected final OclLoader oclLoader = new OclLoader();
    @Resource public DeployService deployService;
    @Resource public ServiceOrderManager serviceOrderManager;
    @Resource protected ServiceDeploymentStorage serviceDeploymentStorage;
    @Resource protected ServiceTemplateStorage serviceTemplateStorage;
    @Resource protected ServiceTemplateRequestHistoryStorage serviceTemplateRequestHistoryStorage;
    @Resource protected ServiceOrderStorage serviceOrderStorage;
    @Resource protected MockMvc mockMvc;
    @MockitoBean protected HuaweiCloudClient huaweiCloudClient;
    @MockitoBean protected FlexibleEngineClient flexibleEngineClient;
    @MockitoBean protected EcsClient mockEcsClient;
    @MockitoBean protected VpcClient mockVpcClient;
    @MockitoBean protected EipClient mockEipClient;
    @MockitoBean protected EvsClient mockEvsClient;
    @MockitoBean protected IamClient mockIamClient;
    @MockitoBean protected BssClient mockBssClient;
    @MockitoBean protected BssintlClient mockBssintlClient;
    protected MockedStatic<OSFactory> mockOsFactory;
    @Resource private TerraBootResultRefetchManager terraBootResultRefetchManager;
    @Resource private TofuMakerResultRefetchManager tofuMakerResultRefetchManager;
    @Resource private ServiceResultReFetchManager serviceResultReFetchManager;

    @BeforeAll
    static void configureObjectMapper() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(
                new SimpleModule()
                        .addSerializer(OffsetDateTime.class, OffsetDateTimeSerializer.INSTANCE));
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);
    }

    @BeforeAll
    static void setEnvVar() {
        System.setProperty(OPENSTACK_TESTLAB_AUTH_URL, "http://127.0.0.1/identity/v3");
        System.setProperty(PLUS_SERVER_AUTH_URL, "http://127.0.0.1/identity/v3");
        System.setProperty(REGIO_CLOUD_AUTH_URL, "http://127.0.0.1/identity/v3");
    }

    @BeforeAll
    static void setupZitadel() {
        ZitadelTestContainer.setup();
        System.out.println("Using Zitadel URL: " + System.getProperty("zitadel.url"));
    }

    protected void mockSdkClientsForHuaweiCloud() {
        when(huaweiCloudClient.getEvsClient(any(), any())).thenReturn(mockEvsClient);
        when(huaweiCloudClient.getEipClient(any(), any())).thenReturn(mockEipClient);
        when(huaweiCloudClient.getEcsClient(any(), any())).thenReturn(mockEcsClient);
        when(huaweiCloudClient.getVpcClient(any(), any())).thenReturn(mockVpcClient);
        when(huaweiCloudClient.getIamClient(any(), any())).thenReturn(mockIamClient);
        when(huaweiCloudClient.getBssintlClient(any())).thenReturn(mockBssintlClient);
        when(huaweiCloudClient.getBssClient(any())).thenReturn(mockBssClient);
    }

    protected void mockSdkClientsForFlexibleEngine() {
        when(flexibleEngineClient.getEvsClient(any(), any())).thenReturn(mockEvsClient);
        when(flexibleEngineClient.getEipClient(any(), any())).thenReturn(mockEipClient);
        when(flexibleEngineClient.getEcsClient(any(), any())).thenReturn(mockEcsClient);
        when(flexibleEngineClient.getVpcClient(any(), any())).thenReturn(mockVpcClient);
    }

    protected OSClient.OSClientV3 getMockOsClientWithMockServices() {
        // mock OSClientV3
        IOSClientBuilder.V3 osClientBuilder = mock(IOSClientBuilder.V3.class);
        when(OSFactory.builderV3()).thenReturn(osClientBuilder);
        when(osClientBuilder.withConfig(any())).thenReturn(osClientBuilder);
        when(osClientBuilder.credentials(anyString(), anyString(), any(Identifier.class)))
                .thenReturn(osClientBuilder);
        when(osClientBuilder.scopeToProject(any(Identifier.class), any(Identifier.class)))
                .thenReturn(osClientBuilder);
        when(osClientBuilder.endpoint(any())).thenReturn(osClientBuilder);
        OSClient.OSClientV3 osClient = mock(OSClient.OSClientV3.class);
        when(osClientBuilder.authenticate()).thenReturn(osClient);
        when(osClient.useRegion(any())).thenReturn(osClient);

        // mock NetworkingServiceImpl for osClient.networking()
        when(osClient.networking()).thenReturn(mock(NetworkingServiceImpl.class));
        // mock NetworkServiceImpl for osClient.networking().network()
        when(osClient.networking().network()).thenReturn(mock(NetworkServiceImpl.class));
        // mock mockSubnetServiceImpl for osClient.networking().subnet()
        when(osClient.networking().subnet()).thenReturn(mock(SubnetServiceImpl.class));
        // mock SecurityGroupServiceImpl for osClient.networking().securitygroup()
        when(osClient.networking().securitygroup())
                .thenReturn(mock(SecurityGroupServiceImpl.class));
        // mock SecurityGroupRuleServiceImpl for osClient.networking().securityrule()
        when(osClient.networking().securityrule())
                .thenReturn(mock(SecurityGroupRuleServiceImpl.class));
        // mock NetFloatingIPServiceImpl for osClient.networking().floatingip()
        when(osClient.networking().floatingip()).thenReturn(mock(NetFloatingIPService.class));
        // mock AvailabilityZoneServiceImpl for osClient.networking().availabilityzone()
        when(osClient.networking().availabilityzone())
                .thenReturn(mock(AvailabilityZoneServiceImpl.class));

        // mock BlockStorageServiceImpl for osClient.blockStorage()
        when(osClient.blockStorage()).thenReturn(mock(BlockStorageServiceImpl.class));
        // mock BlockVolumeServiceImpl for osClient.blockStorage().volumes()
        when(osClient.blockStorage().volumes()).thenReturn(mock(BlockVolumeServiceImpl.class));

        // mock ComputeServiceImpl for osClient.compute()
        when(osClient.compute()).thenReturn(mock(ComputeServiceImpl.class));
        // mock ServerServiceImpl for osClient.compute().servers()
        when(osClient.compute().servers()).thenReturn(mock(ServerServiceImpl.class));
        // mock ComputeServiceImpl for osClient.compute().keypairs()
        when(osClient.compute().keypairs()).thenReturn(mock(KeypairServiceImpl.class));
        return osClient;
    }

    protected ServiceOrder deployService(ServiceTemplateDetailVo serviceTemplate) throws Exception {
        DeployRequest deployRequest = getDeployRequest(serviceTemplate);
        // Run the test
        final MockHttpServletResponse deployResponse =
                mockMvc.perform(
                                post("/xpanse/services")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(deployRequest)))
                        .andReturn()
                        .getResponse();
        ServiceOrder serviceOrder =
                objectMapper.readValue(deployResponse.getContentAsString(), ServiceOrder.class);
        // Verify the results
        assertEquals(HttpStatus.ACCEPTED.value(), deployResponse.getStatus());
        Assertions.assertNotNull(serviceOrder.getServiceId());
        Assertions.assertNotNull(serviceOrder.getOrderId());
        return serviceOrder;
    }

    protected boolean waitServiceDeploymentIsCompleted(UUID serviceId) throws Exception {
        final long endTime = System.nanoTime() + TimeUnit.MINUTES.toNanos(2);
        while (System.nanoTime() < endTime) {
            DeploymentStatusUpdate deploymentStatusUpdate =
                    getLatestServiceDeploymentStatus(serviceId);
            if (Objects.nonNull(deploymentStatusUpdate)) {
                if (deploymentStatusUpdate.getIsOrderCompleted()) {
                    return true;
                }
            }
            Thread.sleep(5000);
        }
        return false;
    }

    protected DeploymentStatusUpdate getLatestServiceDeploymentStatus(UUID serviceId)
            throws InterruptedException {
        DeferredResult<DeploymentStatusUpdate> deferredResult =
                deployService.getLatestServiceDeploymentStatus(serviceId, null);
        while (Objects.isNull(deferredResult.getResult())) {
            Thread.sleep(1000);
        }
        if (deferredResult.getResult() instanceof DeploymentStatusUpdate statusUpdate) {
            return statusUpdate;
        }
        return null;
    }

    protected boolean waitServiceOrderIsCompleted(UUID orderId) throws Exception {
        final long endTime = System.nanoTime() + TimeUnit.MINUTES.toNanos(2);
        while (System.nanoTime() < endTime) {
            ServiceOrderStatusUpdate serviceOrderStatusUpdate =
                    getLatestServiceOrderStatus(orderId, null);
            if (Objects.nonNull(serviceOrderStatusUpdate)) {
                if (serviceOrderStatusUpdate.getIsOrderCompleted()) {
                    return true;
                }
            }
            Thread.sleep(5000);
        }
        return false;
    }

    protected ServiceOrderStatusUpdate getLatestServiceOrderStatus(
            UUID orderId, TaskStatus taskStatus) throws InterruptedException {
        DeferredResult<ServiceOrderStatusUpdate> deferredResult =
                serviceOrderManager.getLatestServiceOrderStatus(orderId, taskStatus);
        while (Objects.isNull(deferredResult.getResult())) {
            Thread.sleep(1000);
        }
        if (deferredResult.getResult() instanceof ServiceOrderStatusUpdate statusUpdate) {
            return statusUpdate;
        }
        return null;
    }

    protected void addCredentialForHuaweiCloud() throws Exception {
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
        createCredential.setTimeToLive(300);
        addUserCredential(createCredential);
    }

    protected void addCredentialForFlexibleEngine() throws Exception {
        final CreateCredential createCredential = new CreateCredential();
        createCredential.setCsp(Csp.FLEXIBLE_ENGINE);
        createCredential.setSite("default");
        createCredential.setType(CredentialType.VARIABLES);
        createCredential.setName("AK_SK");
        createCredential.setDescription("description");
        List<CredentialVariable> credentialVariables = new ArrayList<>();
        credentialVariables.add(
                new CredentialVariable(
                        FlexibleEngineMonitorConstants.OS_ACCESS_KEY,
                        "The access key.",
                        true,
                        false,
                        "AK_VALUE"));
        credentialVariables.add(
                new CredentialVariable(
                        FlexibleEngineMonitorConstants.OS_SECRET_KEY,
                        "The security key.",
                        true,
                        false,
                        "SK_VALUE"));
        createCredential.setVariables(credentialVariables);
        createCredential.setTimeToLive(300);
        addUserCredential(createCredential);
    }

    protected void addCredentialForOpenstack(Csp csp) throws Exception {

        final CreateCredential createCredential = new CreateCredential();
        createCredential.setCsp(csp);
        createCredential.setSite("default");
        createCredential.setType(CredentialType.VARIABLES);
        createCredential.setName("USERNAME_PASSWORD");
        createCredential.setDescription("description");
        List<CredentialVariable> credentialVariables = new ArrayList<>();
        credentialVariables.add(
                new CredentialVariable(
                        OpenstackCommonEnvironmentConstants.PROJECT,
                        "The Name of the Tenant or Project to use.",
                        true,
                        false,
                        "PROJECT"));
        credentialVariables.add(
                new CredentialVariable(
                        OpenstackCommonEnvironmentConstants.USERNAME,
                        "The Username to login with.",
                        true,
                        false,
                        "USERNAME"));
        credentialVariables.add(
                new CredentialVariable(
                        OpenstackCommonEnvironmentConstants.PASSWORD,
                        "The Password to login with.",
                        true,
                        true,
                        "PASSWORD"));
        credentialVariables.add(
                new CredentialVariable(
                        OpenstackCommonEnvironmentConstants.DOMAIN,
                        "The domain to which the openstack user is linked to.",
                        true,
                        false,
                        "DOMAIN"));
        credentialVariables.add(
                new CredentialVariable(
                        OpenstackCommonEnvironmentConstants.USER_DOMAIN,
                        "The domain to which the openstack user is linked to.",
                        true,
                        false,
                        "USER_DOMAIN"));
        credentialVariables.add(
                new CredentialVariable(
                        OpenstackCommonEnvironmentConstants.PROJECT_DOMAIN,
                        "The domain to which the openstack project is linked to.",
                        true,
                        false,
                        "PROJECT_DOMAIN"));
        createCredential.setVariables(credentialVariables);
        createCredential.setTimeToLive(300);
        addUserCredential(createCredential);
    }

    protected void addUserCredential(CreateCredential createCredential) throws Exception {
        MockHttpServletResponse response =
                mockMvc.perform(
                                post("/xpanse/user/credentials")
                                        .content(objectMapper.writeValueAsString(createCredential))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();
        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatus());
        assertNotNull(response.getHeader(HEADER_TRACKING_ID));
    }

    protected ServiceTemplateDetailVo registerServiceTemplateAndApproveRegistration(Ocl ocl)
            throws Exception {
        ServiceTemplateRequestInfo registerRequestInfo = registerServiceTemplate(ocl);
        if (Objects.nonNull(registerRequestInfo)) {
            if (reviewServiceTemplateRequest(registerRequestInfo.getRequestId(), true)) {
                return getServiceTemplateDetailsVo(registerRequestInfo.getServiceTemplateId());
            } else {
                log.error("Review service template register request failed.");
            }
        } else {
            log.error("Register service template failed.");
        }
        return null;
    }

    protected ServiceTemplateRequestInfo registerServiceTemplate(Ocl ocl) throws Exception {
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        ocl.setName(UUID.randomUUID().toString());
        String requestBody = yamlMapper.writeValueAsString(ocl);
        final MockHttpServletResponse registerResponse =
                mockMvc.perform(
                                post("/xpanse/service_templates")
                                        .content(requestBody)
                                        .contentType("application/x-yaml")
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();
        assertNotNull(registerResponse.getHeader(HEADER_TRACKING_ID));
        if (registerResponse.getStatus() == HttpStatus.OK.value()) {
            return objectMapper.readValue(
                    registerResponse.getContentAsString(), ServiceTemplateRequestInfo.class);

        } else {
            ErrorResponse errorResponse =
                    objectMapper.readValue(
                            registerResponse.getContentAsString(), ErrorResponse.class);
            log.error("Register service template failed. Error: " + errorResponse.getDetails());
            return null;
        }
    }

    protected boolean reviewServiceTemplateRequest(UUID requestId, boolean isApproved)
            throws Exception {
        ReviewServiceTemplateRequest request = new ReviewServiceTemplateRequest();
        ServiceReviewResult reviewResult =
                isApproved ? ServiceReviewResult.APPROVED : ServiceReviewResult.REJECTED;
        request.setReviewResult(reviewResult);
        request.setReviewComment(reviewResult.toValue());
        String requestBody = objectMapper.writeValueAsString(request);
        MockHttpServletResponse response =
                mockMvc.perform(
                                put(
                                                "/xpanse/csp/service_templates/requests/review/{requestId}",
                                                requestId)
                                        .content(requestBody)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();
        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatus());
        return response.getStatus() == HttpStatus.NO_CONTENT.value();
    }

    protected ServiceTemplateDetailVo getServiceTemplateDetailsVo(UUID id) throws Exception {
        MockHttpServletResponse response =
                mockMvc.perform(
                                get("/xpanse/service_templates/{id}", id)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertNotNull(response.getHeader(HEADER_TRACKING_ID));
        return objectMapper.readValue(response.getContentAsString(), ServiceTemplateDetailVo.class);
    }

    protected List<ServiceTemplateRequestToReview> listPendingServiceTemplateRequests(
            UUID serviceTemplateId) throws Exception {
        MockHttpServletRequestBuilder getRequestBuilder =
                get("/xpanse/csp/service_templates/requests/pending");
        if (Objects.nonNull(serviceTemplateId)) {
            getRequestBuilder =
                    getRequestBuilder.param("serviceTemplateId", serviceTemplateId.toString());
        }
        MockHttpServletResponse response =
                mockMvc.perform(getRequestBuilder.accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();
        return objectMapper.readValue(response.getContentAsString(), new TypeReference<>() {});
    }

    protected void deleteServiceTemplate(UUID serviceTemplateId) throws Exception {
        serviceTemplateStorage.deleteServiceTemplate(
                serviceTemplateStorage.getServiceTemplateById(serviceTemplateId));
    }

    protected void deleteServiceDeployment(UUID serviceId) {
        ServiceDeploymentEntity deployedService =
                serviceDeploymentStorage.findServiceDeploymentById(serviceId);
        if (Objects.nonNull(deployedService)) {
            serviceDeploymentStorage.deleteServiceDeployment(deployedService);
        }
    }

    protected DeployRequest getDeployRequest(ServiceTemplateDetailVo serviceTemplate) {
        DeployRequest deployRequest = new DeployRequest();
        deployRequest.setServiceName(serviceTemplate.getName());
        deployRequest.setVersion(serviceTemplate.getVersion());
        deployRequest.setCsp(serviceTemplate.getCsp());
        deployRequest.setCategory(serviceTemplate.getCategory());
        deployRequest.setFlavor(
                serviceTemplate.getFlavors().getServiceFlavors().getFirst().getName());
        deployRequest.setRegion(serviceTemplate.getRegions().getFirst());
        deployRequest.setServiceHostingType(serviceTemplate.getServiceHostingType());
        deployRequest.setBillingMode(serviceTemplate.getBilling().getBillingModes().getFirst());

        Map<String, Object> serviceRequestProperties = new HashMap<>();
        serviceTemplate
                .getDeployment()
                .getVariables()
                .forEach(
                        variable ->
                                serviceRequestProperties.put(
                                        variable.getName(), variable.getExample()));
        serviceRequestProperties.put("admin_passwd", "111111111@Qq");
        deployRequest.setServiceRequestProperties(serviceRequestProperties);

        Map<String, String> availabilityZones = new HashMap<>();
        serviceTemplate
                .getDeployment()
                .getServiceAvailabilityConfig()
                .forEach(
                        availabilityZoneConfig ->
                                availabilityZones.put(
                                        availabilityZoneConfig.getVarName(),
                                        availabilityZoneConfig.getDisplayName()));
        deployRequest.setAvailabilityZones(availabilityZones);
        return deployRequest;
    }

    protected void deleteCredential(Csp csp, String site, CredentialType type, String name)
            throws Exception {
        MockHttpServletResponse response =
                mockMvc.perform(
                                delete("/xpanse/user/credentials")
                                        .param("cspName", csp.toValue())
                                        .param("siteName", site)
                                        .param("type", type.toValue())
                                        .param("name", name)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();
        assertNotNull(response.getHeader(HEADER_TRACKING_ID));
        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatus());
    }
}
