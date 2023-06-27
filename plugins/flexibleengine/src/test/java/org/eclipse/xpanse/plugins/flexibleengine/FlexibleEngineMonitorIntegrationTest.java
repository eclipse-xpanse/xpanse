package org.eclipse.xpanse.plugins.flexibleengine;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.huaweicloud.sdk.ces.v1.model.BatchListMetricDataResponse;
import com.huaweicloud.sdk.ces.v1.model.ListMetricsResponse;
import com.huaweicloud.sdk.ces.v1.model.ShowMetricDataResponse;
import com.huaweicloud.sdk.core.http.HttpMethod;
import com.huaweicloud.sdk.core.internal.model.KeystoneListProjectsResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.apache.http.client.methods.HttpRequestBase;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.monitor.Metric;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.monitor.MonitorMetricStore;
import org.eclipse.xpanse.modules.monitor.cache.MonitorMetricCacheManager;
import org.eclipse.xpanse.modules.orchestrator.monitor.ResourceMetricRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ServiceMetricRequest;
import org.eclipse.xpanse.plugins.flexibleengine.monitor.constant.FlexibleEngineMonitorConstants;
import org.eclipse.xpanse.plugins.flexibleengine.monitor.models.FlexibleEngineMonitorClient;
import org.eclipse.xpanse.plugins.flexibleengine.monitor.utils.FlexibleEngineMonitorConverter;
import org.eclipse.xpanse.plugins.flexibleengine.monitor.utils.MetricsService;
import org.eclipse.xpanse.plugins.flexibleengine.monitor.utils.RetryTemplateService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {FlexibleEngineOrchestratorPlugin.class, MetricsService.class,
        FlexibleEngineMonitorClient.class, RetryTemplateService.class,
        FlexibleEngineMonitorConverter.class, CredentialCenter.class,
        MonitorMetricStore.class, MonitorMetricCacheManager.class})
class FlexibleEngineMonitorIntegrationTest {

    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
            .options(wireMockConfig()
                    .dynamicPort()
                    .extensions(new ResponseTemplateTransformer(true)))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    FlexibleEngineOrchestratorPlugin plugin;
    @Autowired
    FlexibleEngineMonitorConverter converter;

    @MockBean
    CredentialCenter credentialCenter;

    @MockBean
    FlexibleEngineMonitorClient client;

    ResourceMetricRequest setUpResourceMetricRequest(MonitorResourceType monitorResourceType,
                                                     Long from, Long to,
                                                     boolean onlyLastKnownMetric) throws Exception {
        final DeployResource deployResource = new DeployResource();
        deployResource.setResourceId("ca0f0cf6-16ef-4e7e-bb39-419d7791d3fd");
        deployResource.setName("name");
        deployResource.setKind(DeployResourceKind.VM);
        deployResource.setProperties(Map.ofEntries(Map.entry("region", "eu-west-0")));
        return new ResourceMetricRequest(deployResource, monitorResourceType, from, to, null,
                onlyLastKnownMetric, "xpanseUserName");

    }

    void mockClientHttpRequest() throws Exception {
        when(this.credentialCenter.getCredential(any(), any(), any())).thenReturn(
                getCredentialDefinition());
        when(this.client.buildGetRequest(any(), matches("/v3/projects(.*)"))).thenReturn(
                getHttpRequestBase(HttpMethod.GET.name(), "/v3/projects"));

        when(this.client.buildGetRequest(any(), matches("/V1.0/.*/metrics(.*)"))).thenReturn(
                getHttpRequestBase(HttpMethod.GET.name(), "/V1.0/project_id/metrics"));

        when(this.client.buildGetRequest(any(),
                matches("/V1.0/.*/metric-data(.*cpu_.*)"))).thenReturn(
                getHttpRequestBase(HttpMethod.GET.name(),
                        "/V1.0/project_id/metric-data?namespace=cpu_"));
        when(this.client.buildGetRequest(any(),
                matches("/V1.0/.*/metric-data(.*mem_.*)"))).thenReturn(
                getHttpRequestBase(HttpMethod.GET.name(),
                        "/V1.0/project_id/metric-data?namespace=mem_"));

        when(this.client.buildGetRequest(any(),
                matches("/V1.0/.*/metric-data(.*net_bitSent.*)"))).thenReturn(
                getHttpRequestBase(HttpMethod.GET.name(),
                        "/V1.0/project_id/metric-data?namespace=net_bitSent"));

        when(this.client.buildGetRequest(any(),
                matches("/V1.0/.*/metric-data(.*net_bitRecv.*)"))).thenReturn(
                getHttpRequestBase(HttpMethod.GET.name(),
                        "/V1.0/project_id/metric-data?namespace=net_bitRecv"));

        when(this.client.buildPostRequest(any(), matches("/V1.0/.*/batch-query-metric-data"),
                any())).thenReturn(
                getHttpRequestBase(HttpMethod.POST.name(),
                        "/V1.0/project_id/batch-query-metric-data"));
    }


