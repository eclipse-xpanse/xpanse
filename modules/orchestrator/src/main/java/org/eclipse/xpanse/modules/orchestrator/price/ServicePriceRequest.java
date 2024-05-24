/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.orchestrator.price;

import lombok.Data;
import org.eclipse.xpanse.modules.models.billing.RatingMode;
import org.eclipse.xpanse.modules.models.billing.enums.BillingMode;

/**
 * Define the request of service pricing.
 */
@Data
public class ServicePriceRequest {
    private String userId;
    private String regionName;
    private RatingMode flavorRatingMode;
    private BillingMode billingMode;
}