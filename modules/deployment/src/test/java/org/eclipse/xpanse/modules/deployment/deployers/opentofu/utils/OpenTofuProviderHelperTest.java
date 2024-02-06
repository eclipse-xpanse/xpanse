package org.eclipse.xpanse.modules.deployment.deployers.opentofu.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.eclipse.xpanse.modules.deployment.deployers.opentofu.exceptions.OpenTofuProviderNotFoundException;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OpenTofuProviderHelperTest {

    @Mock
    private PluginManager mockPluginManager;

    private OpenTofuProviderHelper openTofuProviderHelperUnderTest;

    @BeforeEach
    void setUp() {
        openTofuProviderHelperUnderTest = new OpenTofuProviderHelper();
        openTofuProviderHelperUnderTest.pluginManager = mockPluginManager;
    }

    @Test
    void testGetProvider() {
        // Setup
        when(mockPluginManager.getDeployerProvider(Csp.HUAWEI, DeployerKind.OPEN_TOFU,
                "region")).thenReturn("result");

        // Run the test
        final String result = openTofuProviderHelperUnderTest.getProvider(Csp.HUAWEI, "region");

        // Verify the results
        assertThat(result).isEqualTo("result");
    }


    @Test
    void testGetProvider_ThrowsOpenTofuProviderNotFoundException() {
        // Setup
        when(mockPluginManager.getDeployerProvider(Csp.HUAWEI, DeployerKind.OPEN_TOFU,
                "region")).thenReturn(null);
        // Run the test
        assertThatThrownBy(() -> openTofuProviderHelperUnderTest.getProvider(Csp.HUAWEI,
                "region")).isInstanceOf(OpenTofuProviderNotFoundException.class);
    }
}
