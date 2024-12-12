package org.eclipse.xpanse.plugins.plusserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eclipse.xpanse.plugins.openstack.common.auth.constants.OpenstackCommonEnvironmentConstants.OS_AUTH_URL;
import static org.eclipse.xpanse.plugins.openstack.common.auth.constants.OpenstackCommonEnvironmentConstants.PLUS_SERVER_AUTH_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.resource.ServiceResourceEntity;
import org.eclipse.xpanse.modules.models.billing.FlavorPriceResult;
import org.eclipse.xpanse.modules.models.billing.Price;
import org.eclipse.xpanse.modules.models.billing.RatingMode;
import org.eclipse.xpanse.modules.models.billing.enums.BillingMode;
import org.eclipse.xpanse.modules.models.billing.enums.Currency;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.servicetemplate.CloudServiceProvider;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.UnavailableServiceRegionsException;
import org.eclipse.xpanse.modules.orchestrator.audit.AuditLog;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResourceHandler;
import org.eclipse.xpanse.modules.orchestrator.monitor.ResourceMetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.monitor.ServiceMetricsRequest;
import org.eclipse.xpanse.modules.orchestrator.price.ServiceFlavorPriceRequest;
import org.eclipse.xpanse.modules.orchestrator.servicestate.ServiceStateManageRequest;
import org.eclipse.xpanse.plugins.openstack.common.auth.ProviderAuthInfoResolver;
import org.eclipse.xpanse.plugins.openstack.common.manage.OpenstackResourceManager;
import org.eclipse.xpanse.plugins.openstack.common.manage.OpenstackServersManager;
import org.eclipse.xpanse.plugins.openstack.common.price.OpenstackServicePriceCalculator;
import org.eclipse.xpanse.plugins.openstack.common.resourcehandler.OpenstackTerraformResourceHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PlusServerOrchestratorPluginTest {

    private static final Csp csp = Csp.PLUS_SERVER;
    private final String userId = "userId";
    private final String siteName = "default";
    private final String regionName = "RegionOne";
    private final UUID uuid = UUID.randomUUID();
    @Mock private OpenstackTerraformResourceHandler mockTerraformResourceHandler;
    @Mock private OpenstackServersManager mockServersManager;
    @Mock private OpenstackResourceManager mockResourceManager;
    @Mock private OpenstackServicePriceCalculator mockPricingCalculator;
    @Mock private ProviderAuthInfoResolver mockProviderAuthInfoResolver;
    @Mock private Environment mockEnvironment;

    @InjectMocks private PlusServerOrchestratorPlugin plugin;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(plugin, "autoApproveServiceTemplateEnabled", false);
    }

    @Test
    void testGetCsp() {
        assertThat(plugin.getCsp()).isEqualTo(csp);
    }

    @Test
    void testRequiredProperties() {
        // Setup
        when(mockProviderAuthInfoResolver.getAuthUrlKeyByCsp(csp)).thenReturn(PLUS_SERVER_AUTH_URL);
        // Run the test
        final List<String> result = plugin.requiredProperties();
        // Verify the results
        assertThat(result).isEqualTo(List.of(PLUS_SERVER_AUTH_URL));
    }

    @Test
    void testGetEnvVarKeysMappingMap() {
        // Setup
        // Run the test
        final Map<String, String> result = plugin.getEnvVarKeysMappingMap();
        // Verify the results
        assertFalse(result.isEmpty());
        assertThat(result.get(OS_AUTH_URL)).isNotBlank();
    }

    @Test
    void testResourceHandlers() {
        // Setup
        // Run the test
        final Map<DeployerKind, DeployResourceHandler> result = plugin.resourceHandlers();
        // Verify the results
        result.forEach(
                (key, value) -> {
                    assertThat(value).isInstanceOf(OpenstackTerraformResourceHandler.class);
                });
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
        assertThat(result).isNotEmpty();
        assertThat(result.getFirst().getCsp()).isEqualTo(csp);
        assertThat(result.getFirst().getType()).isEqualTo(CredentialType.VARIABLES);
    }

    @Test
    void testAutoApproveServiceTemplateIsEnabled() {
        assertThat(plugin.autoApproveServiceTemplateIsEnabled()).isFalse();
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
        region.setName("RegionOne");
        region.setSite("default");
        region.setArea("area");
        CloudServiceProvider cloudServiceProvider = new CloudServiceProvider();
        cloudServiceProvider.setRegions(List.of(region));
        ocl.setCloudServiceProvider(cloudServiceProvider);
        // Run the test
        final boolean result = plugin.validateRegionsOfService(ocl);
        // Verify the results
        assertTrue(result);

        // Setup unavailable site name
        region.setSite("error-site");
        // Run the test
        assertThatThrownBy(() -> plugin.validateRegionsOfService(ocl))
                .isInstanceOf(UnavailableServiceRegionsException.class);
    }

    @Test
    void testGetExistingResourceNamesWithKind() {
        // Setup
        when(mockResourceManager.getExistingResourceNamesWithKind(
                        csp, siteName, regionName, userId, DeployResourceKind.VM, uuid))
                .thenReturn(List.of("value"));

        // Run the test
        final List<String> result =
                plugin.getExistingResourceNamesWithKind(
                        siteName, regionName, userId, DeployResourceKind.VM, uuid);

        // Verify the results
        assertThat(result).isEqualTo(List.of("value"));
    }

    @Test
    void testGetExistingResourceNamesWithKind_OpenstackResourceManagerReturnsNoItems() {
        // Setup
        when(mockResourceManager.getExistingResourceNamesWithKind(
                        csp, siteName, regionName, userId, DeployResourceKind.VM, uuid))
                .thenReturn(Collections.emptyList());

        // Run the test
        final List<String> result =
                plugin.getExistingResourceNamesWithKind(
                        siteName, regionName, userId, DeployResourceKind.VM, uuid);

        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void testGetAvailabilityZonesOfRegion() {
        // Setup
        when(mockResourceManager.getAvailabilityZonesOfRegion(
                        csp, siteName, regionName, userId, uuid, null))
                .thenReturn(List.of("value"));

        // Run the test
        final List<String> result =
                plugin.getAvailabilityZonesOfRegion(siteName, regionName, userId, uuid, null);

        // Verify the results
        assertThat(result).isEqualTo(List.of("value"));
    }

    @Test
    void testGetAvailabilityZonesOfRegion_OpenstackResourceManagerReturnsNoItems() {
        // Setup
        when(mockResourceManager.getAvailabilityZonesOfRegion(
                        csp, siteName, regionName, userId, uuid, null))
                .thenReturn(Collections.emptyList());

        // Run the test
        final List<String> result =
                plugin.getAvailabilityZonesOfRegion(siteName, regionName, userId, uuid, null);

        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void testGetMetricsForResource() {
        assertThat(
                        plugin.getMetricsForResource(
                                new ResourceMetricsRequest(
                                        uuid,
                                        getRegion(),
                                        new DeployResource(),
                                        MonitorResourceType.CPU,
                                        0L,
                                        0L,
                                        0,
                                        false,
                                        userId)))
                .isEqualTo(Collections.emptyList());
    }

    @Test
    void testGetMetricsForService() {
        assertThat(
                        plugin.getMetricsForService(
                                new ServiceMetricsRequest(
                                        uuid,
                                        getRegion(),
                                        List.of(new DeployResource()),
                                        MonitorResourceType.CPU,
                                        0L,
                                        0L,
                                        0,
                                        false,
                                        userId)))
                .isEqualTo(Collections.emptyList());
    }

    @Test
    void testStartService() {
        // Setup
        final ServiceStateManageRequest serviceStateManageRequest = new ServiceStateManageRequest();
        serviceStateManageRequest.setUserId("userId");
        serviceStateManageRequest.setRegion(getRegion());
        final ServiceResourceEntity serviceResourceEntity = new ServiceResourceEntity();
        serviceResourceEntity.setId(UUID.fromString("15c8f06e-d7d5-4620-a3b4-6a98f201fa21"));
        serviceResourceEntity.setResourceId("resourceId");
        serviceStateManageRequest.setServiceResourceEntityList(List.of(serviceResourceEntity));

        // Configure OpenstackServersManager.startService(...).
        final ServiceStateManageRequest request = new ServiceStateManageRequest();
        request.setUserId("userId");
        request.setRegion(getRegion());
        final ServiceResourceEntity serviceResourceEntity1 = new ServiceResourceEntity();
        serviceResourceEntity1.setId(UUID.fromString("15c8f06e-d7d5-4620-a3b4-6a98f201fa21"));
        serviceResourceEntity1.setResourceId("resourceId");
        request.setServiceResourceEntityList(List.of(serviceResourceEntity1));
        when(mockServersManager.startService(csp, request)).thenReturn(false);

        // Run the test
        final boolean result = plugin.startService(serviceStateManageRequest);

        // Verify the results
        assertThat(result).isFalse();
    }

    @Test
    void testStartService_OpenstackServersManagerReturnsTrue() {
        // Setup
        final ServiceStateManageRequest serviceStateManageRequest = new ServiceStateManageRequest();
        serviceStateManageRequest.setUserId("userId");
        serviceStateManageRequest.setRegion(getRegion());
        final ServiceResourceEntity serviceResourceEntity = new ServiceResourceEntity();
        serviceResourceEntity.setId(UUID.fromString("15c8f06e-d7d5-4620-a3b4-6a98f201fa21"));
        serviceResourceEntity.setResourceId("resourceId");
        serviceStateManageRequest.setServiceResourceEntityList(List.of(serviceResourceEntity));

        // Configure OpenstackServersManager.startService(...).
        final ServiceStateManageRequest request = new ServiceStateManageRequest();
        request.setUserId("userId");
        request.setRegion(getRegion());
        final ServiceResourceEntity serviceResourceEntity1 = new ServiceResourceEntity();
        serviceResourceEntity1.setId(UUID.fromString("15c8f06e-d7d5-4620-a3b4-6a98f201fa21"));
        serviceResourceEntity1.setResourceId("resourceId");
        request.setServiceResourceEntityList(List.of(serviceResourceEntity1));
        when(mockServersManager.startService(csp, request)).thenReturn(true);

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
        serviceResourceEntity.setId(UUID.fromString("15c8f06e-d7d5-4620-a3b4-6a98f201fa21"));
        serviceResourceEntity.setResourceId("resourceId");
        serviceStateManageRequest.setServiceResourceEntityList(List.of(serviceResourceEntity));

        // Configure OpenstackServersManager.stopService(...).
        final ServiceStateManageRequest request = new ServiceStateManageRequest();
        request.setUserId("userId");
        request.setRegion(getRegion());
        final ServiceResourceEntity serviceResourceEntity1 = new ServiceResourceEntity();
        serviceResourceEntity1.setId(UUID.fromString("15c8f06e-d7d5-4620-a3b4-6a98f201fa21"));
        serviceResourceEntity1.setResourceId("resourceId");
        request.setServiceResourceEntityList(List.of(serviceResourceEntity1));
        when(mockServersManager.stopService(csp, request)).thenReturn(false);

        // Run the test
        final boolean result = plugin.stopService(serviceStateManageRequest);

        // Verify the results
        assertThat(result).isFalse();
    }

    @Test
    void testStopService_OpenstackServersManagerReturnsTrue() {
        // Setup
        final ServiceStateManageRequest serviceStateManageRequest = new ServiceStateManageRequest();
        serviceStateManageRequest.setUserId("userId");
        serviceStateManageRequest.setRegion(getRegion());
        final ServiceResourceEntity serviceResourceEntity = new ServiceResourceEntity();
        serviceResourceEntity.setId(UUID.fromString("15c8f06e-d7d5-4620-a3b4-6a98f201fa21"));
        serviceResourceEntity.setResourceId("resourceId");
        serviceStateManageRequest.setServiceResourceEntityList(List.of(serviceResourceEntity));

        // Configure OpenstackServersManager.stopService(...).
        final ServiceStateManageRequest request = new ServiceStateManageRequest();
        request.setUserId("userId");
        request.setRegion(getRegion());
        final ServiceResourceEntity serviceResourceEntity1 = new ServiceResourceEntity();
        serviceResourceEntity1.setId(UUID.fromString("15c8f06e-d7d5-4620-a3b4-6a98f201fa21"));
        serviceResourceEntity1.setResourceId("resourceId");
        request.setServiceResourceEntityList(List.of(serviceResourceEntity1));
        when(mockServersManager.stopService(csp, request)).thenReturn(true);

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
        serviceResourceEntity.setId(UUID.fromString("15c8f06e-d7d5-4620-a3b4-6a98f201fa21"));
        serviceResourceEntity.setResourceId("resourceId");
        serviceStateManageRequest.setServiceResourceEntityList(List.of(serviceResourceEntity));

        // Configure OpenstackServersManager.restartService(...).
        final ServiceStateManageRequest request = new ServiceStateManageRequest();
        request.setUserId("userId");
        request.setRegion(getRegion());
        final ServiceResourceEntity serviceResourceEntity1 = new ServiceResourceEntity();
        serviceResourceEntity1.setId(UUID.fromString("15c8f06e-d7d5-4620-a3b4-6a98f201fa21"));
        serviceResourceEntity1.setResourceId("resourceId");
        request.setServiceResourceEntityList(List.of(serviceResourceEntity1));
        when(mockServersManager.restartService(csp, request)).thenReturn(false);

        // Run the test
        final boolean result = plugin.restartService(serviceStateManageRequest);

        // Verify the results
        assertThat(result).isFalse();
    }

    @Test
    void testRestartService_OpenstackServersManagerReturnsTrue() {
        // Setup
        final ServiceStateManageRequest serviceStateManageRequest = new ServiceStateManageRequest();
        serviceStateManageRequest.setUserId("userId");
        serviceStateManageRequest.setRegion(getRegion());
        final ServiceResourceEntity serviceResourceEntity = new ServiceResourceEntity();
        serviceResourceEntity.setId(UUID.fromString("15c8f06e-d7d5-4620-a3b4-6a98f201fa21"));
        serviceResourceEntity.setResourceId("resourceId");
        serviceStateManageRequest.setServiceResourceEntityList(List.of(serviceResourceEntity));

        // Configure OpenstackServersManager.restartService(...).
        final ServiceStateManageRequest request = new ServiceStateManageRequest();
        request.setUserId("userId");
        request.setRegion(getRegion());
        final ServiceResourceEntity serviceResourceEntity1 = new ServiceResourceEntity();
        serviceResourceEntity1.setId(UUID.fromString("15c8f06e-d7d5-4620-a3b4-6a98f201fa21"));
        serviceResourceEntity1.setResourceId("resourceId");
        request.setServiceResourceEntityList(List.of(serviceResourceEntity1));
        when(mockServersManager.restartService(csp, request)).thenReturn(true);

        // Run the test
        final boolean result = plugin.restartService(serviceStateManageRequest);

        // Verify the results
        assertThat(result).isTrue();
    }

    @Test
    void testAuditApiRequest() {
        // Setup
        final AuditLog auditLog = new AuditLog();
        auditLog.setMethodName("methodName");
        auditLog.setMethodType("methodType");
        auditLog.setUrl("url");
        auditLog.setParams(new Object[] {"params"});
        auditLog.setResult("result");

        // Run the test
        plugin.auditApiRequest(auditLog);

        // Verify the results
    }

    @Test
    void testGetServiceFlavorPrice() {
        // Setup
        final ServiceFlavorPriceRequest request = new ServiceFlavorPriceRequest();
        request.setServiceTemplateId("serviceTemplateId");
        request.setFlavorName("flavorName");
        request.setUserId(userId);
        request.setRegionName(regionName);
        request.setSiteName(siteName);
        final RatingMode flavorRatingMode = new RatingMode();
        request.setFlavorRatingMode(flavorRatingMode);

        final FlavorPriceResult expectedResult = new FlavorPriceResult();
        expectedResult.setFlavorName("flavorName");
        expectedResult.setBillingMode(BillingMode.FIXED);
        final Price recurringPrice = new Price();
        recurringPrice.setCost(new BigDecimal("0.00"));
        recurringPrice.setCurrency(Currency.USD);
        expectedResult.setRecurringPrice(recurringPrice);

        // Configure OpenstackServicePriceCalculator.getServiceFlavorPrice(...).
        final FlavorPriceResult flavorPriceResult = new FlavorPriceResult();
        flavorPriceResult.setFlavorName("flavorName");
        flavorPriceResult.setBillingMode(BillingMode.FIXED);
        final Price recurringPrice1 = new Price();
        recurringPrice1.setCost(new BigDecimal("0.00"));
        recurringPrice1.setCurrency(Currency.USD);
        flavorPriceResult.setRecurringPrice(recurringPrice1);
        final ServiceFlavorPriceRequest request1 = new ServiceFlavorPriceRequest();
        request1.setServiceTemplateId("serviceTemplateId");
        request1.setFlavorName("flavorName");
        request1.setUserId(userId);
        request1.setRegionName(regionName);
        request1.setSiteName(siteName);
        final RatingMode flavorRatingMode1 = new RatingMode();
        request1.setFlavorRatingMode(flavorRatingMode1);
        when(mockPricingCalculator.getServiceFlavorPrice(request1)).thenReturn(flavorPriceResult);

        // Run the test
        final FlavorPriceResult result = plugin.getServiceFlavorPrice(request);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    private Region getRegion() {
        Region region = new Region();
        region.setName(regionName);
        region.setSite(siteName);
        region.setArea("area");
        return region;
    }
}
