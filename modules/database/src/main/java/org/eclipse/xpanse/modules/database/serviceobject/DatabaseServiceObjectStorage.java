/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.serviceobject;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Implementation of the ServiceObjectStorage. */
@Component
@Transactional
public class DatabaseServiceObjectStorage implements ServiceObjectStorage {

    private final ServiceObjectRepository repository;

    @Autowired
    public DatabaseServiceObjectStorage(ServiceObjectRepository repository) {
        this.repository = repository;
    }

    @Override
    public ServiceObjectEntity storeAndFlush(ServiceObjectEntity entity) {
        return repository.saveAndFlush(entity);
    }

    @Override
    public void delete(ServiceObjectEntity entity) {
        repository.delete(entity);
    }

    @Override
    public ServiceObjectEntity getEntityById(UUID objectId) {
        return repository.findById(objectId).orElse(null);
    }
}
