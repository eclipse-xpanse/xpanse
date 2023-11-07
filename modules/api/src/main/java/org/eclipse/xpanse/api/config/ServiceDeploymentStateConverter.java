/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.config;

import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceDeploymentState;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Bean for serializing string in request parameters to ServiceDeploymentState.
 */
@Component
public class ServiceDeploymentStateConverter implements Converter<String, ServiceDeploymentState> {

    @Override
    public ServiceDeploymentState convert(String status) {
        return ServiceDeploymentState.getByValue(status);
    }
}
