package org.eclipse.xpanse.plugins.openstack;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.resource.DeployResourceEntity;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.monitor.Metric;
import org.eclipse.xpanse.modules.models.monitor.enums.MetricType;
import org.eclipse.xpanse.modules.models.monitor.enums.MetricUnit;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.orchestrator.manage.ServiceManagerRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ResourceMetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ServiceMetricsRequest;
import org.eclipse.xpanse.plugins.openstack.manage.ServersManager;
import org.eclipse.xpanse.plugins.openstack.monitor.MetricsManager;
import org.eclipse.xpanse.plugins.openstack.resourcehandler.OpenstackTerraformResourceHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OpenstackOrchestratorPluginTest {
    private final String terraformOpenStackVersion = "1.52.1";
    @Mock
    private OpenstackTerraformResourceHandler mockOpenstackTerraformResourceHandler;
    @Mock
    private MetricsManager mockMetricsManager;
    @Mock
    private ServersManager mockServersManager;

    @InjectMocks
    private OpenstackOrchestratorPlugin openstackOrchestratorPluginUnderTest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(openstackOrchestratorPluginUnderTest,
                "terraformOpenStackVersion", terraformOpenStackVersion);
    }

    @Test
    void testGetResourceHandler() {
        assertThat(openstackOrchestratorPluginUnderTest.getResourceHandler())
                .isEqualTo(mockOpenstackTerraformResourceHandler);
    }

    @Test
    void testGetCsp() {
        assertThat(openstackOrchestratorPluginUnderTest.getCsp()).isEqualTo(Csp.OPENSTACK);
    }

    @Test
    void testRequiredProperties() {
        assertThat(openstackOrchestratorPluginUnderTest.requiredProperties())
                .isEqualTo(Collections.emptyList());
    }

    @Test
    void testGetAvailableCredentialTypes() {
        assertThat(openstackOrchestratorPluginUnderTest.getAvailableCredentialTypes())
                .isEqualTo(List.of(CredentialType.VARIABLES));
    }

    @Test
    void testGetCredentialDefinitions() {
        // Setup
        // Run the test
        final List<AbstractCredentialInfo> result =
                openstackOrchestratorPluginUnderTest.getCredentialDefinitions();

        // Verify the results
    }

    @Test
    void testGetMetricsForResource() {
        // Setup
        final DeployResource deployResource = new DeployResource();
        deployResource.setResourceId("resourceId");
        deployResource.setName("name");
        deployResource.setKind(DeployResourceKind.VM);
        deployResource.setProperties(Map.ofEntries(Map.entry("value", "value")));
        final ResourceMetricsRequest resourceMetricRequest =
                new ResourceMetricsRequest(deployResource, MonitorResourceType.CPU, 0L, 0L, 0,
                        false, "userId");
        final Metric metric = new Metric();
        metric.setName("name");
        metric.setDescription("description");
        metric.setType(MetricType.COUNTER);
        metric.setMonitorResourceType(MonitorResourceType.CPU);
        metric.setUnit(MetricUnit.MB);
        final List<Metric> expectedResult = List.of(metric);

        // Configure MetricsManager.getMetrics(...).
        final Metric metric1 = new Metric();
        metric1.setName("name");
        metric1.setDescription("description");
        metric1.setType(MetricType.COUNTER);
        metric1.setMonitorResourceType(MonitorResourceType.CPU);
        metric1.setUnit(MetricUnit.MB);
        final List<Metric> metrics = List.of(metric1);
        final DeployResource deployResource1 = new DeployResource();
        deployResource1.setResourceId("resourceId");
        deployResource1.setName("name");
        deployResource1.setKind(DeployResourceKind.VM);
        deployResource1.setProperties(Map.ofEntries(Map.entry("value", "value")));
        final ResourceMetricsRequest resourceMetricRequest1 =
                new ResourceMetricsRequest(deployResource1, MonitorResourceType.CPU, 0L, 0L, 0,
                        false, "userId");
        when(mockMetricsManager.getMetrics(resourceMetricRequest1)).thenReturn(metrics);

        // Run the test
        final List<Metric> result =
                openstackOrchestratorPluginUnderTest.getMetricsForResource(resourceMetricRequest);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetMetricsForResource_MetricsManagerReturnsNoItems() {
        // Setup
        final DeployResource deployResource = new DeployResource();
        deployResource.setResourceId("resourceId");
        deployResource.setName("name");
        deployResource.setKind(DeployResourceKind.VM);
        deployResource.setProperties(Map.ofEntries(Map.entry("value", "value")));
        final ResourceMetricsRequest resourceMetricRequest =
                new ResourceMetricsRequest(deployResource, MonitorResourceType.CPU, 0L, 0L, 0,
                        false, "userId");

        // Configure MetricsManager.getMetrics(...).
        final DeployResource deployResource1 = new DeployResource();
        deployResource1.setResourceId("resourceId");
        deployResource1.setName("name");
        deployResource1.setKind(DeployResourceKind.VM);
        deployResource1.setProperties(Map.ofEntries(Map.entry("value", "value")));
        final ResourceMetricsRequest resourceMetricRequest1 =
                new ResourceMetricsRequest(deployResource1, MonitorResourceType.CPU, 0L, 0L, 0,
                        false, "userId");
        when(mockMetricsManager.getMetrics(resourceMetricRequest1))
                .thenReturn(Collections.emptyList());

        // Run the test
        final List<Metric> result =
                openstackOrchestratorPluginUnderTest.getMetricsForResource(resourceMetricRequest);

        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void testGetMetricsForService() {
        // Setup
        final DeployResource deployResource = new DeployResource();
        deployResource.setResourceId("resourceId");
        deployResource.setName("name");
        deployResource.setKind(DeployResourceKind.VM);
        deployResource.setProperties(Map.ofEntries(Map.entry("value", "value")));
        final ServiceMetricsRequest serviceMetricRequest =
                new ServiceMetricsRequest(List.of(deployResource), MonitorResourceType.CPU, 0L, 0L,
                        0, false, "userId");
        final Metric metric = new Metric();
        metric.setName("name");
        metric.setDescription("description");
        metric.setType(MetricType.COUNTER);
        metric.setMonitorResourceType(MonitorResourceType.CPU);
        metric.setUnit(MetricUnit.MB);
        final List<Metric> expectedResult = List.of(metric);

        // Configure MetricsManager.getMetrics(...).
        final Metric metric1 = new Metric();
        metric1.setName("name");
        metric1.setDescription("description");
        metric1.setType(MetricType.COUNTER);
        metric1.setMonitorResourceType(MonitorResourceType.CPU);
        metric1.setUnit(MetricUnit.MB);
        final List<Metric> metrics = List.of(metric1);
        final DeployResource deployResource1 = new DeployResource();
        deployResource1.setResourceId("resourceId");
        deployResource1.setName("name");
        deployResource1.setKind(DeployResourceKind.VM);
        deployResource1.setProperties(Map.ofEntries(Map.entry("value", "value")));
        final ResourceMetricsRequest resourceMetricRequest =
                new ResourceMetricsRequest(deployResource1, MonitorResourceType.CPU, 0L, 0L, 0,
                        false, "userId");
        when(mockMetricsManager.getMetrics(resourceMetricRequest)).thenReturn(metrics);

        // Run the test
        final List<Metric> result =
                openstackOrchestratorPluginUnderTest.getMetricsForService(serviceMetricRequest);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetMetricsForService_MetricsManagerReturnsNoItems() {
        // Setup
        final DeployResource deployResource = new DeployResource();
        deployResource.setResourceId("resourceId");
        deployResource.setName("name");
        deployResource.setKind(DeployResourceKind.VM);
        deployResource.setProperties(Map.ofEntries(Map.entry("value", "value")));
        final ServiceMetricsRequest serviceMetricRequest =
                new ServiceMetricsRequest(List.of(deployResource), MonitorResourceType.CPU, 0L, 0L,
                        0, false, "userId");

        // Configure MetricsManager.getMetrics(...).
        final DeployResource deployResource1 = new DeployResource();
        deployResource1.setResourceId("resourceId");
        deployResource1.setName("name");
        deployResource1.setKind(DeployResourceKind.VM);
        deployResource1.setProperties(Map.ofEntries(Map.entry("value", "value")));
        final ResourceMetricsRequest resourceMetricRequest =
                new ResourceMetricsRequest(deployResource1, MonitorResourceType.CPU, 0L, 0L, 0,
                        false, "userId");
        when(mockMetricsManager.getMetrics(resourceMetricRequest))
                .thenReturn(Collections.emptyList());

        // Run the test
        final List<Metric> result =
                openstackOrchestratorPluginUnderTest.getMetricsForService(serviceMetricRequest);

        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
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
                """, terraformOpenStackVersion, region);
        assertThat(openstackOrchestratorPluginUnderTest.getProvider("region")).isEqualTo(result);
    }

    @Test
    void testStartService() {
        // Setup
        final ServiceManagerRequest serviceManagerRequest = new ServiceManagerRequest();
        serviceManagerRequest.setUserId("userId");
        serviceManagerRequest.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity = new DeployResourceEntity();
        deployResourceEntity.setId(UUID.fromString("9c4f31a3-8673-47ae-ad66-fa02684748cf"));
        deployResourceEntity.setResourceId("resourceId");
        serviceManagerRequest.setDeployResourceEntityList(List.of(deployResourceEntity));

        // Configure ServersManager.startService(...).
        final ServiceManagerRequest serviceManagerRequest1 = new ServiceManagerRequest();
        serviceManagerRequest1.setUserId("userId");
        serviceManagerRequest1.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity1 = new DeployResourceEntity();
        deployResourceEntity1.setId(UUID.fromString("9c4f31a3-8673-47ae-ad66-fa02684748cf"));
        deployResourceEntity1.setResourceId("resourceId");
        serviceManagerRequest1.setDeployResourceEntityList(List.of(deployResourceEntity1));
        when(mockServersManager.startService(serviceManagerRequest1)).thenReturn(false);

        // Run the test
        final boolean result =
                openstackOrchestratorPluginUnderTest.startService(serviceManagerRequest);

        // Verify the results
        assertThat(result).isFalse();
    }

    @Test
    void testStartService_ServersManagerReturnsTrue() {
        // Setup
        final ServiceManagerRequest serviceManagerRequest = new ServiceManagerRequest();
        serviceManagerRequest.setUserId("userId");
        serviceManagerRequest.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity = new DeployResourceEntity();
        deployResourceEntity.setId(UUID.fromString("9c4f31a3-8673-47ae-ad66-fa02684748cf"));
        deployResourceEntity.setResourceId("resourceId");
        serviceManagerRequest.setDeployResourceEntityList(List.of(deployResourceEntity));

        // Configure ServersManager.startService(...).
        final ServiceManagerRequest serviceManagerRequest1 = new ServiceManagerRequest();
        serviceManagerRequest1.setUserId("userId");
        serviceManagerRequest1.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity1 = new DeployResourceEntity();
        deployResourceEntity1.setId(UUID.fromString("9c4f31a3-8673-47ae-ad66-fa02684748cf"));
        deployResourceEntity1.setResourceId("resourceId");
        serviceManagerRequest1.setDeployResourceEntityList(List.of(deployResourceEntity1));
        when(mockServersManager.startService(serviceManagerRequest1)).thenReturn(true);

        // Run the test
        final boolean result =
                openstackOrchestratorPluginUnderTest.startService(serviceManagerRequest);

        // Verify the results
        assertThat(result).isTrue();
    }

    @Test
    void testStopService() {
        // Setup
        final ServiceManagerRequest serviceManagerRequest = new ServiceManagerRequest();
        serviceManagerRequest.setUserId("userId");
        serviceManagerRequest.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity = new DeployResourceEntity();
        deployResourceEntity.setId(UUID.fromString("9c4f31a3-8673-47ae-ad66-fa02684748cf"));
        deployResourceEntity.setResourceId("resourceId");
        serviceManagerRequest.setDeployResourceEntityList(List.of(deployResourceEntity));

        // Configure ServersManager.stopService(...).
        final ServiceManagerRequest serviceManagerRequest1 = new ServiceManagerRequest();
        serviceManagerRequest1.setUserId("userId");
        serviceManagerRequest1.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity1 = new DeployResourceEntity();
        deployResourceEntity1.setId(UUID.fromString("9c4f31a3-8673-47ae-ad66-fa02684748cf"));
        deployResourceEntity1.setResourceId("resourceId");
        serviceManagerRequest1.setDeployResourceEntityList(List.of(deployResourceEntity1));
        when(mockServersManager.stopService(serviceManagerRequest1)).thenReturn(false);

        // Run the test
        final boolean result =
                openstackOrchestratorPluginUnderTest.stopService(serviceManagerRequest);

        // Verify the results
        assertThat(result).isFalse();
    }

    @Test
    void testStopService_ServersManagerReturnsTrue() {
        // Setup
        final ServiceManagerRequest serviceManagerRequest = new ServiceManagerRequest();
        serviceManagerRequest.setUserId("userId");
        serviceManagerRequest.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity = new DeployResourceEntity();
        deployResourceEntity.setId(UUID.fromString("9c4f31a3-8673-47ae-ad66-fa02684748cf"));
        deployResourceEntity.setResourceId("resourceId");
        serviceManagerRequest.setDeployResourceEntityList(List.of(deployResourceEntity));

        // Configure ServersManager.stopService(...).
        final ServiceManagerRequest serviceManagerRequest1 = new ServiceManagerRequest();
        serviceManagerRequest1.setUserId("userId");
        serviceManagerRequest1.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity1 = new DeployResourceEntity();
        deployResourceEntity1.setId(UUID.fromString("9c4f31a3-8673-47ae-ad66-fa02684748cf"));
        deployResourceEntity1.setResourceId("resourceId");
        serviceManagerRequest1.setDeployResourceEntityList(List.of(deployResourceEntity1));
        when(mockServersManager.stopService(serviceManagerRequest1)).thenReturn(true);

        // Run the test
        final boolean result =
                openstackOrchestratorPluginUnderTest.stopService(serviceManagerRequest);

        // Verify the results
        assertThat(result).isTrue();
    }

    @Test
    void testRestartService() {
        // Setup
        final ServiceManagerRequest serviceManagerRequest = new ServiceManagerRequest();
        serviceManagerRequest.setUserId("userId");
        serviceManagerRequest.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity = new DeployResourceEntity();
        deployResourceEntity.setId(UUID.fromString("9c4f31a3-8673-47ae-ad66-fa02684748cf"));
        deployResourceEntity.setResourceId("resourceId");
        serviceManagerRequest.setDeployResourceEntityList(List.of(deployResourceEntity));

        // Configure ServersManager.restartService(...).
        final ServiceManagerRequest serviceManagerRequest1 = new ServiceManagerRequest();
        serviceManagerRequest1.setUserId("userId");
        serviceManagerRequest1.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity1 = new DeployResourceEntity();
        deployResourceEntity1.setId(UUID.fromString("9c4f31a3-8673-47ae-ad66-fa02684748cf"));
        deployResourceEntity1.setResourceId("resourceId");
        serviceManagerRequest1.setDeployResourceEntityList(List.of(deployResourceEntity1));
        when(mockServersManager.restartService(serviceManagerRequest1)).thenReturn(false);

        // Run the test
        final boolean result =
                openstackOrchestratorPluginUnderTest.restartService(serviceManagerRequest);

        // Verify the results
        assertThat(result).isFalse();
    }

    @Test
    void testRestartService_ServersManagerReturnsTrue() {
        // Setup
        final ServiceManagerRequest serviceManagerRequest = new ServiceManagerRequest();
        serviceManagerRequest.setUserId("userId");
        serviceManagerRequest.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity = new DeployResourceEntity();
        deployResourceEntity.setId(UUID.fromString("9c4f31a3-8673-47ae-ad66-fa02684748cf"));
        deployResourceEntity.setResourceId("resourceId");
        serviceManagerRequest.setDeployResourceEntityList(List.of(deployResourceEntity));

        // Configure ServersManager.restartService(...).
        final ServiceManagerRequest serviceManagerRequest1 = new ServiceManagerRequest();
        serviceManagerRequest1.setUserId("userId");
        serviceManagerRequest1.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity1 = new DeployResourceEntity();
        deployResourceEntity1.setId(UUID.fromString("9c4f31a3-8673-47ae-ad66-fa02684748cf"));
        deployResourceEntity1.setResourceId("resourceId");
        serviceManagerRequest1.setDeployResourceEntityList(List.of(deployResourceEntity1));
        when(mockServersManager.restartService(serviceManagerRequest1)).thenReturn(true);

        // Run the test
        final boolean result =
                openstackOrchestratorPluginUnderTest.restartService(serviceManagerRequest);

        // Verify the results
        assertThat(result).isTrue();
    }
}
