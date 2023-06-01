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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CredentialCacheKey key) {
            return key.csp.equals(this.csp) && key.userName.equals(this.userName);
        } else {
            return false;
        }

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((csp == null) ? 0 : csp.hashCode());
        result = prime * result + ((userName == null) ? 0 : userName.hashCode());
        return result;
    }
}
