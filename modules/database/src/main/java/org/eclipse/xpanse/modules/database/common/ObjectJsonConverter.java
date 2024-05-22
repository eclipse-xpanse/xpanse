/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 * Converter to handle object data type and string automatic conversion between database and the
 * entity.
 */
@Converter
public class ObjectJsonConverter implements AttributeConverter<Object, String> {

    private final ObjectMapper objectMapper =
            new ObjectMapper().registerModule(new JavaTimeModule())
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
                    .configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);

    @Override
    public String convertToDatabaseColumn(Object object) {
        if (Objects.isNull(object)) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Serialising object to string failed.", ex);
        }
    }

    @Override
    public Object convertToEntityAttribute(String dataJson) {
        if (StringUtils.isEmpty(dataJson)) {
            return null;
        }
        try {
            return objectMapper.readValue(dataJson, Object.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Serialising string to object failed.", ex);
        }
    }

}
