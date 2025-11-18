/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker;

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
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.exceptions.OpenTofuMakerRequestFailedException;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.api.AdminApi;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.api.OpenTofuFromGitRepoApi;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.api.OpenTofuFromScriptsApi;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.api.RetrieveOpenTofuResultApi;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuPlan;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.utils.TfResourceTransUtils;
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
import org.eclipse.xpanse.modules.orchestrator.deployment.DeploymentScriptValidationResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.InputValidateDiagnostics;
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

/** Test for OpenTofuMakerDeployment. */
@ContextConfiguration(
        classes = {
            TofuMakerServiceDeployer.class,
            TofuMakerServiceModifier.class,
            DeployEnvironments.class,
            OpenTofuFromScriptsApi.class,
            ServiceDeploymentEntityHandler.class,
            TofuMakerScriptValidator.class,
            TofuMakerServiceDeployer.class,
            TofuMakerDeployment.class,
            AdminApi.class,
            TofuMakerDeploymentPlanManage.class,
            TofuMakerServiceDestroyer.class
        })
@ExtendWith(SpringExtension.class)
@ActiveProfiles("tofu-maker")
class TofuMakerDeploymentTest {

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
    @MockitoBean OpenTofuFromScriptsApi terraformApi;
    @MockitoBean ServiceDeploymentEntityHandler serviceDeploymentEntityHandler;
    @MockitoBean TofuMakerScriptValidator tofuMakerScriptValidator;
    @MockitoBean OpenTofuFromGitRepoApi openTofuFromGitRepoApi;
    @MockitoBean RetrieveOpenTofuResultApi retrieveOpenTofuResultApi;
    @MockitoBean AdminApi adminApi;
    @MockitoBean TofuMakerHelper tofuMakerHelper;

    @Autowired private TofuMakerDeployment openTofuMakerDeployment;

    private DeployTask deployTask;
    private Ocl ocl;

    @BeforeEach
    void setUp() throws Exception {

        OclLoader oclLoader = new OclLoader();
        ocl =
                oclLoader.getOcl(
                        URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.getDeployment().getDeployerTool().setKind(DeployerKind.OPEN_TOFU);

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
        DeployResult deployResult = openTofuMakerDeployment.deploy(deployTask);
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
            DeployResult deployResult = openTofuMakerDeployment.modify(deployTask);
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
            DeployResult destroyResult = this.openTofuMakerDeployment.destroy(deployTask);

            Assertions.assertNotNull(destroyResult);
        }
    }

    @Test
    void testDeploy_ThrowsRestClientException() {
        ocl.getDeployment()
                .getTerraformDeployment()
                .setScriptFiles(Map.of("error_test.tf", errorScript));

        Mockito.doThrow(new OpenTofuMakerRequestFailedException("IO error"))
                .when(terraformApi)
                .asyncDeployWithScripts(any());

        ocl.getDeployment()
                .getTerraformDeployment()
                .setScriptFiles(Map.of("invalid_test.tf", invalidScript));
        Assertions.assertThrows(
                OpenTofuMakerRequestFailedException.class,
                () -> this.openTofuMakerDeployment.deploy(deployTask));
    }

    @Test
    void testDestroy_ThrowsRestClientException() {
        Mockito.doThrow(new OpenTofuMakerRequestFailedException("IO error"))
                .when(terraformApi)
                .asyncDestroyWithScripts(any());

        try (MockedStatic<TfResourceTransUtils> tfResourceTransUtils =
                Mockito.mockStatic(TfResourceTransUtils.class)) {
            tfResourceTransUtils
                    .when(() -> TfResourceTransUtils.getStoredStateContent(any()))
                    .thenReturn("Test");
            Assertions.assertThrows(
                    OpenTofuMakerRequestFailedException.class,
                    () -> this.openTofuMakerDeployment.destroy(deployTask));

            Assertions.assertThrows(
                    OpenTofuMakerRequestFailedException.class,
                    () -> this.openTofuMakerDeployment.destroy(deployTask));
        }
    }

    @Test
    void testGetDeployerKind() {
        DeployerKind deployerKind = openTofuMakerDeployment.getDeployerKind();
        Assertions.assertEquals(DeployerKind.OPEN_TOFU, deployerKind);
    }

    @Test
    void testGetDeployPlanAsJson() {
        OpenTofuPlan terraformPlan = new OpenTofuPlan();
        terraformPlan.setPlan("plan");
        when(terraformApi.planWithScripts(any())).thenReturn(terraformPlan);

        String deployPlanJson = openTofuMakerDeployment.getDeploymentPlanAsJson(deployTask);
        Assertions.assertNotNull(deployPlanJson);
    }

    @Test
    void testGetDeployPlanAsJson_ThrowsException() {

        when(terraformApi.planWithScripts(any()))
                .thenThrow(new OpenTofuMakerRequestFailedException("IO error"));

        Assertions.assertThrows(
                OpenTofuMakerRequestFailedException.class,
                () -> this.openTofuMakerDeployment.getDeploymentPlanAsJson(deployTask));
    }

    @Test
    void testValidate() {

        DeploymentScriptValidationResult expectedResult = new DeploymentScriptValidationResult();
        expectedResult.setValid(true);
        expectedResult.setDiagnostics(Collections.emptyList());

        when(tofuMakerScriptValidator.validateOpenTofuScripts(any())).thenReturn(expectedResult);

        // Run the test
        final DeploymentScriptValidationResult result =
                openTofuMakerDeployment.validate(ocl.getDeployment());

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testValidateFailed() {
        ocl.getDeployment()
                .getTerraformDeployment()
                .setScriptFiles(Map.of("invalid_test.tf", invalidScript));

        DeploymentScriptValidationResult expectedResult = new DeploymentScriptValidationResult();
        expectedResult.setValid(false);
        InputValidateDiagnostics diagnostics = new InputValidateDiagnostics();
        diagnostics.setDetail(
                "A managed resource \"random_id_2\" \"new\" has not been declared in the root "
                        + "module.");
        expectedResult.setDiagnostics(List.of(diagnostics));

        when(tofuMakerScriptValidator.validateOpenTofuScripts(any())).thenReturn(expectedResult);

        // Run the test
        final DeploymentScriptValidationResult result =
                openTofuMakerDeployment.validate(ocl.getDeployment());

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testValidate_ThrowsOpenTofuExecutorException() {
        ocl.getDeployment()
                .getTerraformDeployment()
                .setScriptFiles(Map.of("error_test.tf", errorScript));
        when(tofuMakerScriptValidator.validateOpenTofuScripts(any()))
                .thenThrow(new OpenTofuMakerRequestFailedException("IO error"));

        Assertions.assertThrows(
                OpenTofuMakerRequestFailedException.class,
                () -> this.openTofuMakerDeployment.validate(ocl.getDeployment()));
    }
}
