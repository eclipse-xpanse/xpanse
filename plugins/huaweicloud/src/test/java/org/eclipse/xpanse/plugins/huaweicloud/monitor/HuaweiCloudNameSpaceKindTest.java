package org.eclipse.xpanse.plugins.huaweicloud.monitor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HuaweiCloudNameSpaceKindTest {


    @Test
    void testToValue() {
        assertThat(HuaweiCloudNameSpaceKind.ECS_SYS.toValue()).isEqualTo("SYS.ECS");
        assertThat(HuaweiCloudNameSpaceKind.ECS_AGT.toValue()).isEqualTo("AGT.ECS");
    }

    @Test
    void testGetByValue() {
        assertThat(HuaweiCloudNameSpaceKind.ECS_SYS.getByValue("SYS.ECS"))
                .isEqualTo(HuaweiCloudNameSpaceKind.ECS_SYS);
        assertThat(HuaweiCloudNameSpaceKind.ECS_AGT.getByValue("AGT.ECS"))
                .isEqualTo(HuaweiCloudNameSpaceKind.ECS_AGT);
        assertThat(HuaweiCloudNameSpaceKind.ECS_AGT.getByValue("NULL"))
                .isEqualTo(null);
    }
}
