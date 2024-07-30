/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.cache.credential;

import java.io.Serial;
import java.io.Serializable;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;

/**
 * Defines credential cache key.
 */
public record CredentialCacheKey(Csp csp,
                                 CredentialType credentialType,
                                 String credentialName,
                                 String userId) implements Serializable {

    @Serial
    private static final long serialVersionUID = -663928757399114751L;

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
