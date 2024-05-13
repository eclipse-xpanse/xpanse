package org.eclipse.xpanse.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.fasterxml.jackson.core.type.TypeReference;
import com.huaweicloud.sdk.core.invoker.SyncInvoker;
import com.huaweicloud.sdk.ecs.v2.model.BatchRebootServersResponse;
import com.huaweicloud.sdk.ecs.v2.model.BatchStartServersResponse;
import com.huaweicloud.sdk.ecs.v2.model.BatchStopServersResponse;
import com.huaweicloud.sdk.ecs.v2.model.ShowJobResponse;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.api.config.AuditLogWriter;
import org.eclipse.xpanse.api.config.GetCspInfoFromRequest;
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
import org.eclipse.xpanse.modules.models.service.statemanagement.ServiceStateManagementTaskDetails;
import org.eclipse.xpanse.modules.models.service.statemanagement.enums.ManagementTaskStatus;
import org.eclipse.xpanse.modules.models.service.statemanagement.enums.ServiceState;
import org.eclipse.xpanse.modules.models.service.statemanagement.enums.ServiceStateManagementTaskType;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SuppressWarnings("unchecked")
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=oauth,zitadel,zitadel-testbed",
        "OS_AUTH_URL=http://127.0.0.1/v3/identity"})
@AutoConfigureMockMvc
class ServiceStateManageApiTest extends ApisTestCommon {

    @MockBean
    private DeployServiceStorage deployServiceStorage;

    @SpyBean
    private AuditLogWriter auditLogWriter;

    @MockBean
    private GetCspInfoFromRequest getCspInfoFromRequest;

