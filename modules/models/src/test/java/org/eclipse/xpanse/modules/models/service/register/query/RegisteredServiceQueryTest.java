/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.register.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of RegisteredServiceQuery.
 */
class RegisteredServiceQueryTest {

    private static final Csp csp = Csp.HUAWEI;
    private static final Category category = Category.COMPUTE;
    private static final String serviceName = "kafka";
    private static final String serviceVersion = "v1.0.0";
    private static RegisteredServiceQuery registeredServiceQuery;

    @BeforeEach
    void setUp() {
        registeredServiceQuery = new RegisteredServiceQuery();
        registeredServiceQuery.setCsp(csp);
        registeredServiceQuery.setCategory(category);
        registeredServiceQuery.setServiceName(serviceName);
        registeredServiceQuery.setServiceVersion(serviceVersion);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(csp, registeredServiceQuery.getCsp());
        assertEquals(category, registeredServiceQuery.getCategory());
        assertEquals(serviceName, registeredServiceQuery.getServiceName());
        assertEquals(serviceVersion, registeredServiceQuery.getServiceVersion());
    }

    @Test
    void testEqualsAndHashCode() {
        assertEquals(registeredServiceQuery, registeredServiceQuery);
        assertEquals(registeredServiceQuery.hashCode(), registeredServiceQuery.hashCode());

        Object obj = new Object();
        assertNotEquals(registeredServiceQuery, obj);
        assertNotEquals(registeredServiceQuery, null);
        assertNotEquals(registeredServiceQuery.hashCode(), obj.hashCode());

        RegisteredServiceQuery registeredServiceQuery1 = new RegisteredServiceQuery();
        RegisteredServiceQuery registeredServiceQuery2 = new RegisteredServiceQuery();
        assertNotEquals(registeredServiceQuery, registeredServiceQuery1);
        assertNotEquals(registeredServiceQuery, registeredServiceQuery2);
        assertEquals(registeredServiceQuery1, registeredServiceQuery2);
        assertNotEquals(registeredServiceQuery.hashCode(), registeredServiceQuery1.hashCode());
        assertNotEquals(registeredServiceQuery.hashCode(), registeredServiceQuery2.hashCode());
        assertEquals(registeredServiceQuery1.hashCode(), registeredServiceQuery2.hashCode());

        registeredServiceQuery1.setCsp(csp);
        assertNotEquals(registeredServiceQuery, registeredServiceQuery1);
        assertNotEquals(registeredServiceQuery1, registeredServiceQuery2);
        assertNotEquals(registeredServiceQuery.hashCode(), registeredServiceQuery1.hashCode());
        assertNotEquals(registeredServiceQuery1.hashCode(), registeredServiceQuery2.hashCode());

        registeredServiceQuery1.setCategory(category);
        assertNotEquals(registeredServiceQuery, registeredServiceQuery1);
        assertNotEquals(registeredServiceQuery1, registeredServiceQuery2);
        assertNotEquals(registeredServiceQuery.hashCode(), registeredServiceQuery1.hashCode());
        assertNotEquals(registeredServiceQuery1.hashCode(), registeredServiceQuery2.hashCode());

        registeredServiceQuery1.setServiceName(serviceName);
        assertNotEquals(registeredServiceQuery, registeredServiceQuery1);
        assertNotEquals(registeredServiceQuery1, registeredServiceQuery2);
        assertNotEquals(registeredServiceQuery.hashCode(), registeredServiceQuery1.hashCode());
        assertNotEquals(registeredServiceQuery1.hashCode(), registeredServiceQuery2.hashCode());

        registeredServiceQuery1.setServiceVersion(serviceVersion);
        assertEquals(registeredServiceQuery, registeredServiceQuery1);
        assertNotEquals(registeredServiceQuery1, registeredServiceQuery2);
        assertEquals(registeredServiceQuery.hashCode(), registeredServiceQuery1.hashCode());
        assertNotEquals(registeredServiceQuery1.hashCode(), registeredServiceQuery2.hashCode());
    }

    @Test
    void testToString() {
        String expectedString = "RegisteredServiceQuery(" +
                "csp=" + csp +
                ", category=" + category + "" +
                ", serviceName=" + serviceName + "" +
                ", serviceVersion=" + serviceVersion + "" +
                ")";
        assertEquals(expectedString, registeredServiceQuery.toString());
    }

}
