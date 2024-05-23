/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.plugins.openstack.monitor;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.common.ClasspathFileSource;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.extension.responsetemplating.TemplateEngine;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.database.service.DatabaseDeployServiceStorage;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.deployment.utils.DeployEnvironments;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.exceptions.CredentialsNotFoundException;
import org.eclipse.xpanse.modules.models.monitor.Metric;
import org.eclipse.xpanse.modules.models.monitor.enums.MetricType;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.servicetemplate.DeployVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.Deployment;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.monitor.ServiceMetricsStore;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.monitor.ResourceMetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ServiceMetricsRequest;
import org.eclipse.xpanse.modules.security.common.AesUtil;
import org.eclipse.xpanse.plugins.openstack.OpenstackOrchestratorPlugin;
import org.eclipse.xpanse.plugins.openstack.common.constants.OpenstackEnvironmentConstants;
import org.eclipse.xpanse.plugins.openstack.common.keystone.KeystoneManager;
import org.eclipse.xpanse.plugins.openstack.manage.OpenstackResourceManager;
import org.eclipse.xpanse.plugins.openstack.manage.OpenstackServersManager;
import org.eclipse.xpanse.plugins.openstack.monitor.gnocchi.api.AggregationService;
import org.eclipse.xpanse.plugins.openstack.monitor.gnocchi.api.MeasuresService;
import org.eclipse.xpanse.plugins.openstack.monitor.gnocchi.api.ResourcesService;
import org.eclipse.xpanse.plugins.openstack.monitor.gnocchi.utils.GnocchiToXpanseModelConverter;
import org.eclipse.xpanse.plugins.openstack.monitor.gnocchi.utils.MetricsQueryBuilder;
import org.eclipse.xpanse.plugins.openstack.price.OpenstackPriceCalculator;
import org.eclipse.xpanse.plugins.openstack.resourcehandler.OpenstackTerraformResourceHandler;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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
        CredentialCenter.class, ServiceMetricsStore.class, OpenstackTerraformResourceHandler.class,
        DeployEnvironments.class, AesUtil.class, PluginManager.class, ServiceTemplateStorage.class,
        OpenstackResourceManager.class, OpenstackPriceCalculator.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OpenstackMonitoringIntegrationTest {

    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().extensions(
                    new ResponseTemplateTransformer(TemplateEngine.defaultTemplateEngine(),
                            false, new ClasspathFileSource("src/test/resources/mappings"),
                            Collections.emptyList()))).build();
    @Autowired
    OpenstackOrchestratorPlugin plugin;
    @MockBean
    CredentialCenter credentialCenter;
    @MockBean
    ServiceMetricsStore serviceMetricsStore;
    @MockBean
    OpenstackServersManager openstackServersManager;
    @MockBean
    private DatabaseDeployServiceStorage databaseDeployServiceStorage;
    @MockBean
    private ServiceTemplateStorage serviceTemplateStorage;
    @MockBean
    private DeployEnvironments deployEnvironments;

    @BeforeAll
    void setEnvVar() {
        System.setProperty(OpenstackEnvironmentConstants.AUTH_URL,
                wireMockExtension.getRuntimeInfo().getHttpBaseUrl() + "/identity/v3");
    }

    public ResourceMetricsRequest setupResourceRequest(Long from, Long to, Integer period,
                                                       boolean onlyLastKnownMetric) {
        return new ResourceMetricsRequest(UUID.randomUUID(),
                Instancio.of(DeployResource.class).set(Select.field(DeployResource::getKind),
                        DeployResourceKind.VM).set(Select.field(DeployResource::getResourceId),
                        "7b5b6ee6-cab4-4e72-be6e-854a67c6d381").create(),
                null, from, to, period, onlyLastKnownMetric, "user");
    }

    public ServiceMetricsRequest setupServiceRequest(Long from, Long to, Integer period,
                                                     boolean onlyLastKnownMetric) {
        return new ServiceMetricsRequest(UUID.randomUUID(),
                List.of(Instancio.of(DeployResource.class)
                        .set(Select.field(DeployResource::getKind),
                                DeployResourceKind.VM)
                        .set(Select.field(DeployResource::getResourceId),
                                "7b5b6ee6-cab4-4e72-be6e-854a67c6d381").create()),
                null, from, to, period, onlyLastKnownMetric, "user");
    }

    @Test
    void testGetMetricsHappyCase() {
        when(this.credentialCenter.getCredential(any(), any(), any())).thenReturn(
                getCredentialDefinition());
        DeployServiceEntity deployServiceEntity = new DeployServiceEntity();
        DeployRequest deployRequest = new DeployRequest();
        deployServiceEntity.setDeployRequest(deployRequest);
        deployServiceEntity.setServiceTemplateId(UUID.randomUUID());
        ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        Ocl ocl = new Ocl();
        Deployment deployment = new Deployment();
        List<DeployVariable> variables = new ArrayList<>();
        deployment.setVariables(variables);
        ocl.setDeployment(deployment);
        serviceTemplateEntity.setOcl(ocl);
        when(this.databaseDeployServiceStorage.findDeployServiceById(any())).thenReturn(
                deployServiceEntity);
        when(this.serviceTemplateStorage.getServiceTemplateById(any())).thenReturn(
                serviceTemplateEntity);
        when(this.deployEnvironments.getAllDeploymentVariablesForService(
                any(), any(), any(), any())).thenReturn(
                Map.of("OS_AUTH_URL", wireMockExtension.baseUrl() + "/identity/v3"));
        List<Metric> metrics =
                this.plugin.getMetricsForResource(setupResourceRequest(null, null, null, false));
        Assertions.assertFalse(metrics.isEmpty());
        Assertions.assertEquals(4, metrics.size());
        Assertions.assertEquals(MetricType.GAUGE, metrics.getFirst().getType());
        Assertions.assertEquals(MetricType.GAUGE, metrics.get(1).getType());
        Assertions.assertEquals(MonitorResourceType.CPU.toValue(), metrics.getFirst().getName());
        Assertions.assertEquals(MonitorResourceType.MEM.toValue(), metrics.get(1).getName());
        Assertions.assertEquals(MonitorResourceType.VM_NETWORK_INCOMING.toValue(),
                metrics.get(2).getName());
        Assertions.assertEquals(MonitorResourceType.VM_NETWORK_OUTGOING.toValue(),
                metrics.get(3).getName());
        Assertions.assertEquals(326, metrics.getFirst().getMetrics().size());
    }

    @Test
    void testGetMetricsWithFromAndTo() {
        when(this.credentialCenter.getCredential(any(), any(), any())).thenReturn(
                getCredentialDefinition());
        DeployServiceEntity deployServiceEntity = new DeployServiceEntity();
        DeployRequest deployRequest = new DeployRequest();
        deployServiceEntity.setDeployRequest(deployRequest);
        deployServiceEntity.setServiceTemplateId(UUID.randomUUID());
        ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        Ocl ocl = new Ocl();
        Deployment deployment = new Deployment();
        List<DeployVariable> variables = new ArrayList<>();
        deployment.setVariables(variables);
        ocl.setDeployment(deployment);
        serviceTemplateEntity.setOcl(ocl);
        when(this.databaseDeployServiceStorage.findDeployServiceById(any())).thenReturn(
                deployServiceEntity);
        when(this.serviceTemplateStorage.getServiceTemplateById(any())).thenReturn(
                serviceTemplateEntity);
        when(this.deployEnvironments.getAllDeploymentVariablesForService(
                any(), any(), any(), any())).thenReturn(
                Map.of("OS_AUTH_URL", wireMockExtension.baseUrl() + "/identity/v3"));
        Long currentTime = Instant.now().getEpochSecond();
        List<Metric> metrics =
                this.plugin.getMetricsForResource(
                        setupResourceRequest(currentTime, currentTime, null, false));
        Assertions.assertFalse(metrics.isEmpty());
        Assertions.assertEquals(4, metrics.size());
        Assertions.assertEquals(MetricType.GAUGE, metrics.getFirst().getType());
        Assertions.assertEquals(MetricType.GAUGE, metrics.get(1).getType());
        Assertions.assertEquals(MonitorResourceType.CPU.toValue(), metrics.getFirst().getName());
        Assertions.assertEquals(MonitorResourceType.MEM.toValue(), metrics.get(1).getName());
        Assertions.assertEquals(326, metrics.getFirst().getMetrics().size());
        wireMockExtension.verify(3, postRequestedFor(
                urlEqualTo("/metric/v1/aggregates?start=" + currentTime + "&end=" + currentTime)));


    }

    @Test
    void testGetMetricsWithGranularity() {
        when(this.credentialCenter.getCredential(any(), any(), any())).thenReturn(
                getCredentialDefinition());
        DeployServiceEntity deployServiceEntity = new DeployServiceEntity();
        DeployRequest deployRequest = new DeployRequest();
        deployServiceEntity.setDeployRequest(deployRequest);
        deployServiceEntity.setServiceTemplateId(UUID.randomUUID());
        ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        Ocl ocl = new Ocl();
        Deployment deployment = new Deployment();
        List<DeployVariable> variables = new ArrayList<>();
        deployment.setVariables(variables);
        ocl.setDeployment(deployment);
        serviceTemplateEntity.setOcl(ocl);
        when(this.databaseDeployServiceStorage.findDeployServiceById(any())).thenReturn(
                deployServiceEntity);
        when(this.serviceTemplateStorage.getServiceTemplateById(any())).thenReturn(
                serviceTemplateEntity);
        when(this.deployEnvironments.getAllDeploymentVariablesForService(
                any(), any(), any(), any())).thenReturn(
                Map.of("OS_AUTH_URL", wireMockExtension.baseUrl() + "/identity/v3"));
        List<Metric> metrics =
                this.plugin.getMetricsForResource(setupResourceRequest(null, null, 150, false));
        Assertions.assertFalse(metrics.isEmpty());
        Assertions.assertEquals(4, metrics.size());
        Assertions.assertEquals(MetricType.GAUGE, metrics.getFirst().getType());
        Assertions.assertEquals(MetricType.GAUGE, metrics.get(1).getType());
        Assertions.assertEquals(MonitorResourceType.CPU.toValue(), metrics.getFirst().getName());
        Assertions.assertEquals(MonitorResourceType.MEM.toValue(), metrics.get(1).getName());
        Assertions.assertEquals(326, metrics.getFirst().getMetrics().size());
    }

    @Test
    void testGetOnlyLastKnownMetric() {
        when(this.credentialCenter.getCredential(any(), any(), any())).thenReturn(
                getCredentialDefinition());
        DeployServiceEntity deployServiceEntity = new DeployServiceEntity();
        DeployRequest deployRequest = new DeployRequest();
        deployServiceEntity.setDeployRequest(deployRequest);
        deployServiceEntity.setServiceTemplateId(UUID.randomUUID());
        ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        Ocl ocl = new Ocl();
        Deployment deployment = new Deployment();
        List<DeployVariable> variables = new ArrayList<>();
        deployment.setVariables(variables);
        ocl.setDeployment(deployment);
        serviceTemplateEntity.setOcl(ocl);
        when(this.databaseDeployServiceStorage.findDeployServiceById(any())).thenReturn(
                deployServiceEntity);
        when(this.serviceTemplateStorage.getServiceTemplateById(any())).thenReturn(
                serviceTemplateEntity);
        when(this.deployEnvironments.getAllDeploymentVariablesForService(
                any(), any(), any(), any())).thenReturn(
                Map.of("OS_AUTH_URL", wireMockExtension.baseUrl() + "/identity/v3"));
        List<Metric> metrics =
                this.plugin.getMetricsForResource(setupResourceRequest(null, null, 150, true));
        Assertions.assertFalse(metrics.isEmpty());
        Assertions.assertEquals(4, metrics.size());
        Assertions.assertEquals(MetricType.GAUGE, metrics.getFirst().getType());
        Assertions.assertEquals(MetricType.GAUGE, metrics.get(1).getType());
        Assertions.assertEquals(MonitorResourceType.CPU.toValue(), metrics.getFirst().getName());
        Assertions.assertEquals(MonitorResourceType.MEM.toValue(), metrics.get(1).getName());
        Assertions.assertEquals(1, metrics.getFirst().getMetrics().size());
        Assertions.assertEquals(1, metrics.get(1).getMetrics().size());
    }


    @Test
    void testGetMetricsForServiceOnlyLastKnownMetric() {
        when(this.credentialCenter.getCredential(any(), any(), any())).thenReturn(
                getCredentialDefinition());
        DeployServiceEntity deployServiceEntity = new DeployServiceEntity();
        DeployRequest deployRequest = new DeployRequest();
        deployServiceEntity.setDeployRequest(deployRequest);
        deployServiceEntity.setServiceTemplateId(UUID.randomUUID());
        ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        Ocl ocl = new Ocl();
        Deployment deployment = new Deployment();
        List<DeployVariable> variables = new ArrayList<>();
        deployment.setVariables(variables);
        ocl.setDeployment(deployment);
        serviceTemplateEntity.setOcl(ocl);
        when(this.databaseDeployServiceStorage.findDeployServiceById(any())).thenReturn(
                deployServiceEntity);
        when(this.serviceTemplateStorage.getServiceTemplateById(any())).thenReturn(
                serviceTemplateEntity);
        when(this.deployEnvironments.getAllDeploymentVariablesForService(
                any(), any(), any(), any())).thenReturn(
                Map.of("OS_AUTH_URL", wireMockExtension.baseUrl() + "/identity/v3"));
        List<Metric> metrics =
                this.plugin.getMetricsForService(setupServiceRequest(null, null, 150, true));
        Assertions.assertFalse(metrics.isEmpty());
        Assertions.assertEquals(4, metrics.size());
        Assertions.assertEquals(MetricType.GAUGE, metrics.getFirst().getType());
        Assertions.assertEquals(MetricType.GAUGE, metrics.get(1).getType());
        Assertions.assertEquals(MonitorResourceType.CPU.toValue(), metrics.getFirst().getName());
        Assertions.assertEquals(MonitorResourceType.MEM.toValue(), metrics.get(1).getName());
        Assertions.assertEquals(1, metrics.getFirst().getMetrics().size());
        Assertions.assertEquals(1, metrics.get(1).getMetrics().size());
    }

    @Test
    void testGetMetricsException() {
        when(this.credentialCenter.getCredential(any(), any(), any())).thenReturn(
                this.plugin.getCredentialDefinitions().getFirst());
        Assertions.assertThrows(CredentialsNotFoundException.class,
                () -> this.plugin.getMetricsForResource(
                        setupResourceRequest(null, null, 150, true)));
    }


    private CredentialVariables getCredentialDefinition() {
        CredentialVariables credentialVariables =
                (CredentialVariables) this.plugin.getCredentialDefinitions().getFirst();
        for (CredentialVariable credentialVariable : credentialVariables.getVariables()) {
            if (credentialVariable.getName().equals(OpenstackEnvironmentConstants.USERNAME)) {
                credentialVariable.setValue("admin");
            }
            if (credentialVariable.getName().equals(OpenstackEnvironmentConstants.PASSWORD)) {
                credentialVariable.setValue("test");
            }
            if (credentialVariable.getName().equals(OpenstackEnvironmentConstants.USER_DOMAIN)) {
                credentialVariable.setValue("default");
            }
            if (credentialVariable.getName().equals(OpenstackEnvironmentConstants.PROJECT)) {
                credentialVariable.setValue("service");
            }
        }

        return credentialVariables;
    }

}
