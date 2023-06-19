/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.credential;

import org.eclipse.xpanse.modules.credential.cache.CaffeineCredentialCacheManager;
import org.eclipse.xpanse.modules.credential.cache.CredentialCacheKey;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Component which acts as the gateway to credentials stored in cache.
 */
@Component
public class CredentialsStore {

    private final CaffeineCredentialCacheManager caffeineCredentialCacheManager;

    /**
     * Constructor for CredentialsStore.
     *
     * @param caffeineCredentialCacheManager instance of CaffeineCredentialCacheManager class.
     */
    @Autowired
    public CredentialsStore(CaffeineCredentialCacheManager caffeineCredentialCacheManager) {
        this.caffeineCredentialCacheManager = caffeineCredentialCacheManager;
    }

    /**
     * Methods to add credentials to credentials store.
     *
     * @param abstractCredentialInfo Complete credential configuration object.
     */
    public void storeCredential(AbstractCredentialInfo abstractCredentialInfo) {
        CredentialCacheKey credentialCacheKey =
                new CredentialCacheKey(abstractCredentialInfo.getCsp(),
                        abstractCredentialInfo.getXpanseUser(), abstractCredentialInfo.getType());
        this.caffeineCredentialCacheManager.put(credentialCacheKey, abstractCredentialInfo);
    }

    /**
     * Method to get credential data from credentials store.
     *
     * @param csp            CSP to which the credential belongs to.
     * @param xpanseUserName xpanseUserName to which the credential belongs to.
     * @param credentialType Type of the credential to be searched for.
     * @return returns AbstractCredentialInfo which contains the complete credential information.
     */
    public AbstractCredentialInfo getCredential(Csp csp, CredentialType credentialType,
                                                String xpanseUserName) {
        CredentialCacheKey credentialCacheKey =
                new CredentialCacheKey(csp, xpanseUserName, credentialType);
        return this.caffeineCredentialCacheManager.get(credentialCacheKey);
    }

    /**
     * Method to delete credential data from credentials store.
     *
     * @param csp            CSP to which the credential belongs to.
     * @param xpanseUserName xpanseUserName to which the credential belongs to.
     * @param credentialType Type of the credential to be searched for.
     */
    public void deleteCredential(Csp csp, CredentialType credentialType, String xpanseUserName) {
        CredentialCacheKey credentialCacheKey =
                new CredentialCacheKey(csp, xpanseUserName, credentialType);
        this.caffeineCredentialCacheManager.remove(credentialCacheKey);
    }
}
