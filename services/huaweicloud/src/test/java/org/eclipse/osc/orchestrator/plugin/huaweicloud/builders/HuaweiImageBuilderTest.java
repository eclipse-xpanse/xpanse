package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HuaweiImageBuilderTest {

    @Test
    public void builderTest() {
        HuaweiImageBuilder builer = new HuaweiImageBuilder(null);
        Assertions.assertDoesNotThrow(() -> builer.create(null));
    }
}
