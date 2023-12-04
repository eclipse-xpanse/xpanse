/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.runtime.modules.common;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import org.eclipse.xpanse.common.openapi.OpenApiGeneratorJarManage;
import org.eclipse.xpanse.common.openapi.OpenApiUrlManage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {OpenApiUrlManage.class, OpenApiGeneratorJarManage.class})
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=default"})
class OpenApiGeneratorJarManageTest {

    @Autowired
    private OpenApiGeneratorJarManage openApiGeneratorJarManage;

    @Value("${openapi.path}")
    private String openApiPath;
    @Value("${openapi.download-generator-client-url}")
    private String clientDownloadUrl;

    @Test
    void testGetOpenApiWorkdir() {
        String result = openApiGeneratorJarManage.getOpenApiWorkdir();
        assertNotEquals(0, result.length());
    }

    @Test
    void testDownloadClientJar() throws IOException {
        // SetUp
        File jarFile = openApiGeneratorJarManage.getCliFile();
        URL url = URI.create(clientDownloadUrl).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();

        if (!jarFile.exists()) {
            // Run the test
            boolean result = openApiGeneratorJarManage.downloadClientJar();
            // Verify the results
            Assertions.assertTrue(result);
        } else if (jarFile.delete()) {
            // Run the test
            boolean result = openApiGeneratorJarManage.downloadClientJar();
            // Verify the results
            Assertions.assertTrue(result);
        }

    }

    @Test
    void testGetClientDownLoadUrl() {
        // Run the test
        String result = openApiGeneratorJarManage.getClientDownLoadUrl();
        // Verify the results
        Assertions.assertEquals(clientDownloadUrl, result);
    }

    @Test
    void testGetOpenapiPath() {
        // Run the test
        String result = openApiGeneratorJarManage.getOpenapiPath();
        // Verify the results
        Assertions.assertEquals(openApiPath, result);
    }
}

