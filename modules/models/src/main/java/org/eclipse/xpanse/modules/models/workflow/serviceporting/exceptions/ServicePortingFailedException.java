/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.workflow.serviceporting.exceptions;

/** Exception thrown when the service porting was not requested. */
public class ServicePortingFailedException extends RuntimeException {
    public ServicePortingFailedException(String message) {
        super(message);
    }
}
