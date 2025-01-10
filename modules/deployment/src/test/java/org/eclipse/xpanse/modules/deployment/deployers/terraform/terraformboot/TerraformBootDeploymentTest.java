/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.deployment.ServiceDeploymentEntityHandler;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.exceptions.TerraformBootRequestFailedException;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.config.TerraformBootConfig;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.api.AdminApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.api.RetrieveTerraformResultApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.api.TerraformFromGitRepoApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.api.TerraformFromScriptsApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformPlan;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.utils.TfResourceTransUtils;
import org.eclipse.xpanse.modules.deployment.utils.DeployEnvironments;
import org.eclipse.xpanse.modules.models.service.deployment.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deployment.DeployResult;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployValidateDiagnostics;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeploymentScriptValidationResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/** Test for TerraformDeploy. */
@ContextConfiguration(
        classes = {
            TerraformBootServiceDeployer.class,
            TerraformBootServiceModifier.class,
            DeployEnvironments.class,
            PluginManager.class,
            TerraformFromScriptsApi.class,
            TerraformBootConfig.class,
            ServiceDeploymentEntityHandler.class,
            TerraformBootScriptValidator.class,
            TerraformBootServiceDeployer.class,
            TerraformBootDeployment.class,
            TerraformFromGitRepoApi.class,
            TerraformBootHelper.class,
            AdminApi.class,
            TerraformBootDeploymentPlanManage.class,
            TerraformBootServiceDestroyer.class
        })
@ExtendWith(SpringExtension.class)
@ActiveProfiles("terraform-boot")
class TerraformBootDeploymentTest {

    private final UUID serviceId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();
    private final String errorScript = "error_script";
    private final String invalidScript =
            """
            resource "random_id" "new" {
              byte_length = 4
            }

            output "random_id" {
              value = resource.random_id_2.new.id
            }
            """;
    @MockitoBean DeployEnvironments deployEnvironments;
    @MockitoBean PluginManager pluginManager;
    @MockitoBean TerraformFromScriptsApi terraformApi;
    @MockitoBean TerraformFromGitRepoApi terraformFromGitRepoApi;
    @MockitoBean TerraformBootConfig terraformBootConfig;
    @MockitoBean ServiceDeploymentEntityHandler serviceDeploymentEntityHandler;
    @MockitoBean RetrieveTerraformResultApi retrieveTerraformResultApi;
    @MockitoBean TerraformBootScriptValidator terraformBootScriptValidator;
    @Autowired TerraformBootServiceDeployer terraformBootServiceDeployer;
    @Autowired TerraformBootServiceModifier terraformBootServiceModifier;
    @MockitoBean AdminApi adminApi;

    @Autowired private TerraformBootDeployment terraformBootDeployment;

    private DeployTask deployTask;
    private Ocl ocl;

    @BeforeEach
    void setUp() throws Exception {

        OclLoader oclLoader = new OclLoader();
        ocl =
                oclLoader.getOcl(
                        URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());

        DeployRequest deployRequest = new DeployRequest();
        deployRequest.setServiceName(ocl.getName());
        deployRequest.setVersion(ocl.getServiceVersion());
        deployRequest.setFlavor(ocl.getFlavors().getServiceFlavors().getFirst().getName());
        Region region = new Region();
        region.setName(ocl.getCloudServiceProvider().getRegions().getFirst().getName());
        region.setArea(ocl.getCloudServiceProvider().getRegions().getFirst().getArea());
        deployRequest.setRegion(region);
        deployRequest.setCsp(ocl.getCloudServiceProvider().getName());
        deployRequest.setCategory(ocl.getCategory());
        deployRequest.setCustomerServiceName("test_deploy");
        deployRequest.setServiceRequestProperties(Map.ofEntries(Map.entry("key", "value")));
        deployTask = new DeployTask();
        deployTask.setServiceId(serviceId);
        deployTask.setOrderId(orderId);
        deployTask.setOcl(ocl);
        deployTask.setUserId("userId");
        deployTask.setDeployRequest(deployRequest);
    }

    @Test
    void testDeploy() {
        doReturn(new HashMap<>()).when(this.deployEnvironments).getEnvironmentVariables(any());
        deployTask.setTaskType(ServiceOrderType.DEPLOY);
        DeployResult deployResult = terraformBootDeployment.deploy(deployTask);

        Assertions.assertNotNull(deployResult);
    }

    @Test
    void testModify() {
        try (MockedStatic<TfResourceTransUtils> tfResourceTransUtils =
                Mockito.mockStatic(TfResourceTransUtils.class)) {
            tfResourceTransUtils
                    .when(() -> TfResourceTransUtils.getStoredStateContent(any()))
                    .thenReturn("Test");

            doReturn(new HashMap<>()).when(this.deployEnvironments).getEnvironmentVariables(any());
            deployTask.setTaskType(ServiceOrderType.MODIFY);
            DeployResult deployResult = terraformBootDeployment.modify(deployTask);

            Assertions.assertNotNull(deployResult);
        }
    }

