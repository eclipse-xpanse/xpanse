package org.eclipse.xpanse.plugins.huaweicloud.monitor;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.common.ClasspathFileSource;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.extension.responsetemplating.TemplateEngine;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.huaweicloud.sdk.ces.v1.CesClient;
import com.huaweicloud.sdk.core.HcClient;
import com.huaweicloud.sdk.core.auth.BasicCredentials;
import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.core.http.HttpConfig;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.common.proxy.ProxyConfigurationManager;
import org.eclipse.xpanse.modules.cache.monitor.MonitorMetricsStore;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.monitor.Metric;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.modules.models.service.deployment.DeployResource;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;
import org.eclipse.xpanse.modules.orchestrator.monitor.ResourceMetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ServiceMetricsRequest;
import org.eclipse.xpanse.plugins.huaweicloud.HuaweiCloudOrchestratorPlugin;
import org.eclipse.xpanse.plugins.huaweicloud.common.HuaweiCloudClient;
import org.eclipse.xpanse.plugins.huaweicloud.common.HuaweiCloudRetryStrategy;
import org.eclipse.xpanse.plugins.huaweicloud.manage.HuaweiCloudResourceManager;
import org.eclipse.xpanse.plugins.huaweicloud.manage.HuaweiCloudServerManageRequestConverter;
import org.eclipse.xpanse.plugins.huaweicloud.manage.HuaweiCloudVmStateManager;
import org.eclipse.xpanse.plugins.huaweicloud.monitor.constant.HuaweiCloudMonitorConstants;
import org.eclipse.xpanse.plugins.huaweicloud.price.HuaweiCloudGlobalPriceCalculator;
import org.eclipse.xpanse.plugins.huaweicloud.price.HuaweiCloudPriceCalculator;
import org.eclipse.xpanse.plugins.huaweicloud.resourcehandler.HuaweiCloudTerraformResourceHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {
            HuaweiCloudOrchestratorPlugin.class,
            CredentialCenter.class,
            HuaweiCloudVmStateManager.class,
            HuaweiCloudServerManageRequestConverter.class,
            HuaweiCloudMetricsService.class,
            HuaweiCloudClient.class,
            HuaweiCloudMonitorConstants.class,
            HuaweiCloudDataModelConverter.class,
            MonitorMetricsStore.class,
            HuaweiCloudResourceManager.class,
            HuaweiCloudTerraformResourceHandler.class,
            HuaweiCloudPriceCalculator.class,
            HuaweiCloudGlobalPriceCalculator.class,
            HuaweiCloudRetryStrategy.class,
            ProxyConfigurationManager.class
        })
class HuaweiCloudMonitorIntegrationTest {

    @RegisterExtension
    static WireMockExtension wireMockExtension =
            WireMockExtension.newInstance()
                    .options(
                            wireMockConfig()
                                    .dynamicPort()
                                    .extensions(
                                            new ResponseTemplateTransformer(
                                                    TemplateEngine.defaultTemplateEngine(),
                                                    false,
                                                    new ClasspathFileSource(
                                                            "src/test/resources/mappings"),
                                                    Collections.emptyList())))
                    .build();

    @Autowired HuaweiCloudOrchestratorPlugin plugin;
    @MockitoBean HuaweiCloudClient huaweiCloudClient;
    @MockitoBean CredentialCenter credentialCenter;
    @MockitoBean HuaweiCloudRetryStrategy huaweiCloudRetryStrategy;

    ResourceMetricsRequest setUpResourceMetricRequest(
            MonitorResourceType monitorResourceType,
            Long from,
            Long to,
            boolean onlyLastKnownMetric) {
        Region region = new Region();
        region.setName("eu-west-101");
        region.setSite("Europe");
        region.setArea("Western Europe");
        final DeployResource deployResource = new DeployResource();
        deployResource.setResourceId("ca0f0cf6-16ef-4e7e-bb39-419d7791d3fd");
        deployResource.setResourceName("name");
        deployResource.setResourceKind(DeployResourceKind.VM);
        deployResource.setProperties(Map.ofEntries(Map.entry("region", "cn-southwest-2")));
        return new ResourceMetricsRequest(
                UUID.randomUUID(),
                region,
                deployResource,
                monitorResourceType,
                from,
                to,
                null,
                onlyLastKnownMetric,
                "userId");
    }

    void mockCesClient() {
        when(this.credentialCenter.getCredential(any(), any(), any(), any()))
                .thenReturn(getCredentialDefinition());
        when(this.huaweiCloudClient.getCesClient(any(), any())).thenReturn(getCesClient());
        when(this.huaweiCloudClient.getBasicCredential(any(), any(), any()))
                .thenReturn(getCredential());
    }

