/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.credential.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.springframework.stereotype.Component;

/**
 * Class which instantiates credential cache and handles all read-write access to the cache.
 */
@Component
@Slf4j
@SuppressWarnings("UnnecessarilyFullyQualified")
public class CaffeineCredentialCacheManager {

    private static final Cache<CredentialCacheKey, AbstractCredentialInfo> CREDENTIALS_CACHE =
            credentialsCache();

    private static Cache<CredentialCacheKey, AbstractCredentialInfo> credentialsCache() {
        return Caffeine.newBuilder()
                .expireAfter(new CredentialCacheExpiry())
                .removalListener(
                        (CredentialCacheKey key,
                         AbstractCredentialInfo graph,
                         RemovalCause cause) -> {
                            assert key != null;
                            log.info(String.format(
                                    "Cache entry for csp %s type %s and xpanse user %s "
                                            + "was removed. Reason - %s",
                                    key.csp().toValue(), key.credentialType().toValue(),
                                    key.userId(), cause.toString()));
                        })
                .build();
    }

    public void put(CredentialCacheKey key, AbstractCredentialInfo value) {
        CREDENTIALS_CACHE.put(key, value);
    }

    public AbstractCredentialInfo get(CredentialCacheKey key) {
        return CREDENTIALS_CACHE.getIfPresent(key);
    }

    public void remove(CredentialCacheKey key) {
        CREDENTIALS_CACHE.invalidate(key);
    }

}
