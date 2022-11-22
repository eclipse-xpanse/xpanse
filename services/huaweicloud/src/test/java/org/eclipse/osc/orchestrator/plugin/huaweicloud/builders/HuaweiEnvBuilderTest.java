package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HuaweiEnvBuilderTest {

    @Test
    public void builderTest() {
        HuaweiEnvBuilder builer = new HuaweiEnvBuilder(null);
        Assertions.assertDoesNotThrow(() -> builer.create(null));
    }
}
