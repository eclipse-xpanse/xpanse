/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.billing.exceptions;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Exception thrown when the billing config of service is invalid.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InvalidBillingConfigException extends RuntimeException {

    private final List<String> errorReasons;

    public InvalidBillingConfigException(List<String> message) {
        this.errorReasons = message;
    }
}
