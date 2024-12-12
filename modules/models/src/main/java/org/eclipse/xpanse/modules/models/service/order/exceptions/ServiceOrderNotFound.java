/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.order.exceptions;

/** Exception thrown when the service order management task is not found. */
public final class ServiceOrderNotFound extends RuntimeException {
    public ServiceOrderNotFound(String message) {
        super(message);
    }
}
