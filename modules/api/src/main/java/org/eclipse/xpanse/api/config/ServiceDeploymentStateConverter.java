/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.config;

import jakarta.annotation.Nonnull;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Bean for serializing string in request parameters to ServiceDeploymentState.
 */
@Component
public class ServiceDeploymentStateConverter implements Converter<String, ServiceDeploymentState> {

    @Override
    public ServiceDeploymentState convert(@Nonnull String status) {
        return ServiceDeploymentState.getByValue(status);
    }
}
