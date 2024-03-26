package org.eclipse.xpanse.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.huaweicloud.sdk.core.invoker.SyncInvoker;
import com.huaweicloud.sdk.ecs.v2.EcsClient;
import com.huaweicloud.sdk.ecs.v2.model.BatchRebootServersResponse;
import com.huaweicloud.sdk.ecs.v2.model.BatchStartServersResponse;
import com.huaweicloud.sdk.ecs.v2.model.BatchStopServersResponse;
import com.huaweicloud.sdk.ecs.v2.model.ShowJobResponse;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.resource.DeployResourceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceStorage;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceState;
import org.eclipse.xpanse.modules.models.service.view.DeployedService;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.plugins.flexibleengine.common.FlexibleEngineClient;
import org.eclipse.xpanse.plugins.huaweicloud.common.HuaweiCloudClient;
import org.eclipse.xpanse.runtime.util.ApisTestCommon;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.client.IOSClientBuilder;
import org.openstack4j.api.compute.ComputeService;
import org.openstack4j.api.compute.ServerService;
import org.openstack4j.model.common.ActionResponse;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.compute.Action;
import org.openstack4j.model.compute.RebootType;
import org.openstack4j.model.compute.Server;
import org.openstack4j.openstack.OSFactory;
import org.openstack4j.openstack.compute.internal.ServerServiceImpl;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=oauth,zitadel,zitadel-testbed",
        "OS_AUTH_URL=http://127.0.0.1/v3/identity"})
@AutoConfigureMockMvc
class ServiceStateManageApiTest extends ApisTestCommon {

    @MockBean
    private DeployServiceStorage deployServiceStorage;

    @BeforeEach
    void setUp() {
        mockOsFactory = mockStatic(OSFactory.class);
    }

    @AfterEach
    void tearDown() {
        mockOsFactory.close();
    }

    @Test
    @WithJwt(file = "jwt_user.json")
    void testServiceStateManageApisThrowsException() throws Exception {
        // Setup
        UUID uuid = UUID.randomUUID();
        Response errorResult = Response.errorResponse(ResultType.SERVICE_DEPLOYMENT_NOT_FOUND,
                Collections.singletonList(String.format("Service with id %s not found.", uuid)));
        // run the test
        final MockHttpServletResponse response = startService(uuid);
        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(objectMapper.writeValueAsString(errorResult)).isEqualTo(
                response.getContentAsString());

        // Setup
        DeployServiceEntity service1 = setUpWellDeployServiceEntity();
        service1.setUserId("1");
        Response errorResult1 = Response.errorResponse(ResultType.ACCESS_DENIED,
                Collections.singletonList("No permissions to manage status of the service "
                        + "belonging to other users."));
        when(deployServiceStorage.findDeployServiceById(service1.getId())).thenReturn(service1);
        // run the test
        final MockHttpServletResponse response1 = startService(service1.getId());
        // Verify the results
        assertThat(response1.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response1.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(errorResult1));

        // Setup
        DeployServiceEntity service2 = setUpWellDeployServiceEntity();
        service2.setServiceDeploymentState(ServiceDeploymentState.DEPLOYING);
        Response errorResult2 = Response.errorResponse(ResultType.SERVICE_STATE_INVALID,
                Collections.singletonList(
                        String.format("Service with id %s is %s.", service2.getId(),
                                service2.getServiceDeploymentState())));
        when(deployServiceStorage.findDeployServiceById(service2.getId())).thenReturn(service2);
        // run the test
        final MockHttpServletResponse response2 = startService(service2.getId());
        // Verify the results
        assertThat(response2.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response2.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(errorResult2));
    }

