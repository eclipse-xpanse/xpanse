package org.eclipse.xpanse.plugins.huaweicloud.models;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployResourceKind;
import org.junit.jupiter.api.Test;

class HuaweiResourcePropertyTest {

    @Test
    void testGetPropertiesByResourceKindVm() {
        // Setup
        final Map<String, String> expectedResult = new HashMap<>();
        expectedResult.put("image_id", "image_id");
        expectedResult.put("image_name", "image_name");
        expectedResult.put("ip", "access_ip_v4");
        expectedResult.put("region", "region");

        // Run the test
        final Map<String, String> result =
                HuaweiResourceProperty.getProperties(DeployResourceKind.VM);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetPropertiesByResourceKindContainer() {
        // Setup
        final Map<String, String> expectedResult = new HashMap<>();

        // Run the test
        final Map<String, String> result =
                HuaweiResourceProperty.getProperties(DeployResourceKind.CONTAINER);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetPropertiesByResourceKindVpc() {
        // Setup
        final Map<String, String> expectedResult = new HashMap<>();
        expectedResult.put("subnet", "subnet_id");
        expectedResult.put("vpc", "vpc_id");

        // Run the test
        final Map<String, String> result =
                HuaweiResourceProperty.getProperties(DeployResourceKind.VPC);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetPropertiesByResourceKindPPublicIp() {
        // Setup
        final Map<String, String> expectedResult = new HashMap<>();
        expectedResult.put("ip", "address");

        // Run the test
        final Map<String, String> result =
                HuaweiResourceProperty.getProperties(DeployResourceKind.PUBLIC_IP);

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
                HuaweiResourceProperty.getProperties(DeployResourceKind.VOLUME);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetPropertiesByResourceKindUnknown() {
        // Setup
        final Map<String, String> expectedResult = new HashMap<>();

        // Run the test
        final Map<String, String> result =
                HuaweiResourceProperty.getProperties(DeployResourceKind.UNKNOWN);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
}
