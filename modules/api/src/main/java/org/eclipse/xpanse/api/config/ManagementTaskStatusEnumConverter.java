/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.config;

import lombok.NonNull;
import org.eclipse.xpanse.modules.models.service.statemanagement.enums.ManagementTaskStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Bean for serializing string in request parameters to ManagementTaskStatus enum.
 */
@Component
public class ManagementTaskStatusEnumConverter implements Converter<String, ManagementTaskStatus> {

    @Override
    public ManagementTaskStatus convert(@NonNull String value) {
        return ManagementTaskStatus.getByValue(value);
    }
}
