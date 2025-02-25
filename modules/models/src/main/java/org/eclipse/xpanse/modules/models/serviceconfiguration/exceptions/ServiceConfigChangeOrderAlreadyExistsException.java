/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.serviceconfiguration.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** Exception thrown when service config change order already exists. */
@EqualsAndHashCode(callSuper = true)
@Data
public class ServiceConfigChangeOrderAlreadyExistsException extends RuntimeException {
    public ServiceConfigChangeOrderAlreadyExistsException(String message) {
        super(message);
    }
}
