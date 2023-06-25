/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.deploy.exceptions;

/**
 * Exception thrown when an action on a service is requested during invalid states.
 */
public class InvalidServiceStateException extends RuntimeException {
    public InvalidServiceStateException(String message) {
        super(message);
    }


}

