/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.deploy.exceptions;

/**
 * Exception thrown when the service modify was not ignore impact.
 */
public class ServiceModifyParamsNotFoundException extends RuntimeException {
    public ServiceModifyParamsNotFoundException(String message) {
        super(message);
    }


}
