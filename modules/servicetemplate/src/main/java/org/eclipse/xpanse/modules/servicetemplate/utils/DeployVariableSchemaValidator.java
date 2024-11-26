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
import org.eclipse.xpanse.modules.models.response.ErrorType;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.servicetemplate.DeployVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.InvalidValueSchemaException;

/**
 * Defines method to validate the deploy variable configuration list of deployment.
 */
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
        for (DeployVariable deployVariable : deployVariables) {
            if (varNameSet.contains(deployVariable.getName())) {
                String errorMessage = String.format(
                        "The deploy variable configuration list with duplicated variable name %s",
                        deployVariable.getName());
                throw new InvalidValueSchemaException(List.of(errorMessage));
            } else {
                varNameSet.add(deployVariable.getName());
            }
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
