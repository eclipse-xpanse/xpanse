/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.servicetemplate.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.servicetemplate.AvailabilityZoneConfig;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.InvalidValueSchemaException;

/**
 * Defines method to validate the availability zone configuration list of deployment.
 */
@Slf4j
public class AvailabilityZoneSchemaValidator {

    /**
     * Validate the availability zone configuration list of deployment.
     *
     * @param availabilityZones The availability zone config list.
     */
    public static void validateServiceAvailabilities(
            List<AvailabilityZoneConfig> availabilityZones) {
        if (Objects.isNull(availabilityZones)) {
            return;
        }
        if (availabilityZones.isEmpty()) {
            String errorMessage = "The availability zone configuration list could not be empty.";
            throw new InvalidValueSchemaException(List.of(errorMessage));
        }
        Set<String> availabilityVarName = new HashSet<>();
        for (AvailabilityZoneConfig availabilityZone : availabilityZones) {
            if (availabilityVarName.contains(availabilityZone.getVarName())) {
                String errorMessage = String.format(
                        "The availability zone configuration list with duplicated variable name %s",
                        availabilityZone.getVarName());
                throw new InvalidValueSchemaException(List.of(errorMessage));
            } else {
                availabilityVarName.add(availabilityZone.getVarName());
            }
        }
    }
}
