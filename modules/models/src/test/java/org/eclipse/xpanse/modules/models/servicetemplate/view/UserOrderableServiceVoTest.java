/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.servicetemplate.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.Billing;
import org.eclipse.xpanse.modules.models.servicetemplate.DeployVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.FlavorBasic;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceProviderContactDetails;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of UserOrderableServiceVo.
 */
class UserOrderableServiceVoTest {

    private static final UUID uuid = UUID.fromString("20424910-5f64-4984-84f0-6013c63c64f5");
    private static final Category category = Category.COMPUTE;
    private static final String name = "kafka";
    private static final String version = "v1.0.0";
    private static final Csp csp = Csp.AWS;
    private static final String description = "description";
    private static final String icon = "icon";
    private static List<@Valid Region> regions;
    private static List<@Valid DeployVariable> variables;
    private static List<FlavorBasic> flavorBasics;
    private static Billing billing;
    private static UserOrderableServiceVo userOrderableServiceVo;
    private static ServiceProviderContactDetails serviceProviderContactDetails;

    @BeforeEach
    void setUp() {
        Region region = new Region();
        region.setName("cn-north-1");
        region.setArea("Asia");
        regions = List.of(region);

        DeployVariable deployVariable = new DeployVariable();
        deployVariable.setName("HuaweiClouud AK");
        variables = List.of(deployVariable);

        FlavorBasic flavorBasic = new FlavorBasic();
        flavorBasic.setName("flavorBasic");
        flavorBasics = List.of(flavorBasic);

        billing = new Billing();
        billing.setModel("model");

        userOrderableServiceVo = new UserOrderableServiceVo();
        userOrderableServiceVo.setId(uuid);
        userOrderableServiceVo.setCategory(category);
        userOrderableServiceVo.setName(name);
        userOrderableServiceVo.setVersion(version);
        userOrderableServiceVo.setCsp(csp);
        userOrderableServiceVo.setRegions(regions);
        userOrderableServiceVo.setDescription(description);
        userOrderableServiceVo.setIcon(icon);
        userOrderableServiceVo.setVariables(variables);
        userOrderableServiceVo.setFlavors(flavorBasics);
        userOrderableServiceVo.setBilling(billing);
        userOrderableServiceVo.setServiceHostingType(ServiceHostingType.SELF);
        userOrderableServiceVo.setServiceProviderContactDetails(serviceProviderContactDetails);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(uuid, userOrderableServiceVo.getId());
        assertEquals(category, userOrderableServiceVo.getCategory());
        assertEquals(name, userOrderableServiceVo.getName());
        assertEquals(version, userOrderableServiceVo.getVersion());
        assertEquals(csp, userOrderableServiceVo.getCsp());
        assertEquals(regions, userOrderableServiceVo.getRegions());
        assertEquals(description, userOrderableServiceVo.getDescription());
        assertEquals(icon, userOrderableServiceVo.getIcon());
        assertEquals(variables, userOrderableServiceVo.getVariables());
        assertEquals(billing, userOrderableServiceVo.getBilling());
        assertEquals(ServiceHostingType.SELF, userOrderableServiceVo.getServiceHostingType());
        assertEquals(serviceProviderContactDetails, userOrderableServiceVo.getServiceProviderContactDetails());
    }

