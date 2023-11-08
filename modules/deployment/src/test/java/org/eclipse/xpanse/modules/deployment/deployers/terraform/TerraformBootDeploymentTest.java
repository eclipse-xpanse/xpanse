/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.config.TerraformBootConfig;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.api.TerraformApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.TerraformAsyncDeployFromDirectoryRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.TerraformAsyncDestroyFromDirectoryRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.WebhookConfig;
import org.eclipse.xpanse.modules.deployment.utils.DeployEnvironments;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResult;
import org.eclipse.xpanse.modules.models.service.deploy.enums.TerraformExecState;
import org.eclipse.xpanse.modules.models.servicetemplate.CloudServiceProvider;
import org.eclipse.xpanse.modules.models.servicetemplate.Deployment;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class TerraformBootDeploymentTest {

    @Mock
    private DeployEnvironments mockDeployEnvironments;
    @Mock
    private PluginManager mockPluginManager;
    @Mock
    private TerraformBootConfig mockTerraformBootConfig;
    @Mock
    private TerraformApi mockTerraformApi;

    private TerraformBootDeployment terraformBootDeploymentUnderTest;

    @BeforeEach
    void setUp() {
        terraformBootDeploymentUnderTest =
                new TerraformBootDeployment(mockDeployEnvironments, mockPluginManager,
                        mockTerraformBootConfig, mockTerraformApi, "port");

    }

    @Test
    void testDeploy() {
        // Setup
        final DeployTask deployTask = new DeployTask();
        deployTask.setId(UUID.fromString("800b6caa-5710-4064-8bae-cb446a351cc1"));
        final DeployRequest deployRequest = new DeployRequest();
        deployRequest.setRegion("region");
        deployRequest.setCsp(Csp.HUAWEI);
        deployTask.setDeployRequest(deployRequest);
        final Ocl ocl = new Ocl();
        final CloudServiceProvider cloudServiceProvider = new CloudServiceProvider();
        cloudServiceProvider.setName(Csp.HUAWEI);
        final Region region = new Region();
        region.setName("name");
        cloudServiceProvider.setRegions(List.of(region));
        ocl.setCloudServiceProvider(cloudServiceProvider);
        final Deployment deployment = new Deployment();
        deployment.setDeployer("deployer");
        ocl.setDeployment(deployment);
        deployTask.setOcl(ocl);

        final DeployResult expectedResult = new DeployResult();
        expectedResult.setId(UUID.fromString("2d8fb67f-ef08-4021-9a10-16ac92a6f958"));
        expectedResult.setState(TerraformExecState.INIT);
        final DeployResource deployResource = new DeployResource();
        deployResource.setResourceId("resourceId");
        deployResource.setName("name");
        expectedResult.setResources(List.of(deployResource));

        when(mockPluginManager.getTerraformProviderForRegionByCsp(Csp.HUAWEI, "region"))
                .thenReturn("result");

        // Configure DeployEnvironments.getVariables(...).
        final Map<String, Object> stringStringMap = Map.ofEntries(Map.entry("value", "value"));
        final DeployTask task = new DeployTask();
        task.setId(UUID.fromString("800b6caa-5710-4064-8bae-cb446a351cc1"));
        final DeployRequest deployRequest1 = new DeployRequest();
        deployRequest1.setRegion("region");
        deployRequest1.setCsp(Csp.HUAWEI);
        task.setDeployRequest(deployRequest1);
        final Ocl ocl1 = new Ocl();
        final CloudServiceProvider cloudServiceProvider1 = new CloudServiceProvider();
        cloudServiceProvider1.setName(Csp.HUAWEI);
        final Region region1 = new Region();
        region1.setName("name");
        cloudServiceProvider1.setRegions(List.of(region1));
        ocl1.setCloudServiceProvider(cloudServiceProvider1);
        final Deployment deployment1 = new Deployment();
        deployment1.setDeployer("deployer");
        ocl1.setDeployment(deployment1);
        task.setOcl(ocl1);
        when(mockDeployEnvironments.getVariables(task, true)).thenReturn(stringStringMap);

        // Configure DeployEnvironments.getFlavorVariables(...).
        final Map<String, String> stringStringMap1 = Map.ofEntries(Map.entry("value", "value"));
        final DeployTask task1 = new DeployTask();
        task1.setId(UUID.fromString("800b6caa-5710-4064-8bae-cb446a351cc1"));
        final DeployRequest deployRequest2 = new DeployRequest();
        deployRequest2.setRegion("region");
        deployRequest2.setCsp(Csp.HUAWEI);
        task1.setDeployRequest(deployRequest2);
        final Ocl ocl2 = new Ocl();
        final CloudServiceProvider cloudServiceProvider2 = new CloudServiceProvider();
        cloudServiceProvider2.setName(Csp.HUAWEI);
        final Region region2 = new Region();
        region2.setName("name");
        cloudServiceProvider2.setRegions(List.of(region2));
        ocl2.setCloudServiceProvider(cloudServiceProvider2);
        final Deployment deployment2 = new Deployment();
        deployment2.setDeployer("deployer");
        ocl2.setDeployment(deployment2);
        task1.setOcl(ocl2);
        when(mockDeployEnvironments.getFlavorVariables(task1)).thenReturn(stringStringMap1);

        // Configure DeployEnvironments.getEnv(...).
        final Map<String, String> stringStringMap2 = Map.ofEntries(Map.entry("value", "value"));
        final DeployTask task2 = new DeployTask();
        task2.setId(UUID.fromString("800b6caa-5710-4064-8bae-cb446a351cc1"));
        final DeployRequest deployRequest3 = new DeployRequest();
        deployRequest3.setRegion("region");
        deployRequest3.setCsp(Csp.HUAWEI);
        task2.setDeployRequest(deployRequest3);
        final Ocl ocl3 = new Ocl();
        final CloudServiceProvider cloudServiceProvider3 = new CloudServiceProvider();
        cloudServiceProvider3.setName(Csp.HUAWEI);
        final Region region3 = new Region();
        region3.setName("name");
        cloudServiceProvider3.setRegions(List.of(region3));
        ocl3.setCloudServiceProvider(cloudServiceProvider3);
        final Deployment deployment3 = new Deployment();
        deployment3.setDeployer("deployer");
        ocl3.setDeployment(deployment3);
        task2.setOcl(ocl3);
        when(mockDeployEnvironments.getEnv(task2)).thenReturn(stringStringMap2);

        // Configure DeployEnvironments.getCredentialVariables(...).
        final Map<String, String> stringStringMap3 = Map.ofEntries(Map.entry("value", "value"));
        final DeployTask task3 = new DeployTask();
        task3.setId(UUID.fromString("800b6caa-5710-4064-8bae-cb446a351cc1"));
        final DeployRequest deployRequest4 = new DeployRequest();
        deployRequest4.setRegion("region");
        deployRequest4.setCsp(Csp.HUAWEI);
        task3.setDeployRequest(deployRequest4);
        final Ocl ocl4 = new Ocl();
        final CloudServiceProvider cloudServiceProvider4 = new CloudServiceProvider();
        cloudServiceProvider4.setName(Csp.HUAWEI);
        final Region region4 = new Region();
        region4.setName("name");
        cloudServiceProvider4.setRegions(List.of(region4));
        ocl4.setCloudServiceProvider(cloudServiceProvider4);
        final Deployment deployment4 = new Deployment();
        deployment4.setDeployer("deployer");
        ocl4.setDeployment(deployment4);
        task3.setOcl(ocl4);
        when(mockDeployEnvironments.getCredentialVariablesByHostingType(task3)).thenReturn(stringStringMap3);

        // Configure DeployEnvironments.getPluginMandatoryVariables(...).
        final Map<String, String> stringStringMap4 = Map.ofEntries(Map.entry("value", "value"));
        final DeployTask task4 = new DeployTask();
        task4.setId(UUID.fromString("800b6caa-5710-4064-8bae-cb446a351cc1"));
        final DeployRequest deployRequest5 = new DeployRequest();
        deployRequest5.setRegion("region");
        deployRequest5.setCsp(Csp.HUAWEI);
        task4.setDeployRequest(deployRequest5);
        final Ocl ocl5 = new Ocl();
        final CloudServiceProvider cloudServiceProvider5 = new CloudServiceProvider();
        cloudServiceProvider5.setName(Csp.HUAWEI);
        final Region region5 = new Region();
        region5.setName("name");
        cloudServiceProvider5.setRegions(List.of(region5));
        ocl5.setCloudServiceProvider(cloudServiceProvider5);
        final Deployment deployment5 = new Deployment();
        deployment5.setDeployer("deployer");
        ocl5.setDeployment(deployment5);
        task4.setOcl(ocl5);
        when(mockDeployEnvironments.getPluginMandatoryVariables(task4))
                .thenReturn(stringStringMap4);

        when(mockTerraformBootConfig.getClientBaseUri()).thenReturn("result");
        when(mockTerraformBootConfig.getDeployCallbackUri()).thenReturn("result");

        // Run the test
        final DeployResult result = terraformBootDeploymentUnderTest.deploy(deployTask);

        // Verify the results
        assertThat(result).isEqualTo(new DeployResult());

        // Confirm TerraformApi.asyncDeployWithScripts(...).
        final TerraformAsyncDeployFromDirectoryRequest terraformAsyncDeployFromDirectoryRequest =
                new TerraformAsyncDeployFromDirectoryRequest();
        terraformAsyncDeployFromDirectoryRequest.setIsPlanOnly(false);
        terraformAsyncDeployFromDirectoryRequest.setVariables(
                Map.ofEntries(Map.entry("value", "value")));
        terraformAsyncDeployFromDirectoryRequest.setEnvVariables(
                Map.ofEntries(Map.entry("value", "value")));
        terraformAsyncDeployFromDirectoryRequest.setScripts(List.of("value"));
        final WebhookConfig webhookConfig = new WebhookConfig();
        webhookConfig.setUrl("url");
        webhookConfig.setAuthType(WebhookConfig.AuthTypeEnum.NONE);
        terraformAsyncDeployFromDirectoryRequest.setWebhookConfig(webhookConfig);
    }

    @Test
    void testDestroy() {
        // Setup
        final DeployTask task = new DeployTask();
        task.setId(UUID.fromString("800b6caa-5710-4064-8bae-cb446a351cc1"));
        final DeployRequest deployRequest = new DeployRequest();
        deployRequest.setRegion("region");
        deployRequest.setCsp(Csp.HUAWEI);
        task.setDeployRequest(deployRequest);
        final Ocl ocl = new Ocl();
        final CloudServiceProvider cloudServiceProvider = new CloudServiceProvider();
        cloudServiceProvider.setName(Csp.HUAWEI);
        final Region region = new Region();
        region.setName("name");
        cloudServiceProvider.setRegions(List.of(region));
        ocl.setCloudServiceProvider(cloudServiceProvider);
        final Deployment deployment = new Deployment();
        deployment.setDeployer("deployer");
        ocl.setDeployment(deployment);
        task.setOcl(ocl);

        final DeployResult expectedResult = new DeployResult();
        expectedResult.setId(UUID.fromString("2d8fb67f-ef08-4021-9a10-16ac92a6f958"));
        expectedResult.setState(TerraformExecState.INIT);
        final DeployResource deployResource = new DeployResource();
        deployResource.setResourceId("resourceId");
        deployResource.setName("name");
        expectedResult.setResources(List.of(deployResource));

        when(mockPluginManager.getTerraformProviderForRegionByCsp(Csp.HUAWEI, "region"))
                .thenReturn("result");

        // Configure DeployEnvironments.getVariables(...).
        final Map<String, Object> stringStringMap = Map.ofEntries(Map.entry("value", "value"));
        final DeployTask task1 = new DeployTask();
        task1.setId(UUID.fromString("800b6caa-5710-4064-8bae-cb446a351cc1"));
        final DeployRequest deployRequest1 = new DeployRequest();
        deployRequest1.setRegion("region");
        deployRequest1.setCsp(Csp.HUAWEI);
        task1.setDeployRequest(deployRequest1);
        final Ocl ocl1 = new Ocl();
        final CloudServiceProvider cloudServiceProvider1 = new CloudServiceProvider();
        cloudServiceProvider1.setName(Csp.HUAWEI);
        final Region region1 = new Region();
        region1.setName("name");
        cloudServiceProvider1.setRegions(List.of(region1));
        ocl1.setCloudServiceProvider(cloudServiceProvider1);
        final Deployment deployment1 = new Deployment();
        deployment1.setDeployer("deployer");
        ocl1.setDeployment(deployment1);
        task1.setOcl(ocl1);
        when(mockDeployEnvironments.getVariables(task1, true)).thenReturn(stringStringMap);

        // Configure DeployEnvironments.getFlavorVariables(...).
        final Map<String, String> stringStringMap1 = Map.ofEntries(Map.entry("value", "value"));
        final DeployTask task2 = new DeployTask();
        task2.setId(UUID.fromString("800b6caa-5710-4064-8bae-cb446a351cc1"));
        final DeployRequest deployRequest2 = new DeployRequest();
        deployRequest2.setRegion("region");
        deployRequest2.setCsp(Csp.HUAWEI);
        task2.setDeployRequest(deployRequest2);
        final Ocl ocl2 = new Ocl();
        final CloudServiceProvider cloudServiceProvider2 = new CloudServiceProvider();
        cloudServiceProvider2.setName(Csp.HUAWEI);
        final Region region2 = new Region();
        region2.setName("name");
        cloudServiceProvider2.setRegions(List.of(region2));
        ocl2.setCloudServiceProvider(cloudServiceProvider2);
        final Deployment deployment2 = new Deployment();
        deployment2.setDeployer("deployer");
        ocl2.setDeployment(deployment2);
        task2.setOcl(ocl2);
        when(mockDeployEnvironments.getFlavorVariables(task2)).thenReturn(stringStringMap1);

        // Configure DeployEnvironments.getEnv(...).
        final Map<String, String> stringStringMap2 = Map.ofEntries(Map.entry("value", "value"));
        final DeployTask task3 = new DeployTask();
        task3.setId(UUID.fromString("800b6caa-5710-4064-8bae-cb446a351cc1"));
        final DeployRequest deployRequest3 = new DeployRequest();
        deployRequest3.setRegion("region");
        deployRequest3.setCsp(Csp.HUAWEI);
        task3.setDeployRequest(deployRequest3);
        final Ocl ocl3 = new Ocl();
        final CloudServiceProvider cloudServiceProvider3 = new CloudServiceProvider();
        cloudServiceProvider3.setName(Csp.HUAWEI);
        final Region region3 = new Region();
        region3.setName("name");
        cloudServiceProvider3.setRegions(List.of(region3));
        ocl3.setCloudServiceProvider(cloudServiceProvider3);
        final Deployment deployment3 = new Deployment();
        deployment3.setDeployer("deployer");
        ocl3.setDeployment(deployment3);
        task3.setOcl(ocl3);
        when(mockDeployEnvironments.getEnv(task3)).thenReturn(stringStringMap2);

        // Configure DeployEnvironments.getCredentialVariables(...).
        final Map<String, String> stringStringMap3 = Map.ofEntries(Map.entry("value", "value"));
        final DeployTask task4 = new DeployTask();
        task4.setId(UUID.fromString("800b6caa-5710-4064-8bae-cb446a351cc1"));
        final DeployRequest deployRequest4 = new DeployRequest();
        deployRequest4.setRegion("region");
        deployRequest4.setCsp(Csp.HUAWEI);
        task4.setDeployRequest(deployRequest4);
        final Ocl ocl4 = new Ocl();
        final CloudServiceProvider cloudServiceProvider4 = new CloudServiceProvider();
        cloudServiceProvider4.setName(Csp.HUAWEI);
        final Region region4 = new Region();
        region4.setName("name");
        cloudServiceProvider4.setRegions(List.of(region4));
        ocl4.setCloudServiceProvider(cloudServiceProvider4);
        final Deployment deployment4 = new Deployment();
        deployment4.setDeployer("deployer");
        ocl4.setDeployment(deployment4);
        task4.setOcl(ocl4);
        when(mockDeployEnvironments.getCredentialVariablesByHostingType(task4)).thenReturn(stringStringMap3);

        // Configure DeployEnvironments.getPluginMandatoryVariables(...).
        final Map<String, String> stringStringMap4 = Map.ofEntries(Map.entry("value", "value"));
        final DeployTask task5 = new DeployTask();
        task5.setId(UUID.fromString("800b6caa-5710-4064-8bae-cb446a351cc1"));
        final DeployRequest deployRequest5 = new DeployRequest();
        deployRequest5.setRegion("region");
        deployRequest5.setCsp(Csp.HUAWEI);
        task5.setDeployRequest(deployRequest5);
        final Ocl ocl5 = new Ocl();
        final CloudServiceProvider cloudServiceProvider5 = new CloudServiceProvider();
        cloudServiceProvider5.setName(Csp.HUAWEI);
        final Region region5 = new Region();
        region5.setName("name");
        cloudServiceProvider5.setRegions(List.of(region5));
        ocl5.setCloudServiceProvider(cloudServiceProvider5);
        final Deployment deployment5 = new Deployment();
        deployment5.setDeployer("deployer");
        ocl5.setDeployment(deployment5);
        task5.setOcl(ocl5);
        when(mockDeployEnvironments.getPluginMandatoryVariables(task5))
                .thenReturn(stringStringMap4);

        when(mockTerraformBootConfig.getClientBaseUri()).thenReturn("result");
        when(mockTerraformBootConfig.getDestroyCallbackUri()).thenReturn("result");

        // Run the test
        final DeployResult result = terraformBootDeploymentUnderTest.destroy(task, "stateFile");

        // Verify the results
        assertThat(result).isEqualTo(new DeployResult());

        // Confirm TerraformApi.asyncDestroyWithScripts(...).
        final TerraformAsyncDestroyFromDirectoryRequest terraformAsyncDestroyFromDirectoryRequest =
                new TerraformAsyncDestroyFromDirectoryRequest();
        terraformAsyncDestroyFromDirectoryRequest.setVariables(
                Map.ofEntries(Map.entry("value", "value")));
        terraformAsyncDestroyFromDirectoryRequest.setEnvVariables(
                Map.ofEntries(Map.entry("value", "value")));
        terraformAsyncDestroyFromDirectoryRequest.setScripts(List.of("value"));
        terraformAsyncDestroyFromDirectoryRequest.setTfState("stateFile");
        final WebhookConfig webhookConfig = new WebhookConfig();
        webhookConfig.setUrl("url");
        webhookConfig.setAuthType(WebhookConfig.AuthTypeEnum.NONE);
        terraformAsyncDestroyFromDirectoryRequest.setWebhookConfig(webhookConfig);
    }

    @Test
    void testDeleteTaskWorkspace() {
        terraformBootDeploymentUnderTest.deleteTaskWorkspace("taskId");
    }

    @Test
    void testGetDeployerKind() {
        assertThat(terraformBootDeploymentUnderTest.getDeployerKind())
                .isEqualTo(DeployerKind.TERRAFORM);
    }
}
