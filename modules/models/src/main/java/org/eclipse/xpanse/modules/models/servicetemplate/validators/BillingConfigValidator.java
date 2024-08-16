/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.validators;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.billing.PriceWithRegion;
import org.eclipse.xpanse.modules.models.billing.ResourceUsage;
import org.eclipse.xpanse.modules.models.billing.enums.BillingMode;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceFlavor;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceFlavorWithPrice;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.InvalidServiceFlavorsException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Validator for billing configuration of service flavors.
 */
@Slf4j
@Component
public class BillingConfigValidator {

    /**
     * Validate service flavors.
     *
     * @param ocl ocl
     */
    public void validateServiceFlavors(Ocl ocl) {
        List<String> errors = new ArrayList<>();

        // Check if service flavor names are unique
        Map<String, Long> nameCountMap = ocl.getFlavors().getServiceFlavors().stream()
                .collect(Collectors.groupingBy(ServiceFlavor::getName, Collectors.counting()));
        nameCountMap.entrySet().stream().filter(entry -> entry.getValue() > 1)
                .forEach(entry -> {
                    String message = String.format("Service flavor with name %s is duplicated.",
                            entry.getKey());
                    log.error(message);
                    errors.add(message);
                });

        List<String> billingErrors = validateServiceBillingErrors(ocl);
        if (!CollectionUtils.isEmpty(billingErrors)) {
            errors.addAll(billingErrors);
        }

        if (!CollectionUtils.isEmpty(errors)) {
            throw new InvalidServiceFlavorsException(errors);
        }
    }


    private List<String> validateServiceBillingErrors(Ocl ocl) {
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
                        Map<String, Long> regionPricesCountMap = resourceUsage.getMarkUpPrices()
                                .stream().collect(Collectors.groupingBy(
                                        PriceWithRegion::getRegion, Collectors.counting()));
                        regionPricesCountMap.entrySet().stream()
                                .filter(entry -> entry.getValue() > 1)
                                .forEach(entry -> {
                                    String message = String.format(
                                            "Duplicated region %s in markUpPrices for flavor %s.",
                                            entry.getKey(), flavorName);
                                    log.error(message);
                                    errors.add(message);
                                });
                    }

                    if (!CollectionUtils.isEmpty(resourceUsage.getLicensePrices())) {
                        // Check if there are duplicated regions in licensePrices
                        Map<String, Long> regionPricesCountMap = resourceUsage.getLicensePrices()
                                .stream().collect(Collectors.groupingBy(
                                        PriceWithRegion::getRegion, Collectors.counting()));
                        regionPricesCountMap.entrySet().stream()
                                .filter(entry -> entry.getValue() > 1)
                                .forEach(entry -> {
                                    String message = String.format(
                                            "Duplicated region %s in licensePrices for flavor %s.",
                                            entry.getKey(), flavorName);
                                    log.error(message);
                                    errors.add(message);
                                });
                    }
                }
            }
            flavorNamesUnsupportedPayPerUse.forEach(flavorName -> {
                String message = String.format("Service flavor %s has no 'resourceUsage' "
                                + "defined in 'pricing' for the billing mode 'pay-per-use'.",
                        flavorName);
                log.error(message);
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
                    // Check if there are duplicated regions in fixedPrices
                    Map<String, Long> regionPricesCountMap =
                            fixedPrices.stream().collect(Collectors.groupingBy(
                                    PriceWithRegion::getRegion, Collectors.counting()));
                    regionPricesCountMap.entrySet().stream()
                            .filter(entry -> entry.getValue() > 1)
                            .forEach(entry -> {
                                String message = String.format(
                                        "Duplicated region %s in fixedPrices for flavor %s.",
                                        entry.getKey(), flavorName);
                                log.error(message);
                                errors.add(message);
                            });
                }
            }
            flavorNamesUnsupportedFixed.forEach(flavorName -> {
                String message = String.format("Service flavor %s has no 'fixedPrices' "
                                + "defined in 'pricing' for the billing mode 'fixed'.",
                        flavorName);
                log.error(message);
                errors.add(message);
            });
        }
        return errors;
    }
}
