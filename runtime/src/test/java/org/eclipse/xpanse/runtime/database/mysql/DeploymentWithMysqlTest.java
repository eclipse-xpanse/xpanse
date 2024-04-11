/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.runtime.database.mysql;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import jakarta.annotation.Resource;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.eclipse.xpanse.api.controllers.ServiceDeployerApi;
import org.eclipse.xpanse.api.controllers.ServiceMigrationApi;
import org.eclipse.xpanse.api.controllers.ServiceTemplateApi;
import org.eclipse.xpanse.modules.database.service.DatabaseDeployServiceStorage;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.DatabaseServiceTemplateStorage;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequestBase;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.service.utils.ServiceVariablesJsonSchemaGenerator;
import org.eclipse.xpanse.modules.models.service.view.DeployedServiceDetails;
import org.eclipse.xpanse.modules.models.servicetemplate.AvailabilityZoneConfig;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceRegistrationState;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotApproved;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.eclipse.xpanse.modules.models.workflow.migrate.MigrateRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=oauth,zitadel,zitadel-testbed,mysql"})
@AutoConfigureMockMvc
class DeploymentWithMysqlTest extends AbstractMysqlIntegrationTest {

    @Resource
    private ServiceDeployerApi serviceDeployerApi;
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
        UUID serviceId = deployService(serviceTemplate);

        if (waitServiceUtilTargetState(serviceId, ServiceDeploymentState.DEPLOY_FAILED)) {
            UUID newServiceId = migrateService(serviceId, serviceTemplate);
            if (serviceIsTargetState(newServiceId, ServiceDeploymentState.DEPLOY_SUCCESS)) {
                DeployedServiceDetails newServiceDetails =
                        serviceDeployerApi.getSelfHostedServiceDetailsById(newServiceId.toString());
                Assertions.assertNotNull(newServiceDetails);
                Assertions.assertEquals(ServiceDeploymentState.DEPLOY_SUCCESS,
                        newServiceDetails.getServiceDeploymentState());
                Assertions.assertFalse(newServiceDetails.getDeployedServiceProperties().isEmpty());

                testDestroyAndGetDetails(newServiceId);
            } else {
                if (serviceIsTargetState(serviceId, ServiceDeploymentState.DEPLOY_FAILED)) {
                    testPurgeAndGetDetails(serviceId);
                }
            }
            if (serviceIsTargetState(serviceId, ServiceDeploymentState.DESTROY_SUCCESS)) {
                DeployedServiceDetails oldServiceDetails =
                        serviceDeployerApi.getSelfHostedServiceDetailsById(serviceId.toString());
                Assertions.assertNotNull(oldServiceDetails);
                Assertions.assertEquals(ServiceDeploymentState.DESTROY_SUCCESS,
                        oldServiceDetails.getServiceDeploymentState());
                Assertions.assertNull(oldServiceDetails.getDeployedServiceProperties());
                testPurgeAndGetDetails(serviceId);
            } else {
                if (serviceIsTargetState(serviceId, ServiceDeploymentState.DEPLOY_FAILED)) {
                    testPurgeAndGetDetails(serviceId);
                }

            }
        } else {
            if (serviceIsTargetState(serviceId, ServiceDeploymentState.DEPLOY_FAILED)) {
                testPurgeAndGetDetails(serviceId);
            }
        }
    }

    void approveServiceTemplateRegistration(ServiceTemplateDetailVo serviceTemplate) {
        ServiceTemplateEntity serviceTemplateEntity =
                serviceTemplateStorage.getServiceTemplateById(serviceTemplate.getId());
        serviceTemplateEntity.setServiceRegistrationState(ServiceRegistrationState.APPROVED);
        serviceTemplateStorage.storeAndFlush(serviceTemplateEntity);
    }

    UUID deployService(ServiceTemplateDetailVo serviceTemplate) {
        DeployRequest deployRequest = new DeployRequest();
        DeployRequestBase deployRequestBase = getDeployRequestBase(serviceTemplate);
        BeanUtils.copyProperties(deployRequestBase, deployRequest);
        UUID deployUUid = serviceDeployerApi.deploy(deployRequest);
        Assertions.assertNotNull(deployUUid);
        return deployUUid;
    }

    boolean waitServiceUtilTargetState(UUID id, ServiceDeploymentState targetState)
            throws Exception {
        long startTime = System.currentTimeMillis();
        while (!serviceIsTargetState(id, targetState)) {
            if (serviceIsTargetState(id, targetState)) {
                break;
            } else {
                if (System.currentTimeMillis() - startTime > 120 * 1000) {
                    break;
                }
                Thread.sleep(5 * 1000);
            }
        }
        return serviceIsTargetState(id, targetState);
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

        Map<String, Object> serviceRequestProperties = new HashMap<>();
        serviceRequestProperties.put("admin_passwd", "111111111@Qq");
        deployRequestBase.setServiceRequestProperties(serviceRequestProperties);

        List<AvailabilityZoneConfig> availabilityZoneConfigs =
                serviceTemplate.getDeployment().getServiceAvailability();
        Map<String, String> availabilityZones = new HashMap<>();
        availabilityZoneConfigs.forEach(availabilityZoneConfig -> {
            availabilityZones.put(availabilityZoneConfig.getVarName(),
                    availabilityZoneConfig.getDisplayName());
        });
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
        migrateRequest.setId(taskId);
        DeployRequestBase deployRequestBase = getDeployRequestBase(serviceTemplate);
        BeanUtils.copyProperties(deployRequestBase, migrateRequest);
        UUID migrateId = serviceMigrationApi.migrate(migrateRequest);
        // Verify the results
        Assertions.assertNotNull(migrateId);
        return migrateId;
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
                String.format("Purging task for service with ID %s has started.", taskId);
        Response response = Response.successResponse(Collections.singletonList(successMsg));
        // Run the test
        Response result = serviceDeployerApi.purge(taskId.toString());
        Assertions.assertEquals(result, response);

        Thread.sleep(30 * 1000);

        assertThrows(ServiceNotDeployedException.class,
                () -> serviceDeployerApi.getSelfHostedServiceDetailsById(taskId.toString()));
    }
}
