/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.xpanse.modules.deployment.deployers.terraform.TerraformDeployment.STATE_FILE_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.resource.DeployResourceStorage;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceStorage;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.config.TerraformLocalConfig;
import org.eclipse.xpanse.modules.deployment.utils.DeployEnvironments;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResult;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.deploy.enums.TerraformExecState;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.TerraformExecutorException;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployValidateDiagnostics;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployValidationResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Test for TerraformDeploy.
 */

@ExtendWith({SpringExtension.class})
@ContextConfiguration(classes = {TerraformDeployment.class, DeployEnvironments.class,
        PluginManager.class, DeployServiceStorage.class, DeployResourceStorage.class,
        TerraformLocalConfig.class})
class TerraformDeploymentTest {

    private final UUID id = UUID.randomUUID();
    private final String errorDeployer = "error_deployer";
    private final String invalidDeployer = """
            resource "random_id" "new" {
              byte_length = 4
            }
                                    
            output "random_id" {
              value = resource.random_id_2.new.id
            }
            """;
    private DeployTask deployTask;
    private DeployServiceEntity deployServiceEntity;
    private DeployRequest deployRequest;
    private DeployResult deployResult;
    private Ocl ocl;
    private String tfState;

    @Autowired
    TerraformDeployment terraformDeployment;

    @MockBean
    DeployEnvironments deployEnvironments;

    @MockBean
    DeployServiceStorage deployServiceStorage;

    @MockBean
    DeployResourceStorage deployResourceStorage;

    @MockBean
    TerraformLocalConfig terraformLocalConfig;

    @MockBean
    PluginManager pluginManager;

    @BeforeEach
    void setUp() throws Exception {
        OclLoader oclLoader = new OclLoader();
        ocl = oclLoader.getOcl(URI.create("file:src/test/resources/ocl_test.yaml").toURL());

        deployRequest = new DeployRequest();
        deployRequest.setOcl(ocl);
        deployRequest.setServiceName(ocl.getName());
        deployRequest.setVersion(ocl.getServiceVersion());
        deployRequest.setFlavor(ocl.getFlavors().get(0).getName());
        deployRequest.setRegion(ocl.getCloudServiceProvider().getRegions().get(0).getName());
        deployRequest.setCsp(ocl.getCloudServiceProvider().getName());
        deployRequest.setCategory(ocl.getCategory());
        deployRequest.setCustomerServiceName("test_deploy");
        deployRequest.setServiceRequestProperties(Map.ofEntries(Map.entry("key", "value")));

        deployTask = new DeployTask();
        deployTask.setId(id);
        deployTask.setOcl(ocl);
        deployTask.setDeployResourceHandler(null);
        deployTask.setDeployRequest(deployRequest);


        doReturn(new HashMap<>()).when(this.deployEnvironments)
                .getCredentialVariablesByHostingType(any(DeployTask.class));

        doReturn("""
                terraform {
                  required_providers {
                openstack = {
                          source  = "terraform-provider-openstack/openstack"
                          version = ">= 1.48.0"
                        }
                  }
                }
                            
                provider "openstack" {
                  region = "test"
                }
                """).when(this.pluginManager)
                .getTerraformProviderForRegionByCsp(any(Csp.class), any());
    }

    @Test
    @Order(1)
    void testDeploy() {
        DeployRequest deployRequest = new DeployRequest();
        deployRequest.setOcl(ocl);
        deployRequest.setServiceName(ocl.getName());
        deployRequest.setVersion(ocl.getServiceVersion());
        deployRequest.setFlavor(ocl.getFlavors().get(0).getName());
        deployRequest.setRegion(ocl.getCloudServiceProvider().getRegions().get(0).getName());
        deployRequest.setCsp(ocl.getCloudServiceProvider().getName());
        deployRequest.setCategory(ocl.getCategory());
        deployRequest.setCustomerServiceName("test_deploy");
        deployRequest.setServiceRequestProperties(Map.ofEntries(Map.entry("key", "value")));

        deployServiceEntity = new DeployServiceEntity();
        deployServiceEntity.setId(id);
        deployServiceEntity.setName(ocl.getName());
        deployServiceEntity.setVersion(ocl.getServiceVersion());
        deployServiceEntity.setUserId("userId");
        deployServiceEntity.setServiceDeploymentState(ServiceDeploymentState.DEPLOYING);
        deployServiceEntity.setDeployRequest(deployRequest);
        deployServiceEntity.setCategory(ocl.getCategory());
        deployServiceEntity.setCsp(ocl.getCloudServiceProvider().getName());
        deployServiceEntity.setCustomerServiceName("test_deploy");

        deployTask = new DeployTask();
        deployTask.setId(id);
        deployTask.setOcl(ocl);
        deployTask.setDeployResourceHandler(null);
        deployTask.setDeployRequest(deployRequest);

        when(deployServiceStorage.findDeployServiceById(id)).thenReturn(deployServiceEntity);
        when(deployServiceStorage.storeAndFlush(deployServiceEntity)).thenReturn(
                deployServiceEntity);

        deployResult = terraformDeployment.deploy(deployTask);
        Assertions.assertNotNull(deployResult);
        Assertions.assertNotNull(deployResult.getPrivateProperties());
        tfState = deployResult.getPrivateProperties().get(STATE_FILE_NAME);
        Assertions.assertNotNull(tfState);
        Assertions.assertEquals(TerraformExecState.DEPLOY_SUCCESS, deployResult.getState());

    }