    ServiceMetricRequest setUpServiceMetricRequest(MonitorResourceType monitorResourceType,
                                                   Long from, Long to,
                                                   boolean onlyLastKnownMetric) {
        final DeployResource deployResource = new DeployResource();
        deployResource.setResourceId("ca0f0cf6-16ef-4e7e-bb39-419d7791d3fd");
        deployResource.setName("name");
        deployResource.setKind(DeployResourceKind.VM);
        deployResource.setProperties(Map.ofEntries(Map.entry("region", "eu-west-0")));
        return new ServiceMetricRequest(List.of(deployResource), monitorResourceType, from, to,
                null,
                onlyLastKnownMetric, "xpanseUserName");
    }


    @Test
    void testGetMetricsForResourceWithParamsOnlyLastKnownMetricTrue() throws Exception {

        // Setup
        ResourceMetricRequest resourceMetricRequest =
                setUpResourceMetricRequest(null, null, null, true);
        mockClientHttpRequest();

        // Run the test
        List<Metric> metrics = plugin.getMetricsForResource(resourceMetricRequest);

        // Verify the results
        Assertions.assertFalse(metrics.isEmpty());
        Assertions.assertEquals(4, metrics.size());
        Assertions.assertEquals(1, metrics.get(0).getMetrics().size());
        Assertions.assertEquals(1, metrics.get(1).getMetrics().size());
        Assertions.assertEquals(1, metrics.get(2).getMetrics().size());
        Assertions.assertEquals(1, metrics.get(3).getMetrics().size());
    }

    @Test
    void testGetMetricsForResourceWithParamsFromAndTo() throws Exception {
        // Setup
        ResourceMetricRequest resourceMetricRequest =
                setUpResourceMetricRequest(null, System.currentTimeMillis() -
                                FlexibleEngineMonitorConstants.ONE_DAY_MILLISECONDS,
                        System.currentTimeMillis(), false);
        mockClientHttpRequest();

        // Run the test
        List<Metric> metrics = plugin.getMetricsForResource(resourceMetricRequest);

        // Verify the results
        Assertions.assertFalse(metrics.isEmpty());
        Assertions.assertEquals(4, metrics.size());
        Assertions.assertEquals(5, metrics.get(0).getMetrics().size());
        Assertions.assertEquals(5, metrics.get(1).getMetrics().size());
        Assertions.assertEquals(5, metrics.get(2).getMetrics().size());
        Assertions.assertEquals(5, metrics.get(3).getMetrics().size());
    }

    @Test
    void testGetMetricsForResourceWithParamsTypeCpu() throws Exception {
        // Setup
        ResourceMetricRequest resourceMetricRequest =
                setUpResourceMetricRequest(MonitorResourceType.CPU, System.currentTimeMillis() -
                                FlexibleEngineMonitorConstants.THREE_DAY_MILLISECONDS,
                        System.currentTimeMillis(), false);
        mockClientHttpRequest();

        // Run the test
        List<Metric> metrics = plugin.getMetricsForResource(resourceMetricRequest);

        // Verify the results
        Assertions.assertFalse(metrics.isEmpty());
        Assertions.assertEquals(1, metrics.size());
        Assertions.assertEquals(MonitorResourceType.CPU, metrics.get(0).getMonitorResourceType());
        Assertions.assertEquals(5, metrics.get(0).getMetrics().size());
    }


