package org.eclipse.xpanse.plugins.flexibleengine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.resource.ServiceResourceEntity;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.monitor.Metric;
import org.eclipse.xpanse.modules.models.monitor.enums.MetricType;
import org.eclipse.xpanse.modules.models.monitor.enums.MetricUnit;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.modules.models.service.deployment.DeployResource;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.servicetemplate.CloudServiceProvider;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceTemplateReviewPluginResultType;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.UnavailableServiceRegionsException;
import org.eclipse.xpanse.modules.orchestrator.monitor.ResourceMetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ServiceMetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.servicestate.ServiceStateManageRequest;
import org.eclipse.xpanse.plugins.flexibleengine.manage.FlexibleEngineResourceManager;
import org.eclipse.xpanse.plugins.flexibleengine.manage.FlexibleEngineVmStateManager;
import org.eclipse.xpanse.plugins.flexibleengine.monitor.FlexibleEngineMetricsService;
import org.eclipse.xpanse.plugins.flexibleengine.resourcehandler.FlexibleEngineTerraformResourceHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class FlexibleEngineOrchestratorPluginTest {
    @Mock private FlexibleEngineTerraformResourceHandler mockFlexibleEngineTerraformResourceHandler;
    @Mock private FlexibleEngineMetricsService mockFlexibleEngineMetricsService;
    @Mock private FlexibleEngineVmStateManager mockFlexibleEngineVmStateManagerService;
    @Mock private FlexibleEngineResourceManager flexibleEngineResourceManager;
    @InjectMocks private FlexibleEngineOrchestratorPlugin plugin;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(plugin, "autoApproveServiceTemplateEnabled", true);
    }

    @Test
    void testGetResourceHandler() {
        assertThat(plugin.resourceHandlers().get(DeployerKind.TERRAFORM))
                .isEqualTo(mockFlexibleEngineTerraformResourceHandler);
    }

    @Test
    void testGetCsp() {
        assertThat(plugin.getCsp()).isEqualTo(Csp.FLEXIBLE_ENGINE);
    }

    @Test
    void testRequiredProperties() {
        assertThat(plugin.requiredProperties()).isEqualTo(Collections.emptyList());
    }

    @Test
    void testGetEnvVarKeysMappingMap() {
        // Setup
        // Run the test
        final Map<String, String> result = plugin.getEnvVarKeysMappingMap();

        // Verify the results
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAvailableCredentialTypes() {
        assertThat(plugin.getAvailableCredentialTypes())
                .isEqualTo(List.of(CredentialType.VARIABLES));
    }

    @Test
    void testGetCredentialDefinitions() {
        // Setup
        // Run the test
        final List<AbstractCredentialInfo> result = plugin.getCredentialDefinitions();
        // Verify the results
        assertFalse(result.isEmpty());
    }

    @Test
    void testValidateServiceTemplate() {
        Ocl ocl = Mockito.mock(Ocl.class);
        assertThat(plugin.validateServiceTemplate(ocl))
                .isEqualTo(ServiceTemplateReviewPluginResultType.APPROVED);
    }

    @Test
    void testGetSites() {
        // Setup
        List<String> exceptedSites = List.of("default");
        // Run the test
        final List<String> result = plugin.getSites();
        // Verify the results
        assertEquals(exceptedSites, result);
    }

    @Test
    void testValidateRegionsOfService() {
        // Setup
        Ocl ocl = new Ocl();
        Region region = new Region();
        region.setName("eu-west-0");
        region.setSite("default");
        region.setArea("area");
        CloudServiceProvider cloudServiceProvider = new CloudServiceProvider();
        cloudServiceProvider.setName(Csp.FLEXIBLE_ENGINE);
        cloudServiceProvider.setRegions(List.of(region));
        ocl.setCloudServiceProvider(cloudServiceProvider);
        // Run the test
        final boolean result = plugin.validateRegionsOfService(ocl);
        // Verify the results
        assertTrue(result);

        // Setup unavailable region name
        region.setName("eu-west-error");
        // Run the test
        assertThatThrownBy(() -> plugin.validateRegionsOfService(ocl))
                .isInstanceOf(UnavailableServiceRegionsException.class);

        // Setup unavailable site name
        region.setSite("error-site");
        // Run the test
        assertThatThrownBy(() -> plugin.validateRegionsOfService(ocl))
                .isInstanceOf(UnavailableServiceRegionsException.class);
    }

    @Test
    void testGetMetricsForResource() {
        // Setup
        final UUID serviceId = UUID.randomUUID();
        final DeployResource deployResource = new DeployResource();
        deployResource.setResourceId("resourceId");
        deployResource.setResourceName("name");
        deployResource.setResourceKind(DeployResourceKind.VM);
        deployResource.setProperties(Map.ofEntries(Map.entry("value", "value")));
        final ResourceMetricsRequest resourceMetricRequest =
                new ResourceMetricsRequest(
                        serviceId,
                        getRegion(),
                        deployResource,
                        MonitorResourceType.CPU,
                        0L,
                        0L,
                        0,
                        false,
                        "userId");
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
        deployResource1.setResourceName("name");
        deployResource1.setResourceKind(DeployResourceKind.VM);
        deployResource1.setProperties(Map.ofEntries(Map.entry("value", "value")));
        final ResourceMetricsRequest resourceMetricRequest1 =
                new ResourceMetricsRequest(
                        serviceId,
                        getRegion(),
                        deployResource1,
                        MonitorResourceType.CPU,
                        0L,
                        0L,
                        0,
                        false,
                        "userId");
        when(mockFlexibleEngineMetricsService.getMetricsByResource(resourceMetricRequest1))
                .thenReturn(metrics);

        // Run the test
        final List<Metric> result = plugin.getMetricsForResource(resourceMetricRequest);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetMetricsForResource_FlexibleEngineMetricsServiceReturnsNoItems() {
        // Setup
        final UUID serviceId = UUID.randomUUID();
        final DeployResource deployResource = new DeployResource();
        deployResource.setResourceId("resourceId");
        deployResource.setResourceName("name");
        deployResource.setResourceKind(DeployResourceKind.VM);
        deployResource.setProperties(Map.ofEntries(Map.entry("value", "value")));
        final ResourceMetricsRequest resourceMetricRequest =
                new ResourceMetricsRequest(
                        serviceId,
                        getRegion(),
                        deployResource,
                        MonitorResourceType.CPU,
                        0L,
                        0L,
                        0,
                        false,
                        "userId");

        // Configure FlexibleEngineMetricsService.getMetricsByResource(...).
        final DeployResource deployResource1 = new DeployResource();
        deployResource1.setResourceId("resourceId");
        deployResource1.setResourceName("name");
        deployResource1.setResourceKind(DeployResourceKind.VM);
        deployResource1.setProperties(Map.ofEntries(Map.entry("value", "value")));
        final ResourceMetricsRequest resourceMetricRequest1 =
                new ResourceMetricsRequest(
                        serviceId,
                        getRegion(),
                        deployResource1,
                        MonitorResourceType.CPU,
                        0L,
                        0L,
                        0,
                        false,
                        "userId");
        when(mockFlexibleEngineMetricsService.getMetricsByResource(resourceMetricRequest1))
                .thenReturn(Collections.emptyList());

        // Run the test
        final List<Metric> result = plugin.getMetricsForResource(resourceMetricRequest);

        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void testGetMetricsForService() {
        // Setup
        final UUID serviceId = UUID.randomUUID();
        final DeployResource deployResource = new DeployResource();
        deployResource.setResourceId("resourceId");
        deployResource.setResourceName("name");
        deployResource.setResourceKind(DeployResourceKind.VM);
        deployResource.setProperties(Map.ofEntries(Map.entry("value", "value")));
        final ServiceMetricsRequest serviceMetricRequest =
                new ServiceMetricsRequest(
                        serviceId,
                        getRegion(),
                        List.of(deployResource),
                        MonitorResourceType.CPU,
                        0L,
                        0L,
                        0,
                        false,
                        "userId");
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
        deployResource1.setResourceName("name");
        deployResource1.setResourceKind(DeployResourceKind.VM);
        deployResource1.setProperties(Map.ofEntries(Map.entry("value", "value")));
        final ServiceMetricsRequest serviceMetricRequest1 =
                new ServiceMetricsRequest(
                        serviceId,
                        getRegion(),
                        List.of(deployResource1),
                        MonitorResourceType.CPU,
                        0L,
                        0L,
                        0,
                        false,
                        "userId");
        when(mockFlexibleEngineMetricsService.getMetricsByService(serviceMetricRequest1))
                .thenReturn(metrics);

        // Run the test
        final List<Metric> result = plugin.getMetricsForService(serviceMetricRequest);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetMetricsForService_FlexibleEngineMetricsServiceReturnsNoItems() {
        // Setup
        final UUID serviceId = UUID.randomUUID();
        final DeployResource deployResource = new DeployResource();
        deployResource.setResourceId("resourceId");
        deployResource.setResourceName("name");
        deployResource.setResourceKind(DeployResourceKind.VM);
        deployResource.setProperties(Map.ofEntries(Map.entry("value", "value")));
        final ServiceMetricsRequest serviceMetricRequest =
                new ServiceMetricsRequest(
                        serviceId,
                        getRegion(),
                        List.of(deployResource),
                        MonitorResourceType.CPU,
                        0L,
                        0L,
                        0,
                        false,
                        "userId");

        // Configure FlexibleEngineMetricsService.getMetricsByService(...).
        final DeployResource deployResource1 = new DeployResource();
        deployResource1.setResourceId("resourceId");
        deployResource1.setResourceName("name");
        deployResource1.setResourceKind(DeployResourceKind.VM);
        deployResource1.setProperties(Map.ofEntries(Map.entry("value", "value")));
        final ServiceMetricsRequest serviceMetricRequest1 =
                new ServiceMetricsRequest(
                        serviceId,
                        getRegion(),
                        List.of(deployResource1),
                        MonitorResourceType.CPU,
                        0L,
                        0L,
                        0,
                        false,
                        "userId");
        when(mockFlexibleEngineMetricsService.getMetricsByService(serviceMetricRequest1))
                .thenReturn(Collections.emptyList());

        // Run the test
        final List<Metric> result = plugin.getMetricsForService(serviceMetricRequest);

        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void testStartService() {
        // Setup
        final ServiceStateManageRequest serviceStateManageRequest = new ServiceStateManageRequest();
        serviceStateManageRequest.setUserId("userId");
        serviceStateManageRequest.setRegion(getRegion());
        final ServiceResourceEntity serviceResourceEntity = new ServiceResourceEntity();
        serviceResourceEntity.setId(UUID.fromString("f305c8a2-fa75-4194-a2ad-084418311a7d"));
        serviceResourceEntity.setResourceId("resourceId");
        serviceStateManageRequest.setServiceResourceEntityList(List.of(serviceResourceEntity));

        // Configure FlexibleEngineVmStateManager.startService(...).
        final ServiceStateManageRequest serviceStateManageRequest1 =
                new ServiceStateManageRequest();
        serviceStateManageRequest1.setUserId("userId");
        serviceStateManageRequest1.setRegion(getRegion());
        final ServiceResourceEntity serviceResourceEntity1 = new ServiceResourceEntity();
        serviceResourceEntity1.setId(UUID.fromString("f305c8a2-fa75-4194-a2ad-084418311a7d"));
        serviceResourceEntity1.setResourceId("resourceId");
        serviceStateManageRequest1.setServiceResourceEntityList(List.of(serviceResourceEntity1));
        when(mockFlexibleEngineVmStateManagerService.startService(serviceStateManageRequest1))
                .thenReturn(false);

        // Run the test
        final boolean result = plugin.startService(serviceStateManageRequest);

        // Verify the results
        assertThat(result).isFalse();
    }

    @Test
    void testStartService_FlexibleEngineVmStateManagerReturnsTrue() {
        // Setup
        final ServiceStateManageRequest serviceStateManageRequest = new ServiceStateManageRequest();
        serviceStateManageRequest.setUserId("userId");
        serviceStateManageRequest.setRegion(getRegion());
        final ServiceResourceEntity serviceResourceEntity = new ServiceResourceEntity();
        serviceResourceEntity.setId(UUID.fromString("f305c8a2-fa75-4194-a2ad-084418311a7d"));
        serviceResourceEntity.setResourceId("resourceId");
        serviceStateManageRequest.setServiceResourceEntityList(List.of(serviceResourceEntity));

        // Configure FlexibleEngineVmStateManager.startService(...).
        final ServiceStateManageRequest serviceStateManageRequest1 =
                new ServiceStateManageRequest();
        serviceStateManageRequest1.setUserId("userId");
        serviceStateManageRequest1.setRegion(getRegion());
        final ServiceResourceEntity serviceResourceEntity1 = new ServiceResourceEntity();
        serviceResourceEntity1.setId(UUID.fromString("f305c8a2-fa75-4194-a2ad-084418311a7d"));
        serviceResourceEntity1.setResourceId("resourceId");
        serviceStateManageRequest1.setServiceResourceEntityList(List.of(serviceResourceEntity1));
        when(mockFlexibleEngineVmStateManagerService.startService(serviceStateManageRequest1))
                .thenReturn(true);

        // Run the test
        final boolean result = plugin.startService(serviceStateManageRequest);

        // Verify the results
        assertThat(result).isTrue();
    }

    @Test
    void testStopService() {
        // Setup
        final ServiceStateManageRequest serviceStateManageRequest = new ServiceStateManageRequest();
        serviceStateManageRequest.setUserId("userId");
        serviceStateManageRequest.setRegion(getRegion());
        final ServiceResourceEntity serviceResourceEntity = new ServiceResourceEntity();
        serviceResourceEntity.setId(UUID.fromString("f305c8a2-fa75-4194-a2ad-084418311a7d"));
        serviceResourceEntity.setResourceId("resourceId");
        serviceStateManageRequest.setServiceResourceEntityList(List.of(serviceResourceEntity));

        // Configure FlexibleEngineVmStateManager.stopService(...).
        final ServiceStateManageRequest serviceStateManageRequest1 =
                new ServiceStateManageRequest();
        serviceStateManageRequest1.setUserId("userId");
        serviceStateManageRequest1.setRegion(getRegion());
        final ServiceResourceEntity serviceResourceEntity1 = new ServiceResourceEntity();
        serviceResourceEntity1.setId(UUID.fromString("f305c8a2-fa75-4194-a2ad-084418311a7d"));
        serviceResourceEntity1.setResourceId("resourceId");
        serviceStateManageRequest1.setServiceResourceEntityList(List.of(serviceResourceEntity1));
        when(mockFlexibleEngineVmStateManagerService.stopService(serviceStateManageRequest1))
                .thenReturn(false);

        // Run the test
        final boolean result = plugin.stopService(serviceStateManageRequest);

        // Verify the results
        assertThat(result).isFalse();
    }

    @Test
    void testStopService_FlexibleEngineVmStateManagerReturnsTrue() {
        // Setup
        final ServiceStateManageRequest serviceStateManageRequest = new ServiceStateManageRequest();
        serviceStateManageRequest.setUserId("userId");
        serviceStateManageRequest.setRegion(getRegion());
        final ServiceResourceEntity serviceResourceEntity = new ServiceResourceEntity();
        serviceResourceEntity.setId(UUID.fromString("f305c8a2-fa75-4194-a2ad-084418311a7d"));
        serviceResourceEntity.setResourceId("resourceId");
        serviceStateManageRequest.setServiceResourceEntityList(List.of(serviceResourceEntity));

        // Configure FlexibleEngineVmStateManager.stopService(...).
        final ServiceStateManageRequest serviceStateManageRequest1 =
                new ServiceStateManageRequest();
        serviceStateManageRequest1.setUserId("userId");
        serviceStateManageRequest1.setRegion(getRegion());
        final ServiceResourceEntity serviceResourceEntity1 = new ServiceResourceEntity();
        serviceResourceEntity1.setId(UUID.fromString("f305c8a2-fa75-4194-a2ad-084418311a7d"));
        serviceResourceEntity1.setResourceId("resourceId");
        serviceStateManageRequest1.setServiceResourceEntityList(List.of(serviceResourceEntity1));
        when(mockFlexibleEngineVmStateManagerService.stopService(serviceStateManageRequest1))
                .thenReturn(true);

        // Run the test
        final boolean result = plugin.stopService(serviceStateManageRequest);

        // Verify the results
        assertThat(result).isTrue();
    }

    @Test
    void testRestartService() {
        // Setup
        final ServiceStateManageRequest serviceStateManageRequest = new ServiceStateManageRequest();
        serviceStateManageRequest.setUserId("userId");
        serviceStateManageRequest.setRegion(getRegion());
        final ServiceResourceEntity serviceResourceEntity = new ServiceResourceEntity();
        serviceResourceEntity.setId(UUID.fromString("f305c8a2-fa75-4194-a2ad-084418311a7d"));
        serviceResourceEntity.setResourceId("resourceId");
        serviceStateManageRequest.setServiceResourceEntityList(List.of(serviceResourceEntity));

        // Configure FlexibleEngineVmStateManager.restartService(...).
        final ServiceStateManageRequest serviceStateManageRequest1 =
                new ServiceStateManageRequest();
        serviceStateManageRequest1.setUserId("userId");
        serviceStateManageRequest1.setRegion(getRegion());
        final ServiceResourceEntity serviceResourceEntity1 = new ServiceResourceEntity();
        serviceResourceEntity1.setId(UUID.fromString("f305c8a2-fa75-4194-a2ad-084418311a7d"));
        serviceResourceEntity1.setResourceId("resourceId");
        serviceStateManageRequest1.setServiceResourceEntityList(List.of(serviceResourceEntity1));
        when(mockFlexibleEngineVmStateManagerService.restartService(serviceStateManageRequest1))
                .thenReturn(false);

        // Run the test
        final boolean result = plugin.restartService(serviceStateManageRequest);

        // Verify the results
        assertThat(result).isFalse();
    }

    @Test
    void testRestartService_FlexibleEngineVmStateManagerReturnsTrue() {
        // Setup
        final ServiceStateManageRequest serviceStateManageRequest = new ServiceStateManageRequest();
        serviceStateManageRequest.setUserId("userId");
        serviceStateManageRequest.setRegion(getRegion());
        final ServiceResourceEntity serviceResourceEntity = new ServiceResourceEntity();
        serviceResourceEntity.setId(UUID.fromString("f305c8a2-fa75-4194-a2ad-084418311a7d"));
        serviceResourceEntity.setResourceId("resourceId");
        serviceStateManageRequest.setServiceResourceEntityList(List.of(serviceResourceEntity));

        // Configure FlexibleEngineVmStateManager.restartService(...).
        final ServiceStateManageRequest serviceStateManageRequest1 =
                new ServiceStateManageRequest();
        serviceStateManageRequest1.setUserId("userId");
        serviceStateManageRequest1.setRegion(getRegion());
        final ServiceResourceEntity serviceResourceEntity1 = new ServiceResourceEntity();
        serviceResourceEntity1.setId(UUID.fromString("f305c8a2-fa75-4194-a2ad-084418311a7d"));
        serviceResourceEntity1.setResourceId("resourceId");
        serviceStateManageRequest1.setServiceResourceEntityList(List.of(serviceResourceEntity1));
        when(mockFlexibleEngineVmStateManagerService.restartService(serviceStateManageRequest1))
                .thenReturn(true);

        // Run the test
        final boolean result = plugin.restartService(serviceStateManageRequest);

        // Verify the results
        assertThat(result).isTrue();
    }

    private Region getRegion() {
        Region region = new Region();
        region.setName("eu-west-0");
        region.setSite("default");
        region.setArea("Western Europe");
        return region;
    }
}
