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
import org.eclipse.xpanse.modules.models.servicetemplate.DeployVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployVariableKind;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.InvalidValueSchemaException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.MandatoryValueMissingForFixedVariablesException;

/** Defines method to validate the deploy variable configuration list of deployment. */
@Slf4j
public class DeployVariableSchemaValidator {

    /**
     * Validate the deploy variable configuration list of deployment.
     *
     * @param deployVariables deployVariables.
     */
    public static void validateDeployVariable(List<DeployVariable> deployVariables) {
        if (Objects.isNull(deployVariables)) {
            return;
        }
        if (deployVariables.isEmpty()) {
            String errorMessage = "The deploy variable configuration list could not be empty.";
            throw new InvalidValueSchemaException(List.of(errorMessage));
        }
        Set<String> varNameSet = new HashSet<>();
        List<DeployVariable> missingExampleValueVariables = new ArrayList<>();

        for (DeployVariable deployVariable : deployVariables) {
            if (Objects.nonNull(deployVariable.getKind())
                    && (StringUtils.equals(
                                    deployVariable.getKind().toValue(),
                                    DeployVariableKind.FIX_ENV.toValue())
                            || StringUtils.equals(
                                    deployVariable.getKind().toValue(),
                                    DeployVariableKind.FIX_VARIABLE.toValue()))) {
                if (StringUtils.isEmpty(deployVariable.getExample())) {
                    missingExampleValueVariables.add(deployVariable);
                }
            }
            if (varNameSet.contains(deployVariable.getName())) {
                String errorMessage =
                        String.format(
                                "The deploy variable configuration list with duplicated variable"
                                        + " name %s",
                                deployVariable.getName());
                throw new InvalidValueSchemaException(List.of(errorMessage));
            } else {
                varNameSet.add(deployVariable.getName());
            }
        }
        if (!missingExampleValueVariables.isEmpty()) {
            StringBuilder errorMessageBuilder =
                    new StringBuilder("The deploy variable " + "configuration for ")
                            .append(DeployVariableKind.FIX_ENV.toValue())
                            .append(" or ")
                            .append(DeployVariableKind.FIX_VARIABLE.toValue())
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

        if (!hasAutoFillAndParentKind(deployVariables)) {
            log.error("variable schema definition invalid");
            throw new InvalidValueSchemaException(
                    List.of(ErrorType.VARIABLE_SCHEMA_DEFINITION_INVALID.toValue()));
        }
    }

    private static boolean hasAutoFillAndParentKind(List<DeployVariable> deployVariables) {
        for (DeployVariable deployVariable : deployVariables) {
            if (Objects.nonNull(deployVariable.getAutoFill())) {
                DeployResourceKind currentResourceKind =
                        deployVariable.getAutoFill().getDeployResourceKind();
                DeployResourceKind parentKind = currentResourceKind.getParent();
                if (Objects.nonNull(parentKind)) {
                    for (DeployVariable otherVariable : deployVariables) {
                        if (otherVariable != deployVariable
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
