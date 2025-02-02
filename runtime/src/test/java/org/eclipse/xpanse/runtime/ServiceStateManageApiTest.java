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
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.eclipse.xpanse.api.exceptions.handler.CommonExceptionHandler;
import org.eclipse.xpanse.modules.database.resource.ServiceResourceEntity;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.common.enums.UserOperation;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.response.ErrorType;
import org.eclipse.xpanse.modules.models.response.OrderFailedErrorResponse;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.service.statemanagement.enums.ServiceState;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SuppressWarnings("unchecked")
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=oauth,zitadel,zitadel-testbed,test,dev"})
@AutoConfigureMockMvc
class ServiceStateManageApiTest extends ApisTestCommon {

    @BeforeEach
    void setUp() {
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
    @WithJwt(file = "jwt_all_roles-no-policies.json")
    void testServiceStateManageApis() throws Exception {
        // Setup
        addCredentialForHuaweiCloud();
        Ocl ocl =
                new OclLoader()
                        .getOcl(
                                URI.create("file:src/test/resources/ocl_terraform_test.yml")
                                        .toURL());
        ServiceTemplateDetailVo serviceTemplate =
                registerServiceTemplateAndApproveRegistration(ocl);
        if (Objects.isNull(serviceTemplate)) {
            return;
        }
        ServiceOrder serviceOrder = deployService(serviceTemplate);
        UUID serviceId = serviceOrder.getServiceId();
        assertThat(waitServiceDeploymentIsCompleted(serviceId)).isTrue();
        ServiceDeploymentEntity serviceDeploymentEntity =
                serviceDeploymentStorage.findServiceDeploymentById(serviceOrder.getServiceId());
        testServiceStateManageApisThrowExceptions(serviceDeploymentEntity);
        if (setResources(serviceDeploymentEntity)) {
            testServiceStateManageApisForHuaweiCloud(serviceDeploymentEntity);
            testServiceStateManageApisForFlexibleEngine(serviceDeploymentEntity);
            testServiceStateManageApisForOpenstack(serviceDeploymentEntity);
        }
    }

    void testServiceStateManageApisThrowExceptions(ServiceDeploymentEntity service)
            throws Exception {
        // Setup
        UUID serviceId = UUID.randomUUID();
        String errorMsg = "Service with id " + serviceId + " not found.";
        ErrorResponse result =
                CommonExceptionHandler.getErrorResponse(
                        ErrorType.SERVICE_DEPLOYMENT_NOT_FOUND,
                        Collections.singletonList(errorMsg));
        // run the test
        final MockHttpServletResponse response = startService(serviceId);
        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(result));

        serviceId = service.getId();
        // Setup
        service.setServiceState(ServiceState.NOT_RUNNING);
        service = serviceDeploymentStorage.storeAndFlush(service);
        String errorMsg1 = String.format("Service with id %s has no vm resources.", serviceId);
        ErrorResponse result1 =
                ErrorResponse.errorResponse(
                        ErrorType.SERVICE_DEPLOYMENT_NOT_FOUND,
                        Collections.singletonList(errorMsg1));
        // run the test
        final MockHttpServletResponse response1 = startService(serviceId);

        // Verify the results
        assertThat(response1.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response1.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(result1));

        // Setup
        service.setCsp(Csp.AWS);
        service = serviceDeploymentStorage.storeAndFlush(service);
        String errorMsg2 =
                String.format("Can't find suitable plugin for the Csp %s", Csp.AWS.toValue());
        ErrorResponse result2 =
                ErrorResponse.errorResponse(
                        ErrorType.PLUGIN_NOT_FOUND, Collections.singletonList(errorMsg2));
        // run the test
        final MockHttpServletResponse response2 = startService(serviceId);
        // Verify the results
        assertThat(response2.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response2.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(result2));

        // Setup
        String originalUserId = service.getUserId();
        service.setCsp(Csp.HUAWEI_CLOUD);
        service.setUserId("1");
        service = serviceDeploymentStorage.storeAndFlush(service);
        String errorMsg3 =
                String.format(
                        "No permission to %s owned by other users.",
                        UserOperation.CHANGE_SERVICE_STATE.toValue());
        ErrorResponse result3 =
                ErrorResponse.errorResponse(
                        ErrorType.ACCESS_DENIED, Collections.singletonList(errorMsg3));
        // run the test
        final MockHttpServletResponse response3 = startService(serviceId);

        // Verify the results
        assertThat(response3.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response3.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(result3));

        // Setup
        service.setUserId(originalUserId);
        service.setServiceDeploymentState(ServiceDeploymentState.DEPLOYING);
        service = serviceDeploymentStorage.storeAndFlush(service);
        String errorMsg4 =
                String.format(
                        "Service %s with deployment state %s is not supported to manage power"
                                + " state.",
                        serviceId, service.getServiceDeploymentState().toValue());
        ErrorResponse result4 =
                ErrorResponse.errorResponse(
                        ErrorType.SERVICE_STATE_INVALID, Collections.singletonList(errorMsg4));
        // run the test
        final MockHttpServletResponse response4 = startService(serviceId);
        // Verify the results
        assertThat(response4.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response4.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(result4));

        // Setup
        service.setServiceDeploymentState(ServiceDeploymentState.DEPLOY_SUCCESS);
        service.setServiceState(ServiceState.STARTING);
        service = serviceDeploymentStorage.storeAndFlush(service);
        String errorMsg5 =
                String.format(
                        "Service %s with a running management task, please try again later.",
                        serviceId);
        ErrorResponse errorResult5 =
                ErrorResponse.errorResponse(
                        ErrorType.SERVICE_STATE_INVALID, Collections.singletonList(errorMsg5));
        // run the test
        final MockHttpServletResponse response5 = startService(serviceId);
        // Verify the results
        assertThat(response5.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response5.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(errorResult5));

        // Setup
        service.setServiceState(ServiceState.RUNNING);
        service = serviceDeploymentStorage.storeAndFlush(service);
        String errorMsg6 =
                String.format(
                        "Service %s with state %s is not supported to start.",
                        serviceId, service.getServiceState().toValue());
        ErrorResponse errorResult6 =
                ErrorResponse.errorResponse(
                        ErrorType.SERVICE_STATE_INVALID, Collections.singletonList(errorMsg6));
        // run the test
        final MockHttpServletResponse response6 = startService(serviceId);
        // Verify the results
        assertThat(response6.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response6.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(errorResult6));

        // Setup
        service.setServiceState(ServiceState.NOT_RUNNING);
        service = serviceDeploymentStorage.storeAndFlush(service);
        String errorMsg7 =
                String.format(
                        "Service %s with state %s is not supported to stop.",
                        serviceId, service.getServiceState().toValue());
        ErrorResponse errorResult7 =
                ErrorResponse.errorResponse(
                        ErrorType.SERVICE_STATE_INVALID, Collections.singletonList(errorMsg7));
        // run the test
        final MockHttpServletResponse response7 = stopService(serviceId);
        // Verify the results
        assertThat(response7.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response7.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(errorResult7));

        // Setup
        service.setServiceState(ServiceState.STOPPED);
        service = serviceDeploymentStorage.storeAndFlush(service);
        String errorMsg8 =
                String.format(
                        "Service %s with state %s is not supported to stop.",
                        serviceId, service.getServiceState().toValue());
        ErrorResponse errorResult8 =
                ErrorResponse.errorResponse(
                        ErrorType.SERVICE_STATE_INVALID, Collections.singletonList(errorMsg8));
        // run the test
        final MockHttpServletResponse response8 = stopService(serviceId);
        // Verify the results
        assertThat(response8.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response8.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(errorResult8));

        // Setup
        String errorMsg9 =
                String.format(
                        "Service %s with state %s is not supported to restart.",
                        serviceId, service.getServiceState().toValue());
        ErrorResponse errorResult9 =
                ErrorResponse.errorResponse(
                        ErrorType.SERVICE_STATE_INVALID, Collections.singletonList(errorMsg9));
        // run the test
        final MockHttpServletResponse response9 = restartService(serviceId);
        // Verify the results
        assertThat(response9.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response9.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(errorResult9));

        // Setup
        service.getLockConfig().setModifyLocked(true);
        serviceDeploymentStorage.storeAndFlush(service);
        String errorMsg10 = String.format("Service with id %s is locked from restart.", serviceId);
        ErrorResponse errorResult10 =
                ErrorResponse.errorResponse(
                        ErrorType.SERVICE_LOCKED, Collections.singletonList(errorMsg10));
        // run the test
        final MockHttpServletResponse response10 = restartService(serviceId);
        // Verify the results
        assertThat(response10.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response10.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(errorResult10));

        // Setup
        String errorMsg11 = String.format("Service with id %s is locked from stop.", serviceId);
        ErrorResponse errorResult11 =
                ErrorResponse.errorResponse(
                        ErrorType.SERVICE_LOCKED, Collections.singletonList(errorMsg11));
        // run the test
        final MockHttpServletResponse response11 = stopService(serviceId);
        // Verify the results
        assertThat(response11.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response11.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(errorResult11));
    }

