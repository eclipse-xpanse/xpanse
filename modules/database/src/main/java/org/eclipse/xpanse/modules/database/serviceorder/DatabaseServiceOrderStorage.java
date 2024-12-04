/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.serviceorder;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.service.order.exceptions.ServiceOrderNotFound;
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
public class DatabaseServiceOrderStorage implements ServiceOrderStorage {

    private final ServiceOrderRepository repository;

    @Autowired
    public DatabaseServiceOrderStorage(ServiceOrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public ServiceOrderEntity storeAndFlush(
            ServiceOrderEntity serviceOrderEntity) {
        return repository.saveAndFlush(serviceOrderEntity);
    }

    @Override
    public List<ServiceOrderEntity> queryEntities(ServiceOrderEntity entity) {
        if (Objects.isNull(entity)) {
            return new ArrayList<>();
        }
        Specification<ServiceOrderEntity> specification =
                (root, query, criteriaBuilder) -> {
                    List<Predicate> predicateList = new ArrayList<>();
                    if (Objects.nonNull(entity.getServiceDeploymentEntity())
                            && Objects.nonNull(entity.getServiceDeploymentEntity().getId())) {
                        predicateList.add(criteriaBuilder.equal(root
                                        .get("serviceDeploymentEntity").get("id"),
                                entity.getServiceDeploymentEntity().getId()));
                    }
                    if (Objects.nonNull(entity.getTaskType())) {
                        predicateList.add(criteriaBuilder.equal(root.get("taskType"),
                                entity.getTaskType()));
                    }
                    if (Objects.nonNull(entity.getTaskStatus())) {
                        predicateList.add(criteriaBuilder.equal(root.get("taskStatus"),
                                entity.getTaskStatus()));
                    }
                    if (Objects.nonNull(entity.getParentOrderId())) {
                        predicateList.add(criteriaBuilder.equal(root.get("parentOrderId"),
                                entity.getParentOrderId()));
                    }
                    if (Objects.nonNull(entity.getWorkflowId())) {
                        predicateList.add(criteriaBuilder.equal(root.get("workflowId"),
                                entity.getWorkflowId()));
                    }
                    query.orderBy(criteriaBuilder.desc(root.get("startedTime")));
                    query.distinct(true);
                    return query.where(criteriaBuilder.and(predicateList.toArray(new Predicate[0])))
                            .getRestriction();
                };
        return repository.findAll(specification);
    }

    @Override
    public ServiceOrderEntity getEntityById(UUID uuid) {
        Optional<ServiceOrderEntity> optional = repository.findById(uuid);
        return optional.orElseThrow(() -> new ServiceOrderNotFound(
                String.format("Service order with id %s not found.", uuid)
        ));
    }

    @Override
    public ServiceDeploymentEntity getServiceDeploymentByOrderId(UUID uuid) {
        Optional<ServiceOrderEntity> optional = repository.findById(uuid);
        if (optional.isEmpty()) {
            throw new ServiceOrderNotFound(
                    String.format("Service order with id %s not found.", uuid)
            );
        }
        if (Objects.isNull(optional.get().getServiceDeploymentEntity())) {
            throw new ServiceNotDeployedException(
                    String.format("No service related to order with id %s.", uuid)
            );
        }
        return optional.get().getServiceDeploymentEntity();
    }

    @Override
    public void deleteBatch(List<ServiceOrderEntity> taskEntities) {
        repository.deleteAllInBatch(taskEntities);
    }

    @Override
    public void delete(ServiceOrderEntity taskEntity) {
        repository.delete(taskEntity);
    }
}
