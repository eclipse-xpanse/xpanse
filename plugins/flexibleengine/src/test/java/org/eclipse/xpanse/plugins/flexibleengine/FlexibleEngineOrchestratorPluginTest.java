package org.eclipse.xpanse.plugins.flexibleengine;

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
import org.eclipse.xpanse.plugins.flexibleengine.manage.FlexibleEngineVmStateManager;
import org.eclipse.xpanse.plugins.flexibleengine.monitor.FlexibleEngineMetricsService;
import org.eclipse.xpanse.plugins.flexibleengine.resourcehandler.FlexibleEngineTerraformResourceHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class FlexibleEngineOrchestratorPluginTest {

    private final String terraformFlexibleEngineVersion = "1.52.1";
    @Mock
    private FlexibleEngineTerraformResourceHandler mockFlexibleEngineTerraformResourceHandler;
    @Mock
    private FlexibleEngineMetricsService mockFlexibleEngineMetricsService;
    @Mock
    private FlexibleEngineVmStateManager mockFlexibleEngineVmStateManagerService;
    @InjectMocks
    private FlexibleEngineOrchestratorPlugin flexibleEngineOrchestratorPluginUnderTest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(flexibleEngineOrchestratorPluginUnderTest,
                "terraformFlexibleEngineVersion", terraformFlexibleEngineVersion);
    }

    @Test
    void testGetResourceHandler() {
        assertThat(flexibleEngineOrchestratorPluginUnderTest.getResourceHandler()).isEqualTo(
                mockFlexibleEngineTerraformResourceHandler);
    }

    @Test
    void testGetCsp() {
        assertThat(flexibleEngineOrchestratorPluginUnderTest.getCsp()).isEqualTo(
                Csp.FLEXIBLE_ENGINE);
    }

    @Test
    void testRequiredProperties() {
        assertThat(flexibleEngineOrchestratorPluginUnderTest.requiredProperties()).isEqualTo(
                Collections.emptyList());
    }

    @Test
    void testGetAvailableCredentialTypes() {
        assertThat(
                flexibleEngineOrchestratorPluginUnderTest.getAvailableCredentialTypes()).isEqualTo(
                List.of(CredentialType.VARIABLES));
    }

    @Test
    void testGetCredentialDefinitions() {
        // Setup
        // Run the test
        final List<AbstractCredentialInfo> result =
                flexibleEngineOrchestratorPluginUnderTest.getCredentialDefinitions();

        // Verify the results
    }

    @Test
    void testGetMetricsForResource() {
        // Setup
        final UUID serviceId = UUID.randomUUID();
        final DeployResource deployResource = new DeployResource();
        deployResource.setResourceId("resourceId");
        deployResource.setName("name");
        deployResource.setKind(DeployResourceKind.VM);
        deployResource.setProperties(Map.ofEntries(Map.entry("value", "value")));
        final ResourceMetricsRequest resourceMetricRequest =
                new ResourceMetricsRequest(serviceId, deployResource, MonitorResourceType.CPU, 0L, 0L, 0,
                        false, "userId");
        final Metric metric = new Metric();
        metric.setName("name");
        metric.setDescription("description");
        metric.setType(MetricType.COUNTER);
        metric.setMonitorResourceType(MonitorResourceType.CPU);
        metric.setUnit(MetricUnit.MB);
        final List<Metric> expectedResult = List.of(metric);

        // Configure FlexibleEngineMetricsService.getMetricsByResource(...).
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
                new ResourceMetricsRequest(serviceId, deployResource1, MonitorResourceType.CPU, 0L, 0L, 0,
                        false, "userId");
        when(mockFlexibleEngineMetricsService.getMetricsByResource(
                resourceMetricRequest1)).thenReturn(metrics);

        // Run the test
        final List<Metric> result = flexibleEngineOrchestratorPluginUnderTest.getMetricsForResource(
                resourceMetricRequest);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetMetricsForResource_FlexibleEngineMetricsServiceReturnsNoItems() {
        // Setup
        final UUID serviceId = UUID.randomUUID();
        final DeployResource deployResource = new DeployResource();
        deployResource.setResourceId("resourceId");
        deployResource.setName("name");
        deployResource.setKind(DeployResourceKind.VM);
        deployResource.setProperties(Map.ofEntries(Map.entry("value", "value")));
        final ResourceMetricsRequest resourceMetricRequest =
                new ResourceMetricsRequest(serviceId, deployResource, MonitorResourceType.CPU, 0L, 0L, 0,
                        false, "userId");

        // Configure FlexibleEngineMetricsService.getMetricsByResource(...).
        final DeployResource deployResource1 = new DeployResource();
        deployResource1.setResourceId("resourceId");
        deployResource1.setName("name");
        deployResource1.setKind(DeployResourceKind.VM);
        deployResource1.setProperties(Map.ofEntries(Map.entry("value", "value")));
        final ResourceMetricsRequest resourceMetricRequest1 =
                new ResourceMetricsRequest(serviceId, deployResource1, MonitorResourceType.CPU, 0L, 0L, 0,
                        false, "userId");
        when(mockFlexibleEngineMetricsService.getMetricsByResource(
                resourceMetricRequest1)).thenReturn(Collections.emptyList());

        // Run the test
        final List<Metric> result = flexibleEngineOrchestratorPluginUnderTest.getMetricsForResource(
                resourceMetricRequest);

        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void testGetMetricsForService() {
        // Setup
        final UUID serviceId = UUID.randomUUID();
        final DeployResource deployResource = new DeployResource();
        deployResource.setResourceId("resourceId");
        deployResource.setName("name");
        deployResource.setKind(DeployResourceKind.VM);
        deployResource.setProperties(Map.ofEntries(Map.entry("value", "value")));
        final ServiceMetricsRequest serviceMetricRequest =
                new ServiceMetricsRequest(serviceId, List.of(deployResource), MonitorResourceType.CPU, 0L, 0L,
                        0, false, "userId");
        final Metric metric = new Metric();
        metric.setName("name");
        metric.setDescription("description");
        metric.setType(MetricType.COUNTER);
        metric.setMonitorResourceType(MonitorResourceType.CPU);
        metric.setUnit(MetricUnit.MB);
        final List<Metric> expectedResult = List.of(metric);

        // Configure FlexibleEngineMetricsService.getMetricsByService(...).
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
        final ServiceMetricsRequest serviceMetricRequest1 =
                new ServiceMetricsRequest(serviceId, List.of(deployResource1), MonitorResourceType.CPU, 0L, 0L,
                        0, false, "userId");
        when(mockFlexibleEngineMetricsService.getMetricsByService(
                serviceMetricRequest1)).thenReturn(metrics);

        // Run the test
        final List<Metric> result = flexibleEngineOrchestratorPluginUnderTest.getMetricsForService(
                serviceMetricRequest);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetMetricsForService_FlexibleEngineMetricsServiceReturnsNoItems() {
        // Setup
        final UUID serviceId = UUID.randomUUID();
        final DeployResource deployResource = new DeployResource();
        deployResource.setResourceId("resourceId");
        deployResource.setName("name");
        deployResource.setKind(DeployResourceKind.VM);
        deployResource.setProperties(Map.ofEntries(Map.entry("value", "value")));
        final ServiceMetricsRequest serviceMetricRequest =
                new ServiceMetricsRequest(serviceId, List.of(deployResource), MonitorResourceType.CPU, 0L, 0L,
                        0, false, "userId");

        // Configure FlexibleEngineMetricsService.getMetricsByService(...).
        final DeployResource deployResource1 = new DeployResource();
        deployResource1.setResourceId("resourceId");
        deployResource1.setName("name");
        deployResource1.setKind(DeployResourceKind.VM);
        deployResource1.setProperties(Map.ofEntries(Map.entry("value", "value")));
        final ServiceMetricsRequest serviceMetricRequest1 =
                new ServiceMetricsRequest(serviceId, List.of(deployResource1), MonitorResourceType.CPU, 0L, 0L,
                        0, false, "userId");
        when(mockFlexibleEngineMetricsService.getMetricsByService(
                serviceMetricRequest1)).thenReturn(Collections.emptyList());

        // Run the test
        final List<Metric> result = flexibleEngineOrchestratorPluginUnderTest.getMetricsForService(
                serviceMetricRequest);

        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void testStartService() {
        // Setup
        final ServiceManagerRequest serviceManagerRequest = new ServiceManagerRequest();
        serviceManagerRequest.setUserId("userId");
        serviceManagerRequest.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity = new DeployResourceEntity();
        deployResourceEntity.setId(UUID.fromString("f305c8a2-fa75-4194-a2ad-084418311a7d"));
        deployResourceEntity.setResourceId("resourceId");
        serviceManagerRequest.setDeployResourceEntityList(List.of(deployResourceEntity));

        // Configure FlexibleEngineVmStateManager.startService(...).
        final ServiceManagerRequest serviceManagerRequest1 = new ServiceManagerRequest();
        serviceManagerRequest1.setUserId("userId");
        serviceManagerRequest1.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity1 = new DeployResourceEntity();
        deployResourceEntity1.setId(UUID.fromString("f305c8a2-fa75-4194-a2ad-084418311a7d"));
        deployResourceEntity1.setResourceId("resourceId");
        serviceManagerRequest1.setDeployResourceEntityList(List.of(deployResourceEntity1));
        when(mockFlexibleEngineVmStateManagerService.startService(
                serviceManagerRequest1)).thenReturn(false);

        // Run the test
        final boolean result =
                flexibleEngineOrchestratorPluginUnderTest.startService(serviceManagerRequest);

        // Verify the results
        assertThat(result).isFalse();
    }

    @Test
    void testStartService_FlexibleEngineVmStateManagerReturnsTrue() {
        // Setup
        final ServiceManagerRequest serviceManagerRequest = new ServiceManagerRequest();
        serviceManagerRequest.setUserId("userId");
        serviceManagerRequest.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity = new DeployResourceEntity();
        deployResourceEntity.setId(UUID.fromString("f305c8a2-fa75-4194-a2ad-084418311a7d"));
        deployResourceEntity.setResourceId("resourceId");
        serviceManagerRequest.setDeployResourceEntityList(List.of(deployResourceEntity));

        // Configure FlexibleEngineVmStateManager.startService(...).
        final ServiceManagerRequest serviceManagerRequest1 = new ServiceManagerRequest();
        serviceManagerRequest1.setUserId("userId");
        serviceManagerRequest1.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity1 = new DeployResourceEntity();
        deployResourceEntity1.setId(UUID.fromString("f305c8a2-fa75-4194-a2ad-084418311a7d"));
        deployResourceEntity1.setResourceId("resourceId");
        serviceManagerRequest1.setDeployResourceEntityList(List.of(deployResourceEntity1));
        when(mockFlexibleEngineVmStateManagerService.startService(
                serviceManagerRequest1)).thenReturn(true);

        // Run the test
        final boolean result =
                flexibleEngineOrchestratorPluginUnderTest.startService(serviceManagerRequest);

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
        deployResourceEntity.setId(UUID.fromString("f305c8a2-fa75-4194-a2ad-084418311a7d"));
        deployResourceEntity.setResourceId("resourceId");
        serviceManagerRequest.setDeployResourceEntityList(List.of(deployResourceEntity));

        // Configure FlexibleEngineVmStateManager.stopService(...).
        final ServiceManagerRequest serviceManagerRequest1 = new ServiceManagerRequest();
        serviceManagerRequest1.setUserId("userId");
        serviceManagerRequest1.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity1 = new DeployResourceEntity();
        deployResourceEntity1.setId(UUID.fromString("f305c8a2-fa75-4194-a2ad-084418311a7d"));
        deployResourceEntity1.setResourceId("resourceId");
        serviceManagerRequest1.setDeployResourceEntityList(List.of(deployResourceEntity1));
        when(mockFlexibleEngineVmStateManagerService.stopService(
                serviceManagerRequest1)).thenReturn(false);

        // Run the test
        final boolean result =
                flexibleEngineOrchestratorPluginUnderTest.stopService(serviceManagerRequest);

        // Verify the results
        assertThat(result).isFalse();
    }

    @Test
    void testStopService_FlexibleEngineVmStateManagerReturnsTrue() {
        // Setup
        final ServiceManagerRequest serviceManagerRequest = new ServiceManagerRequest();
        serviceManagerRequest.setUserId("userId");
        serviceManagerRequest.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity = new DeployResourceEntity();
        deployResourceEntity.setId(UUID.fromString("f305c8a2-fa75-4194-a2ad-084418311a7d"));
        deployResourceEntity.setResourceId("resourceId");
        serviceManagerRequest.setDeployResourceEntityList(List.of(deployResourceEntity));

        // Configure FlexibleEngineVmStateManager.stopService(...).
        final ServiceManagerRequest serviceManagerRequest1 = new ServiceManagerRequest();
        serviceManagerRequest1.setUserId("userId");
        serviceManagerRequest1.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity1 = new DeployResourceEntity();
        deployResourceEntity1.setId(UUID.fromString("f305c8a2-fa75-4194-a2ad-084418311a7d"));
        deployResourceEntity1.setResourceId("resourceId");
        serviceManagerRequest1.setDeployResourceEntityList(List.of(deployResourceEntity1));
        when(mockFlexibleEngineVmStateManagerService.stopService(
                serviceManagerRequest1)).thenReturn(true);

        // Run the test
        final boolean result =
                flexibleEngineOrchestratorPluginUnderTest.stopService(serviceManagerRequest);

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
        deployResourceEntity.setId(UUID.fromString("f305c8a2-fa75-4194-a2ad-084418311a7d"));
        deployResourceEntity.setResourceId("resourceId");
        serviceManagerRequest.setDeployResourceEntityList(List.of(deployResourceEntity));

        // Configure FlexibleEngineVmStateManager.restartService(...).
        final ServiceManagerRequest serviceManagerRequest1 = new ServiceManagerRequest();
        serviceManagerRequest1.setUserId("userId");
        serviceManagerRequest1.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity1 = new DeployResourceEntity();
        deployResourceEntity1.setId(UUID.fromString("f305c8a2-fa75-4194-a2ad-084418311a7d"));
        deployResourceEntity1.setResourceId("resourceId");
        serviceManagerRequest1.setDeployResourceEntityList(List.of(deployResourceEntity1));
        when(mockFlexibleEngineVmStateManagerService.restartService(
                serviceManagerRequest1)).thenReturn(false);

        // Run the test
        final boolean result =
                flexibleEngineOrchestratorPluginUnderTest.restartService(serviceManagerRequest);

        // Verify the results
        assertThat(result).isFalse();
    }

    @Test
    void testRestartService_FlexibleEngineVmStateManagerReturnsTrue() {
        // Setup
        final ServiceManagerRequest serviceManagerRequest = new ServiceManagerRequest();
        serviceManagerRequest.setUserId("userId");
        serviceManagerRequest.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity = new DeployResourceEntity();
        deployResourceEntity.setId(UUID.fromString("f305c8a2-fa75-4194-a2ad-084418311a7d"));
        deployResourceEntity.setResourceId("resourceId");
        serviceManagerRequest.setDeployResourceEntityList(List.of(deployResourceEntity));

        // Configure FlexibleEngineVmStateManager.restartService(...).
        final ServiceManagerRequest serviceManagerRequest1 = new ServiceManagerRequest();
        serviceManagerRequest1.setUserId("userId");
        serviceManagerRequest1.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity1 = new DeployResourceEntity();
        deployResourceEntity1.setId(UUID.fromString("f305c8a2-fa75-4194-a2ad-084418311a7d"));
        deployResourceEntity1.setResourceId("resourceId");
        serviceManagerRequest1.setDeployResourceEntityList(List.of(deployResourceEntity1));
        when(mockFlexibleEngineVmStateManagerService.restartService(
                serviceManagerRequest1)).thenReturn(true);

        // Run the test
        final boolean result =
                flexibleEngineOrchestratorPluginUnderTest.restartService(serviceManagerRequest);

        // Verify the results
        assertThat(result).isTrue();
    }

    @Test
    void testGetProvider() {
        String regionName = "region";
        String result = String.format("""
                terraform {
                  required_providers {
                    flexibleengine = {
                      source  = "FlexibleEngineCloud/flexibleengine"
                      version = "%s"
                    }
                  }
                }
                            
                provider "flexibleengine" {
                  region = "%s"
                }
                """, terraformFlexibleEngineVersion, regionName);
        assertThat(flexibleEngineOrchestratorPluginUnderTest.getProvider(regionName)).isEqualTo(
                result);
    }
}
