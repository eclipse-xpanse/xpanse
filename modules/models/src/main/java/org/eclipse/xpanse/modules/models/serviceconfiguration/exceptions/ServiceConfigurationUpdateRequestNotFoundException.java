/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.serviceconfiguration.exceptions;

/**
 * Exception thrown when service configuration update request not found.
 */

public class ServiceConfigurationUpdateRequestNotFoundException  extends RuntimeException {

    public ServiceConfigurationUpdateRequestNotFoundException(String message) {
        super(message);
    }
}
