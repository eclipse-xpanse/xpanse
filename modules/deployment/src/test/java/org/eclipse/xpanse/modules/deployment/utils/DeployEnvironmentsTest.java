/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.service.deployment.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.FlavorInvalidException;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.models.servicetemplate.CloudServiceProvider;
import org.eclipse.xpanse.modules.models.servicetemplate.DeployVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.Deployment;
import org.eclipse.xpanse.modules.models.servicetemplate.FlavorsWithPrice;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceFlavorWithPrice;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployVariableKind;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.security.secrets.SecretsManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

/** Test of DeployEnvironments. */
@Slf4j
@ExtendWith(MockitoExtension.class)
class DeployEnvironmentsTest {

    private final String userId = "userId";
    private final String siteName = "Chinese Mainland";
    private final String regionName = "cn-north-4";
    private final UUID serviceId = UUID.randomUUID();

    private DeployTask task;
    private DeployRequest deployRequest;
    private FlavorsWithPrice flavors;
    private DeployVariable deployVariable1;
    private DeployVariable deployVariable2;
    private DeployVariable deployVariable3;
    private DeployVariable deployVariable4;
    @Mock private SecretsManager secretsManager;
    @Mock private CredentialCenter mockCredentialCenter;
    @Mock private PluginManager pluginManager;
    @Mock private Environment environment;
    @Mock private OrchestratorPlugin mockOrchestratorPlugin;
    private DeployEnvironments deployEnvironmentsUnderTest;

    @BeforeEach
    void setUp() {
        Map<String, Object> serviceRequestProperties = new HashMap<>();
        serviceRequestProperties.put("name", "value");
        serviceRequestProperties.put("key2", "value2");
        serviceRequestProperties.put("example", null);

        deployRequest = new DeployRequest();
        deployRequest.setFlavor("flavor");
        deployRequest.setServiceRequestProperties(serviceRequestProperties);
        deployRequest.setServiceHostingType(ServiceHostingType.SELF);
        deployRequest.setCsp(Csp.HUAWEI_CLOUD);
        Region region = new Region();
        region.setName(regionName);
        String areaName = "Asia China";
        region.setArea(areaName);
        region.setSite(siteName);
        deployRequest.setRegion(region);

        Deployment deployment = new Deployment();
        deployVariable1 = new DeployVariable();
        deployVariable1.setName("name");
        deployVariable1.setKind(DeployVariableKind.ENV);
        deployVariable1.setValue("value");

        deployVariable2 = new DeployVariable();
        deployVariable2.setName("key1");
        deployVariable2.setKind(DeployVariableKind.ENV_ENV);
        deployVariable2.setValue("value1");

        deployVariable3 = new DeployVariable();
        deployVariable3.setName("key2");
        deployVariable3.setKind(DeployVariableKind.FIX_ENV);
        deployVariable3.setValue("value2");

        deployVariable4 = new DeployVariable();
        deployVariable4.setName("example");
        deployVariable4.setKind(DeployVariableKind.ENV);
        deployVariable4.setValue("example_value");

        deployment.setVariables(
                List.of(deployVariable1, deployVariable2, deployVariable3, deployVariable4));
        deployment.setCredentialType(CredentialType.VARIABLES);

        flavors = new FlavorsWithPrice();
        ServiceFlavorWithPrice flavor = new ServiceFlavorWithPrice();
        flavor.setName("flavor");
        flavor.setProperties(Map.ofEntries(Map.entry("key", "value")));
        flavors.setServiceFlavors(List.of(flavor));

        CloudServiceProvider cloudServiceProvider = new CloudServiceProvider();
        cloudServiceProvider.setName(Csp.HUAWEI_CLOUD);

        Ocl ocl = new Ocl();

        ocl.setDeployment(deployment);
        ocl.setFlavors(flavors);
        ocl.setCloudServiceProvider(cloudServiceProvider);

        task = new DeployTask();
        task.setUserId(userId);
        task.setDeployRequest(deployRequest);
        task.setOcl(ocl);
        task.setServiceId(serviceId);

        deployEnvironmentsUnderTest =
                new DeployEnvironments(
                        mockCredentialCenter, secretsManager, pluginManager, environment);
    }

