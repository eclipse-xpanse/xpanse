package org.eclipse.xpanse.modules.orchestrator.deployment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InputValidateDiagnosticsTest {

    String detail = "detail";
    private InputValidateDiagnostics test;

    @BeforeEach
    void setUp() {
        test = new InputValidateDiagnostics();
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

        InputValidateDiagnostics test1 = new InputValidateDiagnostics();
        assertNotEquals(test, test1);
        assertNotEquals(test.hashCode(), test1.hashCode());

        InputValidateDiagnostics test2 = new InputValidateDiagnostics();
        InputValidateDiagnostics test3 = new InputValidateDiagnostics();
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
        String exceptedString = "InputValidateDiagnostics(detail=detail)";
        assertEquals(test.toString(), exceptedString);
    }
}
