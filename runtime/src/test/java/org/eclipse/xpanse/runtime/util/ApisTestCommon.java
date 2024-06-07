package org.eclipse.xpanse.runtime.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import com.huaweicloud.sdk.bss.v2.BssClient;
import com.huaweicloud.sdk.bssintl.v2.BssintlClient;
import com.huaweicloud.sdk.bssintl.v2.model.ListOnDemandResourceRatingsRequest;
import com.huaweicloud.sdk.bssintl.v2.model.ListOnDemandResourceRatingsResponse;
import com.huaweicloud.sdk.core.exception.ClientRequestException;
import com.huaweicloud.sdk.core.invoker.SyncInvoker;
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
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.CreateCredential;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.view.DeployedService;
import org.eclipse.xpanse.modules.models.service.view.DeployedServiceDetails;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.ReviewRegistrationRequest;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceReviewResult;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.eclipse.xpanse.plugins.flexibleengine.common.FlexibleEngineClient;
import org.eclipse.xpanse.plugins.flexibleengine.monitor.constant.FlexibleEngineMonitorConstants;
import org.eclipse.xpanse.plugins.huaweicloud.common.HuaweiCloudClient;
import org.eclipse.xpanse.plugins.huaweicloud.monitor.constant.HuaweiCloudMonitorConstants;
import org.eclipse.xpanse.plugins.openstack.common.constants.OpenstackEnvironmentConstants;
import org.eclipse.xpanse.plugins.scs.common.constants.ScsEnvironmentConstants;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test base class.
 */
public class ApisTestCommon {

    protected static final ObjectMapper objectMapper = new ObjectMapper();
    @Resource
    protected MockMvc mockMvc;
    @MockBean
    protected HuaweiCloudClient huaweiCloudClient;
    @MockBean
    protected FlexibleEngineClient flexibleEngineClient;
    @MockBean
    protected EcsClient mockEcsClient;
    @MockBean
    protected VpcClient mockVpcClient;
    @MockBean
    protected EipClient mockEipClient;
    @MockBean
    protected EvsClient mockEvsClient;
    @MockBean
    protected IamClient mockIamClient;
    @MockBean
    protected BssClient mockBssClient;
    @MockBean
    protected BssintlClient mockBssintlClient;
    protected MockedStatic<OSFactory> mockOsFactory;

