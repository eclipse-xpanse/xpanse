/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.config;

import lombok.NonNull;
import org.eclipse.xpanse.modules.models.billing.enums.PricingPeriod;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Bean for serializing string in request parameters to PricingPeriod enum.
 */
@Component
public class PricingPeriodEnumConverter implements Converter<String, PricingPeriod> {

    @Override
    public PricingPeriod convert(@NonNull String value) {
        return PricingPeriod.getByValue(value);
    }
}
