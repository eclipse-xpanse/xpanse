/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.config;

import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceRegistrationState;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Bean for serializing string in request parameters to ServiceRegistrationState enum.
 */
@Component
public class ServiceRegistrationStateConverter
        implements Converter<String, ServiceRegistrationState> {
    @Override
    public ServiceRegistrationState convert(String serviceRegistrationState) {
        return ServiceRegistrationState.getByValue(serviceRegistrationState);
    }
}
