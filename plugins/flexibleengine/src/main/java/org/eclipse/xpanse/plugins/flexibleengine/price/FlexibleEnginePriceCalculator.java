/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.flexibleengine.price;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.billing.FlavorPriceResult;
import org.eclipse.xpanse.modules.models.billing.Price;
import org.eclipse.xpanse.modules.models.billing.ResourceUsage;
import org.eclipse.xpanse.modules.models.billing.enums.BillingMode;
import org.eclipse.xpanse.modules.models.billing.enums.PricingPeriod;
import org.eclipse.xpanse.modules.models.billing.utils.BillingCommonUtils;
import org.eclipse.xpanse.modules.orchestrator.price.ServiceFlavorPriceRequest;
import org.springframework.stereotype.Component;

/**
 * Class that implements the price calculation for FlexibleEngine.
 */
@Slf4j
@Component
public class FlexibleEnginePriceCalculator {

    private static final BigDecimal HOURS_PER_DAY = BigDecimal.valueOf(24L);
    private static final BigDecimal HOURS_PER_MONTH = BigDecimal.valueOf(24 * 30L);
    private static final BigDecimal HOURS_PER_YEAR = BigDecimal.valueOf(24 * 365L);
    private static final int SCALE = 4;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * Get the price of the service.
     *
     * @param request service price request
     * @return price
     */
    public FlavorPriceResult getServiceFlavorPrice(ServiceFlavorPriceRequest request) {
        if (request.getBillingMode() == BillingMode.PAY_PER_USE) {
            return getServiceFlavorPriceWithPayPerUse(request);
        } else {
            return getServiceFlavorPriceWithFixed(request);
        }
    }

    private FlavorPriceResult getServiceFlavorPriceWithPayPerUse(
            ServiceFlavorPriceRequest request) {
        ResourceUsage resourceUsage = request.getFlavorRatingMode().getResourceUsage();
        FlavorPriceResult flavorPriceResult = new FlavorPriceResult();
        // TODO Get recurring price with resource usage in future.

        // Add markup price if not null
        Price markUpPrice = BillingCommonUtils.getSpecificPriceByRegion(
                resourceUsage.getMarkUpPrices(), request.getRegionName());
        addExtraPaymentPrice(flavorPriceResult, markUpPrice);
        // Add license price if not null
        Price licensePrice = BillingCommonUtils.getSpecificPriceByRegion(
                resourceUsage.getLicensePrices(), request.getRegionName());
        addExtraPaymentPrice(flavorPriceResult, licensePrice);
        return flavorPriceResult;
    }

    private void addExtraPaymentPrice(FlavorPriceResult flavorPriceResult, Price price) {
        if (Objects.nonNull(price)) {
            if (PricingPeriod.ONE_TIME == price.getPeriod()) {
                if (Objects.nonNull(flavorPriceResult.getOneTimePaymentPrice())) {
                    flavorPriceResult.getOneTimePaymentPrice().setCost(
                            flavorPriceResult.getOneTimePaymentPrice().getCost()
                                    .add(price.getCost()));
                } else {
                    Price oneTimePaymentPrice = new Price();
                    oneTimePaymentPrice.setRegion(price.getRegion());
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
                    recurringPrice.setRegion(price.getRegion());
                    recurringPrice.setCost(costPerHour);
                    recurringPrice.setCurrency(price.getCurrency());
                    recurringPrice.setPeriod(price.getPeriod());
                    flavorPriceResult.setRecurringPrice(recurringPrice);
                }
            }
        }
    }

    private BigDecimal getAmountPerHour(Price price) {
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

    private FlavorPriceResult getServiceFlavorPriceWithFixed(ServiceFlavorPriceRequest request) {
        Price fixedPrice = BillingCommonUtils.getSpecificPriceByRegion(
                request.getFlavorRatingMode().getFixedPrices(), request.getRegionName());
        FlavorPriceResult flavorPriceResult = new FlavorPriceResult();
        flavorPriceResult.setRecurringPrice(fixedPrice);
        return flavorPriceResult;
    }

}