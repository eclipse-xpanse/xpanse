/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import jakarta.annotation.Resource;
import org.eclipse.xpanse.modules.database.serviceconfiguration.ServiceConfigurationEntity;
import org.eclipse.xpanse.modules.database.serviceconfiguration.ServiceConfigurationStorage;
import org.springframework.stereotype.Component;

/**
 * Bean to manage service configuration.
 */
@Component
public class ServiceConfigurationManager {

    @Resource
    private ServiceConfigurationStorage serviceConfigurationStorage;

    /**
     * Create new service configuration.
     *
     * @param serviceConfigurationEntity service configuration entity
     * @return DB entity of the service configuration.
     */
    public ServiceConfigurationEntity storeAndFlush(
            ServiceConfigurationEntity serviceConfigurationEntity) {
        return serviceConfigurationStorage.storeAndFlush(serviceConfigurationEntity);
    }

    public void deleteServiceConfiguration(ServiceConfigurationEntity serviceConfigurationEntity) {
        serviceConfigurationStorage.deleteServiceConfiguration(serviceConfigurationEntity);
    }
}
