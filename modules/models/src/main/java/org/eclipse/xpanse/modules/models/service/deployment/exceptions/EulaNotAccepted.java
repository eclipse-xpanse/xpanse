/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.deployment.exceptions;

/**
 * Exception thrown when the service template defines an EULA but the end user has requested for the
 * service without accepting it.
 */
public class EulaNotAccepted extends RuntimeException {

    public EulaNotAccepted(String message) {
        super(message);
    }
}
