package org.eclipse.xpanse.plugins.huaweicloud;

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
import org.eclipse.xpanse.plugins.huaweicloud.manage.HuaweiCloudVmStateManager;
import org.eclipse.xpanse.plugins.huaweicloud.monitor.HuaweiCloudMetricsService;
import org.eclipse.xpanse.plugins.huaweicloud.resourcehandler.HuaweiCloudTerraformResourceHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class HuaweiCloudOrchestratorPluginTest {

    private final String terraformHuaweiCloudVersion = "1.52.1";
    @Mock
    private HuaweiCloudMetricsService mockHuaweiCloudMetricsService;
    @Mock
    private HuaweiCloudVmStateManager mockHuaweiCloudVmStateManager;
    @Mock
    private HuaweiCloudTerraformResourceHandler mockHuaweiCloudTerraformResourceHandler;

    @InjectMocks
    private HuaweiCloudOrchestratorPlugin huaweiCloudOrchestratorPluginUnderTest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(huaweiCloudOrchestratorPluginUnderTest,
                "terraformHuaweiCloudVersion", terraformHuaweiCloudVersion);
    }

    @Test
    void testGetResourceHandler() {
        assertThat(huaweiCloudOrchestratorPluginUnderTest.getResourceHandler())
                .isEqualTo(mockHuaweiCloudTerraformResourceHandler);
    }

    @Test
    void testGetCsp() {
        assertThat(huaweiCloudOrchestratorPluginUnderTest.getCsp()).isEqualTo(Csp.HUAWEI);
    }

    @Test
    void testRequiredProperties() {
        assertThat(huaweiCloudOrchestratorPluginUnderTest.requiredProperties())
                .isEqualTo(Collections.emptyList());
    }

    @Test
    void testGetAvailableCredentialTypes() {
        assertThat(huaweiCloudOrchestratorPluginUnderTest.getAvailableCredentialTypes())
                .isEqualTo(List.of(CredentialType.VARIABLES));
    }

    @Test
    void testGetCredentialDefinitions() {
        // Setup
        // Run the test
        final List<AbstractCredentialInfo> result =
                huaweiCloudOrchestratorPluginUnderTest.getCredentialDefinitions();

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

        // Configure HuaweiCloudMetricsService.getMetricsByResource(...).
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
        when(mockHuaweiCloudMetricsService.getMetricsByResource(resourceMetricRequest1))
                .thenReturn(metrics);

        // Run the test
        final List<Metric> result =
                huaweiCloudOrchestratorPluginUnderTest.getMetricsForResource(resourceMetricRequest);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetMetricsForResource_HuaweiCloudMetricsServiceReturnsNoItems() {
        // Setup
        final DeployResource deployResource = new DeployResource();
        deployResource.setResourceId("resourceId");
        deployResource.setName("name");
        deployResource.setKind(DeployResourceKind.VM);
        deployResource.setProperties(Map.ofEntries(Map.entry("value", "value")));
        final ResourceMetricsRequest resourceMetricRequest =
                new ResourceMetricsRequest(deployResource, MonitorResourceType.CPU, 0L, 0L, 0,
                        false, "userId");

        // Configure HuaweiCloudMetricsService.getMetricsByResource(...).
        final DeployResource deployResource1 = new DeployResource();
        deployResource1.setResourceId("resourceId");
        deployResource1.setName("name");
        deployResource1.setKind(DeployResourceKind.VM);
        deployResource1.setProperties(Map.ofEntries(Map.entry("value", "value")));
        final ResourceMetricsRequest resourceMetricRequest1 =
                new ResourceMetricsRequest(deployResource1, MonitorResourceType.CPU, 0L, 0L, 0,
                        false, "userId");
        when(mockHuaweiCloudMetricsService.getMetricsByResource(resourceMetricRequest1))
                .thenReturn(Collections.emptyList());

        // Run the test
        final List<Metric> result =
                huaweiCloudOrchestratorPluginUnderTest.getMetricsForResource(resourceMetricRequest);

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

        // Configure HuaweiCloudMetricsService.getMetricsByService(...).
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
                new ServiceMetricsRequest(List.of(deployResource1), MonitorResourceType.CPU, 0L, 0L,
                        0, false, "userId");
        when(mockHuaweiCloudMetricsService.getMetricsByService(serviceMetricRequest1))
                .thenReturn(metrics);

        // Run the test
        final List<Metric> result =
                huaweiCloudOrchestratorPluginUnderTest.getMetricsForService(serviceMetricRequest);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetMetricsForService_HuaweiCloudMetricsServiceReturnsNoItems() {
        // Setup
        final DeployResource deployResource = new DeployResource();
        deployResource.setResourceId("resourceId");
        deployResource.setName("name");
        deployResource.setKind(DeployResourceKind.VM);
        deployResource.setProperties(Map.ofEntries(Map.entry("value", "value")));
        final ServiceMetricsRequest serviceMetricRequest =
                new ServiceMetricsRequest(List.of(deployResource), MonitorResourceType.CPU, 0L, 0L,
                        0, false, "userId");

        // Configure HuaweiCloudMetricsService.getMetricsByService(...).
        final DeployResource deployResource1 = new DeployResource();
        deployResource1.setResourceId("resourceId");
        deployResource1.setName("name");
        deployResource1.setKind(DeployResourceKind.VM);
        deployResource1.setProperties(Map.ofEntries(Map.entry("value", "value")));
        final ServiceMetricsRequest serviceMetricRequest1 =
                new ServiceMetricsRequest(List.of(deployResource1), MonitorResourceType.CPU, 0L, 0L,
                        0, false, "userId");
        when(mockHuaweiCloudMetricsService.getMetricsByService(serviceMetricRequest1))
                .thenReturn(Collections.emptyList());

        // Run the test
        final List<Metric> result =
                huaweiCloudOrchestratorPluginUnderTest.getMetricsForService(serviceMetricRequest);

        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void testGetProvider() {
        String regionName = "region";
        String result = String.format("""
                terraform {
                  required_providers {
                    huaweicloud = {
                      source = "huaweicloud/huaweicloud"
                      version = "%s"
                    }
                  }
                }
                            
                provider "huaweicloud" {
                  region = "%s"
                }
                """, terraformHuaweiCloudVersion, regionName);
        assertThat(huaweiCloudOrchestratorPluginUnderTest.getProvider(regionName))
                .isEqualTo(result);
    }

    @Test
    void testStartService() {
        // Setup
        final ServiceManagerRequest serviceManagerRequest = new ServiceManagerRequest();
        serviceManagerRequest.setUserId("userId");
        serviceManagerRequest.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity = new DeployResourceEntity();
        deployResourceEntity.setId(UUID.fromString("06e10d9a-d32c-432e-94a2-d4bf4af17ee6"));
        deployResourceEntity.setResourceId("resourceId");
        serviceManagerRequest.setDeployResourceEntityList(List.of(deployResourceEntity));

        // Configure HuaweiCloudVmStateManager.startService(...).
        final ServiceManagerRequest serviceManagerRequest1 = new ServiceManagerRequest();
        serviceManagerRequest1.setUserId("userId");
        serviceManagerRequest1.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity1 = new DeployResourceEntity();
        deployResourceEntity1.setId(UUID.fromString("06e10d9a-d32c-432e-94a2-d4bf4af17ee6"));
        deployResourceEntity1.setResourceId("resourceId");
        serviceManagerRequest1.setDeployResourceEntityList(List.of(deployResourceEntity1));
        when(mockHuaweiCloudVmStateManager.startService(serviceManagerRequest1)).thenReturn(false);

        // Run the test
        final boolean result =
                huaweiCloudOrchestratorPluginUnderTest.startService(serviceManagerRequest);

        // Verify the results
        assertThat(result).isFalse();
    }

    @Test
    void testStartService_HuaweiCloudVmStateManagerReturnsTrue() {
        // Setup
        final ServiceManagerRequest serviceManagerRequest = new ServiceManagerRequest();
        serviceManagerRequest.setUserId("userId");
        serviceManagerRequest.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity = new DeployResourceEntity();
        deployResourceEntity.setId(UUID.fromString("06e10d9a-d32c-432e-94a2-d4bf4af17ee6"));
        deployResourceEntity.setResourceId("resourceId");
        serviceManagerRequest.setDeployResourceEntityList(List.of(deployResourceEntity));

        // Configure HuaweiCloudVmStateManager.startService(...).
        final ServiceManagerRequest serviceManagerRequest1 = new ServiceManagerRequest();
        serviceManagerRequest1.setUserId("userId");
        serviceManagerRequest1.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity1 = new DeployResourceEntity();
        deployResourceEntity1.setId(UUID.fromString("06e10d9a-d32c-432e-94a2-d4bf4af17ee6"));
        deployResourceEntity1.setResourceId("resourceId");
        serviceManagerRequest1.setDeployResourceEntityList(List.of(deployResourceEntity1));
        when(mockHuaweiCloudVmStateManager.startService(serviceManagerRequest1)).thenReturn(true);

        // Run the test
        final boolean result =
                huaweiCloudOrchestratorPluginUnderTest.startService(serviceManagerRequest);

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
        deployResourceEntity.setId(UUID.fromString("06e10d9a-d32c-432e-94a2-d4bf4af17ee6"));
        deployResourceEntity.setResourceId("resourceId");
        serviceManagerRequest.setDeployResourceEntityList(List.of(deployResourceEntity));

        // Configure HuaweiCloudVmStateManager.stopService(...).
        final ServiceManagerRequest serviceManagerRequest1 = new ServiceManagerRequest();
        serviceManagerRequest1.setUserId("userId");
        serviceManagerRequest1.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity1 = new DeployResourceEntity();
        deployResourceEntity1.setId(UUID.fromString("06e10d9a-d32c-432e-94a2-d4bf4af17ee6"));
        deployResourceEntity1.setResourceId("resourceId");
        serviceManagerRequest1.setDeployResourceEntityList(List.of(deployResourceEntity1));
        when(mockHuaweiCloudVmStateManager.stopService(serviceManagerRequest1)).thenReturn(false);

        // Run the test
        final boolean result =
                huaweiCloudOrchestratorPluginUnderTest.stopService(serviceManagerRequest);

        // Verify the results
        assertThat(result).isFalse();
    }

    @Test
    void testStopService_HuaweiCloudVmStateManagerReturnsTrue() {
        // Setup
        final ServiceManagerRequest serviceManagerRequest = new ServiceManagerRequest();
        serviceManagerRequest.setUserId("userId");
        serviceManagerRequest.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity = new DeployResourceEntity();
        deployResourceEntity.setId(UUID.fromString("06e10d9a-d32c-432e-94a2-d4bf4af17ee6"));
        deployResourceEntity.setResourceId("resourceId");
        serviceManagerRequest.setDeployResourceEntityList(List.of(deployResourceEntity));

        // Configure HuaweiCloudVmStateManager.stopService(...).
        final ServiceManagerRequest serviceManagerRequest1 = new ServiceManagerRequest();
        serviceManagerRequest1.setUserId("userId");
        serviceManagerRequest1.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity1 = new DeployResourceEntity();
        deployResourceEntity1.setId(UUID.fromString("06e10d9a-d32c-432e-94a2-d4bf4af17ee6"));
        deployResourceEntity1.setResourceId("resourceId");
        serviceManagerRequest1.setDeployResourceEntityList(List.of(deployResourceEntity1));
        when(mockHuaweiCloudVmStateManager.stopService(serviceManagerRequest1)).thenReturn(true);

        // Run the test
        final boolean result =
                huaweiCloudOrchestratorPluginUnderTest.stopService(serviceManagerRequest);

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
        deployResourceEntity.setId(UUID.fromString("06e10d9a-d32c-432e-94a2-d4bf4af17ee6"));
        deployResourceEntity.setResourceId("resourceId");
        serviceManagerRequest.setDeployResourceEntityList(List.of(deployResourceEntity));

        // Configure HuaweiCloudVmStateManager.restartService(...).
        final ServiceManagerRequest serviceManagerRequest1 = new ServiceManagerRequest();
        serviceManagerRequest1.setUserId("userId");
        serviceManagerRequest1.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity1 = new DeployResourceEntity();
        deployResourceEntity1.setId(UUID.fromString("06e10d9a-d32c-432e-94a2-d4bf4af17ee6"));
        deployResourceEntity1.setResourceId("resourceId");
        serviceManagerRequest1.setDeployResourceEntityList(List.of(deployResourceEntity1));
        when(mockHuaweiCloudVmStateManager.restartService(serviceManagerRequest1))
                .thenReturn(false);

        // Run the test
        final boolean result =
                huaweiCloudOrchestratorPluginUnderTest.restartService(serviceManagerRequest);

        // Verify the results
        assertThat(result).isFalse();
    }

    @Test
    void testRestartService_HuaweiCloudVmStateManagerReturnsTrue() {
        // Setup
        final ServiceManagerRequest serviceManagerRequest = new ServiceManagerRequest();
        serviceManagerRequest.setUserId("userId");
        serviceManagerRequest.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity = new DeployResourceEntity();
        deployResourceEntity.setId(UUID.fromString("06e10d9a-d32c-432e-94a2-d4bf4af17ee6"));
        deployResourceEntity.setResourceId("resourceId");
        serviceManagerRequest.setDeployResourceEntityList(List.of(deployResourceEntity));

        // Configure HuaweiCloudVmStateManager.restartService(...).
        final ServiceManagerRequest serviceManagerRequest1 = new ServiceManagerRequest();
        serviceManagerRequest1.setUserId("userId");
        serviceManagerRequest1.setRegionName("regionName");
        final DeployResourceEntity deployResourceEntity1 = new DeployResourceEntity();
        deployResourceEntity1.setId(UUID.fromString("06e10d9a-d32c-432e-94a2-d4bf4af17ee6"));
        deployResourceEntity1.setResourceId("resourceId");
        serviceManagerRequest1.setDeployResourceEntityList(List.of(deployResourceEntity1));
        when(mockHuaweiCloudVmStateManager.restartService(serviceManagerRequest1)).thenReturn(true);

        // Run the test
        final boolean result =
                huaweiCloudOrchestratorPluginUnderTest.restartService(serviceManagerRequest);

        // Verify the results
        assertThat(result).isTrue();
    }
}