    @BeforeEach
    void setUp() {
        auditLogWriter = new AuditLogWriter();
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
        Response result = Response.errorResponse(ResultType.SERVICE_DEPLOYMENT_NOT_FOUND,
                Collections.singletonList(String.format("Service with id %s not found.", uuid)));
        // run the test
        final MockHttpServletResponse response = startService(uuid);
        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(objectMapper.writeValueAsString(result)).isEqualTo(
                response.getContentAsString());

        // Setup
        DeployServiceEntity service1 = setUpWellDeployServiceEntity();
        service1.setDeployResourceList(null);
        Response result1 = Response.errorResponse(ResultType.SERVICE_DEPLOYMENT_NOT_FOUND,
                Collections.singletonList(String.format("Service with id %s has no vm resources.",
                        service1.getId())));
        when(deployServiceStorage.findDeployServiceById(service1.getId())).thenReturn(service1);
        // run the test
        final MockHttpServletResponse response1 = startService(service1.getId());
        // Verify the results
        assertThat(response1.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response1.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(result1));

        // Setup
        DeployServiceEntity service2 = setUpWellDeployServiceEntity();
        service2.setCsp(Csp.AWS);
        Response result2 = Response.errorResponse(ResultType.PLUGIN_NOT_FOUND,
                Collections.singletonList(
                        String.format("Can't find suitable plugin for the Csp %s", Csp.AWS)));

        when(deployServiceStorage.findDeployServiceById(service2.getId())).thenReturn(service2);
        // run the test
        final MockHttpServletResponse response2 = startService(service2.getId());

        // Verify the results
        assertThat(response2.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response2.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(result2));

        // Setup
        DeployServiceEntity service3 = setUpWellDeployServiceEntity();
        service3.setUserId("1");
        Response result3 = Response.errorResponse(ResultType.ACCESS_DENIED,
                Collections.singletonList("No permissions to manage status of the service "
                        + "belonging to other users."));
        when(deployServiceStorage.findDeployServiceById(service3.getId())).thenReturn(service3);
        // run the test
        final MockHttpServletResponse response3 = startService(service3.getId());
        // Verify the results
        assertThat(response3.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response3.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(result3));

        // Setup
        DeployServiceEntity service4 = setUpWellDeployServiceEntity();
        service4.setServiceDeploymentState(ServiceDeploymentState.DEPLOYING);
        Response result4 = Response.errorResponse(ResultType.SERVICE_STATE_INVALID,
                Collections.singletonList(
                        String.format("Service with id %s is %s.", service4.getId(),
                                service4.getServiceDeploymentState())));
        when(deployServiceStorage.findDeployServiceById(service4.getId())).thenReturn(service4);
        // run the test
        final MockHttpServletResponse response4 = startService(service4.getId());
        // Verify the results
        assertThat(response4.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response4.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(result4));

        // Setup
        DeployServiceEntity service5 = setUpWellDeployServiceEntity();
        service5.setServiceState(ServiceState.STARTING);
        when(deployServiceStorage.findDeployServiceById(service5.getId())).thenReturn(service5);
        Response errorResult5 = Response.errorResponse(ResultType.SERVICE_STATE_INVALID,
                Collections.singletonList(String.format(
                        "Service %s with a running management task, please try again later.",
                        service5.getId())));
        // run the test
        final MockHttpServletResponse response5 = startService(service5.getId());
        // Verify the results
        assertThat(response5.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response5.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(errorResult5));

        // Setup
        DeployServiceEntity service6 = setUpWellDeployServiceEntity();
        service6.setServiceState(ServiceState.RUNNING);
        when(deployServiceStorage.findDeployServiceById(service6.getId())).thenReturn(service6);
        Response errorResult6 = Response.errorResponse(ResultType.SERVICE_STATE_INVALID,
                Collections.singletonList(
                        String.format("Service %s with state RUNNING is not supported to start.",
                                service6.getId())));
        // run the test
        final MockHttpServletResponse response6 = startService(service6.getId());
        // Verify the results
        assertThat(response6.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response6.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(errorResult6));

        // Setup
        DeployServiceEntity service7 = setUpWellDeployServiceEntity();
        service7.setServiceState(ServiceState.STOPPING);
        Response errorResult7 = Response.errorResponse(ResultType.SERVICE_STATE_INVALID,
                Collections.singletonList(String.format(
                        "Service %s with a running management task, please try again later.",
                        service7.getId())));
        when(deployServiceStorage.findDeployServiceById(service7.getId())).thenReturn(service7);
        // run the test
        final MockHttpServletResponse response7 = stopService(service7.getId());
        // Verify the results
        assertThat(response7.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response7.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(errorResult7));

        DeployServiceEntity service8 = setUpWellDeployServiceEntity();
        service8.setServiceState(ServiceState.STOPPED);
        when(deployServiceStorage.findDeployServiceById(service8.getId())).thenReturn(service8);
        Response errorResult8 = Response.errorResponse(ResultType.SERVICE_STATE_INVALID,
                Collections.singletonList(
                        String.format("Service %s with state STOPPED is not supported to stop.",
                                service8.getId())));
        // run the test
        final MockHttpServletResponse response8 = stopService(service8.getId());
        // Verify the results
        assertThat(response8.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response8.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(errorResult8));

        DeployServiceEntity service9 = setUpWellDeployServiceEntity();
        service9.setServiceState(ServiceState.STOPPED);
        when(deployServiceStorage.findDeployServiceById(service9.getId())).thenReturn(service9);
        Response errorResult9 = Response.errorResponse(ResultType.SERVICE_STATE_INVALID,
                Collections.singletonList(
                        String.format("Service %s with state STOPPED is not supported to restart.",
                                service9.getId())));
        // run the test
        final MockHttpServletResponse response9 = restartService(service9.getId());
        // Verify the results
        assertThat(response9.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response9.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(errorResult9));

        UUID unknownId = UUID.randomUUID();
        Response errorResult10 =
                Response.errorResponse(ResultType.SERVICE_STATE_MANAGEMENT_TASK_NOT_FOUND,
                        Collections.singletonList(
                                String.format("Service state management task with id %s not found.",
                                        unknownId)));
        // run the test
        final MockHttpServletResponse response10 = getManagementTaskDetailsByTaskId(unknownId);
        // Verify the results
        assertThat(response10.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response10.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(errorResult10));
        // run the test
        final MockHttpServletResponse response11 = deleteManagementTaskByTaskId(unknownId);
        // Verify the results
        assertThat(response11.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response11.getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(errorResult10));
    }

    @Test
    @WithJwt(file = "jwt_user.json")
    void testServiceStateManageApisForHuaweiCloud() throws Exception {
        // Setup
        DeployServiceEntity service = setUpWellDeployServiceEntity();
        service.setServiceState(ServiceState.STOPPED);
        when(deployServiceStorage.findDeployServiceById(service.getId())).thenReturn(service);
        when(huaweiCloudClient.getEcsClient(any(), any())).thenReturn(mockEcsClient);
        addCredentialForHuaweiCloud();
        testServiceStateManageApisWithHuaweiCloudSdk(service);
        deleteCredential(Csp.HUAWEI, CredentialType.VARIABLES, "AK_SK");
    }

