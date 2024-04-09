package org.eclipse.xpanse.modules.orchestrator.deployment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.junit.jupiter.api.Test;

class DeploymentScenarioTest {

    @Test
    void testGetByValue() {
        assertEquals(DeploymentScenario.getByValue("destroy"), DeploymentScenario.DESTROY);
        assertEquals(DeploymentScenario.getByValue("rollback"), DeploymentScenario.ROLLBACK);
        assertEquals(DeploymentScenario.getByValue("PUrge"), DeploymentScenario.PURGE);
        assertThrows(UnsupportedEnumValueException.class,
                () -> DeploymentScenario.getByValue("unavailable"));
    }

    @Test
    void testToValue() {
        assertEquals(DeploymentScenario.DESTROY.toValue(), "destroy");
        assertEquals(DeploymentScenario.ROLLBACK.toValue(), "rollback");
        assertEquals(DeploymentScenario.PURGE.toValue(), "purge");
    }
}
