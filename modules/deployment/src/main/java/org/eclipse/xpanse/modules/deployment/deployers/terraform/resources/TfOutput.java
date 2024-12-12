/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/** TfOutput class. */
@Slf4j
@Data
public class TfOutput {

    private static final ObjectMapper MAPPER =
            new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private String type;
    private String value;

    /**
     * Convert type object to string.
     *
     * @param typeObject type object.
     */
    @JsonProperty("type")
    public void setType(Object typeObject) {
        if (typeObject instanceof String) {
            this.type = (String) typeObject;
        } else {
            this.type = getJsonString(typeObject);
        }
    }

    /**
     * Convert value object to string.
     *
     * @param valueObject value object.
     */
    @JsonProperty("value")
    public void setValue(Object valueObject) {
        if (valueObject instanceof String) {
            this.value = (String) valueObject;
        } else {
            this.value = getJsonString(valueObject);
        }
    }

    private String getJsonString(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert object to json string:{}", object, e);
        }
        return null;
    }
}
