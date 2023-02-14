/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.eclipse.xpanse.modules.ocl.state.OclResources;

/**
 * Converter to handle OclResources data type and string automatic conversion between database
 * and the entity.
 */
@Converter
public class OclResourcesConverter implements AttributeConverter<OclResources, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(OclResources oclResources) {
        try {
            return
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(oclResources);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Serial OCL object to json failed.", ex);
        }
    }

    @Override
    public OclResources convertToEntityAttribute(String s) {
        try {
            return objectMapper.readValue(s, OclResources.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
