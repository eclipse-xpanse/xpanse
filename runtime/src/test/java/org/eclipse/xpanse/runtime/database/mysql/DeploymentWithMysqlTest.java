/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.runtime.database.mysql;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import jakarta.annotation.Resource;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.eclipse.xpanse.api.controllers.ServiceDeployerApi;
import org.eclipse.xpanse.api.controllers.ServiceMigrationApi;
import org.eclipse.xpanse.api.controllers.ServiceModificationApi;
import org.eclipse.xpanse.api.controllers.ServiceTemplateApi;
import org.eclipse.xpanse.api.controllers.UserCloudCredentialsApi;
import org.eclipse.xpanse.modules.database.service.DatabaseDeployServiceStorage;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.DatabaseServiceTemplateStorage;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.models.billing.enums.BillingMode;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.CreateCredential;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequestBase;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.modify.ModifyRequest;
import org.eclipse.xpanse.modules.models.service.modify.ServiceModificationAuditDetails;
import org.eclipse.xpanse.modules.models.service.utils.ServiceVariablesJsonSchemaGenerator;
import org.eclipse.xpanse.modules.models.servicetemplate.AvailabilityZoneConfig;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceRegistrationState;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotApproved;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.eclipse.xpanse.modules.models.workflow.migrate.MigrateRequest;
import org.eclipse.xpanse.modules.models.workflow.migrate.enums.MigrationStatus;
import org.eclipse.xpanse.modules.models.workflow.migrate.view.ServiceMigrationDetails;
import org.eclipse.xpanse.plugins.huaweicloud.monitor.constant.HuaweiCloudMonitorConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=oauth,zitadel,zitadel-testbed,mysql"})
@AutoConfigureMockMvc
class DeploymentWithMysqlTest extends AbstractMysqlIntegrationTest {
    @Resource
    private UserCloudCredentialsApi userCloudCredentialsApi;
    @Resource
    private ServiceDeployerApi serviceDeployerApi;
    @Resource
    private ServiceModificationApi serviceModificationApi;
    @Resource
    private ServiceTemplateApi serviceTemplateApi;
    @Resource
    private DatabaseDeployServiceStorage deployServiceStorage;
    @Resource
    private ServiceVariablesJsonSchemaGenerator serviceVariablesJsonSchemaGenerator;
    @Resource
    private OclLoader oclLoader;
    @Resource
    private ServiceMigrationApi serviceMigrationApi;
    @Resource
    private DatabaseServiceTemplateStorage serviceTemplateStorage;

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testDeployer() throws Exception {
        ServiceTemplateDetailVo serviceTemplate = registerServiceTemplate();
        testDeployServiceThrowsServiceTemplateNotApproved(serviceTemplate);
        approveServiceTemplateRegistration(serviceTemplate);
        addCredentialForHuaweiCloud();
        UUID serviceId = deployService(serviceTemplate);
        if (waitServiceUtilTargetState(serviceId, ServiceDeploymentState.DEPLOY_SUCCESS)) {
            testModifyAndGetDetails(serviceId, serviceTemplate);
            waitServiceUtilTargetState(serviceId,
                    ServiceDeploymentState.MODIFICATION_SUCCESSFUL);
            UUID migrateId = migrateService(serviceId, serviceTemplate);
            if (waitMigrationCompleted(migrateId)) {
                ServiceMigrationDetails migrationDetails =
                        serviceMigrationApi.getMigrationOrderDetailsById(migrateId.toString());
                testDestroyAndGetDetails(migrationDetails.getNewServiceId());
                testPurgeAndGetDetails(migrationDetails.getOldServiceId());
            }
        } else {
            if (serviceIsTargetState(serviceId, ServiceDeploymentState.DEPLOY_FAILED)) {
                testPurgeAndGetDetails(serviceId);
            }
        }
    }

    void approveServiceTemplateRegistration(ServiceTemplateDetailVo serviceTemplate) {
        ServiceTemplateEntity serviceTemplateEntity =
                serviceTemplateStorage.getServiceTemplateById(serviceTemplate.getServiceTemplateId());
        serviceTemplateEntity.setServiceRegistrationState(ServiceRegistrationState.APPROVED);
        serviceTemplateStorage.storeAndFlush(serviceTemplateEntity);
    }

    UUID deployService(ServiceTemplateDetailVo serviceTemplate) {
        DeployRequest deployRequest = new DeployRequest();
        DeployRequestBase deployRequestBase = getDeployRequestBase(serviceTemplate);
        BeanUtils.copyProperties(deployRequestBase, deployRequest);
        deployRequest.setBillingMode(BillingMode.FIXED);
        UUID deployUUid = serviceDeployerApi.deploy(deployRequest);
        Assertions.assertNotNull(deployUUid);
        return deployUUid;
    }


    boolean waitServiceUtilTargetState(UUID id, ServiceDeploymentState targetState)
            throws InterruptedException {
        final long endTime = System.nanoTime() + TimeUnit.MINUTES.toNanos(2);
        while (true) {
            if (serviceIsTargetState(id, targetState)) {
                return true;
            }
            if (System.nanoTime() > endTime) {
                return false;
            }
            Thread.sleep(TimeUnit.SECONDS.toMillis(5));
        }
    }