    @Test
    void testGetMetricsForResourceWithParamsTypeMem() throws Exception {
        // Setup
        ResourceMetricRequest resourceMetricRequest =
                setUpResourceMetricRequest(MonitorResourceType.MEM, System.currentTimeMillis() -
                                FlexibleEngineMonitorConstants.TEN_DAY_MILLISECONDS,
                        System.currentTimeMillis(), false);
        mockClientHttpRequest();

        // Run the test
        List<Metric> metrics = plugin.getMetricsForResource(resourceMetricRequest);

        // Verify the results
        Assertions.assertFalse(metrics.isEmpty());
        Assertions.assertEquals(1, metrics.size());
        Assertions.assertEquals(MonitorResourceType.MEM, metrics.get(0).getMonitorResourceType());
        Assertions.assertEquals(5, metrics.get(0).getMetrics().size());
    }


    @Test
    void testGetMetricsForResourceWithParamsTypeVmNetworkIncoming() throws Exception {
        // Setup
        ResourceMetricRequest resourceMetricRequest =
                setUpResourceMetricRequest(MonitorResourceType.VM_NETWORK_INCOMING,
                        System.currentTimeMillis() -
                                FlexibleEngineMonitorConstants.ONE_MONTH_MILLISECONDS,
                        System.currentTimeMillis(), false);
        mockClientHttpRequest();

        // Run the test
        List<Metric> metrics = plugin.getMetricsForResource(resourceMetricRequest);

        // Verify the results
        Assertions.assertFalse(metrics.isEmpty());
        Assertions.assertEquals(1, metrics.size());
        Assertions.assertEquals(MonitorResourceType.VM_NETWORK_INCOMING,
                metrics.get(0).getMonitorResourceType());
        Assertions.assertEquals(5, metrics.get(0).getMetrics().size());
    }


    @Test
    void testGetMetricsForResourceWithParamsTypeVmNetworkOutgoing() throws Exception {
        // Setup
        ResourceMetricRequest resourceMetricRequest =
                setUpResourceMetricRequest(MonitorResourceType.VM_NETWORK_OUTGOING,
                        System.currentTimeMillis() -
                                FlexibleEngineMonitorConstants.ONE_MONTH_MILLISECONDS - 1,
                        System.currentTimeMillis(), false);
        mockClientHttpRequest();

        // Run the test
        List<Metric> metrics = plugin.getMetricsForResource(resourceMetricRequest);

        // Verify the results
        Assertions.assertFalse(metrics.isEmpty());
        Assertions.assertEquals(1, metrics.size());
        Assertions.assertEquals(MonitorResourceType.VM_NETWORK_OUTGOING,
                metrics.get(0).getMonitorResourceType());
        Assertions.assertEquals(5, metrics.get(0).getMetrics().size());
    }


    @Test
    void testGetMetricsForServiceWithParamsOnlyLastKnownMetricTrue() throws Exception {
        // Setup
        ServiceMetricRequest serviceMetricRequest =
                setUpServiceMetricRequest(null, null, null, true);
        mockClientHttpRequest();

        // Run the test
        List<Metric> metrics = plugin.getMetricsForService(serviceMetricRequest);

        // Verify the results
        Assertions.assertFalse(metrics.isEmpty());
        Assertions.assertEquals(4, metrics.size());
        Assertions.assertEquals(1, metrics.get(0).getMetrics().size());
        Assertions.assertEquals(1, metrics.get(1).getMetrics().size());
        Assertions.assertEquals(1, metrics.get(2).getMetrics().size());
        Assertions.assertEquals(1, metrics.get(3).getMetrics().size());
    }

    @Test
    void testGetMetricsForServiceWithParamsFromAndTo() throws Exception {
        // Setup
        ServiceMetricRequest serviceMetricRequest =
                setUpServiceMetricRequest(null, System.currentTimeMillis() -
                                FlexibleEngineMonitorConstants.ONE_DAY_MILLISECONDS,
                        System.currentTimeMillis(), false);
        mockClientHttpRequest();

        // Run the test
        List<Metric> metrics = plugin.getMetricsForService(serviceMetricRequest);

        // Verify the results
        Assertions.assertEquals(4, metrics.size());
        Assertions.assertEquals(4, metrics.get(0).getMetrics().size());
        Assertions.assertEquals(4, metrics.get(1).getMetrics().size());
        Assertions.assertEquals(4, metrics.get(2).getMetrics().size());
        Assertions.assertEquals(4, metrics.get(3).getMetrics().size());
    }

