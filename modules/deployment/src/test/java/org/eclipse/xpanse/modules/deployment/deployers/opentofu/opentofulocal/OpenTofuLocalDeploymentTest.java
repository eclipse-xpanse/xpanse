/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofulocal;

import static org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofulocal.OpenTofuLocalDeployment.STATE_FILE_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.async.TaskConfiguration;
import org.eclipse.xpanse.modules.deployment.DeployService;
import org.eclipse.xpanse.modules.deployment.DeployServiceEntityHandler;
import org.eclipse.xpanse.modules.deployment.ResourceHandlerManager;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.callbacks.OpenTofuDeploymentResultCallbackManager;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofulocal.config.OpenTofuLocalConfig;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.exceptions.OpenTofuExecutorException;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.utils.OpenTofuProviderHelper;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.utils.TfResourceTransUtils;
import org.eclipse.xpanse.modules.deployment.utils.DeployEnvironments;
import org.eclipse.xpanse.modules.deployment.utils.ScriptsGitRepoManage;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployerTaskStatus;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Test for OpenTofuDeployment.
 */

@ExtendWith({SpringExtension.class})
@ContextConfiguration(classes = {OpenTofuLocalDeployment.class, DeployEnvironments.class,
        PluginManager.class, OpenTofuLocalConfig.class, DeployService.class,
        TaskConfiguration.class, ResourceHandlerManager.class, OpenTofuProviderHelper.class,
        ScriptsGitRepoManage.class})
class OpenTofuLocalDeploymentTest {

    private final UUID id = UUID.randomUUID();
    private final String errorDeployer = "error_deployer";
    @Autowired
    OpenTofuLocalDeployment openTofuLocalDeployment;
    @MockBean
    DeployEnvironments deployEnvironments;
    @MockBean
    OpenTofuLocalConfig openTofuLocalConfig;
    @MockBean
    PluginManager pluginManager;
    @MockBean
    DeployService deployService;
    @MockBean
    Executor taskExecutor;
    @MockBean
    OpenTofuDeploymentResultCallbackManager openTofuDeploymentResultCallbackManager;
    @MockBean
    DeployServiceEntityHandler deployServiceEntityHandler;
    private DeployRequest deployRequest;
    private DeployResult deployResult;
    private Ocl ocl;
    private String tfState;

    @BeforeEach
    void setUp() throws Exception {
        OclLoader oclLoader = new OclLoader();
        ocl = oclLoader.getOcl(URI.create("file:src/test/resources/opentofu_test.yaml").toURL());

        deployRequest = new DeployRequest();
        deployRequest.setServiceName(ocl.getName());
        deployRequest.setVersion(ocl.getServiceVersion());
        deployRequest.setFlavor(ocl.getFlavors().getFirst().getName());
        deployRequest.setRegion(ocl.getCloudServiceProvider().getRegions().getFirst().getName());
        deployRequest.setCsp(ocl.getCloudServiceProvider().getName());
        deployRequest.setCategory(ocl.getCategory());
        deployRequest.setCustomerServiceName("test_deploy");
        deployRequest.setServiceRequestProperties(Map.ofEntries(Map.entry("key", "value")));

        doReturn(new HashMap<>()).when(this.deployEnvironments)
                .getCredentialVariablesByHostingType(any(), any(), any(), any());

        doReturn("""
                    terraform {
                      required_providers {
                        huaweicloud = {
                          source = "huaweicloud/huaweicloud"
                          version = "~>1.51.0"
                        }
                      }
                    }
                                
                    provider "huaweicloud" {
                      region = "test"
                    }
                """).when(this.pluginManager)
                .getDeployerProvider(any(Csp.class), any(DeployerKind.class), any());
    }

    @Test
    @Order(1)
    void testDeploy() {
        DeployTask deployTask = new DeployTask();
        deployTask.setId(id);
        deployTask.setOcl(ocl);
        deployTask.setDeployRequest(deployRequest);
        deployResult = openTofuLocalDeployment.deploy(deployTask);
        Assertions.assertNotNull(deployResult);
        Assertions.assertNotNull(deployResult.getPrivateProperties());
        tfState = deployResult.getPrivateProperties().get(STATE_FILE_NAME);
        Assertions.assertNull(tfState);
        Assertions.assertNull(deployResult.getState());

    }

