/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.plugins.openstack;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import java.time.Instant;
import java.util.List;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.monitor.Metric;
import org.eclipse.xpanse.modules.models.monitor.enums.MetricType;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.orchestrator.monitor.ResourceMetricRequest;
import org.eclipse.xpanse.plugins.openstack.constants.OpenstackEnvironmentConstants;
import org.eclipse.xpanse.plugins.openstack.monitor.gnocchi.api.AggregationService;
import org.eclipse.xpanse.plugins.openstack.monitor.gnocchi.api.MeasuresService;
import org.eclipse.xpanse.plugins.openstack.monitor.gnocchi.api.ResourcesService;
import org.eclipse.xpanse.plugins.openstack.monitor.gnocchi.api.utils.MetricsQueryBuilder;
import org.eclipse.xpanse.plugins.openstack.monitor.keystone.KeystoneManager;
import org.eclipse.xpanse.plugins.openstack.monitor.utils.GnocchiToXpanseModelConverter;
import org.eclipse.xpanse.plugins.openstack.monitor.utils.MetricsManager;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {OpenstackOrchestratorPlugin.class, MetricsManager.class,
        KeystoneManager.class, ResourcesService.class, GnocchiToXpanseModelConverter.class,
        AggregationService.class, MeasuresService.class, MetricsQueryBuilder.class,
        CredentialCenter.class})
class OpenstackMonitoringIntegrationTest {

    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
            .options(wireMockConfig()
                    .dynamicPort()
                    .extensions(new ResponseTemplateTransformer(true)))
            .build();

    @Autowired
    OpenstackOrchestratorPlugin plugin;

    @MockBean
    CredentialCenter credentialCenter;

    public ResourceMetricRequest setupResourceRequest(Long from, Long to, Integer period,
                                                      boolean onlyLastKnownMetric) {
        return new ResourceMetricRequest(
                Instancio.of(DeployResource.class).set(Select.field(DeployResource::getKind),
                        DeployResourceKind.VM).set(Select.field(DeployResource::getResourceId),
                        "7b5b6ee6-cab4-4e72-be6e-854a67c6d381").create(),
                null, from, to, period, onlyLastKnownMetric, "user");
    }

    @Test
    void testGetMetricsHappyCase() {
        when(this.credentialCenter.getCredential(any(), any(), any())).thenReturn(
                getCredentialDefinition(wireMockExtension.getRuntimeInfo()));
        List<Metric> metrics =
                this.plugin.getMetrics(setupResourceRequest(null, null, null, false));
        Assertions.assertFalse(metrics.isEmpty());
        Assertions.assertEquals(4, metrics.size());
        Assertions.assertEquals(MetricType.GAUGE, metrics.get(0).getType());
        Assertions.assertEquals(MetricType.GAUGE, metrics.get(1).getType());
        Assertions.assertEquals(MonitorResourceType.CPU.toValue(), metrics.get(0).getName());
        Assertions.assertEquals(MonitorResourceType.MEM.toValue(), metrics.get(1).getName());
        Assertions.assertEquals(MonitorResourceType.VM_NETWORK_INCOMING.toValue(),
                metrics.get(2).getName());
        Assertions.assertEquals(MonitorResourceType.VM_NETWORK_OUTGOING.toValue(),
                metrics.get(3).getName());
        Assertions.assertEquals(326, metrics.get(0).getMetrics().size());
    }

    @Test
    void testGetMetricsWithFromAndTo() {
        when(this.credentialCenter.getCredential(any(), any(), any())).thenReturn(
                getCredentialDefinition(wireMockExtension.getRuntimeInfo()));
        Long currentTime = Instant.now().getEpochSecond();
        List<Metric> metrics =
                this.plugin.getMetrics(setupResourceRequest(currentTime, currentTime, null, false));
        Assertions.assertFalse(metrics.isEmpty());
        Assertions.assertEquals(4, metrics.size());
        Assertions.assertEquals(MetricType.GAUGE, metrics.get(0).getType());
        Assertions.assertEquals(MetricType.GAUGE, metrics.get(1).getType());
        Assertions.assertEquals(MonitorResourceType.CPU.toValue(), metrics.get(0).getName());
        Assertions.assertEquals(MonitorResourceType.MEM.toValue(), metrics.get(1).getName());
        Assertions.assertEquals(326, metrics.get(0).getMetrics().size());
        wireMockExtension.verify(3, postRequestedFor(
                urlEqualTo("/metric/v1/aggregates?start=" + currentTime + "&end=" + currentTime)));


    }

    @Test
    void testGetMetricsWithGranularity() {
        when(this.credentialCenter.getCredential(any(), any(), any())).thenReturn(
                getCredentialDefinition(wireMockExtension.getRuntimeInfo()));
        List<Metric> metrics = this.plugin.getMetrics(setupResourceRequest(null, null, 150, false));
        Assertions.assertFalse(metrics.isEmpty());
        Assertions.assertEquals(4, metrics.size());
        Assertions.assertEquals(MetricType.GAUGE, metrics.get(0).getType());
        Assertions.assertEquals(MetricType.GAUGE, metrics.get(1).getType());
        Assertions.assertEquals(MonitorResourceType.CPU.toValue(), metrics.get(0).getName());
        Assertions.assertEquals(MonitorResourceType.MEM.toValue(), metrics.get(1).getName());
        Assertions.assertEquals(326, metrics.get(0).getMetrics().size());
    }

    @Test
    void testGetOnlyLastKnownMetric() {
        when(this.credentialCenter.getCredential(any(), any(), any())).thenReturn(
                getCredentialDefinition(wireMockExtension.getRuntimeInfo()));
        List<Metric> metrics = this.plugin.getMetrics(setupResourceRequest(null, null, 150, true));
        Assertions.assertFalse(metrics.isEmpty());
        Assertions.assertEquals(4, metrics.size());
        Assertions.assertEquals(MetricType.GAUGE, metrics.get(0).getType());
        Assertions.assertEquals(MetricType.GAUGE, metrics.get(1).getType());
        Assertions.assertEquals(MonitorResourceType.CPU.toValue(), metrics.get(0).getName());
        Assertions.assertEquals(MonitorResourceType.MEM.toValue(), metrics.get(1).getName());
        Assertions.assertEquals(1, metrics.get(0).getMetrics().size());
        Assertions.assertEquals(1, metrics.get(1).getMetrics().size());
    }

    private CredentialVariables getCredentialDefinition(WireMockRuntimeInfo wmRuntimeInfo) {
        CredentialVariables credentialVariables =
                (CredentialVariables) this.plugin.getCredentialDefinitions().get(0);
        for (CredentialVariable credentialVariable : credentialVariables.getVariables()) {
            if (credentialVariable.getName().equals(OpenstackEnvironmentConstants.AUTH_URL)) {
                credentialVariable.setValue(wmRuntimeInfo.getHttpBaseUrl() + "/identity/v3");
            }
            if (credentialVariable.getName().equals(OpenstackEnvironmentConstants.USERNAME)) {
                credentialVariable.setValue("admin");
            }
            if (credentialVariable.getName().equals(OpenstackEnvironmentConstants.PASSWORD)) {
                credentialVariable.setValue("test");
            }
            if (credentialVariable.getName().equals(OpenstackEnvironmentConstants.DOMAIN)) {
                credentialVariable.setValue("default");
            }
            if (credentialVariable.getName().equals(OpenstackEnvironmentConstants.TENANT)) {
                credentialVariable.setValue("service");
            }
        }

        return credentialVariables;
    }

}
