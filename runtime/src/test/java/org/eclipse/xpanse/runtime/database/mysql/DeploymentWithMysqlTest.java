/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.runtime.database.mysql;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.eclipse.xpanse.api.controllers.ServiceDeployerApi;
import org.eclipse.xpanse.api.controllers.ServiceTemplateApi;
import org.eclipse.xpanse.modules.database.service.DatabaseDeployServiceStorage;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.workflow.migrate.MigrateRequest;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.service.utils.ServiceVariablesJsonSchemaGenerator;
import org.eclipse.xpanse.modules.models.service.view.DeployedServiceDetails;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceRegistrationState;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=zitadel,mysql,zitadel-testbed"})
@AutoConfigureMockMvc
class DeploymentWithMysqlTest extends AbstractMysqlIntegrationTest {

    static ServiceTemplateDetailVo serviceTemplate;
    @Autowired
    ServiceDeployerApi serviceDeployerApi;
    @Autowired
    ServiceTemplateApi ServiceTemplateApi;
    @Autowired
    DatabaseDeployServiceStorage deployServiceStorage;
    @Autowired
    ServiceVariablesJsonSchemaGenerator serviceVariablesJsonSchemaGenerator;
    @Autowired
    OclLoader oclLoader;

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testDeployer() throws Exception {
        if (Objects.isNull(serviceTemplate)) {
            registerServiceTemplate();
        }
        UUID serviceId = deployService();

        if (waitServiceUtilTargetState(serviceId, ServiceDeploymentState.DEPLOY_FAILED)) {
            UUID newServiceId = migrateService(serviceId);
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

    UUID deployService() {
        DeployRequest deployRequest = new DeployRequest();
        deployRequest.setUserId("userId");
        deployRequest.setServiceName(serviceTemplate.getName());
        deployRequest.setVersion(serviceTemplate.getVersion());
        deployRequest.setCsp(serviceTemplate.getCsp());
        deployRequest.setCategory(serviceTemplate.getCategory());
        deployRequest.setFlavor(serviceTemplate.getFlavors().get(0).getName());
        deployRequest.setRegion(serviceTemplate.getRegions().get(0).getName());
        deployRequest.setServiceHostingType(ServiceHostingType.SELF);
        Map<String, Object> serviceRequestProperties = new HashMap<>();
        serviceRequestProperties.put("admin_passwd", "111111111@Qq");
        deployRequest.setServiceRequestProperties(serviceRequestProperties);

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
        return Objects.nonNull(deployedService) &&
                deployedService.getServiceDeploymentState() == targetState;
    }

    void registerServiceTemplate() throws Exception {
        Ocl ocl = oclLoader.getOcl(URI.create("file:src/test/resources/ocl_test.yaml").toURL());
        serviceTemplate = ServiceTemplateApi.register(ocl);
        Assertions.assertNotNull(serviceTemplate);
        Assertions.assertEquals(ServiceRegistrationState.REGISTERED,
                serviceTemplate.getServiceRegistrationState());
        Assertions.assertEquals(serviceTemplate.getCsp(), ocl.getCloudServiceProvider().getName());
        Assertions.assertEquals(serviceTemplate.getName(), ocl.getName().toLowerCase());
        Assertions.assertTrue(serviceTemplate.getVariables().size() > 1);
    }

    UUID migrateService(UUID taskId) {

        MigrateRequest migrateRequest = new MigrateRequest();
        migrateRequest.setId(taskId);
        migrateRequest.setServiceName(serviceTemplate.getName());
        migrateRequest.setVersion(serviceTemplate.getVersion());
        migrateRequest.setCsp(serviceTemplate.getCsp());
        migrateRequest.setCategory(serviceTemplate.getCategory());
        migrateRequest.setFlavor(serviceTemplate.getFlavors().get(0).getName());
        migrateRequest.setRegion(serviceTemplate.getRegions().get(0).toString());
        migrateRequest.setServiceHostingType(serviceTemplate.getServiceHostingType());
        Map<String, Object> serviceRequestProperties = new HashMap<>();
        serviceRequestProperties.put("admin_passwd", "22222222@Qq");
        migrateRequest.setServiceRequestProperties(serviceRequestProperties);


        UUID newServiceId = serviceDeployerApi.migrate(migrateRequest);
        // Verify the results
        Assertions.assertNotNull(newServiceId);
        Assertions.assertNotEquals(taskId, newServiceId);
        return newServiceId;

    }

    void testDestroyAndGetDetails(UUID taskId) throws Exception {

        // SetUp
        String successMsg = String.format(
                "Task for destroying managed service %s has started.", taskId);
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
        String successMsg = String.format(
                "Purging task for service with ID %s has started.", taskId);
        Response response = Response.successResponse(Collections.singletonList(successMsg));
        // Run the test
        Response result = serviceDeployerApi.purge(taskId.toString());
        Assertions.assertEquals(result, response);

        Thread.sleep(30 * 1000);

        Assertions.assertThrows(ServiceNotDeployedException.class,
                () -> serviceDeployerApi.getSelfHostedServiceDetailsById(taskId.toString()));
    }
}
