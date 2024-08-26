/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.servicetemplate.price;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.billing.FlavorPriceResult;
import org.eclipse.xpanse.modules.models.billing.Price;
import org.eclipse.xpanse.modules.models.billing.PriceWithRegion;
import org.eclipse.xpanse.modules.models.billing.enums.PricingPeriod;
import org.springframework.util.CollectionUtils;

/**
 * Defines common methods for service billing.
 */
public class BillingCommonUtils {

    private static final String ANY_REGION = "any";
    private static final BigDecimal HOURS_PER_DAY = BigDecimal.valueOf(24L);
    private static final BigDecimal HOURS_PER_MONTH = BigDecimal.valueOf(24 * 30L);
    private static final BigDecimal HOURS_PER_YEAR = BigDecimal.valueOf(24 * 365L);
    private static final int SCALE = 4;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;


    /**
     * Get the specific price by region.
     *
     * @param prices price list
     * @param region region
     * @return the specific price.
     */
    public static Price getSpecificPriceByRegionAndSite(List<PriceWithRegion> prices,
                                                        String region, String site) {
        if (CollectionUtils.isEmpty(prices)
                || StringUtils.isBlank(region) || StringUtils.isEmpty(site)) {
            return null;
        }
        // Find the specific price by target region and target site.
        Optional<PriceWithRegion> specificPrice = prices.stream()
                .filter(price -> StringUtils.equalsIgnoreCase(region, price.getRegionName())
                        && StringUtils.equalsIgnoreCase(site, price.getSiteName()))
                .findFirst();
        if (specificPrice.isPresent()) {
            return specificPrice.get().getPrice();
        }
        // Find the specific price by 'any' region and target site.
        Optional<PriceWithRegion> anyPrice = prices.stream()
                .filter(price -> StringUtils.equalsIgnoreCase(ANY_REGION, price.getRegionName())
                        && StringUtils.equalsIgnoreCase(site, price.getSiteName()))
                .findFirst();
        return anyPrice.map(PriceWithRegion::getPrice).orElse(null);
    }


    /**
     * Add extra payment price to flavor price result.
     *
     * @param flavorPriceResult flavor price result
     * @param price             extra payment price
     */
    public static void addExtraPaymentPrice(FlavorPriceResult flavorPriceResult, Price price) {
        if (Objects.nonNull(price)) {
            if (PricingPeriod.ONE_TIME == price.getPeriod()) {
                if (Objects.nonNull(flavorPriceResult.getOneTimePaymentPrice())) {
                    flavorPriceResult.getOneTimePaymentPrice().setCost(
                            flavorPriceResult.getOneTimePaymentPrice().getCost()
                                    .add(price.getCost()));
                } else {
                    Price oneTimePaymentPrice = new Price();
                    oneTimePaymentPrice.setCost(price.getCost());
                    oneTimePaymentPrice.setCurrency(price.getCurrency());
                    oneTimePaymentPrice.setPeriod(PricingPeriod.ONE_TIME);
                    flavorPriceResult.setOneTimePaymentPrice(oneTimePaymentPrice);
                }
            } else {
                BigDecimal costPerHour = getAmountPerHour(price);
                if (Objects.nonNull(flavorPriceResult.getRecurringPrice())) {
                    flavorPriceResult.getRecurringPrice().setCost(
                            flavorPriceResult.getRecurringPrice().getCost().add(costPerHour));
                } else {
                    Price recurringPrice = new Price();
                    recurringPrice.setCost(costPerHour);
                    recurringPrice.setCurrency(price.getCurrency());
                    recurringPrice.setPeriod(PricingPeriod.HOURLY);
                    flavorPriceResult.setRecurringPrice(recurringPrice);
                }
            }
        }
    }


    private static BigDecimal getAmountPerHour(Price price) {
        if (Objects.isNull(price) || Objects.isNull(price.getCost())
                || Objects.isNull(price.getPeriod())) {
            return BigDecimal.ZERO;
        }
        BigDecimal periodCost = price.getCost();
        return switch (price.getPeriod()) {
            case YEARLY -> periodCost.divide(HOURS_PER_YEAR, SCALE, ROUNDING_MODE);
            case MONTHLY -> periodCost.divide(HOURS_PER_MONTH, SCALE, ROUNDING_MODE);
            case DAILY -> periodCost.divide(HOURS_PER_DAY, SCALE, ROUNDING_MODE);
            default -> periodCost.setScale(SCALE, ROUNDING_MODE);
        };
    }
}
