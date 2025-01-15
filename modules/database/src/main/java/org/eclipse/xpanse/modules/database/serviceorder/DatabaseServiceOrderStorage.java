/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.serviceorder;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.models.service.order.exceptions.ServiceOrderNotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Bean to manage all service task to database. */
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
    public ServiceOrderEntity storeAndFlush(ServiceOrderEntity serviceOrderEntity) {
        checkEntityData(serviceOrderEntity);
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
                        predicateList.add(
                                criteriaBuilder.equal(
                                        root.get("serviceDeploymentEntity").get("id"),
                                        entity.getServiceDeploymentEntity().getId()));
                    }
                    if (Objects.nonNull(entity.getTaskType())) {
                        predicateList.add(
                                criteriaBuilder.equal(root.get("taskType"), entity.getTaskType()));
                    }
                    if (Objects.nonNull(entity.getTaskStatus())) {
                        predicateList.add(
                                criteriaBuilder.equal(
                                        root.get("taskStatus"), entity.getTaskStatus()));
                    }
                    if (Objects.nonNull(entity.getParentOrderId())) {
                        predicateList.add(
                                criteriaBuilder.equal(
                                        root.get("parentOrderId"), entity.getParentOrderId()));
                    }
                    if (Objects.nonNull(entity.getWorkflowId())) {
                        predicateList.add(
                                criteriaBuilder.equal(
                                        root.get("workflowId"), entity.getWorkflowId()));
                    }
                    assert query != null;
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
        return optional.orElseThrow(
                () ->
                        new ServiceOrderNotFound(
                                String.format("Service order with id %s not found.", uuid)));
    }

    @Override
    public ServiceDeploymentEntity getServiceDeploymentByOrderId(UUID uuid) {
        Optional<ServiceOrderEntity> optional = repository.findById(uuid);
        if (optional.isEmpty()) {
            throw new ServiceOrderNotFound(
                    String.format("Service order with id %s not found.", uuid));
        }
        if (Objects.isNull(optional.get().getServiceDeploymentEntity())) {
            throw new ServiceNotDeployedException(
                    String.format("No service related to order with id %s.", uuid));
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

    private void checkEntityData(ServiceOrderEntity entity) {
        TaskStatus taskStatus = entity.getTaskStatus();
        if (taskStatus == TaskStatus.CREATED || taskStatus == TaskStatus.IN_PROGRESS) {
            return;
        }
        if (Objects.isNull(entity.getCompletedTime())) {
            String errorMsg =
                    "The completed time could not be null when the order task is completed.";
            log.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        if (taskStatus == TaskStatus.FAILED) {
            if (Objects.isNull(entity.getErrorResponse())) {
                String errorMsg =
                        "The error response could not be null when the order task is failed.";
                log.error(errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }
        }
        ServiceOrderType taskType = entity.getTaskType();
        Map<String, Object> resultProperties = entity.getResultProperties();
        // Check necessary to ensure the developer handles the result properties for new types
        // introduced.
        switch (taskType) {
            case DEPLOY:
            case RETRY:
            case MODIFY:
            case PORT:
            case RECREATE:
            case DESTROY:
            case ROLLBACK:
            case PURGE:
                if (Objects.isNull(resultProperties) || resultProperties.isEmpty()) {
                    String errorMsg =
                            String.format(
                                    "The result properties of task with type %s could not be"
                                            + " empty.",
                                    taskType.toValue());
                    log.error(errorMsg);
                    throw new IllegalArgumentException(errorMsg);
                }
                break;
            default:
                break;
        }
    }
}
