/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.servicetemplate.utils;

import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.servicetemplate.DeployVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.InvalidValueSchemaException;

/**
 * Deployment variables automatically fill in the verification class.
 */
@Slf4j
public class DeployVariableAutoFillValidator {

    /**
     * Define a method to verify automatic filling of deployment variables.
     *
     * @param deployVariables deployVariables.
     */
    public static void validateDeployVariableAutoFill(List<DeployVariable> deployVariables) {
        if (!hasAutoFillAndParentKind(deployVariables)) {
            log.error("variable schema definition invalid");
            throw new InvalidValueSchemaException(
                    List.of(ResultType.VARIABLE_SCHEMA_DEFINITION_INVALID.toValue()));
        }
        return;
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
