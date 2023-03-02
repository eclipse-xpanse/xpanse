/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.register;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.eclipse.xpanse.modules.ocl.loader.data.models.Ocl;

/**
 * Converter to handle Oclv2 data type and string automatic conversion between database and the
 * entity.
 */
@Converter
public class OclConverter implements AttributeConverter<Ocl, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Ocl ocl) {
        try {
            return
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(ocl);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Serialising Oclv2 object to string failed.", ex);
        }
    }

    @Override
    public Ocl convertToEntityAttribute(String s) {
        try {
            return objectMapper.readValue(s, Ocl.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