    @Test
    void testDestroy() {
        try (MockedStatic<TfResourceTransUtils> tfResourceTransUtils =
                Mockito.mockStatic(TfResourceTransUtils.class)) {
            tfResourceTransUtils
                    .when(() -> TfResourceTransUtils.getStoredStateContent(any()))
                    .thenReturn("Test");
            deployTask.setTaskType(ServiceOrderType.DESTROY);
            DeployResult destroyResult = this.terraformBootDeployment.destroy(deployTask);

            Assertions.assertNotNull(destroyResult);
        }
    }

    @Test
    void testDeploy_ThrowsRestClientException() {
        ocl.getDeployment().setScriptFiles(Map.of("error_test.tf", errorScript));
        deployTask.setTaskType(ServiceOrderType.DEPLOY);
        Mockito.doThrow(new TerraformBootRequestFailedException("IO error"))
                .when(terraformApi)
                .asyncDeployWithScripts(any());

        ocl.getDeployment().setScriptFiles(Map.of("invalid_test.tf", invalidScript));

        Assertions.assertThrows(
                TerraformBootRequestFailedException.class,
                () -> this.terraformBootDeployment.deploy(deployTask));
    }

    @Test
    void testDestroy_ThrowsRestClientException() {
        Mockito.doThrow(new TerraformBootRequestFailedException("IO error"))
                .when(terraformApi)
                .asyncDestroyWithScripts(any());

        try (MockedStatic<TfResourceTransUtils> tfResourceTransUtils =
                Mockito.mockStatic(TfResourceTransUtils.class)) {
            tfResourceTransUtils
                    .when(() -> TfResourceTransUtils.getStoredStateContent(any()))
                    .thenReturn("Test");
            deployTask.setTaskType(ServiceOrderType.MODIFY);
            Assertions.assertThrows(
                    TerraformBootRequestFailedException.class,
                    () -> this.terraformBootDeployment.destroy(deployTask));

            Assertions.assertThrows(
                    TerraformBootRequestFailedException.class,
                    () -> this.terraformBootDeployment.destroy(deployTask));
        }
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
        when(terraformApi.planWithScripts(any())).thenReturn(terraformPlan);

        String deployPlanJson = terraformBootDeployment.getDeploymentPlanAsJson(deployTask);
        Assertions.assertNotNull(deployPlanJson);
    }

    @Test
    void testGetDeployPlanAsJson_ThrowsException() {

        when(terraformApi.planWithScripts(any()))
                .thenThrow(new TerraformBootRequestFailedException("IO error"));

        Assertions.assertThrows(
                TerraformBootRequestFailedException.class,
                () -> this.terraformBootDeployment.getDeploymentPlanAsJson(deployTask));
    }

    @Test
    void testValidate() {

        DeploymentScriptValidationResult expectedResult = new DeploymentScriptValidationResult();
        expectedResult.setValid(true);
        expectedResult.setDiagnostics(Collections.emptyList());

        when(terraformBootScriptValidator.validateTerraformScripts(any()))
                .thenReturn(expectedResult);

        // Run the test
        final DeploymentScriptValidationResult result =
                terraformBootDeployment.validate(ocl.getDeployment());

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testValidateFailed() {
        ocl.getDeployment().setScriptFiles(Map.of("invalid_test.tf", invalidScript));

        DeploymentScriptValidationResult expectedResult = new DeploymentScriptValidationResult();
        expectedResult.setValid(false);
        DeployValidateDiagnostics diagnostics = new DeployValidateDiagnostics();
        diagnostics.setDetail(
                "A managed resource \"random_id_2\" \"new\" has not been declared in the root "
                        + "module.");
        expectedResult.setDiagnostics(List.of(diagnostics));

        when(terraformBootScriptValidator.validateTerraformScripts(any()))
                .thenReturn(expectedResult);

        // Run the test
        final DeploymentScriptValidationResult result =
                terraformBootDeployment.validate(ocl.getDeployment());

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testValidate_ThrowsTerraformExecutorException() {
        ocl.getDeployment().setScriptFiles(Map.of("error_test.tf", errorScript));
        when(terraformBootScriptValidator.validateTerraformScripts(any()))
                .thenThrow(new TerraformBootRequestFailedException("IO error"));

        Assertions.assertThrows(
                TerraformBootRequestFailedException.class,
                () -> this.terraformBootDeployment.validate(ocl.getDeployment()));
    }
}
