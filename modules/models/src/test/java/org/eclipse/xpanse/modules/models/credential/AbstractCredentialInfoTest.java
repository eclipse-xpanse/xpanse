/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.credential;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.junit.jupiter.api.Test;

/** Test of AbstractCredentialInfo. */
class AbstractCredentialInfoTest {

    @Test
    public void testConstructorAndGetters() {
        Csp csp = Csp.HUAWEI_CLOUD;
        String site = "International";
        String userId = "userId";
        String name = "credential";
        String description = "Test credential";
        CredentialType type = CredentialType.VARIABLES;
        Integer timeToLive = 100000;

        AbstractCredentialInfo credentialInfo =
                new AbstractCredentialInfoImpl(csp, site, type, name, description, userId);

        credentialInfo.setTimeToLive(timeToLive);

        assertEquals(csp, credentialInfo.getCsp());
        assertEquals(userId, credentialInfo.getUserId());
        assertEquals(site, credentialInfo.getSite());
        assertEquals(name, credentialInfo.getName());
        assertEquals(description, credentialInfo.getDescription());
        assertEquals(type, credentialInfo.getType());
        assertEquals(timeToLive, credentialInfo.getTimeToLive());
    }

    private static class AbstractCredentialInfoImpl extends AbstractCredentialInfo {
        AbstractCredentialInfoImpl(
                Csp csp,
                String site,
                CredentialType type,
                String name,
                String description,
                String userId) {
            super(csp, site, type, name, description, userId);
        }
    }
}
