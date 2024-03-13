/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.deploy;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

/**
 * Test of CreateRequest.
 */
class DeployRequestTest {

    private final UUID id = UUID.fromString("ed6248d4-2bcd-4e94-84b0-29e014c05137");
    private final String userId = "userId" ;
    private final Category category = Category.COMPUTE;
    private final String serviceName = "service" ;
    private final String customerServiceName = "customerService" ;
    private final String version = "1.0" ;
    private final String regionName = "us-east-1" ;
    private final String areaName = "Asia China" ;
    private final Region region = new Region();
    private final Csp csp = Csp.AWS;
    private final String flavor = "flavor" ;
    private final ServiceHostingType serviceHostingType = ServiceHostingType.SELF;
    private final Map<String, Object> properties = Collections.singletonMap("key", "value");
    private final Map<String, String> availabilityZones = Collections.singletonMap("key", "value");
    private DeployRequest request;

    @BeforeEach
    void setUp() {
        request = new DeployRequest();
        request.setId(id);
        request.setUserId(userId);
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
    }

    @Test
    void testGetters() {
        assertEquals(id, request.getId());
        assertEquals(userId, request.getUserId());
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
        String expectedToString = "DeployRequest(super=DeployRequestBase(" +
                "userId=" + userId +
                ", category=" + category +
                ", serviceName=" + serviceName +
                ", customerServiceName=" + customerServiceName +
                ", version=" + version +
                ", region=" + region +
                ", csp=" + csp +
                ", flavor=" + flavor +
                ", serviceHostingType=" + serviceHostingType +
                ", serviceRequestProperties=" + properties +
                ", availabilityZones=" + availabilityZones +
                "), id=" + id + ")";
        assertEquals(expectedToString, request.toString());
    }

}
