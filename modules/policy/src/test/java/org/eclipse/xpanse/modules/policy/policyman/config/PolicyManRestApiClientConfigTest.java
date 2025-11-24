package org.eclipse.xpanse.modules.policy.policyman.config;

import static org.mockito.Mockito.verify;

import org.eclipse.xpanse.modules.policy.policyman.generated.ApiClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(
        classes = {
            PolicyManRestApiClientConfig.class,
            PolicyManProperties.class,
            RefreshAutoConfiguration.class
        })
@TestPropertySource(properties = {"xpanse.policy-man.endpoint=http://localhost:9090"})
@ExtendWith(SpringExtension.class)
class PolicyManRestApiClientConfigTest {

    @MockitoBean private ApiClient mockApiClient;

    @Autowired private PolicyManRestApiClientConfig policyManRestApiClientConfigUnderTest;

    @Test
    void testApiClientConfig() {

        // Verify the results
        verify(mockApiClient).setBasePath("http://localhost:9090");
    }
}
