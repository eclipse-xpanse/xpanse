/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofulocal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformlocal.TerraformLocalDeployment.STATE_FILE_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.deployment.DeployService;
import org.eclipse.xpanse.modules.deployment.DeployServiceEntityHandler;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.callbacks.OpenTofuDeploymentResultCallbackManager;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.exceptions.OpenTofuExecutorException;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofulocal.config.OpenTofuLocalConfig;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.utils.TfResourceTransUtils;
import org.eclipse.xpanse.modules.deployment.utils.DeployEnvironments;
import org.eclipse.xpanse.modules.deployment.utils.DeployResultFileUtils;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.enums.DeployerTaskStatus;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

/**
 * Test for OpenTofuDeployment.
 */
@Slf4j
@ExtendWith({MockitoExtension.class})
class OpenTofuLocalDeploymentTest {

    private final String errorDeployer = "error_deployer";
    private final String invalidDeployer = """
            resource "random_id" "new" {
              byte_length = 4
            }
                                    
            output "random_id" {
              value = resource.random_id_2.new.id
            }
            """;
    @InjectMocks
    OpenTofuLocalDeployment openTofuLocalDeployment;
    @Mock
    OpenTofuInstaller openTofuInstaller;
    @Mock
    DeployResultFileUtils deployResultFileUtils;
    @Mock
    DeployEnvironments deployEnvironments;
    @Mock
    OpenTofuLocalConfig openTofuLocalConfig;
    @Mock
    PluginManager pluginManager;
    @Mock
    DeployService deployService;
    @Mock
    Executor taskExecutor;
    @Mock
    OpenTofuDeploymentResultCallbackManager openTofuDeploymentResultCallbackManager;
    @Mock
    DeployServiceEntityHandler deployServiceEntityHandler;
    private Ocl ocl;
    private Ocl oclWithGitScripts;

    @BeforeEach
    void setUp() throws Exception {
        OclLoader oclLoader = new OclLoader();
        ocl = oclLoader.getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.getDeployment().getDeployerTool().setKind(DeployerKind.OPEN_TOFU);

        oclWithGitScripts = oclLoader.getOcl(
                URI.create("file:src/test/resources/ocl_terraform_from_git_test.yml").toURL());
        oclWithGitScripts.getDeployment().getDeployerTool().setKind(DeployerKind.OPEN_TOFU);
    }

    DeployTask getDeployTask(Ocl ocl, ServiceOrderType serviceOrderType) {
        DeployTask deployTask = new DeployTask();
        deployTask.setOrderId(UUID.randomUUID());
        deployTask.setServiceId(UUID.randomUUID());
        deployTask.setUserId("userId");
        deployTask.setTaskType(serviceOrderType);
        deployTask.setOcl(ocl);
        deployTask.setDeployRequest(getDeployRequest(ocl));
        return deployTask;
    }

    DeployRequest getDeployRequest(Ocl ocl) {
        DeployRequest deployRequest = new DeployRequest();
        deployRequest.setServiceName(ocl.getName());
        deployRequest.setVersion(ocl.getServiceVersion());
        deployRequest.setFlavor(ocl.getFlavors().getServiceFlavors().getFirst().getName());
        Region region = ocl.getCloudServiceProvider().getRegions().getFirst();
        deployRequest.setRegion(region);
        deployRequest.setCsp(ocl.getCloudServiceProvider().getName());
        deployRequest.setCategory(ocl.getCategory());
        deployRequest.setCustomerServiceName("test_deploy");
        deployRequest.setServiceHostingType(ocl.getServiceHostingType());
        Map<String, Object> serviceRequestProperties = new HashMap<>();
        ocl.getDeployment().getVariables().forEach(
                variable -> serviceRequestProperties.put(variable.getName(),
                        variable.getExample()));
        serviceRequestProperties.put("admin_passwd", "111111111@Qq");
        serviceRequestProperties.putAll(
                ocl.getFlavors().getServiceFlavors().getFirst().getProperties());
        serviceRequestProperties.put("region", region.getName());
        deployRequest.setServiceRequestProperties(serviceRequestProperties);

        Map<String, String> availabilityZones = new HashMap<>();
        ocl.getDeployment().getServiceAvailabilityConfig().forEach(
                availabilityZoneConfig -> availabilityZones.put(availabilityZoneConfig.getVarName(),
                        availabilityZoneConfig.getDisplayName()));
        deployRequest.setAvailabilityZones(availabilityZones);
        return deployRequest;
    }

