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
import org.eclipse.xpanse.modules.deployment.DeployServiceEntityHandler;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.exceptions.OpenTofuMakerRequestFailedException;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.config.TofuMakerConfig;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.api.AdminApi;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.api.OpenTofuFromGitRepoApi;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.api.OpenTofuFromScriptsApi;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuPlan;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.utils.TfResourceTransUtils;
import org.eclipse.xpanse.modules.deployment.utils.DeployEnvironments;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployValidateDiagnostics;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeploymentScenario;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeploymentScriptValidationResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Test for OpenTofuMakerDeployment.
 */

@ContextConfiguration(classes = {TofuMakerServiceDeployer.class, TofuMakerServiceModifier.class,
        DeployEnvironments.class,
        PluginManager.class, OpenTofuFromScriptsApi.class, TofuMakerConfig.class,
        DeployServiceEntityHandler.class, TofuMakerScriptValidator.class,
        TofuMakerServiceDeployer.class, TofuMakerDeployment.class, TofuMakerHelper.class,
        AdminApi.class, TofuMakerDeploymentPlanManage.class, TofuMakerServiceDestroyer.class})
@ExtendWith(SpringExtension.class)
@ActiveProfiles("tofu-maker")
class TofuMakerDeploymentTest {

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
    @MockBean
    DeployEnvironments deployEnvironments;
    @MockBean
    PluginManager pluginManager;
    @MockBean
    OpenTofuFromScriptsApi terraformApi;
    @MockBean
    TofuMakerConfig tofuMakerConfig;
    @MockBean
    DeployServiceEntityHandler deployServiceEntityHandler;
    @MockBean
    TofuMakerScriptValidator tofuMakerScriptValidator;
    @MockBean
    OpenTofuFromGitRepoApi openTofuFromGitRepoApi;
    @Autowired
    TofuMakerServiceDeployer tofuMakerServiceDeployer;
    @Autowired
    TofuMakerServiceModifier tofuMakerServiceModifier;
    @MockBean
    AdminApi adminApi;

    @Autowired
    private TofuMakerDeployment openTofuMakerDeployment;

    private DeployTask deployTask;
    private Ocl ocl;

    @BeforeEach
    void setUp() throws Exception {

        OclLoader oclLoader = new OclLoader();
        ocl = oclLoader.getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.getDeployment().setKind(DeployerKind.OPEN_TOFU);

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
        deployTask.setId(id);
        deployTask.setOcl(ocl);
        deployTask.setDeployRequest(deployRequest);
    }


    @Test
    void testDeploy() {
        doReturn(new HashMap<>()).when(this.deployEnvironments)
                .getCredentialVariablesByHostingType(any(), any(), any(), any());
        deployTask.setDeploymentScenario(DeploymentScenario.DEPLOY);
        DeployResult deployResult = openTofuMakerDeployment.deploy(deployTask);

        Assertions.assertNotNull(deployResult);
        Assertions.assertEquals(id, deployResult.getId());
    }

    @Test
    void testModify() {
        try (MockedStatic<TfResourceTransUtils> tfResourceTransUtils = Mockito.mockStatic(
                TfResourceTransUtils.class)) {
            tfResourceTransUtils.when(() -> TfResourceTransUtils.getStoredStateContent(any()))
                    .thenReturn("Test");
            doReturn(new HashMap<>()).when(this.deployEnvironments)
                    .getCredentialVariablesByHostingType(any(), any(), any(), any());
            deployTask.setDeploymentScenario(DeploymentScenario.MODIFY);
            DeployResult deployResult = openTofuMakerDeployment.modify(deployTask);

            Assertions.assertNotNull(deployResult);
            Assertions.assertEquals(id, deployResult.getId());
        }
    }

    @Test
    void testDestroy() {
        try (MockedStatic<TfResourceTransUtils> tfResourceTransUtils = Mockito.mockStatic(
                TfResourceTransUtils.class)) {
            tfResourceTransUtils.when(() -> TfResourceTransUtils.getStoredStateContent(any()))
                    .thenReturn("Test");
            deployTask.setDeploymentScenario(DeploymentScenario.DESTROY);
            DeployResult destroyResult = this.openTofuMakerDeployment.destroy(deployTask);

            Assertions.assertNotNull(destroyResult);
            Assertions.assertEquals(id, destroyResult.getId());
        }
    }


    @Test
    void testDeploy_ThrowsRestClientException() {
        ocl.getDeployment().setDeployer(errorDeployer);

        Mockito.doThrow(new OpenTofuMakerRequestFailedException("IO error")).when(terraformApi)
                .asyncDeployWithScripts(any(), any());

        ocl.getDeployment().setDeployer(invalidDeployer);
        deployTask.setDeploymentScenario(DeploymentScenario.DEPLOY);

        Assertions.assertThrows(OpenTofuMakerRequestFailedException.class,
                () -> this.openTofuMakerDeployment.deploy(deployTask));
    }

    @Test
    void testDestroy_ThrowsRestClientException() {
        Mockito.doThrow(new OpenTofuMakerRequestFailedException("IO error")).when(terraformApi)
                .asyncDestroyWithScripts(any(), any());

        try (MockedStatic<TfResourceTransUtils> tfResourceTransUtils = Mockito.mockStatic(
                TfResourceTransUtils.class)) {
            tfResourceTransUtils.when(() -> TfResourceTransUtils.getStoredStateContent(any()))
                    .thenReturn("Test");
            deployTask.setDeploymentScenario(DeploymentScenario.DESTROY);

            Assertions.assertThrows(OpenTofuMakerRequestFailedException.class,
                    () -> this.openTofuMakerDeployment.destroy(deployTask));

            Assertions.assertThrows(OpenTofuMakerRequestFailedException.class,
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
        when(terraformApi.planWithScripts(any(), any())).thenReturn(terraformPlan);

        String deployPlanJson = openTofuMakerDeployment.getDeploymentPlanAsJson(deployTask);
        Assertions.assertNotNull(deployPlanJson);

    }

    @Test
    void testGetDeployPlanAsJson_ThrowsException() {

        when(terraformApi.planWithScripts(any(), any())).thenThrow(
                new OpenTofuMakerRequestFailedException("IO error"));

        Assertions.assertThrows(OpenTofuMakerRequestFailedException.class,
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
        ocl.getDeployment().setDeployer(invalidDeployer);

        DeploymentScriptValidationResult expectedResult = new DeploymentScriptValidationResult();
        expectedResult.setValid(false);
        DeployValidateDiagnostics diagnostics = new DeployValidateDiagnostics();
        diagnostics.setDetail(
                "A managed resource \"random_id_2\" \"new\" has not been declared in the root " +
                        "module.");
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
        ocl.getDeployment().setDeployer(errorDeployer);
        when(tofuMakerScriptValidator.validateOpenTofuScripts(any())).thenThrow(
                new OpenTofuMakerRequestFailedException("IO error"));

        Assertions.assertThrows(OpenTofuMakerRequestFailedException.class,
                () -> this.openTofuMakerDeployment.validate(ocl.getDeployment()));
    }

}
