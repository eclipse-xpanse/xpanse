package org.eclipse.xpanse.modules.models.servicetemplate.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.billing.Billing;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.AvailabilityZoneConfig;
import org.eclipse.xpanse.modules.models.servicetemplate.DeployVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.EndUserFlavors;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceAction;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceChangeParameter;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceProviderContactDetails;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;

@ExtendWith(MockitoExtension.class)
class UserOrderableServiceVoTest {

    final String serviceVendor = "ISV-A";
    final String description = "description";
    final String icon = "icon";
    final String eula = "eula";
    private final UUID id = UUID.randomUUID();
    private final String name = "name";
    private final String version = "version";
    private final List<ServiceChangeParameter> configurationParameters = List.of();
    @Mock private Category mockCategory;
    @Mock private Csp mockCsp;
    @Mock private List<Region> mockRegions;
    @Mock private List<DeployVariable> mockVariables;
    @Mock private EndUserFlavors mockFlavors;
    @Mock private Billing mockBilling;
    @Mock private ServiceHostingType mockServiceHostingType;
    @Mock private ServiceProviderContactDetails mockServiceProviderContactDetails;
    @Mock private List<AvailabilityZoneConfig> mockserviceAvailabilityConfig;
    private List<ServiceAction> serviceActions = List.of();
    private UserOrderableServiceVo test;

    @BeforeEach
    void setUp() {
        test = new UserOrderableServiceVo();
        test.setServiceTemplateId(id);
        test.setName(name);
        test.setVersion(version);
        test.setServiceVendor(serviceVendor);
        test.setDescription(description);
        test.setIcon(icon);
        test.setCategory(mockCategory);
        test.setCsp(mockCsp);
        test.setRegions(mockRegions);
        test.setVariables(mockVariables);
        test.setFlavors(mockFlavors);
        test.setBilling(mockBilling);
        test.setServiceHostingType(mockServiceHostingType);
        test.setServiceProviderContactDetails(mockServiceProviderContactDetails);
        test.setServiceAvailabilityConfig(mockserviceAvailabilityConfig);
        test.setEula(eula);
        test.setConfigurationParameters(configurationParameters);
        test.setServiceActions(serviceActions);
    }

    @Test
    void testGetters() {
        assertThat(test.getServiceTemplateId()).isEqualTo(id);
        assertThat(test.getServiceVendor()).isEqualTo(serviceVendor);
        assertThat(test.getDescription()).isEqualTo(description);
        assertThat(test.getCategory()).isEqualTo(mockCategory);
        assertThat(test.getName()).isEqualTo(name);
        assertThat(test.getVersion()).isEqualTo(version);
        assertThat(test.getIcon()).isEqualTo(icon);
        assertThat(test.getRegions()).isEqualTo(mockRegions);
        assertThat(test.getCsp()).isEqualTo(mockCsp);
        assertThat(test.getVariables()).isEqualTo(mockVariables);
        assertThat(test.getFlavors()).isEqualTo(mockFlavors);
        assertThat(test.getBilling()).isEqualTo(mockBilling);
        assertThat(test.getServiceHostingType()).isEqualTo(mockServiceHostingType);
        assertThat(test.getServiceProviderContactDetails())
                .isEqualTo(mockServiceProviderContactDetails);
        assertThat(test.getServiceAvailabilityConfig()).isEqualTo(mockserviceAvailabilityConfig);
        assertThat(test.getEula()).isEqualTo(eula);
        assertThat(test.getConfigurationParameters()).isEqualTo(configurationParameters);
        assertThat(test.getServiceActions()).isEqualTo(serviceActions);
    }

    @Test
    public void testEqualsAndHashCode() {
        assertNotEquals(test, new Object());
        assertNotEquals(test.hashCode(), new Object().hashCode());
        UserOrderableServiceVo test1 = new UserOrderableServiceVo();
        assertNotEquals(test, test1);
        assertNotEquals(test.hashCode(), test1.hashCode());
        UserOrderableServiceVo test2 = new UserOrderableServiceVo();
        BeanUtils.copyProperties(test, test2);
        test2.setEula(eula);
        assertEquals(test, test2);
        assertEquals(test.hashCode(), test2.hashCode());
    }

    @Test
    void testToString() {
        String expectedToString =
                "UserOrderableServiceVo(serviceTemplateId="
                        + id
                        + ", category="
                        + mockCategory
                        + ", name="
                        + name
                        + ", version="
                        + version
                        + ", csp="
                        + mockCsp
                        + ", regions="
                        + mockRegions
                        + ", serviceVendor="
                        + serviceVendor
                        + ", description="
                        + description
                        + ", icon="
                        + icon
                        + ", variables="
                        + mockVariables
                        + ", flavors="
                        + mockFlavors
                        + ", billing="
                        + mockBilling
                        + ", serviceHostingType="
                        + mockServiceHostingType
                        + ", serviceProviderContactDetails="
                        + mockServiceProviderContactDetails
                        + ", serviceAvailabilityConfig="
                        + mockserviceAvailabilityConfig
                        + ", eula="
                        + eula
                        + ", configurationParameters="
                        + configurationParameters
                        + ", serviceActions="
                        + serviceActions
                        + ")";
        assertEquals(expectedToString, test.toString());
    }
}