    String getFileContent() {
        String content = "";
        try {
            ClassPathResource classPathResource = new ClassPathResource(STATE_FILE_NAME);
            InputStream inputStream = classPathResource.getInputStream();
            content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to read file: {}", STATE_FILE_NAME, e);
        }
        return content;
    }

    @Test
    void testDeploy() {
        when(openTofuInstaller.getExecutorPathThatMatchesRequiredVersion(any())).thenReturn("tofu");
        DeployTask deployTask = getDeployTask(ocl, ServiceOrderType.DEPLOY);
        deployTask.setDeploymentScenario(DeploymentScenario.DEPLOY);
        DeployResult deployResult = openTofuLocalDeployment.deploy(deployTask);
        String tfState = deployResult.getPrivateProperties().get(STATE_FILE_NAME);
        Assertions.assertNotNull(deployResult);
        Assertions.assertNotNull(deployResult.getPrivateProperties());
        Assertions.assertNull(tfState);
        Assertions.assertNull(deployResult.getState());

        try {
            DeployTask deployTask1 = getDeployTask(oclWithGitScripts, ServiceOrderType.DEPLOY);
            DeployResult deployResult1 = openTofuLocalDeployment.deploy(deployTask1);
            Assertions.assertNotNull(deployResult1);
            Assertions.assertNotNull(deployResult1.getPrivateProperties());
            String tfState1 = deployResult1.getPrivateProperties().get(STATE_FILE_NAME);
            Assertions.assertNull(tfState1);
            Assertions.assertNull(deployResult1.getState());
        } catch (Exception e) {
            log.error("testDeploy throw unexpected exception.", e);
        }

    }

    @Test
    void testModify() {
        when(openTofuInstaller.getExecutorPathThatMatchesRequiredVersion(any())).thenReturn("tofu");
        DeployTask deployTask = getDeployTask(ocl, ServiceOrderType.MODIFY);
        String tfState = getFileContent();
        DeployServiceEntity deployServiceEntity = new DeployServiceEntity();
        deployServiceEntity.setPrivateProperties(Map.of(STATE_FILE_NAME, tfState));
        when(deployServiceEntityHandler.getDeployServiceEntity(any())).thenReturn(
                deployServiceEntity);

        deployTask.setDeploymentScenario(DeploymentScenario.MODIFY);

        DeployResult deployResult = openTofuLocalDeployment.modify(deployTask);
        Assertions.assertNotNull(deployResult);
        Assertions.assertNotNull(deployResult.getPrivateProperties());
        Assertions.assertNull(deployResult.getState());

        try {
            DeployTask deployTask1 = getDeployTask(oclWithGitScripts, ServiceOrderType.MODIFY);
            DeployResult deployResult1 =
                    openTofuLocalDeployment.modify(deployTask1);
            Assertions.assertNotNull(deployResult1);
            Assertions.assertNotNull(deployResult1.getPrivateProperties());
            String tfState1 = deployResult1.getPrivateProperties().get(STATE_FILE_NAME);
            Assertions.assertNull(tfState1);
            Assertions.assertNull(deployResult1.getState());
        } catch (Exception e) {
            log.error("testDeploy throw unexpected exception.", e);
        }

    }

