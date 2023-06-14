/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.credential.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.credential.AbstractCredentialInfo;
import org.springframework.stereotype.Component;

/**
 * Class which instantiates credential cache and handles all read-write access to the cache.
 */
@Component
@Slf4j
public class CaffeineCredentialCacheManager {

    private static final Cache<CredentialCacheKey, AbstractCredentialInfo> credentialsCache =
            credentialsCache();

    private static Cache<CredentialCacheKey, AbstractCredentialInfo> credentialsCache() {
        return Caffeine.newBuilder()
                .expireAfter(new CredentialCacheExpiry())
                .removalListener(
                        (CredentialCacheKey key,
                         AbstractCredentialInfo graph,
                         RemovalCause cause) ->
                                log.info(String.format(
                                        "Cache entry for csp %s type %s and xpanse user %s "
                                                + "was removed. Reason - %s",
                                        key.csp().toValue(), key.credentialType().toValue(),
                                        key.userName(), cause.toString())))
                .build();
    }

    public void put(CredentialCacheKey key, AbstractCredentialInfo value) {
        credentialsCache.put(key, value);
    }

    public AbstractCredentialInfo get(CredentialCacheKey key) {
        return credentialsCache.getIfPresent(key);
    }

    public void remove(CredentialCacheKey key) {
        credentialsCache.invalidate(key);
    }

}
