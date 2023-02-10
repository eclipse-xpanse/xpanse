/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.ocl.loader.data.utils;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation to validate CIDRs.
 */
@Target({ElementType.FIELD})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = CidrValidator.class)
public @interface Cidr {

    /**
     * Validator Tip messages.
     */
    String message() default "Cidr is invalid";

    /**
     * Groups.
     */
    Class<?>[] groups() default {};

    /**
     * Payloads.
     */
    Class<? extends Payload>[] payload() default {};
}
