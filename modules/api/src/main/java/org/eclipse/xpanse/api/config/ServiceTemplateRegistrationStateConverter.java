/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.config;

import jakarta.annotation.Nullable;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceTemplateRegistrationState;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Bean for serializing string in request parameters to ServiceTemplateRegistrationState enum.
 */
@Component
public class ServiceTemplateRegistrationStateConverter
        implements Converter<String, ServiceTemplateRegistrationState> {
    @Override
    public ServiceTemplateRegistrationState convert(@Nullable String registrationState) {
        return ServiceTemplateRegistrationState.getByValue(registrationState);
    }
}
