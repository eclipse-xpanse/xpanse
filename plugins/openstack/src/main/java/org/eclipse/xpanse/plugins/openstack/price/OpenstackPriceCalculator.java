/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.openstack.price;

import java.math.BigDecimal;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.billing.Price;
import org.eclipse.xpanse.modules.models.billing.ResourceUsage;
import org.eclipse.xpanse.modules.models.billing.ServicePrice;
import org.eclipse.xpanse.modules.models.billing.enums.BillingMode;
import org.eclipse.xpanse.modules.models.billing.enums.PricingPeriod;
import org.eclipse.xpanse.modules.orchestrator.price.ServicePriceRequest;
import org.springframework.stereotype.Component;

/**
 * Class that implements the price calculation for Openstack.
 */
@Slf4j
@Component
public class OpenstackPriceCalculator {


    private static final BigDecimal HOURS_PER_DAY = BigDecimal.valueOf(24L);
    private static final BigDecimal HOURS_PER_MONTH = BigDecimal.valueOf(24 * 30L);
    private static final BigDecimal HOURS_PER_YEAR = BigDecimal.valueOf(24 * 365L);

    /**
     * Get the price of the service.
     *
     * @param request service price request
     * @return price
     */
    public ServicePrice getServicePrice(ServicePriceRequest request) {
        if (request.getBillingMode() == BillingMode.PAY_PER_USE) {
            return getServicePriceWithPayPerUse(request);
        } else {
            return getServicePriceWithFixed(request);
        }
    }

    private ServicePrice getServicePriceWithPayPerUse(ServicePriceRequest request) {
        ResourceUsage resourceUsage = request.getFlavorRatingMode().getResourceUsage();
        ServicePrice servicePrice = new ServicePrice();
        // TODO Get recurring price with resource usage in future.
        // Add markup price if not null
        Price markUpPrice = resourceUsage.getMarkUpPrice();
        addExtraPaymentPrice(servicePrice, markUpPrice);
        // Add license price if not null
        Price licensePrice = resourceUsage.getLicensePrice();
        addExtraPaymentPrice(servicePrice, licensePrice);
        return servicePrice;
    }


    private void addExtraPaymentPrice(ServicePrice servicePrice, Price price) {
        if (Objects.nonNull(price)) {
            if (PricingPeriod.ONE_TIME == price.getPeriod()) {
                if (Objects.nonNull(servicePrice.getOneTimePaymentPrice())) {
                    servicePrice.getOneTimePaymentPrice().setCost(
                            servicePrice.getOneTimePaymentPrice().getCost().add(price.getCost()));
                } else {
                    Price oneTimePaymentPrice = new Price();
                    oneTimePaymentPrice.setCost(price.getCost());
                    oneTimePaymentPrice.setCurrency(price.getCurrency());
                    oneTimePaymentPrice.setPeriod(PricingPeriod.ONE_TIME);
                    servicePrice.setOneTimePaymentPrice(oneTimePaymentPrice);
                }
            } else {
                BigDecimal costPerHour = getAmountPerHour(price);
                if (Objects.nonNull(servicePrice.getRecurringPrice())) {
                    servicePrice.getRecurringPrice().setCost(
                            servicePrice.getRecurringPrice().getCost().add(costPerHour));
                } else {
                    Price recurringPrice = new Price();
                    recurringPrice.setCost(costPerHour);
                    recurringPrice.setCurrency(price.getCurrency());
                    recurringPrice.setPeriod(price.getPeriod());
                    servicePrice.setRecurringPrice(recurringPrice);
                }
            }
        }
    }

    private BigDecimal getAmountPerHour(Price price) {
        BigDecimal cost = price.getCost();
        return switch (price.getPeriod()) {
            case YEARLY -> cost.divide(HOURS_PER_YEAR);
            case MONTHLY -> cost.divide(HOURS_PER_MONTH);
            case DAILY -> cost.divide(HOURS_PER_DAY);
            default -> cost;
        };
    }

    private ServicePrice getServicePriceWithFixed(ServicePriceRequest request) {
        Price fixedPrice = request.getFlavorRatingMode().getFixedPrice();
        ServicePrice servicePrice = new ServicePrice();
        servicePrice.setRecurringPrice(fixedPrice);
        return servicePrice;
    }
}