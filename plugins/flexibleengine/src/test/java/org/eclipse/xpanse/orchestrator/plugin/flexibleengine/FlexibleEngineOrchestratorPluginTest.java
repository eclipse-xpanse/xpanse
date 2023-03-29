package org.eclipse.xpanse.orchestrator.plugin.flexibleengine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.xpanse.modules.models.enums.Csp;
import org.junit.jupiter.api.Test;

public class FlexibleEngineOrchestratorPluginTest {

    private final FlexibleEngineOrchestratorPlugin plugin = new FlexibleEngineOrchestratorPlugin();

    @Test
    void getResourceHandler() {
        assertTrue(plugin.getResourceHandler() instanceof FlexibleTerraformResourceHandler);
    }

    @Test
    void getCsp() {
        assertEquals(Csp.FLEXIBLE, plugin.getCsp());
    }
}