    @Test
    void testDestroy() {
        when(openTofuInstaller.getExecutorPathThatMatchesRequiredVersion(any())).thenReturn("tofu");
        String tfState = getFileContent();
        DeployServiceEntity deployServiceEntity = new DeployServiceEntity();
        deployServiceEntity.setPrivateProperties(Map.of(STATE_FILE_NAME, tfState));
        when(deployServiceEntityHandler.getDeployServiceEntity(any())).thenReturn(
                deployServiceEntity);


        DeployTask deployTask = getDeployTask(ocl, ServiceOrderType.DESTROY);
        deployTask.setDeploymentScenario(DeploymentScenario.DESTROY);
        DeployResult destroyResult = openTofuLocalDeployment.destroy(deployTask);
        Assertions.assertNotNull(destroyResult);
        Assertions.assertNotNull(destroyResult.getPrivateProperties());

        try {
            DeployTask deployTask1 = getDeployTask(oclWithGitScripts, ServiceOrderType.DESTROY);
            deployTask1.setDeploymentScenario(DeploymentScenario.DESTROY);
            DeployResult destroyResult1 = openTofuLocalDeployment.destroy(deployTask1);
            Assertions.assertNotNull(destroyResult1);
            Assertions.assertNotNull(destroyResult1.getPrivateProperties());
        } catch (Exception e) {
            log.error("testDestroy throw unexpected exception.", e);
        }
    }

    @Test
    void testDeleteTaskWorkspace() {
        String workspacePath = System.getProperty("java.io.tmpdir") + File.separator
                + openTofuLocalConfig.getWorkspaceDirectory() + File.separator + UUID.randomUUID();
        this.openTofuLocalDeployment.deleteTaskWorkspace(UUID.randomUUID());
        Assertions.assertFalse(new File(workspacePath).exists());
    }

    @Test
    void testDeploy_FailedCausedByOpenTofuExecutorException() {
        when(openTofuInstaller.getExecutorPathThatMatchesRequiredVersion(any())).thenReturn("tofu");
        ocl.getDeployment().setDeployer(invalidDeployer);
        DeployTask deployTask = getDeployTask(ocl, ServiceOrderType.DEPLOY);
        DeployResult deployResult = this.openTofuLocalDeployment.deploy(deployTask);
        Assertions.assertNull(deployResult.getState());
        Assertions.assertNotEquals(DeployerTaskStatus.DEPLOY_SUCCESS.toValue(),
                deployResult.getMessage());

    }

    @Test
    void testModify_FailedCausedByOpenTofuExecutorException() {
        when(openTofuInstaller.getExecutorPathThatMatchesRequiredVersion(any())).thenReturn("tofu");
        try (MockedStatic<TfResourceTransUtils> tfResourceTransUtils = Mockito.mockStatic(
                TfResourceTransUtils.class)) {
            tfResourceTransUtils.when(() -> TfResourceTransUtils.getStoredStateContent(any()))
                    .thenReturn("Test");
            String tfState = getFileContent();
            DeployServiceEntity deployServiceEntity = new DeployServiceEntity();
            deployServiceEntity.setPrivateProperties(Map.of(STATE_FILE_NAME, tfState));
            when(deployServiceEntityHandler.getDeployServiceEntity(any())).thenReturn(
                    deployServiceEntity);
            ocl.getDeployment().setDeployer(errorDeployer);
            DeployTask deployTask = getDeployTask(ocl, ServiceOrderType.MODIFY);
            DeployResult deployResult = this.openTofuLocalDeployment.modify(deployTask);
            Assertions.assertNull(deployResult.getState());
            Assertions.assertNotEquals(DeployerTaskStatus.MODIFICATION_SUCCESSFUL.toValue(),
                    deployResult.getMessage());
        }
    }

    @Test
    void testDestroy_FailedCausedByOpenTofuExecutorException() {
        when(openTofuInstaller.getExecutorPathThatMatchesRequiredVersion(any())).thenReturn("tofu");
        try (MockedStatic<TfResourceTransUtils> tfResourceTransUtils = Mockito.mockStatic(
                TfResourceTransUtils.class)) {
            tfResourceTransUtils.when(() -> TfResourceTransUtils.getStoredStateContent(any()))
                    .thenReturn("Test");
            ocl.getDeployment().setDeployer(errorDeployer);
            DeployTask deployTask = getDeployTask(ocl, ServiceOrderType.DESTROY);
            DeployResult deployResult = this.openTofuLocalDeployment.destroy(deployTask);
            Assertions.assertTrue(deployResult.getProperties().isEmpty());
            Assertions.assertNull(deployResult.getState());
            Assertions.assertNotEquals(DeployerTaskStatus.DESTROY_FAILED.toValue(),
                    deployResult.getMessage());
        }
    }

