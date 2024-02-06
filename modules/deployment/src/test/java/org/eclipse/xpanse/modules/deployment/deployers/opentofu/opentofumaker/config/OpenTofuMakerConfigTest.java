package org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofumaker.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class OpenTofuMakerConfigTest {

    private OpenTofuMakerConfig openTofuMakerConfigUnderTest;

    @BeforeEach
    void setUp() {
        openTofuMakerConfigUnderTest = new OpenTofuMakerConfig();
        ReflectionTestUtils.setField(openTofuMakerConfigUnderTest, "clientBaseUri", "");
        ReflectionTestUtils.setField(openTofuMakerConfigUnderTest, "deployCallbackUri",
                "/webhook/tofu-maker/deploy");
        ReflectionTestUtils.setField(openTofuMakerConfigUnderTest, "destroyCallbackUri",
                "/webhook/tofu-maker/destroy");
    }

    @Test
    void testGetClientBaseUri() {
        assertThat(openTofuMakerConfigUnderTest.getClientBaseUri()).isEqualTo("");
    }

    @Test
    void testGetDeployCallbackUri() {
        assertThat(openTofuMakerConfigUnderTest.getDeployCallbackUri())
                .isEqualTo("/webhook/tofu-maker/deploy");
    }

    @Test
    void testGetDestroyCallbackUri() {
        assertThat(openTofuMakerConfigUnderTest.getDestroyCallbackUri())
                .isEqualTo("/webhook/tofu-maker/destroy");
    }
}
