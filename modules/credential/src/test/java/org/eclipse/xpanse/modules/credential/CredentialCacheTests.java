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
                credentialsStore.getCredential(Csp.OPENSTACK, CredentialType.VARIABLES, "user")));
    }

    @Test
    void testCacheEviction() {
        credentialsStore.storeCredential(getCredentialToBeStored());
        assertTrue(Objects.nonNull(
                credentialsStore.getCredential(Csp.OPENSTACK, CredentialType.VARIABLES, "user")));
        Awaitility.await().pollDelay(6, TimeUnit.SECONDS).until(() -> true);
        assertTrue(Objects.isNull(
                credentialsStore.getCredential(Csp.OPENSTACK, CredentialType.VARIABLES, "user")));
    }

    @Test
    void testCacheKey() {
        credentialsStore.storeCredential(getCredentialToBeStored());
        assertTrue(Objects.nonNull(
                credentialsStore.getCredential(Csp.OPENSTACK, CredentialType.VARIABLES, "user")));
        assertTrue(Objects.isNull(
                credentialsStore.getCredential(Csp.OPENSTACK, CredentialType.API_KEY, "user123")));
        CredentialCacheKey key1 =
                new CredentialCacheKey(Csp.FLEXIBLE_ENGINE, "user1", CredentialType.API_KEY);
        CredentialCacheKey key2 =
                new CredentialCacheKey(Csp.FLEXIBLE_ENGINE, "user1", CredentialType.API_KEY);
        CredentialCacheKey key3 =
                new CredentialCacheKey(Csp.FLEXIBLE_ENGINE, "user2", CredentialType.API_KEY);
        CredentialCacheKey key4 =
                new CredentialCacheKey(Csp.OPENSTACK, "user1", CredentialType.API_KEY);
        CredentialCacheKey key5 =
                new CredentialCacheKey(Csp.FLEXIBLE_ENGINE, "user1", CredentialType.VARIABLES);

        CredentialCacheKey key6 = new CredentialCacheKey(null, "user1", CredentialType.API_KEY);
        CredentialCacheKey key7 =
                new CredentialCacheKey(Csp.FLEXIBLE_ENGINE, null, CredentialType.API_KEY);
        CredentialCacheKey key8 = new CredentialCacheKey(Csp.FLEXIBLE_ENGINE, "user1", null);

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
                credentialsStore.getCredential(Csp.OPENSTACK, CredentialType.VARIABLES, "user")));
        credentialsStore.deleteCredential(Csp.OPENSTACK, CredentialType.VARIABLES,
                getCredentialToBeStored().getXpanseUser());
        assertTrue(Objects.isNull(
                credentialsStore.getCredential(Csp.OPENSTACK, CredentialType.VARIABLES, "user")));
    }

    private AbstractCredentialInfo getCredentialToBeStored() {
        CredentialVariables credentialVariables = new CredentialVariables(
                Csp.OPENSTACK, "user", "test", "test", CredentialType.VARIABLES, new ArrayList<>());
        credentialVariables.setTimeToLive(5);
        return credentialVariables;
    }
}
