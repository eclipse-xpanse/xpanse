/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service;

import lombok.Data;

/**
 * Billing product result model.
 */
@Data
public class BillingProductResult {

    private String id;

    private String productId;

    private Double amount;

    private Double discountAmount;

    private Double officialWebsiteAmount;

    private Integer measureId;

}
