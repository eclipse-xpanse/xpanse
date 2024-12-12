/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.cache.exceptions;

/** Exception thrown when no cache found. */
public class CacheNotFoundException extends RuntimeException {

    public CacheNotFoundException(String message) {
        super(message);
    }
}
