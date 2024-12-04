/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.config;

import jakarta.annotation.Nonnull;
import org.eclipse.xpanse.modules.models.servicetemplate.change.enums.ServiceTemplateChangeStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Bean for serializing string in request parameters to ServiceTemplateChangeStatus enum.
 */
@Component
public class ServiceTemplateChangeStatusEnumConverter
        implements Converter<String, ServiceTemplateChangeStatus> {

    @Override
    public ServiceTemplateChangeStatus convert(@Nonnull String type) {
        return ServiceTemplateChangeStatus.getByValue(type);
    }
}
