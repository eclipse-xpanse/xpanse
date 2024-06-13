package org.eclipse.xpanse.modules.monitor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.resource.DeployResourceEntity;
import org.eclipse.xpanse.modules.database.resource.DeployResourceStorage;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceStorage;
import org.eclipse.xpanse.modules.models.billing.FlavorPriceResult;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.monitor.Metric;
import org.eclipse.xpanse.modules.models.monitor.enums.MetricType;
import org.eclipse.xpanse.modules.models.monitor.enums.MetricUnit;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.modules.models.monitor.exceptions.ResourceNotFoundException;
import org.eclipse.xpanse.modules.models.monitor.exceptions.ResourceNotSupportedForMonitoringException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.audit.AuditLog;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResourceHandler;
import org.eclipse.xpanse.modules.orchestrator.monitor.ResourceMetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ServiceMetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.price.ServicePriceRequest;
import org.eclipse.xpanse.modules.orchestrator.servicestate.ServiceStateManageRequest;
import org.eclipse.xpanse.modules.security.UserServiceHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Slf4j
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ServiceMetricsAdapter.class, PluginManager.class,
        DeployResourceStorage.class, DeployServiceStorage.class, UserServiceHelper.class})
class ServiceMetricsAdapterTest {

    private final String resourceId = "8d1495ae-8420-4172-93c1-746c09b4a005";
    private final String serviceId = "23cc529b-64d9-4875-a2f0-08b415705964";
    private final String userId = "defaultUserId";
    @MockBean
    DeployServiceStorage mockDeployServiceStorage;
    @MockBean
    DeployResourceStorage mockDeployResourceStorage;
    @MockBean
    private PluginManager mockPluginManager;
    @MockBean
    private OrchestratorPlugin orchestratorPlugin;
    @MockBean
    private UserServiceHelper userServiceHelper;
    @Autowired
    private ServiceMetricsAdapter serviceMetricsAdapterUnderTest;

    @Test
    void testGetMetricsByServiceId() {
        // Setup
        final Metric metric = new Metric();
        metric.setName("name");
        metric.setDescription("description");
        metric.setType(MetricType.COUNTER);
        metric.setMonitorResourceType(MonitorResourceType.CPU);
        metric.setUnit(MetricUnit.MB);
        final List<Metric> expectedResult = List.of(metric);

        // Configure DeployServiceStorage.findDeployServiceById(...).
        final DeployServiceEntity deployServiceEntity = new DeployServiceEntity();
        deployServiceEntity.setId(UUID.fromString(serviceId));
        deployServiceEntity.setUserId(userId);
        deployServiceEntity.setCsp(Csp.HUAWEI);
        final DeployResourceEntity deployResourceEntity = new DeployResourceEntity();
        deployResourceEntity.setResourceId(resourceId);
        deployResourceEntity.setKind(DeployResourceKind.VM);
        deployResourceEntity.setProperties(Map.ofEntries(Map.entry("value", "value")));
        deployServiceEntity.setDeployResourceList(List.of(deployResourceEntity));
        when(mockDeployServiceStorage.findDeployServiceById(
                eq(UUID.fromString(serviceId)))).thenReturn(deployServiceEntity);
        when(mockPluginManager.getOrchestratorPlugin(any(Csp.class))).thenReturn(
                getOrchestratorPlugin(Csp.HUAWEI, expectedResult));
        when(orchestratorPlugin.getMetricsForService(any())).thenReturn(expectedResult);
        when(userServiceHelper.currentUserIsOwner(userId)).thenReturn(true);
        // Run the test
        final List<Metric> result =
                serviceMetricsAdapterUnderTest.getMetricsByServiceId(serviceId, null, null, null,
                        null, true);

        final List<Metric> result1 =
                serviceMetricsAdapterUnderTest.getMetricsByServiceId(serviceId, null, null, null,
                        null, false);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
        assertThat(result1).isEqualTo(expectedResult);
    }


    @Test
    void testGetMetricsByResourceIdException() {

        // Configure DeployResourceStorage.findDeployResourceByResourceId(...).
        final DeployResourceEntity deployResourceEntity = new DeployResourceEntity();
        deployResourceEntity.setKind(DeployResourceKind.PUBLIC_IP);
        deployResourceEntity.setResourceId(resourceId);
        when(mockDeployResourceStorage.findDeployResourceByResourceId(eq(resourceId))).thenReturn(
                deployResourceEntity);

        Assertions.assertThrows(ResourceNotSupportedForMonitoringException.class,
                () -> serviceMetricsAdapterUnderTest.getMetricsByResourceId(resourceId, null, null,
                        null, null, false));

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> serviceMetricsAdapterUnderTest.getMetricsByResourceId(
                        UUID.randomUUID().toString(), null, Long.MAX_VALUE, Long.MAX_VALUE, null,
                        false));

