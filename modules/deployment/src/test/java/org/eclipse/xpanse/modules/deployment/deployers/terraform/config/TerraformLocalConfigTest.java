package org.eclipse.xpanse.modules.deployment.deployers.terraform.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class TerraformLocalConfigTest {

    private TerraformLocalConfig terraformLocalConfigUnderTest;

    @BeforeEach
    void setUp() {
        terraformLocalConfigUnderTest = new TerraformLocalConfig();
        ReflectionTestUtils.setField(terraformLocalConfigUnderTest, "workspaceDirectory",
                "xpanse_deploy_ws");
        ReflectionTestUtils.setField(terraformLocalConfigUnderTest, "isDebugEnabled", false);
        ReflectionTestUtils.setField(terraformLocalConfigUnderTest, "debugLogLevel",
                "debugLogLevel");
    }

    @Test
    void testGetWorkspaceDirectory() {
        assertThat(terraformLocalConfigUnderTest.getWorkspaceDirectory())
                .isEqualTo("xpanse_deploy_ws");
    }

    @Test
    void testIsDebugEnabled() {
        assertThat(terraformLocalConfigUnderTest.isDebugEnabled()).isFalse();
    }

    @Test
    void testGetDebugLogLevel() {
        assertThat(terraformLocalConfigUnderTest.getDebugLogLevel()).isEqualTo("debugLogLevel");
    }
}
