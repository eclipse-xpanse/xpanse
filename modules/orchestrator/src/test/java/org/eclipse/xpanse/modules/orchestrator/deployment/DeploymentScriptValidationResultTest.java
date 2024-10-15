package org.eclipse.xpanse.modules.orchestrator.deployment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

class DeploymentScriptValidationResultTest {
    String detail = "detail";
    String deployerVersionUsed = "1.6.0";
    DeployValidateDiagnostics diagnostics = new DeployValidateDiagnostics();
    private DeploymentScriptValidationResult test;

    @BeforeEach
    void setUp() {
        test = new DeploymentScriptValidationResult();
        test.setValid(true);
        diagnostics.setDetail(detail);
        test.setDiagnostics(List.of(diagnostics));
        test.setDeployerVersionUsed(deployerVersionUsed);
    }

    @Test
    void testGetters() {
        assertTrue(test.isValid());
        assertEquals(1, test.getDiagnostics().size());
        assertEquals(deployerVersionUsed, test.getDeployerVersionUsed());
    }

    @Test
    void testEqualsAndHashCode() {
        Object object = new Object();
        assertNotEquals(test, object);
        assertNotEquals(test.hashCode(), object.hashCode());

        DeploymentScriptValidationResult test1 = new DeploymentScriptValidationResult();
        assertNotEquals(test, test1);
        assertNotEquals(test.hashCode(), test1.hashCode());

        BeanUtils.copyProperties(test, test1);
        assertEquals(test, test1);
        assertEquals(test.hashCode(), test1.hashCode());
    }

    @Test
    void testToString() {
        String exceptedString = "DeploymentScriptValidationResult(valid=true"
                + ", deployerVersionUsed=" + deployerVersionUsed
                + ", diagnostics=[DeployValidateDiagnostics(detail=detail)])";
        assertEquals(test.toString(), exceptedString);
    }

}
