/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.deployment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.Map;
import org.eclipse.xpanse.modules.models.billing.enums.BillingMode;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

/** Test of CreateRequest. */
class DeployRequestTest {

    private final boolean IS_ACCEPT_EULA = true;
    private final Category category = Category.COMPUTE;
    private final String serviceName = "service";
    private final String customerServiceName = "customerService";
    private final String version = "1.0";
    private final String regionName = "us-east-1";
    private final String areaName = "Asia China";
    private final Region region = new Region();
    private final Csp csp = Csp.HUAWEI_CLOUD;
    private final String flavor = "flavor";
    private final ServiceHostingType serviceHostingType = ServiceHostingType.SELF;
    private final Map<String, Object> properties = Collections.singletonMap("key", "value");
    private final Map<String, String> availabilityZones = Collections.singletonMap("key", "value");
    private final BillingMode billingMode = BillingMode.FIXED;
    private DeployRequest request;

    @BeforeEach
    void setUp() {
        request = new DeployRequest();
        request.setCategory(category);
        request.setServiceName(serviceName);
        request.setCustomerServiceName(customerServiceName);
        request.setVersion(version);
        region.setName(regionName);
        region.setArea(areaName);
        request.setRegion(region);
        request.setCsp(csp);
        request.setFlavor(flavor);
        request.setServiceRequestProperties(properties);
        request.setServiceHostingType(serviceHostingType);
        request.setAvailabilityZones(availabilityZones);
        request.setEulaAccepted(IS_ACCEPT_EULA);
        request.setBillingMode(billingMode);
    }

    @Test
    void testGetters() {
        assertEquals(category, request.getCategory());
        assertEquals(serviceName, request.getServiceName());
        assertEquals(customerServiceName, request.getCustomerServiceName());
        assertEquals(version, request.getVersion());
        assertEquals(region, request.getRegion());
        assertEquals(areaName, request.getRegion().getArea());
        assertEquals(regionName, request.getRegion().getName());
        assertEquals(csp, request.getCsp());
        assertEquals(flavor, request.getFlavor());
        assertEquals(properties, request.getServiceRequestProperties());
        assertEquals(availabilityZones, request.getAvailabilityZones());
        assertEquals(serviceHostingType, request.getServiceHostingType());
        assertEquals(IS_ACCEPT_EULA, request.isEulaAccepted());
        assertEquals(billingMode, request.getBillingMode());
    }

    @Test
    void testEqualsAndHashCode() {
        Object obj = new Object();
        assertThat(request).isNotEqualTo(obj);
        assertThat(request.hashCode()).isNotEqualTo(obj.hashCode());
        DeployRequest test1 = new DeployRequest();
        assertThat(request).isNotEqualTo(test1);
        assertThat(request.hashCode()).isNotEqualTo(test1.hashCode());
        DeployRequest test2 = new DeployRequest();
        BeanUtils.copyProperties(request, test2);
        assertThat(request).isEqualTo(test2);
        assertThat(request.hashCode()).isEqualTo(test2.hashCode());
    }

    @Test
    void testCanEqual() {
        assertThat(request.canEqual("other")).isFalse();
        assertThat(request.canEqual(new DeployRequest())).isTrue();
    }

    @Test
    void testToString() {
        String expectedToString =
                "DeployRequest(category="
                        + category
                        + ", serviceName="
                        + serviceName
                        + ", customerServiceName="
                        + customerServiceName
                        + ", version="
                        + version
                        + ", region="
                        + region
                        + ", csp="
                        + csp
                        + ", flavor="
                        + flavor
                        + ", serviceHostingType="
                        + serviceHostingType
                        + ", serviceRequestProperties="
                        + properties
                        + ", availabilityZones="
                        + availabilityZones
                        + ", isEulaAccepted="
                        + IS_ACCEPT_EULA
                        + ", billingMode="
                        + billingMode
                        + ")";
        assertEquals(expectedToString, request.toString());
    }
}
