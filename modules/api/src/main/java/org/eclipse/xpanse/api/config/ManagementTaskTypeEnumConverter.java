/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.config;

import lombok.NonNull;
import org.eclipse.xpanse.modules.models.service.statemanagement.enums.ServiceStateManagementTaskType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Bean for serializing string in request parameters to ServiceStateManagementTaskType enum.
 */
@Component
public class ManagementTaskTypeEnumConverter implements
        Converter<String, ServiceStateManagementTaskType> {

    @Override
    public ServiceStateManagementTaskType convert(@NonNull String value) {
        return ServiceStateManagementTaskType.getByValue(value);
    }
}
