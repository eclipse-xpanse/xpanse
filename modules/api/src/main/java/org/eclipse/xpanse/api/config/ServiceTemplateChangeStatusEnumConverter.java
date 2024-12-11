/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.config;

import jakarta.annotation.Nonnull;
import org.eclipse.xpanse.modules.models.servicetemplate.request.enums.ServiceTemplateRequestStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Bean for serializing string in request parameters to ServiceTemplateRequestStatus enum.
 */
@Component
public class ServiceTemplateChangeStatusEnumConverter
        implements Converter<String, ServiceTemplateRequestStatus> {

    @Override
    public ServiceTemplateRequestStatus convert(@Nonnull String type) {
        return ServiceTemplateRequestStatus.getByValue(type);
    }
}
