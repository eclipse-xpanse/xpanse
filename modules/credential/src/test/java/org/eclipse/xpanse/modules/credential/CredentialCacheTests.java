/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.credential;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.eclipse.xpanse.modules.credential.cache.CaffeineCredentialCacheManager;
import org.eclipse.xpanse.modules.credential.cache.CredentialCacheKey;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Test of CredentialCacheKey.
 */
@ContextConfiguration(classes = {CaffeineCredentialCacheManager.class, CredentialsStore.class})
@ExtendWith(SpringExtension.class)
class CredentialCacheTests {

    @Autowired
    CaffeineCredentialCacheManager caffeineCredentialCacheManager;

    @Autowired
    CredentialsStore credentialsStore;

    @Test
    void testWriteToCache() {
        credentialsStore.storeCredential(getCredentialToBeStored());
        assertTrue(Objects.nonNull(
                credentialsStore.getCredential(Csp.OPENSTACK, CredentialType.VARIABLES, "AK_SK",
                        "userId")));
    }

    @Test
    void testCacheEviction() {
        credentialsStore.storeCredential(getCredentialToBeStored());
        assertTrue(Objects.nonNull(
                credentialsStore.getCredential(Csp.OPENSTACK, CredentialType.VARIABLES, "AK_SK",
                        "userId")));
        Awaitility.await().pollDelay(6, TimeUnit.SECONDS).until(() -> true);
        assertTrue(Objects.isNull(
                credentialsStore.getCredential(Csp.OPENSTACK, CredentialType.VARIABLES, "AK_SK",
                        "userId")));
    }

    @Test
    void testCacheKey() {
        credentialsStore.storeCredential(getCredentialToBeStored());
        assertTrue(Objects.nonNull(
                credentialsStore.getCredential(Csp.OPENSTACK, CredentialType.VARIABLES, "AK_SK",
                        "userId")));
        assertTrue(Objects.isNull(
                credentialsStore.getCredential(Csp.OPENSTACK, CredentialType.API_KEY, "AK_SK",
                        "user123")));
        CredentialCacheKey key1 =
                new CredentialCacheKey(Csp.FLEXIBLE_ENGINE, CredentialType.API_KEY, "AK_SK",
                        "userId");
        CredentialCacheKey key2 =
                new CredentialCacheKey(Csp.FLEXIBLE_ENGINE, CredentialType.API_KEY, "AK_SK",
                        "userId");
        CredentialCacheKey key3 =
                new CredentialCacheKey(Csp.FLEXIBLE_ENGINE, CredentialType.API_KEY, "AK_SK",
                        "userId1");
        CredentialCacheKey key4 =
                new CredentialCacheKey(Csp.OPENSTACK, CredentialType.API_KEY, "AK_SK", "userId1");
        CredentialCacheKey key5 =
                new CredentialCacheKey(Csp.FLEXIBLE_ENGINE, CredentialType.VARIABLES, "AK_SK",
                        "userId1");

        CredentialCacheKey key6 =
                new CredentialCacheKey(null, CredentialType.API_KEY, "AK_SK", "userId1");
        CredentialCacheKey key7 =
                new CredentialCacheKey(Csp.FLEXIBLE_ENGINE, CredentialType.API_KEY, "",
                        "userId1");
        CredentialCacheKey key8 =
                new CredentialCacheKey(Csp.FLEXIBLE_ENGINE, CredentialType.API_KEY, "", null);

        assertEquals(key1, key1);
        assertEquals(key1, key2);
        assertEquals(key2, key1);

        assertNotEquals(null, key1);
        assertNotEquals(key1, key3);
        assertNotEquals(key1, key4);
        assertNotEquals(key1, key5);

        assertEquals(key1.hashCode(), key1.hashCode());
        assertEquals(key1.hashCode(), key2.hashCode());

        assertNotEquals(key1.hashCode(), key3.hashCode());
        assertNotEquals(key1.hashCode(), key4.hashCode());
        assertNotEquals(key1.hashCode(), key5.hashCode());
        assertNotEquals(key1.hashCode(), key6.hashCode());
        assertNotEquals(key1.hashCode(), key7.hashCode());
        assertNotEquals(key1.hashCode(), key8.hashCode());
    }

    @Test
    void testCacheDeletion() {
        credentialsStore.storeCredential(getCredentialToBeStored());
        assertTrue(Objects.nonNull(
                credentialsStore.getCredential(Csp.OPENSTACK, CredentialType.VARIABLES, "AK_SK",
                        "userId")));
        credentialsStore.deleteCredential(Csp.OPENSTACK, CredentialType.VARIABLES, "AK_SK",
                getCredentialToBeStored().getUserId());
        assertTrue(Objects.isNull(
                credentialsStore.getCredential(Csp.OPENSTACK, CredentialType.VARIABLES, "AK_SK",
                        "userId")));
    }

    private AbstractCredentialInfo getCredentialToBeStored() {
        CredentialVariables credentialVariables = new CredentialVariables(
                Csp.OPENSTACK, CredentialType.VARIABLES, "AK_SK", "test", "userId",
                new ArrayList<>());
        credentialVariables.setTimeToLive(5);
        return credentialVariables;
    }
}
