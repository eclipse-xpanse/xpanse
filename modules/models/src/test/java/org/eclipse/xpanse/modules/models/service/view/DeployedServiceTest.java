/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceState;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of DeployedService.
 */
class DeployedServiceTest {

    private static final UUID uuid = UUID.fromString("20424910-5f64-4984-84f0-6013c63c64f5");
    private static final String userId = "userId";
    private static final Category category = Category.COMPUTE;
    private static final String name = "kafka";
    private static final String customerServiceName = "customerServiceName";
    private static final String version = "1.0.0";
    private static final Csp csp = Csp.AWS;
    private static final String flavor = "basic";
    private static final UUID serviceTemplateId =
            UUID.fromString("20424910-5f64-4984-84f0-6013c63c64f4");
    private static final ServiceDeploymentState serviceDeploymentState =
            ServiceDeploymentState.DEPLOY_SUCCESS;
    private static final ServiceState SERVICE_STATE = ServiceState.NOT_RUNNING;
    private static final OffsetDateTime createTime = OffsetDateTime.now();
    private static final OffsetDateTime lastModifiedTime = OffsetDateTime.now();
    private static DeployedService deployedService;
    private static final OffsetDateTime LAST_STARTED_AT = OffsetDateTime.now();
    private static final OffsetDateTime LAST_STOPPED_AT = OffsetDateTime.now();

    @BeforeEach
    void setUp() {
        deployedService = new DeployedService();
        deployedService.setId(uuid);
        deployedService.setCategory(category);
        deployedService.setName(name);
        deployedService.setCustomerServiceName(customerServiceName);
        deployedService.setVersion(version);
        deployedService.setCsp(csp);
        deployedService.setFlavor(flavor);
        deployedService.setServiceTemplateId(serviceTemplateId);
        deployedService.setServiceDeploymentState(serviceDeploymentState);
        deployedService.setServiceState(SERVICE_STATE);
        deployedService.setCreateTime(createTime);
        deployedService.setLastModifiedTime(lastModifiedTime);
        deployedService.setServiceHostingType(ServiceHostingType.SERVICE_VENDOR);
        deployedService.setLastStartedAt(LAST_STARTED_AT);
        deployedService.setLastStoppedAt(LAST_STOPPED_AT);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(uuid, deployedService.getId());
        assertEquals(category, deployedService.getCategory());
        assertEquals(name, deployedService.getName());
        assertEquals(customerServiceName, deployedService.getCustomerServiceName());
        assertEquals(version, deployedService.getVersion());
        assertEquals(csp, deployedService.getCsp());
        assertEquals(flavor, deployedService.getFlavor());
        assertEquals(serviceTemplateId, deployedService.getServiceTemplateId());
        assertEquals(serviceDeploymentState, deployedService.getServiceDeploymentState());
        assertEquals(createTime, deployedService.getCreateTime());
        assertEquals(lastModifiedTime, deployedService.getLastModifiedTime());
        assertEquals(LAST_STARTED_AT, deployedService.getLastStartedAt());
        assertEquals(LAST_STOPPED_AT, deployedService.getLastStoppedAt());
    }