    private boolean serviceIsTargetState(UUID id, ServiceDeploymentState targetState) {
        DeployServiceEntity deployedService = deployServiceStorage.findDeployServiceById(id);
        return Objects.nonNull(deployedService)
                && deployedService.getServiceDeploymentState() == targetState;
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
                serviceTemplate.getDeployment().getServiceAvailabilityConfigs();
        Map<String, String> availabilityZones = new HashMap<>();
        availabilityZoneConfigs.forEach(availabilityZoneConfig ->
                availabilityZones.put(availabilityZoneConfig.getVarName(),
                        availabilityZoneConfig.getDisplayName()));
        deployRequestBase.setAvailabilityZones(availabilityZones);
        return deployRequestBase;
    }

    ServiceTemplateDetailVo registerServiceTemplate() throws Exception {
        Ocl ocl = oclLoader.getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        return serviceTemplateApi.register(ocl);
    }

    void testDeployServiceThrowsServiceTemplateNotApproved(
            ServiceTemplateDetailVo serviceTemplate) {
        // SetUp
        DeployRequest deployRequest = new DeployRequest();
        DeployRequestBase deployRequestBase = getDeployRequestBase(serviceTemplate);
        BeanUtils.copyProperties(deployRequestBase, deployRequest);
        // run the test
        assertThrows(ServiceTemplateNotApproved.class,
                () -> serviceDeployerApi.deploy(deployRequest));
    }

    UUID migrateService(UUID taskId, ServiceTemplateDetailVo serviceTemplate) {
        MigrateRequest migrateRequest = new MigrateRequest();
        migrateRequest.setOriginalServiceId(taskId);
        DeployRequestBase deployRequestBase = getDeployRequestBase(serviceTemplate);
        BeanUtils.copyProperties(deployRequestBase, migrateRequest);
        UUID migrateId = serviceMigrationApi.migrate(migrateRequest);
        // Verify the results
        Assertions.assertNotNull(migrateId);
        return migrateId;
    }

    boolean waitMigrationCompleted(UUID id) throws Exception {
        long startTime = System.currentTimeMillis();
        while (!migrationStatueIsCompleted(id)) {
            if (migrationStatueIsCompleted(id)) {
                break;
            } else {
                if (System.currentTimeMillis() - startTime > 300 * 1000) {
                    break;
                }
                Thread.sleep(5 * 1000);
            }
        }
        return migrationStatueIsCompleted(id);
    }

    private boolean migrationStatueIsCompleted(UUID id) {
        ServiceMigrationDetails migrationDetails =
                serviceMigrationApi.getMigrationOrderDetailsById(id.toString());
        return migrationDetails.getMigrationStatus() == MigrationStatus.MIGRATION_COMPLETED;
    }

    void testModifyAndGetDetails(UUID serviceId, ServiceTemplateDetailVo serviceTemplate) {
        // SetUp
        ModifyRequest modifyRequest = new ModifyRequest();
        modifyRequest.setFlavor(
                serviceTemplate.getFlavors().getServiceFlavors().getLast().getName());
        Map<String, Object> serviceRequestProperties = new HashMap<>();
        serviceRequestProperties.put("admin_passwd", "2222222222@Qq");
        modifyRequest.setServiceRequestProperties(serviceRequestProperties);
        // Run the test
        UUID modificationId = serviceModificationApi.modify(serviceId.toString(), modifyRequest);
        // Verify the results
        Assertions.assertNotNull(modificationId);

        ServiceModificationAuditDetails serviceModificationDetails =
                serviceModificationApi.getAuditDetailsByModificationId(modificationId.toString());
        Assertions.assertNotNull(serviceModificationDetails);
        Assertions.assertEquals(serviceModificationDetails.getServiceId(), serviceId);
    }

    void testDestroyAndGetDetails(UUID taskId) throws Exception {

        // SetUp
        String successMsg =
                String.format("Task for destroying managed service %s has started.", taskId);
        Response response = Response.successResponse(Collections.singletonList(successMsg));

        // Run the test
        Response result = serviceDeployerApi.destroy(taskId.toString());

        // Verify the results
        Assertions.assertEquals(response, result);

        if (serviceIsTargetState(taskId, ServiceDeploymentState.DESTROY_SUCCESS)) {
            // Run the test
            testPurgeAndGetDetails(taskId);
        }

    }

    void testPurgeAndGetDetails(UUID taskId) throws Exception {
        // SetUp
        String successMsg =
                String.format("Task for purging managed service %s has started.", taskId);
        Response response = Response.successResponse(Collections.singletonList(successMsg));
        // Run the test
        Response result = serviceDeployerApi.purge(taskId.toString());
        Assertions.assertEquals(result, response);

        Thread.sleep(30 * 1000);

        assertThrows(ServiceNotDeployedException.class,
                () -> serviceDeployerApi.getSelfHostedServiceDetailsById(taskId.toString()));
    }

    private void addCredentialForHuaweiCloud() {
        final CreateCredential createCredential = new CreateCredential();
        createCredential.setCsp(Csp.HUAWEI_CLOUD);
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
        userCloudCredentialsApi.addUserCloudCredential(createCredential);
    }
}
