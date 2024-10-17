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
import com.huaweicloud.sdk.ecs.v2.model.BatchRebootServersRequest;
import com.huaweicloud.sdk.ecs.v2.model.BatchRebootServersResponse;
import com.huaweicloud.sdk.ecs.v2.model.BatchStartServersRequest;
import com.huaweicloud.sdk.ecs.v2.model.BatchStartServersResponse;
import com.huaweicloud.sdk.ecs.v2.model.BatchStopServersRequest;
import com.huaweicloud.sdk.ecs.v2.model.BatchStopServersResponse;
import com.huaweicloud.sdk.ecs.v2.model.ShowJobRequest;
import com.huaweicloud.sdk.ecs.v2.model.ShowJobResponse;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.api.config.AuditLogWriter;
import org.eclipse.xpanse.api.config.GetCspInfoFromRequest;
import org.eclipse.xpanse.modules.database.resource.DeployResourceEntity;
import org.eclipse.xpanse.modules.database.service.DatabaseDeployServiceStorage;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.service.statemanagement.enums.ServiceState;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.runtime.util.ApisTestCommon;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.common.ActionResponse;
import org.openstack4j.model.compute.Action;
import org.openstack4j.model.compute.RebootType;
import org.openstack4j.model.compute.Server;
import org.openstack4j.openstack.OSFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Slf4j
@SuppressWarnings("unchecked")
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
        "spring.profiles.active=oauth,zitadel,zitadel-testbed",
        "http.request.retry.max.attempts=5",
        "http.request.retry.delay.milliseconds=1000",
        "OS_AUTH_URL=http://127.0.0.1/v3/identity"
})
@AutoConfigureMockMvc
class ServiceStateManageApiTest extends ApisTestCommon {

    @MockBean
    private DatabaseDeployServiceStorage mockDeployServiceStorage;

    @SpyBean
    private AuditLogWriter auditLogWriter;

    @MockBean
    private GetCspInfoFromRequest getCspInfoFromRequest;

    @BeforeEach
    void setUp() {
        auditLogWriter = new AuditLogWriter();
        if (mockOsFactory != null) {
            mockOsFactory.close();
        }
        mockOsFactory = mockStatic(OSFactory.class);
    }

    @AfterEach
    void tearDown() {
        mockOsFactory.close();
    }

    @Test
    @WithJwt(file = "jwt_user.json")
    void testServiceStateManageApis() throws Exception {
        testServiceStateManageApisThrowExceptions();

        testServiceStateManageApisForHuaweiCloud();
        testServiceStateManageApisForFlexibleEngine();
        testServiceStateManageApisForOpenstack();
    }

