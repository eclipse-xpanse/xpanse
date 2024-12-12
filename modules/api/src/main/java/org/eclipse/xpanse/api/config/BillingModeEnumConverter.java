/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.config;

import lombok.NonNull;
import org.eclipse.xpanse.modules.models.billing.enums.BillingMode;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/** Bean for serializing string in request parameters to BillingMode enum. */
@Component
public class BillingModeEnumConverter implements Converter<String, BillingMode> {

    @Override
    public BillingMode convert(@NonNull String value) {
        return BillingMode.getByValue(value);
    }
}
