/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.huaweicloud.price.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/** Defines data model for price calculation with the global rest apis of HuaweiCloud. */
@Data
public class ProductInfo {
    @JsonProperty("cloudServiceType")
    private String cloudServiceType;

    @JsonProperty("id")
    private String id;

    @JsonProperty("productNum")
    private int productNum;

    @JsonProperty("resourceSpecCode")
    private String resourceSpecCode;

    @JsonProperty("resourceType")
    private String resourceType;

    @JsonProperty("usageFactor")
    private String usageFactor;

    @JsonProperty("usageMeasureId")
    private int usageMeasureId;

    @JsonProperty("usageValue")
    private String usageValue;

    @JsonProperty("resourceSize")
    private Integer resourceSize;

    @JsonProperty("resouceSizeMeasureId")
    private Integer resourceSizeMeasureId;
}
