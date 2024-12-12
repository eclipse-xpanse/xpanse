package org.eclipse.xpanse.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
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
import jakarta.transaction.Transactional;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.eclipse.xpanse.api.exceptions.handler.CommonExceptionHandler;
import org.eclipse.xpanse.modules.database.resource.ServiceResourceEntity;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.deployment.PolicyValidator;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Transactional
@SuppressWarnings("unchecked")
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=oauth,zitadel,zitadel-testbed,test"})
@AutoConfigureMockMvc
class ServiceStateManageApiTest extends ApisTestCommon {
    @MockitoBean private PolicyValidator mockPolicyValidator;

    void mockDeploymentWitPolicies() {
        doNothing().when(mockPolicyValidator).validateDeploymentWithPolicies(any());
    }

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
        ServiceTemplateDetailVo serviceTemplate = registerServiceTemplate(ocl);
        if (Objects.isNull(serviceTemplate)) {
            return;
        }
        approveServiceTemplateRegistration(serviceTemplate.getServiceTemplateId());
        mockDeploymentWitPolicies();
        ServiceOrder serviceOrder = deployService(serviceTemplate);
        if (waitServiceDeploymentIsCompleted(serviceOrder.getServiceId())) {
            ServiceDeploymentEntity serviceDeploymentEntity =
                    serviceDeploymentStorage.findServiceDeploymentById(serviceOrder.getServiceId());
            testServiceStateManageApisThrowExceptions(serviceDeploymentEntity);

            serviceDeploymentEntity = setResources(serviceDeploymentEntity);
            testServiceStateManageApisForHuaweiCloud(serviceDeploymentEntity);
            testServiceStateManageApisForFlexibleEngine(serviceDeploymentEntity);
            testServiceStateManageApisForOpenstack(serviceDeploymentEntity);
        }
    }

    void testServiceStateManageApisThrowExceptions(ServiceDeploymentEntity service)
            throws Exception {
        // Setup
        UUID uuid = UUID.randomUUID();
        String taskIdPrefix = "Task ";
        // run the test
        final MockHttpServletResponse response = startService(uuid);

        ErrorResponse result =
                CommonExceptionHandler.getErrorResponse(
                        ErrorType.SERVICE_DEPLOYMENT_NOT_FOUND,
                        Collections.singletonList(
                                String.format("Service with id %s not found.", uuid)));
        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(result));

        Region region = new Region();
        region.setName("cn-southwest-2");
        region.setSite("Chinese Mainland");
        // Setup

        // run the test
        final MockHttpServletResponse response1 = startService(service.getId());

        int taskIdStartIndex1 = response1.getContentAsString().indexOf(taskIdPrefix);
        int taskIdEndIndex1 = response1.getContentAsString().indexOf(":", taskIdStartIndex1);
        String taskId1 =
                response1.getContentAsString().substring(taskIdStartIndex1, taskIdEndIndex1 + 1);

        ErrorResponse result1 =
                ErrorResponse.errorResponse(
                        ErrorType.SERVICE_DEPLOYMENT_NOT_FOUND,
                        Collections.singletonList(
                                String.format(
                                        "%s Service with id %s has no vm resources.",
                                        taskId1, service.getId())));
        // Verify the results
        assertThat(response1.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response1.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(result1));

        // Setup
        service.setCsp(Csp.AWS);
        service = serviceDeploymentStorage.storeAndFlush(service);
        ErrorResponse result2 =
                ErrorResponse.errorResponse(
                        ErrorType.PLUGIN_NOT_FOUND,
                        Collections.singletonList(
                                String.format(
                                        "Can't find suitable plugin for the Csp %s",
                                        Csp.AWS.toValue())));

        // run the test
        final MockHttpServletResponse response2 = startService(service.getId());

        // Verify the results
        assertThat(response2.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response2.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(result2));

        // Setup
        service.setCsp(Csp.HUAWEI_CLOUD);
        service.setUserId("1");
        service = serviceDeploymentStorage.storeAndFlush(service);
        // run the test
        final MockHttpServletResponse response3 = startService(service.getId());

        int taskIdStartIndex3 = response3.getContentAsString().indexOf(taskIdPrefix);
        int taskIdEndIndex3 = response3.getContentAsString().indexOf(":", taskIdStartIndex3);
        String taskId3 =
                response3.getContentAsString().substring(taskIdStartIndex3, taskIdEndIndex3 + 1);
        ErrorResponse result3 =
                ErrorResponse.errorResponse(
                        ErrorType.ACCESS_DENIED,
                        Collections.singletonList(
                                String.format(
                                        "%s No permissions to manage status of the service "
                                                + "belonging to other users.",
                                        taskId3)));

        // Verify the results
        assertThat(response3.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response3.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(result3));

        // Setup
        service.setServiceDeploymentState(ServiceDeploymentState.DEPLOYING);
        service = serviceDeploymentStorage.storeAndFlush(service);

        // run the test
        final MockHttpServletResponse response4 = startService(service.getId());

        int taskIdStartIndex4 = response4.getContentAsString().indexOf(taskIdPrefix);
        int taskIdEndIndex4 = response4.getContentAsString().indexOf(":", taskIdStartIndex4);
        String taskId4 =
                response4.getContentAsString().substring(taskIdStartIndex4, taskIdEndIndex4 + 1);

        String errorMsg4 =
                String.format(
                        "%s Service %s with deployment state %s is not supported"
                                + " to manage status.",
                        taskId4, service.getId(), service.getServiceDeploymentState());
        ErrorResponse result4 =
                ErrorResponse.errorResponse(
                        ErrorType.SERVICE_STATE_INVALID, Collections.singletonList(errorMsg4));

        // Verify the results
        assertThat(response4.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response4.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(result4));

        // Setup
        service.setServiceDeploymentState(ServiceDeploymentState.DEPLOY_SUCCESS);
        service.setServiceState(ServiceState.STARTING);
        service = serviceDeploymentStorage.storeAndFlush(service);

        // run the test
        final MockHttpServletResponse response5 = startService(service.getId());
        int taskIdStartIndex5 = response5.getContentAsString().indexOf(taskIdPrefix);
        int taskIdEndIndex5 = response5.getContentAsString().indexOf(":", taskIdStartIndex5);
        String taskId5 =
                response5.getContentAsString().substring(taskIdStartIndex5, taskIdEndIndex5 + 1);

        ErrorResponse errorResult5 =
                ErrorResponse.errorResponse(
                        ErrorType.SERVICE_STATE_INVALID,
                        Collections.singletonList(
                                String.format(
                                        "%s Service %s with a running management task, please try"
                                                + " again later.",
                                        taskId5, service.getId())));

        // Verify the results
        assertThat(response5.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response5.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(errorResult5));

        // Setup
        service.setServiceState(ServiceState.RUNNING);
        service = serviceDeploymentStorage.storeAndFlush(service);
        // run the test
        final MockHttpServletResponse response6 = startService(service.getId());

        int taskIdStartIndex6 = response6.getContentAsString().indexOf(taskIdPrefix);
        int taskIdEndIndex6 = response6.getContentAsString().indexOf(":", taskIdStartIndex6);
        String taskId6 =
                response6.getContentAsString().substring(taskIdStartIndex6, taskIdEndIndex6 + 1);

        ErrorResponse errorResult6 =
                ErrorResponse.errorResponse(
                        ErrorType.SERVICE_STATE_INVALID,
                        Collections.singletonList(
                                String.format(
                                        "%s Service %s with state RUNNING is not supported to "
                                                + "start.",
                                        taskId6, service.getId())));

        // Verify the results
        assertThat(response6.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response6.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(errorResult6));

        // Setup
        service.setServiceState(ServiceState.STOPPING);
        service = serviceDeploymentStorage.storeAndFlush(service);
        // run the test
        final MockHttpServletResponse response7 = stopService(service.getId());
        int taskIdStartIndex7 = response7.getContentAsString().indexOf(taskIdPrefix);
        int taskIdEndIndex7 = response7.getContentAsString().indexOf(":", taskIdStartIndex7);
        String taskId7 =
                response7.getContentAsString().substring(taskIdStartIndex7, taskIdEndIndex7 + 1);
        ErrorResponse errorResult7 =
                ErrorResponse.errorResponse(
                        ErrorType.SERVICE_STATE_INVALID,
                        Collections.singletonList(
                                String.format(
                                        "%s Service %s with a running management task, please try"
                                                + " again later.",
                                        taskId7, service.getId())));

        // Verify the results
        assertThat(response7.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response7.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(errorResult7));

        service.setServiceState(ServiceState.STOPPED);
        service = serviceDeploymentStorage.storeAndFlush(service);

        // run the test
        final MockHttpServletResponse response8 = stopService(service.getId());
        int taskIdStartIndex8 = response8.getContentAsString().indexOf(taskIdPrefix);
        int taskIdEndIndex8 = response8.getContentAsString().indexOf(":", taskIdStartIndex8);
        String taskId8 =
                response8.getContentAsString().substring(taskIdStartIndex8, taskIdEndIndex8 + 1);

        ErrorResponse errorResult8 =
                ErrorResponse.errorResponse(
                        ErrorType.SERVICE_STATE_INVALID,
                        Collections.singletonList(
                                String.format(
                                        "%s Service %s with state STOPPED is not supported to"
                                                + " stop.",
                                        taskId8, service.getId())));

        // Verify the results
        assertThat(response8.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response8.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(errorResult8));

        service.setServiceState(ServiceState.STOPPED);
        service = serviceDeploymentStorage.storeAndFlush(service);
        // run the test
        final MockHttpServletResponse response9 = restartService(service.getId());
        int taskIdStartIndex9 = response9.getContentAsString().indexOf(taskIdPrefix);
        int taskIdEndIndex9 = response9.getContentAsString().indexOf(":", taskIdStartIndex9);
        String taskId9 =
                response9.getContentAsString().substring(taskIdStartIndex9, taskIdEndIndex9 + 1);
        ErrorResponse errorResult9 =
                ErrorResponse.errorResponse(
                        ErrorType.SERVICE_STATE_INVALID,
                        Collections.singletonList(
                                String.format(
                                        "%s Service %s with state STOPPED is not supported to "
                                                + "restart.",
                                        taskId9, service.getId())));
        // Verify the results
        assertThat(response9.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response9.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(errorResult9));
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
        ShowJobResponse startFailedJobResponse = new ShowJobResponse();
        startFailedJobResponse.setHttpStatusCode(200);
        startFailedJobResponse.setStatus(ShowJobResponse.StatusEnum.FAIL);
        mockShowJobInvoker(startFailedJobResponse);
        // Run the test
        final MockHttpServletResponse startFailedResponse = startService(service.getId());
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
        final MockHttpServletResponse startResponse = startService(service.getId());
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
        final MockHttpServletResponse restartFailedResponse = restartService(service.getId());
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
        final MockHttpServletResponse restartResponse = restartService(service.getId());
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
        final MockHttpServletResponse stopFailedResponse = stopService(service.getId());
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
        final MockHttpServletResponse stopResponse = stopService(service.getId());
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

        Server mockServer = mock(Server.class);
        when(mockOsClient.compute().servers().get(anyString())).thenReturn(mockServer);
        when(mockServer.getStatus()).thenReturn(Server.Status.STOPPED);

        ActionResponse startActionFailedResponse = ActionResponse.actionFailed("failed", 403);
        when(mockOsClient.compute().servers().action(service.getId().toString(), Action.START))
                .thenReturn(startActionFailedResponse);
        // Run the test
        final MockHttpServletResponse startFailedResponse = startService(service.getId());
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
        when(mockOsClient.compute().servers().action(service.getId().toString(), Action.START))
                .thenReturn(startActionResponse);
        // Run the test
        final MockHttpServletResponse startResponse = startService(service.getId());
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
        when(mockOsClient.compute().servers().reboot(service.getId().toString(), RebootType.SOFT))
                .thenReturn(restartActionFailedResponse);

        // Run the test
        final MockHttpServletResponse restartFailedResponse = restartService(service.getId());
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
        when(mockOsClient.compute().servers().reboot(service.getId().toString(), RebootType.SOFT))
                .thenReturn(restartActionResponse);
        // Run the test
        final MockHttpServletResponse restartResponse = restartService(service.getId());
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
        when(mockOsClient.compute().servers().action(service.getId().toString(), Action.STOP))
                .thenReturn(stopActionFailedResponse);

        // Run the test
        final MockHttpServletResponse stopFailedResponse = stopService(service.getId());
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
        when(mockOsClient.compute().servers().action(service.getId().toString(), Action.STOP))
                .thenReturn(stopActionResponse);

        // Run the test
        final MockHttpServletResponse stopSdkResponse = stopService(service.getId());
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

    ServiceDeploymentEntity setResources(ServiceDeploymentEntity deployService) {
        UUID id = UUID.randomUUID();
        ServiceResourceEntity deployedResource = new ServiceResourceEntity();
        deployedResource.setId(id);
        deployedResource.setResourceId(id.toString());
        deployedResource.setResourceName("test-service-ecs");
        deployedResource.setResourceKind(DeployResourceKind.VM);
        deployedResource.setProperties(Map.of("region", "cn-southwest-2"));
        deployedResource.setServiceDeploymentEntity(deployService);
        deployService.setDeployResourceList(List.of(deployedResource));
        return serviceDeploymentStorage.storeAndFlush(deployService);
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
