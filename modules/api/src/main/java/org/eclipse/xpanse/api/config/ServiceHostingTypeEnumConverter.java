/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.config;

import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/** Bean for serializing string in request parameters to ServiceHostingType enum. */
@Component
public class ServiceHostingTypeEnumConverter implements Converter<String, ServiceHostingType> {

    @Override
    public ServiceHostingType convert(String serviceHostingType) {
        return ServiceHostingType.getByValue(serviceHostingType);
    }
}
