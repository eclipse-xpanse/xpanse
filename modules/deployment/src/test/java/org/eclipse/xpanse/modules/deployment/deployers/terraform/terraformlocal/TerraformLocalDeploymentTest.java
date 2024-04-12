/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformlocal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformlocal.TerraformLocalDeployment.STATE_FILE_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
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
import org.eclipse.xpanse.modules.async.TaskConfiguration;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.deployment.DeployService;
import org.eclipse.xpanse.modules.deployment.DeployServiceEntityHandler;
import org.eclipse.xpanse.modules.deployment.ResourceHandlerManager;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.callbacks.TerraformDeploymentResultCallbackManager;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.exceptions.TerraformExecutorException;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformlocal.config.TerraformLocalConfig;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.utils.TfResourceTransUtils;
import org.eclipse.xpanse.modules.deployment.utils.DeployEnvironments;
import org.eclipse.xpanse.modules.deployment.utils.ScriptsGitRepoManage;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployerTaskStatus;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployValidateDiagnostics;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeploymentScriptValidationResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeploymentScenario;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Test for TerraformDeployment.
 */
@Slf4j
@ExtendWith({SpringExtension.class})
@ContextConfiguration(classes = {TerraformLocalDeployment.class, DeployEnvironments.class,
        PluginManager.class, TerraformLocalConfig.class, DeployService.class,
        TaskConfiguration.class, ResourceHandlerManager.class, ScriptsGitRepoManage.class})
class TerraformLocalDeploymentTest {

    private final String errorDeployer = "error_deployer";
    private final String invalidDeployer = """
            resource "random_id" "new" {
              byte_length = 4
            }
                                    
            output "random_id" {
              value = resource.random_id_2.new.id
            }
            """;
    @Autowired
    TerraformLocalDeployment terraformLocalDeployment;
    @MockBean
    DeployEnvironments deployEnvironments;
    @MockBean
    TerraformLocalConfig terraformLocalConfig;
    @MockBean
    PluginManager pluginManager;
    @MockBean
    DeployService deployService;
    @MockBean
    Executor taskExecutor;
    @MockBean
    TerraformDeploymentResultCallbackManager terraformDeploymentResultCallbackManager;
    @MockBean
    DeployServiceEntityHandler deployServiceEntityHandler;

    private Ocl ocl;
    private Ocl oclWithGitScripts;

    @BeforeEach
    void setUp() throws Exception {
        when(terraformLocalConfig.getWorkspaceDirectory()).thenReturn("tf-ws-test");
        OclLoader oclLoader = new OclLoader();
        ocl = oclLoader.getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());

