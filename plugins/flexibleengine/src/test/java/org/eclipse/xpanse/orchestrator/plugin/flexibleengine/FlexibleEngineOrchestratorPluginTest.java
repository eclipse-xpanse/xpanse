package org.eclipse.xpanse.orchestrator.plugin.flexibleengine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.orchestrator.plugin.flexibleengine.monitor.MetricsService;
import org.eclipse.xpanse.orchestrator.plugin.flexibleengine.monitor.utils.FlexibleEngineMonitorCache;
import org.eclipse.xpanse.orchestrator.plugin.flexibleengine.monitor.utils.FlexibleEngineMonitorConverter;
import org.junit.jupiter.api.Test;

public class FlexibleEngineOrchestratorPluginTest {

    private final FlexibleEngineOrchestratorPlugin plugin = new FlexibleEngineOrchestratorPlugin(
            new MetricsService(new FlexibleEngineMonitorConverter(),
                    new FlexibleEngineMonitorCache()));

    @Test
    void getResourceHandler() {
        assertTrue(plugin.getResourceHandler() instanceof FlexibleEngineTerraformResourceHandler);
    }

    @Test
    void getCsp() {
        assertEquals(Csp.FLEXIBLE_ENGINE, plugin.getCsp());
    }
}