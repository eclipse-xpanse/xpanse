/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.methods;

import lombok.Data;

/**
 * Describes the method parameters of the method generated in the service controller's OpenAPI
 * schema. It's for the query and path parameters.
 */
@Data
public class MethodParameter {

    private String name;
    private String description;
    private Boolean mandatory;
    private String in;
}
