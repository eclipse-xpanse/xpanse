/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.runtime.modules.credential;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.File;
import org.eclipse.xpanse.common.openapi.OpenApiGeneratorJarManage;
import org.eclipse.xpanse.common.openapi.OpenApiUrlManage;
import org.eclipse.xpanse.modules.cache.credential.CredentialsStore;
import org.eclipse.xpanse.modules.credential.CredentialOpenApiGenerator;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.runtime.XpanseApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** Test of CredentialOpenApiGenerator. */
@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = {
            XpanseApplication.class,
            CredentialsStore.class,
            ServletUriComponentsBuilder.class,
            OpenApiUrlManage.class,
            PluginManager.class
        },
        properties = {
            "spring.profiles.active=oauth,zitadel,zitadel-testbed,terraform-boot,tofu-maker,test,dev"
        })
class CredentialOpenApiGeneratorTest {

    @Autowired private CredentialOpenApiGenerator credentialOpenApiGenerator;

    @Autowired private OpenApiGeneratorJarManage openApiGeneratorJarManage;

    @Test
    void testGetServiceUrl() {

        final String result = credentialOpenApiGenerator.getServiceUrl();

        assertNotEquals(0, result.length());
    }

    @Test
    void testRun() {
        credentialOpenApiGenerator.onApplicationEvent(null);
    }

    @Test
    void testGetCredentialOpenApiUrl() {
        Csp csp = Csp.HUAWEI_CLOUD;
        CredentialType type = CredentialType.VARIABLES;

        String htmlFileName = csp.toValue() + "_" + type.toValue() + "_credentialApi.html";
        String credentialApiDir = openApiGeneratorJarManage.getOpenApiWorkdir();
        File htmlFile = new File(credentialApiDir, htmlFileName);
        if (htmlFile.exists()) {
            htmlFile.delete();
        }

        String result = credentialOpenApiGenerator.getCredentialOpenApiUrl(csp, type);

        assertNotEquals(0, result.length());
    }
}