    @Test
    @WithJwt(file = "jwt_user.json")
    void testServiceStateManageApisFailedByErrorState() throws Exception {
        // Setup
        DeployServiceEntity service = setUpWellDeployServiceEntity();
        service.setServiceState(ServiceState.STARTING);
        when(deployServiceStorage.findDeployServiceById(service.getId())).thenReturn(service);
        // run the test
        final MockHttpServletResponse response = startService(service.getId());
        DeployedService returnedService =
                objectMapper.readValue(response.getContentAsString(), DeployedService.class);
        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(returnedService.getServiceState()).isEqualTo(ServiceState.STARTING_FAILED);

        // Setup
        DeployServiceEntity service1 = setUpWellDeployServiceEntity();
        service1.setServiceState(ServiceState.STOPPING);
        when(deployServiceStorage.findDeployServiceById(service1.getId())).thenReturn(service1);
        // run the test
        final MockHttpServletResponse response1 = stopService(service1.getId());
        DeployedService returnedService1 =
                objectMapper.readValue(response1.getContentAsString(), DeployedService.class);
        // Verify the results
        assertThat(response1.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(returnedService1.getServiceState()).isEqualTo(ServiceState.STOPPING_FAILED);

        // Setup
        DeployServiceEntity service2 = setUpWellDeployServiceEntity();
        service2.setCsp(Csp.AWS);
        when(deployServiceStorage.findDeployServiceById(service2.getId())).thenReturn(service2);
        // run the test
        final MockHttpServletResponse response2 = startService(service2.getId());
        DeployedService returnedService2 =
                objectMapper.readValue(response2.getContentAsString(), DeployedService.class);
        // Verify the results
        assertThat(response2.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(returnedService2.getServiceState()).isEqualTo(ServiceState.STARTING_FAILED);
    }

    @Test
    @WithJwt(file = "jwt_user.json")
    void testServiceStateManageApisForHuaweiCloud() throws Exception {
        // Setup
        DeployServiceEntity startService = setUpWellDeployServiceEntity();
        startService.setServiceState(ServiceState.STOPPED);
        when(deployServiceStorage.findDeployServiceById(startService.getId())).thenReturn(
                startService);
        addCredentialForHuaweiCloud();
        when(huaweiCloudClient.getEcsClient(any(), any())).thenReturn(mockEcsClient);
        BatchStartServersResponse startResponse = new BatchStartServersResponse();
        startResponse.setHttpStatusCode(200);
        startResponse.setJobId(UUID.randomUUID().toString());
        mockBatchStartServersInvoker(startResponse);

        ShowJobResponse startJobResponse = new ShowJobResponse();
        startJobResponse.setHttpStatusCode(200);
        startJobResponse.setStatus(ShowJobResponse.StatusEnum.SUCCESS);
        mockShowJobInvoker(startJobResponse);
        // Run the test
        final MockHttpServletResponse huaweiStartResponse = startService(startService.getId());
        DeployedService startedService =
                objectMapper.readValue(huaweiStartResponse.getContentAsString(),
                        DeployedService.class);
        // Verify the results
        assertThat(huaweiStartResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(startedService.getServiceState()).isEqualTo(ServiceState.RUNNING);

        ShowJobResponse startFailedJobResponse = new ShowJobResponse();
        startFailedJobResponse.setHttpStatusCode(200);
        startFailedJobResponse.setStatus(ShowJobResponse.StatusEnum.FAIL);

        mockShowJobInvoker(startFailedJobResponse);
        // Run the test
        final MockHttpServletResponse huaweiStartFailedResponse =
                startService(startService.getId());
        DeployedService startFailedService =
                objectMapper.readValue(huaweiStartFailedResponse.getContentAsString(),
                        DeployedService.class);
        // Verify the results
        assertThat(huaweiStartResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(startFailedService.getServiceState()).isEqualTo(
                ServiceState.STARTING_FAILED);


        // Setup
        DeployServiceEntity stopService = setUpWellDeployServiceEntity();
        stopService.setServiceState(ServiceState.RUNNING);
        when(deployServiceStorage.findDeployServiceById(stopService.getId())).thenReturn(
                stopService);
        addCredentialForHuaweiCloud();
        when(huaweiCloudClient.getEcsClient(any(), any())).thenReturn(mockEcsClient);
        BatchStopServersResponse stopResponse = new BatchStopServersResponse();
        stopResponse.setHttpStatusCode(200);
        stopResponse.setJobId(UUID.randomUUID().toString());
        mockBatchStopServersInvoker(stopResponse);

        ShowJobResponse stopJobResponse = new ShowJobResponse();
        stopJobResponse.setHttpStatusCode(200);
        stopJobResponse.setStatus(ShowJobResponse.StatusEnum.SUCCESS);
        mockShowJobInvoker(stopJobResponse);
        // Run the test
        final MockHttpServletResponse huaweiStopResponse = stopService(stopService.getId());
        DeployedService stoppedService =
                objectMapper.readValue(huaweiStopResponse.getContentAsString(),
                        DeployedService.class);
        // Verify the results
        assertThat(huaweiStopResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(stoppedService.getServiceState()).isEqualTo(ServiceState.STOPPED);

        ShowJobResponse stopFailedJobResponse = new ShowJobResponse();
        stopFailedJobResponse.setHttpStatusCode(200);
        stopFailedJobResponse.setStatus(ShowJobResponse.StatusEnum.FAIL);

        mockShowJobInvoker(stopFailedJobResponse);
        // Run the test
        final MockHttpServletResponse huaweiStopFailedResponse = stopService(stopService.getId());
        DeployedService stopFailedService =
                objectMapper.readValue(huaweiStopFailedResponse.getContentAsString(),
                        DeployedService.class);
        // Verify the results
        assertThat(huaweiStopResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(stopFailedService.getServiceState()).isEqualTo(
                ServiceState.STOPPING_FAILED);


        // Setup
        DeployServiceEntity rebootService = setUpWellDeployServiceEntity();
        rebootService.setServiceState(ServiceState.STOPPED);
        when(deployServiceStorage.findDeployServiceById(rebootService.getId())).thenReturn(
                rebootService);
        addCredentialForHuaweiCloud();
        when(huaweiCloudClient.getEcsClient(any(), any())).thenReturn(mockEcsClient);
        BatchRebootServersResponse rebootResponse = new BatchRebootServersResponse();
        rebootResponse.setHttpStatusCode(200);
        rebootResponse.setJobId(UUID.randomUUID().toString());
        mockBatchRebootServersInvoker(rebootResponse);

        ShowJobResponse rebootJobResponse = new ShowJobResponse();
        rebootJobResponse.setHttpStatusCode(200);
        rebootJobResponse.setStatus(ShowJobResponse.StatusEnum.SUCCESS);
        mockShowJobInvoker(rebootJobResponse);
        // Run the test
        final MockHttpServletResponse huaweiRebootResponse = restartService(rebootService.getId());
        DeployedService rebootedService =
                objectMapper.readValue(huaweiRebootResponse.getContentAsString(),
                        DeployedService.class);
        // Verify the results
        assertThat(huaweiRebootResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(rebootedService.getServiceState()).isEqualTo(ServiceState.RUNNING);

        ShowJobResponse rebootFailedJobResponse = new ShowJobResponse();
        rebootFailedJobResponse.setHttpStatusCode(200);
        rebootFailedJobResponse.setStatus(ShowJobResponse.StatusEnum.FAIL);

        mockShowJobInvoker(rebootFailedJobResponse);
        // Run the test
        final MockHttpServletResponse huaweiRebootFailedResponse =
                restartService(rebootService.getId());
        DeployedService rebootFailedService =
                objectMapper.readValue(huaweiRebootFailedResponse.getContentAsString(),
                        DeployedService.class);
        // Verify the results
        assertThat(huaweiRebootResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(rebootFailedService.getServiceState()).isEqualTo(
                ServiceState.STARTING_FAILED);

        deleteCredential(Csp.HUAWEI, CredentialType.VARIABLES, "AK_SK");

    }


    void mockBatchStartServersInvoker(BatchStartServersResponse startResponse) {
        SyncInvoker mockStartSyncInvoker = mock(SyncInvoker.class);
        when(mockEcsClient.batchStartServersInvoker(any())).thenReturn(mockStartSyncInvoker);
        when(mockStartSyncInvoker.retryTimes(anyInt())).thenReturn(mockStartSyncInvoker);
        when(mockStartSyncInvoker.retryCondition(any())).thenReturn(mockStartSyncInvoker);
        when(mockStartSyncInvoker.backoffStrategy(any())).thenReturn(mockStartSyncInvoker);
        when(mockStartSyncInvoker.invoke()).thenReturn(startResponse);
    }

    void mockBatchStopServersInvoker(BatchStopServersResponse stopResponse) {
        SyncInvoker mockStopSyncInvoker = mock(SyncInvoker.class);
        when(mockEcsClient.batchStopServersInvoker(any())).thenReturn(mockStopSyncInvoker);
        when(mockStopSyncInvoker.retryTimes(anyInt())).thenReturn(mockStopSyncInvoker);
        when(mockStopSyncInvoker.retryCondition(any())).thenReturn(mockStopSyncInvoker);
        when(mockStopSyncInvoker.backoffStrategy(any())).thenReturn(mockStopSyncInvoker);
        when(mockStopSyncInvoker.invoke()).thenReturn(stopResponse);
    }

    void mockBatchRebootServersInvoker(BatchRebootServersResponse rebootResponse) {
        SyncInvoker mockRebootSyncInvoker = mock(SyncInvoker.class);
        when(mockEcsClient.batchRebootServersInvoker(any())).thenReturn(mockRebootSyncInvoker);
        when(mockRebootSyncInvoker.retryTimes(anyInt())).thenReturn(
                mockRebootSyncInvoker);
        when(mockRebootSyncInvoker.retryCondition(any())).thenReturn(mockRebootSyncInvoker);
        when(mockRebootSyncInvoker.backoffStrategy(any())).thenReturn(mockRebootSyncInvoker);
        when(mockRebootSyncInvoker.invoke()).thenReturn(rebootResponse);
    }

    void mockShowJobInvoker(ShowJobResponse jobResponse) {
        SyncInvoker mockJobSyncInvoker = mock(SyncInvoker.class);
        when(mockEcsClient.showJobInvoker(any())).thenReturn(mockJobSyncInvoker);
        when(mockJobSyncInvoker.retryTimes(anyInt())).thenReturn(mockJobSyncInvoker);
        when(mockJobSyncInvoker.retryCondition(any())).thenReturn(mockJobSyncInvoker);
        when(mockJobSyncInvoker.backoffStrategy(any())).thenReturn(mockJobSyncInvoker);
        when(mockJobSyncInvoker.invoke()).thenReturn(jobResponse);
    }

    @Test
    @WithJwt(file = "jwt_user.json")
    void testServiceStateManageApisForFlexibleEngine() throws Exception {
        // Setup
        DeployServiceEntity startService = setUpWellDeployServiceEntity();
        startService.setCsp(Csp.FLEXIBLE_ENGINE);
        startService.setServiceState(ServiceState.STOPPED);
        when(deployServiceStorage.findDeployServiceById(startService.getId())).thenReturn(
                startService);
        addCredentialForFlexibleEngine();
        when(flexibleEngineClient.getEcsClient(any(), any())).thenReturn(mockEcsClient);
        BatchStartServersResponse startResponse = new BatchStartServersResponse();
        startResponse.setHttpStatusCode(200);
        startResponse.setJobId(UUID.randomUUID().toString());
        mockBatchStartServersInvoker(startResponse);

        ShowJobResponse startJobResponse = new ShowJobResponse();
        startJobResponse.setHttpStatusCode(200);
        startJobResponse.setStatus(ShowJobResponse.StatusEnum.SUCCESS);
        mockShowJobInvoker(startJobResponse);
        // Run the test
        final MockHttpServletResponse flexibleEngineStartResponse =
                startService(startService.getId());
        DeployedService startedService =
                objectMapper.readValue(flexibleEngineStartResponse.getContentAsString(),
                        DeployedService.class);
        // Verify the results
        assertThat(flexibleEngineStartResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(startedService.getServiceState()).isEqualTo(ServiceState.RUNNING);

        ShowJobResponse startFailedJobResponse = new ShowJobResponse();
        startFailedJobResponse.setHttpStatusCode(200);
        startFailedJobResponse.setStatus(ShowJobResponse.StatusEnum.FAIL);

        mockShowJobInvoker(startFailedJobResponse);
        // Run the test
        final MockHttpServletResponse flexibleEngineStartFailedResponse =
                startService(startService.getId());
        DeployedService startFailedService =
                objectMapper.readValue(flexibleEngineStartFailedResponse.getContentAsString(),
                        DeployedService.class);
        // Verify the results
        assertThat(flexibleEngineStartResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(startFailedService.getServiceState()).isEqualTo(
                ServiceState.STARTING_FAILED);


        // Setup
        DeployServiceEntity stopService = setUpWellDeployServiceEntity();
        stopService.setCsp(Csp.FLEXIBLE_ENGINE);
        stopService.setServiceState(ServiceState.RUNNING);
        when(deployServiceStorage.findDeployServiceById(stopService.getId())).thenReturn(
                stopService);
        addCredentialForFlexibleEngine();
        when(flexibleEngineClient.getEcsClient(any(), any())).thenReturn(mockEcsClient);
        BatchStopServersResponse stopResponse = new BatchStopServersResponse();
        stopResponse.setHttpStatusCode(200);
        stopResponse.setJobId(UUID.randomUUID().toString());
        mockBatchStopServersInvoker(stopResponse);

        ShowJobResponse stopJobResponse = new ShowJobResponse();
        stopJobResponse.setHttpStatusCode(200);
        stopJobResponse.setStatus(ShowJobResponse.StatusEnum.SUCCESS);
        mockShowJobInvoker(stopJobResponse);
        // Run the test
        final MockHttpServletResponse flexibleEngineStopResponse = stopService(stopService.getId());
        DeployedService stoppedService =
                objectMapper.readValue(flexibleEngineStopResponse.getContentAsString(),
                        DeployedService.class);
        // Verify the results
        assertThat(flexibleEngineStopResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(stoppedService.getServiceState()).isEqualTo(ServiceState.STOPPED);

        ShowJobResponse stopFailedJobResponse = new ShowJobResponse();
        stopFailedJobResponse.setHttpStatusCode(200);
        stopFailedJobResponse.setStatus(ShowJobResponse.StatusEnum.FAIL);

        mockShowJobInvoker(stopFailedJobResponse);
        // Run the test
        final MockHttpServletResponse flexibleEngineStopFailedResponse =
                stopService(stopService.getId());
        DeployedService stopFailedService =
                objectMapper.readValue(flexibleEngineStopFailedResponse.getContentAsString(),
                        DeployedService.class);
        // Verify the results
        assertThat(flexibleEngineStopResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(stopFailedService.getServiceState()).isEqualTo(
                ServiceState.STOPPING_FAILED);


        // Setup
        DeployServiceEntity rebootService = setUpWellDeployServiceEntity();
        rebootService.setServiceState(ServiceState.STOPPED);
        rebootService.setCsp(Csp.FLEXIBLE_ENGINE);
        when(deployServiceStorage.findDeployServiceById(rebootService.getId())).thenReturn(
                rebootService);
        addCredentialForFlexibleEngine();
        when(flexibleEngineClient.getEcsClient(any(), any())).thenReturn(mockEcsClient);
        BatchRebootServersResponse rebootResponse = new BatchRebootServersResponse();
        rebootResponse.setHttpStatusCode(200);
        rebootResponse.setJobId(UUID.randomUUID().toString());
        mockBatchRebootServersInvoker(rebootResponse);

        ShowJobResponse rebootJobResponse = new ShowJobResponse();
        rebootJobResponse.setHttpStatusCode(200);
        rebootJobResponse.setStatus(ShowJobResponse.StatusEnum.SUCCESS);
        mockShowJobInvoker(rebootJobResponse);
        // Run the test
        final MockHttpServletResponse flexibleEngineRebootResponse =
                restartService(rebootService.getId());
        DeployedService rebootedService =
                objectMapper.readValue(flexibleEngineRebootResponse.getContentAsString(),
                        DeployedService.class);
        // Verify the results
        assertThat(flexibleEngineRebootResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(rebootedService.getServiceState()).isEqualTo(ServiceState.RUNNING);

        ShowJobResponse rebootFailedJobResponse = new ShowJobResponse();
        rebootFailedJobResponse.setHttpStatusCode(200);
        rebootFailedJobResponse.setStatus(ShowJobResponse.StatusEnum.FAIL);

        mockShowJobInvoker(rebootFailedJobResponse);
        // Run the test
        final MockHttpServletResponse flexibleEngineRebootFailedResponse =
                restartService(rebootService.getId());
        DeployedService rebootFailedService =
                objectMapper.readValue(flexibleEngineRebootFailedResponse.getContentAsString(),
                        DeployedService.class);
        // Verify the results
        assertThat(flexibleEngineRebootResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(rebootFailedService.getServiceState()).isEqualTo(
                ServiceState.STARTING_FAILED);

        deleteCredential(Csp.FLEXIBLE_ENGINE, CredentialType.VARIABLES, "AK_SK");
    }

    @Test
    @WithJwt(file = "jwt_user.json")
    void testServiceStateManageApisForOpenstack() throws Exception {
        OSClient.OSClientV3 mockOsClient = getMockOsClientWithMockServices();
        addCredentialForOpenstack();
        // Setup
        Server mockServer = mock(Server.class);
        when(mockOsClient.compute().servers().get(anyString())).thenReturn(mockServer);
        when(mockServer.getStatus()).thenReturn(Server.Status.STOPPED);
        DeployServiceEntity startService = setUpWellDeployServiceEntity();
        startService.setCsp(Csp.OPENSTACK);
        startService.setServiceState(ServiceState.STOPPED);
        when(deployServiceStorage.findDeployServiceById(startService.getId()))
                .thenReturn(startService);
        ActionResponse startActionResponse = ActionResponse.actionSuccess();
        when(mockOsClient.compute().servers().action(anyString(), any(Action.class)))
                .thenReturn(startActionResponse);
        // Run the test
        final MockHttpServletResponse openstackStartResponse =
                startService(startService.getId());
        DeployedService startedService =
                objectMapper.readValue(openstackStartResponse.getContentAsString(),
                        DeployedService.class);
        // Verify the results
        assertThat(openstackStartResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(startedService.getServiceState()).isEqualTo(
                ServiceState.RUNNING);

        ActionResponse startActionFailedResponse =
                ActionResponse.actionFailed("failed", 403);
        when(mockOsClient.compute().servers().action(anyString(), any(Action.class)))
                .thenReturn(startActionFailedResponse);
        // Run the test
        final MockHttpServletResponse startFailedResponse =
                startService(startService.getId());
        DeployedService startFailedService =
                objectMapper.readValue(startFailedResponse.getContentAsString(),
                        DeployedService.class);
        // Verify the results
        assertThat(startFailedResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(startFailedService.getServiceState()).isEqualTo(
                ServiceState.STARTING_FAILED);


        when(mockServer.getStatus()).thenReturn(Server.Status.ACTIVE);
        DeployServiceEntity stopService = setUpWellDeployServiceEntity();
        stopService.setCsp(Csp.OPENSTACK);
        stopService.setServiceState(ServiceState.RUNNING);
        when(deployServiceStorage.findDeployServiceById(stopService.getId()))
                .thenReturn(stopService);
        ActionResponse stopActionResponse = ActionResponse.actionSuccess();
        when(mockOsClient.compute().servers().action(anyString(), any(Action.class)))
                .thenReturn(stopActionResponse);
        // Run the test
        final MockHttpServletResponse openstackStopResponse =
                stopService(stopService.getId());
        DeployedService stoppedService =
                objectMapper.readValue(openstackStopResponse.getContentAsString(),
                        DeployedService.class);
        // Verify the results
        assertThat(openstackStopResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(stoppedService.getServiceState()).isEqualTo(ServiceState.STOPPED);

        ActionResponse stopActionFailedResponse = ActionResponse.actionFailed("failed", 403);
        when(mockOsClient.compute().servers().action(anyString(), any(Action.class)))
                .thenReturn(stopActionFailedResponse);
        // Run the test
        final MockHttpServletResponse stopFailedResponse =
                stopService(stopService.getId());
        DeployedService stopFailedService =
                objectMapper.readValue(stopFailedResponse.getContentAsString(),
                        DeployedService.class);
        // Verify the results
        assertThat(stopFailedResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(stopFailedService.getServiceState()).isEqualTo(
                ServiceState.STOPPING_FAILED);


        when(mockServer.getStatus()).thenReturn(Server.Status.ACTIVE);
        DeployServiceEntity rebootService = setUpWellDeployServiceEntity();
        rebootService.setCsp(Csp.OPENSTACK);
        rebootService.setServiceState(ServiceState.STOPPED);
        when(deployServiceStorage.findDeployServiceById(rebootService.getId()))
                .thenReturn(rebootService);
        ActionResponse rebootActionResponse = ActionResponse.actionSuccess();
        when(mockOsClient.compute().servers().reboot(anyString(), any(RebootType.class)))
                .thenReturn(rebootActionResponse);
        // Run the test
        final MockHttpServletResponse openstackRebootResponse =
                restartService(rebootService.getId());
        DeployedService rebootedService =
                objectMapper.readValue(openstackRebootResponse.getContentAsString(),
                        DeployedService.class);
        // Verify the results
        assertThat(openstackRebootResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(rebootedService.getServiceState()).isEqualTo(
                ServiceState.RUNNING);

        ActionResponse rebootActionFailedResponse = ActionResponse.actionFailed("failed", 403);
        when(mockOsClient.compute().servers().reboot(anyString(), any(RebootType.class)))
                .thenReturn(rebootActionFailedResponse);
        // Run the test
        final MockHttpServletResponse rebootFailedResponse =
                restartService(rebootService.getId());
        DeployedService rebootFailedService =
                objectMapper.readValue(rebootFailedResponse.getContentAsString(),
                        DeployedService.class);
        // Verify the results
        assertThat(rebootFailedResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(rebootFailedService.getServiceState()).isEqualTo(
                ServiceState.STARTING_FAILED);

        deleteCredential(Csp.OPENSTACK, CredentialType.VARIABLES, "USERNAME_PASSWORD");
    }

    @Test
    @WithJwt(file = "jwt_user.json")
    void testServiceStateManageApisForScs() throws Exception {
        OSClient.OSClientV3 mockOsClient = getMockOsClientWithMockServices();
        addCredentialForScs();
        Server mockServer = mock(Server.class);
        when(mockOsClient.compute().servers().get(anyString())).thenReturn(mockServer);
        // Setup
        when(mockServer.getStatus()).thenReturn(Server.Status.STOPPED);
        DeployServiceEntity startService = setUpWellDeployServiceEntity();
        startService.setCsp(Csp.SCS);
        startService.setServiceState(ServiceState.STOPPED);
        when(deployServiceStorage.findDeployServiceById(startService.getId()))
                .thenReturn(startService);
        ActionResponse startActionResponse = ActionResponse.actionSuccess();
        when(mockOsClient.compute().servers().action(anyString(), any(Action.class)))
                .thenReturn(startActionResponse);
        // Run the test
        final MockHttpServletResponse scsStartResponse =
                startService(startService.getId());
        DeployedService startedService =
                objectMapper.readValue(scsStartResponse.getContentAsString(),
                        DeployedService.class);
        // Verify the results
        assertThat(scsStartResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(startedService.getServiceState()).isEqualTo(
                ServiceState.RUNNING);

        ActionResponse startActionFailedResponse =
                ActionResponse.actionFailed("failed", 403);
        when(mockOsClient.compute().servers().action(anyString(), any(Action.class)))
                .thenReturn(startActionFailedResponse);
        // Run the test
        final MockHttpServletResponse startFailedResponse =
                startService(startService.getId());
        DeployedService startFailedService =
                objectMapper.readValue(startFailedResponse.getContentAsString(),
                        DeployedService.class);
        // Verify the results
        assertThat(startFailedResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(startFailedService.getServiceState()).isEqualTo(
                ServiceState.STARTING_FAILED);


        when(mockServer.getStatus()).thenReturn(Server.Status.ACTIVE);
        DeployServiceEntity stopService = setUpWellDeployServiceEntity();
        stopService.setCsp(Csp.SCS);
        stopService.setServiceState(ServiceState.RUNNING);
        when(deployServiceStorage.findDeployServiceById(stopService.getId()))
                .thenReturn(stopService);
        ActionResponse stopActionResponse = ActionResponse.actionSuccess();
        when(mockOsClient.compute().servers().action(anyString(), any(Action.class)))
                .thenReturn(stopActionResponse);
        // Run the test
        final MockHttpServletResponse scsStopResponse =
                stopService(stopService.getId());
        DeployedService stoppedService =
                objectMapper.readValue(scsStopResponse.getContentAsString(),
                        DeployedService.class);
        // Verify the results
        assertThat(scsStopResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(stoppedService.getServiceState()).isEqualTo(ServiceState.STOPPED);

        ActionResponse stopActionFailedResponse = ActionResponse.actionFailed("failed", 403);
        when(mockOsClient.compute().servers().action(anyString(), any(Action.class)))
                .thenReturn(stopActionFailedResponse);
        // Run the test
        final MockHttpServletResponse stopFailedResponse =
                stopService(stopService.getId());
        DeployedService stopFailedService =
                objectMapper.readValue(stopFailedResponse.getContentAsString(),
                        DeployedService.class);
        // Verify the results
        assertThat(stopFailedResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(stopFailedService.getServiceState()).isEqualTo(
                ServiceState.STOPPING_FAILED);


        when(mockServer.getStatus()).thenReturn(Server.Status.ACTIVE);
        DeployServiceEntity rebootService = setUpWellDeployServiceEntity();
        rebootService.setCsp(Csp.SCS);
        rebootService.setServiceState(ServiceState.STOPPED);
        when(deployServiceStorage.findDeployServiceById(rebootService.getId()))
                .thenReturn(rebootService);
        ActionResponse rebootActionResponse = ActionResponse.actionSuccess();
        when(mockOsClient.compute().servers().reboot(anyString(), any(RebootType.class)))
                .thenReturn(rebootActionResponse);
        // Run the test
        final MockHttpServletResponse scsRebootResponse =
                restartService(rebootService.getId());
        DeployedService rebootedService =
                objectMapper.readValue(scsRebootResponse.getContentAsString(),
                        DeployedService.class);
        // Verify the results
        assertThat(scsRebootResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(rebootedService.getServiceState()).isEqualTo(
                ServiceState.RUNNING);

        ActionResponse rebootActionFailedResponse = ActionResponse.actionFailed("failed", 403);
        when(mockOsClient.compute().servers().reboot(anyString(), any(RebootType.class)))
                .thenReturn(rebootActionFailedResponse);
        // Run the test
        final MockHttpServletResponse rebootFailedResponse =
                restartService(rebootService.getId());
        DeployedService rebootFailedService =
                objectMapper.readValue(rebootFailedResponse.getContentAsString(),
                        DeployedService.class);
        // Verify the results
        assertThat(rebootFailedResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(rebootFailedService.getServiceState()).isEqualTo(
                ServiceState.STARTING_FAILED);

        deleteCredential(Csp.SCS, CredentialType.VARIABLES, "USERNAME_PASSWORD");
    }


    DeployServiceEntity setUpWellDeployServiceEntity() {
        UUID id = UUID.randomUUID();
        DeployServiceEntity deployedServiceEntity = new DeployServiceEntity();
        deployedServiceEntity.setId(id);
        deployedServiceEntity.setCsp(Csp.HUAWEI);
        deployedServiceEntity.setCategory(Category.COMPUTE);
        deployedServiceEntity.setName("test-service");
        deployedServiceEntity.setVersion("1.0");
        deployedServiceEntity.setUserId("1234566");
        deployedServiceEntity.setServiceDeploymentState(ServiceDeploymentState.DEPLOY_SUCCESS);
        DeployRequest deployRequest = new DeployRequest();
        deployRequest.setServiceHostingType(ServiceHostingType.SELF);
        deployedServiceEntity.setDeployRequest(deployRequest);
        deployedServiceEntity.setServiceState(ServiceState.NOT_RUNNING);
        DeployResourceEntity deployedResource = new DeployResourceEntity();
        deployedResource.setId(id);
        deployedResource.setResourceId(id.toString());
        deployedResource.setName("test-service-ecs");
        deployedResource.setKind(DeployResourceKind.VM);
        deployedResource.setProperties(Map.of("region", "cn-southwest-2"));
        deployedResource.setDeployService(deployedServiceEntity);
        deployedServiceEntity.setDeployResourceList(List.of(deployedResource));
        deployedServiceEntity.setCreateTime(OffsetDateTime.now());
        deployedServiceEntity.setLastModifiedTime(OffsetDateTime.now());
        return deployedServiceEntity;
    }


    MockHttpServletResponse startService(UUID id) throws Exception {
        return mockMvc.perform(
                        put("/xpanse/services/start/{id}", id).accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }

    MockHttpServletResponse stopService(UUID id) throws Exception {
        return mockMvc.perform(
                        put("/xpanse/services/stop/{id}", id).accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }

    MockHttpServletResponse restartService(UUID id) throws Exception {
        return mockMvc.perform(
                        put("/xpanse/services/restart/{id}", id).accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }
}
