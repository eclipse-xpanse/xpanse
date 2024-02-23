package org.eclipse.xpanse.plugins.flexibleengine.monitor;

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
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.monitor.Metric;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.monitor.ServiceMetricsStore;
import org.eclipse.xpanse.modules.monitor.cache.ServiceMetricsCacheManager;
import org.eclipse.xpanse.modules.orchestrator.monitor.ResourceMetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ServiceMetricsRequest;
import org.eclipse.xpanse.plugins.flexibleengine.FlexibleEngineOrchestratorPlugin;
import org.eclipse.xpanse.plugins.flexibleengine.common.FlexibleEngineClient;
import org.eclipse.xpanse.plugins.flexibleengine.common.FlexibleEngineConstants;
import org.eclipse.xpanse.plugins.flexibleengine.manage.FlexibleEngineResourceManager;
import org.eclipse.xpanse.plugins.flexibleengine.manage.FlexibleEngineServerManageRequestConverter;
import org.eclipse.xpanse.plugins.flexibleengine.manage.FlexibleEngineVmStateManager;
import org.eclipse.xpanse.plugins.flexibleengine.monitor.constant.FlexibleEngineMonitorConstants;
import org.eclipse.xpanse.plugins.flexibleengine.resourcehandler.FlexibleEngineTerraformResourceHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {FlexibleEngineOrchestratorPlugin.class,
        FlexibleEngineVmStateManager.class, FlexibleEngineServerManageRequestConverter.class,
        FlexibleEngineMetricsService.class, FlexibleEngineClient.class,
        FlexibleEngineMonitorConstants.class, FlexibleEngineDataModelConverter.class,
        CredentialCenter.class, ServiceMetricsStore.class, ServiceMetricsCacheManager.class,
        FlexibleEngineTerraformResourceHandler.class, FlexibleEngineResourceManager.class})
class FlexibleEngineMonitorIntegrationTest {

    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension.newInstance().options(
            wireMockConfig().dynamicPort().extensions(
                    new ResponseTemplateTransformer(TemplateEngine.defaultTemplateEngine(), false,
                            new ClasspathFileSource("src/test/resources/mappings"),
                            Collections.emptyList()))).build();

    @Autowired
    FlexibleEngineOrchestratorPlugin plugin;

    @MockBean
    CredentialCenter credentialCenter;

    @MockBean
    FlexibleEngineClient client;

    ResourceMetricsRequest setUpResourceMetricRequest(MonitorResourceType monitorResourceType,
                                                      Long from, Long to,
                                                      boolean onlyLastKnownMetric) {
        final DeployResource deployResource = new DeployResource();
        deployResource.setResourceId("ca0f0cf6-16ef-4e7e-bb39-419d7791d3fd");
        deployResource.setName("name");
        deployResource.setKind(DeployResourceKind.VM);
        deployResource.setProperties(Map.ofEntries(Map.entry("region", "eu-west-0")));
        return new ResourceMetricsRequest(UUID.randomUUID(), deployResource, monitorResourceType, from, to, null,
                onlyLastKnownMetric, "userId");

    }

    void mockClientHttpRequest() throws Exception {
        when(this.credentialCenter.getCredential(any(), any(), any())).thenReturn(
                getCredentialDefinition());
        when(this.client.getCesClient(any(), any())).thenReturn(getCesClient());
        when(this.client.getCredential(any())).thenReturn(getCredential());
    }


    ServiceMetricsRequest setUpServiceMetricRequest(MonitorResourceType monitorResourceType,
                                                    Long from, Long to,
                                                    boolean onlyLastKnownMetric) {
        final DeployResource deployResource = new DeployResource();
        deployResource.setResourceId("ca0f0cf6-16ef-4e7e-bb39-419d7791d3fd");
        deployResource.setName("name");
        deployResource.setKind(DeployResourceKind.VM);
        deployResource.setProperties(Map.ofEntries(Map.entry("region", "eu-west-0")));
        return new ServiceMetricsRequest(UUID.randomUUID(), List.of(deployResource), monitorResourceType, from, to,
                null, onlyLastKnownMetric, "userId");
    }


    @Test
    void testGetMetricsForResourceWithParamsOnlyLastKnownMetricTrue() throws Exception {

        // Setup
        ResourceMetricsRequest resourceMetricRequest =
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
        ResourceMetricsRequest resourceMetricRequest = setUpResourceMetricRequest(null,
                System.currentTimeMillis() - FlexibleEngineMonitorConstants.ONE_DAY_MILLISECONDS,
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
        ResourceMetricsRequest resourceMetricRequest =
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
        ResourceMetricsRequest resourceMetricRequest =
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
        ResourceMetricsRequest resourceMetricRequest =
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
        ResourceMetricsRequest resourceMetricRequest =
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
        ServiceMetricsRequest serviceMetricRequest =
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
        ServiceMetricsRequest serviceMetricRequest = setUpServiceMetricRequest(null,
                System.currentTimeMillis() - FlexibleEngineMonitorConstants.ONE_DAY_MILLISECONDS,
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
        ServiceMetricsRequest serviceMetricRequest =
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
        ServiceMetricsRequest serviceMetricRequest =
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
        ServiceMetricsRequest serviceMetricRequest =
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
        ServiceMetricsRequest serviceMetricRequest =
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
            if (credentialVariable.getName().equals(FlexibleEngineConstants.OS_ACCESS_KEY)) {
                credentialVariable.setValue(FlexibleEngineConstants.OS_ACCESS_KEY);
            }
            if (credentialVariable.getName().equals(FlexibleEngineConstants.OS_SECRET_KEY)) {
                credentialVariable.setValue(FlexibleEngineConstants.OS_SECRET_KEY);
            }
        }
        return credentialVariables;
    }


    private CesClient getCesClient() {
        ICredential iCredential =
                new BasicCredentials().withAk(FlexibleEngineMonitorConstants.OS_ACCESS_KEY)
                        .withSk(FlexibleEngineMonitorConstants.OS_SECRET_KEY);
        HcClient hcClient = new HcClient(HttpConfig.getDefaultHttpConfig());
        hcClient.withCredential(iCredential);
        hcClient.withEndpoints(
                Collections.singletonList(wireMockExtension.getRuntimeInfo().getHttpBaseUrl()));
        return new CesClient(hcClient);
    }

    private ICredential getCredential() {
        return new BasicCredentials().withAk(FlexibleEngineMonitorConstants.OS_ACCESS_KEY)
                .withSk(FlexibleEngineMonitorConstants.OS_SECRET_KEY);
    }

}
