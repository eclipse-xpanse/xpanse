/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.servicestatemanagement;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.service.statemanagement.exceptions.ServiceStateManagementTaskNotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Bean to manage all service task to database.
 */
@Slf4j
@Component
@Transactional
public class DatabaseServiceStateManagementTaskStorage
        implements ServiceStateManagementTaskStorage {

    private final ServiceStateManagementTaskRepository repository;

    @Autowired
    public DatabaseServiceStateManagementTaskStorage(
            ServiceStateManagementTaskRepository repository) {
        this.repository = repository;
    }

    @Override
    public ServiceStateManagementTaskEntity storeAndFlush(
            ServiceStateManagementTaskEntity managementTaskEntity) {
        return repository.saveAndFlush(managementTaskEntity);
    }

    @Override
    public List<ServiceStateManagementTaskEntity> queryTasks(
            ServiceStateManagementTaskEntity taskQuery) {
        Specification<ServiceStateManagementTaskEntity> specification =
                (root, query, criteriaBuilder) -> {
                    List<Predicate> predicateList = new ArrayList<>();
                    if (Objects.nonNull(taskQuery.getServiceId())) {
                        predicateList.add(criteriaBuilder.equal(root.get("serviceId"),
                                taskQuery.getServiceId()));
                    }
                    if (Objects.nonNull(taskQuery.getTaskType())) {
                        predicateList.add(criteriaBuilder.equal(root.get("taskType"),
                                taskQuery.getTaskType()));
                    }
                    if (Objects.nonNull(taskQuery.getTaskStatus())) {
                        predicateList.add(criteriaBuilder.equal(root.get("taskStatus"),
                                taskQuery.getTaskStatus()));
                    }
                    query.orderBy(criteriaBuilder.desc(root.get("startedTime")));
                    query.distinct(true);
                    return query.where(criteriaBuilder.and(predicateList.toArray(new Predicate[0])))
                            .getRestriction();
                };

        return repository.findAll(specification);
    }

    @Override
    public ServiceStateManagementTaskEntity getTaskById(UUID uuid) {
        Optional<ServiceStateManagementTaskEntity> optional = repository.findById(uuid);
        return optional.orElseThrow(() -> new ServiceStateManagementTaskNotFound(
                String.format("Service state management task with id %s not found.", uuid)
        ));
    }

    @Override
    public void batchRemove(List<ServiceStateManagementTaskEntity> taskEntities) {
        repository.deleteAll(taskEntities);
    }

    @Override
    public void remove(ServiceStateManagementTaskEntity taskEntity) {
        repository.delete(taskEntity);
    }
}
