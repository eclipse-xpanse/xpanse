/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.config;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;

/**
 * Converter configuration for the yaml.
 */
@Configuration
public class YamlJackson2HttpMessageConverterConfiguration {

    static final class YamlJackson2HttpMessageConverter
            extends AbstractJackson2HttpMessageConverter {

        YamlJackson2HttpMessageConverter() {
            super(new YAMLMapper(), MediaType.parseMediaType("application/x-yaml"),
                    MediaType.parseMediaType("application/yaml"),
                    MediaType.parseMediaType("application/yml"));
        }
    }

    @Bean
    public AbstractJackson2HttpMessageConverter fastJsonHttpMessageConverters() {
        return new YamlJackson2HttpMessageConverter();
    }
}