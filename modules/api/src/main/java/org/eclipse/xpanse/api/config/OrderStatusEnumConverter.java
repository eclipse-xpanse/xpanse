/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.config;

import jakarta.annotation.Nonnull;
import org.eclipse.xpanse.modules.models.service.enums.OrderStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/** Bean for serializing string in request parameters to OrderStatus enum. */
@Component
public class OrderStatusEnumConverter implements Converter<String, OrderStatus> {

    @Override
    public OrderStatus convert(@Nonnull String orderStatus) {
        return OrderStatus.getByValue(orderStatus);
    }
}
