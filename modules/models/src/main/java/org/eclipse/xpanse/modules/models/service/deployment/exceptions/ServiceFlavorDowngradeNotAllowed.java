/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.deployment.exceptions;

/** Exception thrown when the service flavor not allowed to be downgraded. */
public class ServiceFlavorDowngradeNotAllowed extends RuntimeException {
    public ServiceFlavorDowngradeNotAllowed(String message) {
        super(message);
    }
}