    void testServiceStateManageApisWithHuaweiCloudSdk(DeployServiceEntity service)
            throws Exception {
        ShowJobResponse startFailedJobResponse = new ShowJobResponse();
        startFailedJobResponse.setHttpStatusCode(200);
        startFailedJobResponse.setStatus(ShowJobResponse.StatusEnum.FAIL);
        mockShowJobInvoker(startFailedJobResponse);
        // Run the test
        final MockHttpServletResponse startFailedResponse = startService(service.getId());
        UUID startFailedTaskId =
                objectMapper.readValue(startFailedResponse.getContentAsString(), UUID.class);
        // Verify the results
        assertThat(startFailedResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        assertThat(startFailedTaskId).isNotNull();
        assertTaskStatus(startFailedTaskId, ManagementTaskStatus.FAILED);
        List<ServiceStateManagementTaskDetails> startFailedTasks =
                listServiceStateManagementTasks(service.getId(), ServiceStateManagementTaskType.START,
                        ManagementTaskStatus.FAILED);
        assertThat(startFailedTasks.size()).isEqualTo(1);

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
        UUID startTaskId = objectMapper.readValue(startResponse.getContentAsString(), UUID.class);
        // Verify the results
        assertThat(startResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        assertThat(startTaskId).isNotNull();
        assertTaskStatus(startTaskId, ManagementTaskStatus.SUCCESSFUL);
        List<ServiceStateManagementTaskDetails> startSuccessFulTasks =
                listServiceStateManagementTasks(service.getId(), ServiceStateManagementTaskType.START,
                        ManagementTaskStatus.SUCCESSFUL);
        assertThat(startSuccessFulTasks.size()).isEqualTo(1);

        service.setServiceState(ServiceState.RUNNING);
        // Setup
        ShowJobResponse restartFailedJobResponse = new ShowJobResponse();
        restartFailedJobResponse.setHttpStatusCode(200);
        restartFailedJobResponse.setStatus(ShowJobResponse.StatusEnum.FAIL);

        mockShowJobInvoker(restartFailedJobResponse);
        // Run the test
        final MockHttpServletResponse restartFailedResponse = restartService(service.getId());
        UUID restartFailedTaskId =
                objectMapper.readValue(restartFailedResponse.getContentAsString(), UUID.class);
        // Verify the results
        assertThat(restartFailedResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        assertThat(restartFailedTaskId).isNotNull();
        assertTaskStatus(restartFailedTaskId, ManagementTaskStatus.FAILED);

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
        UUID restartTaskId =
                objectMapper.readValue(restartResponse.getContentAsString(), UUID.class);
        // Verify the results
        assertThat(restartResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        assertThat(restartTaskId).isNotNull();
        assertTaskStatus(restartTaskId, ManagementTaskStatus.SUCCESSFUL);
        service.setServiceState(ServiceState.RUNNING);

        // Setup
        ShowJobResponse stopFailedJobResponse = new ShowJobResponse();
        stopFailedJobResponse.setHttpStatusCode(200);
        stopFailedJobResponse.setStatus(ShowJobResponse.StatusEnum.FAIL);

        mockShowJobInvoker(stopFailedJobResponse);
        // Run the test
        final MockHttpServletResponse stopFailedResponse = stopService(service.getId());
        UUID stopFailedTaskId =
                objectMapper.readValue(stopFailedResponse.getContentAsString(), UUID.class);
        // Verify the results
        assertThat(stopFailedResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        assertThat(stopFailedTaskId).isNotNull();
        assertTaskStatus(stopFailedTaskId, ManagementTaskStatus.FAILED);
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
        UUID stopTaskId = objectMapper.readValue(stopResponse.getContentAsString(), UUID.class);
        // Verify the results
        assertThat(stopResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        assertThat(stopTaskId).isNotNull();
        assertTaskStatus(stopTaskId, ManagementTaskStatus.SUCCESSFUL);
        service.setServiceState(ServiceState.STOPPED);

        listAndDeleteTasks(service.getId());
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

    void assertTaskStatus(UUID taskId, ManagementTaskStatus expectedStatus) throws Exception {
        Thread.sleep(1000);
        MockHttpServletResponse response = getManagementTaskDetailsByTaskId(taskId);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        ServiceStateManagementTaskDetails taskDetails =
                objectMapper.readValue(response.getContentAsString(),
                        ServiceStateManagementTaskDetails.class);
        assertThat(taskDetails.getTaskStatus()).isEqualTo(expectedStatus);
    }

    @Test
    @WithJwt(file = "jwt_user.json")
    void testServiceStateManageApisForFlexibleEngine() throws Exception {
        // Setup
        DeployServiceEntity service = setUpWellDeployServiceEntity();
        service.setCsp(Csp.FLEXIBLE_ENGINE);
        when(deployServiceStorage.findDeployServiceById(service.getId())).thenReturn(service);
        when(flexibleEngineClient.getEcsClient(any(), any())).thenReturn(mockEcsClient);
        addCredentialForFlexibleEngine();
        testServiceStateManageApisWithHuaweiCloudSdk(service);
        deleteCredential(Csp.FLEXIBLE_ENGINE, CredentialType.VARIABLES, "AK_SK");
    }

    @Test
    @WithJwt(file = "jwt_user.json")
    void testServiceStateManageApisForOpenstack() throws Exception {
        addCredentialForOpenstack();
        DeployServiceEntity service = setUpWellDeployServiceEntity();
        service.setCsp(Csp.OPENSTACK);
        when(deployServiceStorage.findDeployServiceById(service.getId())).thenReturn(service);
        testServiceStateManageApisWithOpenstackSdk(service);
        deleteCredential(Csp.OPENSTACK, CredentialType.VARIABLES, "USERNAME_PASSWORD");
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

        listAndDeleteTasks(service.getId());
    }

    void listAndDeleteTasks(UUID serviceId) throws Exception {


        List<ServiceStateManagementTaskDetails> allTasks =
                listServiceStateManagementTasks(serviceId, null, null);
        assertThat(allTasks.size()).isEqualTo(6);

        MockHttpServletResponse deleteTaskResponse =
                deleteManagementTaskByTaskId(allTasks.getFirst().getTaskId());
        assertThat(deleteTaskResponse.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());

        MockHttpServletResponse deleteTasksResponse = deleteManagementTasksByServiceId(serviceId);
        assertThat(deleteTasksResponse.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    @Test
    @WithJwt(file = "jwt_user.json")
    void testServiceStateManageApisForScs() throws Exception {
        addCredentialForScs();
        DeployServiceEntity service = setUpWellDeployServiceEntity();
        service.setCsp(Csp.SCS);
        when(deployServiceStorage.findDeployServiceById(service.getId())).thenReturn(service);
        testServiceStateManageApisWithOpenstackSdk(service);
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
        deployedServiceEntity.setFlavor("2vCPUs-4GB-normal");
        deployedServiceEntity.setServiceDeploymentState(ServiceDeploymentState.DEPLOY_SUCCESS);
        DeployRequest deployRequest = new DeployRequest();
        deployRequest.setServiceHostingType(ServiceHostingType.SELF);
        deployRequest.setFlavor("2vCPUs-4GB-normal");
        deployRequest.setServiceRequestProperties(new HashMap<>());
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

    List<ServiceStateManagementTaskDetails> listServiceStateManagementTasks(UUID serviceId,
                                                                            ServiceStateManagementTaskType taskType,
                                                                            ManagementTaskStatus taskStatus)
            throws Exception {
        MockHttpServletRequestBuilder listRequestBuilder =
                get("/xpanse/services/{id}/tasks", serviceId).accept(MediaType.APPLICATION_JSON);
        if (taskType != null) {
            listRequestBuilder.param("taskType", taskType.toValue());
        }
        if (taskStatus != null) {
            listRequestBuilder.param("taskStatus", taskStatus.toValue());
        }
        MockHttpServletResponse listTasksResponse =
                mockMvc.perform(listRequestBuilder).andReturn().getResponse();
        assertThat(listTasksResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        return objectMapper.readValue(listTasksResponse.getContentAsString(),
                new TypeReference<>() {
                });
    }

    MockHttpServletResponse deleteManagementTasksByServiceId(UUID serviceId) throws Exception {
        return mockMvc.perform(delete("/xpanse/services/{serviceId}/tasks", serviceId).accept(
                MediaType.APPLICATION_JSON)).andReturn().getResponse();
    }

    MockHttpServletResponse getManagementTaskDetailsByTaskId(UUID taskId) throws Exception {
        return mockMvc.perform(
                        get("/xpanse/services/tasks/{taskId}", taskId).accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }

    MockHttpServletResponse deleteManagementTaskByTaskId(UUID taskId) throws Exception {
        return mockMvc.perform(
                        delete("/xpanse/services/tasks/{id}", taskId).accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }
}