    void testServiceStateManageApisThrowExceptions() throws Exception {
        // Setup
        UUID uuid = UUID.randomUUID();
        String taskIdPrefix = "Task ";
        // run the test
        final MockHttpServletResponse response = startService(uuid);

        Response result = Response.errorResponse(ResultType.SERVICE_DEPLOYMENT_NOT_FOUND,
                Collections.singletonList(String.format("Service with id %s not found.", uuid)));
        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(objectMapper.writeValueAsString(result)).isEqualTo(
                response.getContentAsString());

        Region region = new Region();
        region.setName("cn-southwest-2");
        region.setSite("Chinese Mainland");
        // Setup
        DeployServiceEntity service1 = setUpWellDeployServiceEntity(Csp.HUAWEI_CLOUD, region);
        service1.setDeployResourceList(null);
        when(mockDeployServiceStorage.findDeployServiceById(service1.getId())).thenReturn(service1);
        // run the test
        final MockHttpServletResponse response1 = startService(service1.getId());

        int taskIdStartIndex1 = response1.getContentAsString().indexOf(taskIdPrefix);
        int taskIdEndIndex1 = response1.getContentAsString().indexOf(":", taskIdStartIndex1);
        String taskId1 =
                response1.getContentAsString().substring(taskIdStartIndex1, taskIdEndIndex1 + 1);

        Response result1 = Response.errorResponse(ResultType.SERVICE_DEPLOYMENT_NOT_FOUND,
                Collections.singletonList(
                        String.format("%s Service with id %s has no vm resources.",
                                taskId1, service1.getId())));
        // Verify the results
        assertThat(response1.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response1.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(result1));

        // Setup
        DeployServiceEntity service2 = setUpWellDeployServiceEntity(Csp.AWS, region);
        Response result2 = Response.errorResponse(ResultType.PLUGIN_NOT_FOUND,
                Collections.singletonList(
                        String.format("Can't find suitable plugin for the Csp %s",
                                Csp.AWS.toValue())));

        when(mockDeployServiceStorage.findDeployServiceById(service2.getId())).thenReturn(service2);
        // run the test
        final MockHttpServletResponse response2 = startService(service2.getId());

        // Verify the results
        assertThat(response2.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response2.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(result2));

        // Setup
        DeployServiceEntity service3 = setUpWellDeployServiceEntity(Csp.HUAWEI_CLOUD, region);
        service3.setUserId("1");
        when(mockDeployServiceStorage.findDeployServiceById(service3.getId())).thenReturn(service3);
        // run the test
        final MockHttpServletResponse response3 = startService(service3.getId());

        int taskIdStartIndex3 = response3.getContentAsString().indexOf(taskIdPrefix);
        int taskIdEndIndex3 = response3.getContentAsString().indexOf(":", taskIdStartIndex3);
        String taskId3 =
                response3.getContentAsString().substring(taskIdStartIndex3, taskIdEndIndex3 + 1);
        Response result3 = Response.errorResponse(ResultType.ACCESS_DENIED,
                Collections.singletonList(
                        String.format("%s No permissions to manage status of the service "
                                + "belonging to other users.", taskId3)));

        // Verify the results
        assertThat(response3.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response3.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(result3));

        // Setup
        DeployServiceEntity service4 = setUpWellDeployServiceEntity(Csp.HUAWEI_CLOUD, region);
        service4.setServiceDeploymentState(ServiceDeploymentState.DEPLOYING);

        when(mockDeployServiceStorage.findDeployServiceById(service4.getId())).thenReturn(service4);
        // run the test
        final MockHttpServletResponse response4 = startService(service4.getId());

        int taskIdStartIndex4 = response4.getContentAsString().indexOf(taskIdPrefix);
        int taskIdEndIndex4 = response4.getContentAsString().indexOf(":", taskIdStartIndex4);
        String taskId4 =
                response4.getContentAsString().substring(taskIdStartIndex4, taskIdEndIndex4 + 1);

        String errorMsg4 =
                String.format("%s Service %s with deployment state %s is not supported"
                                + " to manage status.", taskId4, service4.getId(),
                        service4.getServiceDeploymentState());
        Response result4 = Response.errorResponse(ResultType.SERVICE_STATE_INVALID,
                Collections.singletonList(errorMsg4));

        // Verify the results
        assertThat(response4.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response4.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(result4));

        // Setup
        DeployServiceEntity service5 = setUpWellDeployServiceEntity(Csp.HUAWEI_CLOUD, region);
        service5.setServiceState(ServiceState.STARTING);
        when(mockDeployServiceStorage.findDeployServiceById(service5.getId())).thenReturn(service5);

        // run the test
        final MockHttpServletResponse response5 = startService(service5.getId());
        int taskIdStartIndex5 = response5.getContentAsString().indexOf(taskIdPrefix);
        int taskIdEndIndex5 = response5.getContentAsString().indexOf(":", taskIdStartIndex5);
        String taskId5 =
                response5.getContentAsString().substring(taskIdStartIndex5, taskIdEndIndex5 + 1);

        Response errorResult5 = Response.errorResponse(ResultType.SERVICE_STATE_INVALID,
                Collections.singletonList(String.format(
                        "%s Service %s with a running management task, please try again " +
                                "later.",
                        taskId5, service5.getId())));

        // Verify the results
        assertThat(response5.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response5.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(errorResult5));

        // Setup
        DeployServiceEntity service6 = setUpWellDeployServiceEntity(Csp.HUAWEI_CLOUD, region);
        service6.setServiceState(ServiceState.RUNNING);
        when(mockDeployServiceStorage.findDeployServiceById(service6.getId())).thenReturn(service6);
        // run the test
        final MockHttpServletResponse response6 = startService(service6.getId());

        int taskIdStartIndex6 = response6.getContentAsString().indexOf(taskIdPrefix);
        int taskIdEndIndex6 = response6.getContentAsString().indexOf(":", taskIdStartIndex6);
        String taskId6 =
                response6.getContentAsString().substring(taskIdStartIndex6, taskIdEndIndex6 + 1);

        Response errorResult6 = Response.errorResponse(ResultType.SERVICE_STATE_INVALID,
                Collections.singletonList(
                        String.format(
                                "%s Service %s with state RUNNING is not supported to " +
                                        "start.",
                                taskId6, service6.getId())));

        // Verify the results
        assertThat(response6.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response6.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(errorResult6));

        // Setup
        DeployServiceEntity service7 = setUpWellDeployServiceEntity(Csp.HUAWEI_CLOUD, region);
        service7.setServiceState(ServiceState.STOPPING);
        when(mockDeployServiceStorage.findDeployServiceById(service7.getId())).thenReturn(service7);
        // run the test
        final MockHttpServletResponse response7 = stopService(service7.getId());
        int taskIdStartIndex7 = response7.getContentAsString().indexOf(taskIdPrefix);
        int taskIdEndIndex7 = response7.getContentAsString().indexOf(":", taskIdStartIndex7);
        String taskId7 =
                response7.getContentAsString().substring(taskIdStartIndex7, taskIdEndIndex7 + 1);
        Response errorResult7 = Response.errorResponse(ResultType.SERVICE_STATE_INVALID,
                Collections.singletonList(String.format(
                        "%s Service %s with a running management task, please try again " +
                                "later.",
                        taskId7, service7.getId())));

        // Verify the results
        assertThat(response7.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response7.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(errorResult7));

        DeployServiceEntity service8 = setUpWellDeployServiceEntity(Csp.HUAWEI_CLOUD, region);
        service8.setServiceState(ServiceState.STOPPED);
        when(mockDeployServiceStorage.findDeployServiceById(service8.getId())).thenReturn(service8);

        // run the test
        final MockHttpServletResponse response8 = stopService(service8.getId());
        int taskIdStartIndex8 = response8.getContentAsString().indexOf(taskIdPrefix);
        int taskIdEndIndex8 = response8.getContentAsString().indexOf(":", taskIdStartIndex8);
        String taskId8 =
                response8.getContentAsString().substring(taskIdStartIndex8, taskIdEndIndex8 + 1);

        Response errorResult8 = Response.errorResponse(ResultType.SERVICE_STATE_INVALID,
                Collections.singletonList(
                        String.format(
                                "%s Service %s with state STOPPED is not supported to stop.",
                                taskId8, service8.getId())));

        // Verify the results
        assertThat(response8.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response8.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(errorResult8));

        DeployServiceEntity service9 = setUpWellDeployServiceEntity(Csp.HUAWEI_CLOUD, region);
        service9.setServiceState(ServiceState.STOPPED);
        when(mockDeployServiceStorage.findDeployServiceById(service9.getId())).thenReturn(service9);

        // run the test
        final MockHttpServletResponse response9 = restartService(service9.getId());
        int taskIdStartIndex9 = response9.getContentAsString().indexOf(taskIdPrefix);
        int taskIdEndIndex9 = response9.getContentAsString().indexOf(":", taskIdStartIndex9);
        String taskId9 =
                response9.getContentAsString().substring(taskIdStartIndex9, taskIdEndIndex9 + 1);
        Response errorResult9 = Response.errorResponse(ResultType.SERVICE_STATE_INVALID,
                Collections.singletonList(
                        String.format(
                                "%s Service %s with state STOPPED is not supported to " +
                                        "restart.",
                                taskId9, service9.getId())));
        // Verify the results
        assertThat(response9.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response9.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(errorResult9));
    }

