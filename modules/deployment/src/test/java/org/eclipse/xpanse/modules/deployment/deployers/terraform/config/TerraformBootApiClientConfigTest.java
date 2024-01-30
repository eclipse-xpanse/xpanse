package org.eclipse.xpanse.modules.deployment.deployers.terraform.config;

import static org.mockito.Mockito.verify;

import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.ApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TerraformBootApiClientConfigTest {

    @Mock
    private ApiClient mockApiClient;

    @InjectMocks
    private TerraformBootApiClientConfig terraformBootApiClientConfigUnderTest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(terraformBootApiClientConfigUnderTest, "terraformBootBaseUrl",
                "http://localhost:9090");
    }

    @Test
    void testApiClientConfig() {
        // Setup
        // Run the test
        terraformBootApiClientConfigUnderTest.apiClientConfig();

        // Verify the results
        verify(mockApiClient).setBasePath("http://localhost:9090");
    }
}
