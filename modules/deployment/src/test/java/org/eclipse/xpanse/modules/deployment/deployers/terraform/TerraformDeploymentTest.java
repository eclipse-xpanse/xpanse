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

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.config.TerraformLocalConfig;
import org.eclipse.xpanse.modules.deployment.utils.DeployEnvironments;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResult;
import org.eclipse.xpanse.modules.models.service.deploy.enums.TerraformExecState;
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
        PluginManager.class, TerraformLocalConfig.class})
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
    private DeployRequest deployRequest;
    private DeployResult deployResult;
    private Ocl ocl;
    private String tfState;

    @Autowired
    TerraformDeployment terraformDeployment;

    @MockBean
    DeployEnvironments deployEnvironments;


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
        DeployTask deployTask = new DeployTask();
        deployTask.setId(id);
        deployTask.setOcl(ocl);
        deployTask.setDeployResourceHandler(null);
        deployTask.setDeployRequest(deployRequest);
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
        DeployTask deployTask = new DeployTask();
        deployTask.setId(id);
        deployTask.setOcl(ocl);
        deployTask.setDeployResourceHandler(null);
        deployTask.setDeployRequest(deployRequest);
        if (StringUtils.isBlank(tfState)) {
            testDeploy();
        }
        when(this.deployEnvironments.getFlavorVariables(any(DeployTask.class))).thenReturn(
                new HashMap<>());
        DeployResult destroyResult = this.terraformDeployment.destroy(deployTask,
                deployResult.getPrivateProperties().get(STATE_FILE_NAME));

        Assertions.assertNotNull(destroyResult);
        Assertions.assertNotNull(destroyResult.getPrivateProperties());
        Assertions.assertNull(destroyResult.getPrivateProperties().get(STATE_FILE_NAME));
        Assertions.assertEquals(TerraformExecState.DESTROY_SUCCESS, destroyResult.getState());
    }

    @Test
    @Order(3)
    void testDeleteTaskWorkspace() {
        String workspacePath = System.getProperty("java.io.tmpdir") + File.separator
                + terraformLocalConfig.getWorkspaceDirectory() + File.separator + id;
        this.terraformDeployment.deleteTaskWorkspace(id.toString());
        Assertions.assertFalse(new File(workspacePath).exists());
    }

    @Test
    void testDeploy_FailedCausedByTerraformExecutorException() {
        ocl.getDeployment().setDeployer(invalidDeployer);
        DeployTask deployTask = new DeployTask();
        deployTask.setId(UUID.randomUUID());
        deployTask.setOcl(ocl);
        deployTask.setDeployResourceHandler(null);
        deployTask.setDeployRequest(deployRequest);
        DeployResult deployResult = this.terraformDeployment.deploy(deployTask);
        Assertions.assertEquals(TerraformExecState.DEPLOY_FAILED, deployResult.getState());
        Assertions.assertNotEquals(TerraformExecState.DEPLOY_SUCCESS.toValue(),
                deployResult.getMessage());

    }

    @Test
    void testDestroy_FailedCausedByTerraformExecutorException() {
        ocl.getDeployment().setDeployer(errorDeployer);
        DeployTask deployTask = new DeployTask();
        deployTask.setId(id);
        deployTask.setOcl(ocl);
        deployTask.setDeployResourceHandler(null);
        deployTask.setDeployRequest(deployRequest);
        DeployResult deployResult = this.terraformDeployment.destroy(deployTask, "error_tfState");
        Assertions.assertTrue(deployResult.getProperties().isEmpty());
        Assertions.assertEquals(TerraformExecState.DESTROY_FAILED, deployResult.getState());
        Assertions.assertNotEquals(TerraformExecState.DESTROY_FAILED.toValue(),
                deployResult.getMessage());
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
        DeployTask deployTask = new DeployTask();
        deployTask.setId(UUID.randomUUID());
        deployTask.setOcl(ocl);
        deployTask.setDeployResourceHandler(null);
        deployTask.setDeployRequest(deployRequest);
        deployResult = this.terraformDeployment.deploy(deployTask);

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
