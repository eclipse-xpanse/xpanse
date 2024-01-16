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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.monitor.Metric;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.FlavorInvalidException;
import org.eclipse.xpanse.modules.models.servicetemplate.CloudServiceProvider;
import org.eclipse.xpanse.modules.models.servicetemplate.DeployVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.Deployment;
import org.eclipse.xpanse.modules.models.servicetemplate.Flavor;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployVariableKind;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResourceHandler;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.servicestate.ServiceStateManageRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ResourceMetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ServiceMetricsRequest;
import org.eclipse.xpanse.modules.security.common.AesUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

/**
 * Test of DeployEnvironments.
 */
@ExtendWith(MockitoExtension.class)
class DeployEnvironmentsTest {

    private static final String userId = "userId";
    private static DeployTask task;
    private static DeployRequest deployRequest;
    private static Ocl ocl;
    private static Flavor flavor;
    private static DeployVariable deployVariable1;
    private static DeployVariable deployVariable2;
    private static DeployVariable deployVariable3;
    private static DeployVariable deployVariable4;
    @Mock
    private AesUtil aesUtil;
    @Mock
    private CredentialCenter mockCredentialCenter;
    @Mock
    private PluginManager pluginManager;
    @Mock
    private Environment environment;
    private DeployEnvironments deployEnvironmentsUnderTest;

    @BeforeEach
    void setUp() {
        Map<String, Object> serviceRequestProperties = new HashMap<>();
        serviceRequestProperties.put("name", "value");
        serviceRequestProperties.put("key2", "value2");
        serviceRequestProperties.put("example", null);

        deployRequest = new DeployRequest();
        deployRequest.setUserId(userId);
        deployRequest.setFlavor("flavor");
        deployRequest.setServiceRequestProperties(serviceRequestProperties);
        deployRequest.setServiceHostingType(ServiceHostingType.SELF);
        deployRequest.setCsp(Csp.HUAWEI);

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

        flavor = new Flavor();
        flavor.setName("flavor");
        flavor.setProperties(Map.ofEntries(Map.entry("key", "value")));

        CloudServiceProvider cloudServiceProvider = new CloudServiceProvider();
        cloudServiceProvider.setName(Csp.HUAWEI);

        ocl = new Ocl();
        ocl.setDeployment(deployment);
        ocl.setFlavors(List.of(flavor));
        ocl.setCloudServiceProvider(cloudServiceProvider);

        task = new DeployTask();
        task.setDeployRequest(deployRequest);
        task.setOcl(ocl);

        deployEnvironmentsUnderTest = new DeployEnvironments(mockCredentialCenter, aesUtil,
                pluginManager, environment);
    }