    @Test
    public void testEqualsAndHashCode() {
        assertEquals(userOrderableServiceVo.hashCode(), userOrderableServiceVo.hashCode());

        Object obj = new Object();
        assertNotEquals(userOrderableServiceVo, obj);
        assertNotEquals(userOrderableServiceVo, null);
        assertNotEquals(userOrderableServiceVo.hashCode(), obj.hashCode());

        UserOrderableServiceVo userOrderableServiceVo1 = new UserOrderableServiceVo();
        UserOrderableServiceVo userOrderableServiceVo2 = new UserOrderableServiceVo();
        assertNotEquals(userOrderableServiceVo, userOrderableServiceVo1);
        assertNotEquals(userOrderableServiceVo, userOrderableServiceVo2);
        assertEquals(userOrderableServiceVo1, userOrderableServiceVo2);
        assertNotEquals(userOrderableServiceVo.hashCode(), userOrderableServiceVo1.hashCode());
        assertNotEquals(userOrderableServiceVo.hashCode(), userOrderableServiceVo2.hashCode());
        assertEquals(userOrderableServiceVo1.hashCode(), userOrderableServiceVo2.hashCode());

        userOrderableServiceVo1.setId(uuid);
        assertNotEquals(userOrderableServiceVo, userOrderableServiceVo1);
        assertNotEquals(userOrderableServiceVo1, userOrderableServiceVo2);
        assertNotEquals(userOrderableServiceVo.hashCode(), userOrderableServiceVo1.hashCode());
        assertNotEquals(userOrderableServiceVo1.hashCode(), userOrderableServiceVo2.hashCode());

        userOrderableServiceVo1.setCategory(category);
        assertNotEquals(userOrderableServiceVo, userOrderableServiceVo1);
        assertNotEquals(userOrderableServiceVo1, userOrderableServiceVo2);
        assertNotEquals(userOrderableServiceVo.hashCode(), userOrderableServiceVo1.hashCode());
        assertNotEquals(userOrderableServiceVo1.hashCode(), userOrderableServiceVo2.hashCode());

        userOrderableServiceVo1.setName(name);
        assertNotEquals(userOrderableServiceVo, userOrderableServiceVo1);
        assertNotEquals(userOrderableServiceVo1, userOrderableServiceVo2);
        assertNotEquals(userOrderableServiceVo.hashCode(), userOrderableServiceVo1.hashCode());
        assertNotEquals(userOrderableServiceVo1.hashCode(), userOrderableServiceVo2.hashCode());

        userOrderableServiceVo1.setVersion(version);
        assertNotEquals(userOrderableServiceVo, userOrderableServiceVo1);
        assertNotEquals(userOrderableServiceVo1, userOrderableServiceVo2);
        assertNotEquals(userOrderableServiceVo.hashCode(), userOrderableServiceVo1.hashCode());
        assertNotEquals(userOrderableServiceVo1.hashCode(), userOrderableServiceVo2.hashCode());

        userOrderableServiceVo1.setCsp(csp);
        assertNotEquals(userOrderableServiceVo, userOrderableServiceVo1);
        assertNotEquals(userOrderableServiceVo1, userOrderableServiceVo2);
        assertNotEquals(userOrderableServiceVo.hashCode(), userOrderableServiceVo1.hashCode());
        assertNotEquals(userOrderableServiceVo1.hashCode(), userOrderableServiceVo2.hashCode());

        userOrderableServiceVo1.setRegions(regions);
        assertNotEquals(userOrderableServiceVo, userOrderableServiceVo1);
        assertNotEquals(userOrderableServiceVo1, userOrderableServiceVo2);
        assertNotEquals(userOrderableServiceVo.hashCode(), userOrderableServiceVo1.hashCode());
        assertNotEquals(userOrderableServiceVo1.hashCode(), userOrderableServiceVo2.hashCode());

        userOrderableServiceVo1.setDescription(description);
        assertNotEquals(userOrderableServiceVo, userOrderableServiceVo1);
        assertNotEquals(userOrderableServiceVo1, userOrderableServiceVo2);
        assertNotEquals(userOrderableServiceVo.hashCode(), userOrderableServiceVo1.hashCode());
        assertNotEquals(userOrderableServiceVo1.hashCode(), userOrderableServiceVo2.hashCode());

        userOrderableServiceVo1.setIcon(icon);
        assertNotEquals(userOrderableServiceVo, userOrderableServiceVo1);
        assertNotEquals(userOrderableServiceVo1, userOrderableServiceVo2);
        assertNotEquals(userOrderableServiceVo.hashCode(), userOrderableServiceVo1.hashCode());
        assertNotEquals(userOrderableServiceVo1.hashCode(), userOrderableServiceVo2.hashCode());

        userOrderableServiceVo1.setVariables(variables);
        assertNotEquals(userOrderableServiceVo, userOrderableServiceVo1);
        assertNotEquals(userOrderableServiceVo1, userOrderableServiceVo2);
        assertNotEquals(userOrderableServiceVo.hashCode(), userOrderableServiceVo1.hashCode());
        assertNotEquals(userOrderableServiceVo1.hashCode(), userOrderableServiceVo2.hashCode());

        userOrderableServiceVo1.setFlavors(flavorBasics);
        assertNotEquals(userOrderableServiceVo, userOrderableServiceVo1);
        assertNotEquals(userOrderableServiceVo1, userOrderableServiceVo2);
        assertNotEquals(userOrderableServiceVo.hashCode(), userOrderableServiceVo1.hashCode());
        assertNotEquals(userOrderableServiceVo1.hashCode(), userOrderableServiceVo2.hashCode());


        userOrderableServiceVo1.setBilling(billing);
        userOrderableServiceVo1.setServiceHostingType(ServiceHostingType.SELF);
        userOrderableServiceVo1.setServiceProviderContactDetails(serviceProviderContactDetails);
        assertEquals(userOrderableServiceVo, userOrderableServiceVo1);
        assertNotEquals(userOrderableServiceVo1, userOrderableServiceVo2);
        assertEquals(userOrderableServiceVo.hashCode(), userOrderableServiceVo1.hashCode());
        assertNotEquals(userOrderableServiceVo1.hashCode(), userOrderableServiceVo2.hashCode());
    }

    @Test
    void testToString() {
        String expectedToString = "UserOrderableServiceVo(" +
                "id=" + uuid + ", " +
                "category=" + category + ", " +
                "name=" + name + ", " +
                "version=" + version + ", " +
                "csp=" + csp + ", " +
                "regions=" + regions + ", " +
                "description=" + description + ", " +
                "icon=" + icon + ", " +
                "variables=" + variables + ", " +
                "flavors=" + flavorBasics + ", " +
                "billing=" + billing + ", " +
                "serviceHostingType=" + ServiceHostingType.SELF + ", "+
                "serviceProviderContactDetails=" + serviceProviderContactDetails + ")";
        assertEquals(expectedToString, userOrderableServiceVo.toString());
    }

}
