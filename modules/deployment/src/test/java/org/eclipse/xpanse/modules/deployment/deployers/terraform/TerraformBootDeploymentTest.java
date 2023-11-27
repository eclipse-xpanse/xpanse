/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.config.TerraformBootConfig;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.api.TerraformApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.TerraformPlan;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.TerraformValidateDiagnostics;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.TerraformValidationResult;
import org.eclipse.xpanse.modules.deployment.utils.DeployEnvironments;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResult;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.TerraformBootRequestFailedException;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployValidateDiagnostics;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployValidationResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test for TerraformDeploy.
 */

@ExtendWith(MockitoExtension.class)
class TerraformBootDeploymentTest {

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
    @Mock
    DeployEnvironments deployEnvironments;
    @Mock
    PluginManager pluginManager;
    @Mock
    TerraformApi terraformApi;
    @Mock
    TerraformBootConfig terraformBootConfig;

    @InjectMocks
    private TerraformBootDeployment terraformBootDeployment;

    private DeployTask deployTask;
    private Ocl ocl;

    @BeforeEach
    void setUp() throws Exception {

        OclLoader oclLoader = new OclLoader();
        ocl = oclLoader.getOcl(URI.create("file:src/test/resources/ocl_test.yaml").toURL());

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

        deployTask = new DeployTask();
        deployTask.setId(id);
        deployTask.setOcl(ocl);
        deployTask.setDeployResourceHandler(null);
        deployTask.setDeployRequest(deployRequest);
    }

    @Test
    void testDeploy() {
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

        DeployResult deployResult = terraformBootDeployment.deploy(deployTask);

        Assertions.assertNotNull(deployResult);
        Assertions.assertEquals(id, deployResult.getId());
    }

    @Test
    void testDestroy() {

        DeployResult destroyResult = this.terraformBootDeployment.destroy(deployTask, "tfstate");

        Assertions.assertNotNull(destroyResult);
        Assertions.assertEquals(id, destroyResult.getId());
    }


    @Test
    void testDeploy_ThrowsRestClientException() {
        ocl.getDeployment().setDeployer(errorDeployer);

        Mockito.doThrow(new TerraformBootRequestFailedException("IO error")).when(terraformApi)
                .asyncDeployWithScripts(any(), any());

        ocl.getDeployment().setDeployer(invalidDeployer);

        Assertions.assertThrows(TerraformBootRequestFailedException.class,
                () -> this.terraformBootDeployment.deploy(deployTask));
    }

    @Test
    void testDestroy_ThrowsRestClientException() {
        Mockito.doThrow(new TerraformBootRequestFailedException("IO error")).when(terraformApi)
                .asyncDestroyWithScripts(any(), any());

        Assertions.assertThrows(TerraformBootRequestFailedException.class,
                () -> this.terraformBootDeployment.destroy(deployTask, ""));

        Assertions.assertThrows(TerraformBootRequestFailedException.class,
                () -> this.terraformBootDeployment.destroy(deployTask, "error_tdState"));
    }

    @Test
    void testGetDeployerKind() {
        DeployerKind deployerKind = terraformBootDeployment.getDeployerKind();
        Assertions.assertEquals(DeployerKind.TERRAFORM, deployerKind);
    }

    @Test
    void testGetDeployPlanAsJson() {
        TerraformPlan terraformPlan = new TerraformPlan();
        terraformPlan.setPlan("plan");
        when(terraformApi.planWithScripts(any(), any())).thenReturn(terraformPlan);
        String deployPlanJson = terraformBootDeployment.getDeployPlanAsJson(deployTask);
        Assertions.assertNotNull(deployPlanJson);

    }

    @Test
    void testGetDeployPlanAsJson_ThrowsException() {

        when(terraformApi.planWithScripts(any(), any())).thenThrow(
                new TerraformBootRequestFailedException("IO error"));

        Assertions.assertThrows(TerraformBootRequestFailedException.class,
                () -> this.terraformBootDeployment.getDeployPlanAsJson(deployTask));

    }

    @Test
    void testValidate() {
        DeployValidationResult expectedResult = new DeployValidationResult();
        expectedResult.setValid(true);

        TerraformValidationResult validate = new TerraformValidationResult();
        validate.setValid(true);

        when(terraformApi.validateWithScripts(any())).thenReturn(validate);

        // Run the test
        final DeployValidationResult result = terraformBootDeployment.validate(ocl);

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

        TerraformValidationResult validate = new TerraformValidationResult();
        validate.setValid(false);
        TerraformValidateDiagnostics terraformValidateDiagnostics =
                new TerraformValidateDiagnostics();

        terraformValidateDiagnostics.setDetail(
                "A managed resource \"random_id_2\" \"new\" has not been declared in the root module.");
        validate.setDiagnostics(List.of(terraformValidateDiagnostics));

        when(terraformApi.validateWithScripts(any())).thenReturn(validate);

        // Run the test
        final DeployValidationResult result = terraformBootDeployment.validate(ocl);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testValidate_ThrowsTerraformExecutorException() {
        ocl.getDeployment().setDeployer(errorDeployer);
        when(terraformApi.validateWithScripts(any())).thenThrow(
                new TerraformBootRequestFailedException("IO error"));
        Assertions.assertThrows(TerraformBootRequestFailedException.class,
                () -> this.terraformBootDeployment.validate(ocl));
    }

}
