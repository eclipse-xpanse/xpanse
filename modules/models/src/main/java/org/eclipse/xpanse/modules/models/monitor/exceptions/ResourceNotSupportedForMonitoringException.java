/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.monitor.exceptions;

/**
 * Exception thrown when monitoring data is requested for a resource which does cannot be monitored.
 */
public class ResourceNotSupportedForMonitoringException extends RuntimeException {

    public ResourceNotSupportedForMonitoringException(String message) {
        super(message);
    }
}

