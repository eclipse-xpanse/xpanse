/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.cache.credential;

import static org.eclipse.xpanse.modules.cache.consts.CacheConstants.CREDENTIAL_CACHE_NAME;
import static org.eclipse.xpanse.modules.cache.consts.CacheConstants.DEFAULT_CREDENTIAL_CACHE_EXPIRE_TIME_IN_SECONDS;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.cache.config.CacheProperties;
import org.eclipse.xpanse.modules.cache.exceptions.CacheNotFoundException;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/** Component for managing credentials stored in cache using Spring Cache annotations. */
@Slf4j
@Component
public class CredentialsStore {

    private final RedisTemplate<String, AbstractCredentialInfo> credentialRedisTemplate;
    private final CacheProperties cacheProperties;

    /** Constructor for CredentialsStore. */
    @Autowired
    public CredentialsStore(
            CacheProperties cacheProperties,
            @Nullable RedisTemplate<String, AbstractCredentialInfo> credentialRedisTemplate) {
        this.cacheProperties = cacheProperties;
        this.credentialRedisTemplate = credentialRedisTemplate;
    }

    /**
     * Methods to add credentials to credentials store.
     *
     * @param credentialInfo Complete credential configuration object.
     */
    @CachePut(cacheNames = CREDENTIAL_CACHE_NAME, key = "#key")
    public AbstractCredentialInfo storeCredential(
            CredentialCacheKey key, AbstractCredentialInfo credentialInfo) {
        log.info("Store credential cache entry with key:{}", key);
        return credentialInfo;
    }

    /**
     * Method to get credential data from credentials store.
     *
     * @param key CredentialCacheKey.
     */
    @Cacheable(cacheNames = CREDENTIAL_CACHE_NAME, key = "#key")
    public AbstractCredentialInfo getCredential(CredentialCacheKey key) {
        // This method body can be left empty, throw exception when cache miss occurs.
        throw new CacheNotFoundException("No credential cache entry found with key: " + key);
    }

    /**
     * Method to delete credential data from credentials store.
     *
     * @param key CredentialCacheKey.
     */
    @CacheEvict(cacheNames = CREDENTIAL_CACHE_NAME, key = "#key")
    public void deleteCredential(CredentialCacheKey key) {
        // This method body is not required when using @CacheEvict.
        log.info("Delete credential cache entry with key:{}", key.toString());
    }

    /**
     * Method to update the time-to-live of the credential in the redis cache.
     *
     * @param cacheKey CredentialCacheKey.
     */
    @SuppressFBWarnings(
            value = "REC_CATCH_EXCEPTION",
            justification = "Errors need not be forwarded to client.")
    public void updateCredentialCacheTimeToLive(CredentialCacheKey cacheKey) {
        if (!cacheProperties.isRedisEnabled()
                || Objects.isNull(credentialRedisTemplate)
                || Objects.isNull(cacheKey)) {
            return;
        }
        String redisKey = CREDENTIAL_CACHE_NAME + "::" + cacheKey;
        long timeToLive = DEFAULT_CREDENTIAL_CACHE_EXPIRE_TIME_IN_SECONDS;
        try {
            AbstractCredentialInfo credentialInfo =
                    credentialRedisTemplate.opsForValue().get(redisKey);
            if (Objects.nonNull(credentialInfo)) {
                timeToLive = credentialInfo.getTimeToLive();
                Boolean flag =
                        credentialRedisTemplate.expire(redisKey, timeToLive, TimeUnit.SECONDS);
                if (Boolean.TRUE.equals(flag)) {
                    log.info(
                            "Updated expiration of the redis key:{} with the time:{} in seconds "
                                    + "successfully.",
                            redisKey,
                            timeToLive);
                }
            }
        } catch (Exception e) {
            log.error(
                    "Updated expiration of the redis key:{} with the time:{} in minutes failed.",
                    redisKey,
                    timeToLive);
        }
    }
}
