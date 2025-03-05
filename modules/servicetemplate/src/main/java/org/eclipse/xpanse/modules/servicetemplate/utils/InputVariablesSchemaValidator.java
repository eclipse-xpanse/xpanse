/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.servicetemplate.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.response.ErrorType;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.servicetemplate.InputVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.VariableKind;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.InvalidValueSchemaException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.MandatoryValueMissingForFixedVariablesException;

/** Defines method to validate the input variable configuration list of deployment. */
@Slf4j
public class InputVariablesSchemaValidator {

    /**
     * Validate the deploy variable configuration list of deployment.
     *
     * @param inputVariables inputVariables.
     */
    public static void validateInputVariables(List<InputVariable> inputVariables) {
        if (Objects.isNull(inputVariables)) {
            return;
        }
        if (inputVariables.isEmpty()) {
            String errorMessage = "The deploy variable configuration list could not be empty.";
            throw new InvalidValueSchemaException(List.of(errorMessage));
        }
        Set<String> varNameSet = new HashSet<>();
        List<InputVariable> missingExampleValueVariables = new ArrayList<>();

        for (InputVariable inputVariable : inputVariables) {
            if (inputVariable.getKind() == VariableKind.FIX_ENV
                    || inputVariable.getKind() == VariableKind.FIX_VARIABLE) {
                if (StringUtils.isEmpty(inputVariable.getValue())) {
                    missingExampleValueVariables.add(inputVariable);
                }
            }
            if (varNameSet.contains(inputVariable.getName())) {
                String errorMessage =
                        String.format(
                                "The deploy variable configuration list with duplicated variable"
                                        + " name %s",
                                inputVariable.getName());
                throw new InvalidValueSchemaException(List.of(errorMessage));
            } else {
                varNameSet.add(inputVariable.getName());
            }
        }
        if (!missingExampleValueVariables.isEmpty()) {
            StringBuilder errorMessageBuilder =
                    new StringBuilder("The deploy variable " + "configuration for ")
                            .append(VariableKind.FIX_ENV.toValue())
                            .append(" or ")
                            .append(VariableKind.FIX_VARIABLE.toValue())
                            .append(" kind has an empty default value. Details: ");

            missingExampleValueVariables.forEach(
                    variable -> {
                        errorMessageBuilder.append(
                                String.format(
                                        "Variable %s has type %s but a value for it is not"
                                                + " provided. ",
                                        variable.getName(), variable.getDataType()));
                    });

            String errorMessage = errorMessageBuilder.toString().trim();
            throw new MandatoryValueMissingForFixedVariablesException(errorMessage);
        }

        if (!hasAutoFillAndParentKind(inputVariables)) {
            log.error("variable schema definition invalid");
            throw new InvalidValueSchemaException(
                    List.of(ErrorType.VARIABLE_SCHEMA_DEFINITION_INVALID.toValue()));
        }
    }

    private static boolean hasAutoFillAndParentKind(List<InputVariable> inputVariables) {
        for (InputVariable inputVariable : inputVariables) {
            if (Objects.nonNull(inputVariable.getAutoFill())) {
                DeployResourceKind currentResourceKind =
                        inputVariable.getAutoFill().getDeployResourceKind();
                DeployResourceKind parentKind = currentResourceKind.getParent();
                if (Objects.nonNull(parentKind)) {
                    for (InputVariable otherVariable : inputVariables) {
                        if (otherVariable != inputVariable
                                && Objects.nonNull(otherVariable.getAutoFill())
                                && otherVariable.getAutoFill().getDeployResourceKind()
                                        == parentKind) {
                            return true;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }
}
