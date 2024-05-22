/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.modify.exceptions;

/**
 * Exception thrown when the service modification audit is not found.
 */
public final class ServiceModificationAuditNotFound extends RuntimeException {
    public ServiceModificationAuditNotFound(String message) {
        super(message);
    }

}