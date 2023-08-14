/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.runtime.modules.common;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.xpanse.common.openapi.OpenApiGeneratorJarManage;
import org.eclipse.xpanse.common.openapi.OpenApiUrlManage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {OpenApiUrlManage.class, OpenApiGeneratorJarManage.class})
@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {"openapi.download-generator-client-url=https://repo1.maven.org"
        + "/maven2/org/openapitools/openapi-generator-cli/6.6.0/openapi-generator-cli-6.6.0.jar",
        "openapi.path=openapi",
        "server.port=8080"})
class OpenApiUrlManageTest {

    @Autowired
    private OpenApiUrlManage openApiUrlManage;

    @Autowired
    private OpenApiGeneratorJarManage openApiGeneratorJarManage;

    @Test
    void testGetServiceUrl() {
        String result = openApiUrlManage.getServiceUrl();
        assertNotEquals(0, result.length());
    }

    @Test
    void testGetOpenApiUrl() {
        final String result = openApiUrlManage.getOpenApiUrl("id");

        assertNotEquals(0, result.length());
    }

    @Test
    void testDownloadClientJar() throws Exception {
        assertTrue(openApiGeneratorJarManage.downloadClientJar());

    }

    @Test
    void testGetOpenApiWorkdir() {
        String result = openApiGeneratorJarManage.getOpenApiWorkdir();
        assertNotEquals(0, result.length());
    }
}

