/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * Validator for Deployer version.
 */
public class DeployerVersionValidator
        implements ConstraintValidator<DeployerVersionConstraint, String> {


    /**
     * Regular expression for matching deployer version.
     * This Pattern object is used to validate strings against a specific version number format.
     * The version number format includes: =, >=, <=, followed by optional space and v/V,
     * and then three numeric parts separated by dots.
     */
    private static final Pattern VERSION_PATTERN =
            Pattern.compile("^(=|>=|<=)\\s*([vV])?\\d+\\.\\d+\\.\\d+$");

    @Override
    public void initialize(DeployerVersionConstraint constraintAnnotation) {
        // No initialization required
    }

    @Override
    public boolean isValid(String version, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(version)) {
            return false;
        }
        return VERSION_PATTERN.matcher(version).matches();
    }
}
