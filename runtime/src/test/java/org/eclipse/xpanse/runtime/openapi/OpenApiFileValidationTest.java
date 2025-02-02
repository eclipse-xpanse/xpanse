/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.runtime.openapi;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import java.io.FileOutputStream;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@Slf4j
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.profiles.active=generate-openapi-doc,noauth,test,dev"})
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
public class OpenApiFileValidationTest {

    private static final String API_DOCS_PATH = "/v3/api-docs";
    private static final String DOWNLOAD_OPENAPI_JSON_PATH = "target/openapi.json";

    @Autowired private MockMvc mockMvc;

    @Test
    void validateOpenApiDoc() throws Exception {
        final MvcResult response =
                mockMvc.perform(get(API_DOCS_PATH)).andExpect(status().isOk()).andReturn();

        assertNotNull(response);
        assertNotNull(response.getResponse());
        final byte[] file = response.getResponse().getContentAsByteArray();
        assertNotEquals(0, file.length);
        try (final FileOutputStream fos = new FileOutputStream(DOWNLOAD_OPENAPI_JSON_PATH)) {
            fos.write(file);
        }
        SwaggerParseResult result =
                new OpenAPIV3Parser().readLocation(DOWNLOAD_OPENAPI_JSON_PATH, null, null);
        log.info("OpenAPI validation result: {}", result.getMessages());
        assertTrue(
                Objects.isNull(result.getMessages()) || result.getMessages().isEmpty(),
                String.join(",", result.getMessages()));
    }
}
