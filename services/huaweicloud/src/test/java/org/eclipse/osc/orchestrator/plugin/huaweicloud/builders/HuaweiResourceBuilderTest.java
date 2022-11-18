package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HuaweiResourceBuilderTest {

    @Test
    public void builderTest() {
        HuaweiResourceBuilder builer = new HuaweiResourceBuilder(null);
        Assertions.assertDoesNotThrow(() -> builer.create(null));
    }
}
