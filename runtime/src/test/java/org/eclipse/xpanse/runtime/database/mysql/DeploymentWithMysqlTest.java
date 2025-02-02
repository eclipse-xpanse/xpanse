/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.runtime.database.mysql;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import jakarta.annotation.Resource;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.api.controllers.ServiceDeployerApi;
import org.eclipse.xpanse.api.controllers.ServiceOrderManageApi;
import org.eclipse.xpanse.api.controllers.ServicePortingApi;
import org.eclipse.xpanse.api.controllers.ServiceTemplateApi;
import org.eclipse.xpanse.api.controllers.UserCloudCredentialsApi;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.models.billing.enums.BillingMode;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.CreateCredential;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.service.deployment.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deployment.DeployRequestBase;
import org.eclipse.xpanse.modules.models.service.deployment.ModifyRequest;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrderDetails;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrderStatusUpdate;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.models.service.order.exceptions.ServiceOrderNotFound;
import org.eclipse.xpanse.modules.models.service.utils.ServiceDeployVariablesJsonSchemaGenerator;
import org.eclipse.xpanse.modules.models.service.view.DeployedServiceDetails;
import org.eclipse.xpanse.modules.models.servicetemplate.AvailabilityZoneConfig;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceTemplateRegistrationState;
import org.eclipse.xpanse.modules.models.servicetemplate.request.ServiceTemplateRequestInfo;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.eclipse.xpanse.modules.models.servicetemplate.view.UserOrderableServiceVo;
import org.eclipse.xpanse.modules.models.workflow.serviceporting.ServicePortingRequest;
import org.eclipse.xpanse.plugins.huaweicloud.monitor.constant.HuaweiCloudMonitorConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.context.request.async.DeferredResult;
import org.testcontainers.junit.jupiter.Testcontainers;

@Slf4j
@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(
        properties = {"spring.profiles.active=oauth,zitadel,zitadel-testbed,mysql,test,dev"})
@AutoConfigureMockMvc
class DeploymentWithMysqlTest extends AbstractMysqlIntegrationTest {

    @Resource private ServiceDeployerApi serviceDeployerApi;
    @Resource private UserCloudCredentialsApi userCloudCredentialsApi;
    @Resource private ServiceOrderManageApi serviceOrderManageApi;
    @Resource private ServiceTemplateApi serviceTemplateApi;

    @Resource
    private ServiceDeployVariablesJsonSchemaGenerator serviceDeployVariablesJsonSchemaGenerator;

    @Resource private OclLoader oclLoader;
    @Resource private ServicePortingApi servicePortingApi;
    @Resource private ServiceTemplateStorage serviceTemplateStorage;

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testDeployer() throws Exception {
        ServiceTemplateRequestInfo serviceTemplateHistory = registerServiceTemplate();
        if (Objects.isNull(serviceTemplateHistory)) {
            return;
        }
        ServiceTemplateDetailVo serviceTemplate =
                serviceTemplateApi.getServiceTemplateDetailsById(
                        serviceTemplateHistory.getServiceTemplateId());
        approveServiceTemplateRegistration(serviceTemplate);
        addCredentialForHuaweiCloud();
        ServiceOrder serviceOrder = deployService(serviceTemplate);
        UUID serviceId = serviceOrder.getServiceId();
        UUID deployOrderId = serviceOrder.getOrderId();
        boolean deployOrderIsCompleted = waitServiceOrderIsCompleted(deployOrderId);
        if (deployOrderIsCompleted) {
            DeployedServiceDetails deployedServiceDetails = getDeployedServiceDetails(serviceId);

            UserOrderableServiceVo orderableServiceDetails =
                    serviceDeployerApi.getOrderableServiceDetailsByServiceId(serviceId);
            assertEquals(
                    orderableServiceDetails.getServiceTemplateId(),
                    serviceTemplateHistory.getServiceTemplateId());
            ServiceDeploymentState serviceDeploymentState =
                    deployedServiceDetails.getServiceDeploymentState();

            if (serviceDeploymentState.equals(ServiceDeploymentState.DEPLOY_SUCCESS)) {
                testModifyAndGetDetails(serviceId, serviceTemplate);
                ServiceOrder servicePortingOrder = portService(serviceId, serviceTemplate);
                if (waitServiceOrderIsCompleted(servicePortingOrder.getOrderId())) {
                    ServiceOrderDetails serviceOrderDetails =
                            serviceOrderManageApi.getOrderDetailsByOrderId(
                                    servicePortingOrder.getOrderId());
                    testDestroyAndGetDetails(serviceOrderDetails.getServiceId());
                    testListServiceOrders(serviceId);
                    testDeleteServiceOrders(serviceId);
                    testPurgeAndGetDetails(serviceOrderDetails.getOriginalServiceId());
                }
            } else {
                testListServiceOrders(serviceId);
                testDeleteServiceOrders(serviceId);
                testPurgeAndGetDetails(serviceId);
            }
        }
    }

