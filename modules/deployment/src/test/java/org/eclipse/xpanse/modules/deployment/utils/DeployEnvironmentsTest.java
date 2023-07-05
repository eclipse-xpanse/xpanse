/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.CreateRequest;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.FlavorInvalidException;
import org.eclipse.xpanse.modules.models.service.register.CloudServiceProvider;
import org.eclipse.xpanse.modules.models.service.register.DeployVariable;
import org.eclipse.xpanse.modules.models.service.register.Deployment;
import org.eclipse.xpanse.modules.models.service.register.Flavor;
import org.eclipse.xpanse.modules.models.service.register.Ocl;
import org.eclipse.xpanse.modules.models.service.register.enums.DeployVariableKind;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test of DeployEnvironments.
 */
@ExtendWith(MockitoExtension.class)
class DeployEnvironmentsTest {

    private static final String userName = "userName";
    private static DeployTask task;
    private static CreateRequest createRequest;
    private static Ocl ocl;
    private static Flavor flavor;
    private static DeployVariable deployVariable1;
    private static DeployVariable deployVariable2;
    private static DeployVariable deployVariable3;
    private static DeployVariable deployVariable4;
    @Mock
    private CredentialCenter mockCredentialCenter;
    private DeployEnvironments deployEnvironmentsUnderTest;

    @BeforeEach
    void setUp() {
        Map<String, String> serviceRequestProperties = new HashMap<>();
        serviceRequestProperties.put("name", "value");
        serviceRequestProperties.put("key2", "value2");
        serviceRequestProperties.put("example", null);

        createRequest = new CreateRequest();
        createRequest.setUserName(userName);
        createRequest.setFlavor("flavor");
        createRequest.setServiceRequestProperties(serviceRequestProperties);


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
        task.setCreateRequest(createRequest);
        task.setOcl(ocl);

        deployEnvironmentsUnderTest = new DeployEnvironments(mockCredentialCenter);
    }

    @Test
    void testGetEnv() {
        Map<String, String> expectedResult = new HashMap<>();
        expectedResult.put("name", "value");
        expectedResult.put("example", null);
        expectedResult.put("key1", null);
        expectedResult.put("key2", "value2");

        Map<String, String> result = deployEnvironmentsUnderTest.getEnv(task);

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

        final Map<String, String> result = deployEnvironmentsUnderTest.getVariables(task);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetCredentialVariables() {
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
        CredentialVariables credentialVariables =
                new CredentialVariables(csp, userName, "name", "description", credentialType,
                        variables);

        AbstractCredentialInfo abstractCredentialInfo = credentialVariables;

        when(mockCredentialCenter.getCredential(csp, createRequest.getUserName(), credentialType))
                .thenReturn(abstractCredentialInfo);

        Map<String, String> variablesActual =
                deployEnvironmentsUnderTest.getCredentialVariables(task);

        assertEquals(2, variablesActual.size());
        for (CredentialVariable variable : variables) {
            assertTrue(variablesActual.containsKey(variable.getName()));
            assertEquals(variable.getValue(), variablesActual.get(variable.getName()));
        }
        verify(mockCredentialCenter, times(1))
                .getCredential(csp, userName, credentialType);
    }

}
