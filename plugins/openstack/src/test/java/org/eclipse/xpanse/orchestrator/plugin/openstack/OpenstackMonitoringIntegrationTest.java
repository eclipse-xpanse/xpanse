/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.orchestrator.plugin.openstack;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.xpanse.modules.credential.CredentialDefinition;
import org.eclipse.xpanse.modules.credential.CredentialVariable;
import org.eclipse.xpanse.modules.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.modules.models.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.service.DeployResource;
import org.eclipse.xpanse.modules.monitor.Metric;
import org.eclipse.xpanse.modules.monitor.ResourceMetricRequest;
import org.eclipse.xpanse.modules.monitor.enums.MetricType;
import org.eclipse.xpanse.modules.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.orchestrator.plugin.openstack.constants.OpenstackEnvironmentConstants;
import org.eclipse.xpanse.orchestrator.plugin.openstack.monitor.gnocchi.api.AggregationService;
import org.eclipse.xpanse.orchestrator.plugin.openstack.monitor.gnocchi.api.MeasuresService;
import org.eclipse.xpanse.orchestrator.plugin.openstack.monitor.gnocchi.api.ResourcesService;
import org.eclipse.xpanse.orchestrator.plugin.openstack.monitor.gnocchi.api.utils.MetricsQueryBuilder;
import org.eclipse.xpanse.orchestrator.plugin.openstack.monitor.keystone.KeystoneManager;
import org.eclipse.xpanse.orchestrator.plugin.openstack.monitor.utils.GnocchiToXpanseModelConverter;
import org.eclipse.xpanse.orchestrator.plugin.openstack.monitor.utils.MetricsManager;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {OpenstackOrchestratorPlugin.class, MetricsManager.class,
        KeystoneManager.class, ResourcesService.class, GnocchiToXpanseModelConverter.class,
        AggregationService.class, MeasuresService.class, MetricsQueryBuilder.class})
public class OpenstackMonitoringIntegrationTest {

    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
            .options(wireMockConfig()
                    .dynamicPort()
                    .extensions(new ResponseTemplateTransformer(true)))
            .build();

    @Autowired
    OpenstackOrchestratorPlugin plugin;

    public ResourceMetricRequest setupResourceRequest(
            WireMockRuntimeInfo wmRuntimeInfo, Long from, Long to, Integer period) {
        return new ResourceMetricRequest(
                Instancio.of(DeployResource.class).set(Select.field(DeployResource::getKind),
                        DeployResourceKind.VM).set(Select.field(DeployResource::getResourceId), "7b5b6ee6-cab4-4e72-be6e-854a67c6d381").create(), getCredentialDefinition(wmRuntimeInfo),
                null, from, to, period);
    }

    @Test
    public void testGetMetricsHappyCase() {
        List<Metric> metrics = this.plugin.getMetrics(setupResourceRequest(
                wireMockExtension.getRuntimeInfo(), null, null, null));
        Assertions.assertFalse(metrics.isEmpty());
        Assertions.assertEquals(4, metrics.size());
        Assertions.assertEquals(MetricType.GAUGE, metrics.get(0).getType());
        Assertions.assertEquals(MetricType.GAUGE, metrics.get(1).getType());
        Assertions.assertEquals(MonitorResourceType.CPU.toValue(), metrics.get(0).getName());
        Assertions.assertEquals(MonitorResourceType.MEM.toValue(), metrics.get(1).getName());
        Assertions.assertEquals(MonitorResourceType.VM_NETWORK_INCOMING.toValue(), metrics.get(2).getName());
        Assertions.assertEquals(MonitorResourceType.VM_NETWORK_OUTGOING.toValue(), metrics.get(3).getName());
        Assertions.assertEquals(326, metrics.get(0).getMetrics().size());
    }

    @Test
    public void testGetMetricsWithFromAndTo() {
        Long currentTime = Instant.now().getEpochSecond();
        List<Metric> metrics = this.plugin.getMetrics(setupResourceRequest(
                wireMockExtension.getRuntimeInfo(), currentTime, currentTime, null));
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
    public void testGetMetricsWithGranularity() {
        List<Metric> metrics = this.plugin.getMetrics(setupResourceRequest(
                wireMockExtension.getRuntimeInfo(), null, null, 150));
        Assertions.assertFalse(metrics.isEmpty());
        Assertions.assertEquals(4, metrics.size());
        Assertions.assertEquals(MetricType.GAUGE, metrics.get(0).getType());
        Assertions.assertEquals(MetricType.GAUGE, metrics.get(1).getType());
        Assertions.assertEquals(MonitorResourceType.CPU.toValue(), metrics.get(0).getName());
        Assertions.assertEquals(MonitorResourceType.MEM.toValue(), metrics.get(1).getName());
        Assertions.assertEquals(326, metrics.get(0).getMetrics().size());
    }

    private CredentialDefinition getCredentialDefinition(WireMockRuntimeInfo wmRuntimeInfo) {
        List<CredentialVariable> credentialVariables = new ArrayList<>();

        CredentialVariable url = new CredentialVariable(OpenstackEnvironmentConstants.AUTH_URL, "");
        url.setValue(wmRuntimeInfo.getHttpBaseUrl() + "/identity/v3");
        credentialVariables.add(url);

        CredentialVariable username =
                new CredentialVariable(OpenstackEnvironmentConstants.USERNAME, "");
        username.setValue("admin");
        credentialVariables.add(username);

        CredentialVariable password =
                new CredentialVariable(OpenstackEnvironmentConstants.PASSWORD, "");
        password.setValue("StrongAdminSecret");
        credentialVariables.add(password);

        CredentialVariable domain =
                new CredentialVariable(OpenstackEnvironmentConstants.DOMAIN, "");
        domain.setValue("default");
        credentialVariables.add(domain);

        CredentialVariable tenant =
                new CredentialVariable(OpenstackEnvironmentConstants.TENANT, "");
        tenant.setValue("service");
        credentialVariables.add(tenant);

        return new CredentialDefinition(Csp.OPENSTACK, "", "", CredentialType.VARIABLES,
                credentialVariables);
    }

}
