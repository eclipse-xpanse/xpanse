/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Data model to describe how the request or response body of a method in the service controller's
 * OpenAPI schema will look like.
 */
@Data
public class CustomBody {

    @NotNull
    @Schema(
            description =
                    "This is the name to be given the body type. This replaces the common name from"
                        + " xpanse. Additionally to this common type, the additional type is added"
                        + " as a property.")
    private String typeName;

    @Schema(
            description =
                    "JSON Schema representation of fields that are added to the standard xpanse"
                        + " request body. The standard request body depends on the service order"
                        + " type. This additional type is added with the title as key to the the"
                        + " original data model.",
            examples = {
                // CHECKSTYLE OFF: Indentation
                """
                  {
                      "$schema": "https://json-schema.org/draft/2020-12/schema#",
                      "title": "User",
                      "type": "object",
                      "properties": {
                        "id": { "type": "string" },
                        "age": { "type": "integer" },
                        "tags": {
                          "type": "array",
                          "items": { "type": "string" }
                        }
                      }
                    }
                """
                // CHECKSTYLE ON: Indentation
            })
    private String additionalType;
}