    @Test
    @Order(2)
    void testDestroy() {
        DeployTask deployTask = new DeployTask();
        deployTask.setId(id);
        deployTask.setOcl(ocl);
        deployTask.setDeployRequest(deployRequest);
        if (StringUtils.isBlank(tfState)) {
            testDeploy();
        }
        when(this.deployEnvironments.getFlavorVariables(any(DeployTask.class))).thenReturn(
                new HashMap<>());
        Assertions.assertThrows(ServiceNotDeployedException.class,
                () -> openTofuLocalDeployment.destroy(deployTask));
    }

    @Test
    @Order(3)
    void testDeleteTaskWorkspace() {
        String workspacePath = System.getProperty("java.io.tmpdir") + File.separator
                + openTofuLocalConfig.getWorkspaceDirectory() + File.separator + id;
        this.openTofuLocalDeployment.deleteTaskWorkspace(id);
        Assertions.assertFalse(new File(workspacePath).exists());
    }

    @Test
    void testDeploy_FailedCausedByOpenTofuExecutorException() {
        String invalidDeployer = """
                resource "random_id" "new" {
                  byte_length = 4
                }
                                        
                output "random_id" {
                  value = resource.random_id_2.new.id
                }
                """;
        ocl.getDeployment().setDeployer(invalidDeployer);
        DeployTask deployTask = new DeployTask();
        deployTask.setId(UUID.randomUUID());
        deployTask.setOcl(ocl);
        deployTask.setDeployRequest(deployRequest);
        DeployResult deployResult = this.openTofuLocalDeployment.deploy(deployTask);
        Assertions.assertNull(deployResult.getState());
        Assertions.assertNotEquals(DeployerTaskStatus.DEPLOY_SUCCESS.toValue(),
                deployResult.getMessage());

    }

    @Test
    void testDestroy_FailedCausedByOpenTofuExecutorException() {
        try (MockedStatic<TfResourceTransUtils> tfResourceTransUtils = Mockito.mockStatic(
                TfResourceTransUtils.class)) {
            tfResourceTransUtils.when(() -> TfResourceTransUtils.getStoredStateContent(any()))
                    .thenReturn("Test");
            ocl.getDeployment().setDeployer(errorDeployer);
            DeployTask deployTask = new DeployTask();
            deployTask.setId(id);
            deployTask.setOcl(ocl);
            deployTask.setDeployRequest(deployRequest);
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
        DeployRequest deployRequest = new DeployRequest();
        deployRequest.setServiceName(ocl.getName());
        deployRequest.setVersion(ocl.getServiceVersion());
        deployRequest.setFlavor(ocl.getFlavors().getFirst().getName());
        deployRequest.setRegion(ocl.getCloudServiceProvider().getRegions().getFirst().getName());
        deployRequest.setCsp(ocl.getCloudServiceProvider().getName());
        deployRequest.setCategory(ocl.getCategory());
        deployRequest.setCustomerServiceName("test_deploy");
        deployRequest.setServiceRequestProperties(Map.ofEntries(Map.entry("key", "value")));

        DeployTask deployTask = new DeployTask();
        deployTask.setId(id);
        deployTask.setOcl(ocl);
        deployTask.setDeployRequest(deployRequest);

        String deployPlanJson = openTofuLocalDeployment.getDeploymentPlanAsJson(deployTask);
        Assertions.assertNotNull(deployPlanJson);

    }

    @Test
    void testGetDeployPlanAsJson_ThrowsException() {

        ocl.getDeployment().setDeployer(errorDeployer);
        DeployTask deployTask = new DeployTask();
        deployTask.setId(UUID.randomUUID());
        deployTask.setOcl(ocl);
        deployTask.setDeployRequest(deployRequest);
        deployResult = this.openTofuLocalDeployment.deploy(deployTask);

        Assertions.assertThrows(OpenTofuExecutorException.class,
                () -> this.openTofuLocalDeployment.getDeploymentPlanAsJson(deployTask));

    }

}
