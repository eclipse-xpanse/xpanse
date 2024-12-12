/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.policy.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** Exception thrown when validate the policy. */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("UnnecessarilyFullyQualified")
public class PoliciesValidationFailedException extends RuntimeException {

    private final String errorReason;

    public PoliciesValidationFailedException(String errorReason) {
        super(errorReason);
        this.errorReason = errorReason;
    }
}