        // Verify the results
        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> serviceMetricsAdapterUnderTest.getMetricsByResourceId("id", null, null, null,
                        null, false));
    }

    @Test
    void testGetMetricsByResourceId() {

        // Setup
        final Metric metric = new Metric();
        metric.setName("name");
        metric.setDescription("description");
        metric.setType(MetricType.COUNTER);
        metric.setMonitorResourceType(MonitorResourceType.CPU);
        metric.setUnit(MetricUnit.MB);
        final List<Metric> expectedResult = List.of(metric);

        // Configure DeployResourceStorage.findDeployResourceByResourceId(...).
        final DeployResourceEntity deployResourceEntity = new DeployResourceEntity();
        deployResourceEntity.setKind(DeployResourceKind.VM);
        deployResourceEntity.setResourceId(resourceId);
        final DeployServiceEntity deployService = new DeployServiceEntity();
        deployService.setId(UUID.fromString(serviceId));
        deployService.setUserId(userId);
        deployService.setCsp(Csp.HUAWEI);
        deployService.setDeployResourceList(List.of(new DeployResourceEntity()));
        deployResourceEntity.setDeployService(deployService);
        deployResourceEntity.setProperties(Map.ofEntries(Map.entry("value", "value")));
        when(mockDeployResourceStorage.findDeployResourceByResourceId(eq(resourceId))).thenReturn(
                deployResourceEntity);
        when(mockPluginManager.getOrchestratorPlugin(Csp.HUAWEI)).thenReturn(
                getOrchestratorPlugin(Csp.HUAWEI, expectedResult));
        when(orchestratorPlugin.getMetricsForService(any())).thenReturn(expectedResult);
        when(mockDeployServiceStorage.findDeployServiceById(
                eq(UUID.fromString(serviceId)))).thenReturn(deployService);
        when(mockPluginManager.getOrchestratorPlugin(Csp.HUAWEI)).thenReturn(
                getOrchestratorPlugin(Csp.HUAWEI, expectedResult));
        when(orchestratorPlugin.getMetricsForService(any())).thenReturn(expectedResult);
        when(userServiceHelper.currentUserIsOwner(userId)).thenReturn(true);
        // Run the test
        final List<Metric> result =
                serviceMetricsAdapterUnderTest.getMetricsByResourceId(resourceId, null, null, null,
                        null, true);

        final List<Metric> result1 =
                serviceMetricsAdapterUnderTest.getMetricsByResourceId(resourceId, null, null, null,
                        null, false);
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);

        assertThat(result1).isEqualTo(expectedResult);
    }

    @Test
    void testGetMetricsBySourceIdException() {
        long currentTimeMillis = System.currentTimeMillis();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> serviceMetricsAdapterUnderTest.getMetricsByServiceId(
                        UUID.randomUUID().toString(), null, currentTimeMillis, 1L, null, false));

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> serviceMetricsAdapterUnderTest.getMetricsByServiceId(
                        UUID.randomUUID().toString(), null, currentTimeMillis + 3000,
                        currentTimeMillis + 5000, null, false));

        Assertions.assertThrows(ServiceNotDeployedException.class,
                () -> serviceMetricsAdapterUnderTest.getMetricsByServiceId(
                        UUID.randomUUID().toString(), null, null, null, null, false));
    }

    private OrchestratorPlugin getOrchestratorPlugin(Csp csp, List<Metric> metrics) {
        return new OrchestratorPlugin() {

            @Override
            public FlavorPriceResult getServicePrice(ServicePriceRequest request) {
                return null;
            }

            @Override
            public void auditApiRequest(AuditLog auditLog) {
                log.info(auditLog.toString());
            }

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
                return csp;
            }

            @Override
            public List<String> requiredProperties() {
                return null;
            }

            @Override
            public boolean autoApproveServiceTemplateIsEnabled() {
                return false;
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
            public Map<DeployerKind, DeployResourceHandler> resourceHandlers() {
                return null;
            }

            @Override
            public List<String> getExistingResourceNamesWithKind(String userId, String region,
                                                                 DeployResourceKind kind) {
                return new ArrayList<>();
            }

            @Override
            public List<String> getAvailabilityZonesOfRegion(String userId, String region,
                                                             UUID serviceId) {
                return new ArrayList<>();
            }

            @Override
            public List<Metric> getMetricsForResource(
                    ResourceMetricsRequest resourceMetricRequest) {
                return metrics;
            }

            @Override
            public List<Metric> getMetricsForService(ServiceMetricsRequest serviceMetricRequest) {
                return metrics;
            }
        };

    }

}
