/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.exceptions;

/** Exception thrown when OpenTofu get the hostname of Xpanse service. */
public class OpenTofuMakerRequestFailedException extends RuntimeException {

    public OpenTofuMakerRequestFailedException(String message) {
        super("OpenTofuExecutor Exception: " + message);
    }
}
