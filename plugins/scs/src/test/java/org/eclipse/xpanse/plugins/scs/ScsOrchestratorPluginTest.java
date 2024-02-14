/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.scs;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.eclipse.xpanse.modules.database.resource.DeployResourceEntity;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.orchestrator.servicestate.ServiceStateManageRequest;
import org.eclipse.xpanse.plugins.scs.common.constants.ScsEnvironmentConstants;
import org.eclipse.xpanse.plugins.scs.manage.ScsServersManager;
import org.eclipse.xpanse.plugins.scs.resourcehandler.ScsTerraformResourceHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {ScsOrchestratorPlugin.class})
class ScsOrchestratorPluginTest {
    private final String terraformScsVersion = "1.52.0";

    @InjectMocks
    private ScsOrchestratorPlugin plugin;
    @Mock
    private ScsTerraformResourceHandler mockScsTerraformResourceHandler;
    @Mock
    private ScsServersManager mockScsServersManager;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(plugin,
                "terraformScsVersion", terraformScsVersion);
    }
    @Test
    void getResourceHandler() {
        assertInstanceOf(ScsTerraformResourceHandler.class,
                plugin.resourceHandlers().get(DeployerKind.TERRAFORM));
    }

    @Test
    void getCsp() {
        assertEquals(Csp.SCS, plugin.getCsp());
    }

    @Test
    void getAvailableCredentialTypes() {
        assertEquals(List.of(CredentialType.VARIABLES), plugin.getAvailableCredentialTypes());
    }

    @Test
    void getCredentialDefinitions() {
        List<AbstractCredentialInfo> result = plugin.getCredentialDefinitions();

        //Verify whether the returned results meet expectations
        assertNotNull(result);
        assertEquals(1, result.size());

        //Verify that the attribute values of the CredentialVariables object match expectations
        CredentialVariables credentialVariables = (CredentialVariables) result.get(0);
        assertEquals(plugin.getCsp(), credentialVariables.getCsp());
        assertNull(credentialVariables.getUserId());
        assertEquals("USERNAME_PASSWORD", credentialVariables.getName());
        assertEquals("Authenticate at the specified URL using an account and password.",
                credentialVariables.getDescription());
        assertEquals(CredentialType.VARIABLES, credentialVariables.getType());

        List<CredentialVariable> variables = credentialVariables.getVariables();
        assertEquals(4, variables.size());

        //Verify the attribute value of the first CredentialVariable
        CredentialVariable authUrlVariable = variables.get(0);
        assertEquals(ScsEnvironmentConstants.PROJECT, authUrlVariable.getName());
        assertEquals("The Name of the Tenant or Project to use.", authUrlVariable.getDescription());
        assertTrue(authUrlVariable.getIsMandatory());
        assertFalse(authUrlVariable.getIsSensitive());
    }

    @Test
    void testRequiredProperties() {
        assertThat(plugin.requiredProperties()).isEqualTo(Collections.emptyList());
    }

    @Test
    void testGetProvider() {
        String region = "region";
        String result = String.format("""
                terraform {
                  required_providers {
                    openstack = {
                          source  = "terraform-provider-openstack/openstack"
                          version = "%s"
                        }
                  }
                }
                            
                provider "openstack" {
                  region = "%s"
                }
                """, terraformScsVersion, region);
        Assertions.assertThat(plugin.getProvider(DeployerKind.TERRAFORM,
                "region")).isEqualTo(result);
    }

    @Test
    void testStartService() {
        // Setup
        final ServiceStateManageRequest serviceStateManageRequest = new ServiceStateManageRequest();
        serviceStateManageRequest.setUserId("userId");
        serviceStateManageRequest.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity = new DeployResourceEntity();
        deployResourceEntity.setId(UUID.fromString("9c4f31a3-8673-47ae-ad66-fa02684748cf"));
        deployResourceEntity.setResourceId("resourceId");
        serviceStateManageRequest.setDeployResourceEntityList(List.of(deployResourceEntity));

        // Configure ServersManager.startService(...).
        final ServiceStateManageRequest serviceStateManageRequest1 =
                new ServiceStateManageRequest();
        serviceStateManageRequest1.setUserId("userId");
        serviceStateManageRequest1.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity1 = new DeployResourceEntity();
        deployResourceEntity1.setId(UUID.fromString("9c4f31a3-8673-47ae-ad66-fa02684748cf"));
        deployResourceEntity1.setResourceId("resourceId");
        serviceStateManageRequest1.setDeployResourceEntityList(List.of(deployResourceEntity1));
        when(mockScsServersManager.startService(serviceStateManageRequest1)).thenReturn(false);

        // Run the test
        final boolean result =
                plugin.startService(serviceStateManageRequest);

        // Verify the results
        Assertions.assertThat(result).isFalse();
    }

    @Test
    void testStartService_ServersManagerReturnsTrue() {
        // Setup
        final ServiceStateManageRequest serviceStateManageRequest = new ServiceStateManageRequest();
        serviceStateManageRequest.setUserId("userId");
        serviceStateManageRequest.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity = new DeployResourceEntity();
        deployResourceEntity.setId(UUID.fromString("9c4f31a3-8673-47ae-ad66-fa02684748cf"));
        deployResourceEntity.setResourceId("resourceId");
        serviceStateManageRequest.setDeployResourceEntityList(List.of(deployResourceEntity));

        // Configure ServersManager.startService(...).
        final ServiceStateManageRequest serviceStateManageRequest1 =
                new ServiceStateManageRequest();
        serviceStateManageRequest1.setUserId("userId");
        serviceStateManageRequest1.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity1 = new DeployResourceEntity();
        deployResourceEntity1.setId(UUID.fromString("9c4f31a3-8673-47ae-ad66-fa02684748cf"));
        deployResourceEntity1.setResourceId("resourceId");
        serviceStateManageRequest1.setDeployResourceEntityList(List.of(deployResourceEntity1));
        when(mockScsServersManager.startService(serviceStateManageRequest1)).thenReturn(true);

        // Run the test
        final boolean result =
                plugin.startService(serviceStateManageRequest);

        // Verify the results
        Assertions.assertThat(result).isTrue();
    }

    @Test
    void testStopService() {
        // Setup
        final ServiceStateManageRequest serviceStateManageRequest = new ServiceStateManageRequest();
        serviceStateManageRequest.setUserId("userId");
        serviceStateManageRequest.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity = new DeployResourceEntity();
        deployResourceEntity.setId(UUID.fromString("9c4f31a3-8673-47ae-ad66-fa02684748cf"));
        deployResourceEntity.setResourceId("resourceId");
        serviceStateManageRequest.setDeployResourceEntityList(List.of(deployResourceEntity));

        // Configure ServersManager.stopService(...).
        final ServiceStateManageRequest serviceStateManageRequest1 =
                new ServiceStateManageRequest();
        serviceStateManageRequest1.setUserId("userId");
        serviceStateManageRequest1.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity1 = new DeployResourceEntity();
        deployResourceEntity1.setId(UUID.fromString("9c4f31a3-8673-47ae-ad66-fa02684748cf"));
        deployResourceEntity1.setResourceId("resourceId");
        serviceStateManageRequest1.setDeployResourceEntityList(List.of(deployResourceEntity1));
        when(mockScsServersManager.stopService(serviceStateManageRequest1)).thenReturn(false);

        // Run the test
        final boolean result =
                plugin.stopService(serviceStateManageRequest);

        // Verify the results
        Assertions.assertThat(result).isFalse();
    }

    @Test
    void testStopService_ServersManagerReturnsTrue() {
        // Setup
        final ServiceStateManageRequest serviceStateManageRequest = new ServiceStateManageRequest();
        serviceStateManageRequest.setUserId("userId");
        serviceStateManageRequest.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity = new DeployResourceEntity();
        deployResourceEntity.setId(UUID.fromString("9c4f31a3-8673-47ae-ad66-fa02684748cf"));
        deployResourceEntity.setResourceId("resourceId");
        serviceStateManageRequest.setDeployResourceEntityList(List.of(deployResourceEntity));

        // Configure ServersManager.stopService(...).
        final ServiceStateManageRequest serviceStateManageRequest1 =
                new ServiceStateManageRequest();
        serviceStateManageRequest1.setUserId("userId");
        serviceStateManageRequest1.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity1 = new DeployResourceEntity();
        deployResourceEntity1.setId(UUID.fromString("9c4f31a3-8673-47ae-ad66-fa02684748cf"));
        deployResourceEntity1.setResourceId("resourceId");
        serviceStateManageRequest1.setDeployResourceEntityList(List.of(deployResourceEntity1));
        when(mockScsServersManager.stopService(serviceStateManageRequest1)).thenReturn(true);

        // Run the test
        final boolean result =
                plugin.stopService(serviceStateManageRequest);

        // Verify the results
        Assertions.assertThat(result).isTrue();
    }

    @Test
    void testRestartService() {
        // Setup
        final ServiceStateManageRequest serviceStateManageRequest = new ServiceStateManageRequest();
        serviceStateManageRequest.setUserId("userId");
        serviceStateManageRequest.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity = new DeployResourceEntity();
        deployResourceEntity.setId(UUID.fromString("9c4f31a3-8673-47ae-ad66-fa02684748cf"));
        deployResourceEntity.setResourceId("resourceId");
        serviceStateManageRequest.setDeployResourceEntityList(List.of(deployResourceEntity));

        // Configure ServersManager.restartService(...).
        final ServiceStateManageRequest serviceStateManageRequest1 =
                new ServiceStateManageRequest();
        serviceStateManageRequest1.setUserId("userId");
        serviceStateManageRequest1.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity1 = new DeployResourceEntity();
        deployResourceEntity1.setId(UUID.fromString("9c4f31a3-8673-47ae-ad66-fa02684748cf"));
        deployResourceEntity1.setResourceId("resourceId");
        serviceStateManageRequest1.setDeployResourceEntityList(List.of(deployResourceEntity1));
        when(mockScsServersManager.restartService(serviceStateManageRequest1)).thenReturn(false);

        // Run the test
        final boolean result =
                plugin.restartService(serviceStateManageRequest);

        // Verify the results
        Assertions.assertThat(result).isFalse();
    }

    @Test
    void testRestartService_ServersManagerReturnsTrue() {
        // Setup
        final ServiceStateManageRequest serviceStateManageRequest = new ServiceStateManageRequest();
        serviceStateManageRequest.setUserId("userId");
        serviceStateManageRequest.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity = new DeployResourceEntity();
        deployResourceEntity.setId(UUID.fromString("9c4f31a3-8673-47ae-ad66-fa02684748cf"));
        deployResourceEntity.setResourceId("resourceId");
        serviceStateManageRequest.setDeployResourceEntityList(List.of(deployResourceEntity));

        // Configure ServersManager.restartService(...).
        final ServiceStateManageRequest serviceStateManageRequest1 =
                new ServiceStateManageRequest();
        serviceStateManageRequest1.setUserId("userId");
        serviceStateManageRequest1.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity1 = new DeployResourceEntity();
        deployResourceEntity1.setId(UUID.fromString("9c4f31a3-8673-47ae-ad66-fa02684748cf"));
        deployResourceEntity1.setResourceId("resourceId");
        serviceStateManageRequest1.setDeployResourceEntityList(List.of(deployResourceEntity1));
        when(mockScsServersManager.restartService(serviceStateManageRequest1)).thenReturn(true);

        // Run the test
        final boolean result =
                plugin.restartService(serviceStateManageRequest);

        // Verify the results
        Assertions.assertThat(result).isTrue();
    }
}
