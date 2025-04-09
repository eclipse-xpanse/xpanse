/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.serviceaction.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** Exception thrown when service action change order already exists. */
@EqualsAndHashCode(callSuper = true)
@Data
public class ServiceActionChangeOrderAlreadyExistsException extends RuntimeException {
    public ServiceActionChangeOrderAlreadyExistsException(String message) {
        super(message);
    }
}
