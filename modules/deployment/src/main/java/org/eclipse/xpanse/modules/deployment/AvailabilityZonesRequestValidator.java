/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.VariableInvalidException;
import org.eclipse.xpanse.modules.models.servicetemplate.AvailabilityZoneConfig;
import org.springframework.util.CollectionUtils;

/**
 * Check whether the input map of available zones meet the deployment requirements.
 */
@Slf4j
public class AvailabilityZonesRequestValidator {

    /**
     * Check whether the input map of available zones meet the deployment requirements.
     *
     * @param inputMap    The map of input available zones.
     * @param zoneConfigs The list of availability zone configs.
     */
    public static void validateAvailabilityZones(Map<String, String> inputMap,
                                                 List<AvailabilityZoneConfig> zoneConfigs) {
        if (CollectionUtils.isEmpty(zoneConfigs)) {
            return;
        }
        Set<String> requiredZoneVarNameSet =
                zoneConfigs.stream().filter(AvailabilityZoneConfig::getMandatory)
                        .map(AvailabilityZoneConfig::getVarName).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(requiredZoneVarNameSet)) {
            return;
        }
        if (CollectionUtils.isEmpty(inputMap)) {
            String missingZonesMsg =
                    String.format("The required availability zones variables %s are missing.",
                            StringUtils.join(requiredZoneVarNameSet, ", "));
            throw new VariableInvalidException(List.of(missingZonesMsg));
        }
        Set<String> missingZoneVarNames = requiredZoneVarNameSet.stream()
                .filter(requiredZoneVarName -> !inputMap.containsKey(requiredZoneVarName))
                .collect(Collectors.toSet());
        if (!missingZoneVarNames.isEmpty()) {
            String missingZonesMsg =
                    String.format("The required availability zones variable names %s are missing.",
                            StringUtils.join(missingZoneVarNames, ", "));
            throw new VariableInvalidException(List.of(missingZonesMsg));
        }
        Map<String, String> requiredZoneVarValues = new HashMap<>();
        for (String zoneVarName : inputMap.keySet()) {
            if (requiredZoneVarNameSet.contains(zoneVarName)) {
                if (requiredZoneVarValues.containsValue(inputMap.get(zoneVarName))) {
                    String duplicatedValuesMessage = String.format("The values of required "
                                    + "availability zones variables %s are duplicated.",
                            inputMap.get(zoneVarName));
                    throw new VariableInvalidException(List.of(duplicatedValuesMessage));
                } else {
                    requiredZoneVarValues.put(zoneVarName, inputMap.get(zoneVarName));
                }
            }
        }

    }
}
