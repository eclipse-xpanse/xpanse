/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.servicetemplate.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import jakarta.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.Billing;
import org.eclipse.xpanse.modules.models.servicetemplate.DeployVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.Deployment;
import org.eclipse.xpanse.modules.models.servicetemplate.Flavor;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceRegistrationState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of UserAvailableServiceVo.
 */
class UserAvailableServiceVoTest {

    private static final UUID uuid = UUID.fromString("20424910-5f64-4984-84f0-6013c63c64f5");
    private static final Category category = Category.COMPUTE;
    private static final String name = "kafka";
    private static final String version = "v1.0.0";
    private static final Csp csp = Csp.AWS;
    private static final String description = "description";
    private static final String namespace = "namespace";
    private static final String icon = "icon";
    private static final Deployment DEPLOYMENT = new Deployment();
    private static final Date createTime = new Date();
    private static final Date lastModifiedTime = new Date();
    private static final ServiceRegistrationState serviceRegistrationState =
            ServiceRegistrationState.REGISTERED;
    private static List<@Valid Region> regions;
    private static List<@Valid DeployVariable> variables;
    private static List<@Valid Flavor> flavors;
    private static Billing billing;
    private static UserAvailableServiceVo userAvailableServiceVo;

    @BeforeEach
    void setUp() {
        Region region = new Region();
        region.setName("cn-north-1");
        region.setArea("Asia");
        regions = List.of(region);

        DeployVariable deployVariable = new DeployVariable();
        deployVariable.setName("HuaweiClouud AK");
        variables = List.of(deployVariable);

        Flavor flavor = new Flavor();
        flavor.setName("flavor");
        flavors = List.of(flavor);

        billing = new Billing();
        billing.setModel("model");

        userAvailableServiceVo = new UserAvailableServiceVo();
        userAvailableServiceVo.setId(uuid);
        userAvailableServiceVo.setCategory(category);
        userAvailableServiceVo.setName(name);
        userAvailableServiceVo.setVersion(version);
        userAvailableServiceVo.setCsp(csp);
        userAvailableServiceVo.setRegions(regions);
        userAvailableServiceVo.setDescription(description);
        userAvailableServiceVo.setNamespace(namespace);
        userAvailableServiceVo.setIcon(icon);
        userAvailableServiceVo.setDeployment(DEPLOYMENT);
        userAvailableServiceVo.setVariables(variables);
        userAvailableServiceVo.setFlavors(flavors);
        userAvailableServiceVo.setBilling(billing);
        userAvailableServiceVo.setCreateTime(createTime);
        userAvailableServiceVo.setLastModifiedTime(lastModifiedTime);
        userAvailableServiceVo.setServiceRegistrationState(serviceRegistrationState);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(uuid, userAvailableServiceVo.getId());
        assertEquals(category, userAvailableServiceVo.getCategory());
        assertEquals(name, userAvailableServiceVo.getName());
        assertEquals(version, userAvailableServiceVo.getVersion());
        assertEquals(csp, userAvailableServiceVo.getCsp());
        assertEquals(regions, userAvailableServiceVo.getRegions());
        assertEquals(description, userAvailableServiceVo.getDescription());
        assertEquals(namespace, userAvailableServiceVo.getNamespace());
        assertEquals(icon, userAvailableServiceVo.getIcon());
        assertEquals(variables, userAvailableServiceVo.getVariables());
        assertEquals(flavors, userAvailableServiceVo.getFlavors());
        assertEquals(billing, userAvailableServiceVo.getBilling());
        assertEquals(createTime, userAvailableServiceVo.getCreateTime());
        assertEquals(lastModifiedTime, userAvailableServiceVo.getLastModifiedTime());
        assertEquals(serviceRegistrationState, userAvailableServiceVo.getServiceRegistrationState());
    }

