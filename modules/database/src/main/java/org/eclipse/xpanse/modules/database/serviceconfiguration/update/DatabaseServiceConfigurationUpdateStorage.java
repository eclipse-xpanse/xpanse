/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.serviceconfiguration.update;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the ServiceConfigurationUpdateStorage.
 */
@Component
@Transactional
public class DatabaseServiceConfigurationUpdateStorage
        implements ServiceConfigurationUpdateStorage {

    private final ServiceConfigurationUpdateRepository serviceConfigurationUpdateRepository;

    @Autowired
    public DatabaseServiceConfigurationUpdateStorage(
            ServiceConfigurationUpdateRepository serviceConfigurationUpdateRepository) {
        this.serviceConfigurationUpdateRepository = serviceConfigurationUpdateRepository;
    }

    @Override
    public ServiceConfigurationUpdateRequest storeAndFlush(
            ServiceConfigurationUpdateRequest serviceConfigurationUpdateRequest) {
        return serviceConfigurationUpdateRepository.saveAndFlush(serviceConfigurationUpdateRequest);
    }
}
