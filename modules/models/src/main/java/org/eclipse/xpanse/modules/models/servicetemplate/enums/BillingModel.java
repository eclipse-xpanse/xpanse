/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;


/**
 * Model enum of Billing.
 */
public enum BillingModel {

    YEARLY("yearly"),
    MONTHLY("monthly"),
    DAILY("daily"),
    HOURLY("hourly"),
    PAY_PER_USE("pay_per_use");

    private final String model;

    BillingModel(String model) {
        this.model = model;
    }

    /**
     * For BillingModel serialize.
     */
    @JsonCreator
    public static BillingModel getByValue(String model) {
        for (BillingModel billingModel : values()) {
            if (billingModel.model.equals(StringUtils.lowerCase(model))) {
                return billingModel;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("BillingModel value %s is not supported.", model));
    }

    /**
     * For BillingModel deserialize.
     */
    @JsonValue
    public String toValue() {
        return this.model;
    }
}