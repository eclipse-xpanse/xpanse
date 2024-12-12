/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.config;

import jakarta.annotation.Nonnull;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/** Bean for serializing string in request parameters to ServiceOrderType enum. */
@Component
public class ServiceOrderTypeEnumConverter implements Converter<String, ServiceOrderType> {

    @Override
    public ServiceOrderType convert(@Nonnull String taskType) {
        return ServiceOrderType.getByValue(taskType);
    }
}
