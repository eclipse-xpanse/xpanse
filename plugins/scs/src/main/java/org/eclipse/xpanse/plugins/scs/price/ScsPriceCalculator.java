/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.scs.price;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.billing.FlavorPriceResult;
import org.eclipse.xpanse.modules.models.billing.Price;
import org.eclipse.xpanse.modules.models.billing.ResourceUsage;
import org.eclipse.xpanse.modules.models.billing.enums.BillingMode;
import org.eclipse.xpanse.modules.models.billing.enums.PricingPeriod;
import org.eclipse.xpanse.modules.orchestrator.price.ServicePriceRequest;
import org.springframework.stereotype.Component;

/**
 * Class that implements the price calculation for SCS.
 */
@Slf4j
@Component
public class ScsPriceCalculator {


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
    public FlavorPriceResult getServicePrice(ServicePriceRequest request) {
        if (request.getBillingMode() == BillingMode.PAY_PER_USE) {
            return getServicePriceWithPayPerUse(request);
        } else {
            return getServicePriceWithFixed(request);
        }
    }

    private FlavorPriceResult getServicePriceWithPayPerUse(ServicePriceRequest request) {
        ResourceUsage resourceUsage = request.getFlavorRatingMode().getResourceUsage();
        FlavorPriceResult flavorPriceResult = new FlavorPriceResult();
        // TODO Get recurring price with resource usage in future.
        // Add markup price if not null
        Price markUpPrice = resourceUsage.getMarkUpPrice();
        addExtraPaymentPrice(flavorPriceResult, markUpPrice);
        // Add license price if not null
        Price licensePrice = resourceUsage.getLicensePrice();
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

    private FlavorPriceResult getServicePriceWithFixed(ServicePriceRequest request) {
        Price fixedPrice = request.getFlavorRatingMode().getFixedPrice();
        FlavorPriceResult flavorPriceResult = new FlavorPriceResult();
        flavorPriceResult.setRecurringPrice(fixedPrice);
        return flavorPriceResult;
    }

}