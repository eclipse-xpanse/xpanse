/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.deploy.exceptions;

/** Exception is thrown when ISV users query deployed service details by ID. */
public class ServiceDetailsNotAccessible extends RuntimeException {
    public ServiceDetailsNotAccessible(String message) {
        super(message);
    }
}