    @Test
    public void testEqualsAndHashCode() {
        assertEquals(deployedService.hashCode(), deployedService.hashCode());

        Object obj = new Object();
        assertNotEquals(deployedService, obj);
        assertNotEquals(deployedService, null);
        assertNotEquals(deployedService.hashCode(), obj.hashCode());

        DeployedService deployedService1 = new DeployedService();
        DeployedService deployedService2 = new DeployedService();
        assertNotEquals(deployedService, deployedService1);
        assertNotEquals(deployedService, deployedService2);
        assertEquals(deployedService1, deployedService2);
        assertNotEquals(deployedService.hashCode(), deployedService1.hashCode());
        assertNotEquals(deployedService.hashCode(), deployedService2.hashCode());
        assertEquals(deployedService1.hashCode(), deployedService2.hashCode());

        deployedService1.setId(uuid);
        assertNotEquals(deployedService, deployedService1);
        assertNotEquals(deployedService1, deployedService2);
        assertNotEquals(deployedService.hashCode(), deployedService1.hashCode());
        assertNotEquals(deployedService1.hashCode(), deployedService2.hashCode());

        assertNotEquals(deployedService, deployedService1);
        assertNotEquals(deployedService1, deployedService2);
        assertNotEquals(deployedService.hashCode(), deployedService1.hashCode());
        assertNotEquals(deployedService1.hashCode(), deployedService2.hashCode());

        deployedService1.setCategory(category);
        assertNotEquals(deployedService, deployedService1);
        assertNotEquals(deployedService1, deployedService2);
        assertNotEquals(deployedService.hashCode(), deployedService1.hashCode());
        assertNotEquals(deployedService1.hashCode(), deployedService2.hashCode());

        deployedService1.setName(name);
        assertNotEquals(deployedService, deployedService1);
        assertNotEquals(deployedService1, deployedService2);
        assertNotEquals(deployedService.hashCode(), deployedService1.hashCode());
        assertNotEquals(deployedService1.hashCode(), deployedService2.hashCode());

        deployedService1.setCustomerServiceName(customerServiceName);
        assertNotEquals(deployedService, deployedService1);
        assertNotEquals(deployedService1, deployedService2);
        assertNotEquals(deployedService.hashCode(), deployedService1.hashCode());
        assertNotEquals(deployedService1.hashCode(), deployedService2.hashCode());

        deployedService1.setVersion(version);
        assertNotEquals(deployedService, deployedService1);
        assertNotEquals(deployedService1, deployedService2);
        assertNotEquals(deployedService.hashCode(), deployedService1.hashCode());
        assertNotEquals(deployedService1.hashCode(), deployedService2.hashCode());

        deployedService1.setCsp(csp);
        assertNotEquals(deployedService, deployedService1);
        assertNotEquals(deployedService1, deployedService2);
        assertNotEquals(deployedService.hashCode(), deployedService1.hashCode());
        assertNotEquals(deployedService1.hashCode(), deployedService2.hashCode());

        deployedService1.setFlavor(flavor);
        assertNotEquals(deployedService, deployedService1);
        assertNotEquals(deployedService1, deployedService2);
        assertNotEquals(deployedService.hashCode(), deployedService1.hashCode());
        assertNotEquals(deployedService1.hashCode(), deployedService2.hashCode());

        deployedService1.setServiceTemplateId(serviceTemplateId);
        assertNotEquals(deployedService, deployedService1);
        assertNotEquals(deployedService1, deployedService2);
        assertNotEquals(deployedService.hashCode(), deployedService1.hashCode());
        assertNotEquals(deployedService1.hashCode(), deployedService2.hashCode());

        deployedService1.setServiceDeploymentState(serviceDeploymentState);
        assertNotEquals(deployedService, deployedService1);
        assertNotEquals(deployedService1, deployedService2);
        assertNotEquals(deployedService.hashCode(), deployedService1.hashCode());
        assertNotEquals(deployedService1.hashCode(), deployedService2.hashCode());

        deployedService1.setCreateTime(createTime);
        assertNotEquals(deployedService, deployedService1);
        assertNotEquals(deployedService1, deployedService2);
        assertNotEquals(deployedService.hashCode(), deployedService1.hashCode());
        assertNotEquals(deployedService1.hashCode(), deployedService2.hashCode());

        deployedService1.setLastModifiedTime(lastModifiedTime);
        deployedService1.setServiceHostingType(ServiceHostingType.SERVICE_VENDOR);
        assertNotEquals(deployedService, deployedService1);
        assertNotEquals(deployedService1, deployedService2);
        assertNotEquals(deployedService.hashCode(), deployedService1.hashCode());
        assertNotEquals(deployedService1.hashCode(), deployedService2.hashCode());

        deployedService1.setLastStartedAt(LAST_STARTED_AT);
        deployedService1.setServiceHostingType(ServiceHostingType.SERVICE_VENDOR);
        assertNotEquals(deployedService, deployedService1);
        assertNotEquals(deployedService1, deployedService2);
        assertNotEquals(deployedService.hashCode(), deployedService1.hashCode());
        assertNotEquals(deployedService1.hashCode(), deployedService2.hashCode());

        deployedService1.setLastStoppedAt(LAST_STOPPED_AT);
        deployedService1.setServiceHostingType(ServiceHostingType.SERVICE_VENDOR);
        assertEquals(deployedService, deployedService1);
        assertNotEquals(deployedService1, deployedService2);
        assertEquals(deployedService.hashCode(), deployedService1.hashCode());
        assertNotEquals(deployedService1.hashCode(), deployedService2.hashCode());
    }

    @Test
    void testToString() {
        String expectedString = "DeployedService(" +
                "id=" + uuid +
                ", category=" + category +
                ", name=" + name +
                ", customerServiceName=" + customerServiceName +
                ", version=" + version +
                ", csp=" + csp +
                ", flavor=" + flavor +
                ", serviceTemplateId=" + serviceTemplateId +
                ", serviceDeploymentState=" + serviceDeploymentState +
                ", serviceState=" + SERVICE_STATE +
                ", serviceHostingType=" + ServiceHostingType.SERVICE_VENDOR +
                ", createTime=" + createTime +
                ", lastModifiedTime=" + lastModifiedTime +
                ", lastStartedAt=" + LAST_STARTED_AT +
                ", lastStoppedAt=" + LAST_STOPPED_AT +
                ")";
        assertEquals(expectedString, deployedService.toString());
    }

}
