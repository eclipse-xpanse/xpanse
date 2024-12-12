/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.deploy.exceptions;

/** Exception thrown When the tfstate file is locked for deployment retries. */
public class FileLockedException extends RuntimeException {

    public FileLockedException(String message) {
        super(message);
    }
}
