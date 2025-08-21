/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.credential.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.io.Serial;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;

/**
 * Custom deserializer for CredentialVariable class. This deserializer is used to deserialize JSON
 * data into an instance of CredentialVariable, this is needed because of the final fields which
 * Jackson cannot handle.
 */
public class CredentialVariableDeserializer extends StdDeserializer<CredentialVariable> {

    @Serial private static final long serialVersionUID = 20240612001L;

    /** Constructor for CredentialVariableDeserializer. */
    public CredentialVariableDeserializer() {
        this(CredentialVariable.class);
    }

    /** Constructor for CredentialVariableDeserializer. */
    public CredentialVariableDeserializer(Class<?> deserializedClass) {
        super(deserializedClass);
    }

    @Override
    public CredentialVariable deserialize(
            JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {

        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        String name = node.get("name").isNull() ? null : node.get("name").asText();
        String description =
                node.get("description").isNull() ? null : node.get("description").asText();
        Boolean isMandatory =
                node.get("isMandatory").isNull() ? null : node.get("isMandatory").asBoolean();
        Boolean isSensitive =
                node.get("isSensitive").isNull() ? null : node.get("isSensitive").asBoolean();
        String value = node.get("value").isNull() ? null : node.get("value").asText();

        return new CredentialVariable(
                name,
                description,
                Boolean.TRUE.equals(isMandatory),
                Boolean.TRUE.equals(isSensitive),
                value);
    }
}
