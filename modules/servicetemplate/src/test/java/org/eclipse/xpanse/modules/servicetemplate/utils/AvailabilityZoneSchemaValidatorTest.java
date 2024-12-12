package org.eclipse.xpanse.modules.servicetemplate.utils;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.xpanse.modules.models.servicetemplate.AvailabilityZoneConfig;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.InvalidValueSchemaException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AvailabilityZoneSchemaValidatorTest {
    @Test
    void testValidateServiceAvailabilities() {
        // Setup
        final List<AvailabilityZoneConfig> availabilityZones = new ArrayList<>();
        // Run the test
        Assertions.assertThrows(
                InvalidValueSchemaException.class,
                () ->
                        AvailabilityZoneSchemaValidator.validateServiceAvailabilities(
                                availabilityZones));

        final AvailabilityZoneConfig availabilityZoneConfig = new AvailabilityZoneConfig();
        availabilityZoneConfig.setDisplayName("displayName");
        availabilityZoneConfig.setVarName("varName");
        availabilityZoneConfig.setMandatory(false);
        availabilityZoneConfig.setDescription("description");
        availabilityZones.add(availabilityZoneConfig);

        // Run the test
        Assertions.assertDoesNotThrow(
                () ->
                        AvailabilityZoneSchemaValidator.validateServiceAvailabilities(
                                availabilityZones));

        final AvailabilityZoneConfig availabilityZoneConfig1 = new AvailabilityZoneConfig();
        availabilityZoneConfig1.setDisplayName("displayName1");
        availabilityZoneConfig1.setVarName("varName");
        availabilityZoneConfig1.setMandatory(false);
        availabilityZoneConfig1.setDescription("description1");

        availabilityZones.add(availabilityZoneConfig1);
        // Run the test
        Assertions.assertThrows(
                InvalidValueSchemaException.class,
                () ->
                        AvailabilityZoneSchemaValidator.validateServiceAvailabilities(
                                availabilityZones));
    }
}
