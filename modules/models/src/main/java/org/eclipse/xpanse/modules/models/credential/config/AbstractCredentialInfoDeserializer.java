/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.credential.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;

/**
 * Custom deserializer for AbstractCredentialInfo class.
 * This deserializer is used to deserialize JSON data into an instance of AbstractCredentialInfo,
 * this is needed because of the final fields which Jackson cannot handle.
 */
@Slf4j
public class AbstractCredentialInfoDeserializer extends StdDeserializer<AbstractCredentialInfo> {

    /**
     * Default constructor.
     */
    public AbstractCredentialInfoDeserializer() {
        this(AbstractCredentialInfo.class);
    }

    /**
     * The constructor with the class to be deserialized.
     */
    public AbstractCredentialInfoDeserializer(Class<?> deserializedClass) {
        super(deserializedClass);
    }

    @Override
    public AbstractCredentialInfo deserialize(
            JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        Csp csp;
        try {
            csp = Csp.getByValue(node.get("csp").asText());
        } catch (Exception e) {
            log.error("Unsupported csp value: {}", node.get("csp").asText());
            return null;
        }
        CredentialType type;
        try {
            type = CredentialType.getByValue(node.get("type").asText());
        } catch (UnsupportedEnumValueException e) {
            log.error("Unsupported credential type: {}", node.get("type").asText());
            return null;
        }

        try {
            String name = safeGet(node, "name", JsonNode::asText);
            String description = safeGet(node, "description", JsonNode::asText);
            String userId = safeGet(node, "userId", JsonNode::asText);
            Integer timeToLive = safeGet(node, "timeToLive", JsonNode::asInt);
            List<CredentialVariable> variables =
                    deserializeCredentialVariables(node.get("variables"));
            return new CredentialVariables(csp, type, name, description, userId, timeToLive,
                    variables);
        } catch (Exception e) {
            log.error("IllegalArgumentException: {}", e.getMessage());
        }
        return null;
    }

    private <T> T safeGet(JsonNode node, String fieldName, Function<JsonNode, T> mapper) {
        if (node == null || node.get(fieldName) == null) {
            return null;
        }
        try {
            return mapper.apply(node.get(fieldName));
        } catch (Exception e) {
            log.error("Failed to map field {} due to: {}", fieldName, e.getMessage());
            return null;
        }
    }


    private List<CredentialVariable> deserializeCredentialVariables(JsonNode node) {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(CredentialVariable.class, new CredentialVariableDeserializer());
        mapper.registerModule(module);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        List<CredentialVariable> credentialVariable = null;
        try {
            credentialVariable = mapper.convertValue(node,
                    mapper.getTypeFactory().constructCollectionType(List.class,
                            CredentialVariable.class));
        } catch (IllegalArgumentException e) {
            log.error("Deserialize CredentialVariables with value:{} failed.", node, e);
        }
        return credentialVariable;
    }
}
