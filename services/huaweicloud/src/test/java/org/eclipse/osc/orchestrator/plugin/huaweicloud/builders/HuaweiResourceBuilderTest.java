package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HuaweiResourceBuilderTest {

    @Test
    public void builderTest() {
        HuaweiResourceBuilder builder = new HuaweiResourceBuilder(null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> builder.create(null));
    }
}