    @Test
    void testGetDeployerKind() {
        DeployerKind deployerKind = openTofuLocalDeployment.getDeployerKind();
        Assertions.assertEquals(DeployerKind.OPEN_TOFU, deployerKind);
    }

    @Test
    void testGetDeployPlanAsJson() {
        when(openTofuInstaller.getExecutorPathThatMatchesRequiredVersion(any())).thenReturn("tofu");
        DeployTask deployTask = getDeployTask(ocl, ServiceOrderType.DEPLOY);
        String deployPlanJson = openTofuLocalDeployment.getDeploymentPlanAsJson(deployTask);
        Assertions.assertNotNull(deployPlanJson);

        try {
            DeployTask deployTask1 = getDeployTask(oclWithGitScripts, ServiceOrderType.DEPLOY);
            String deployPlanJson1 = openTofuLocalDeployment.getDeploymentPlanAsJson(deployTask1);
            Assertions.assertNotNull(deployPlanJson1);
        } catch (Exception e) {
            log.error("testGetDeployPlanAsJson throw unexpected exception.", e);
        }

    }

    @Test
    void testGetDeployPlanAsJson_ThrowsException() {
        when(openTofuInstaller.getExecutorPathThatMatchesRequiredVersion(any())).thenReturn("tofu");
        ocl.getDeployment().setDeployer(errorDeployer);
        DeployTask deployTask = getDeployTask(ocl, ServiceOrderType.DEPLOY);
        Assertions.assertThrows(OpenTofuExecutorException.class,
                () -> this.openTofuLocalDeployment.getDeploymentPlanAsJson(deployTask));

    }

    @Test
    void testValidate() {
        when(openTofuInstaller.getExecutorPathThatMatchesRequiredVersion(any())).thenReturn("tofu");
        DeploymentScriptValidationResult expectedResult = new DeploymentScriptValidationResult();
        expectedResult.setValid(true);
        expectedResult.setDiagnostics(new ArrayList<>());

        // Run the test
        final DeploymentScriptValidationResult result =
                openTofuLocalDeployment.validate(ocl.getDeployment());
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);

        try {
            // Run the test
            final DeploymentScriptValidationResult result1 =
                    openTofuLocalDeployment.validate(oclWithGitScripts.getDeployment());
            // Verify the results
            assertThat(result1).isEqualTo(expectedResult);
        } catch (Exception e) {
            log.error("testValidate throw unexpected exception.", e);
        }
    }

    @Test
    void testValidateFailed() {
        when(openTofuInstaller.getExecutorPathThatMatchesRequiredVersion(any())).thenReturn("tofu");
        ocl.getDeployment().setDeployer(invalidDeployer);

        DeploymentScriptValidationResult expectedResult = new DeploymentScriptValidationResult();
        expectedResult.setValid(false);
        DeployValidateDiagnostics diagnostics = new DeployValidateDiagnostics();
        diagnostics.setDetail(
                "A managed resource \"random_id_2\" \"new\" has not been declared in the root " +
                        "module.");
        expectedResult.setDiagnostics(List.of(diagnostics));

        // Run the test
        Assertions.assertThrows(OpenTofuExecutorException.class,
                () -> openTofuLocalDeployment.validate(ocl.getDeployment()));
    }

    @Test
    void testValidate_ThrowsTerraformExecutorException() {
        when(openTofuInstaller.getExecutorPathThatMatchesRequiredVersion(any())).thenReturn("tofu");
        ocl.getDeployment().setDeployer(errorDeployer);
        Assertions.assertThrows(OpenTofuExecutorException.class,
                () -> openTofuLocalDeployment.validate(ocl.getDeployment()));
    }

}
