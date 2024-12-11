package org.eclipse.xpanse.modules.policy.policyman.config;

import static org.mockito.Mockito.verify;

import org.eclipse.xpanse.modules.policy.policyman.generated.ApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PolicyManRestApiClientConfigTest {

    @Mock private ApiClient mockApiClient;

    @InjectMocks private PolicyManRestApiClientConfig policyManRestApiClientConfigUnderTest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                policyManRestApiClientConfigUnderTest, "policyManBaseUrl", "basePath");
    }

    @Test
    void testApiClientConfig() {
        // Setup
        // Run the test
        policyManRestApiClientConfigUnderTest.apiClientConfig();

        // Verify the results
        verify(mockApiClient).setBasePath("basePath");
    }
}
