package org.eclipse.xpanse.plugins.flexibleengine.monitor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FlexibleEngineNameSpaceKindTest {

    @Test
    void testToValue() {
        assertThat(FlexibleEngineNameSpaceKind.ECS_SYS.toValue()).isEqualTo("SYS.ECS");
        assertThat(FlexibleEngineNameSpaceKind.ECS_AGT.toValue()).isEqualTo("AGT.ECS");
    }

    @Test
    void testGetByValue() {
        assertThat(FlexibleEngineNameSpaceKind.ECS_SYS.getByValue("SYS.ECS"))
                .isEqualTo(FlexibleEngineNameSpaceKind.ECS_SYS);
        assertThat(FlexibleEngineNameSpaceKind.ECS_AGT.getByValue("AGT.ECS"))
                .isEqualTo(FlexibleEngineNameSpaceKind.ECS_AGT);
        assertThat(FlexibleEngineNameSpaceKind.ECS_AGT.getByValue("NULL")).isEqualTo(null);
    }
}
