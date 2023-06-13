/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.credential;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.credential.enums.CredentialType;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;

/**
 * Manage user credential cache.
 */
@Slf4j
@Configuration
public class CredentialCacheManager {

    private static final Map<CredentialCacheKey, Map<CredentialType, CredentialVariables>> CACHES =
            new ConcurrentHashMap<>();

    /**
     * Is the cache container empty.
     *
     * @return true or false.
     */
    public boolean isEmpty() {
        return CACHES.isEmpty();
    }

    /**
     * Put cache.
     *
     * @param key   key
     * @param value value
     */
    public void putCache(CredentialCacheKey key, CredentialVariables value) {
        if (CACHES.containsKey(key)) {
            CACHES.get(key).put(value.getType(), value);
        } else {
            Map<CredentialType, CredentialVariables> typeMap = new ConcurrentHashMap<>();
            typeMap.put(value.getType(), value);
            CACHES.put(key, typeMap);
        }
    }

    /**
     * Get cache values.
     *
     * @param key key
     */
    public List<CredentialVariables> getAllTypeCaches(CredentialCacheKey key) {
        if (CACHES.containsKey(key)) {
            Map<CredentialType, CredentialVariables> credentialMap = CACHES.get(key);
            return credentialMap.values().stream().filter(credential ->
                    Objects.nonNull(credential)
                            && credential.getExpiredTime() > System.currentTimeMillis()
            ).sorted(Comparator.comparing(CredentialVariables::getType)).toList();
        }
        return Collections.emptyList();

    }

    /**
     * Get cache value by key and credential type.
     *
     * @param key  key
     * @param type credential type
     */
    public CredentialVariables getCachesByType(CredentialCacheKey key, CredentialType type) {
        if (CACHES.containsKey(key)) {
            CredentialVariables credentialVariables = CACHES.get(key).get(type);
            if (credentialVariables.getExpiredTime() > System.currentTimeMillis()) {
                return credentialVariables;
            }
        }
        return null;
    }

    /**
     * Remove cache.
     *
     * @param key key
     */
    public void removeAllTypeCaches(CredentialCacheKey key) {
        CACHES.remove(key);
    }

    /**
     * Remove cache by key and credential type.
     *
     * @param key  key
     * @param type credential type
     */
    public void removeCacheByType(CredentialCacheKey key, CredentialType type) {
        if (CACHES.containsKey(key)) {
            CACHES.get(key).remove(type);
            log.info("Removed the cache with key:{} and type:{} successfully.", key, type);
        }
    }

    /**
     * Scheduling task to remove the expired cache based on time.
     */
    @Async("taskExecutor")
    public void removeJob() {
        MDC.put("TASK_ID", UUID.randomUUID().toString());
        log.info("Scheduling task to remove the expired cache based on time start.");
        if (CACHES.size() <= 0) {
            return;
        }
        for (Entry<CredentialCacheKey, Map<CredentialType, CredentialVariables>> next :
                CACHES.entrySet()) {
            CredentialCacheKey key = next.getKey();
            Set<Entry<CredentialType, CredentialVariables>> typeEntrySet =
                    next.getValue().entrySet();
            for (Entry<CredentialType, CredentialVariables> typeEntry : typeEntrySet) {
                if (typeEntry.getValue().getExpiredTime() < System.currentTimeMillis()) {
                    removeCacheByType(key, typeEntry.getKey());
                }
            }
        }
    }

}
