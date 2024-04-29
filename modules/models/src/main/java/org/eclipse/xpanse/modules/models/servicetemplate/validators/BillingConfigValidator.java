/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.validators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
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
        List<ServiceFlavorWithPrice> serviceFlavors = ocl.getFlavors().getServiceFlavors();
        Map<String, Long> nameCountMap = serviceFlavors.stream()
                .collect(Collectors.groupingBy(ServiceFlavor::getName, Collectors.counting()));
        nameCountMap.entrySet().stream().filter(entry -> entry.getValue() > 1).forEach(entry -> {
            String message =
                    String.format("Service flavor with name %s is duplicated.", entry.getKey());
            log.error(message);
            errors.add(message);
        });
        List<BillingMode> billingModes = ocl.getBilling().getBillingModes();

        boolean isSupportedPayPerUse = billingModes.contains(BillingMode.PAY_PER_USE);
        boolean isSupportedFixed = billingModes.contains(BillingMode.FIXED);
        if (isSupportedPayPerUse) {
            Set<String> missingResourceUsageFlavorNames =
                    serviceFlavors.stream().filter(serviceFlavor ->
                                    Objects.isNull(serviceFlavor.getPricing().getResourceUsage()))
                            .map(ServiceFlavorWithPrice::getName).collect(Collectors.toSet());
            missingResourceUsageFlavorNames.forEach(flavorName -> {
                String message = String.format("Service flavor %s has no 'resourceUsage' "
                                + "defined in 'pricing' for the billing mode 'pay-per-use'.",
                        flavorName);
                log.error(message);
                errors.add(message);
            });
        }
        if (isSupportedFixed) {
            Set<String> missingFixedPriceFlavorNames =
                    serviceFlavors.stream().filter(serviceFlavor ->
                                    Objects.isNull(serviceFlavor.getPricing().getFixedPrice()))
                            .map(ServiceFlavorWithPrice::getName).collect(Collectors.toSet());
            missingFixedPriceFlavorNames.forEach(flavorName -> {
                String message = String.format("Service flavor %s has no 'fixedPrice' "
                                + "defined in 'pricing' for the billing mode 'fixed'.",
                        flavorName);
                log.error(message);
                errors.add(message);
            });
        }
        if (!CollectionUtils.isEmpty(errors)) {
            throw new InvalidServiceFlavorsException(errors);
        }
    }
}
