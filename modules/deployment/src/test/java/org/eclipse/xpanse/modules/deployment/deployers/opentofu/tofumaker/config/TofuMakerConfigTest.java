package org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class TofuMakerConfigTest {

    private TofuMakerConfig tofuMakerConfigUnderTest;

    @BeforeEach
    void setUp() {
        tofuMakerConfigUnderTest = new TofuMakerConfig();
        ReflectionTestUtils.setField(tofuMakerConfigUnderTest, "clientBaseUri", "");
        ReflectionTestUtils.setField(tofuMakerConfigUnderTest, "deployCallbackUri",
                "/webhook/tofu-maker/deploy");
        ReflectionTestUtils.setField(tofuMakerConfigUnderTest, "modifyCallbackUri",
                "/webhook/tofu-maker/modify");
        ReflectionTestUtils.setField(tofuMakerConfigUnderTest, "destroyCallbackUri",
                "/webhook/tofu-maker/destroy");
    }

    @Test
    void testGetClientBaseUri() {
        assertThat(tofuMakerConfigUnderTest.getClientBaseUri()).isEqualTo("");
    }

    @Test
    void testGetDeployCallbackUri() {
        assertThat(tofuMakerConfigUnderTest.getDeployCallbackUri())
                .isEqualTo("/webhook/tofu-maker/deploy");
    }

    @Test
    void testGetModifyCallbackUri() {
        assertThat(tofuMakerConfigUnderTest.getModifyCallbackUri())
                .isEqualTo("/webhook/tofu-maker/modify");
    }

    @Test
    void testGetDestroyCallbackUri() {
        assertThat(tofuMakerConfigUnderTest.getDestroyCallbackUri())
                .isEqualTo("/webhook/tofu-maker/destroy");
    }
}
