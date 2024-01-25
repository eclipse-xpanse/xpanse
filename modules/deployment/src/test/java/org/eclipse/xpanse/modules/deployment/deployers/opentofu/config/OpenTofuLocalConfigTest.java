package org.eclipse.xpanse.modules.deployment.deployers.opentofu.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class OpenTofuLocalConfigTest {

    private OpenTofuLocalConfig openTofuLocalConfigUnderTest;

    @BeforeEach
    void setUp() {
        openTofuLocalConfigUnderTest = new OpenTofuLocalConfig();
        ReflectionTestUtils.setField(openTofuLocalConfigUnderTest, "workspaceDirectory",
                "xpanse_deploy_ws");
        ReflectionTestUtils.setField(openTofuLocalConfigUnderTest, "isDebugEnabled", false);
        ReflectionTestUtils.setField(openTofuLocalConfigUnderTest, "debugLogLevel",
                "debugLogLevel");
    }

    @Test
    void testGetWorkspaceDirectory() {
        assertThat(openTofuLocalConfigUnderTest.getWorkspaceDirectory())
                .isEqualTo("xpanse_deploy_ws");
    }

    @Test
    void testIsDebugEnabled() {
        assertThat(openTofuLocalConfigUnderTest.isDebugEnabled()).isFalse();
    }

    @Test
    void testGetDebugLogLevel() {
        assertThat(openTofuLocalConfigUnderTest.getDebugLogLevel()).isEqualTo("debugLogLevel");
    }
}
