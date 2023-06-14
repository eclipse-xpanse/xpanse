/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.credential.cache;

import org.eclipse.xpanse.modules.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.enums.Csp;

/**
 * Defines credential cache key.
 */
public record CredentialCacheKey(Csp csp,
                                 String userName,
                                 CredentialType credentialType) {

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CredentialCacheKey key) {
            return key.csp == this.csp
                    && key.userName.equals(this.userName)
                    && key.credentialType == this.credentialType;
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
        result = prime * result + ((credentialType == null) ? 0 : credentialType.hashCode());
        return result;
    }
}