    ServiceMetricsRequest setUpServiceMetricRequest(
            MonitorResourceType monitorResourceType,
            Long from,
            Long to,
            boolean onlyLastKnownMetric) {
        Region region = new Region();
        region.setName("eu-west-101");
        region.setSite("Europe");
        region.setArea("Western Europe");
        final DeployResource deployResource = new DeployResource();
        deployResource.setResourceId("ca0f0cf6-16ef-4e7e-bb39-419d7791d3fd");
        deployResource.setResourceName("name");
        deployResource.setResourceKind(DeployResourceKind.VM);
        deployResource.setProperties(Map.ofEntries(Map.entry("region", "cn-southwest-2")));
        return new ServiceMetricsRequest(
                UUID.randomUUID(),
                region,
                List.of(deployResource),
                monitorResourceType,
                from,
                to,
                null,
                onlyLastKnownMetric,
                "userId");
    }

    void mockAllRequestForService() {
        when(this.credentialCenter.getCredential(any(), any(), any(), any()))
                .thenReturn(getCredentialDefinition());
        when(huaweiCloudClient.getCesClient(any(), any())).thenReturn(getCesClient());
    }

    @Test
    void testGetMetricsForResourceWithParamsOnlyLastKnownMetricTrue() {

        // Setup
        ResourceMetricsRequest resourceMetricRequest =
                setUpResourceMetricRequest(null, null, null, true);
        mockCesClient();

        // Run the test
        List<Metric> metrics = plugin.getMetricsForResource(resourceMetricRequest);

        // Verify the results
        Assertions.assertFalse(metrics.isEmpty());
        Assertions.assertEquals(4, metrics.size());
        Assertions.assertEquals(1, metrics.getFirst().getMetrics().size());
        Assertions.assertEquals(1, metrics.get(1).getMetrics().size());
        Assertions.assertEquals(1, metrics.get(2).getMetrics().size());
        Assertions.assertEquals(1, metrics.get(3).getMetrics().size());
    }

    @Test
    void testGetMetricsForResourceWithParamsFromAndTo() {
        mockCesClient();
        // Setup
        ResourceMetricsRequest resourceMetricRequest =
                setUpResourceMetricRequest(
                        null,
                        System.currentTimeMillis()
                                - HuaweiCloudMonitorConstants.ONE_DAY_MILLISECONDS,
                        System.currentTimeMillis(),
                        false);
        mockCesClient();

        // Run the test
        List<Metric> metrics = plugin.getMetricsForResource(resourceMetricRequest);

        // Verify the results
        Assertions.assertFalse(metrics.isEmpty());
        Assertions.assertEquals(4, metrics.size());
        Assertions.assertEquals(5, metrics.getFirst().getMetrics().size());
        Assertions.assertEquals(5, metrics.get(1).getMetrics().size());
        Assertions.assertEquals(5, metrics.get(2).getMetrics().size());
        Assertions.assertEquals(5, metrics.get(3).getMetrics().size());
    }

    @Test
    void testGetMetricsForResourceWithParamsTypeCpu() {
        // Setup
        ResourceMetricsRequest resourceMetricRequest =
                setUpResourceMetricRequest(
                        MonitorResourceType.CPU,
                        System.currentTimeMillis()
                                - HuaweiCloudMonitorConstants.THREE_DAY_MILLISECONDS,
                        System.currentTimeMillis(),
                        false);
        mockCesClient();

        // Run the test
        List<Metric> metrics = plugin.getMetricsForResource(resourceMetricRequest);

        // Verify the results
        Assertions.assertFalse(metrics.isEmpty());
        Assertions.assertEquals(1, metrics.size());
        Assertions.assertEquals(
                MonitorResourceType.CPU, metrics.getFirst().getMonitorResourceType());
        Assertions.assertEquals(5, metrics.getFirst().getMetrics().size());
    }

    @Test
    void testGetMetricsForResourceWithParamsTypeMem() {
        // Setup
        ResourceMetricsRequest resourceMetricRequest =
                setUpResourceMetricRequest(
                        MonitorResourceType.MEM,
                        System.currentTimeMillis()
                                - HuaweiCloudMonitorConstants.TEN_DAY_MILLISECONDS,
                        System.currentTimeMillis(),
                        false);
        mockCesClient();

        // Run the test
        List<Metric> metrics = plugin.getMetricsForResource(resourceMetricRequest);

        // Verify the results
        Assertions.assertFalse(metrics.isEmpty());
        Assertions.assertEquals(1, metrics.size());
        Assertions.assertEquals(
                MonitorResourceType.MEM, metrics.getFirst().getMonitorResourceType());
        Assertions.assertEquals(5, metrics.getFirst().getMetrics().size());
    }

