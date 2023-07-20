package org.eclipse.xpanse.modules.models.system;

import org.eclipse.xpanse.modules.models.system.enums.BackendSystemType;
import org.eclipse.xpanse.modules.models.system.enums.HealthStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of BackendSystemStatus.
 */
class BackendSystemStatusTest {

    private BackendSystemStatus test;

    @BeforeEach
    void setUp() {
        test = new BackendSystemStatus();
        test.setBackendSystemType(BackendSystemType.IDENTITY_PROVIDER);
        test.setName("zitadel");
        test.setHealthStatus(HealthStatus.OK);
        test.setEndpoint("endpoint");
        test.setDetails("OK");
    }

    @Test
    void testGetters() {
        Assertions.assertEquals(BackendSystemType.IDENTITY_PROVIDER, test.getBackendSystemType());
        Assertions.assertEquals("zitadel", test.getName());
        Assertions.assertEquals("endpoint", test.getEndpoint());
        Assertions.assertEquals(HealthStatus.OK, test.getHealthStatus());
        Assertions.assertEquals("OK", test.getDetails());
    }


    @Test
    void testEqualsAndHashCode() {

        Assertions.assertEquals(test, test);
        Assertions.assertNotEquals(test.hashCode(), 0);

        Object object = new Object();
        Assertions.assertNotEquals(test, object);
        Assertions.assertNotEquals(test.hashCode(), object.hashCode());

        BackendSystemStatus test1 = new BackendSystemStatus();
        Assertions.assertNotEquals(test, test1);
        Assertions.assertNotEquals(test.hashCode(), test1.hashCode());

        BackendSystemStatus test2 = new BackendSystemStatus();
        BackendSystemStatus test3 = new BackendSystemStatus();
        test2.setBackendSystemType(BackendSystemType.IDENTITY_PROVIDER);
        test3.setBackendSystemType(BackendSystemType.DATABASE);
        Assertions.assertNotEquals(test, test1);
        Assertions.assertNotEquals(test, test2);
        Assertions.assertNotEquals(test, test3);
        Assertions.assertNotEquals(test1, test2);
        Assertions.assertNotEquals(test2, test3);
        Assertions.assertNotEquals(test.hashCode(), test1.hashCode());
        Assertions.assertNotEquals(test2.hashCode(), test3.hashCode());
        test2.setEndpoint("endpoint2");
        test3.setEndpoint("endpoint3");
        Assertions.assertNotEquals(test, test1);
        Assertions.assertNotEquals(test, test2);
        Assertions.assertNotEquals(test, test3);
        Assertions.assertNotEquals(test1, test2);
        Assertions.assertNotEquals(test2, test3);
        Assertions.assertNotEquals(test.hashCode(), test1.hashCode());
        Assertions.assertNotEquals(test2.hashCode(), test3.hashCode());

        test2.setHealthStatus(HealthStatus.OK);
        test3.setHealthStatus(HealthStatus.NOK);
        Assertions.assertNotEquals(test, test1);
        Assertions.assertNotEquals(test, test2);
        Assertions.assertNotEquals(test, test3);
        Assertions.assertNotEquals(test1, test2);
        Assertions.assertNotEquals(test2, test3);
        Assertions.assertNotEquals(test.hashCode(), test1.hashCode());
        Assertions.assertNotEquals(test2.hashCode(), test3.hashCode());

        test2.setName("zitadel");
        test3.setName("h2");
        Assertions.assertNotEquals(test, test1);
        Assertions.assertNotEquals(test, test2);
        Assertions.assertNotEquals(test, test3);
        Assertions.assertNotEquals(test1, test2);
        Assertions.assertNotEquals(test2, test3);
        Assertions.assertNotEquals(test.hashCode(), test1.hashCode());
        Assertions.assertNotEquals(test2.hashCode(), test3.hashCode());

        test2.setDetails("OK");
        test2.setDetails("NOK");
        Assertions.assertNotEquals(test, test1);
        Assertions.assertNotEquals(test, test2);
        Assertions.assertNotEquals(test, test3);
        Assertions.assertNotEquals(test1, test2);
        Assertions.assertNotEquals(test2, test3);
        Assertions.assertNotEquals(test.hashCode(), test1.hashCode());
        Assertions.assertNotEquals(test2.hashCode(), test3.hashCode());
    }


    @Test
    void testToString() {
        String exceptedString = "BackendSystemStatus(backendSystemType=IDENTITY_PROVIDER, "
                + "name=zitadel, healthStatus=OK, endpoint=endpoint, details=OK)";
        Assertions.assertEquals(test.toString(), exceptedString);
        Assertions.assertNotEquals(test.toString(), null);
    }

}
