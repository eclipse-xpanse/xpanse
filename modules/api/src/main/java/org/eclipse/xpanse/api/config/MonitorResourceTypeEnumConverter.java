/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.config;

import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Bean for serializing string in request parameters to MonitorResourceType enum.
 */
@Component
public class MonitorResourceTypeEnumConverter implements Converter<String, MonitorResourceType> {

    @Override
    public MonitorResourceType convert(String monitorResourceType) {
        return MonitorResourceType.getByValue(monitorResourceType);
    }
}
