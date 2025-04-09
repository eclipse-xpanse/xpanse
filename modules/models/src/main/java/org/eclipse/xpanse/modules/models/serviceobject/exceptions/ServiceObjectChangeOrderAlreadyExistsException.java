/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.serviceobject.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** Exception thrown when service object manage order already exists. */
@EqualsAndHashCode(callSuper = true)
@Data
public class ServiceObjectChangeOrderAlreadyExistsException extends RuntimeException {
    public ServiceObjectChangeOrderAlreadyExistsException(String message) {
        super(message);
    }
}
