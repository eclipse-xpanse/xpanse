/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.ocl.loader.data.utils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.net.util.SubnetUtils;

/**
 * Cidr validator.
 */
public class CidrValidator implements ConstraintValidator<Cidr, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        try {
            new SubnetUtils(value);
        } catch (IllegalArgumentException ex) {
            return false;
        }

        return true;
    }

}
