/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.credential;

import org.eclipse.xpanse.modules.models.enums.Csp;

/**
 * Defines credential cache key.
 */
public record CredentialCacheKey(Csp csp,
                                 String userName) {

    /**
     * The constructor.
     */
    public CredentialCacheKey {
    }

    @Override
    public String toString() {
        return this.csp.toValue() + "_" + this.userName;
    }
}
