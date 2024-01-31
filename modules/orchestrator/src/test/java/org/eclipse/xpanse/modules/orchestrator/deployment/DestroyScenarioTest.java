package org.eclipse.xpanse.modules.orchestrator.deployment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.junit.jupiter.api.Test;

class DestroyScenarioTest {

    @Test
    void testGetByValue() {
        assertEquals(DestroyScenario.getByValue("destroy"), DestroyScenario.DESTROY);
        assertEquals(DestroyScenario.getByValue("rollback"), DestroyScenario.ROLLBACK);
        assertEquals(DestroyScenario.getByValue("PUrge"), DestroyScenario.PURGE);
        assertThrows(UnsupportedEnumValueException.class,
                () -> DestroyScenario.getByValue("unavailable"));
    }

    @Test
    void testToValue() {
        assertEquals(DestroyScenario.DESTROY.toValue(), "destroy");
        assertEquals(DestroyScenario.ROLLBACK.toValue(), "rollback");
        assertEquals(DestroyScenario.PURGE.toValue(), "purge");
    }
}
