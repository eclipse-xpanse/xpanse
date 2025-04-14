/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.serviceobject;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.servicechange.ServiceChangeRequestEntity;
import org.eclipse.xpanse.modules.models.serviceobject.exceptions.ServiceObjectNotFoundException;
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
        return repository
                .findById(objectId)
                .orElseThrow(
                        () ->
                                new ServiceObjectNotFoundException(
                                        String.format(
                                                "Service object with id %s not found.", objectId)));
    }

    @Override
    public List<ServiceObjectEntity> getEntitiesByIds(List<UUID> objectIds) {
        return repository.findAllById(objectIds);
    }

    @Override
    public Set<UUID> getObjectIdsByDependentObjectId(UUID dependentObjectId) {
        return repository.findObjectIdsByDependentObjectId(dependentObjectId);
    }

    @Override
    public List<ServiceObjectEntity> getObjectsByServiceId(UUID serviceId) {
        return repository.findAll(
                (root, query, cb) -> {
                    Join<ServiceChangeRequestEntity, ServiceDeploymentEntity> deploymentJoin =
                            root.join("serviceDeploymentEntity", JoinType.INNER);
                    Predicate serviceIdPredicate = cb.equal(deploymentJoin.get("id"), serviceId);
                    query.distinct(true);
                    return cb.and(serviceIdPredicate);
                });
    }
}
