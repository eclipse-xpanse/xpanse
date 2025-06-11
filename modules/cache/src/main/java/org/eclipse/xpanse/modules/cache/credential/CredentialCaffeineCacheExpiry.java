/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.cache.credential;

import static org.eclipse.xpanse.modules.cache.consts.CacheConstants.DEFAULT_CACHE_EXPIRE_TIME_IN_MINUTES;

import com.github.benmanes.caffeine.cache.Expiry;
import java.util.concurrent.TimeUnit;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;

/** Class to configure caffeine cache eviction policy for Credential Cache entries. */
public class CredentialCaffeineCacheExpiry implements Expiry<Object, Object> {
    @Override
    public long expireAfterCreate(Object key, Object value, long currentTime) {
        if (value instanceof AbstractCredentialInfo credentialInfo) {
            return TimeUnit.SECONDS.toNanos(credentialInfo.getTimeToLive());
        }
        return TimeUnit.SECONDS.toNanos(DEFAULT_CACHE_EXPIRE_TIME_IN_MINUTES * 60);
    }

    @Override
    public long expireAfterUpdate(
            Object key, Object value, long currentTime, long currentDuration) {
        return currentDuration;
    }

    @Override
    public long expireAfterRead(Object key, Object value, long currentTime, long currentDuration) {
        return currentDuration;
    }
}