    void approveServiceTemplateRegistration(ServiceTemplateDetailVo serviceTemplate) {
        ServiceTemplateEntity serviceTemplateEntity =
                serviceTemplateStorage.getServiceTemplateById(
                        serviceTemplate.getServiceTemplateId());
        serviceTemplateEntity.setServiceTemplateRegistrationState(
                ServiceTemplateRegistrationState.APPROVED);
        serviceTemplateEntity.setIsAvailableInCatalog(true);
        serviceTemplateStorage.storeAndFlush(serviceTemplateEntity);
    }

    ServiceOrder deployService(ServiceTemplateDetailVo serviceTemplate) {
        DeployRequest deployRequest = new DeployRequest();
        DeployRequestBase deployRequestBase = getDeployRequestBase(serviceTemplate);
        BeanUtils.copyProperties(deployRequestBase, deployRequest);
        deployRequest.setBillingMode(BillingMode.FIXED);
        ServiceOrder serviceOrder = serviceDeployerApi.deploy(deployRequest);
        Assertions.assertNotNull(serviceOrder.getOrderId());
        Assertions.assertNotNull(serviceOrder.getServiceId());
        return serviceOrder;
    }

    DeployedServiceDetails getDeployedServiceDetails(UUID serviceId) {
        return serviceDeployerApi.getSelfHostedServiceDetailsById(serviceId);
    }

    private boolean waitServiceOrderIsCompleted(UUID orderId) throws Exception {
        final long endTime = System.nanoTime() + TimeUnit.MINUTES.toNanos(2);
        boolean isCompleted = false;
        while (System.nanoTime() < endTime && !isCompleted) {
            isCompleted = serviceOrderIsCompleted(orderId);
            Thread.sleep(5000);
        }
        return isCompleted;
    }

    private boolean serviceOrderIsCompleted(UUID orderId) throws InterruptedException {
        DeferredResult<ServiceOrderStatusUpdate> deferredResult =
                serviceOrderManageApi.getLatestServiceOrderStatus(orderId, null);
        while (Objects.isNull(deferredResult.getResult())) {
            Thread.sleep(1000);
        }
        if (deferredResult.getResult() instanceof ServiceOrderStatusUpdate statusUpdate) {
            return statusUpdate.getIsOrderCompleted();
        }
        return false;
    }

    DeployRequestBase getDeployRequestBase(ServiceTemplateDetailVo serviceTemplate) {
        DeployRequestBase deployRequestBase = new DeployRequestBase();
        deployRequestBase.setServiceName(serviceTemplate.getName());
        deployRequestBase.setVersion(serviceTemplate.getVersion());
        deployRequestBase.setCsp(serviceTemplate.getCsp());
        deployRequestBase.setCategory(serviceTemplate.getCategory());
        deployRequestBase.setFlavor(
                serviceTemplate.getFlavors().getServiceFlavors().getFirst().getName());
        deployRequestBase.setRegion(serviceTemplate.getRegions().getFirst());
        deployRequestBase.setServiceHostingType(serviceTemplate.getServiceHostingType());
        deployRequestBase.setBillingMode(serviceTemplate.getBilling().getBillingModes().getFirst());

        Map<String, Object> serviceRequestProperties = new HashMap<>();
        serviceRequestProperties.put("admin_passwd", "111111111@Qq");
        deployRequestBase.setServiceRequestProperties(serviceRequestProperties);

        List<AvailabilityZoneConfig> availabilityZoneConfigs =
                serviceTemplate.getDeployment().getServiceAvailabilityConfig();
        Map<String, String> availabilityZones = new HashMap<>();
        availabilityZoneConfigs.forEach(
                availabilityZoneConfig ->
                        availabilityZones.put(
                                availabilityZoneConfig.getVarName(),
                                availabilityZoneConfig.getDisplayName()));
        deployRequestBase.setAvailabilityZones(availabilityZones);
        return deployRequestBase;
    }

