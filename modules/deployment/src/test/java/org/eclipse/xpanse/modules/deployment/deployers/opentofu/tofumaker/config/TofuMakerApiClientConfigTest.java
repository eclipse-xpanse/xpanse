package org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.config;

import static org.mockito.Mockito.verify;

import org.eclipse.xpanse.modules.deployment.config.DeploymentProperties;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.ApiClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(
        classes = {
            TofuMakerApiClientConfig.class,
            DeploymentProperties.class,
            RefreshAutoConfiguration.class
        })
@TestPropertySource(
        properties = {
            "xpanse.deployer.tofu-maker.endpoint=http://localhost:9090",
        })
@ActiveProfiles("tofu-maker")
@ExtendWith(SpringExtension.class)
class TofuMakerApiClientConfigTest {

    @MockitoBean private ApiClient mockApiClient;

    @Autowired private TofuMakerApiClientConfig openTofuMakerApiClientConfigUnderTest;

    @Test
    void testApiClientConfig() {
        // Verify the results
        verify(mockApiClient).setBasePath("http://localhost:9090");
    }
}
