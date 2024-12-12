/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.servicetemplate.price;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.billing.PriceWithRegion;
import org.eclipse.xpanse.modules.models.billing.ResourceUsage;
import org.eclipse.xpanse.modules.models.billing.enums.BillingMode;
import org.eclipse.xpanse.modules.models.billing.exceptions.InvalidBillingConfigException;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceFlavorWithPrice;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** Validator for billing config of service flavors. */
@Slf4j
@Component
public class BillingConfigValidator {

    /**
     * Validate the billing config.
     *
     * @param ocl ocl object
     */
    public void validateBillingConfig(Ocl ocl) {
        List<String> errors = new ArrayList<>();
        List<ServiceFlavorWithPrice> serviceFlavors = ocl.getFlavors().getServiceFlavors();
        List<BillingMode> billingModes = ocl.getBilling().getBillingModes();
        boolean isSupportedPayPerUse = billingModes.contains(BillingMode.PAY_PER_USE);
        boolean isSupportedFixed = billingModes.contains(BillingMode.FIXED);
        if (isSupportedPayPerUse) {
            // Check if resourceUsage is defined for each flavor supporting pay-per-use
            Set<String> flavorNamesUnsupportedPayPerUse = new HashSet<>();
            for (ServiceFlavorWithPrice serviceFlavor : serviceFlavors) {
                String flavorName = serviceFlavor.getName();
                ResourceUsage resourceUsage = serviceFlavor.getPricing().getResourceUsage();
                if (Objects.isNull(resourceUsage)) {
                    flavorNamesUnsupportedPayPerUse.add(flavorName);
                } else {
                    if (!CollectionUtils.isEmpty(resourceUsage.getMarkUpPrices())) {
                        // Check if there are duplicated regions in markUpPrices
                        Set<String> regionInSites = new HashSet<>();
                        resourceUsage
                                .getMarkUpPrices()
                                .forEach(
                                        price -> {
                                            String regionInSite =
                                                    price.getRegionName()
                                                            + ":"
                                                            + price.getSiteName();
                                            if (regionInSites.contains(regionInSite)) {
                                                String message =
                                                        String.format(
                                                                "Duplicated items with regionName:"
                                                                    + " %s and siteName: %s in"
                                                                    + " markUpPrices for the flavor"
                                                                    + " with name: %s.",
                                                                price.getRegionName(),
                                                                price.getSiteName(),
                                                                flavorName);
                                                errors.add(message);
                                            } else {
                                                regionInSites.add(regionInSite);
                                            }
                                        });
                    }
                    if (!CollectionUtils.isEmpty(resourceUsage.getLicensePrices())) {
                        // Check if there are duplicate items with region-site in licensePrices
                        Set<String> regionInSites = new HashSet<>();
                        resourceUsage
                                .getLicensePrices()
                                .forEach(
                                        price -> {
                                            String regionInSite =
                                                    price.getRegionName()
                                                            + ":"
                                                            + price.getSiteName();
                                            if (regionInSites.contains(regionInSite)) {
                                                String message =
                                                        String.format(
                                                                "Duplicated items with regionName:"
                                                                        + " %s and siteName: %s in"
                                                                        + " licensePrices for the"
                                                                        + " flavor with name: %s.",
                                                                price.getRegionName(),
                                                                price.getSiteName(),
                                                                flavorName);
                                                errors.add(message);
                                            } else {
                                                regionInSites.add(regionInSite);
                                            }
                                        });
                    }
                }
            }
            flavorNamesUnsupportedPayPerUse.forEach(
                    flavorName -> {
                        String message =
                                String.format(
                                        "Service flavor %s has no 'resourceUsage' defined in"
                                                + " 'pricing' for the billing mode 'pay-per-use'.",
                                        flavorName);
                        errors.add(message);
                    });
        }

        if (isSupportedFixed) {
            // Check if fixedPrices is defined for each flavor supporting fixed
            Set<String> flavorNamesUnsupportedFixed = new HashSet<>();
            for (ServiceFlavorWithPrice serviceFlavor : serviceFlavors) {
                String flavorName = serviceFlavor.getName();
                List<PriceWithRegion> fixedPrices = serviceFlavor.getPricing().getFixedPrices();
                if (CollectionUtils.isEmpty(fixedPrices)) {
                    flavorNamesUnsupportedFixed.add(flavorName);
                } else {
                    // Check if there are duplicate items with region-site in fixedPrices
                    Set<String> regionInSites = new HashSet<>();
                    fixedPrices.forEach(
                            price -> {
                                String regionInSite =
                                        price.getRegionName() + ":" + price.getSiteName();
                                if (regionInSites.contains(regionInSite)) {
                                    String message =
                                            String.format(
                                                    "Duplicated items with regionName: %s and"
                                                            + " siteName: %s in fixedPrices for the"
                                                            + " flavor with name: %s.",
                                                    price.getRegionName(),
                                                    price.getSiteName(),
                                                    flavorName);
                                    errors.add(message);
                                } else {
                                    regionInSites.add(regionInSite);
                                }
                            });
                }
            }
            flavorNamesUnsupportedFixed.forEach(
                    flavorName -> {
                        String message =
                                String.format(
                                        "Service flavor %s has no 'fixedPrices' defined in"
                                                + " 'pricing' for the billing mode 'fixed'.",
                                        flavorName);
                        log.error(message);
                        errors.add(message);
                    });
        }
        if (!CollectionUtils.isEmpty(errors)) {
            throw new InvalidBillingConfigException(errors);
        }
    }
}
