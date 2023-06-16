/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.credential.cache;

import com.github.benmanes.caffeine.cache.Expiry;
import java.util.concurrent.TimeUnit;
import org.checkerframework.checker.index.qual.NonNegative;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;

/**
 * Class to configure cache eviction policy for Credential Cache entries.
 */
public class CredentialCacheExpiry implements Expiry<CredentialCacheKey, AbstractCredentialInfo> {

    @Override
    public long expireAfterCreate(CredentialCacheKey key, AbstractCredentialInfo value,
                                  long currentTime) {
        return TimeUnit.SECONDS.toNanos(value.getTimeToLive());
    }

    @Override
    public long expireAfterUpdate(CredentialCacheKey key,
                                  AbstractCredentialInfo value, long currentTime,
                                  @NonNegative long currentDuration) {
        return currentDuration;
    }

    @Override
    public long expireAfterRead(CredentialCacheKey key,
                                AbstractCredentialInfo value, long currentTime,
                                @NonNegative long currentDuration) {
        return currentDuration;
    }
}
