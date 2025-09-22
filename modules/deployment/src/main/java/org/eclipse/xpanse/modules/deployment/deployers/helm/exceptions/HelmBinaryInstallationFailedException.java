/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.helm.exceptions;

/** Exception throw when helm binary installation fails. */
public class HelmBinaryInstallationFailedException extends RuntimeException {

    public HelmBinaryInstallationFailedException(String message) {
        super("Helm binary installation failed with exception: " + message);
    }
}