    @BeforeAll
    static void configureObjectMapper() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new SimpleModule().addSerializer(OffsetDateTime.class,
                OffsetDateTimeSerializer.INSTANCE));
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);
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
        when(osClientBuilder.credentials(anyString(), anyString(),
                any(Identifier.class))).thenReturn(osClientBuilder);
        when(osClientBuilder.scopeToProject(any(Identifier.class),
                any(Identifier.class))).thenReturn(osClientBuilder);
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
        when(osClient.networking().securitygroup()).thenReturn(
                mock(SecurityGroupServiceImpl.class));
        // mock SecurityGroupRuleServiceImpl for osClient.networking().securityrule()
        when(osClient.networking().securityrule()).thenReturn(
                mock(SecurityGroupRuleServiceImpl.class));
        // mock NetFloatingIPServiceImpl for osClient.networking().floatingip()
        when(osClient.networking().floatingip()).thenReturn(mock(NetFloatingIPService.class));
        // mock AvailabilityZoneServiceImpl for osClient.networking().availabilityzone()
        when(osClient.networking().availabilityzone()).thenReturn(
                mock(AvailabilityZoneServiceImpl.class));

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

    protected boolean waitUntilExceptedState(UUID id, ServiceDeploymentState targetState)
            throws Exception {
        final long endTime = System.nanoTime() + TimeUnit.MINUTES.toNanos(1);
        while (true) {
            DeployedService deployedService = getDeployedServiceDetails(id);
            if (Objects.nonNull(deployedService)
                    && deployedService.getServiceDeploymentState() == targetState) {
                return true;
            }
            if (System.nanoTime() > endTime) {
                return false;
            }
            Thread.sleep(TimeUnit.SECONDS.toMillis(5));
        }
    }

    protected UUID deployService(ServiceTemplateDetailVo serviceTemplateDetailVo) throws Exception {
        DeployRequest deployRequest = getDeployRequest(serviceTemplateDetailVo);
        // Run the test
        final MockHttpServletResponse deployResponse = mockMvc.perform(
                        post("/xpanse/services").contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(deployRequest))).andReturn()
                .getResponse();
        UUID taskId = objectMapper.readValue(deployResponse.getContentAsString(), UUID.class);
        // Verify the results
        assertEquals(HttpStatus.ACCEPTED.value(), deployResponse.getStatus());
        Assertions.assertNotNull(taskId);
        return taskId;
    }

    protected DeployedServiceDetails getDeployedServiceDetails(UUID serviceId) throws Exception {
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

    protected void addCredentialForHuaweiCloud() throws Exception {
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
        createCredential.setTimeToLive(300);
        addUserCredential(createCredential);
    }

    protected void addCredentialForFlexibleEngine() throws Exception {
        final CreateCredential createCredential = new CreateCredential();
        createCredential.setCsp(Csp.FLEXIBLE_ENGINE);
        createCredential.setType(CredentialType.VARIABLES);
        createCredential.setName("AK_SK");
        createCredential.setDescription("description");
        List<CredentialVariable> credentialVariables = new ArrayList<>();
        credentialVariables.add(new CredentialVariable(FlexibleEngineMonitorConstants.OS_ACCESS_KEY,
                "The access key.", true, false, "AK_VALUE"));
        credentialVariables.add(new CredentialVariable(FlexibleEngineMonitorConstants.OS_SECRET_KEY,
                "The security key.", true, false, "SK_VALUE"));
        createCredential.setVariables(credentialVariables);
        createCredential.setTimeToLive(300);
        addUserCredential(createCredential);
    }

    protected void addCredentialForOpenstack() throws Exception {

        final CreateCredential createCredential = new CreateCredential();
        createCredential.setCsp(Csp.OPENSTACK);
        createCredential.setType(CredentialType.VARIABLES);
        createCredential.setName("USERNAME_PASSWORD");
        createCredential.setDescription("description");
        List<CredentialVariable> credentialVariables = new ArrayList<>();
        credentialVariables.add(new CredentialVariable(OpenstackEnvironmentConstants.PROJECT,
                "The Name of the Tenant or Project to use.", true, false, "PROJECT"));
        credentialVariables.add(new CredentialVariable(OpenstackEnvironmentConstants.USERNAME,
                "The Username to login with.", true, false, "USERNAME"));
        credentialVariables.add(new CredentialVariable(OpenstackEnvironmentConstants.PASSWORD,
                "The Password to login with.", true, true, "PASSWORD"));
        credentialVariables.add(new CredentialVariable(OpenstackEnvironmentConstants.USER_DOMAIN,
                "The domain to which the openstack user is linked to.", true, false,
                "USER_DOMAIN"));
        credentialVariables.add(new CredentialVariable(OpenstackEnvironmentConstants.PROJECT_DOMAIN,
                "The domain to which the openstack project is linked to.", true, false,
                "PROJECT_DOMAIN"));
        createCredential.setVariables(credentialVariables);
        createCredential.setTimeToLive(300);
        addUserCredential(createCredential);
    }


    protected void addCredentialForScs() throws Exception {

        final CreateCredential createCredential = new CreateCredential();
        createCredential.setCsp(Csp.SCS);
        createCredential.setType(CredentialType.VARIABLES);
        createCredential.setName("USERNAME_PASSWORD");
        createCredential.setDescription("description");
        List<CredentialVariable> credentialVariables = new ArrayList<>();
        credentialVariables.add(new CredentialVariable(ScsEnvironmentConstants.PROJECT,
                "The Name of the Tenant or Project to use.", true, false, "PROJECT"));
        credentialVariables.add(new CredentialVariable(ScsEnvironmentConstants.USERNAME,
                "The Username to login with.", true, false, "USERNAME"));
        credentialVariables.add(new CredentialVariable(ScsEnvironmentConstants.PASSWORD,
                "The Password to login with.", true, true, "PASSWORD"));
        credentialVariables.add(new CredentialVariable(ScsEnvironmentConstants.DOMAIN,
                "The domain of the SCS installation to be used.", true, false, "DOMAIN"));
        createCredential.setVariables(credentialVariables);
        createCredential.setTimeToLive(300);
        addUserCredential(createCredential);
    }

    protected void addUserCredential(CreateCredential createCredential) throws Exception {
        mockMvc.perform(post("/xpanse/user/credentials").content(
                                objectMapper.writeValueAsString(createCredential))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }

    protected ServiceTemplateDetailVo registerServiceTemplate(Ocl ocl) throws Exception {

        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        String requestBody = yamlMapper.writeValueAsString(ocl);
        final MockHttpServletResponse registerResponse = mockMvc.perform(
                        post("/xpanse/service_templates").content(requestBody)
                                .contentType("application/x-yaml").accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        try {
            return objectMapper.readValue(registerResponse.getContentAsString(),
                    ServiceTemplateDetailVo.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    protected void approveServiceTemplateRegistration(UUID id) throws Exception {
        ReviewRegistrationRequest request = new ReviewRegistrationRequest();
        request.setReviewResult(ServiceReviewResult.APPROVED);
        request.setReviewComment("Approved");
        String requestBody = objectMapper.writeValueAsString(request);
        mockMvc.perform(put("/xpanse/service_templates/review/{id}", id).content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }

    protected void unregisterServiceTemplate(UUID id) throws Exception {
        mockMvc.perform(
                        delete("/xpanse/service_templates/{id}", id).accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
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
        serviceTemplate.getDeployment().getVariables().forEach(
                variable -> serviceRequestProperties.put(variable.getName(),
                        variable.getExample()));
        serviceRequestProperties.put("admin_passwd", "111111111@Qq");
        deployRequest.setServiceRequestProperties(serviceRequestProperties);

        Map<String, String> availabilityZones = new HashMap<>();
        serviceTemplate.getDeployment().getServiceAvailability().forEach(
                availabilityZoneConfig -> availabilityZones.put(availabilityZoneConfig.getVarName(),
                        availabilityZoneConfig.getDisplayName()));
        deployRequest.setAvailabilityZones(availabilityZones);
        return deployRequest;
    }

    protected void deleteCredential(Csp csp, CredentialType type, String name) throws Exception {
        mockMvc.perform(delete("/xpanse/user/credentials").param("cspName", csp.toValue())
                .param("type", type.toValue()).param("name", name)
                .accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
    }

    protected void mockListOnDemandResourceRatingsInvokerWithBssintlClientThrowAccessDeniedException() {
        ClientRequestException clientRequestException = new ClientRequestException(403, "CBC.0150"
                , "Access Denied", UUID.randomUUID().toString());
        SyncInvoker<ListOnDemandResourceRatingsRequest, ListOnDemandResourceRatingsResponse>
                mockInvoker = mock(SyncInvoker.class);
        when(mockBssintlClient.listOnDemandResourceRatingsInvoker(any())).thenReturn(mockInvoker);
        when(mockInvoker.retryTimes(anyInt())).thenReturn(mockInvoker);
        when(mockInvoker.retryCondition(any())).thenReturn(mockInvoker);
        when(mockInvoker.backoffStrategy(any())).thenReturn(mockInvoker);
        when(mockInvoker.invoke()).thenThrow(clientRequestException);
    }
}


