package org.eclipse.xpanse.modules.deployment.deployers.terraform.config;

import org.eclipse.xpanse.modules.logging.RestTemplateLoggingInterceptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class TerraformBootRestTemplateConfigTest {

    @Mock
    private RestTemplateLoggingInterceptor mockRestTemplateLoggingInterceptor;

    private TerraformBootRestTemplateConfig terraformBootRestTemplateConfigUnderTest;

    @BeforeEach
    void setUp() {
        terraformBootRestTemplateConfigUnderTest = new TerraformBootRestTemplateConfig();
        terraformBootRestTemplateConfigUnderTest.restTemplateLoggingInterceptor =
                mockRestTemplateLoggingInterceptor;
    }

    @Test
    void testRestTemplate() {
        // Setup
        // Run the test
        final RestTemplate result = terraformBootRestTemplateConfigUnderTest.restTemplate();

        // Verify the results
        Assertions.assertNotNull(result);
    }
}
