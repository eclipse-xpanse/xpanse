package org.eclipse.xpanse.modules.security.zitadel.config;

import org.eclipse.xpanse.modules.logging.RestTemplateLoggingInterceptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class ZitadelRestTemplateConfigTest {

    @Mock
    private RestTemplateLoggingInterceptor mockRestTemplateLoggingInterceptor;

    @InjectMocks
    private ZitadelRestTemplateConfig zitadelRestTemplateConfigUnderTest;

    @Test
    void testZitadelRestTemplate() {
        // Setup
        // Run the test
        final RestTemplate result = zitadelRestTemplateConfigUnderTest.zitadelRestTemplate();
        // Verify the results
        Assertions.assertNotNull(result);
    }
}
