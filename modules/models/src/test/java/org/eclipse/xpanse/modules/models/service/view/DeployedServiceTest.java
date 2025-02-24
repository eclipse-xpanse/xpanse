/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.billing.enums.BillingMode;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.config.ServiceLockConfig;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.statemanagement.enums.ServiceState;
import org.eclipse.xpanse.modules.models.serviceconfiguration.ServiceConfigurationDetails;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

/** Test of DeployedService. */
class DeployedServiceTest {

    private final UUID uuid = UUID.fromString("20424910-5f64-4984-84f0-6013c63c64f5");
    private final String userId = "userId";
    private final Category category = Category.COMPUTE;
    private final String name = "kafka";
    private final String customerServiceName = "customerServiceName";
    private final String version = "1.0.0";
    private final Csp csp = Csp.HUAWEI_CLOUD;
    private final String flavor = "basic";
    private final BillingMode billingMode = BillingMode.FIXED;
    private final Region region = new Region();
    private final ServiceHostingType serviceHostingType = ServiceHostingType.SELF;
    private final Map<String, String> availabilityZones = new HashMap<>();
    private final Map<String, String> inputProperties = new HashMap<>();
    private final UUID serviceTemplateId = UUID.fromString("20424910-5f64-4984-84f0-6013c63c64f4");
    private final ServiceDeploymentState serviceDeploymentState =
            ServiceDeploymentState.DEPLOY_SUCCESS;
    private final ServiceState SERVICE_STATE = ServiceState.NOT_RUNNING;
    private final OffsetDateTime createTime = OffsetDateTime.now();
    private final OffsetDateTime lastModifiedTime = OffsetDateTime.now();
    private final OffsetDateTime LAST_STARTED_AT = OffsetDateTime.now();
    private final OffsetDateTime LAST_STOPPED_AT = OffsetDateTime.now();
    private final ServiceLockConfig LOCK_CONFIG = new ServiceLockConfig();
    private final ServiceConfigurationDetails serviceConfigurationDetails =
            new ServiceConfigurationDetails();
    private DeployedService deployedService;

    @BeforeEach
    void setUp() {
        deployedService = new DeployedService();
        deployedService.setServiceId(uuid);
        deployedService.setUserId(userId);
        deployedService.setCategory(category);
        deployedService.setName(name);
        deployedService.setCustomerServiceName(customerServiceName);
        deployedService.setVersion(version);
        deployedService.setCsp(csp);
        deployedService.setServiceHostingType(serviceHostingType);
        deployedService.setRegion(region);
        deployedService.setAvailabilityZones(availabilityZones);
        deployedService.setFlavor(flavor);
        deployedService.setBillingMode(billingMode);
        deployedService.setInputProperties(inputProperties);
        deployedService.setServiceTemplateId(serviceTemplateId);
        deployedService.setServiceDeploymentState(serviceDeploymentState);
        deployedService.setServiceState(SERVICE_STATE);
        deployedService.setCreateTime(createTime);
        deployedService.setLastModifiedTime(lastModifiedTime);
        deployedService.setLastStartedAt(LAST_STARTED_AT);
        deployedService.setLastStoppedAt(LAST_STOPPED_AT);
        deployedService.setLockConfig(LOCK_CONFIG);
        deployedService.setServiceConfigurationDetails(serviceConfigurationDetails);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(uuid, deployedService.getServiceId());
        assertEquals(userId, deployedService.getUserId());
        assertEquals(category, deployedService.getCategory());
        assertEquals(name, deployedService.getName());
        assertEquals(customerServiceName, deployedService.getCustomerServiceName());
        assertEquals(version, deployedService.getVersion());
        assertEquals(csp, deployedService.getCsp());
        assertEquals(serviceHostingType, deployedService.getServiceHostingType());
        assertEquals(region, deployedService.getRegion());
        assertEquals(flavor, deployedService.getFlavor());
        assertEquals(billingMode, deployedService.getBillingMode());
        assertEquals(inputProperties, deployedService.getInputProperties());
        assertEquals(serviceTemplateId, deployedService.getServiceTemplateId());
        assertEquals(serviceDeploymentState, deployedService.getServiceDeploymentState());
        assertEquals(createTime, deployedService.getCreateTime());
        assertEquals(lastModifiedTime, deployedService.getLastModifiedTime());
        assertEquals(LAST_STARTED_AT, deployedService.getLastStartedAt());
        assertEquals(LAST_STOPPED_AT, deployedService.getLastStoppedAt());
        assertEquals(SERVICE_STATE, deployedService.getServiceState());
        assertEquals(LOCK_CONFIG, deployedService.getLockConfig());
        assertEquals(serviceConfigurationDetails, deployedService.getServiceConfigurationDetails());
    }

    @Test
    public void testEqualsAndHashCode() {

        Object obj = new Object();
        assertNotEquals(deployedService, obj);
        assertNotEquals(deployedService.hashCode(), obj.hashCode());

        DeployedService deployedService1 = new DeployedService();
        assertNotEquals(deployedService, deployedService1);
        assertNotEquals(deployedService.hashCode(), deployedService1.hashCode());

        BeanUtils.copyProperties(deployedService, deployedService1);
        assertEquals(deployedService, deployedService1);
        assertEquals(deployedService.hashCode(), deployedService1.hashCode());
    }

    @Test
    void testToString() {
        String expectedString =
                "DeployedService("
                        + "serviceId="
                        + uuid
                        + ", category="
                        + category
                        + ", name="
                        + name
                        + ", customerServiceName="
                        + customerServiceName
                        + ", version="
                        + version
                        + ", csp="
                        + csp
                        + ", serviceHostingType="
                        + serviceHostingType
                        + ", region=Region(name="
                        + region.getName()
                        + ", site="
                        + region.getSite()
                        + ", area="
                        + region.getArea()
                        + "), availabilityZones="
                        + availabilityZones
                        + ", flavor="
                        + flavor
                        + ", billingMode="
                        + billingMode
                        + ", inputProperties="
                        + inputProperties
                        + ", serviceTemplateId="
                        + serviceTemplateId
                        + ", userId="
                        + userId
                        + ", serviceDeploymentState="
                        + serviceDeploymentState
                        + ", serviceState="
                        + SERVICE_STATE
                        + ", createTime="
                        + createTime
                        + ", lastModifiedTime="
                        + lastModifiedTime
                        + ", lastStartedAt="
                        + LAST_STARTED_AT
                        + ", lastStoppedAt="
                        + LAST_STOPPED_AT
                        + ", lockConfig="
                        + LOCK_CONFIG
                        + ", serviceConfigurationDetails="
                        + serviceConfigurationDetails
                        + ")";
        assertEquals(expectedString, deployedService.toString());
    }
}