    void testServiceStateManageApisForHuaweiCloud() throws Exception {
        // Setup
        String site = "Chinese Mainland";
        Region region = new Region();
        region.setName("cn-southwest-2");
        region.setSite(site);
        DeployServiceEntity service = setUpWellDeployServiceEntity(Csp.HUAWEI_CLOUD, region);
        service.setServiceState(ServiceState.STOPPED);
        when(mockDeployServiceStorage.findDeployServiceById(service.getId())).thenReturn(service);
        when(huaweiCloudClient.getEcsClient(any(), any())).thenReturn(mockEcsClient);
        addCredentialForHuaweiCloud();
        testServiceStateManageApisWithHuaweiCloudSdk(service);
        deleteCredential(Csp.HUAWEI_CLOUD, site, CredentialType.VARIABLES, "AK_SK");
    }

    void testServiceStateManageApisWithHuaweiCloudSdk(DeployServiceEntity service)
            throws Exception {
        ShowJobResponse startFailedJobResponse = new ShowJobResponse();
        startFailedJobResponse.setHttpStatusCode(200);
        startFailedJobResponse.setStatus(ShowJobResponse.StatusEnum.FAIL);
        mockShowJobInvoker(startFailedJobResponse);
        // Run the test
        final MockHttpServletResponse startFailedResponse = startService(service.getId());
        System.out.println("startFailedResponse.getContentAsString()");
        System.out.println(startFailedResponse.getContentAsString());
        ServiceOrder serviceOrder =
                objectMapper.readValue(startFailedResponse.getContentAsString(),
                        ServiceOrder.class);
        UUID startFailedTaskId = serviceOrder.getOrderId();
        // Verify the results
        assertThat(startFailedResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        assertThat(startFailedTaskId).isNotNull();

        service.setServiceState(ServiceState.STOPPED);
        BatchStartServersResponse startSdkResponse = new BatchStartServersResponse();
        startSdkResponse.setHttpStatusCode(200);
        startSdkResponse.setJobId(UUID.randomUUID().toString());
        mockBatchStartServersInvoker(startSdkResponse);

        ShowJobResponse startJobResponse = new ShowJobResponse();
        startJobResponse.setHttpStatusCode(200);
        startJobResponse.setStatus(ShowJobResponse.StatusEnum.SUCCESS);
        mockShowJobInvoker(startJobResponse);
        // Run the test
        final MockHttpServletResponse startResponse = startService(service.getId());
        ServiceOrder serviceOrder1 =
                objectMapper.readValue(startResponse.getContentAsString(), ServiceOrder.class);
        UUID startTaskId = serviceOrder1.getOrderId();
        // Verify the results
        assertThat(startResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        assertThat(startTaskId).isNotNull();

        service.setServiceState(ServiceState.RUNNING);
        // Setup
        ShowJobResponse restartFailedJobResponse = new ShowJobResponse();
        restartFailedJobResponse.setHttpStatusCode(200);
        restartFailedJobResponse.setStatus(ShowJobResponse.StatusEnum.FAIL);

        mockShowJobInvoker(restartFailedJobResponse);
        // Run the test
        final MockHttpServletResponse restartFailedResponse = restartService(service.getId());
        ServiceOrder restartServiceOrder =
                objectMapper.readValue(restartFailedResponse.getContentAsString(),
                        ServiceOrder.class);
        UUID restartFailedTaskId =
                restartServiceOrder.getOrderId();
        // Verify the results
        assertThat(restartFailedResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        assertThat(restartFailedTaskId).isNotNull();

        service.setServiceState(ServiceState.RUNNING);
        BatchRebootServersResponse rebootSdkResponse = new BatchRebootServersResponse();
        rebootSdkResponse.setHttpStatusCode(200);
        rebootSdkResponse.setJobId(UUID.randomUUID().toString());
        mockBatchRebootServersInvoker(rebootSdkResponse);
        ShowJobResponse rebootJobResponse = new ShowJobResponse();
        rebootJobResponse.setHttpStatusCode(200);
        rebootJobResponse.setStatus(ShowJobResponse.StatusEnum.SUCCESS);
        mockShowJobInvoker(rebootJobResponse);
        // Run the test
        final MockHttpServletResponse restartResponse = restartService(service.getId());
        ServiceOrder restartServiceOrder1 =
                objectMapper.readValue(restartResponse.getContentAsString(),
                        ServiceOrder.class);
        UUID restartTaskId =
                restartServiceOrder1.getOrderId();
        // Verify the results
        assertThat(restartResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        assertThat(restartTaskId).isNotNull();
        service.setServiceState(ServiceState.RUNNING);

        // Setup
        ShowJobResponse stopFailedJobResponse = new ShowJobResponse();
        stopFailedJobResponse.setHttpStatusCode(200);
        stopFailedJobResponse.setStatus(ShowJobResponse.StatusEnum.FAIL);

        mockShowJobInvoker(stopFailedJobResponse);
        // Run the test
        final MockHttpServletResponse stopFailedResponse = stopService(service.getId());
        ServiceOrder stopServiceOrder =
                objectMapper.readValue(stopFailedResponse.getContentAsString(),
                        ServiceOrder.class);
        UUID stopFailedTaskId =
                stopServiceOrder.getOrderId();
        // Verify the results
        assertThat(stopFailedResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        assertThat(stopFailedTaskId).isNotNull();
        service.setServiceState(ServiceState.RUNNING);

        BatchStopServersResponse stopSdkResponse = new BatchStopServersResponse();
        stopSdkResponse.setHttpStatusCode(200);
        stopSdkResponse.setJobId(UUID.randomUUID().toString());
        mockBatchStopServersInvoker(stopSdkResponse);

        ShowJobResponse stopJobResponse = new ShowJobResponse();
        stopJobResponse.setHttpStatusCode(200);
        stopJobResponse.setStatus(ShowJobResponse.StatusEnum.SUCCESS);
        mockShowJobInvoker(stopJobResponse);
        // Run the test
        final MockHttpServletResponse stopResponse = stopService(service.getId());
        ServiceOrder stopServiceOrder1 =
                objectMapper.readValue(stopResponse.getContentAsString(),
                        ServiceOrder.class);
        UUID stopTaskId = stopServiceOrder1.getOrderId();
        // Verify the results
        assertThat(stopResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        assertThat(stopTaskId).isNotNull();
        service.setServiceState(ServiceState.STOPPED);
    }


    void mockBatchStartServersInvoker(BatchStartServersResponse startResponse) {
        SyncInvoker<BatchStartServersRequest, BatchStartServersResponse> mockStartSyncInvoker =
                mock(SyncInvoker.class);
        when(mockEcsClient.batchStartServersInvoker(any())).thenReturn(mockStartSyncInvoker);
        when(mockStartSyncInvoker.retryTimes(anyInt())).thenReturn(mockStartSyncInvoker);
        when(mockStartSyncInvoker.retryCondition(any())).thenReturn(mockStartSyncInvoker);
        when(mockStartSyncInvoker.backoffStrategy(any())).thenReturn(mockStartSyncInvoker);
        when(mockStartSyncInvoker.invoke()).thenReturn(startResponse);
    }

    void mockBatchStopServersInvoker(BatchStopServersResponse stopResponse) {
        SyncInvoker<BatchStopServersRequest, BatchStopServersResponse> mockStopSyncInvoker =
                mock(SyncInvoker.class);
        when(mockEcsClient.batchStopServersInvoker(any())).thenReturn(mockStopSyncInvoker);
        when(mockStopSyncInvoker.retryTimes(anyInt())).thenReturn(mockStopSyncInvoker);
        when(mockStopSyncInvoker.retryCondition(any())).thenReturn(mockStopSyncInvoker);
        when(mockStopSyncInvoker.backoffStrategy(any())).thenReturn(mockStopSyncInvoker);
        when(mockStopSyncInvoker.invoke()).thenReturn(stopResponse);
    }

    void mockBatchRebootServersInvoker(BatchRebootServersResponse rebootResponse) {
        SyncInvoker<BatchRebootServersRequest, BatchRebootServersResponse> mockRebootSyncInvoker =
                mock(SyncInvoker.class);
        when(mockEcsClient.batchRebootServersInvoker(any())).thenReturn(mockRebootSyncInvoker);
        when(mockRebootSyncInvoker.retryTimes(anyInt())).thenReturn(
                mockRebootSyncInvoker);
        when(mockRebootSyncInvoker.retryCondition(any())).thenReturn(mockRebootSyncInvoker);
        when(mockRebootSyncInvoker.backoffStrategy(any())).thenReturn(mockRebootSyncInvoker);
        when(mockRebootSyncInvoker.invoke()).thenReturn(rebootResponse);
    }

    void mockShowJobInvoker(ShowJobResponse jobResponse) {
        SyncInvoker<ShowJobRequest, ShowJobResponse> mockJobSyncInvoker = mock(SyncInvoker.class);
        when(mockEcsClient.showJobInvoker(any())).thenReturn(mockJobSyncInvoker);
        when(mockJobSyncInvoker.retryTimes(anyInt())).thenReturn(mockJobSyncInvoker);
        when(mockJobSyncInvoker.retryCondition(any())).thenReturn(mockJobSyncInvoker);
        when(mockJobSyncInvoker.backoffStrategy(any())).thenReturn(mockJobSyncInvoker);
        when(mockJobSyncInvoker.invoke()).thenReturn(jobResponse);
    }

    void testServiceStateManageApisForFlexibleEngine() throws Exception {
        // Setup
        Region region = new Region();
        region.setName("eu-west-0");
        region.setSite("default");
        DeployServiceEntity service = setUpWellDeployServiceEntity(Csp.FLEXIBLE_ENGINE, region);
        when(mockDeployServiceStorage.findDeployServiceById(service.getId())).thenReturn(service);
        when(flexibleEngineClient.getEcsClient(any(), any())).thenReturn(mockEcsClient);
        addCredentialForFlexibleEngine();
        testServiceStateManageApisWithHuaweiCloudSdk(service);
        deleteCredential(Csp.FLEXIBLE_ENGINE, "default", CredentialType.VARIABLES, "AK_SK");
    }

    void testServiceStateManageApisForOpenstack() throws Exception {
        Region region = new Region();
        region.setName("RegionOne");
        region.setSite("default");
        addCredentialForOpenstack(Csp.OPENSTACK_TESTLAB);
        DeployServiceEntity service = setUpWellDeployServiceEntity(Csp.OPENSTACK_TESTLAB, region);
        when(mockDeployServiceStorage.findDeployServiceById(service.getId())).thenReturn(service);
        testServiceStateManageApisWithOpenstackSdk(service);
        deleteCredential(Csp.OPENSTACK_TESTLAB, "default", CredentialType.VARIABLES,
                "USERNAME_PASSWORD");
    }

    void testServiceStateManageApisWithOpenstackSdk(DeployServiceEntity service) throws Exception {
        OSClient.OSClientV3 mockOsClient = getMockOsClientWithMockServices();

        Server mockServer = mock(Server.class);
        when(mockOsClient.compute().servers().get(anyString())).thenReturn(mockServer);
        when(mockServer.getStatus()).thenReturn(Server.Status.STOPPED);

        ActionResponse startActionFailedResponse = ActionResponse.actionFailed("failed", 403);
        when(mockOsClient.compute().servers()
                .action(service.getId().toString(), Action.START)).thenReturn(
                startActionFailedResponse);
        // Run the test
        final MockHttpServletResponse startFailedResponse = startService(service.getId());
        UUID startFailedTaskId =
                objectMapper.readValue(startFailedResponse.getContentAsString(), UUID.class);
        // Verify the results
        assertThat(startFailedResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        assertThat(startFailedTaskId).isNotNull();

        Thread.sleep(1000);
        service.setServiceState(ServiceState.STOPPED);

        ActionResponse startActionResponse = ActionResponse.actionSuccess();
        when(mockOsClient.compute().servers()
                .action(service.getId().toString(), Action.START)).thenReturn(startActionResponse);
        // Run the test
        final MockHttpServletResponse startResponse = startService(service.getId());
        UUID startTaskId = objectMapper.readValue(startResponse.getContentAsString(), UUID.class);
        assertThat(startResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        assertThat(startTaskId).isNotNull();

        Thread.sleep(1000);
        service.setServiceState(ServiceState.RUNNING);

        // Setup
        when(mockServer.getStatus()).thenReturn(Server.Status.ACTIVE);
        ActionResponse restartActionFailedResponse = ActionResponse.actionFailed("failed", 403);
        when(mockOsClient.compute().servers()
                .reboot(service.getId().toString(), RebootType.SOFT)).thenReturn(
                restartActionFailedResponse);

        // Run the test
        final MockHttpServletResponse restartFailedResponse = restartService(service.getId());
        log.info(restartFailedResponse.getContentAsString());
        UUID restartFailedTaskId =
                objectMapper.readValue(restartFailedResponse.getContentAsString(), UUID.class);
        // Verify the results
        assertThat(restartFailedResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        assertThat(restartFailedTaskId).isNotNull();

        Thread.sleep(1000);
        service.setServiceState(ServiceState.RUNNING);

        ActionResponse restartActionResponse = ActionResponse.actionSuccess();
        when(mockOsClient.compute().servers()
                .reboot(service.getId().toString(), RebootType.SOFT)).thenReturn(
                restartActionResponse);
        // Run the test
        final MockHttpServletResponse restartResponse = restartService(service.getId());
        UUID restartTaskId =
                objectMapper.readValue(restartResponse.getContentAsString(), UUID.class);
        // Verify the results
        assertThat(restartResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        assertThat(restartTaskId).isNotNull();

        Thread.sleep(1000);
        service.setServiceState(ServiceState.RUNNING);

        // Setup
        ActionResponse stopActionFailedResponse = ActionResponse.actionFailed("failed", 403);
        when(mockOsClient.compute().servers()
                .action(service.getId().toString(), Action.STOP)).thenReturn(
                stopActionFailedResponse);

        // Run the test
        final MockHttpServletResponse stopFailedResponse = stopService(service.getId());
        UUID stopFailedTaskId =
                objectMapper.readValue(stopFailedResponse.getContentAsString(), UUID.class);
        // Verify the results
        assertThat(stopFailedResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        assertThat(stopFailedTaskId).isNotNull();

        Thread.sleep(1000);
        service.setServiceState(ServiceState.RUNNING);

        ActionResponse stopActionResponse = ActionResponse.actionSuccess();
        when(mockOsClient.compute().servers()
                .action(service.getId().toString(), Action.STOP)).thenReturn(stopActionResponse);

        // Run the test
        final MockHttpServletResponse stopSdkResponse = stopService(service.getId());
        UUID stopTaskId = objectMapper.readValue(stopSdkResponse.getContentAsString(), UUID.class);
        // Verify the results
        assertThat(stopSdkResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        assertThat(stopTaskId).isNotNull();

        Thread.sleep(1000);
        service.setServiceState(ServiceState.STOPPED);
    }

    DeployServiceEntity setUpWellDeployServiceEntity(Csp csp, Region region) {
        UUID id = UUID.randomUUID();
        DeployServiceEntity deployedServiceEntity = new DeployServiceEntity();
        deployedServiceEntity.setId(id);
        deployedServiceEntity.setCsp(csp);
        deployedServiceEntity.setCategory(Category.COMPUTE);
        deployedServiceEntity.setName("test-service");
        deployedServiceEntity.setVersion("1.0");
        deployedServiceEntity.setUserId("userId");
        deployedServiceEntity.setFlavor("2vCPUs-4GB-normal");
        deployedServiceEntity.setServiceDeploymentState(ServiceDeploymentState.DEPLOY_SUCCESS);
        DeployRequest deployRequest = new DeployRequest();
        deployRequest.setRegion(region);
        deployRequest.setServiceHostingType(ServiceHostingType.SELF);
        deployRequest.setFlavor("2vCPUs-4GB-normal");
        deployRequest.setServiceRequestProperties(new HashMap<>());
        deployedServiceEntity.setDeployRequest(deployRequest);
        deployedServiceEntity.setServiceState(ServiceState.NOT_RUNNING);
        DeployResourceEntity deployedResource = new DeployResourceEntity();
        deployedResource.setId(id);
        deployedResource.setResourceId(id.toString());
        deployedResource.setResourceName("test-service-ecs");
        deployedResource.setResourceKind(DeployResourceKind.VM);
        deployedResource.setProperties(Map.of("region", "cn-southwest-2"));
        deployedResource.setDeployService(deployedServiceEntity);
        deployedServiceEntity.setDeployResourceList(List.of(deployedResource));
        deployedServiceEntity.setCreateTime(OffsetDateTime.now());
        deployedServiceEntity.setLastModifiedTime(OffsetDateTime.now());
        return deployedServiceEntity;
    }

    MockHttpServletResponse startService(UUID serviceId) throws Exception {
        return mockMvc.perform(
                        put("/xpanse/services/start/{id}", serviceId).accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }

    MockHttpServletResponse stopService(UUID serviceId) throws Exception {
        return mockMvc.perform(
                        put("/xpanse/services/stop/{id}", serviceId).accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }

    MockHttpServletResponse restartService(UUID serviceId) throws Exception {
        return mockMvc.perform(
                        put("/xpanse/services/restart/{id}", serviceId).accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }
}
