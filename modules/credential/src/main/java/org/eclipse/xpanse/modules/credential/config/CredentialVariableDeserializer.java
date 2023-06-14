/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.credential.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import org.eclipse.xpanse.modules.credential.CredentialVariable;

/**
 * Custom deserializer for CredentialVariable class.
 * This deserializer is used to deserialize JSON data into an instance of CredentialVariable,
 * this is needed because of the final fields which Jackson cannot handle.
 */
public class CredentialVariableDeserializer extends StdDeserializer<CredentialVariable> {

    public CredentialVariableDeserializer() {
        this(null);
    }

    public CredentialVariableDeserializer(Class<?> deserializedClass) {
        super(deserializedClass);
    }

    @Override
    public CredentialVariable deserialize(JsonParser jsonParser,
                                          DeserializationContext deserializationContext) throws
            IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        String name = node.get("name").asText();
        String description = node.get("description").asText();
        boolean isMandatory = node.get("mandatory").asBoolean();
        boolean isSensitive = node.get("sensitive").asBoolean();
        String value = node.get("value").asText();

        return new CredentialVariable(name, description, isMandatory, isSensitive, value);
    }
}