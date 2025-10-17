/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicechange.exceptions;

/**
 * Exception thrown when we try to control the resources of a service which does not allow users to
 * start/stop/restart a service.
 */
public class ServiceStateNotControllable extends RuntimeException {
    public ServiceStateNotControllable(String message) {
        super(message);
    }
}