    @Test
    void testGetEnv() {
        Map<String, String> expectedResult = new HashMap<>();
        expectedResult.put("name", "value");
        expectedResult.put("example", null);
        expectedResult.put("key1", null);
        expectedResult.put("key2", "value2");
        task.getOcl().getCloudServiceProvider().setName(Csp.HUAWEI_CLOUD);
        when(pluginManager.getOrchestratorPlugin(any())).thenReturn(mockOrchestratorPlugin);
        when(pluginManager.getOrchestratorPlugin(Csp.HUAWEI_CLOUD).getEnvVarKeysMappingMap())
                .thenReturn(Collections.emptyMap());
        Map<String, String> result = deployEnvironmentsUnderTest.getEnvironmentVariables(task);
        assertThat(result).isEqualTo(expectedResult);

        String osAuthUrl = "http://127.0.0.1";
        expectedResult.put("OS_AUTH_URL", osAuthUrl);
        task.getOcl().getCloudServiceProvider().setName(Csp.OPENSTACK_TESTLAB);
        when(pluginManager.getOrchestratorPlugin(Csp.OPENSTACK_TESTLAB).getEnvVarKeysMappingMap())
                .thenReturn(Map.of("OS_AUTH_URL", "OPENSTACK_TESTLAB_AUTH_URL"));
        when(environment.getProperty("OPENSTACK_TESTLAB_AUTH_URL")).thenReturn(osAuthUrl);
        Map<String, String> result2 = deployEnvironmentsUnderTest.getEnvironmentVariables(task);
        assertThat(result2).isEqualTo(expectedResult);
    }

    @Test
    void testGetFlavorVariables() {
        Map<String, String> expectedResult =
                Map.ofEntries(Map.entry("region", "cn-north-4"), Map.entry("key", "value"));

        Map<String, Object> result = deployEnvironmentsUnderTest.getInputVariables(task, true);

        // Verify the results
        assertEquals(result, expectedResult);
    }

    @Test
    void testGetFlavorVariables_FlavorInvalidException() {
        ServiceFlavorWithPrice flavor = new ServiceFlavorWithPrice();
        flavor.setName("name");
        flavors.setServiceFlavors(List.of(flavor));

        // Verify the results
        assertThrows(
                FlavorInvalidException.class,
                () -> deployEnvironmentsUnderTest.getInputVariables(task, true));
    }