    @Test
    void testGetMetricsForResourceWithParamsTypeVmNetworkIncoming() {
        // Setup
        ResourceMetricsRequest resourceMetricRequest =
                setUpResourceMetricRequest(
                        MonitorResourceType.VM_NETWORK_INCOMING,
                        System.currentTimeMillis()
                                - HuaweiCloudMonitorConstants.ONE_MONTH_MILLISECONDS,
                        System.currentTimeMillis(),
                        false);
        mockCesClient();

        // Run the test
        List<Metric> metrics = plugin.getMetricsForResource(resourceMetricRequest);

        // Verify the results
        Assertions.assertFalse(metrics.isEmpty());
        Assertions.assertEquals(1, metrics.size());
        Assertions.assertEquals(
                MonitorResourceType.VM_NETWORK_INCOMING,
                metrics.getFirst().getMonitorResourceType());
        Assertions.assertEquals(5, metrics.getFirst().getMetrics().size());
    }

    @Test
    void testGetMetricsForResourceWithParamsTypeVmNetworkOutgoing() {
        // Setup
        ResourceMetricsRequest resourceMetricRequest =
                setUpResourceMetricRequest(
                        MonitorResourceType.VM_NETWORK_OUTGOING,
                        System.currentTimeMillis()
                                - HuaweiCloudMonitorConstants.ONE_MONTH_MILLISECONDS
                                - 1,
                        System.currentTimeMillis(),
                        false);
        mockCesClient();

        // Run the test
        List<Metric> metrics = plugin.getMetricsForResource(resourceMetricRequest);

        // Verify the results
        Assertions.assertFalse(metrics.isEmpty());
        Assertions.assertEquals(1, metrics.size());
        Assertions.assertEquals(
                MonitorResourceType.VM_NETWORK_OUTGOING,
                metrics.getFirst().getMonitorResourceType());
        Assertions.assertEquals(5, metrics.getFirst().getMetrics().size());
    }

    @Test
    void testGetMetricsForServiceWithParamsOnlyLastKnownMetricTrue() {
        // Setup
        ServiceMetricsRequest serviceMetricRequest =
                setUpServiceMetricRequest(null, null, null, true);
        mockAllRequestForService();

        // Run the test
        List<Metric> metrics = plugin.getMetricsForService(serviceMetricRequest);

        // Verify the results
        Assertions.assertFalse(metrics.isEmpty());
        Assertions.assertEquals(4, metrics.size());
        Assertions.assertEquals(1, metrics.getFirst().getMetrics().size());
        Assertions.assertEquals(1, metrics.get(1).getMetrics().size());
        Assertions.assertEquals(1, metrics.get(2).getMetrics().size());
        Assertions.assertEquals(1, metrics.get(3).getMetrics().size());
    }

    @Test
    void testGetMetricsForServiceWithParamsFromAndTo() {
        // Setup
        ServiceMetricsRequest serviceMetricRequest =
                setUpServiceMetricRequest(
                        null,
                        System.currentTimeMillis()
                                - HuaweiCloudMonitorConstants.ONE_DAY_MILLISECONDS,
                        System.currentTimeMillis(),
                        false);
        mockAllRequestForService();

        // Run the test
        List<Metric> metrics = plugin.getMetricsForService(serviceMetricRequest);

        // Verify the results
        Assertions.assertEquals(4, metrics.size());
        Assertions.assertEquals(4, metrics.getFirst().getMetrics().size());
        Assertions.assertEquals(4, metrics.get(1).getMetrics().size());
        Assertions.assertEquals(4, metrics.get(2).getMetrics().size());
        Assertions.assertEquals(4, metrics.get(3).getMetrics().size());
    }

    @Test
    void testGetMetricsForServiceWithParamsTypeCpu() {
        // Setup
        ServiceMetricsRequest serviceMetricRequest =
                setUpServiceMetricRequest(
                        MonitorResourceType.CPU,
                        System.currentTimeMillis()
                                - HuaweiCloudMonitorConstants.ONE_DAY_MILLISECONDS,
                        System.currentTimeMillis(),
                        false);
        mockAllRequestForService();

        // Run the test
        List<Metric> metrics = plugin.getMetricsForService(serviceMetricRequest);

        // Verify the results
        Assertions.assertEquals(1, metrics.size());
        Assertions.assertEquals(
                MonitorResourceType.CPU, metrics.getFirst().getMonitorResourceType());
        Assertions.assertEquals(4, metrics.getFirst().getMetrics().size());
    }

