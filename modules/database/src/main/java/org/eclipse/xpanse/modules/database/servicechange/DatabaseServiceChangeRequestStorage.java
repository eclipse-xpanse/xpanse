/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.servicechange;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Implementation of the ServiceChangeRequestStorage. */
@Component
@Transactional
public class DatabaseServiceChangeRequestStorage implements ServiceChangeRequestStorage {

    private final ServiceChangeRequestRepository repository;

    @Autowired
    public DatabaseServiceChangeRequestStorage(ServiceChangeRequestRepository repository) {
        this.repository = repository;
    }

    @Override
    public ServiceChangeRequestEntity storeAndFlush(
            ServiceChangeRequestEntity serviceChangeRequestEntity) {
        return repository.saveAndFlush(serviceChangeRequestEntity);
    }

    @Override
    public <S extends ServiceChangeRequestEntity> List<S> saveAll(Iterable<S> entities) {
        return repository.saveAll(entities);
    }

    @Override
    public List<ServiceChangeRequestEntity> getServiceChangeRequestEntities(
            ServiceChangeRequestQueryModel requestQuery) {
        Specification<ServiceChangeRequestEntity> specification =
                (root, query, criteriaBuilder) -> {
                    List<Predicate> predicateList = new ArrayList<>();
                    if (Objects.nonNull(requestQuery.getOrderId())) {
                        predicateList.add(
                                criteriaBuilder.equal(
                                        root.get("serviceOrderEntity").get("orderId"),
                                        requestQuery.getOrderId()));
                    }

                    if (Objects.nonNull(requestQuery.getServiceId())) {
                        predicateList.add(
                                criteriaBuilder.equal(
                                        root.get("serviceDeploymentEntity").get("id"),
                                        requestQuery.getServiceId()));
                    }
                    if (StringUtils.isNotBlank(requestQuery.getResourceName())) {
                        predicateList.add(
                                criteriaBuilder.equal(
                                        root.get("resourceName"),
                                        StringUtils.lowerCase(requestQuery.getResourceName())));
                    }
                    if (StringUtils.isNotBlank(requestQuery.getConfigManager())) {
                        predicateList.add(
                                criteriaBuilder.equal(
                                        root.get("configManager"),
                                        StringUtils.lowerCase(requestQuery.getConfigManager())));
                    }
                    if (Objects.nonNull(requestQuery.getStatus())) {
                        predicateList.add(
                                criteriaBuilder.equal(
                                        root.get("status"), requestQuery.getStatus()));
                    }
                    return query.where(criteriaBuilder.and(predicateList.toArray(new Predicate[0])))
                            .getRestriction();
                };
        return repository.findAll(specification);
    }

    @Override
    public ServiceChangeRequestEntity findById(UUID changeId) {
        return repository.findById(changeId).orElse(null);
    }
}
