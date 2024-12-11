/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Get custom annotations for csp information. */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditApiRequest {

    /** The class of the method can get csp info. */
    Class<?> clazz() default GetCspInfoFromRequest.class;

    /** The method name can get csp info. */
    String methodName() default "";

    /** The param types of the method can get csp info. */
    Class<?>[] paramTypes() default {String.class};

    /** The index of params can be used to get csp info with the method. */
    int[] paramIndexes() default {0};

    /** Identifies whether csp can be obtained directly. */
    boolean enabled() default true;
}
