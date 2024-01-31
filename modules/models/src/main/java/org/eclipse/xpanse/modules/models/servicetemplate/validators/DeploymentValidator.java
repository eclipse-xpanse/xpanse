/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;
import org.eclipse.xpanse.modules.models.servicetemplate.Deployment;

/**
 * Validator for Deployment scripts source.
 */
public class DeploymentValidator implements
        ConstraintValidator<DeploymentScriptsConstraint, Deployment> {


    @Override
    public boolean isValid(Deployment deployment, ConstraintValidatorContext context) {
        if (Objects.nonNull(deployment.getScriptsRepo())
                && Objects.nonNull(deployment.getDeployer())) {
            return false;
        }
        return !Objects.isNull(deployment.getScriptsRepo())
                || !Objects.isNull(deployment.getDeployer());
    }
}
