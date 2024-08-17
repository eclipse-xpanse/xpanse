/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.billing.utils;

import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.billing.Price;
import org.eclipse.xpanse.modules.models.billing.PriceWithRegion;
import org.springframework.util.CollectionUtils;

/**
 * Defines common methods for service billing.
 */
public class BillingCommonUtils {

    public static final String REGION_ANY = "any";


    /**
     * Get the specific price by region.
     *
     * @param prices price list
     * @param region region
     * @return the specific price.
     */
    public static Price getSpecificPriceByRegion(List<PriceWithRegion> prices, String region) {
        if (CollectionUtils.isEmpty(prices) || StringUtils.isBlank(region)) {
            return null;
        }
        // Find the specific price by region
        Optional<PriceWithRegion> specificPrice = prices.stream()
                .filter(price -> price.getRegion().equalsIgnoreCase(region))
                .findFirst();
        if (specificPrice.isPresent()) {
            return specificPrice.get().getPrice();
        }
        // When the specific price is not found, return the price for the 'any' region or null.
        Optional<PriceWithRegion> anyPrice = prices.stream()
                .filter(price -> price.getRegion().equalsIgnoreCase(REGION_ANY))
                .findFirst();
        return anyPrice.map(PriceWithRegion::getPrice).orElse(null);
    }
}