    @Test
    void testGetMetricsForServiceWithParamsTypeCpu() throws Exception {
        // Setup
        ServiceMetricRequest serviceMetricRequest =
                setUpServiceMetricRequest(MonitorResourceType.CPU, System.currentTimeMillis() -
                                FlexibleEngineMonitorConstants.ONE_DAY_MILLISECONDS,
                        System.currentTimeMillis(), false);
        mockClientHttpRequest();

        // Run the test
        List<Metric> metrics = plugin.getMetricsForService(serviceMetricRequest);

        // Verify the results
        Assertions.assertEquals(1, metrics.size());
        Assertions.assertEquals(MonitorResourceType.CPU, metrics.get(0).getMonitorResourceType());
        Assertions.assertEquals(4, metrics.get(0).getMetrics().size());
    }

    @Test
    void testGetMetricsForServiceWithParamsTypeMem() throws Exception {
        // Setup
        ServiceMetricRequest serviceMetricRequest =
                setUpServiceMetricRequest(MonitorResourceType.MEM, System.currentTimeMillis() -
                                FlexibleEngineMonitorConstants.ONE_DAY_MILLISECONDS,
                        System.currentTimeMillis(), false);
        mockClientHttpRequest();

        // Run the test
        List<Metric> metrics = plugin.getMetricsForService(serviceMetricRequest);

        // Verify the results
        Assertions.assertEquals(1, metrics.size());
        Assertions.assertEquals(MonitorResourceType.MEM, metrics.get(0).getMonitorResourceType());
        Assertions.assertEquals(4, metrics.get(0).getMetrics().size());
    }

    @Test
    void testGetMetricsForServiceWithParamsTypeVmNetworkIncoming() throws Exception {
        // Setup
        ServiceMetricRequest serviceMetricRequest =
                setUpServiceMetricRequest(MonitorResourceType.VM_NETWORK_INCOMING,
                        System.currentTimeMillis() -
                                FlexibleEngineMonitorConstants.ONE_DAY_MILLISECONDS,
                        System.currentTimeMillis(), false);
        mockClientHttpRequest();

        // Run the test
        List<Metric> metrics = plugin.getMetricsForService(serviceMetricRequest);

        // Verify the results
        Assertions.assertEquals(1, metrics.size());
        Assertions.assertEquals(MonitorResourceType.VM_NETWORK_INCOMING,
                metrics.get(0).getMonitorResourceType());
        Assertions.assertEquals(4, metrics.get(0).getMetrics().size());
    }

    @Test
    void testGetMetricsForServiceWithParamsTypeVmNetworkOutgoing() throws Exception {
        // Setup
        ServiceMetricRequest serviceMetricRequest =
                setUpServiceMetricRequest(MonitorResourceType.VM_NETWORK_OUTGOING,
                        System.currentTimeMillis() -
                                FlexibleEngineMonitorConstants.ONE_DAY_MILLISECONDS,
                        System.currentTimeMillis(), false);
        mockClientHttpRequest();

        // Run the test
        List<Metric> metrics = plugin.getMetricsForService(serviceMetricRequest);

        // Verify the results
        Assertions.assertEquals(1, metrics.size());
        Assertions.assertEquals(MonitorResourceType.VM_NETWORK_OUTGOING,
                metrics.get(0).getMonitorResourceType());
        Assertions.assertEquals(4, metrics.get(0).getMetrics().size());
    }


    private CredentialVariables getCredentialDefinition() {
        CredentialVariables credentialVariables =
                (CredentialVariables) this.plugin.getCredentialDefinitions().get(0);
        for (CredentialVariable credentialVariable : credentialVariables.getVariables()) {
            if (credentialVariable.getName().equals(FlexibleEngineMonitorConstants.OS_ACCESS_KEY)) {
                credentialVariable.setValue(FlexibleEngineMonitorConstants.OS_ACCESS_KEY);
            }
            if (credentialVariable.getName().equals(FlexibleEngineMonitorConstants.OS_SECRET_KEY)) {
                credentialVariable.setValue(FlexibleEngineMonitorConstants.OS_SECRET_KEY);
            }
        }
        return credentialVariables;
    }


    private HttpRequestBase getHttpRequestBase(String httpMethod, String url)
            throws URISyntaxException {
        HttpRequestBase httpRequestBase = new HttpRequestBase() {
            @Override
            public String getMethod() {
                return httpMethod;
            }
        };
        httpRequestBase.setURI(new URI(wireMockExtension.getRuntimeInfo().getHttpBaseUrl() + url));
        httpRequestBase.setHeader("Content-Type", "application/json");
        return httpRequestBase;
    }

}
