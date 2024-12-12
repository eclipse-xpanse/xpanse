/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.config;

import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This customization is needed to work around an open issue in swagger-core which is used by
 * spring-doc for generating the openapi schema. <a
 * href="https://github.com/swagger-api/swagger-core/issues/4316">...</a> without this fix, the
 * datatype for Object will be shown as Map always. So a {@code Map<String, Object>}` will be {@code
 * Map<String, Map<String, Object>} and this causes issues in the client.
 */
@Configuration
public class OpenApiCustomizerConfig {

    @Bean
    public OpenApiCustomizer enableArbitraryObjects() {
        return openApi ->
                openApi.getComponents().getSchemas().values().forEach(this::enableArbitraryObjects);
    }

    private void enableArbitraryObjects(Schema<Object> schema) {
        if (schema instanceof MapSchema) {
            if (schema.getAdditionalProperties() instanceof Schema
                    && ((Schema<?>) schema.getAdditionalProperties())
                            .getType()
                            .equalsIgnoreCase("object")) {
                schema.setAdditionalProperties(true);
            }
        } else if (schema.getType() != null
                && schema.getType().equalsIgnoreCase("object")
                && schema.getProperties() != null) {
            Map<String, Schema> properties = schema.getProperties();
            properties.values().forEach(this::enableArbitraryObjects);
        }
    }
}
