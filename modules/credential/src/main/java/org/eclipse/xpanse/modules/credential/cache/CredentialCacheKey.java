/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.credential.cache;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;

/**
 * Defines credential cache key.
 */
public record CredentialCacheKey(Csp csp,
                                 CredentialType credentialType,
                                 String credentialName,
                                 String userId) {

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CredentialCacheKey key) {
            return StringUtils.isBlank(userId)
                    ?
                    key.csp == this.csp
                            && key.credentialType == this.credentialType
                            && key.credentialName.equals(this.credentialName)
                    : key.csp == this.csp
                            && key.credentialType == this.credentialType
                            && key.credentialName.equals(this.credentialName)
                            && key.userId.equals(this.userId);
        } else {
            return false;
        }

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((csp == null) ? 0 : csp.hashCode());
        result = prime * result + ((credentialType == null) ? 0 : credentialType.hashCode());
        result = prime * result + ((credentialName == null) ? 0 : credentialName.hashCode());
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
        return result;
    }
}
