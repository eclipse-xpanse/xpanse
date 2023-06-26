package org.eclipse.xpanse.plugins.flexibleengine;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huaweicloud.sdk.ces.v1.model.BatchListMetricDataResponse;
import com.huaweicloud.sdk.ces.v1.model.ListMetricsResponse;
import com.huaweicloud.sdk.ces.v1.model.ShowMetricDataResponse;
import com.huaweicloud.sdk.core.internal.model.KeystoneListProjectsResponse;
import java.io.IOException;
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
import org.eclipse.xpanse.plugins.flexibleengine.monitor.utils.FlexibleEngineMonitorConverter;
import org.eclipse.xpanse.plugins.flexibleengine.monitor.utils.MetricsService;
import org.eclipse.xpanse.plugins.flexibleengine.monitor.utils.RetryTemplateService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {FlexibleEngineOrchestratorPlugin.class, MetricsService.class,
        FlexibleEngineMonitorConverter.class, RetryTemplateService.class, CredentialCenter.class,
        MonitorMetricStore.class, MonitorMetricCacheManager.class})
class FlexibleEngineMonitorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    FlexibleEngineOrchestratorPlugin plugin;

    @MockBean
    CredentialCenter credentialCenter;

    @MockBean
    RetryTemplateService retryTemplateService;

    ResourceMetricRequest setUpResourceMetricRequest(MonitorResourceType monitorResourceType,
                                                     Long from, Long to,
                                                     boolean onlyLastKnownMetric) {
        final DeployResource deployResource = new DeployResource();
        deployResource.setResourceId("ca0f0cf6-16ef-4e7e-bb39-419d7791d3fd");
        deployResource.setName("name");
        deployResource.setKind(DeployResourceKind.VM);
        deployResource.setProperties(Map.ofEntries(Map.entry("region", "eu-west-0")));
        return new ResourceMetricRequest(deployResource, monitorResourceType, from, to, null,
                onlyLastKnownMetric, "xpanseUserName");
    }

    void mockAllRequestForResource(MonitorResourceType monitorResourceType) throws IOException {
        when(this.credentialCenter.getCredential(any(), any(), any())).thenReturn(
                getCredentialDefinition());
        when(this.retryTemplateService.queryProjectInfo(any())).thenReturn(getProjectResponse());
        when(this.retryTemplateService.queryMetricItemList(any())).thenReturn(
                getListMetricsResponse());
        when(this.retryTemplateService.queryMetricData(any())).thenReturn(
                getShowMetricDataResponse(monitorResourceType));
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

    void mockAllRequestForService() throws IOException {
        when(this.credentialCenter.getCredential(any(), any(), any())).thenReturn(
                getCredentialDefinition());
        when(this.retryTemplateService.queryProjectInfo(any())).thenReturn(
                getProjectResponse());
        when(this.retryTemplateService.queryMetricItemList(any())).thenReturn(
                getListMetricsResponse());
        when(this.retryTemplateService.batchQueryMetricData(any(), any())).thenReturn(
                getBatchListMetricDataResponse());
    }

    @Test
    void testGetMetricsForResourceWithParamsOnlyLastKnownMetricTrue() throws IOException {

        // Setup
        ResourceMetricRequest resourceMetricRequest =
                setUpResourceMetricRequest(null, null, null, true);
        mockAllRequestForResource(null);

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
    void testGetMetricsForResourceWithParamsFromAndTo() throws IOException {
        mockAllRequestForResource(null);
        // Setup
        ResourceMetricRequest resourceMetricRequest =
                setUpResourceMetricRequest(null, System.currentTimeMillis() -
                                FlexibleEngineMonitorConstants.ONE_DAY_MILLISECONDS,
                        System.currentTimeMillis(), false);
        mockAllRequestForResource(null);

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
    void testGetMetricsForResourceWithParamsTypeCpu() throws IOException {
        // Setup
        ResourceMetricRequest resourceMetricRequest =
                setUpResourceMetricRequest(MonitorResourceType.CPU, System.currentTimeMillis() -
                                FlexibleEngineMonitorConstants.THREE_DAY_MILLISECONDS,
                        System.currentTimeMillis(), false);
        mockAllRequestForResource(null);

        // Run the test
        List<Metric> metrics = plugin.getMetricsForResource(resourceMetricRequest);

        // Verify the results
        Assertions.assertFalse(metrics.isEmpty());
        Assertions.assertEquals(1, metrics.size());
        Assertions.assertEquals(MonitorResourceType.CPU, metrics.get(0).getMonitorResourceType());
        Assertions.assertEquals(5, metrics.get(0).getMetrics().size());
    }


    @Test
    void testGetMetricsForResourceWithParamsTypeMem() throws IOException {
        // Setup
        ResourceMetricRequest resourceMetricRequest =
                setUpResourceMetricRequest(MonitorResourceType.MEM, System.currentTimeMillis() -
                                FlexibleEngineMonitorConstants.TEN_DAY_MILLISECONDS,
                        System.currentTimeMillis(), false);
        mockAllRequestForResource(MonitorResourceType.MEM);

        // Run the test
        List<Metric> metrics = plugin.getMetricsForResource(resourceMetricRequest);

        // Verify the results
        Assertions.assertFalse(metrics.isEmpty());
        Assertions.assertEquals(1, metrics.size());
        Assertions.assertEquals(MonitorResourceType.MEM, metrics.get(0).getMonitorResourceType());
        Assertions.assertEquals(5, metrics.get(0).getMetrics().size());
    }


    @Test
    void testGetMetricsForResourceWithParamsTypeVmNetworkIncoming() throws IOException {
        // Setup
        ResourceMetricRequest resourceMetricRequest =
                setUpResourceMetricRequest(MonitorResourceType.VM_NETWORK_INCOMING,
                        System.currentTimeMillis() -
                                FlexibleEngineMonitorConstants.ONE_MONTH_MILLISECONDS,
                        System.currentTimeMillis(), false);
        mockAllRequestForResource(MonitorResourceType.VM_NETWORK_INCOMING);

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
    void testGetMetricsForResourceWithParamsTypeVmNetworkOutgoing() throws IOException {
        // Setup
        ResourceMetricRequest resourceMetricRequest =
                setUpResourceMetricRequest(MonitorResourceType.VM_NETWORK_OUTGOING,
                        System.currentTimeMillis() -
                                FlexibleEngineMonitorConstants.ONE_MONTH_MILLISECONDS - 1,
                        System.currentTimeMillis(), false);
        mockAllRequestForResource(MonitorResourceType.VM_NETWORK_OUTGOING);

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
    void testGetMetricsForServiceWithParamsOnlyLastKnownMetricTrue() throws IOException {
        // Setup
        ServiceMetricRequest serviceMetricRequest =
                setUpServiceMetricRequest(null, null, null, true);
        mockAllRequestForService();

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
    void testGetMetricsForServiceWithParamsFromAndTo() throws IOException {
        // Setup
        ServiceMetricRequest serviceMetricRequest =
                setUpServiceMetricRequest(null, System.currentTimeMillis() -
                                FlexibleEngineMonitorConstants.ONE_DAY_MILLISECONDS,
                        System.currentTimeMillis(), false);
        mockAllRequestForService();

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
    void testGetMetricsForServiceWithParamsTypeCpu() throws IOException {
        // Setup
        ServiceMetricRequest serviceMetricRequest =
                setUpServiceMetricRequest(MonitorResourceType.CPU, System.currentTimeMillis() -
                                FlexibleEngineMonitorConstants.ONE_DAY_MILLISECONDS,
                        System.currentTimeMillis(), false);
        mockAllRequestForService();

        // Run the test
        List<Metric> metrics = plugin.getMetricsForService(serviceMetricRequest);

        // Verify the results
        Assertions.assertEquals(1, metrics.size());
        Assertions.assertEquals(MonitorResourceType.CPU, metrics.get(0).getMonitorResourceType());
        Assertions.assertEquals(4, metrics.get(0).getMetrics().size());
    }

    @Test
    void testGetMetricsForServiceWithParamsTypeMem() throws IOException {
        // Setup
        ServiceMetricRequest serviceMetricRequest =
                setUpServiceMetricRequest(MonitorResourceType.MEM, System.currentTimeMillis() -
                                FlexibleEngineMonitorConstants.ONE_DAY_MILLISECONDS,
                        System.currentTimeMillis(), false);
        mockAllRequestForService();

        // Run the test
        List<Metric> metrics = plugin.getMetricsForService(serviceMetricRequest);

        // Verify the results
        Assertions.assertEquals(1, metrics.size());
        Assertions.assertEquals(MonitorResourceType.MEM, metrics.get(0).getMonitorResourceType());
        Assertions.assertEquals(4, metrics.get(0).getMetrics().size());
    }

    @Test
    void testGetMetricsForServiceWithParamsTypeVmNetworkIncoming() throws IOException {
        // Setup
        ServiceMetricRequest serviceMetricRequest =
                setUpServiceMetricRequest(MonitorResourceType.VM_NETWORK_INCOMING,
                        System.currentTimeMillis() -
                                FlexibleEngineMonitorConstants.ONE_DAY_MILLISECONDS,
                        System.currentTimeMillis(), false);
        mockAllRequestForService();

        // Run the test
        List<Metric> metrics = plugin.getMetricsForService(serviceMetricRequest);

        // Verify the results
        Assertions.assertEquals(1, metrics.size());
        Assertions.assertEquals(MonitorResourceType.VM_NETWORK_INCOMING,
                metrics.get(0).getMonitorResourceType());
        Assertions.assertEquals(4, metrics.get(0).getMetrics().size());
    }

    @Test
    void testGetMetricsForServiceWithParamsTypeVmNetworkOutgoing() throws IOException {
        // Setup
        ServiceMetricRequest serviceMetricRequest =
                setUpServiceMetricRequest(MonitorResourceType.VM_NETWORK_OUTGOING,
                        System.currentTimeMillis() -
                                FlexibleEngineMonitorConstants.ONE_DAY_MILLISECONDS,
                        System.currentTimeMillis(), false);
        mockAllRequestForService();

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

    private ListMetricsResponse getListMetricsResponse() throws IOException {
        return objectMapper.readValue(
                new URL("file:./target/test-classes/results/query_metrics_list.json"),
                ListMetricsResponse.class);
    }

    private KeystoneListProjectsResponse getProjectResponse() throws IOException {
        return objectMapper.readValue(
                new URL("file:./target/test-classes/results/query_project_info.json"),
                KeystoneListProjectsResponse.class);
    }

    private BatchListMetricDataResponse getBatchListMetricDataResponse() throws IOException {
        return objectMapper.readValue(
                new URL("file:./target/test-classes/results/batch_query_metric_data.json"),
                BatchListMetricDataResponse.class);
    }

    private ShowMetricDataResponse getShowMetricDataResponse(MonitorResourceType type)
            throws IOException {

        if (MonitorResourceType.MEM == type) {
            return objectMapper.readValue(
                    new URL("file:./target/test-classes/results/query_metric_data_mem.json"),
                    ShowMetricDataResponse.class);
        }
        if (MonitorResourceType.VM_NETWORK_INCOMING == type) {
            return objectMapper.readValue(
                    new URL("file:./target/test-classes/results/query_metric_data_net_in.json"),
                    ShowMetricDataResponse.class);
        }
        if (MonitorResourceType.VM_NETWORK_OUTGOING == type) {
            return objectMapper.readValue(
                    new URL("file:./target/test-classes/results/query_metric_data_net_out.json"),
                    ShowMetricDataResponse.class);
        }
        return objectMapper.readValue(
                new URL("file:./target/test-classes/results/query_metric_data_cpu.json"),
                ShowMetricDataResponse.class);

    }

    private HttpRequestBase getHttpRequestBase(String url,
                                               String httpMethod) throws URISyntaxException {
        HttpRequestBase httpRequestBase = new HttpRequestBase() {
            @Override
            public String getMethod() {
                return httpMethod;
            }
        };
        httpRequestBase.setURI(new URI(url));
        httpRequestBase.setHeader("Content-Type", "application/json");
        return httpRequestBase;
    }

}
