/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.policy.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Exception thrown when found the policy entity.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("UnnecessarilyFullyQualified")
public class PolicyNotFoundException extends RuntimeException {

    private final String errorReason;

    public PolicyNotFoundException(String errorReason) {
        super(errorReason);
        this.errorReason = errorReason;
    }


}
