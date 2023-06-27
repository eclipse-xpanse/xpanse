/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.deploy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.register.Ocl;
import org.junit.jupiter.api.Test;

/**
 * Test of CreateRequest.
 */
class CreateRequestTest {

    private static final UUID id = UUID.fromString("ed6248d4-2bcd-4e94-84b0-29e014c05137");
    private static final String userName = "userName";
    private static final Category category = Category.COMPUTE;
    private static final String serviceName = "service";
    private static final String customerServiceName = "customerService";
    private static final String version = "1.0";
    private static final String region = "us-east-1";
    private static final Csp csp = Csp.AWS;
    private static final String flavor = "flavor";
    private static final Ocl ocl = new Ocl();
    private static final Map<String, String> properties = Collections.singletonMap("key", "value");

    @Test
    void testGetterAndSetter() {
        CreateRequest request = new CreateRequest();
        request.setId(id);
        request.setUserName(userName);
        request.setCategory(category);
        request.setServiceName(serviceName);
        request.setCustomerServiceName(customerServiceName);
        request.setVersion(version);
        request.setRegion(region);
        request.setCsp(csp);
        request.setFlavor(flavor);
        request.setOcl(ocl);
        request.setServiceRequestProperties(properties);

        assertEquals(id, request.getId());
        assertEquals(userName, request.getUserName());
        assertEquals(category, request.getCategory());
        assertEquals(serviceName, request.getServiceName());
        assertEquals(customerServiceName, request.getCustomerServiceName());
        assertEquals(version, request.getVersion());
        assertEquals(region, request.getRegion());
        assertEquals(csp, request.getCsp());
        assertEquals(flavor, request.getFlavor());
        assertEquals(ocl, request.getOcl());
        assertEquals(properties, request.getServiceRequestProperties());
    }

    @Test
    void testEqualsAndHashCode() {
        CreateRequest request1 = new CreateRequest();
        request1.setId(id);
        request1.setUserName(userName);
        request1.setCategory(category);
        request1.setServiceName(serviceName);
        request1.setCustomerServiceName(customerServiceName);
        request1.setVersion(version);
        request1.setRegion(region);
        request1.setCsp(csp);
        request1.setFlavor(flavor);
        request1.setOcl(ocl);
        request1.setServiceRequestProperties(properties);

        CreateRequest request2 = new CreateRequest();
        request2.setId(id);
        request2.setUserName(userName);
        request2.setCategory(category);
        request2.setServiceName(serviceName);
        request2.setCustomerServiceName(customerServiceName);
        request2.setVersion(version);
        request2.setRegion(region);
        request2.setCsp(csp);
        request2.setFlavor(flavor);
        request2.setOcl(ocl);
        request2.setServiceRequestProperties(properties);

        CreateRequest request3 = new CreateRequest();
        request3.setId(UUID.fromString("20424910-5f64-4984-84f0-6013c63c64f5"));
        request3.setUserName(userName);
        request3.setCategory(category);
        request3.setServiceName(serviceName);
        request3.setCustomerServiceName(customerServiceName);
        request3.setVersion(version);
        request3.setRegion(region);
        request3.setCsp(csp);
        request3.setFlavor(flavor);
        request3.setOcl(ocl);
        request3.setServiceRequestProperties(properties);

        assertEquals(request1, request1);
        assertEquals(request1, request2);
        assertNotEquals(request1, request3);

        assertEquals(request1.hashCode(), request1.hashCode());
        assertEquals(request1.hashCode(), request2.hashCode());
        assertNotEquals(request1.hashCode(), request3.hashCode());
    }

    @Test
    void testToString() {
        CreateRequest request = new CreateRequest();
        request.setId(id);
        request.setUserName(userName);
        request.setCategory(category);
        request.setServiceName(serviceName);
        request.setCustomerServiceName(customerServiceName);
        request.setVersion(version);
        request.setRegion(region);
        request.setCsp(csp);
        request.setFlavor(flavor);
        request.setOcl(ocl);
        request.setServiceRequestProperties(properties);

        String expectedToString = "CreateRequest(" +
                "id=" + id +
                ", userName=" + userName +
                ", category=" + category +
                ", serviceName=" + serviceName +
                ", customerServiceName=" + customerServiceName +
                ", version=" + version +
                ", region=" + region +
                ", csp=" + csp +
                ", flavor=" + flavor +
                ", ocl=" + ocl +
                ", serviceRequestProperties=" + properties +
                ')';
        assertEquals(expectedToString, request.toString());
    }

}
