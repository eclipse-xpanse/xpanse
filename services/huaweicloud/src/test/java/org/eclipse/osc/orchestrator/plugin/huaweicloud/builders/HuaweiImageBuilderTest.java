package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders;

import org.eclipse.osc.orchestrator.plugin.huaweicloud.exceptions.BuilderException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HuaweiImageBuilderTest {

    @Test
    public void builderTest() {
        HuaweiImageBuilder builder = new HuaweiImageBuilder(null);
        Assertions.assertThrows(BuilderException.class, () -> builder.create(null));
    }
}
