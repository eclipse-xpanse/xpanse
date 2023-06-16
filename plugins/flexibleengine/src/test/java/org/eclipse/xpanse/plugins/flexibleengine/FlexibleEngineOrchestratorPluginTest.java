package org.eclipse.xpanse.plugins.flexibleengine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.plugins.flexibleengine.monitor.MetricsService;
import org.eclipse.xpanse.plugins.flexibleengine.monitor.utils.FlexibleEngineMonitorCache;
import org.eclipse.xpanse.plugins.flexibleengine.monitor.utils.FlexibleEngineMonitorConverter;
import org.eclipse.xpanse.plugins.flexibleengine.monitor.utils.RetryTemplateService;
import org.junit.jupiter.api.Test;

public class FlexibleEngineOrchestratorPluginTest {

    private final FlexibleEngineOrchestratorPlugin plugin = new FlexibleEngineOrchestratorPlugin(
            new MetricsService(new FlexibleEngineMonitorConverter(),
                    new FlexibleEngineMonitorCache(),
                    new RetryTemplateService(), null));

    @Test
    void getResourceHandler() {
        assertTrue(plugin.getResourceHandler() instanceof FlexibleEngineTerraformResourceHandler);
    }

    @Test
    void getCsp() {
        assertEquals(Csp.FLEXIBLE_ENGINE, plugin.getCsp());
    }
}
