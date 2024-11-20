/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.servicetemplatehistory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements methods of ServiceTemplateHistoryStorage interface.
 */
@Slf4j
@Component
@Transactional
public class DatabaseServiceTemplateHistoryStorage implements ServiceTemplateHistoryStorage {

    private final ServiceTemplateHistoryRepository repository;

    @Autowired
    public DatabaseServiceTemplateHistoryStorage(
            ServiceTemplateHistoryRepository serviceTemplateHistoryRepository) {
        this.repository = serviceTemplateHistoryRepository;
    }

    @Override
    public ServiceTemplateHistoryEntity storeAndFlush(
            ServiceTemplateHistoryEntity serviceTemplateHistoryEntity) {
        return repository.saveAndFlush(serviceTemplateHistoryEntity);
    }


}
