/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Exception thrown when a deployment service version is invalid.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InvalidServiceVersionException extends RuntimeException {
    public InvalidServiceVersionException(String message) {
        super(message);
    }
}