    @Test
    public void testEqualsAndHashCode() {
        assertEquals(userAvailableServiceVo, userAvailableServiceVo);
        assertEquals(userAvailableServiceVo.hashCode(), userAvailableServiceVo.hashCode());

        Object obj = new Object();
        assertNotEquals(userAvailableServiceVo, obj);
        assertNotEquals(userAvailableServiceVo, null);
        assertNotEquals(userAvailableServiceVo.hashCode(), obj.hashCode());

        UserAvailableServiceVo userAvailableServiceVo1 = new UserAvailableServiceVo();
        UserAvailableServiceVo userAvailableServiceVo2 = new UserAvailableServiceVo();
        assertNotEquals(userAvailableServiceVo, userAvailableServiceVo1);
        assertNotEquals(userAvailableServiceVo, userAvailableServiceVo2);
        assertEquals(userAvailableServiceVo1, userAvailableServiceVo2);
        assertNotEquals(userAvailableServiceVo.hashCode(), userAvailableServiceVo1.hashCode());
        assertNotEquals(userAvailableServiceVo.hashCode(), userAvailableServiceVo2.hashCode());
        assertEquals(userAvailableServiceVo1.hashCode(), userAvailableServiceVo2.hashCode());

        userAvailableServiceVo1.setId(uuid);
        assertNotEquals(userAvailableServiceVo, userAvailableServiceVo1);
        assertNotEquals(userAvailableServiceVo1, userAvailableServiceVo2);
        assertNotEquals(userAvailableServiceVo.hashCode(), userAvailableServiceVo1.hashCode());
        assertNotEquals(userAvailableServiceVo1.hashCode(), userAvailableServiceVo2.hashCode());

        userAvailableServiceVo1.setCategory(category);
        assertNotEquals(userAvailableServiceVo, userAvailableServiceVo1);
        assertNotEquals(userAvailableServiceVo1, userAvailableServiceVo2);
        assertNotEquals(userAvailableServiceVo.hashCode(), userAvailableServiceVo1.hashCode());
        assertNotEquals(userAvailableServiceVo1.hashCode(), userAvailableServiceVo2.hashCode());

        userAvailableServiceVo1.setName(name);
        assertNotEquals(userAvailableServiceVo, userAvailableServiceVo1);
        assertNotEquals(userAvailableServiceVo1, userAvailableServiceVo2);
        assertNotEquals(userAvailableServiceVo.hashCode(), userAvailableServiceVo1.hashCode());
        assertNotEquals(userAvailableServiceVo1.hashCode(), userAvailableServiceVo2.hashCode());

        userAvailableServiceVo1.setVersion(version);
        assertNotEquals(userAvailableServiceVo, userAvailableServiceVo1);
        assertNotEquals(userAvailableServiceVo1, userAvailableServiceVo2);
        assertNotEquals(userAvailableServiceVo.hashCode(), userAvailableServiceVo1.hashCode());
        assertNotEquals(userAvailableServiceVo1.hashCode(), userAvailableServiceVo2.hashCode());

        userAvailableServiceVo1.setCsp(csp);
        assertNotEquals(userAvailableServiceVo, userAvailableServiceVo1);
        assertNotEquals(userAvailableServiceVo1, userAvailableServiceVo2);
        assertNotEquals(userAvailableServiceVo.hashCode(), userAvailableServiceVo1.hashCode());
        assertNotEquals(userAvailableServiceVo1.hashCode(), userAvailableServiceVo2.hashCode());

        userAvailableServiceVo1.setRegions(regions);
        assertNotEquals(userAvailableServiceVo, userAvailableServiceVo1);
        assertNotEquals(userAvailableServiceVo1, userAvailableServiceVo2);
        assertNotEquals(userAvailableServiceVo.hashCode(), userAvailableServiceVo1.hashCode());
        assertNotEquals(userAvailableServiceVo1.hashCode(), userAvailableServiceVo2.hashCode());

        userAvailableServiceVo1.setDescription(description);
        assertNotEquals(userAvailableServiceVo, userAvailableServiceVo1);
        assertNotEquals(userAvailableServiceVo1, userAvailableServiceVo2);
        assertNotEquals(userAvailableServiceVo.hashCode(), userAvailableServiceVo1.hashCode());
        assertNotEquals(userAvailableServiceVo1.hashCode(), userAvailableServiceVo2.hashCode());

        userAvailableServiceVo1.setNamespace(namespace);
        assertNotEquals(userAvailableServiceVo, userAvailableServiceVo1);
        assertNotEquals(userAvailableServiceVo1, userAvailableServiceVo2);
        assertNotEquals(userAvailableServiceVo.hashCode(), userAvailableServiceVo1.hashCode());
        assertNotEquals(userAvailableServiceVo1.hashCode(), userAvailableServiceVo2.hashCode());

        userAvailableServiceVo1.setIcon(icon);
        assertNotEquals(userAvailableServiceVo, userAvailableServiceVo1);
        assertNotEquals(userAvailableServiceVo1, userAvailableServiceVo2);
        assertNotEquals(userAvailableServiceVo.hashCode(), userAvailableServiceVo1.hashCode());
        assertNotEquals(userAvailableServiceVo1.hashCode(), userAvailableServiceVo2.hashCode());

        userAvailableServiceVo1.setDeployment(DEPLOYMENT);
        assertNotEquals(userAvailableServiceVo, userAvailableServiceVo1);
        assertNotEquals(userAvailableServiceVo1, userAvailableServiceVo2);
        assertNotEquals(userAvailableServiceVo.hashCode(), userAvailableServiceVo1.hashCode());
        assertNotEquals(userAvailableServiceVo1.hashCode(), userAvailableServiceVo2.hashCode());

        userAvailableServiceVo1.setVariables(variables);
        assertNotEquals(userAvailableServiceVo, userAvailableServiceVo1);
        assertNotEquals(userAvailableServiceVo1, userAvailableServiceVo2);
        assertNotEquals(userAvailableServiceVo.hashCode(), userAvailableServiceVo1.hashCode());
        assertNotEquals(userAvailableServiceVo1.hashCode(), userAvailableServiceVo2.hashCode());

        userAvailableServiceVo1.setFlavors(flavors);
        assertNotEquals(userAvailableServiceVo, userAvailableServiceVo1);
        assertNotEquals(userAvailableServiceVo1, userAvailableServiceVo2);
        assertNotEquals(userAvailableServiceVo.hashCode(), userAvailableServiceVo1.hashCode());
        assertNotEquals(userAvailableServiceVo1.hashCode(), userAvailableServiceVo2.hashCode());

        userAvailableServiceVo1.setBilling(billing);
        assertNotEquals(userAvailableServiceVo, userAvailableServiceVo1);
        assertNotEquals(userAvailableServiceVo1, userAvailableServiceVo2);
        assertNotEquals(userAvailableServiceVo.hashCode(), userAvailableServiceVo1.hashCode());
        assertNotEquals(userAvailableServiceVo1.hashCode(), userAvailableServiceVo2.hashCode());

        userAvailableServiceVo1.setCreateTime(createTime);
        assertNotEquals(userAvailableServiceVo, userAvailableServiceVo1);
        assertNotEquals(userAvailableServiceVo1, userAvailableServiceVo2);
        assertNotEquals(userAvailableServiceVo.hashCode(), userAvailableServiceVo1.hashCode());
        assertNotEquals(userAvailableServiceVo1.hashCode(), userAvailableServiceVo2.hashCode());

        userAvailableServiceVo1.setLastModifiedTime(lastModifiedTime);
        assertNotEquals(userAvailableServiceVo, userAvailableServiceVo1);
        assertNotEquals(userAvailableServiceVo1, userAvailableServiceVo2);
        assertNotEquals(userAvailableServiceVo.hashCode(), userAvailableServiceVo1.hashCode());
        assertNotEquals(userAvailableServiceVo1.hashCode(), userAvailableServiceVo2.hashCode());

        userAvailableServiceVo1.setServiceRegistrationState(serviceRegistrationState);
        assertEquals(userAvailableServiceVo, userAvailableServiceVo1);
        assertNotEquals(userAvailableServiceVo1, userAvailableServiceVo2);
        assertEquals(userAvailableServiceVo.hashCode(), userAvailableServiceVo1.hashCode());
        assertNotEquals(userAvailableServiceVo1.hashCode(), userAvailableServiceVo2.hashCode());
    }

    @Test
    void testToString() {
        String expectedToString = "UserAvailableServiceVo(" +
                "id=" + uuid + ", " +
                "category=" + category + ", " +
                "name=" + name + ", " +
                "version=" + version + ", " +
                "csp=" + csp + ", " +
                "regions=" + regions + ", " +
                "description=" + description + ", " +
                "namespace=" + namespace + ", " +
                "icon=" + icon + ", " +
                "deployment=" + DEPLOYMENT + ", " +
                "variables=" + variables + ", " +
                "flavors=" + flavors + ", " +
                "billing=" + billing + ", " +
                "createTime=" + createTime + ", " +
                "lastModifiedTime=" + lastModifiedTime + ", " +
                "serviceRegistrationState=" + serviceRegistrationState + ")";

        assertEquals(expectedToString, userAvailableServiceVo.toString());
    }

}
