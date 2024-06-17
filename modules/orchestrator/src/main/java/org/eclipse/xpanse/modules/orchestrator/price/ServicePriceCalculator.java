/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.orchestrator.price;

import org.eclipse.xpanse.modules.models.billing.FlavorPriceResult;

/**
 * Interface to calculate the service price.
 */
public interface ServicePriceCalculator {

    FlavorPriceResult getServiceFlavorPrice(ServiceFlavorPriceRequest request);
}
