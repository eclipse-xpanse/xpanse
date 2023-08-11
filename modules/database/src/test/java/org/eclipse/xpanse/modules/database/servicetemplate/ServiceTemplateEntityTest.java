/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.servicetemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.UUID;
import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceRegistrationState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServiceTemplateEntityTest {

    private static final UUID ID = UUID.randomUUID();
    private static final String NAME = "name";
    private static final String VERSION = "2.0";
    private static final Csp CSP = Csp.HUAWEI;
    private static final Category CATEGORY = Category.MIDDLEWARE;
    private static final ServiceRegistrationState SERVICE_STATE =
            ServiceRegistrationState.REGISTERED;
    private static final Ocl OCL = new Ocl();

    private ServiceTemplateEntity test;

    @BeforeEach
    void setUp() {
        test = new ServiceTemplateEntity();
        test.setId(ID);
        test.setName(NAME);
        test.setVersion(VERSION);
        test.setCsp(CSP);
        test.setCategory(CATEGORY);
        test.setOcl(OCL);
        test.setServiceRegistrationState(SERVICE_STATE);
    }

    @Test
    void testToString() {
        String expectedToString =
                "ServiceTemplateEntity(id=" + ID + ", "
                        + "name=" + NAME + ", "
                        + "version=" + VERSION + ", "
                        + "csp=" + CSP + ", "
                        + "category=" + CATEGORY + ", "
                        + "ocl=" + OCL + ", "
                        + "serviceRegistrationState=" + SERVICE_STATE + ")";
        assertEquals(expectedToString, test.toString());
    }

    @Test
    void testEqualsAndHashCode() {
        assertEquals(test, test);
        assertEquals(test.hashCode(), test.hashCode());

        Object o = new Object();
        assertNotEquals(test, o);
        assertNotEquals(test.hashCode(), o.hashCode());

        ServiceTemplateEntity test1 = new ServiceTemplateEntity();
        ServiceTemplateEntity test2 = new ServiceTemplateEntity();
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertEquals(test1, test2);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test.hashCode(), test2.hashCode());
        assertEquals(test1.hashCode(), test2.hashCode());

        test1.setId(ID);
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test1, test2);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setName(NAME);
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test1, test2);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setVersion(VERSION);
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test1, test2);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setCsp(CSP);
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test1, test2);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setCategory(CATEGORY);
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test1, test2);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setOcl(OCL);
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test1, test2);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setServiceRegistrationState(SERVICE_STATE);
        assertEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test1, test2);
        assertEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test1.hashCode(), test2.hashCode());
    }
}
