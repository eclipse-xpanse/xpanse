/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.servicetemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.net.URL;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.utils.ServiceVariablesJsonSchemaGenerator;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceRegistrationState;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.JsonObjectSchema;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
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
    private Ocl ocl;
    private JsonObjectSchema jsonObjectSchema;

    private ServiceTemplateEntity test;

    @BeforeEach
    void setUp() throws Exception {
        OclLoader oclLoader = new OclLoader();
        ocl = oclLoader.getOcl(new URL("file:src/test/resources/ocl_test.yaml"));
        ServiceVariablesJsonSchemaGenerator serviceVariablesJsonSchemaGenerator =
                new ServiceVariablesJsonSchemaGenerator();
        jsonObjectSchema =
                serviceVariablesJsonSchemaGenerator.buildJsonObjectSchema(
                        ocl.getDeployment().getVariables());
        test = new ServiceTemplateEntity();
        test.setId(ID);
        test.setName(NAME);
        test.setVersion(VERSION);
        test.setCsp(CSP);
        test.setCategory(CATEGORY);
        test.setNamespace("test");
        test.setOcl(ocl);
        test.setServiceRegistrationState(SERVICE_STATE);
        test.setJsonObjectSchema(jsonObjectSchema);
    }

    @Test
    void testToString() {
        String expectedToString =
                "ServiceTemplateEntity(id=" + ID + ", "
                        + "name=" + NAME + ", "
                        + "version=" + VERSION + ", "
                        + "csp=" + CSP + ", "
                        + "category=" + CATEGORY + ", "
                        + "namespace=test, "
                        + "serviceHostingType=null" + ", "
                        + "ocl=" + ocl + ", "
                        + "serviceRegistrationState=" + SERVICE_STATE + ", "
                        + "jsonObjectSchema=" + jsonObjectSchema + ")";
        assertEquals(expectedToString, test.toString());
    }

    @Test
    void testEqualsAndHashCode() {
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

        test1.setNamespace("test");
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test1, test2);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setOcl(ocl);
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test1, test2);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setServiceRegistrationState(SERVICE_STATE);
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test1, test2);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setJsonObjectSchema(jsonObjectSchema);
        assertEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test1, test2);
        assertEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test1.hashCode(), test2.hashCode());
    }
}
