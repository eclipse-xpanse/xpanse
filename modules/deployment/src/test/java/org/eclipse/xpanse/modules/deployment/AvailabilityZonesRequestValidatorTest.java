package org.eclipse.xpanse.modules.deployment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.VariableInvalidException;
import org.eclipse.xpanse.modules.models.servicetemplate.AvailabilityZoneConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AvailabilityZonesRequestValidatorTest {

    @Test
    void testValidateAvailabilityZonesWithEmptyZoneConfig() {
        // Setup
        final Map<String, String> inputMap = new HashMap<>();
        final List<AvailabilityZoneConfig> zoneConfigs = new ArrayList<>();
        Assertions.assertDoesNotThrow(() -> {
            AvailabilityZonesRequestValidator.validateAvailabilityZones(inputMap, zoneConfigs);
        });
    }

    @Test
    void testValidateAvailabilityZonesWithEmptyRequiredZoneConfig() {
        // Setup
        final AvailabilityZoneConfig availabilityZoneConfig = new AvailabilityZoneConfig();
        availabilityZoneConfig.setDisplayName("displayName");
        availabilityZoneConfig.setVarName("varName");
        availabilityZoneConfig.setMandatory(false);
        availabilityZoneConfig.setDescription("description");
        final List<AvailabilityZoneConfig> zoneConfigs = List.of(availabilityZoneConfig);
        final Map<String, String> inputMap = Map.ofEntries(Map.entry("value", "value"));
        // Run the test
        Assertions.assertDoesNotThrow(() -> {
            AvailabilityZonesRequestValidator.validateAvailabilityZones(inputMap, zoneConfigs);
        });
    }

    @Test
    void testValidateAvailabilityZonesWithoutRequiredZoneValues() {
        // Setup
        final AvailabilityZoneConfig availabilityZoneConfig = new AvailabilityZoneConfig();
        availabilityZoneConfig.setDisplayName("displayName");
        availabilityZoneConfig.setVarName("varName");
        availabilityZoneConfig.setMandatory(true);
        availabilityZoneConfig.setDescription("description");
        final List<AvailabilityZoneConfig> zoneConfigs = List.of(availabilityZoneConfig);
        final Map<String, String> inputMap = Map.ofEntries(Map.entry("value", "value"));
        // Run the test
        Assertions.assertThrows(VariableInvalidException.class, () -> {
            AvailabilityZonesRequestValidator.validateAvailabilityZones(inputMap, zoneConfigs);
        });
    }

    @Test
    void testValidateAvailabilityZonesWithDuplicatedValues() {
        // Setup
        final AvailabilityZoneConfig availabilityZoneConfig = new AvailabilityZoneConfig();
        availabilityZoneConfig.setDisplayName("displayName");
        availabilityZoneConfig.setVarName("varName");
        availabilityZoneConfig.setMandatory(true);
        availabilityZoneConfig.setDescription("description");
        final AvailabilityZoneConfig availabilityZoneConfig1 = new AvailabilityZoneConfig();
        availabilityZoneConfig1.setDisplayName("displayName1");
        availabilityZoneConfig1.setVarName("varName1");
        availabilityZoneConfig1.setMandatory(true);
        availabilityZoneConfig1.setDescription("description1");
        final List<AvailabilityZoneConfig> zoneConfigs = List.of(availabilityZoneConfig,
                availabilityZoneConfig1);
        final Map<String, String> inputMap = Map.ofEntries(
                Map.entry("varName", "value"), Map.entry("varName1", "value"));
        // Run the test
        Assertions.assertThrows(VariableInvalidException.class, () -> {
            AvailabilityZonesRequestValidator.validateAvailabilityZones(inputMap, zoneConfigs);
        });
    }
}
