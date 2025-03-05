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
    private final Map<String, String> deployedServiceProperties = new HashMap<>();
    private final UUID serviceTemplateId = UUID.fromString("20424910-5f64-4984-84f0-6013c63c64f4");
    private final ServiceDeploymentState serviceDeploymentState =
            ServiceDeploymentState.DEPLOY_SUCCESS;
    private final ServiceState SERVICE_STATE = ServiceState.NOT_RUNNING;
    private final OffsetDateTime createdTime = OffsetDateTime.now();
    private final OffsetDateTime lastModifiedTime = OffsetDateTime.now();
    private final OffsetDateTime LAST_STARTED_AT = OffsetDateTime.now();
    private final OffsetDateTime LAST_STOPPED_AT = OffsetDateTime.now();
    private final ServiceLockConfig LOCK_CONFIG = new ServiceLockConfig();
    private final ServiceConfigurationDetails serviceConfigurationDetails =
            new ServiceConfigurationDetails();

    private DeployedService test;

    @BeforeEach
    void setUp() {
        test = new DeployedService();
        test.setServiceId(uuid);
        test.setUserId(userId);
        test.setCategory(category);
        test.setName(name);
        test.setCustomerServiceName(customerServiceName);
        test.setVersion(version);
        test.setCsp(csp);
        test.setServiceHostingType(serviceHostingType);
        test.setRegion(region);
        test.setAvailabilityZones(availabilityZones);
        test.setFlavor(flavor);
        test.setBillingMode(billingMode);
        test.setInputProperties(inputProperties);
        test.setDeployedServiceProperties(deployedServiceProperties);
        test.setServiceTemplateId(serviceTemplateId);
        test.setServiceDeploymentState(serviceDeploymentState);
        test.setServiceState(SERVICE_STATE);
        test.setCreatedTime(createdTime);
        test.setLastModifiedTime(lastModifiedTime);
        test.setLastStartedAt(LAST_STARTED_AT);
        test.setLastStoppedAt(LAST_STOPPED_AT);
        test.setLockConfig(LOCK_CONFIG);
        test.setServiceConfigurationDetails(serviceConfigurationDetails);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(uuid, test.getServiceId());
        assertEquals(userId, test.getUserId());
        assertEquals(category, test.getCategory());
        assertEquals(name, test.getName());
        assertEquals(customerServiceName, test.getCustomerServiceName());
        assertEquals(version, test.getVersion());
        assertEquals(csp, test.getCsp());
        assertEquals(serviceHostingType, test.getServiceHostingType());
        assertEquals(region, test.getRegion());
        assertEquals(flavor, test.getFlavor());
        assertEquals(billingMode, test.getBillingMode());
        assertEquals(inputProperties, test.getInputProperties());
        assertEquals(deployedServiceProperties, test.getDeployedServiceProperties());
        assertEquals(serviceTemplateId, test.getServiceTemplateId());
        assertEquals(serviceDeploymentState, test.getServiceDeploymentState());
        assertEquals(createdTime, test.getCreatedTime());
        assertEquals(lastModifiedTime, test.getLastModifiedTime());
        assertEquals(LAST_STARTED_AT, test.getLastStartedAt());
        assertEquals(LAST_STOPPED_AT, test.getLastStoppedAt());
        assertEquals(SERVICE_STATE, test.getServiceState());
        assertEquals(LOCK_CONFIG, test.getLockConfig());
        assertEquals(serviceConfigurationDetails, test.getServiceConfigurationDetails());
    }

    @Test
    public void testEqualsAndHashCode() {

        Object obj = new Object();
        assertNotEquals(test, obj);
        assertNotEquals(test.hashCode(), obj.hashCode());

        DeployedService test1 = new DeployedService();
        assertNotEquals(test, test1);
        assertNotEquals(test.hashCode(), test1.hashCode());

        BeanUtils.copyProperties(test, test1);
        assertEquals(test, test1);
        assertEquals(test.hashCode(), test1.hashCode());
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
                        + ", deployedServiceProperties="
                        + deployedServiceProperties
                        + ", serviceTemplateId="
                        + serviceTemplateId
                        + ", userId="
                        + userId
                        + ", serviceDeploymentState="
                        + serviceDeploymentState
                        + ", serviceState="
                        + SERVICE_STATE
                        + ", createdTime="
                        + createdTime
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
        assertEquals(expectedString, test.toString());
    }
}
