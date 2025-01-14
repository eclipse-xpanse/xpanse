/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.VariableValidationFailedException;
import org.eclipse.xpanse.modules.models.servicetemplate.AvailabilityZoneConfig;
import org.springframework.util.CollectionUtils;

/** Check whether the input map of available zones meet the deployment requirements. */
@Slf4j
public class AvailabilityZonesRequestValidator {

    /**
     * Check whether the input map of available zones meet the deployment requirements.
     *
     * @param inputMap The map of input available zones.
     * @param zoneConfigs The list of availability zone configs.
     */
    public static void validateAvailabilityZones(
            Map<String, String> inputMap, List<AvailabilityZoneConfig> zoneConfigs) {
        if (CollectionUtils.isEmpty(zoneConfigs)) {
            return;
        }
        List<String> requiredZoneVarNames =
                zoneConfigs.stream()
                        .filter(AvailabilityZoneConfig::getMandatory)
                        .map(AvailabilityZoneConfig::getVarName)
                        .toList();
        if (CollectionUtils.isEmpty(requiredZoneVarNames)) {
            return;
        }
        List<String> errorMessages = new ArrayList<>();
        if (CollectionUtils.isEmpty(inputMap)) {
            requiredZoneVarNames.forEach(varName -> errorMessages.add(getErrorMessage(varName)));
        } else {
            requiredZoneVarNames.stream()
                    .filter(varName -> !inputMap.containsKey(varName))
                    .forEach(varName -> errorMessages.add(getErrorMessage(varName)));
        }
        if (!errorMessages.isEmpty()) {
            throw new VariableValidationFailedException(errorMessages);
        }

        Map<String, String> requiredZoneVarValues = new HashMap<>();
        for (String zoneVarName : inputMap.keySet()) {
            if (requiredZoneVarNames.contains(zoneVarName)) {
                if (requiredZoneVarValues.containsValue(inputMap.get(zoneVarName))) {
                    String duplicatedValuesMessage =
                            String.format(
                                    "The values of required "
                                            + "availability zones variables %s are duplicated.",
                                    inputMap.get(zoneVarName));
                    throw new VariableValidationFailedException(List.of(duplicatedValuesMessage));
                } else {
                    requiredZoneVarValues.put(zoneVarName, inputMap.get(zoneVarName));
                }
            }
        }
    }

    private static String getErrorMessage(String varName) {
        return String.format("required availability zone property '%s' not found", varName);
    }
}