    void testServiceStateManageApisForHuaweiCloud(ServiceDeploymentEntity service)
            throws Exception {
        // Setup
        String site = "Chinese Mainland";
        Region region = new Region();
        region.setName("cn-southwest-2");
        region.setSite(site);
        service.getDeployRequest().setRegion(region);
        service = serviceDeploymentStorage.storeAndFlush(service);
        when(huaweiCloudClient.getEcsClient(any(), any())).thenReturn(mockEcsClient);
        addCredentialForHuaweiCloud();
        testServiceStateManageApisWithHuaweiCloudSdk(service);
        deleteCredential(Csp.HUAWEI_CLOUD, site, CredentialType.VARIABLES, "AK_SK");
    }

    void testServiceStateManageApisWithHuaweiCloudSdk(ServiceDeploymentEntity service)
            throws Exception {
        UUID serviceId = service.getId();
        ShowJobResponse startFailedJobResponse = new ShowJobResponse();
        startFailedJobResponse.setHttpStatusCode(200);
        startFailedJobResponse.setStatus(ShowJobResponse.StatusEnum.FAIL);
        mockShowJobInvoker(startFailedJobResponse);
        // Run the test
        final MockHttpServletResponse startFailedResponse = startService(serviceId);
        ServiceOrder serviceOrder =
                objectMapper.readValue(
                        startFailedResponse.getContentAsString(), ServiceOrder.class);
        UUID startFailedTaskId = serviceOrder.getOrderId();
        // Verify the results
        assertThat(startFailedResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        assertThat(startFailedTaskId).isNotNull();
        Thread.sleep(1000);

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
        final MockHttpServletResponse startResponse = startService(serviceId);
        ServiceOrder serviceOrder1 =
                objectMapper.readValue(startResponse.getContentAsString(), ServiceOrder.class);
        UUID startTaskId = serviceOrder1.getOrderId();
        // Verify the results
        assertThat(startResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        assertThat(startTaskId).isNotNull();
        Thread.sleep(1000);

        service.setServiceState(ServiceState.RUNNING);
        // Setup
        ShowJobResponse restartFailedJobResponse = new ShowJobResponse();
        restartFailedJobResponse.setHttpStatusCode(200);
        restartFailedJobResponse.setStatus(ShowJobResponse.StatusEnum.FAIL);
        mockShowJobInvoker(restartFailedJobResponse);
        // Run the test
        final MockHttpServletResponse restartFailedResponse = restartService(serviceId);
        OrderFailedErrorResponse restartServiceOrder =
                objectMapper.readValue(
                        restartFailedResponse.getContentAsString(), OrderFailedErrorResponse.class);
        UUID restartFailedTaskId = UUID.fromString(restartServiceOrder.getOrderId());
        // Verify the results
        assertThat(restartFailedResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        assertThat(restartFailedTaskId).isNotNull();
        Thread.sleep(1000);

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
        final MockHttpServletResponse restartResponse = restartService(serviceId);
        OrderFailedErrorResponse restartServiceOrder1 =
                objectMapper.readValue(
                        restartResponse.getContentAsString(), OrderFailedErrorResponse.class);
        UUID restartTaskId = UUID.fromString(restartServiceOrder1.getOrderId());
        // Verify the results
        assertThat(restartResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        assertThat(restartTaskId).isNotNull();
        Thread.sleep(1000);
        service.setServiceState(ServiceState.RUNNING);

        // Setup
        ShowJobResponse stopFailedJobResponse = new ShowJobResponse();
        stopFailedJobResponse.setHttpStatusCode(200);
        stopFailedJobResponse.setStatus(ShowJobResponse.StatusEnum.FAIL);

        mockShowJobInvoker(stopFailedJobResponse);
        // Run the test
        final MockHttpServletResponse stopFailedResponse = stopService(serviceId);
        ServiceOrder stopServiceOrder =
                objectMapper.readValue(stopFailedResponse.getContentAsString(), ServiceOrder.class);
        UUID stopFailedTaskId = stopServiceOrder.getOrderId();
        // Verify the results
        assertThat(stopFailedResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        assertThat(stopFailedTaskId).isNotNull();
        Thread.sleep(1000);
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
        final MockHttpServletResponse stopResponse = stopService(serviceId);
        ServiceOrder stopServiceOrder1 =
                objectMapper.readValue(stopResponse.getContentAsString(), ServiceOrder.class);
        UUID stopTaskId = stopServiceOrder1.getOrderId();
        // Verify the results
        assertThat(stopResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        assertThat(stopTaskId).isNotNull();
        Thread.sleep(1000);
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
        when(mockRebootSyncInvoker.retryTimes(anyInt())).thenReturn(mockRebootSyncInvoker);
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

    void testServiceStateManageApisForFlexibleEngine(ServiceDeploymentEntity service)
            throws Exception {
        // Setup
        Region region = new Region();
        region.setName("eu-west-0");
        region.setSite("default");
        service.setCsp(Csp.FLEXIBLE_ENGINE);
        service.getDeployRequest().setRegion(region);
        service = serviceDeploymentStorage.storeAndFlush(service);
        when(flexibleEngineClient.getEcsClient(any(), any())).thenReturn(mockEcsClient);
        addCredentialForFlexibleEngine();
        testServiceStateManageApisWithHuaweiCloudSdk(service);
        deleteCredential(Csp.FLEXIBLE_ENGINE, "default", CredentialType.VARIABLES, "AK_SK");
    }

    void testServiceStateManageApisForOpenstack(ServiceDeploymentEntity service) throws Exception {
        Region region = new Region();
        region.setName("RegionOne");
        region.setSite("default");
        addCredentialForOpenstack(Csp.OPENSTACK_TESTLAB);
        service.getDeployRequest().setRegion(region);
        service = serviceDeploymentStorage.storeAndFlush(service);
        testServiceStateManageApisWithOpenstackSdk(service);
        deleteCredential(
                Csp.OPENSTACK_TESTLAB, "default", CredentialType.VARIABLES, "USERNAME_PASSWORD");
    }

    void testServiceStateManageApisWithOpenstackSdk(ServiceDeploymentEntity service)
            throws Exception {
        OSClient.OSClientV3 mockOsClient = getMockOsClientWithMockServices();

        UUID serviceId = service.getId();
        Server mockServer = mock(Server.class);
        when(mockOsClient.compute().servers().get(anyString())).thenReturn(mockServer);
        when(mockServer.getStatus()).thenReturn(Server.Status.STOPPED);

        ActionResponse startActionFailedResponse = ActionResponse.actionFailed("failed", 403);
        when(mockOsClient.compute().servers().action(serviceId.toString(), Action.START))
                .thenReturn(startActionFailedResponse);
        // Run the test
        final MockHttpServletResponse startFailedResponse = startService(serviceId);
        OrderFailedErrorResponse startServiceOrder =
                objectMapper.readValue(
                        startFailedResponse.getContentAsString(), OrderFailedErrorResponse.class);
        UUID startFailedTaskId = UUID.fromString(startServiceOrder.getOrderId());

        // Verify the results
        assertThat(startFailedResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        assertThat(startFailedTaskId).isNotNull();

        Thread.sleep(1000);
        service.setServiceState(ServiceState.STOPPED);

        ActionResponse startActionResponse = ActionResponse.actionSuccess();
        when(mockOsClient.compute().servers().action(serviceId.toString(), Action.START))
                .thenReturn(startActionResponse);
        // Run the test
        final MockHttpServletResponse startResponse = startService(serviceId);
        OrderFailedErrorResponse startServiceOrder1 =
                objectMapper.readValue(
                        startResponse.getContentAsString(), OrderFailedErrorResponse.class);
        UUID startTaskId = UUID.fromString(startServiceOrder1.getOrderId());
        assertThat(startResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        assertThat(startTaskId).isNotNull();

        Thread.sleep(1000);
        service.setServiceState(ServiceState.RUNNING);

        // Setup
        when(mockServer.getStatus()).thenReturn(Server.Status.ACTIVE);
        ActionResponse restartActionFailedResponse = ActionResponse.actionFailed("failed", 403);
        when(mockOsClient.compute().servers().reboot(serviceId.toString(), RebootType.SOFT))
                .thenReturn(restartActionFailedResponse);

        // Run the test
        final MockHttpServletResponse restartFailedResponse = restartService(serviceId);
        OrderFailedErrorResponse restartServiceOrder =
                objectMapper.readValue(
                        restartFailedResponse.getContentAsString(), OrderFailedErrorResponse.class);
        UUID restartFailedTaskId = UUID.fromString(restartServiceOrder.getOrderId());
        // Verify the results
        assertThat(restartFailedResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        assertThat(restartFailedTaskId).isNotNull();

        Thread.sleep(1000);
        service.setServiceState(ServiceState.RUNNING);

        ActionResponse restartActionResponse = ActionResponse.actionSuccess();
        when(mockOsClient.compute().servers().reboot(serviceId.toString(), RebootType.SOFT))
                .thenReturn(restartActionResponse);
        // Run the test
        final MockHttpServletResponse restartResponse = restartService(serviceId);
        OrderFailedErrorResponse restartServiceOrder1 =
                objectMapper.readValue(
                        restartResponse.getContentAsString(), OrderFailedErrorResponse.class);
        UUID restartTaskId = UUID.fromString(restartServiceOrder1.getOrderId());
        // Verify the results
        assertThat(restartResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        assertThat(restartTaskId).isNotNull();

        Thread.sleep(1000);
        service.setServiceState(ServiceState.RUNNING);

        // Setup
        ActionResponse stopActionFailedResponse = ActionResponse.actionFailed("failed", 403);
        when(mockOsClient.compute().servers().action(serviceId.toString(), Action.STOP))
                .thenReturn(stopActionFailedResponse);

        // Run the test
        final MockHttpServletResponse stopFailedResponse = stopService(serviceId);
        OrderFailedErrorResponse stopServiceOrder =
                objectMapper.readValue(
                        stopFailedResponse.getContentAsString(), OrderFailedErrorResponse.class);
        UUID stopFailedTaskId = UUID.fromString(stopServiceOrder.getOrderId());
        // Verify the results
        assertThat(stopFailedResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        assertThat(stopFailedTaskId).isNotNull();

        Thread.sleep(1000);
        service.setServiceState(ServiceState.RUNNING);

        ActionResponse stopActionResponse = ActionResponse.actionSuccess();
        when(mockOsClient.compute().servers().action(serviceId.toString(), Action.STOP))
                .thenReturn(stopActionResponse);

        // Run the test
        final MockHttpServletResponse stopSdkResponse = stopService(serviceId);
        OrderFailedErrorResponse stopServiceOrder1 =
                objectMapper.readValue(
                        stopFailedResponse.getContentAsString(), OrderFailedErrorResponse.class);
        UUID stopTaskId = UUID.fromString(stopServiceOrder1.getOrderId());
        // Verify the results
        assertThat(stopSdkResponse.getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
        assertThat(stopTaskId).isNotNull();

        Thread.sleep(1000);
        service.setServiceState(ServiceState.STOPPED);
    }

    boolean setResources(ServiceDeploymentEntity deployService) {
        UUID id = UUID.randomUUID();
        ServiceResourceEntity deployedResource = new ServiceResourceEntity();
        deployedResource.setId(id);
        deployedResource.setResourceId("test-resource-id");
        deployedResource.setResourceName("test-service-ecs");
        deployedResource.setResourceKind(DeployResourceKind.VM);
        deployedResource.setProperties(Map.of("region", "cn-southwest-2"));
        deployedResource.setServiceDeploymentEntity(deployService);
        deployService.setDeployResources(List.of(deployedResource));
        try {
            serviceDeploymentStorage.storeAndFlush(deployService);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    MockHttpServletResponse startService(UUID serviceId) throws Exception {
        return mockMvc.perform(
                        put("/xpanse/services/start/{id}", serviceId)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
    }

    MockHttpServletResponse stopService(UUID serviceId) throws Exception {
        return mockMvc.perform(
                        put("/xpanse/services/stop/{id}", serviceId)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
    }

    MockHttpServletResponse restartService(UUID serviceId) throws Exception {
        return mockMvc.perform(
                        put("/xpanse/services/restart/{id}", serviceId)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
    }
}