        oclWithGitScripts = oclLoader.getOcl(
                URI.create("file:src/test/resources/ocl_terraform_from_git_test.yml").toURL());
        doReturn(new HashMap<>()).when(this.deployEnvironments)
                .getCredentialVariablesByHostingType(any(), any(), any(), any());
    }

    DeployTask getDeployTask(Ocl ocl) {
        DeployTask deployTask = new DeployTask();
        deployTask.setId(UUID.randomUUID());
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
        ocl.getDeployment().getServiceAvailability().forEach(
                availabilityZoneConfig -> availabilityZones.put(availabilityZoneConfig.getVarName(),
                        availabilityZoneConfig.getDisplayName()));
        deployRequest.setAvailabilityZones(availabilityZones);
        return deployRequest;
    }

    String getFileContent(String fileName) {
        String content = "";
        try {
            ClassPathResource classPathResource = new ClassPathResource(fileName);
            InputStream inputStream = classPathResource.getInputStream();
            content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to read file: {}", fileName, e);
        }
        return content;
    }

    @Test
    void testDeploy() {

        DeployTask deployTask = getDeployTask(ocl);
        deployTask.setDeploymentScenario(DeploymentScenario.DEPLOY);
        DeployResult deployResult = terraformLocalDeployment.deploy(deployTask);
        String tfState = deployResult.getPrivateProperties().get(STATE_FILE_NAME);
        Assertions.assertNotNull(deployResult);
        Assertions.assertNotNull(deployResult.getPrivateProperties());
        Assertions.assertNull(tfState);
        Assertions.assertNull(deployResult.getState());

        try {
            DeployTask deployTask1 = getDeployTask(oclWithGitScripts);
            DeployResult deployResult1 = terraformLocalDeployment.deploy(deployTask1);
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
        String tfState = getFileContent(STATE_FILE_NAME);
        DeployServiceEntity deployServiceEntity = new DeployServiceEntity();
        deployServiceEntity.setPrivateProperties(Map.of(STATE_FILE_NAME, tfState));
        when(deployServiceEntityHandler.getDeployServiceEntity(any())).thenReturn(
                deployServiceEntity);

        DeployTask deployTask = getDeployTask(ocl);
        deployTask.setDeploymentScenario(DeploymentScenario.MODIFY);
        DeployResult deployResult = terraformLocalDeployment.modify(deployTask);
        Assertions.assertNotNull(deployResult);
        Assertions.assertNotNull(deployResult.getPrivateProperties());
        Assertions.assertNull(deployResult.getState());

        try {
            DeployTask deployTask1 = getDeployTask(oclWithGitScripts);
            DeployResult deployResult1 = terraformLocalDeployment.deploy(deployTask1);
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
        String tfState = getFileContent(STATE_FILE_NAME);
        DeployServiceEntity deployServiceEntity = new DeployServiceEntity();
        deployServiceEntity.setPrivateProperties(Map.of(STATE_FILE_NAME, tfState));
        when(deployServiceEntityHandler.getDeployServiceEntity(any())).thenReturn(
                deployServiceEntity);

        DeployTask deployTask = getDeployTask(ocl);
        deployTask.setDeploymentScenario(DeploymentScenario.DESTROY);
        DeployResult destroyResult = terraformLocalDeployment.destroy(deployTask);
        Assertions.assertNotNull(destroyResult);
        Assertions.assertNotNull(destroyResult.getPrivateProperties());

        try {
            DeployTask deployTask1 = getDeployTask(oclWithGitScripts);
            deployTask1.setDeploymentScenario(DeploymentScenario.DESTROY);
            DeployResult destroyResult1 = terraformLocalDeployment.destroy(deployTask1);
            Assertions.assertNotNull(destroyResult1);
            Assertions.assertNotNull(destroyResult1.getPrivateProperties());
        } catch (Exception e) {
            log.error("testDestroy throw unexpected exception.", e);
        }

    }

    @Test
    void testDeleteTaskWorkspace() {
        String workspacePath = System.getProperty("java.io.tmpdir") + File.separator
                + terraformLocalConfig.getWorkspaceDirectory() + File.separator + UUID.randomUUID();
        this.terraformLocalDeployment.deleteTaskWorkspace(UUID.randomUUID());
        Assertions.assertFalse(new File(workspacePath).exists());
    }

    @Test
    void testDeploy_FailedCausedByTerraformExecutorException() {
        ocl.getDeployment().setDeployer(invalidDeployer);
        DeployTask deployTask = getDeployTask(ocl);
        deployTask.setDeploymentScenario(DeploymentScenario.DEPLOY);
        DeployResult deployResult = this.terraformLocalDeployment.deploy(deployTask);
        Assertions.assertNull(deployResult.getState());
        Assertions.assertNotEquals(DeployerTaskStatus.DEPLOY_SUCCESS.toValue(),
                deployResult.getMessage());

    }

    @Test
    void testDestroy_FailedCausedByTerraformExecutorException() {
        try (MockedStatic<TfResourceTransUtils> tfResourceTransUtils = Mockito.mockStatic(
                TfResourceTransUtils.class)) {
            tfResourceTransUtils.when(() -> TfResourceTransUtils.getStoredStateContent(any()))
                    .thenReturn("Test");
            ocl.getDeployment().setDeployer(errorDeployer);
            DeployTask deployTask = getDeployTask(ocl);
            deployTask.setDeploymentScenario(DeploymentScenario.DESTROY);
            DeployResult deployResult = this.terraformLocalDeployment.destroy(deployTask);
            Assertions.assertTrue(deployResult.getProperties().isEmpty());
            Assertions.assertNull(deployResult.getState());
            Assertions.assertNotEquals(DeployerTaskStatus.DESTROY_FAILED.toValue(),
                    deployResult.getMessage());
        }
    }

    @Test
    void testGetDeployerKind() {
        DeployerKind deployerKind = terraformLocalDeployment.getDeployerKind();
        Assertions.assertEquals(DeployerKind.TERRAFORM, deployerKind);
    }

    @Test
    void testGetDeployPlanAsJson() {
        DeployTask deployTask = getDeployTask(ocl);
        deployTask.setDeploymentScenario(DeploymentScenario.DEPLOY);
        String deployPlanJson = terraformLocalDeployment.getDeploymentPlanAsJson(deployTask);
        Assertions.assertNotNull(deployPlanJson);

        try {
            DeployTask deployTask1 = getDeployTask(oclWithGitScripts);
            String deployPlanJson1 = terraformLocalDeployment.getDeploymentPlanAsJson(deployTask1);
            Assertions.assertNotNull(deployPlanJson1);
        } catch (Exception e) {
            log.error("testGetDeployPlanAsJson throw unexpected exception.", e);
        }
    }

    @Test
    void testGetDeployPlanAsJson_ThrowsException() {
        ocl.getDeployment().setDeployer(errorDeployer);
        DeployTask deployTask = getDeployTask(ocl);
        Assertions.assertThrows(TerraformExecutorException.class,
                () -> this.terraformLocalDeployment.getDeploymentPlanAsJson(deployTask));
    }

    @Test
    void testValidate() {
        DeploymentScriptValidationResult expectedResult = new DeploymentScriptValidationResult();
        expectedResult.setValid(true);
        expectedResult.setDiagnostics(new ArrayList<>());

        // Run the test
        final DeploymentScriptValidationResult result =
                terraformLocalDeployment.validate(ocl.getDeployment());
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);

        try {
            // Run the test
            final DeploymentScriptValidationResult result1 =
                    terraformLocalDeployment.validate(oclWithGitScripts.getDeployment());
            // Verify the results
            assertThat(result1).isEqualTo(expectedResult);
        } catch (Exception e) {
            log.error("testValidate throw unexpected exception.", e);
        }
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

        // Run the test
        final DeploymentScriptValidationResult result =
                terraformLocalDeployment.validate(ocl.getDeployment());

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testValidate_ThrowsTerraformExecutorException() {
        ocl.getDeployment().setDeployer(errorDeployer);
        Assertions.assertThrows(TerraformExecutorException.class,
                () -> this.terraformLocalDeployment.validate(ocl.getDeployment()));
    }

}
