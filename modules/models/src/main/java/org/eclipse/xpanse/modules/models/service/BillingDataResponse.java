/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service;

import java.util.List;
import java.util.UUID;
import lombok.Data;

/**
 * Billing model.
 */
@Data
public class BillingDataResponse {

    private UUID serviceId;

    private Double amount;

    private Double discountAmount;

    private Double officialWebsiteAmount;

    private Integer measureId;

    private String currency;

    private List<BillingProductResult> billingProductResults;

}
