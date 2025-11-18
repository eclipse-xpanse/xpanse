package org.eclipse.xpanse.modules.policy.policyman.config;

import org.eclipse.xpanse.modules.logging.RestTemplateLoggingInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class PolicyManRestTemplateConfigTest {

    @Mock private RestTemplateLoggingInterceptor mockRestTemplateLoggingInterceptor;

    private PolicyManRestTemplateConfig policyManRestTemplateConfigUnderTest;

    @BeforeEach
    void setUp() {
        policyManRestTemplateConfigUnderTest =
                new PolicyManRestTemplateConfig(mockRestTemplateLoggingInterceptor);
    }

    @Test
    void testRestTemplate() {
        // Setup
        // Run the test
        final RestTemplate result = policyManRestTemplateConfigUnderTest.restTemplate();

        // Verify the results
    }
}