    ServiceTemplateRequestInfo registerServiceTemplate() throws Exception {
        Ocl ocl =
                oclLoader.getOcl(
                        URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.setName(UUID.randomUUID().toString());
        return serviceTemplateApi.register(ocl);
    }

    ServiceOrder portService(UUID serviceId, ServiceTemplateDetailVo serviceTemplate) {
        ServicePortingRequest servicePortingRequest = new ServicePortingRequest();
        servicePortingRequest.setOriginalServiceId(serviceId);
        DeployRequestBase deployRequestBase = getDeployRequestBase(serviceTemplate);
        BeanUtils.copyProperties(deployRequestBase, servicePortingRequest);
        return servicePortingApi.port(servicePortingRequest);
    }

    void testModifyAndGetDetails(UUID serviceId, ServiceTemplateDetailVo serviceTemplate)
            throws Exception {
        // SetUp
        ModifyRequest modifyRequest = new ModifyRequest();
        modifyRequest.setFlavor(
                serviceTemplate.getFlavors().getServiceFlavors().getLast().getName());
        Map<String, Object> serviceRequestProperties = new HashMap<>();
        serviceRequestProperties.put("admin_passwd", "2222222222@Qq");
        modifyRequest.setServiceRequestProperties(serviceRequestProperties);
        // Run the test
        ServiceOrder serviceOrder = serviceDeployerApi.modify(serviceId, modifyRequest);
        // Verify the results
        Assertions.assertNotNull(serviceOrder.getOrderId());
        Assertions.assertNotNull(serviceOrder.getServiceId());
        if (waitServiceOrderIsCompleted(serviceOrder.getOrderId())) {
            ServiceOrderDetails serviceOrderDetails =
                    serviceOrderManageApi.getOrderDetailsByOrderId(serviceOrder.getOrderId());
            assertEquals(serviceOrderDetails.getTaskStatus(), TaskStatus.SUCCESSFUL);
            assertEquals(serviceOrderDetails.getTaskType(), ServiceOrderType.MODIFY);
        }
    }

    void testDestroyAndGetDetails(UUID serviceId) throws Exception {

        // Run the test
        ServiceOrder serviceOrder = serviceDeployerApi.destroy(serviceId);
        if (waitServiceOrderIsCompleted(serviceOrder.getOrderId())) {
            ServiceOrderDetails serviceOrderDetails =
                    serviceOrderManageApi.getOrderDetailsByOrderId(serviceOrder.getOrderId());
            assertEquals(serviceOrderDetails.getTaskStatus(), TaskStatus.SUCCESSFUL);
            assertEquals(serviceOrderDetails.getTaskType(), ServiceOrderType.DESTROY);
        }
    }

    void testPurgeAndGetDetails(UUID serviceId) {
        // Run the test
        ServiceOrder serviceOrder = serviceDeployerApi.purge(serviceId);
        try {
            waitServiceOrderIsCompleted(serviceOrder.getOrderId());
        } catch (Exception e) {
            if (e instanceof ServiceOrderNotFound) {
                return;
            }
        }
    }

    void testListServiceOrders(UUID serviceId) {
        List<ServiceOrderDetails> serviceOrders =
                serviceOrderManageApi.getAllOrdersByServiceId(serviceId, null, null);
        Assertions.assertNotNull(serviceOrders);
    }

    void testDeleteServiceOrders(UUID serviceId) {
        serviceOrderManageApi.deleteOrdersByServiceId(serviceId);
    }

    private void addCredentialForHuaweiCloud() {
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
        userCloudCredentialsApi.addUserCloudCredential(createCredential);
    }
}
