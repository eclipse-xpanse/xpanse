/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.enums.DeployVariableKind;
import org.eclipse.xpanse.modules.models.enums.DeployVariableType;
import org.eclipse.xpanse.modules.models.enums.VariableValidator;
import org.eclipse.xpanse.modules.models.resource.DeployVariable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Tool class to analyse object or list of DeployVariable then get variable info map.
 */
@Slf4j
@Component
public class DeployVariableValidator {

    private static final String NUMBER_REGEX = "-?[0-9]+(\\\\\\\\.[0-9]+)?";

    /**
     * Get info map form every variable in list of DeployVariable entity.
     *
     * @param deployVariables list of DeployVariable entity.
     * @return variable validator map
     */
    public Map<String, Map<String, Object>> getVariableApiInfoMap(
            List<DeployVariable> deployVariables) {
        Map<String, Map<String, Object>> varInfoMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(deployVariables)) {
            for (DeployVariable deployVariable : deployVariables) {
                DeployVariableKind kind = deployVariable.getKind();
                // filter kind variable
                if (Objects.equals(kind, DeployVariableKind.VARIABLE)
                        || Objects.equals(kind, DeployVariableKind.FIX_VARIABLE)) {
                    DeployVariableType type = deployVariable.getType();
                    Map<String, Object> infoMap = new HashMap<>();
                    infoMap.put("type", type.toValue());
                    infoMap.put("description", deployVariable.getDescription());
                    infoMap.put("example", deployVariable.getValue());
                    if (StringUtils.isNotBlank(deployVariable.getValidator())) {
                        Map<VariableValidator, String> validatorMap = getValidatorMap(
                                deployVariable.getName(), deployVariable.getValidator(), type);
                        if (!validatorMap.isEmpty()) {
                            for (VariableValidator validator : validatorMap.keySet()) {
                                String valueStr = validatorMap.get(validator);
                                if (validator.equals(VariableValidator.MINIMUM)
                                        || validator.equals(VariableValidator.MAXIMUM)) {
                                    if (valueStr.contains(".")) {
                                        infoMap.put(validator.toValue(),
                                                Double.parseDouble(valueStr));
                                    } else {
                                        infoMap.put(validator.toValue(),
                                                Long.parseLong(valueStr));
                                    }
                                } else if (validator.equals(VariableValidator.MINLENGTH)
                                        || validator.equals(VariableValidator.MAXLENGTH)) {
                                    infoMap.put(validator.toValue(),
                                            Integer.parseInt(valueStr));
                                } else {
                                    infoMap.put(validator.toValue(), validatorMap.get(validator));
                                }

                            }
                        }
                    }
                    varInfoMap.put(deployVariable.getName(), infoMap);
                }
            }
        }
        return varInfoMap;
    }

    /**
     * Get VariableValidator map form every variable in list of DeployVariable entity.
     *
     * @param deployVariables list of DeployVariable entity.
     * @return variable validator map
     */
    private Map<String, Map<VariableValidator, String>> getVariableValidatorMap(
            List<DeployVariable> deployVariables) {
        Map<String, Map<VariableValidator, String>> varValidatorMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(deployVariables)) {
            for (DeployVariable deployVariable : deployVariables) {
                DeployVariableKind kind = deployVariable.getKind();
                // filter kind variable
                if (Objects.equals(kind, DeployVariableKind.VARIABLE)
                        || Objects.equals(kind, DeployVariableKind.FIX_VARIABLE)) {
                    DeployVariableType type = deployVariable.getType();
                    if (StringUtils.isNotBlank(deployVariable.getValidator())) {
                        Map<VariableValidator, String> validatorMap = getValidatorMap(
                                deployVariable.getName(),
                                deployVariable.getValidator(), type);
                        varValidatorMap.put(deployVariable.getName(), validatorMap);
                    }
                }
            }
        }
        return varValidatorMap;
    }

    /**
     * Get Set of required keys form list of DeployVariable entity.
     *
     * @param deployVariables list of DeployVariable entity.
     * @return Set of required keys
     */
    public Set<String> getRequiredKeySet(List<DeployVariable> deployVariables) {
        Set<String> requiredKeys = new HashSet<>();
        if (!CollectionUtils.isEmpty(deployVariables)) {
            for (DeployVariable deployVariable : deployVariables) {
                DeployVariableKind kind = deployVariable.getKind();
                // filter kind variable
                if (Objects.equals(kind, DeployVariableKind.VARIABLE) || deployVariable.getKind()
                        .equals(DeployVariableKind.VARIABLE)) {
                    if (deployVariable.getMandatory()) {
                        requiredKeys.add(deployVariable.getName());
                    }
                }
            }
        }
        return requiredKeys;
    }

    /**
     * Get valid validators map by splitting validatorStr.
     *
     * @param variableName key of variable.
     * @param validatorStr string of validators.
     * @param type         dataType
     * @return Map of validators.
     */
    public Map<VariableValidator, String> getValidatorMap(String variableName, String validatorStr,
            DeployVariableType type) {
        Map<VariableValidator, String> validatorMap = new HashMap<>();
        if (StringUtils.isNotBlank(validatorStr)) {
            String[] validStrArray = StringUtils.split(validatorStr, "|");
            for (String validStr : validStrArray) {
                String[] keyValueArray = StringUtils.split(validStr, "=", 2);
                if (keyValueArray.length == 2) {
                    String key = keyValueArray[0];
                    String value = keyValueArray[1];
                    try {
                        // check validator key.
                        VariableValidator validator =
                                VariableValidator.valueOf(StringUtils.upperCase(key));
                        // check number validator.
                        if (DeployVariableType.NUMBER.equals(type)) {
                            if (validator.equals(VariableValidator.MINIMUM)
                                    || validator.equals(VariableValidator.MAXIMUM)) {
                                if (value.matches(NUMBER_REGEX)) {
                                    validatorMap.put(validator, value);
                                }
                            } // check string validator.
                        } else if (DeployVariableType.STRING.equals(type)) {
                            if (validator.equals(VariableValidator.MINLENGTH)
                                    || validator.equals(VariableValidator.MAXLENGTH)) {
                                if (Integer.parseInt(value) >= 0) {
                                    validatorMap.put(validator, value);
                                }
                            } else if (validator.equals(VariableValidator.ENUM)) {
                                if (value.startsWith("[") && value.endsWith("]")) {
                                    validatorMap.put(validator, value);
                                }

                            } else if (validator.equals(
                                    VariableValidator.PATTERN)) {
                                if (key.matches(value)) {
                                    validatorMap.put(validator, value);
                                }
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        log.error("Variable:{} contains a invalid validaStr:{}", variableName,
                                validStr);
                    }
                }
            }
        }
        return validatorMap;
    }


    /**
     * Check validation of deploy property map by list of deployVariables in registered service.
     *
     * @param deployVariables list of deployVariables in registered service
     * @param deployProperty  deploy property map
     * @return isValid
     */
    public boolean isVariableValid(List<DeployVariable> deployVariables,
            Map<String, String> deployProperty) {

        if (!CollectionUtils.isEmpty(deployVariables) && Objects.nonNull(deployProperty)) {
            // check required keys
            Set<String> userPutKeys = deployProperty.keySet();
            Set<String> requiredKeys = getRequiredKeySet(deployVariables);
            Set<String> ignoredKeys = new HashSet<>();
            if (!userPutKeys.containsAll(requiredKeys)) {
                for (String requiredKey : requiredKeys) {
                    if (!userPutKeys.contains(requiredKey)) {
                        ignoredKeys.add(requiredKey);
                    }
                }
            }
            if (!ignoredKeys.isEmpty()) {
                throw new IllegalArgumentException(String.format("Required keys %s  of deploy "
                        + " variables not found in deploy property", ignoredKeys));
            }
            // check input value of variables is valid by validator of Ocl context.
            Map<String, Map<VariableValidator, String>> variableInfoMap = getVariableValidatorMap(
                    deployVariables);
            if (variableInfoMap.isEmpty() || userPutKeys.isEmpty()) {
                return true;
            }
            for (String userKey : userPutKeys) {
                Map<VariableValidator, String> validatorMap = variableInfoMap.get(userKey);
                if (Objects.nonNull(validatorMap) && !validatorMap.isEmpty()) {
                    String userPutValue = deployProperty.get(userKey);
                    if (StringUtils.isEmpty(userPutValue) && !requiredKeys.contains(userKey)) {
                        continue;
                    }
                    for (VariableValidator validator : validatorMap.keySet()) {
                        validVariable(userPutValue, userKey, validator,
                                validatorMap.get(validator));
                    }
                }
            }
            return true;
        }
        return true;
    }

    private void validVariable(String userPutValue, String userKey, VariableValidator validator,
            String validatorValue) {
        boolean isValid = true;
        String errorMsg = String.format("Key %s with value %s in is valid. Validator [%s: %s]",
                userKey, userPutValue, validator.toValue(), validatorValue);
        try {
            if (validator.equals(VariableValidator.MINIMUM)
                    || validator.equals(VariableValidator.MAXIMUM)) {
                if (userPutValue.matches(NUMBER_REGEX) && validatorValue.matches(NUMBER_REGEX)) {
                    if (userPutValue.contains(".")) {
                        double userDouble = Double.parseDouble(userPutValue);
                        double validatorDouble = Double.parseDouble(validatorValue);
                        if (validator.equals(VariableValidator.MINIMUM)) {
                            if (userDouble < validatorDouble) {
                                isValid = false;
                            }
                        } else {
                            if (userDouble > validatorDouble) {
                                isValid = false;
                            }
                        }
                    } else {
                        long userLong = Long.parseLong(userPutValue);
                        long validatorLong = Long.parseLong(validatorValue);
                        if (validator.equals(VariableValidator.MINIMUM)) {
                            if (userLong < validatorLong) {
                                isValid = false;
                            }
                        } else {
                            if (userLong > validatorLong) {
                                isValid = false;
                            }
                        }
                    }
                } else {
                    isValid = false;
                }
            }

            // check string length
            if (validator.equals(VariableValidator.MINLENGTH)
                    || validator.equals(VariableValidator.MAXLENGTH)) {
                int validatorIntValue = Integer.parseInt(validatorValue);
                int inputLength = StringUtils.isEmpty(userPutValue) ? 0 : userPutValue.length();
                // length user input value < minLength
                if (validator.equals(VariableValidator.MINLENGTH)) {
                    if (inputLength < validatorIntValue) {
                        isValid = false;
                    }
                } else {
                    // length user input value > maxLength
                    if (inputLength > validatorIntValue) {
                        isValid = false;
                    }
                }
            }

            if (validator.equals(VariableValidator.ENUM)) {
                // enums not contains user input value
                String valueStr = validatorValue.replace("[", "")
                        .replace("]", "").replaceAll("\"", "");
                Set<String> valueSet =
                        new HashSet<>(Arrays.asList(StringUtils.split(valueStr, ",")));
                if (!valueSet.contains(userPutValue)) {
                    isValid = false;
                }
            }
            if (validator.equals(VariableValidator.PATTERN)) {
                // user input value not matches validatorValue
                if (!userPutValue.matches(validatorValue)) {
                    isValid = false;
                }
            }
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(errorMsg, ex);
        }
        if (!isValid) {
            throw new IllegalArgumentException(errorMsg);
        }
    }


}
