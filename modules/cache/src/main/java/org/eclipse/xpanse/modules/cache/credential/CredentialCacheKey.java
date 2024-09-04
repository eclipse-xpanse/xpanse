/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.cache.credential;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;

/**
 * Defines credential cache key.
 */
public record CredentialCacheKey(Csp csp,
                                 String site,
                                 CredentialType credentialType,
                                 String credentialName,
                                 String userId) implements Serializable {

    @Serial
    private static final long serialVersionUID = 9144354512858062247L;

    @Override
    public boolean equals(Object obj) {
        if (Objects.isNull(obj)) {
            return false;
        }
        if (obj instanceof CredentialCacheKey key) {
            return Objects.equals(key.csp, this.csp)
                    && Objects.equals(key.userId, this.userId)
                    && Objects.equals(key.site, this.site)
                    && Objects.equals(key.credentialType, this.credentialType)
                    && Objects.equals(key.credentialName, this.credentialName);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((csp == null) ? 0 : csp.hashCode());
        result = prime * result + ((site == null) ? 0 : site.hashCode());
        result = prime * result + ((credentialType == null) ? 0 : credentialType.hashCode());
        result = prime * result + ((credentialName == null) ? 0 : credentialName.hashCode());
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
        return result;
    }
}
