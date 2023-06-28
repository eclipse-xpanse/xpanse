package org.eclipse.xpanse.modules.orchestrator.deployment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeployValidateDiagnosticsTest {

    String detail = "detail";
    private DeployValidateDiagnostics test;

    @BeforeEach
    void setUp() {
        test = new DeployValidateDiagnostics();
        test.setDetail(detail);
    }

    @Test
    void testGetters() {
        assertEquals(detail, test.getDetail());
    }


    @Test
    void testEqualsAndHashCode() {
        assertEquals(test, test);
        assertNotEquals(test.hashCode(), 0);

        Object object = new Object();
        assertNotEquals(test, object);
        assertNotEquals(test.hashCode(), object.hashCode());

        DeployValidateDiagnostics test1 = new DeployValidateDiagnostics();
        assertNotEquals(test, test1);
        assertNotEquals(test.hashCode(), test1.hashCode());

        DeployValidateDiagnostics test2 = new DeployValidateDiagnostics();
        DeployValidateDiagnostics test3 = new DeployValidateDiagnostics();
        test2.setDetail(detail);
        test3.setDetail(detail + "1");
        assertNotEquals(test, test1);
        assertEquals(test, test2);
        assertNotEquals(test, test3);
        assertNotEquals(test1, test2);
        assertNotEquals(test2, test3);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test2.hashCode(), test3.hashCode());

    }

    @Test
    void testToString() {
        assertNotEquals(test.toString(), null);
        String exceptedString = "DeployValidateDiagnostics(detail=detail)";
        assertEquals(test.toString(), exceptedString);
    }
}
