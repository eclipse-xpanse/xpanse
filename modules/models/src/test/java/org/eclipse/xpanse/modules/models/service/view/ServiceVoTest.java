/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Date;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceDeploymentState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of ServiceVo.
 */
class ServiceVoTest {

    private static final UUID uuid = UUID.fromString("20424910-5f64-4984-84f0-6013c63c64f5");
    private static final String userId = "userId";
    private static final Category category = Category.COMPUTE;
    private static final String name = "kafka";
    private static final String customerServiceName = "customerServiceName";
    private static final String version = "v1.0.0";
    private static final Csp csp = Csp.AWS;
    private static final String flavor = "basic";
    private static final ServiceDeploymentState serviceDeploymentState =
            ServiceDeploymentState.DEPLOY_SUCCESS;
    private static final Date createTime = new Date();
    private static final Date lastModifiedTime = new Date();
    private static ServiceVo serviceVo;

    @BeforeEach
    void setUp() {
        serviceVo = new ServiceVo();
        serviceVo.setId(uuid);
        serviceVo.setUserId(userId);
        serviceVo.setCategory(category);
        serviceVo.setName(name);
        serviceVo.setCustomerServiceName(customerServiceName);
        serviceVo.setVersion(version);
        serviceVo.setCsp(csp);
        serviceVo.setFlavor(flavor);
        serviceVo.setServiceDeploymentState(serviceDeploymentState);
        serviceVo.setCreateTime(createTime);
        serviceVo.setLastModifiedTime(lastModifiedTime);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(uuid, serviceVo.getId());
        assertEquals(userId, serviceVo.getUserId());
        assertEquals(category, serviceVo.getCategory());
        assertEquals(name, serviceVo.getName());
        assertEquals(customerServiceName, serviceVo.getCustomerServiceName());
        assertEquals(version, serviceVo.getVersion());
        assertEquals(csp, serviceVo.getCsp());
        assertEquals(flavor, serviceVo.getFlavor());
        assertEquals(serviceDeploymentState, serviceVo.getServiceDeploymentState());
        assertEquals(createTime, serviceVo.getCreateTime());
        assertEquals(lastModifiedTime, serviceVo.getLastModifiedTime());
    }

    @Test
    public void testEqualsAndHashCode() {
        assertEquals(serviceVo, serviceVo);
        assertEquals(serviceVo.hashCode(), serviceVo.hashCode());

        Object obj = new Object();
        assertNotEquals(serviceVo, obj);
        assertNotEquals(serviceVo, null);
        assertNotEquals(serviceVo.hashCode(), obj.hashCode());

        ServiceVo serviceVo1 = new ServiceVo();
        ServiceVo serviceVo2 = new ServiceVo();
        assertNotEquals(serviceVo, serviceVo1);
        assertNotEquals(serviceVo, serviceVo2);
        assertEquals(serviceVo1, serviceVo2);
        assertNotEquals(serviceVo.hashCode(), serviceVo1.hashCode());
        assertNotEquals(serviceVo.hashCode(), serviceVo2.hashCode());
        assertEquals(serviceVo1.hashCode(), serviceVo2.hashCode());

        serviceVo1.setId(uuid);
        assertNotEquals(serviceVo, serviceVo1);
        assertNotEquals(serviceVo1, serviceVo2);
        assertNotEquals(serviceVo.hashCode(), serviceVo1.hashCode());
        assertNotEquals(serviceVo1.hashCode(), serviceVo2.hashCode());

        serviceVo1.setUserId(userId);
        assertNotEquals(serviceVo, serviceVo1);
        assertNotEquals(serviceVo1, serviceVo2);
        assertNotEquals(serviceVo.hashCode(), serviceVo1.hashCode());
        assertNotEquals(serviceVo1.hashCode(), serviceVo2.hashCode());

        serviceVo1.setCategory(category);
        assertNotEquals(serviceVo, serviceVo1);
        assertNotEquals(serviceVo1, serviceVo2);
        assertNotEquals(serviceVo.hashCode(), serviceVo1.hashCode());
        assertNotEquals(serviceVo1.hashCode(), serviceVo2.hashCode());

        serviceVo1.setName(name);
        assertNotEquals(serviceVo, serviceVo1);
        assertNotEquals(serviceVo1, serviceVo2);
        assertNotEquals(serviceVo.hashCode(), serviceVo1.hashCode());
        assertNotEquals(serviceVo1.hashCode(), serviceVo2.hashCode());

        serviceVo1.setCustomerServiceName(customerServiceName);
        assertNotEquals(serviceVo, serviceVo1);
        assertNotEquals(serviceVo1, serviceVo2);
        assertNotEquals(serviceVo.hashCode(), serviceVo1.hashCode());
        assertNotEquals(serviceVo1.hashCode(), serviceVo2.hashCode());

        serviceVo1.setVersion(version);
        assertNotEquals(serviceVo, serviceVo1);
        assertNotEquals(serviceVo1, serviceVo2);
        assertNotEquals(serviceVo.hashCode(), serviceVo1.hashCode());
        assertNotEquals(serviceVo1.hashCode(), serviceVo2.hashCode());

        serviceVo1.setCsp(csp);
        assertNotEquals(serviceVo, serviceVo1);
        assertNotEquals(serviceVo1, serviceVo2);
        assertNotEquals(serviceVo.hashCode(), serviceVo1.hashCode());
        assertNotEquals(serviceVo1.hashCode(), serviceVo2.hashCode());

        serviceVo1.setFlavor(flavor);
        assertNotEquals(serviceVo, serviceVo1);
        assertNotEquals(serviceVo1, serviceVo2);
        assertNotEquals(serviceVo.hashCode(), serviceVo1.hashCode());
        assertNotEquals(serviceVo1.hashCode(), serviceVo2.hashCode());

        serviceVo1.setServiceDeploymentState(serviceDeploymentState);
        assertNotEquals(serviceVo, serviceVo1);
        assertNotEquals(serviceVo1, serviceVo2);
        assertNotEquals(serviceVo.hashCode(), serviceVo1.hashCode());
        assertNotEquals(serviceVo1.hashCode(), serviceVo2.hashCode());

        serviceVo1.setCreateTime(createTime);
        assertNotEquals(serviceVo, serviceVo1);
        assertNotEquals(serviceVo1, serviceVo2);
        assertNotEquals(serviceVo.hashCode(), serviceVo1.hashCode());
        assertNotEquals(serviceVo1.hashCode(), serviceVo2.hashCode());

        serviceVo1.setLastModifiedTime(lastModifiedTime);
        assertEquals(serviceVo, serviceVo1);
        assertNotEquals(serviceVo1, serviceVo2);
        assertEquals(serviceVo.hashCode(), serviceVo1.hashCode());
        assertNotEquals(serviceVo1.hashCode(), serviceVo2.hashCode());
    }

    @Test
    void testToString() {
        String expectedString = "ServiceVo(" +
                "id=" + uuid +
                ", userId=" + userId + "" +
                ", category=" + category +
                ", name=" + name + "" +
                ", customerServiceName=" + customerServiceName + "" +
                ", version=" + version + "" +
                ", csp=" + csp +
                ", flavor=" + flavor + "" +
                ", serviceDeploymentState=" + serviceDeploymentState +
                ", createTime=" + createTime +
                ", lastModifiedTime=" + lastModifiedTime +
                ")";
        assertEquals(expectedString, serviceVo.toString());
    }

}