    @Test
    @Order(2)
    void testDestroy() {
        if (StringUtils.isBlank(tfState)) {
            testDeploy();
        }
        when(this.deployEnvironments.getFlavorVariables(any(DeployTask.class))).thenReturn(
                new HashMap<>());
        when(deployServiceStorage.storeAndFlush(deployServiceEntity)).thenReturn(
                deployServiceEntity);

        when(deployServiceStorage.findDeployServiceById(id)).thenReturn(deployServiceEntity);
        DeployResult destroyResult = this.terraformDeployment.destroy(deployTask,
                deployResult.getPrivateProperties().get(STATE_FILE_NAME));

        Assertions.assertNotNull(destroyResult);
        Assertions.assertNotNull(destroyResult.getPrivateProperties());
        Assertions.assertNull(destroyResult.getPrivateProperties().get(STATE_FILE_NAME));
        Assertions.assertEquals(TerraformExecState.DESTROY_SUCCESS, destroyResult.getState());
    }


    @Test
    void testDeploy_ThrowsTerraformExecutorException() {
        ocl.getDeployment().setDeployer(errorDeployer);

        Assertions.assertThrows(TerraformExecutorException.class,
                () -> this.terraformDeployment.deploy(deployTask));

        ocl.getDeployment().setDeployer(invalidDeployer);

        Assertions.assertThrows(TerraformExecutorException.class,
                () -> this.terraformDeployment.deploy(deployTask));
    }

    @Test
    void testDestroy_ThrowsException() {

        Assertions.assertThrows(ServiceNotDeployedException.class,
                () -> this.terraformDeployment.destroy(deployTask, ""));

        Assertions.assertThrows(TerraformExecutorException.class,
                () -> this.terraformDeployment.destroy(deployTask, "error_tdState"));
    }

    @Test
    void testGetDeployerKind() {
        DeployerKind deployerKind = terraformDeployment.getDeployerKind();

        Assertions.assertEquals(DeployerKind.TERRAFORM, deployerKind);
    }

    @Test
    void testGetDeployPlanAsJson() {
        DeployRequest deployRequest = new DeployRequest();
        deployRequest.setOcl(ocl);
        deployRequest.setServiceName(ocl.getName());
        deployRequest.setVersion(ocl.getServiceVersion());
        deployRequest.setFlavor(ocl.getFlavors().get(0).getName());
        deployRequest.setRegion(ocl.getCloudServiceProvider().getRegions().get(0).getName());
        deployRequest.setCsp(ocl.getCloudServiceProvider().getName());
        deployRequest.setCategory(ocl.getCategory());
        deployRequest.setCustomerServiceName("test_deploy");
        deployRequest.setServiceRequestProperties(Map.ofEntries(Map.entry("key", "value")));

        DeployTask deployTask = new DeployTask();
        deployTask.setId(id);
        deployTask.setOcl(ocl);
        deployTask.setDeployResourceHandler(null);
        deployTask.setDeployRequest(deployRequest);

        String deployPlanJson = terraformDeployment.getDeployPlanAsJson(deployTask);
        Assertions.assertNotNull(deployPlanJson);

    }

    @Test
    void testGetDeployPlanAsJson_ThrowsException() {

        ocl.getDeployment().setDeployer(errorDeployer);

        Assertions.assertThrows(TerraformExecutorException.class,
                () -> this.terraformDeployment.getDeployPlanAsJson(deployTask));

    }

    @Test
    void testValidate() {
        DeployValidationResult expectedResult = new DeployValidationResult();
        expectedResult.setValid(true);
        expectedResult.setDiagnostics(new ArrayList<>());

        // Run the test
        final DeployValidationResult result = terraformDeployment.validate(ocl);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testValidateFailed() {
        ocl.getDeployment().setDeployer(invalidDeployer);

        DeployValidationResult expectedResult = new DeployValidationResult();
        expectedResult.setValid(false);
        DeployValidateDiagnostics diagnostics = new DeployValidateDiagnostics();
        diagnostics.setDetail(
                "A managed resource \"random_id_2\" \"new\" has not been declared in the root module.");
        expectedResult.setDiagnostics(List.of(diagnostics));

        // Run the test
        final DeployValidationResult result = terraformDeployment.validate(ocl);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testValidate_ThrowsTerraformExecutorException() {
        ocl.getDeployment().setDeployer(errorDeployer);

        Assertions.assertThrows(TerraformExecutorException.class,
                () -> this.terraformDeployment.validate(ocl));
    }

}
