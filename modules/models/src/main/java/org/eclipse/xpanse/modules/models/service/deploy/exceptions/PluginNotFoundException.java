/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.deploy.exceptions;

/** Exception thrown no plugin bean found for the requested service on a specific Csp. */
public class PluginNotFoundException extends RuntimeException {
    public PluginNotFoundException(String message) {
        super(message);
    }
}