    @Test
    void testGetEnv() {
        Map<String, String> expectedResult = new HashMap<>();
        expectedResult.put("name", "value");
        expectedResult.put("example", null);
        expectedResult.put("key1", null);
        expectedResult.put("key2", "value2");

        Map<String, String> result = deployEnvironmentsUnderTest.getEnvFromDeployTask(task);

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetFlavorVariables() {
        Map<String, String> expectedResult = Map.ofEntries(Map.entry("key", "value"));

        Map<String, String> result = deployEnvironmentsUnderTest.getFlavorVariables(task);

        // Verify the results
        assertEquals(result, expectedResult);
    }


    @Test
    void testGetFlavorVariables_FlavorInvalidException() {
        flavor.setName("name");

        // Verify the results
        assertThrows(FlavorInvalidException.class,
                () -> deployEnvironmentsUnderTest.getFlavorVariables(task));
    }

    @Test
    void testGetVariables() {
        deployVariable1.setKind(DeployVariableKind.VARIABLE);
        deployVariable4.setKind(DeployVariableKind.VARIABLE);
        deployVariable2.setKind(DeployVariableKind.ENV_VARIABLE);
        deployVariable3.setKind(DeployVariableKind.FIX_VARIABLE);

        Map<String, String> expectedResult = new HashMap<>();
        expectedResult.put("name", "value");
        expectedResult.put("key1", null);
        expectedResult.put("key2", "value2");
        expectedResult.put("example", null);

        final Map<String, Object> result = deployEnvironmentsUnderTest.getVariablesFromDeployTask(task, true);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetCredentialVariablesWithHostingType_SELF() {
        deployVariable1.setKind(DeployVariableKind.VARIABLE);
        deployVariable4.setKind(DeployVariableKind.VARIABLE);
        deployVariable2.setKind(DeployVariableKind.ENV_VARIABLE);
        deployVariable3.setKind(DeployVariableKind.FIX_VARIABLE);

        Csp csp = Csp.HUAWEI;
        List<CredentialVariable> variables = new ArrayList<>();
        variables.add(
                new CredentialVariable(
                        "HW_AK",
                        "The access key.", true));
        variables.add(
                new CredentialVariable("HW_SK",
                        "The security key.", true));

        CredentialType credentialType = CredentialType.VARIABLES;

        AbstractCredentialInfo abstractCredentialInfo =
                new CredentialVariables(csp, credentialType, "AK_SK", "description", userId,
                        variables);
        when(mockCredentialCenter.getCredential(csp, credentialType,
                deployRequest.getUserId()))
                .thenReturn(abstractCredentialInfo);

        Map<String, String> variablesActual =
                deployEnvironmentsUnderTest.getCredentialVariablesByHostingType(
                        task.getDeployRequest().getServiceHostingType(),
                        task.getOcl().getDeployment().getCredentialType(),
                        task.getDeployRequest().getCsp(),
                        task.getDeployRequest().getUserId());

        assertEquals(2, variablesActual.size());
        for (CredentialVariable variable : variables) {
            assertTrue(variablesActual.containsKey(variable.getName()));
            assertEquals(variable.getValue(), variablesActual.get(variable.getName()));
        }
        verify(mockCredentialCenter, times(1))
                .getCredential(csp, credentialType, userId);

    }

    @Test
    void testGetCredentialVariablesWithHostingType_SERVICE_VENDOR() {
        deployVariable1.setKind(DeployVariableKind.VARIABLE);
        deployVariable4.setKind(DeployVariableKind.VARIABLE);
        deployVariable2.setKind(DeployVariableKind.ENV_VARIABLE);
        deployVariable3.setKind(DeployVariableKind.FIX_VARIABLE);
        deployRequest.setServiceHostingType(ServiceHostingType.SERVICE_VENDOR);
        Csp csp = Csp.HUAWEI;
        List<CredentialVariable> variables = new ArrayList<>();
        variables.add(
                new CredentialVariable(
                        "HW_AK",
                        "The access key.", true));
        variables.add(
                new CredentialVariable("HW_SK",
                        "The security key.", true));

        CredentialType credentialType = CredentialType.VARIABLES;

        AbstractCredentialInfo abstractCredentialInfo =
                new CredentialVariables(csp, credentialType, "AK_SK", "description", null,
                        variables);
        when(mockCredentialCenter.getCredential(csp, credentialType,
                null))
                .thenReturn(abstractCredentialInfo);

        Map<String, String> variablesActual =
                deployEnvironmentsUnderTest.getCredentialVariablesByHostingType(
                        task.getDeployRequest().getServiceHostingType(),
                        task.getOcl().getDeployment().getCredentialType(),
                        task.getDeployRequest().getCsp(),
                        task.getDeployRequest().getUserId());

        assertEquals(2, variablesActual.size());
        for (CredentialVariable variable : variables) {
            assertTrue(variablesActual.containsKey(variable.getName()));
            assertEquals(variable.getValue(), variablesActual.get(variable.getName()));
        }
        verify(mockCredentialCenter, times(1))
                .getCredential(csp, credentialType, null);


    }

    @Test
    void testGetPluginMandatoryVariable() throws Exception {

        OclLoader oclLoader = new OclLoader();
        Ocl ocl = oclLoader.getOcl(URI.create("file:src/test/resources/ocl_test.yaml").toURL());

        DeployRequest deployRequest = new DeployRequest();
        deployRequest.setServiceName(ocl.getName());
        deployRequest.setCustomerServiceName("test");
        deployRequest.setCsp(ocl.getCloudServiceProvider().getName());
        deployRequest.setVersion(ocl.getServiceVersion());
        deployRequest.setFlavor(ocl.getFlavors().get(0).getName());

        Map<String, Object> property = new HashMap<>();
        property.put("secgroup_id", "1234567890");
        deployRequest.setServiceRequestProperties(property);

        DeployTask xpanseDeployTask = new DeployTask();
        xpanseDeployTask.setId(UUID.randomUUID());
        xpanseDeployTask.setOcl(ocl);
        xpanseDeployTask.setDeployResourceHandler(null);
        xpanseDeployTask.setDeployRequest(deployRequest);

        OrchestratorPlugin plugin = new OrchestratorPlugin() {
            @Override
            public boolean startService(ServiceStateManageRequest serviceStateManageRequest) {
                return true;
            }

            @Override
            public boolean stopService(ServiceStateManageRequest serviceStateManageRequest) {
                return true;
            }

            @Override
            public boolean restartService(ServiceStateManageRequest serviceStateManageRequest) {
                return true;
            }

            @Override
            public Csp getCsp() {
                return Csp.OPENSTACK;
            }

            @Override
            public List<String> requiredProperties() {
                return List.of("OS_AUTH_URL");
            }

            @Override
            public List<CredentialType> getAvailableCredentialTypes() {
                return null;
            }

            @Override
            public List<AbstractCredentialInfo> getCredentialDefinitions() {
                return null;
            }

            @Override
            public DeployResourceHandler getResourceHandler() {
                return null;
            }

            @Override
            public String getProvider(String region) {
                return null;
            }

            @Override
            public List<Metric> getMetricsForResource(
                    ResourceMetricsRequest resourceMetricRequest) {
                return null;
            }

            @Override
            public List<Metric> getMetricsForService(ServiceMetricsRequest serviceMetricRequest) {
                return null;
            }
        };
        when(this.pluginManager.getOrchestratorPlugin(any(Csp.class))).thenReturn(plugin);
        Map<String, String> variables =
                deployEnvironmentsUnderTest.getPluginMandatoryVariables(xpanseDeployTask.getDeployRequest().getCsp());

        Assertions.assertNotNull(variables);
    }

}