    @Test
    void testGetVariables() {
        deployVariable1.setKind(DeployVariableKind.VARIABLE);
        deployVariable4.setKind(DeployVariableKind.VARIABLE);
        deployVariable2.setKind(DeployVariableKind.ENV_VARIABLE);
        deployVariable3.setKind(DeployVariableKind.FIX_VARIABLE);

        Map<String, String> expectedResult = new HashMap<>();
        expectedResult.put("example", null);
        expectedResult.put("key", "value");
        expectedResult.put("key1", null);
        expectedResult.put("key2", "value2");
        expectedResult.put("name", "value");
        expectedResult.put("region", regionName);

        final Map<String, Object> result =
                deployEnvironmentsUnderTest.getInputVariables(task, true);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetCredentialVariablesWithHostingType_SELF() {
        deployVariable1.setKind(DeployVariableKind.VARIABLE);
        deployVariable4.setKind(DeployVariableKind.VARIABLE);
        deployVariable2.setKind(DeployVariableKind.ENV_VARIABLE);
        deployVariable3.setKind(DeployVariableKind.FIX_VARIABLE);

        Csp csp = Csp.HUAWEI_CLOUD;
        List<CredentialVariable> variables = new ArrayList<>();
        variables.add(new CredentialVariable("HW_AK", "The access key.", true));
        variables.add(new CredentialVariable("HW_SK", "The security key.", true));

        CredentialType credentialType = CredentialType.VARIABLES;

        AbstractCredentialInfo abstractCredentialInfo =
                new CredentialVariables(
                        csp, siteName, credentialType, "AK_SK", "description", userId, variables);
        when(mockCredentialCenter.getCredential(csp, siteName, credentialType, userId))
                .thenReturn(abstractCredentialInfo);
        when(pluginManager.getOrchestratorPlugin(any())).thenReturn(mockOrchestratorPlugin);
        when(pluginManager.getOrchestratorPlugin(Csp.HUAWEI_CLOUD).getEnvVarKeysMappingMap())
                .thenReturn(Collections.emptyMap());
        Map<String, String> variablesActual =
                deployEnvironmentsUnderTest.getEnvironmentVariables(task);

        assertEquals(2, variablesActual.size());
        for (CredentialVariable variable : variables) {
            assertTrue(variablesActual.containsKey(variable.getName()));
            assertEquals(variable.getValue(), variablesActual.get(variable.getName()));
        }
        verify(mockCredentialCenter, times(1)).getCredential(csp, siteName, credentialType, userId);
    }

    @Test
    void testGetCredentialVariablesWithHostingType_SERVICE_VENDOR() {
        deployVariable1.setKind(DeployVariableKind.VARIABLE);
        deployVariable4.setKind(DeployVariableKind.VARIABLE);
        deployVariable2.setKind(DeployVariableKind.ENV_VARIABLE);
        deployVariable3.setKind(DeployVariableKind.FIX_VARIABLE);
        deployRequest.setServiceHostingType(ServiceHostingType.SERVICE_VENDOR);
        Csp csp = Csp.HUAWEI_CLOUD;
        List<CredentialVariable> variables = new ArrayList<>();
        variables.add(new CredentialVariable("HW_AK", "The access key.", true));
        variables.add(new CredentialVariable("HW_SK", "The security key.", true));

        CredentialType credentialType = CredentialType.VARIABLES;

        AbstractCredentialInfo abstractCredentialInfo =
                new CredentialVariables(
                        csp, siteName, credentialType, "AK_SK", "description", null, variables);
        when(mockCredentialCenter.getCredential(csp, siteName, credentialType, null))
                .thenReturn(abstractCredentialInfo);
        when(pluginManager.getOrchestratorPlugin(any())).thenReturn(mockOrchestratorPlugin);
        when(pluginManager.getOrchestratorPlugin(Csp.HUAWEI_CLOUD).getEnvVarKeysMappingMap())
                .thenReturn(Collections.emptyMap());
        Map<String, String> variablesActual =
                deployEnvironmentsUnderTest.getEnvironmentVariables(task);

        assertEquals(2, variablesActual.size());
        for (CredentialVariable variable : variables) {
            assertTrue(variablesActual.containsKey(variable.getName()));
            assertEquals(variable.getValue(), variablesActual.get(variable.getName()));
        }
        verify(mockCredentialCenter, times(1)).getCredential(csp, siteName, credentialType, null);
    }

    @Test
    void testGetPluginMandatoryVariable() throws Exception {

        OclLoader oclLoader = new OclLoader();
        Ocl ocl =
                oclLoader.getOcl(
                        URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());

        DeployRequest deployRequest = new DeployRequest();
        deployRequest.setServiceName(ocl.getName());
        deployRequest.setCustomerServiceName("test");
        deployRequest.setCsp(ocl.getCloudServiceProvider().getName());
        deployRequest.setVersion(ocl.getServiceVersion());
        deployRequest.setFlavor(ocl.getFlavors().getServiceFlavors().getFirst().getName());
        Region region = new Region();
        region.setName(regionName);
        String areaName = "Asia China";
        region.setArea(areaName);
        region.setSite(siteName);
        deployRequest.setRegion(region);

        Map<String, Object> property = new HashMap<>();
        property.put("secgroup_id", "1234567890");
        deployRequest.setServiceRequestProperties(property);

        DeployTask xpanseDeployTask = new DeployTask();
        xpanseDeployTask.setServiceId(UUID.randomUUID());
        xpanseDeployTask.setOrderId(UUID.randomUUID());
        xpanseDeployTask.setTaskType(ServiceOrderType.DEPLOY);
        xpanseDeployTask.setUserId("userId");
        xpanseDeployTask.setOcl(ocl);
        xpanseDeployTask.setDeployRequest(deployRequest);
        when(this.pluginManager.getOrchestratorPlugin(any(Csp.class)))
                .thenReturn(mockOrchestratorPlugin);
        when(mockOrchestratorPlugin.requiredProperties()).thenReturn(List.of("OS_AUTH_URL"));
        Map<String, String> variables =
                deployEnvironmentsUnderTest.getEnvironmentVariables(xpanseDeployTask);

        Assertions.assertNotNull(variables);
    }
}
