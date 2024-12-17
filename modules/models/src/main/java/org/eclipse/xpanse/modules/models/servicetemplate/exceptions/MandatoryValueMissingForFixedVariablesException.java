/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** Exception thrown when a mandatory value missing for fixed variables. */
@EqualsAndHashCode(callSuper = true)
@Data
public class MandatoryValueMissingForFixedVariablesException extends RuntimeException {
    public MandatoryValueMissingForFixedVariablesException(String message) {
        super(message);
    }
}
