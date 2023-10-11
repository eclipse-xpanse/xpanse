package org.eclipse.xpanse.modules.deployment.deployers.terraform.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class TerraformBootConfigTest {

    private TerraformBootConfig terraformBootConfigUnderTest;

    @BeforeEach
    void setUp() {
        terraformBootConfigUnderTest = new TerraformBootConfig();
        ReflectionTestUtils.setField(terraformBootConfigUnderTest, "clientBaseUri",
                "http://localhost:9090");
        ReflectionTestUtils.setField(terraformBootConfigUnderTest, "deployCallbackUri",
                "/webhook/deploy/");
        ReflectionTestUtils.setField(terraformBootConfigUnderTest, "destroyCallbackUri",
                "/webhook/destroy/");
    }

    @Test
    void testGetClientBaseUri() {
        assertThat(terraformBootConfigUnderTest.getClientBaseUri()).isEqualTo(
                "http://localhost:9090");
    }

    @Test
    void testGetDeployCallbackUri() {
        assertThat(terraformBootConfigUnderTest.getDeployCallbackUri())
                .isEqualTo("/webhook/deploy/");
    }

    @Test
    void testGetDestroyCallbackUri() {
        assertThat(terraformBootConfigUnderTest.getDestroyCallbackUri())
                .isEqualTo("/webhook/destroy/");
    }
}
