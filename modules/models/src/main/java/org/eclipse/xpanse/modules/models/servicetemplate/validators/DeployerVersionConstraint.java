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
 * Bean validation for deployer version.
 */
@Documented
@Constraint(validatedBy = DeployerVersionValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DeployerVersionConstraint {

    /**
     * Message for invalid deployer version.
     */
    String message() default "Invalid deployer version.";

    /**
     * Classes which the constraint applies to.
     */
    Class<?>[] groups() default {};

    /**
     * Payload for the constraint.
     */
    Class<? extends Payload>[] payload() default {};
}