    @Test
    void testGetMetricsForServiceWithParamsTypeMem() {
        // Setup
        ServiceMetricsRequest serviceMetricRequest =
                setUpServiceMetricRequest(
                        MonitorResourceType.MEM,
                        System.currentTimeMillis()
                                - HuaweiCloudMonitorConstants.ONE_DAY_MILLISECONDS,
                        System.currentTimeMillis(),
                        false);
        mockAllRequestForService();

        // Run the test
        List<Metric> metrics = plugin.getMetricsForService(serviceMetricRequest);

        // Verify the results
        Assertions.assertEquals(1, metrics.size());
        Assertions.assertEquals(
                MonitorResourceType.MEM, metrics.getFirst().getMonitorResourceType());
        Assertions.assertEquals(4, metrics.getFirst().getMetrics().size());
    }

    @Test
    void testGetMetricsForServiceWithParamsTypeVmNetworkIncoming() {
        // Setup
        ServiceMetricsRequest serviceMetricRequest =
                setUpServiceMetricRequest(
                        MonitorResourceType.VM_NETWORK_INCOMING,
                        System.currentTimeMillis()
                                - HuaweiCloudMonitorConstants.ONE_DAY_MILLISECONDS,
                        System.currentTimeMillis(),
                        false);
        mockAllRequestForService();

        // Run the test
        List<Metric> metrics = plugin.getMetricsForService(serviceMetricRequest);

        // Verify the results
        Assertions.assertEquals(1, metrics.size());
        Assertions.assertEquals(
                MonitorResourceType.VM_NETWORK_INCOMING,
                metrics.getFirst().getMonitorResourceType());
        Assertions.assertEquals(4, metrics.getFirst().getMetrics().size());
    }

    @Test
    void testGetMetricsForServiceWithParamsTypeVmNetworkOutgoing() {
        // Setup
        ServiceMetricsRequest serviceMetricRequest =
                setUpServiceMetricRequest(
                        MonitorResourceType.VM_NETWORK_OUTGOING,
                        System.currentTimeMillis()
                                - HuaweiCloudMonitorConstants.ONE_DAY_MILLISECONDS,
                        System.currentTimeMillis(),
                        false);
        mockAllRequestForService();

        // Run the test
        List<Metric> metrics = plugin.getMetricsForService(serviceMetricRequest);

        // Verify the results
        Assertions.assertEquals(1, metrics.size());
        Assertions.assertEquals(
                MonitorResourceType.VM_NETWORK_OUTGOING,
                metrics.getFirst().getMonitorResourceType());
        Assertions.assertEquals(4, metrics.getFirst().getMetrics().size());
    }

    private CredentialVariables getCredentialDefinition() {
        CredentialVariables credentialVariables =
                (CredentialVariables) this.plugin.getCredentialDefinitions().getFirst();
        for (CredentialVariable credentialVariable : credentialVariables.getVariables()) {
            if (credentialVariable.getName().equals(HuaweiCloudMonitorConstants.HW_ACCESS_KEY)) {
                credentialVariable.setValue(HuaweiCloudMonitorConstants.HW_ACCESS_KEY);
            }
            if (credentialVariable.getName().equals(HuaweiCloudMonitorConstants.HW_SECRET_KEY)) {
                credentialVariable.setValue(HuaweiCloudMonitorConstants.HW_SECRET_KEY);
            }
        }
        return credentialVariables;
    }

    private CesClient getCesClient() {
        ICredential iCredential =
                new BasicCredentials()
                        .withAk(HuaweiCloudMonitorConstants.HW_ACCESS_KEY)
                        .withSk(HuaweiCloudMonitorConstants.HW_SECRET_KEY);
        HcClient hcClient = new HcClient(HttpConfig.getDefaultHttpConfig());
        hcClient.withCredential(iCredential);
        hcClient.withEndpoints(
                Collections.singletonList(wireMockExtension.getRuntimeInfo().getHttpBaseUrl()));
        return new CesClient(hcClient);
    }

    private ICredential getCredential() {
        return new BasicCredentials()
                .withAk(HuaweiCloudMonitorConstants.HW_ACCESS_KEY)
                .withSk(HuaweiCloudMonitorConstants.HW_SECRET_KEY);
    }
}
