/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.credential;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.eclipse.xpanse.modules.credential.cache.CaffeineCredentialCacheManager;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {CaffeineCredentialCacheManager.class, CredentialsStore.class})
@ExtendWith(SpringExtension.class)
class CredentialCacheTests {

    @Autowired
    CaffeineCredentialCacheManager caffeineCredentialCacheManager;

    @Autowired
    CredentialsStore credentialsStore;

    @Test
    void testWriteToCache() {
        credentialsStore.storeCredential(Csp.OPENSTACK, "user", getCredentialToBeStored());
        Assertions.assertTrue(Objects.nonNull(
                credentialsStore.getCredential(Csp.OPENSTACK, CredentialType.VARIABLES, "user")));
    }

    @Test
    void testCacheEviction() {
        credentialsStore.storeCredential(Csp.OPENSTACK, "user", getCredentialToBeStored());
        Assertions.assertTrue(Objects.nonNull(
                credentialsStore.getCredential(Csp.OPENSTACK, CredentialType.VARIABLES, "user")));
        Awaitility.await().pollDelay(6, TimeUnit.SECONDS).until(() -> true);
        Assertions.assertTrue(Objects.isNull(
                credentialsStore.getCredential(Csp.OPENSTACK, CredentialType.VARIABLES, "user")));
    }

    @Test
    void testCacheKey() {
        credentialsStore.storeCredential(Csp.HUAWEI, "user", getCredentialToBeStored());
        Assertions.assertTrue(Objects.isNull(
                credentialsStore.getCredential(Csp.OPENSTACK, CredentialType.VARIABLES, "user")));
        Assertions.assertTrue(Objects.isNull(
                credentialsStore.getCredential(Csp.OPENSTACK, CredentialType.API_KEY, "user123")));
    }

    @Test
    void testCacheDeletion() {
        credentialsStore.storeCredential(Csp.OPENSTACK, "user", getCredentialToBeStored());
        Assertions.assertTrue(Objects.nonNull(
                credentialsStore.getCredential(Csp.OPENSTACK, CredentialType.VARIABLES, "user")));
        credentialsStore.deleteCredential(Csp.OPENSTACK, CredentialType.VARIABLES,
                getCredentialToBeStored().getXpanseUser());
        Assertions.assertTrue(Objects.isNull(
                credentialsStore.getCredential(Csp.OPENSTACK, CredentialType.VARIABLES, "user")));
    }

    private AbstractCredentialInfo getCredentialToBeStored() {
        CredentialVariables credentialVariables = new CredentialVariables(
                Csp.OPENSTACK, "user", "test", "test", CredentialType.VARIABLES, new ArrayList<>());
        credentialVariables.setTimeToLive(5);
        return credentialVariables;
    }
}
