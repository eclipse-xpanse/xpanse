/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.utils;

import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * The class is used to describe the structure and validation rules of JSON data.
 */
@Data
public class JsonObjectSchema {

    private String type = "object";
    private Map<String, Map<String, Object>> properties;
    private List<String> required;
    private boolean additionalProperties;

}
