/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bean validation for Deployment scripts.
 */
@Documented
@Constraint(validatedBy = DeploymentValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DeploymentScriptsConstraint {
    /**
     * Message returned when constraint validation fails.
     *
     * @return Error message.
     */
    String message() default "Either deployer or deployFromGitRepo must be provided";

    /**
     * standard field to be provided to javax validator. We just
     * provide an empty array.
     */
    Class<?>[] groups() default {};

    /**
     * standard field to be provided to javax validator. We just
     * provide an empty array.
     */
    Class<? extends Payload>[] payload() default {};
}
