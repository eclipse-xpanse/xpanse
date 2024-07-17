/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.plugins.openstacktestlab;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.xpanse.plugins.openstack.common.auth.constants.OpenstackCommonEnvironmentConstants.OPENSTACK_TESTLAB_AUTH_URL;
import static org.eclipse.xpanse.plugins.openstack.common.auth.constants.OpenstackCommonEnvironmentConstants.OS_AUTH_URL;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.common.ClasspathFileSource;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.extension.responsetemplating.TemplateEngine;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.database.resource.DeployResourceEntity;
import org.eclipse.xpanse.modules.database.service.DatabaseDeployServiceStorage;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.deployment.utils.DeployEnvironments;
import org.eclipse.xpanse.modules.models.billing.FlavorPriceResult;
import org.eclipse.xpanse.modules.models.billing.Price;
import org.eclipse.xpanse.modules.models.billing.enums.BillingMode;
import org.eclipse.xpanse.modules.models.billing.enums.Currency;
import org.eclipse.xpanse.modules.models.billing.enums.PricingPeriod;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.monitor.Metric;
import org.eclipse.xpanse.modules.models.monitor.enums.MetricType;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.servicetemplate.DeployVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.Deployment;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.monitor.ServiceMetricsStore;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.monitor.ResourceMetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ServiceMetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.price.ServiceFlavorPriceRequest;
import org.eclipse.xpanse.modules.orchestrator.servicestate.ServiceStateManageRequest;
import org.eclipse.xpanse.modules.security.common.AesUtil;
import org.eclipse.xpanse.plugins.openstack.common.auth.ProviderAuthInfoResolver;
import org.eclipse.xpanse.plugins.openstack.common.auth.constants.OpenstackCommonEnvironmentConstants;
import org.eclipse.xpanse.plugins.openstack.common.auth.keystone.OpenstackKeystoneManager;
import org.eclipse.xpanse.plugins.openstack.common.auth.keystone.ScsKeystoneManager;
import org.eclipse.xpanse.plugins.openstack.common.manage.OpenstackResourceManager;
import org.eclipse.xpanse.plugins.openstack.common.manage.OpenstackServersManager;
import org.eclipse.xpanse.plugins.openstack.common.monitor.OpenstackServiceMetricsManager;
import org.eclipse.xpanse.plugins.openstack.common.monitor.gnocchi.api.AggregationService;
import org.eclipse.xpanse.plugins.openstack.common.monitor.gnocchi.api.MeasuresService;
import org.eclipse.xpanse.plugins.openstack.common.monitor.gnocchi.api.ResourcesService;
import org.eclipse.xpanse.plugins.openstack.common.monitor.gnocchi.utils.GnocchiToXpanseModelConverter;
import org.eclipse.xpanse.plugins.openstack.common.monitor.gnocchi.utils.MetricsQueryBuilder;
import org.eclipse.xpanse.plugins.openstack.common.price.OpenstackServicePriceCalculator;
import org.eclipse.xpanse.plugins.openstack.common.resourcehandler.OpenstackTerraformResourceHandler;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {OpenstackTestlabOrchestratorPlugin.class, ScsKeystoneManager.class,
        OpenstackKeystoneManager.class, OpenstackServiceMetricsManager.class,
        ResourcesService.class, GnocchiToXpanseModelConverter.class, AggregationService.class,
        MeasuresService.class, MetricsQueryBuilder.class, CredentialCenter.class, AesUtil.class,
        ServiceMetricsStore.class, OpenstackTerraformResourceHandler.class, PluginManager.class,
        ServiceTemplateStorage.class, OpenstackResourceManager.class,
        OpenstackServicePriceCalculator.class, ProviderAuthInfoResolver.class
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = {"OPENSTACK_TESTLAB_AUTH_URL=http://127.0.0.1/identity/v3"})
class OpenstackTestlabOrchestratorPluginTest {
    private static final Csp csp = Csp.OPENSTACK_TESTLAB;
    private static final String userId = "userId";
    private static final String region = "RegionOne";
    private static final UUID uuid = UUID.randomUUID();
    private static final String resourceId = "7b5b6ee6-cab4-4e72-be6e-854a67c6d381";

    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().extensions(
                    new ResponseTemplateTransformer(TemplateEngine.defaultTemplateEngine(),
                            false, new ClasspathFileSource("src/test/resources/mappings"),
                            Collections.emptyList()))).build();
    @MockBean
    private CredentialCenter mockCredentialCenter;
    @MockBean
    private ServiceMetricsStore mockServiceMetricsStore;
    @MockBean
    private OpenstackServersManager mockServersManager;
    @MockBean
    private OpenstackResourceManager mockResourceManager;
    @MockBean
    private OpenstackServicePriceCalculator mockPriceCalculator;
    @MockBean
    private DatabaseDeployServiceStorage databaseDeployServiceStorage;
    @MockBean
    private ServiceTemplateStorage serviceTemplateStorage;
    @MockBean
    private DeployEnvironments deployEnvironments;

    @Resource
    private OpenstackTestlabOrchestratorPlugin plugin;

    @BeforeAll
    void setEnvVar() {
        System.setProperty(OPENSTACK_TESTLAB_AUTH_URL,
                wireMockExtension.getRuntimeInfo().getHttpBaseUrl() + "/identity/v3");
    }

    public ResourceMetricsRequest setupResourceRequest(Long from, Long to, Integer period,
                                                       boolean onlyLastKnownMetric) {
        return new ResourceMetricsRequest(uuid,
                Instancio.of(DeployResource.class).set(field(DeployResource::getResourceKind),
                        DeployResourceKind.VM).set(field(DeployResource::getResourceId),
                        resourceId).create(),
                null, from, to, period, onlyLastKnownMetric, userId);
    }

    public ServiceMetricsRequest setupServiceRequest(Long from, Long to, Integer period,
                                                     boolean onlyLastKnownMetric) {
        return new ServiceMetricsRequest(uuid,
                List.of(Instancio.of(DeployResource.class)
                        .set(field(DeployResource::getResourceKind),
                                DeployResourceKind.VM)
                        .set(field(DeployResource::getResourceId),
                                resourceId).create()),
                null, from, to, period, onlyLastKnownMetric, userId);
    }

    @Test
    void testGetResourceHandler() {
        assertThat(plugin.resourceHandlers().get(DeployerKind.TERRAFORM)).isNotNull()
                .isInstanceOf(OpenstackTerraformResourceHandler.class);
    }

    @Test
    void testGetCsp() {
        assertThat(plugin.getCsp()).isEqualTo(csp);
    }

    @Test
    void testAutoApproveServiceTemplateIsEnabled() {
        assertThat(plugin.autoApproveServiceTemplateIsEnabled()).isFalse();
    }

    @Test
    void testRequiredProperties() {
        assertThat(plugin.requiredProperties()).isEqualTo(List.of(OPENSTACK_TESTLAB_AUTH_URL));
    }

    @Test
    void testGetProviderMappingProperties() {
        // Setup
        // Run the test
        final Map<String, String> result = plugin.getEnvVarKeysMappingMap();
        // Verify the results
        assertFalse(result.isEmpty());
        assertThat(result.get(OS_AUTH_URL)).isNotBlank();
    }



    @Test
    void testGetAvailableCredentialTypes() {
        assertThat(plugin.getAvailableCredentialTypes()).isEqualTo(
                List.of(CredentialType.VARIABLES));
    }

    @Test
    void testGetCredentialDefinitions() {
        // Run the test
        final List<AbstractCredentialInfo> result = plugin.getCredentialDefinitions();
        // Verify the results
        assertFalse(result.isEmpty());
    }

    ServiceStateManageRequest setupServiceStateManageRequest() {
        final ServiceStateManageRequest serviceStateManageRequest = new ServiceStateManageRequest();
        serviceStateManageRequest.setServiceId(uuid);
        serviceStateManageRequest.setUserId(userId);
        serviceStateManageRequest.setRegionName(region);
        final DeployResourceEntity deployResourceEntity = new DeployResourceEntity();
        deployResourceEntity.setId(uuid);
        deployResourceEntity.setResourceId(resourceId);
        serviceStateManageRequest.setDeployResourceEntityList(List.of(deployResourceEntity));
        return serviceStateManageRequest;
    }

    @Test
    void testStartService() {
        when(this.mockServersManager.startService(any(), any())).thenReturn(false);
        // Run the test
        final boolean result = plugin.startService(setupServiceStateManageRequest());
        // Verify the results
        assertThat(result).isFalse();
    }

    @Test
    void testStopService() {
        // Setup
        when(this.mockServersManager.stopService(any(), any())).thenReturn(false);
        // Run the test
        final boolean result = plugin.stopService(setupServiceStateManageRequest());
        // Verify the results
        assertThat(result).isFalse();
    }


    @Test
    void testRestartService() {
        when(this.mockServersManager.restartService(any(), any())).thenReturn(false);
        // Run the test
        final boolean result = plugin.restartService(setupServiceStateManageRequest());
        // Verify the results
        assertThat(result).isFalse();
    }

    void mockGetAuthUrl() {
        when(this.mockCredentialCenter.getCredential(any(), any(), any())).thenReturn(
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
                Map.of(OS_AUTH_URL, wireMockExtension.baseUrl() + "/identity/v3"));
    }

    @Test
    void testGetMetricsForResource() {
        mockGetAuthUrl();
        testGetMetricsForResourceHappyCase();
        testGetMetricsForResourceWithFromAndTo();
        testGetMetricsForResourceWithGranularity();
        testGetMetricsForResourceWithOnlyLastKnownMetric();
    }

    @Test
    void testGetMetricsForService() {
        mockGetAuthUrl();
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

    void testGetMetricsForResourceHappyCase() {
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

    void testGetMetricsForResourceWithFromAndTo() {
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

    void testGetMetricsForResourceWithGranularity() {
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

    void testGetMetricsForResourceWithOnlyLastKnownMetric() {
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
    void testGetServiceFlavorPrice() {
        // Setup
        ServiceFlavorPriceRequest fixedPriceRequest =
                Instancio.of(ServiceFlavorPriceRequest.class)
                        .set(field(ServiceFlavorPriceRequest::getFlavorName), "flavorName")
                        .set(field(ServiceFlavorPriceRequest::getBillingMode), BillingMode.FIXED)
                        .create();
        Price price = Instancio.of(Price.class)
                .set(field(Price::getCost), BigDecimal.valueOf(1.0))
                .set(field(Price::getCurrency), Currency.USD)
                .set(field(Price::getPeriod), PricingPeriod.HOURLY).create();
        FlavorPriceResult expectedResult = Instancio.of(FlavorPriceResult.class)
                .set(field(FlavorPriceResult::getFlavorName), "flavorName")
                .set(field(FlavorPriceResult::getBillingMode), BillingMode.FIXED)
                .set(field(FlavorPriceResult::getRecurringPrice), price).create();

        when(mockPriceCalculator.getServiceFlavorPrice(fixedPriceRequest)).thenReturn(
                expectedResult);

        // Run the test
        final FlavorPriceResult result = plugin.getServiceFlavorPrice(fixedPriceRequest);
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    private CredentialVariables getCredentialDefinition() {
        CredentialVariables credentialVariables =
                (CredentialVariables) this.plugin.getCredentialDefinitions().getFirst();
        for (CredentialVariable credentialVariable : credentialVariables.getVariables()) {
            if (credentialVariable.getName().equals(OpenstackCommonEnvironmentConstants.USERNAME)) {
                credentialVariable.setValue("admin");
            }
            if (credentialVariable.getName().equals(OpenstackCommonEnvironmentConstants.PASSWORD)) {
                credentialVariable.setValue("test");
            }
            if (credentialVariable.getName()
                    .equals(OpenstackCommonEnvironmentConstants.USER_DOMAIN)) {
                credentialVariable.setValue("default");
            }
            if (credentialVariable.getName().equals(OpenstackCommonEnvironmentConstants.PROJECT)) {
                credentialVariable.setValue("service");
            }
        }
        return credentialVariables;
    }

    @Test
    void testGetExistingResourceNamesWithKind() {
        // Setup
        mockGetAuthUrl();
        // Run the test
        final List<String> vms = plugin.getExistingResourceNamesWithKind(
                userId, region, DeployResourceKind.VM, null);
        // Verify the results
        assertThat(vms).isEqualTo(Collections.emptyList());

        // Run the test
        final List<String> vpcs = plugin.getExistingResourceNamesWithKind(
                userId, region, DeployResourceKind.VPC, null);
        // Verify the results
        assertThat(vpcs).isEqualTo(Collections.emptyList());

        // Run the test
        final List<String> subnets = plugin.getExistingResourceNamesWithKind(
                userId, region, DeployResourceKind.SUBNET, null);
        // Verify the results
        assertThat(subnets).isEqualTo(Collections.emptyList());

        // Run the test
        final List<String> securityGroupIds = plugin.getExistingResourceNamesWithKind(
                userId, region, DeployResourceKind.SECURITY_GROUP, null);
        // Verify the results
        assertThat(securityGroupIds).isEqualTo(Collections.emptyList());

        // Run the test
        final List<String> securityGroupRules = plugin.getExistingResourceNamesWithKind(
                userId, region, DeployResourceKind.SECURITY_GROUP_RULE, null);
        // Verify the results
        assertThat(securityGroupRules).isEqualTo(Collections.emptyList());

        // Run the test
        final List<String> volumes = plugin.getExistingResourceNamesWithKind(
                userId, region, DeployResourceKind.VOLUME, null);
        // Verify the results
        assertThat(volumes).isEqualTo(Collections.emptyList());

        // Run the test
        final List<String> keypairs = plugin.getExistingResourceNamesWithKind(
                userId, region, DeployResourceKind.VOLUME, null);
        // Verify the results
        assertThat(keypairs).isEqualTo(Collections.emptyList());

        // Run the test
        final List<String> publicIps = plugin.getExistingResourceNamesWithKind(
                userId, region, DeployResourceKind.VOLUME, null);
        // Verify the results
        assertThat(publicIps).isEqualTo(Collections.emptyList());
    }


    @Test
    void testGetAvailabilityZonesOfRegion() {
        mockGetAuthUrl();
        // Run the test
        final List<String> result = plugin.getAvailabilityZonesOfRegion(userId, region, null);
        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());

        // Run the test
        final List<String> result2 = plugin.getAvailabilityZonesOfRegion(userId, region, uuid);
        // Verify the results
        assertThat(result2).isEqualTo(Collections.emptyList());
    }
}
