package org.eclipse.xpanse.plugins.flexibleengine.models;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployResourceKind;
import org.junit.jupiter.api.Test;

class FlexibleEngineResourcePropertyTest {

    @Test
    void testGetVmProperties_VM() {
        // Setup
        final Map<String, String> expectedResult = new HashMap<>();
        expectedResult.put("image_id", "image_id");
        expectedResult.put("image_name", "image_name");
        expectedResult.put("ip", "access_ip_v4");
        expectedResult.put("project_id", "owner");
        expectedResult.put("region", "region");

        // Run the test
        final Map<String, String> result =
                FlexibleEngineResourceProperty.getProperties(DeployResourceKind.VM);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetProperties_CONTAINER() {
        // Setup
        final Map<String, String> expectedResult = new HashMap<>();

        // Run the test
        final Map<String, String> result =
                FlexibleEngineResourceProperty.getProperties(DeployResourceKind.CONTAINER);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetProperties_VPC() {
        // Setup
        final Map<String, String> expectedResult = new HashMap<>();
        expectedResult.put("subnet", "subnet_id");
        expectedResult.put("vpc", "vpc_id");

        // Run the test
        final Map<String, String> result =
                FlexibleEngineResourceProperty.getProperties(DeployResourceKind.VPC);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetProperties_PUBLIC_IP() {
        // Setup
        final Map<String, String> expectedResult = new HashMap<>();
        expectedResult.put("ip", "address");

        // Run the test
        final Map<String, String> result =
                FlexibleEngineResourceProperty.getProperties(DeployResourceKind.PUBLIC_IP);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }


    @Test
    void testGetProperties_VOLUME() {
        // Setup
        final Map<String, String> expectedResult = new HashMap<>();
        expectedResult.put("size", "size");
        expectedResult.put("type", "volume_type");

        // Run the test
        final Map<String, String> result =
                FlexibleEngineResourceProperty.getProperties(DeployResourceKind.VOLUME);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetProperties_UNKNOWN() {
        // Setup
        final Map<String, String> expectedResult = new HashMap<>();

        // Run the test
        final Map<String, String> result =
                FlexibleEngineResourceProperty.getProperties(DeployResourceKind.UNKNOWN);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
}
