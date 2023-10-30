package org.eclipse.xpanse.plugins.flexibleengine.models;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployResourceKind;
import org.junit.jupiter.api.Test;

class FlexibleEngineResourcePropertyTest {

    @Test
    void testGetPropertiesByResourceKindVM() {
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
    void testGetPropertiesByResourceKindContainer() {
        // Setup
        final Map<String, String> expectedResult = new HashMap<>();

        // Run the test
        final Map<String, String> result =
                FlexibleEngineResourceProperty.getProperties(DeployResourceKind.CONTAINER);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetPropertiesByResourceKindVpc() {
        // Setup
        final Map<String, String> expectedResult = new HashMap<>();
        expectedResult.put("cidr", "cidr");
        expectedResult.put("region", "region");

        // Run the test
        final Map<String, String> result =
                FlexibleEngineResourceProperty.getProperties(DeployResourceKind.VPC);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetPropertiesByResourceKindPublicIp() {
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
    void testGetPropertiesByResourceKindVolume() {
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
    void testGetPropertiesByResourceKindUnknown() {
        // Setup
        final Map<String, String> expectedResult = new HashMap<>();

        // Run the test
        final Map<String, String> result =
                FlexibleEngineResourceProperty.getProperties(DeployResourceKind.UNKNOWN);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetPropertiesByResourceKindSubnet() {
        // Setup
        final Map<String, String> expectedResult = new HashMap<>();
        expectedResult.put("vpc", "vpc_id");
        expectedResult.put("subnet", "cidr");
        expectedResult.put("gateway", "gateway_ip");

        // Run the test
        final Map<String, String> result =
                FlexibleEngineResourceProperty.getProperties(DeployResourceKind.SUBNET);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
}
