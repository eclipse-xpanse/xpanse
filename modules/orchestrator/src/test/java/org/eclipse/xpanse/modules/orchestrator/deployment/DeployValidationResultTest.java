package org.eclipse.xpanse.modules.orchestrator.deployment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeployValidationResultTest {
    String detail = "detail";
    DeployValidateDiagnostics diagnostics = new DeployValidateDiagnostics();
    private DeployValidationResult test;

    @BeforeEach
    void setUp() {
        test = new DeployValidationResult();
        test.setValid(true);
        diagnostics.setDetail(detail);
        test.setDiagnostics(List.of(diagnostics));
    }

    @Test
    void testGetters() {
        assertTrue(test.isValid());
        assertEquals(1, test.getDiagnostics().size());
    }

    @Test
    void testEqualsAndHashCode() {
        assertEquals(test, test);
        assertNotEquals(test.hashCode(), 0);

        Object object = new Object();
        assertNotEquals(test, object);
        assertNotEquals(test.hashCode(), object.hashCode());

        DeployValidationResult test1 = new DeployValidationResult();
        assertNotEquals(test, test1);
        assertNotEquals(test.hashCode(), test1.hashCode());

        DeployValidationResult test2 = new DeployValidationResult();
        DeployValidationResult test3 = new DeployValidationResult();
        test2.setValid(true);
        test3.setValid(false);
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test, test3);
        assertNotEquals(test1, test2);
        assertNotEquals(test2, test3);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test2.hashCode(), test3.hashCode());

        diagnostics.setDetail(detail);
        test2.setDiagnostics(List.of(diagnostics));
        DeployValidateDiagnostics diagnostics1 = new DeployValidateDiagnostics();
        diagnostics1.setDetail(detail+"1");
        test3.setDiagnostics(List.of(diagnostics1));
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

        String exceptedString = "DeployValidationResult(valid=true,"
                + " diagnostics=[DeployValidateDiagnostics(detail=detail)])";
        assertEquals(test.toString(), exceptedString);
    }

}
