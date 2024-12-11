/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.openstack.common.price;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.billing.FlavorPriceResult;
import org.eclipse.xpanse.modules.models.billing.Price;
import org.eclipse.xpanse.modules.models.billing.ResourceUsage;
import org.eclipse.xpanse.modules.models.billing.enums.BillingMode;
import org.eclipse.xpanse.modules.orchestrator.price.ServiceFlavorPriceRequest;
import org.eclipse.xpanse.modules.servicetemplate.price.BillingCommonUtils;
import org.springframework.stereotype.Component;

/** Class that implements the price calculation for Openstack. */
@Slf4j
@Component
public class OpenstackServicePriceCalculator {

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
        Price markUpPrice =
                BillingCommonUtils.getSpecificPriceByRegionAndSite(
                        resourceUsage.getMarkUpPrices(),
                        request.getRegionName(),
                        request.getSiteName());
        BillingCommonUtils.addExtraPaymentPrice(flavorPriceResult, markUpPrice);
        // Add license price if not null
        Price licensePrice =
                BillingCommonUtils.getSpecificPriceByRegionAndSite(
                        resourceUsage.getLicensePrices(),
                        request.getRegionName(),
                        request.getSiteName());
        BillingCommonUtils.addExtraPaymentPrice(flavorPriceResult, licensePrice);
        flavorPriceResult.setFlavorName(request.getFlavorName());
        flavorPriceResult.setBillingMode(request.getBillingMode());
        flavorPriceResult.setSuccessful(true);
        return flavorPriceResult;
    }

    private FlavorPriceResult getServiceFlavorPriceWithFixed(ServiceFlavorPriceRequest request) {
        Price fixedPrice =
                BillingCommonUtils.getSpecificPriceByRegionAndSite(
                        request.getFlavorRatingMode().getFixedPrices(),
                        request.getRegionName(),
                        request.getSiteName());
        FlavorPriceResult flavorPriceResult = new FlavorPriceResult();
        flavorPriceResult.setRecurringPrice(fixedPrice);
        flavorPriceResult.setFlavorName(request.getFlavorName());
        flavorPriceResult.setBillingMode(request.getBillingMode());
        flavorPriceResult.setSuccessful(true);
        return flavorPriceResult;
    }
}
