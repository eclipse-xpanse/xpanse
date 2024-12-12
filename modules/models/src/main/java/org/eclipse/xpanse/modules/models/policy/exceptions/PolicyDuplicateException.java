/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.policy.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** Exception thrown when found the same policy already created. */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("UnnecessarilyFullyQualified")
public class PolicyDuplicateException extends RuntimeException {

    private final String errorReason;

    public PolicyDuplicateException(String errorReason) {
        super(errorReason);
        this.errorReason = errorReason;
    }
}
