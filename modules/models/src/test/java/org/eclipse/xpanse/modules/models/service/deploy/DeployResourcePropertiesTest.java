package org.eclipse.xpanse.modules.models.service.deploy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployResourceKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeployResourcePropertiesTest {

    private DeployResourceProperties deployResourcePropertiesUnderTest;

    @BeforeEach
    void setUp() {
        deployResourcePropertiesUnderTest = new DeployResourceProperties() {
        };
    }

    @Test
    void testGetResourceKind() {
        assertThat(deployResourcePropertiesUnderTest.getResourceKind())
                .isEqualTo(DeployResourceKind.UNKNOWN);
    }

    @Test
    void testGetResourceProperties() {
        // Setup
        final Map<String, String> expectedResult = new HashMap<>();

        // Run the test
        final Map<String, String> result =
                deployResourcePropertiesUnderTest.getResourceProperties();

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
}
