/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.serviceconfiguration;

import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Implementation of the ServiceConfigurationStorage. */
@Component
@Transactional
public class DatabaseServiceConfigurationStorage implements ServiceConfigurationStorage {

    private final ServiceConfigurationRepository serviceConfigurationRepository;

    @Autowired
    public DatabaseServiceConfigurationStorage(
            ServiceConfigurationRepository serviceConfigurationRepository) {
        this.serviceConfigurationRepository = serviceConfigurationRepository;
    }

    @Override
    public ServiceConfigurationEntity storeAndFlush(
            ServiceConfigurationEntity serviceConfigurationEntity) {
        return serviceConfigurationRepository.saveAndFlush(serviceConfigurationEntity);
    }

    @Override
    public ServiceConfigurationEntity findServiceConfigurationById(UUID id) {
        Optional<ServiceConfigurationEntity> optional =
                this.serviceConfigurationRepository.findById(id);
        return optional.orElse(null);
    }

    @Override
    public void deleteServiceConfiguration(ServiceConfigurationEntity serviceConfigurationEntity) {
        this.serviceConfigurationRepository.delete(serviceConfigurationEntity);
    }
}
