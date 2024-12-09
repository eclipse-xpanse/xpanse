/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.servicetemplatehistory;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.servicetemplate.change.exceptions.ServiceTemplateChangeRequestNotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
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

    @Override
    public ServiceTemplateHistoryEntity getEntityById(UUID changeId) {
        return repository.findById(changeId).orElseThrow(() ->
                new ServiceTemplateChangeRequestNotFound(
                        "Service template change request with id " + changeId + " not found.")
        );
    }

    @Override
    public List<ServiceTemplateHistoryEntity> listServiceTemplateHistoryByQueryModel(
            ServiceTemplateHistoryQueryModel queryModel) {
        Specification<ServiceTemplateHistoryEntity> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicateList = new ArrayList<>();
            if (Objects.nonNull(queryModel.getServiceTemplateId())) {
                predicateList.add(criteriaBuilder.equal(root.get("serviceTemplate").get("id"),
                        queryModel.getServiceTemplateId()));
            }
            if (Objects.nonNull(queryModel.getCsp())) {
                predicateList.add(criteriaBuilder.equal(root.get("serviceTemplate").get("csp"),
                        queryModel.getCsp()));
            }
            if (Objects.nonNull(queryModel.getRequestType())) {
                predicateList.add(criteriaBuilder.equal(root.get("requestType"),
                        queryModel.getRequestType()));
            }
            if (Objects.nonNull(queryModel.getStatus())) {
                predicateList.add(
                        criteriaBuilder.equal(root.get("status"), queryModel.getStatus()));
            }
            query.distinct(true);
            query.orderBy(criteriaBuilder.asc(root.get("createTime")));
            return criteriaBuilder.and(predicateList.toArray(new Predicate[0]));
        };

        return repository.findAll(spec);
    }
}
