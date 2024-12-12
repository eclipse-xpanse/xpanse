/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.config;

import javax.annotation.Nonnull;
import org.eclipse.xpanse.modules.models.servicetemplate.request.enums.ServiceTemplateRequestType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/** Bean for serializing string in request parameters to ServiceTemplateRequestType enum. */
@Component
public class ServiceTemplateRequestTypeEnumConverter
        implements Converter<String, ServiceTemplateRequestType> {

    @Override
    public ServiceTemplateRequestType convert(@Nonnull String type) {
        return ServiceTemplateRequestType.getByValue(type);
    }
}
