/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime.cache.redis;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.xpanse.api.controllers.UserCloudCredentialsApi;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CreateCredential;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.plugins.huaweicloud.monitor.constant.HuaweiCloudMonitorConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/** Test for UserCloudCredentialsApi with Redis cache. */
@ExtendWith(SpringExtension.class)
@SpringBootTest(
        properties = {
            "spring.profiles.active=oauth,zitadel,zitadel-testbed,test,dev",
            "enable.redis.distributed.cache=true"
        })
@AutoConfigureMockMvc
class UserCredentialsWithRedisTest extends AbstractRedisIntegrationTest {

    private final Csp csp = Csp.HUAWEI_CLOUD;
    private final String site = "Chinese Mainland";
    private final String credentialName = "AK_SK";
    private final CredentialType credentialType = CredentialType.VARIABLES;
    @Resource private UserCloudCredentialsApi userCloudCredentialsApi;

    @Test
    @WithJwt(file = "jwt_user.json")
    void testUserCloudCredentialApis() {
        testAddCredential();
        testUpdateCredential();
        testDeleteCredential();
    }

    private CreateCredential getCreateCredential(Integer timeToLive) {
        final CreateCredential createCredential = new CreateCredential();
        createCredential.setCsp(csp);
        createCredential.setSite(site);
        createCredential.setType(credentialType);
        createCredential.setName(credentialName);
        createCredential.setDescription("description");
        List<CredentialVariable> credentialVariables = new ArrayList<>();
        credentialVariables.add(
                new CredentialVariable(
                        HuaweiCloudMonitorConstants.HW_ACCESS_KEY,
                        "The access key.",
                        true,
                        false,
                        "AK_VALUE"));
        credentialVariables.add(
                new CredentialVariable(
                        HuaweiCloudMonitorConstants.HW_SECRET_KEY,
                        "The security key.",
                        true,
                        false,
                        "SK_VALUE"));
        createCredential.setVariables(credentialVariables);
        createCredential.setTimeToLive(timeToLive);
        return createCredential;
    }

    void testAddCredential() {
        // Setup
        final CreateCredential createCredential = getCreateCredential(300);

        userCloudCredentialsApi.addUserCloudCredential(createCredential);

        List<AbstractCredentialInfo> credentialInfos =
                userCloudCredentialsApi.getUserCloudCredentials(csp, credentialType);

        // Verify the results
        Assertions.assertFalse(credentialInfos.isEmpty());
        Assertions.assertInstanceOf(CredentialVariables.class, credentialInfos.getFirst());
        CredentialVariables credentialVariables = (CredentialVariables) credentialInfos.getFirst();
        Assertions.assertEquals(
                createCredential.getVariables(), credentialVariables.getVariables());
    }

    void testUpdateCredential() {
        // Setup
        final CreateCredential updateCredential = getCreateCredential(600);

        userCloudCredentialsApi.updateUserCloudCredential(updateCredential);

        List<AbstractCredentialInfo> credentialInfos =
                userCloudCredentialsApi.getUserCloudCredentials(csp, credentialType);

        // Verify the results
        Assertions.assertFalse(credentialInfos.isEmpty());
        Assertions.assertInstanceOf(CredentialVariables.class, credentialInfos.getFirst());
        CredentialVariables credentialVariables = (CredentialVariables) credentialInfos.getFirst();
        Assertions.assertEquals(
                updateCredential.getVariables(), credentialVariables.getVariables());
    }

    void testDeleteCredential() {
        // Setup
        userCloudCredentialsApi.deleteUserCloudCredential(
                csp, site, credentialType, credentialName);
        // Verify the results
        List<AbstractCredentialInfo> credentialInfos =
                userCloudCredentialsApi.getUserCloudCredentials(csp, credentialType);
        assertTrue(credentialInfos.isEmpty());
    }
}
