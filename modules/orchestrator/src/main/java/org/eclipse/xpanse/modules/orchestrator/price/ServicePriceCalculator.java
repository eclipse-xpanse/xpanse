/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.orchestrator.price;

import org.eclipse.xpanse.modules.models.billing.ServicePrice;

/**
 * Interface to calculate the service price.
 */
public interface ServicePriceCalculator {

    ServicePrice getServicePrice(ServicePriceRequest request);
}
