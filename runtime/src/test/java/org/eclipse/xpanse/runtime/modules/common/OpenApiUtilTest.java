/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.runtime.modules.common;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.xpanse.common.openapi.OpenApiUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {OpenApiUtil.class})
@ExtendWith(SpringExtension.class)
class OpenApiUtilTest {

    @Autowired
    private OpenApiUtil openApiUtil;


    @Test
    void testGetServiceUrl() {
        String result = openApiUtil.getServiceUrl();
        assertNotEquals(0, result.length());
    }

    @Test
    void testGetOpenApiUrl() {
        final String result = openApiUtil.getOpenApiUrl("id");

        assertNotEquals(0, result.length());
    }

    @Test
    void testDownloadClientJar() throws Exception {
        assertTrue(openApiUtil.downloadClientJar(openApiUtil.getOpenApiWorkdir()));

    }

    @Test
    void testGetOpenApiWorkdir() {
        String result = openApiUtil.getOpenApiWorkdir();
        assertNotEquals(0, result.length());
    }
}

